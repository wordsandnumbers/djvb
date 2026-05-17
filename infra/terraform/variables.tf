variable "region" {
  description = "AWS region. us-east-1 is cheapest and is the default."
  type        = string
  default     = "us-east-1"
}

variable "name" {
  description = "Resource name prefix."
  type        = string
  default     = "djvb"
}

variable "instance_type" {
  description = "EC2 instance type for the single ECS host."
  type        = string
  default     = "t4g.small"
}

variable "data_volume_size_gb" {
  description = "Size of the EBS data volume mounted at /var/lib/djvb-data."
  type        = number
  default     = 10
}

variable "domain_name" {
  description = "Public hostname for the app (e.g. djvb.example.com). Caddy auto-provisions Let's Encrypt for this name."
  type        = string
}

variable "hosted_zone_id" {
  description = "Route53 hosted zone ID that owns domain_name."
  type        = string
}

variable "ssh_pubkey" {
  description = "Optional SSH public key for the EC2 key pair. Leave empty to skip SSH access (rely on SSM Session Manager)."
  type        = string
  default     = ""
}

variable "image_tag_server" {
  description = "Tag of the djvb-server image to deploy."
  type        = string
  default     = "latest"
}

variable "image_tag_ui" {
  description = "Tag of the djvb-ui image to deploy."
  type        = string
  default     = "latest"
}

variable "github_repo" {
  description = "GitHub repo (owner/name) allowed to assume the OIDC deploy role."
  type        = string
  default     = "wordsandnumbers/djvb"
}
