#!/usr/bin/env bash
set -euo pipefail

# Register a new ECS task-definition revision pinned to a specific image tag,
# point the service at it, and wait for the rollout to stabilize.
#
# Usage: deploy-service.sh <service-name> <image-tag>
#   service-name   e.g. djvb-server, djvb-ui  (also used as the task-def family)
#   image-tag      e.g. a 12-char git SHA

SERVICE="${1:?service name required}"
TAG="${2:?image tag required}"
CLUSTER="${CLUSTER:-djvb}"

current_def=$(aws ecs describe-task-definition --task-definition "$SERVICE")

new_def=$(jq --arg tag "$TAG" '
  .taskDefinition
  | .containerDefinitions[0].image |= (sub(":[^:]+$"; ":" + $tag))
  | del(
      .taskDefinitionArn,
      .revision,
      .status,
      .requiresAttributes,
      .compatibilities,
      .registeredAt,
      .registeredBy
    )
' <<<"$current_def")

new_arn=$(aws ecs register-task-definition \
  --cli-input-json "$new_def" \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text)

echo "Registered $new_arn"

aws ecs update-service \
  --cluster "$CLUSTER" \
  --service "$SERVICE" \
  --task-definition "$new_arn" \
  >/dev/null

echo "Updated service $SERVICE -> $new_arn"
