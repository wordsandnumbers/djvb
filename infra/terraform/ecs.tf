resource "aws_ecs_cluster" "this" {
  name = var.name

  setting {
    name  = "containerInsights"
    value = "disabled"
  }
}

resource "aws_ecs_capacity_provider" "host" {
  name = "${var.name}-host"

  auto_scaling_group_provider {
    auto_scaling_group_arn         = aws_autoscaling_group.host.arn
    managed_termination_protection = "ENABLED"

    managed_scaling {
      status                    = "DISABLED"
      target_capacity           = 100
      minimum_scaling_step_size = 1
      maximum_scaling_step_size = 1
    }
  }
}

resource "aws_ecs_cluster_capacity_providers" "this" {
  cluster_name       = aws_ecs_cluster.this.name
  capacity_providers = [aws_ecs_capacity_provider.host.name]

  default_capacity_provider_strategy {
    capacity_provider = aws_ecs_capacity_provider.host.name
    weight            = 100
    base              = 1
  }
}

resource "aws_cloudwatch_log_group" "djvb" {
  name              = "/ecs/${var.name}"
  retention_in_days = 14
}

locals {
  log_config = {
    logDriver = "awslogs"
    options = {
      "awslogs-group"         = aws_cloudwatch_log_group.djvb.name
      "awslogs-region"        = var.region
      "awslogs-stream-prefix" = "ecs"
    }
  }

  ecr_url_server = aws_ecr_repository.server.repository_url
  ecr_url_ui     = aws_ecr_repository.ui.repository_url
}

# -- Mongo task ---------------------------------------------------------------
# All tasks run with network_mode=host: containers bind directly to ports on
# the EC2 host's network and reach each other via `localhost`. The host
# security group only allows 80/443 from the internet, so Mongo (27017) and
# Redis (6379) are reachable only from other containers on the same instance.

resource "aws_ecs_task_definition" "mongo" {
  family             = "${var.name}-mongo"
  network_mode       = "host"
  execution_role_arn = aws_iam_role.ecs_task_execution.arn

  volume {
    name      = "mongo-data"
    host_path = "/var/lib/djvb-data/mongo"
  }

  container_definitions = jsonencode([{
    name      = "mongo"
    image     = "mongo:6"
    essential = true
    memory    = 480
    cpu       = 256
    command   = ["mongod", "--wiredTigerCacheSizeGB", "0.25"]
    mountPoints = [{
      sourceVolume  = "mongo-data"
      containerPath = "/data/db"
      readOnly      = false
    }]
    portMappings = [{
      containerPort = 27017
      hostPort      = 27017
      protocol      = "tcp"
    }]
    logConfiguration = local.log_config
  }])
}

# -- Redis task ---------------------------------------------------------------
resource "aws_ecs_task_definition" "redis" {
  family             = "${var.name}-redis"
  network_mode       = "host"
  execution_role_arn = aws_iam_role.ecs_task_execution.arn

  volume {
    name      = "redis-data"
    host_path = "/var/lib/djvb-data/redis"
  }

  container_definitions = jsonencode([{
    name       = "redis"
    image      = "redis:7-alpine"
    essential  = true
    memory     = 96
    cpu        = 128
    entryPoint = ["sh", "-c"]
    command    = ["exec redis-server --maxmemory 64mb --maxmemory-policy allkeys-lru --requirepass \"$REDIS_PASSWORD\""]
    secrets = [
      { name = "REDIS_PASSWORD", valueFrom = aws_ssm_parameter.redis_password.arn },
    ]
    mountPoints = [{
      sourceVolume  = "redis-data"
      containerPath = "/data"
      readOnly      = false
    }]
    portMappings = [{
      containerPort = 6379
      hostPort      = 6379
      protocol      = "tcp"
    }]
    logConfiguration = local.log_config
  }])
}

# -- Server task (Spring Boot) ------------------------------------------------
resource "aws_ecs_task_definition" "server" {
  family             = "${var.name}-server"
  network_mode       = "host"
  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name      = "server"
    image     = "${local.ecr_url_server}:${var.image_tag_server}"
    essential = true
    memory    = 500
    cpu       = 512
    environment = [
      { name = "JAVA_OPTS", value = "-Xms128m -Xmx384m" },
      { name = "REDIS_HOST", value = "localhost" },
      { name = "REDIS_PORT", value = "6379" },
      { name = "DEFAULT_LANGUAGE", value = "English" },
      { name = "MCP_PUBLIC_BASE_URL", value = "https://${var.domain_name}" },
    ]
    secrets = [
      { name = "FIREBASE_KEY_JSON", valueFrom = aws_ssm_parameter.firebase_key.arn },
      { name = "MONGO_URI", valueFrom = aws_ssm_parameter.mongo_uri.arn },
      { name = "REDIS_PASSWORD", valueFrom = aws_ssm_parameter.redis_password.arn },
      { name = "VB_ORGANIZATION", valueFrom = aws_ssm_parameter.vb_organization.arn },
      { name = "MANAGER_NAME", valueFrom = aws_ssm_parameter.manager_name.arn },
      { name = "MCP_OAUTH_CONSENT_CODE", valueFrom = aws_ssm_parameter.mcp_oauth_consent_code.arn },
    ]
    portMappings = [{
      containerPort = 8080
      hostPort      = 8080
      protocol      = "tcp"
    }]
    healthCheck = {
      command     = ["CMD-SHELL", "curl -fsS http://localhost:8080/actuator/health || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 90
    }
    logConfiguration = local.log_config
  }])
}

# -- Caddy / UI task (the only task with public ingress) ----------------------
resource "aws_ecs_task_definition" "ui" {
  family             = "${var.name}-ui"
  network_mode       = "host"
  execution_role_arn = aws_iam_role.ecs_task_execution.arn

  volume {
    name      = "caddy-data"
    host_path = "/var/lib/djvb-data/caddy"
  }

  container_definitions = jsonencode([{
    name      = "caddy-ui"
    image     = "${local.ecr_url_ui}:${var.image_tag_ui}"
    essential = true
    memory    = 96
    cpu       = 128
    environment = [
      { name = "SITE_ADDRESS", value = var.domain_name },
      { name = "SERVER_UPSTREAM", value = "localhost:8080" },
    ]
    mountPoints = [{
      sourceVolume  = "caddy-data"
      containerPath = "/data"
      readOnly      = false
    }]
    portMappings = [
      { containerPort = 80, hostPort = 80, protocol = "tcp" },
      { containerPort = 443, hostPort = 443, protocol = "tcp" },
    ]
    logConfiguration = local.log_config
  }])
}

# -- Services -----------------------------------------------------------------
resource "aws_ecs_service" "mongo" {
  name                               = "${var.name}-mongo"
  cluster                            = aws_ecs_cluster.this.id
  task_definition                    = aws_ecs_task_definition.mongo.arn
  desired_count                      = 1
  scheduling_strategy                = "REPLICA"
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100
  force_new_deployment               = false
}

resource "aws_ecs_service" "redis" {
  name                               = "${var.name}-redis"
  cluster                            = aws_ecs_cluster.this.id
  task_definition                    = aws_ecs_task_definition.redis.arn
  desired_count                      = 1
  scheduling_strategy                = "REPLICA"
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100
}

resource "aws_ecs_service" "server" {
  name                               = "${var.name}-server"
  cluster                            = aws_ecs_cluster.this.id
  task_definition                    = aws_ecs_task_definition.server.arn
  desired_count                      = 1
  scheduling_strategy                = "REPLICA"
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  depends_on = [aws_ecs_service.mongo, aws_ecs_service.redis]
}

resource "aws_ecs_service" "ui" {
  name                               = "${var.name}-ui"
  cluster                            = aws_ecs_cluster.this.id
  task_definition                    = aws_ecs_task_definition.ui.arn
  desired_count                      = 1
  scheduling_strategy                = "REPLICA"
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  depends_on = [aws_ecs_service.server]
}
