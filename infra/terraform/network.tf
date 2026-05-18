# Use the default VPC + public subnets in the chosen region. Avoids the cost
# of NAT gateways and keeps this stack to a single AZ-agnostic host.
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  filter {
    name   = "default-for-az"
    values = ["true"]
  }
}

# Pick one AZ deterministically. The EBS data volume lives in this AZ and the
# ASG is constrained to it so replacement instances can reattach the volume.
locals {
  subnet_id = sort(data.aws_subnets.default.ids)[0]
}

data "aws_subnet" "host" {
  id = local.subnet_id
}

resource "aws_security_group" "host" {
  name        = "${var.name}-host"
  description = "djvb single-host ingress"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "HTTP -- Lets Encrypt HTTP-01 challenge + redirect to HTTPS"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  dynamic "ingress" {
    for_each = var.ssh_pubkey == "" ? [] : [1]
    content {
      description = "SSH (only if a key pair is configured)"
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
    }
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
