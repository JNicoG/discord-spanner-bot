@echo off
REM Start the local PostgreSQL database for development

REM --- Load docker\.env if present (do not override already defined variables) ---
if exist "%~dp0.env" (
    for /f "usebackq tokens=1* delims==" %%A in ("%~dp0.env") do (
        if not "%%A"=="" (
            if not defined %%A (
                set "%%A=%%~B"
            )
        )
    )
)

REM Check if required environment variables are set
set "missing_vars="

if not defined SPANNER_BOT_DB_USERNAME (
    set "missing_vars=%missing_vars% SPANNER_BOT_DB_USERNAME"
)
if not defined SPANNER_BOT_DB_PASSWORD (
    set "missing_vars=%missing_vars% SPANNER_BOT_DB_PASSWORD"
)
if not defined SPANNER_BOT_DB_NAME (
    set "missing_vars=%missing_vars% SPANNER_BOT_DB_NAME"
)
if not defined SPANNER_BOT_DB_PORT (
    set "missing_vars=%missing_vars% SPANNER_BOT_DB_PORT"
)

if defined missing_vars (
    echo ERROR: Required environment variables are not set:%missing_vars%
    echo.
    echo Please set the following environment variables before running this script:
    echo   $env:SPANNER_BOT_DB_USERNAME = "your_username"
    echo   $env:SPANNER_BOT_DB_PASSWORD = "your_password"
    echo   $env:SPANNER_BOT_DB_NAME = "your_database"
    echo   $env:SPANNER_BOT_DB_PORT = "5432"
    echo.
    echo Or create a .env file in the docker directory. See .env.example for template.
    exit /b 1
)

echo Starting PostgreSQL database...
docker compose -f "%~dp0docker-compose.yml" up -d

echo.
echo Waiting for database to be ready...
:wait_loop
docker exec spanner-bot-db pg_isready -U %SPANNER_BOT_DB_USERNAME% -d %SPANNER_BOT_DB_NAME% >nul 2>&1
if errorlevel 1 (
    timeout /t 1 /nobreak >nul
    goto wait_loop
)

echo.
echo Database is ready!
echo.
echo Connection details:
echo   Host:     localhost
echo   Port:     %SPANNER_BOT_DB_PORT%
echo   Database: %SPANNER_BOT_DB_NAME%
echo   Username: %SPANNER_BOT_DB_USERNAME%
echo   Password: (hidden)
echo.
echo JDBC URL: jdbc:postgresql://localhost:%SPANNER_BOT_DB_PORT%/%SPANNER_BOT_DB_NAME%
