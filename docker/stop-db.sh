#!/bin/bash
# Stop the local PostgreSQL database

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Stopping PostgreSQL database..."
docker compose -f "$SCRIPT_DIR/docker-compose.yml" down

echo "Database stopped."
echo ""
echo "NOTE: Database data is preserved. To completely reset and remove all data,"
echo "      use reset-db.sh instead."

