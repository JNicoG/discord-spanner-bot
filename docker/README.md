# Docker Setup for Local Development

This directory contains Docker configuration for running a local PostgreSQL database for development.

## Features

- PostgreSQL 17 Alpine-based image
- Automatic database and table initialisation (place init SQL scripts in `docker/init-scripts/`)
- Note: The application uses Flyway for database migrations, so the init scripts are optional and only needed if you want to pre-populate the database with specific data or schema before the application runs.
- Health checks for container status monitoring
- Persistent data volume

## Prerequisites

- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)

## Configuration

### Using Environment Variables

The docker-compose.yml supports environment variables for database configuration. You can set these variables in a `.env` file or directly in your shell before starting the container.

#### Option 1: Using a .env file (Recommended)

Create a `.env` file in the `docker/` directory with the following template:

```env
SPANNER_BOT_DB_NAME=
SPANNER_BOT_DB_USERNAME=
SPANNER_BOT_DB_PASSWORD=
SPANNER_BOT_DB_PORT=
```

Docker Compose automatically loads this file.

#### Option 2: Set environment variables manually

**Windows (PowerShell):**
```powershell
$env:SPANNER_BOT_DB_NAME = "spanner_bot"
$env:SPANNER_BOT_DB_USERNAME = "spanner_bot"
$env:SPANNER_BOT_DB_PASSWORD = "spanner_bot"
$env:SPANNER_BOT_DB_PORT = 5432
cd docker
.\start-db.bat
```

**Linux/macOS:**
```bash
export SPANNER_BOT_DB_NAME="spanner_bot"
export SPANNER_BOT_DB_USERNAME="spanner_bot"
export SPANNER_BOT_DB_PASSWORD="spanner_bot"
export SPANNER_BOT_DB_PORT=5432
./docker/start-db.sh
```

### Supported Environment Variables

| Variable | Description |
|----------|-------------|
| `SPANNER_BOT_DB_NAME` | PostgreSQL database name |
| `SPANNER_BOT_DB_USERNAME` | PostgreSQL username |
| `SPANNER_BOT_DB_PASSWORD` | PostgreSQL password |
| `SPANNER_BOT_DB_PORT` | Host port mapping |

**Note:** These variables configure the Docker container. Your Spring Boot application needs matching values in its own environment variables for connection.

## Quick Start

### Windows

```batch
# Start the database
docker\start-db.bat

# Stop the database
docker\stop-db.bat

# Reset the database (deletes the volume containing database data)
docker\reset-db.bat
```

### Linux/macOS

```bash
# Make scripts executable (first time only)
chmod +x docker/*.sh

# Start the database
./docker/start-db.sh

# Stop the database
./docker/stop-db.sh

# Reset the database (deletes volume containing database data)
./docker/reset-db.sh
```

## Connection Details

**Example values** (you must set these via environment variables or .env file):

| Property  | Example Value                                |
|-----------|----------------------------------------------|
| Host      | localhost                                    |
| Port      | 5432                                         |
| Database  | spanner_bot                                  |
| Username  | spanner_bot                                  |
| Password  | spanner_bot                                  |
| JDBC URL  | jdbc:postgresql://localhost:5432/spanner_bot |

**Note:** The actual values depend on what you configure in your environment variables.

## Running the Application

Once the database is running, set the required environment variables and start the application:

### Windows (PowerShell)
```powershell
$env:SPANNER_BOT_DB_URL = "jdbc:postgresql://localhost:5432/spanner_bot"
$env:SPANNER_BOT_DB_USERNAME = "spanner_bot"
$env:SPANNER_BOT_DB_PASSWORD = "spanner_bot"
./mvnw spring-boot:run
```

### Windows (Command Prompt)
```batch
set SPANNER_BOT_DB_URL=jdbc:postgresql://localhost:5432/spanner_bot
set SPANNER_BOT_DB_USERNAME=spanner_bot
set SPANNER_BOT_DB_PASSWORD=spanner_bot
mvnw.cmd spring-boot:run
```

### Linux/macOS
```bash
export SPANNER_BOT_DB_URL="jdbc:postgresql://localhost:5432/spanner_bot"
export SPANNER_BOT_DB_USERNAME="spanner_bot"
export SPANNER_BOT_DB_PASSWORD="spanner_bot"
./mvnw spring-boot:run
```

## Running with a Compiled JAR

First, compile the application:

```bash
./mvnw clean package -DskipTests
```

Then start the database and run the JAR:

### Windows (PowerShell)
```powershell
# Start the database
docker\start-db.bat

# Set environment variables and run the JAR
$env:SPANNER_BOT_TOKEN = "your-discord-bot-token"
$env:SPANNER_BOT_DB_URL = "jdbc:postgresql://localhost:5432/spanner_bot"
$env:SPANNER_BOT_DB_USERNAME = "spanner_bot"
$env:SPANNER_BOT_DB_PASSWORD = "spanner_bot"
$env:SPANNER_BOT_VERSION_NUMBER = "3.0.0"
java -jar target/discord-spanner-bot-%SPANNER_BOT_VERSION_NUMBER%-SNAPSHOT.jar
```

### Linux/macOS
```bash
# Start the database
./docker/start-db.sh

# Set environment variables and run the JAR
export SPANNER_BOT_TOKEN="your-discord-bot-token"
export SPANNER_BOT_DB_URL="jdbc:postgresql://localhost:5432/spanner_bot"
export SPANNER_BOT_DB_USERNAME="spanner_bot"
export SPANNER_BOT_DB_PASSWORD="spanner_bot"
export SPANNER_BOT_VERSION_NUMBER="3.0.0"
java -jar target/discord-spanner-bot-${SPANNER_BOT_VERSION_NUMBER}-SNAPSHOT.jar
```

The application will:
1. Connect to the local PostgreSQL database
2. Run Flyway migrations to create/update the schema
3. Start the Discord bot

## Environment Variables

The following environment variables **must** be set:

| Variable | Description | Local Development Value |
|----------|-------------|------------------------|
| `SPANNER_BOT_TOKEN` | Discord bot token | Your Discord bot token |
| `SPANNER_BOT_DB_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/spanner_bot` |
| `SPANNER_BOT_DB_USERNAME` | Database username | `spanner_bot` |
| `SPANNER_BOT_DB_PASSWORD` | Database password | `spanner_bot` |

## Database Migrations

The database schema is managed by **Flyway migrations**, which are located in `src/main/resources/db/migration/`.

Migration files follow the naming convention: `V{version}__{description}.sql`

Example: `V1__Initial_schema.sql`

When you start the Spring Boot application, Flyway will automatically:
1. Create a `flyway_schema_history` table to track which migrations have been applied
2. Execute any pending migrations in version order
3. Update the schema to the latest version

**Important:** The Docker container provides an empty PostgreSQL database. The application's Flyway migrations handle all schema creation and updates. This ensures consistency between local development, testing, and production environments.

## Troubleshooting

### Container won't start
```bash
# Check if port 5432 is already in use
netstat -an | findstr 5432    # Windows
lsof -i :5432                 # Linux/macOS

# View container logs
docker logs spanner-bot-db
```

### Reset everything
```bash
docker compose -f docker/docker-compose.yml down -v
docker compose -f docker/docker-compose.yml up -d
```

### Password authentication failed error on application startup
```
Check if port 5432 is already in use:
Ensure port 5432 is not conflicting with another instance e.g. a local PostgreSQL installation:
If you have previously installed PostgreSQL locally, it may be already using port 5432.
You can either stop the local PostgreSQL service or change the port mapping in `docker-compose.yml` to use a different port (e.g. `5433:5432`)
```