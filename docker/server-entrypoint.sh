#!/usr/bin/env bash
set -euo pipefail

# If a Firebase service-account JSON is provided as an env var (e.g. injected
# from SSM/Secrets Manager by ECS), materialize it to a file and point
# GOOGLE_APPLICATION_CREDENTIALS_PATH at it. FirebaseConfig will pick it up.
if [[ -n "${FIREBASE_KEY_JSON:-}" ]]; then
    install -m 600 /dev/null /run/firebase.json
    printf '%s' "$FIREBASE_KEY_JSON" > /run/firebase.json
    export GOOGLE_APPLICATION_CREDENTIALS_PATH=/run/firebase.json
fi

JAVA_OPTS="${JAVA_OPTS:--Xms128m -Xmx384m}"
exec java $JAVA_OPTS -jar /app/app.jar
