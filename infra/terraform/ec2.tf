# ECS-optimized Amazon Linux 2023 ARM AMI, looked up by SSM public parameter.
data "aws_ssm_parameter" "ecs_ami" {
  name = "/aws/service/ecs/optimized-ami/amazon-linux-2023/arm64/recommended/image_id"
}

resource "aws_key_pair" "host" {
  count      = var.ssh_pubkey == "" ? 0 : 1
  key_name   = "${var.name}-host"
  public_key = var.ssh_pubkey
}

# Separate data volume that survives instance replacement. Mongo and Redis
# bind-mount their data directories onto subdirectories of /var/lib/djvb-data.
resource "aws_ebs_volume" "data" {
  availability_zone = data.aws_subnet.host.availability_zone
  size              = var.data_volume_size_gb
  type              = "gp3"
  encrypted         = true

  tags = { Name = "${var.name}-data" }

  lifecycle {
    prevent_destroy = true
  }
}

locals {
  data_volume_device = "/dev/sdf" # NVMe rename happens inside the OS

  user_data = <<-USERDATA
    #!/bin/bash
    set -euxo pipefail

    # Join the ECS cluster.
    cat >> /etc/ecs/ecs.config <<EOF
    ECS_CLUSTER=${aws_ecs_cluster.this.name}
    ECS_ENABLE_CONTAINER_METADATA=true
    EOF

    # Wait for the data volume to attach and resolve its NVMe device name.
    DATA_DEV=""
    for i in $(seq 1 30); do
      for d in /dev/nvme*n1; do
        [ -e "$d" ] || continue
        # Skip the root volume (mounted at /).
        if findmnt -no SOURCE / | grep -q "$d"; then continue; fi
        DATA_DEV="$d"
        break
      done
      [ -n "$DATA_DEV" ] && break
      sleep 2
    done

    if [ -z "$DATA_DEV" ]; then
      echo "Could not find data EBS device" >&2
      exit 1
    fi

    # Format on first boot only.
    if ! blkid "$DATA_DEV" >/dev/null 2>&1; then
      mkfs.ext4 -L djvb-data "$DATA_DEV"
    fi

    mkdir -p /var/lib/djvb-data
    grep -q "/var/lib/djvb-data" /etc/fstab || \
      echo "LABEL=djvb-data /var/lib/djvb-data ext4 defaults,nofail 0 2" >> /etc/fstab
    mount -a

    mkdir -p /var/lib/djvb-data/mongo /var/lib/djvb-data/redis
    # mongo:6 image runs as uid 999, redis:7-alpine as uid 999 as well.
    chown -R 999:999 /var/lib/djvb-data/mongo /var/lib/djvb-data/redis
  USERDATA
}

resource "aws_launch_template" "host" {
  name_prefix   = "${var.name}-host-"
  image_id      = data.aws_ssm_parameter.ecs_ami.value
  instance_type = var.instance_type
  key_name      = var.ssh_pubkey == "" ? null : aws_key_pair.host[0].key_name

  iam_instance_profile {
    arn = aws_iam_instance_profile.ecs_instance.arn
  }

  network_interfaces {
    associate_public_ip_address = true
    security_groups             = [aws_security_group.host.id]
  }

  metadata_options {
    http_tokens   = "required"
    http_endpoint = "enabled"
  }

  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_size           = 8
      volume_type           = "gp3"
      encrypted             = true
      delete_on_termination = true
    }
  }

  user_data = base64encode(local.user_data)

  tag_specifications {
    resource_type = "instance"
    tags          = { Name = "${var.name}-host" }
  }
}

resource "aws_autoscaling_group" "host" {
  name                = "${var.name}-host"
  min_size            = 1
  max_size            = 1
  desired_capacity    = 1
  vpc_zone_identifier = [local.subnet_id]
  protect_from_scale_in = true

  launch_template {
    id      = aws_launch_template.host.id
    version = "$Latest"
  }

  tag {
    key                 = "Name"
    value               = "${var.name}-host"
    propagate_at_launch = true
  }

  tag {
    key                 = "AmazonECSManaged"
    value               = "true"
    propagate_at_launch = true
  }

  lifecycle {
    # ECS capacity provider manages this; ignore desired_capacity drift.
    ignore_changes = [desired_capacity]
  }
}

# Look up the single running instance so we can attach the EBS volume and EIP.
data "aws_instances" "host" {
  instance_state_names = ["running", "pending"]

  filter {
    name   = "tag:aws:autoscaling:groupName"
    values = [aws_autoscaling_group.host.name]
  }

  depends_on = [aws_autoscaling_group.host]
}

resource "aws_volume_attachment" "data" {
  device_name  = local.data_volume_device
  volume_id    = aws_ebs_volume.data.id
  instance_id  = data.aws_instances.host.ids[0]
  force_detach = true
  stop_instance_before_detaching = false
}

resource "aws_eip" "host" {
  domain = "vpc"
}

resource "aws_eip_association" "host" {
  instance_id   = data.aws_instances.host.ids[0]
  allocation_id = aws_eip.host.id
}
