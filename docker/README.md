# Docker (local PostgreSQL) — quick reference

This `docker/` folder contains the Docker Compose configuration and helper scripts used to run a local PostgreSQL database for development.

For full project setup and developer instructions see the top-level `README.md` in the project root.

What this folder contains
- `docker-compose.yml` — service definition for the PostgreSQL container
- `Dockerfile.postgres` — base image configuration and healthcheck
- `init-scripts/` — optional SQL files copied into the image on first initialization
- Management scripts:
  - `start-db.bat` / `start-db.sh` — start database (reads `docker/.env`)
  - `stop-db.bat` / `stop-db.sh` — stop database
  - `reset-db.bat` / `reset-db.sh` — remove data volume and start fresh database (reads `docker/.env`)

Environment variables

This folder uses a small `.env` file to provide database credentials to `docker-compose`.

Quick commands

Windows (PowerShell):
```powershell
cd docker
.\start-db.bat
# stop
.\stop-db.bat
# reset (deletes DB data)
.\reset-db.bat
```

Linux / macOS:
```bash
cd docker
./start-db.sh
# stop
./stop-db.sh
# reset (deletes DB data)
./reset-db.sh
```

Notes
- The start/reset scripts will load `docker/.env` (if present) and validate required variables.
- The Docker image copies SQL files from `init-scripts/` into `/docker-entrypoint-initdb.d/` and those scripts are executed only when the DB data directory is empty. If you use `init-scripts/` and Flyway migrations together, make sure they don't conflict.

For more complete developer information (Discord bot setup, running the app, commands, diagrams, testing), open the top-level `README.md`.
