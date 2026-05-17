output "ecr_server_url" {
  value       = aws_ecr_repository.server.repository_url
  description = "Push tag here: <repo_url>:<image_tag>"
}

output "ecr_ui_url" {
  value       = aws_ecr_repository.ui.repository_url
  description = "Push tag here: <repo_url>:<image_tag>"
}

output "host_public_ip" {
  value = aws_eip.host.public_ip
}

output "domain" {
  value = var.domain_name
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.this.name
}

output "github_deploy_role_arn" {
  value       = aws_iam_role.github_deploy.arn
  description = "Set this as AWS_DEPLOY_ROLE in the repo's GitHub Actions secrets."
}
