#!/bin/bash
# Reset the local PostgreSQL database (removes all data)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# --- Load docker/.env into the environment without overwriting existing vars ---
ENV_FILE="$SCRIPT_DIR/.env"
if [ -f "$ENV_FILE" ]; then
    while IFS='=' read -r key value; do
        key="${key// /}"
        if [[ -z "$key" || ${key:0:1} == '#' ]]; then
            continue
        fi
        value="${value%"}"
        value="${value#"}"
        if [ -z "${!key}" ]; then
            export "$key=$value"
        fi
    done < <(grep -v '^\s*$' "$ENV_FILE" | grep -v '^\s*#')
fi

# Check if required environment variables are set
missing_vars=()

if [ -z "$SPANNER_BOT_DB_USERNAME" ]; then
    missing_vars+=("SPANNER_BOT_DB_USERNAME")
fi
if [ -z "$SPANNER_BOT_DB_PASSWORD" ]; then
    missing_vars+=("SPANNER_BOT_DB_PASSWORD")
fi
if [ -z "$SPANNER_BOT_DB_NAME" ]; then
    missing_vars+=("SPANNER_BOT_DB_NAME")
fi
if [ -z "$SPANNER_BOT_DB_PORT" ]; then
    missing_vars+=("SPANNER_BOT_DB_PORT")
fi

if [ ${#missing_vars[@]} -gt 0 ]; then
    echo "ERROR: Required environment variables are not set: ${missing_vars[*]}"
    echo ""
    echo "Please set the following environment variables before running this script:"
    echo "  export SPANNER_BOT_DB_USERNAME=\"your_username\""
    echo "  export SPANNER_BOT_DB_PASSWORD=\"your_password\""
    echo "  export SPANNER_BOT_DB_NAME=\"your_database\""
    echo "  export SPANNER_BOT_DB_PORT=\"5432\""
    echo ""
    echo "Or create a .env file in the docker directory. See .env.example for template."
    exit 1
fi

echo "============================================================"
echo "WARNING: This will DELETE ALL database data and volumes!"
echo "============================================================"
echo ""
read -p "Are you sure you want to continue? (y/N) " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "Stopping container and removing volumes..."
    docker compose -f "$SCRIPT_DIR/docker-compose.yml" down -v

    echo "Starting fresh database..."
    docker compose -f "$SCRIPT_DIR/docker-compose.yml" up -d

    echo ""
    echo "Waiting for database to be ready..."
    until docker exec spanner-bot-db pg_isready -U "$SPANNER_BOT_DB_USERNAME" -d "$SPANNER_BOT_DB_NAME" > /dev/null 2>&1; do
        sleep 1
    done

    echo ""
    echo "Database reset complete!"
    echo ""
    echo "Connection details:"
    echo "  Host:     localhost"
    echo "  Port:     $SPANNER_BOT_DB_PORT"
    echo "  Database: $SPANNER_BOT_DB_NAME"
    echo "  Username: $SPANNER_BOT_DB_USERNAME"
    echo "  Password: (hidden)"
else
    echo "Cancelled."
fi

