@echo off
REM Stop the local PostgreSQL database

echo Stopping PostgreSQL database...
docker compose -f "%~dp0docker-compose.yml" down

echo Database stopped.
echo.
echo NOTE: Database data is preserved. To completely reset and remove all data,
echo       use reset-db.bat instead.

