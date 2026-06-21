#!/bin/bash
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-./backups}"
DB_NAME="${DB_NAME:-octane_db}"
DB_USER="${DB_USER:-octane}"
DB_HOST="${DB_HOST:-localhost}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

mkdir -p "$BACKUP_DIR"
pg_dump -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -Fc > "$BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.dump"

# Keep only last 7 backups
ls -t "$BACKUP_DIR"/*.dump | tail -n +8 | xargs -r rm

echo "Backup saved: ${DB_NAME}_${TIMESTAMP}.dump"
