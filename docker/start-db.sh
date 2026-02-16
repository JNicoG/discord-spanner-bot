#!/bin/bash
# Start the local PostgreSQL database for development

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# --- Load docker/.env into the environment without overwriting existing vars ---
ENV_FILE="$SCRIPT_DIR/.env"
if [ -f "$ENV_FILE" ]; then
    while IFS='=' read -r key value; do
        # strip whitespace
        key="${key// /}"
        # skip comments and empty lines
        if [[ -z "$key" || ${key:0:1} == '#' ]]; then
            continue
        fi
        # remove surrounding quotes from value
        value="${value%"}"
        value="${value#"}"
        # only export if not already set
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

echo "Starting PostgreSQL database..."
docker compose -f "$SCRIPT_DIR/docker-compose.yml" up -d

echo ""
echo "Waiting for database to be ready..."
until docker exec spanner-bot-db pg_isready -U "$SPANNER_BOT_DB_USERNAME" -d "$SPANNER_BOT_DB_NAME" > /dev/null 2>&1; do
    sleep 1
done

echo ""
echo "Database is ready!"
echo ""
echo "Connection details:"
echo "  Host:     localhost"
echo "  Port:     $SPANNER_BOT_DB_PORT"
echo "  Database: $SPANNER_BOT_DB_NAME"
echo "  Username: $SPANNER_BOT_DB_USERNAME"
echo "  Password: (hidden)"
echo ""
echo "JDBC URL: jdbc:postgresql://localhost:$SPANNER_BOT_DB_PORT/$SPANNER_BOT_DB_NAME"
