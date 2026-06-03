# Secret values are populated out-of-band via `aws ssm put-parameter` so they
# never enter Terraform state. We declare the parameters with `lifecycle.ignore_changes`
# on `value` and a placeholder default so `terraform apply` works on a fresh
# environment, then the operator overwrites the value via AWS CLI.

resource "aws_ssm_parameter" "firebase_key" {
  name        = "/${var.name}/firebase_key"
  description = "Firebase service-account JSON, raw string. Consumed by server-entrypoint.sh as FIREBASE_KEY_JSON."
  type        = "SecureString"
  value       = "PLACEHOLDER_OVERWRITE_VIA_CLI"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "mongo_uri" {
  name  = "/${var.name}/mongo_uri"
  type  = "SecureString"
  value = "PLACEHOLDER_OVERWRITE_VIA_CLI"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "redis_password" {
  name  = "/${var.name}/redis_password"
  type  = "SecureString"
  value = "PLACEHOLDER_OVERWRITE_VIA_CLI"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "vb_organization" {
  name  = "/${var.name}/vb_organization"
  type  = "SecureString"
  value = "PLACEHOLDER_OVERWRITE_VIA_CLI"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "manager_name" {
  name  = "/${var.name}/manager_name"
  type  = "String"
  value = "VBONE"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "mcp_oauth_consent_code" {
  name        = "/${var.name}/mcp_oauth_consent_code"
  description = "Shared operator authorization code for MCP OAuth consent."
  type        = "SecureString"
  value       = "PLACEHOLDER_OVERWRITE_VIA_CLI"

  lifecycle {
    ignore_changes = [value]
  }
}
