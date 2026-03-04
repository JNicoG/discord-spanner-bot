# Local Development Guide

How to run the bot locally and test changes before raising a PR.

---

## Prerequisites

Install these once:

- **Java 25** — [adoptium.net](https://adoptium.net)
- **Docker Desktop** — [docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop) (must be running whenever you use the DB or run tests)

---

## One-time setup

### 1. Create the database env file

Create `docker/.env` with the following content (values can be anything you like — they are local only):

```
SPANNER_BOT_DB_NAME=spannerbot
SPANNER_BOT_DB_USERNAME=spannerbot
SPANNER_BOT_DB_PASSWORD=localpassword
SPANNER_BOT_DB_PORT=5432
```

This file is gitignored and will not be committed.

### 2. Create a test Discord bot

You need your own bot so you can test without touching the production bot on Heroku.

1. Go to [discord.com/developers/applications](https://discord.com/developers/applications) and click **New Application**
2. Name it something like `spanner-bot-dev`
3. Go to the **Bot** tab → click **Reset Token** → copy the token
4. On the same Bot tab, scroll to **Privileged Gateway Intents** and enable:
   - Server Members Intent
   - Message Content Intent
5. Go to **OAuth2 → URL Generator**, tick `bot` and `applications.commands`, tick permissions: **Send Messages**, **Read Message History**, **Manage Messages**
6. Open the generated URL and invite the bot to a private test Discord server you own

---

## Running locally

### Step 1 — Start the database

```powershell
cd docker
.\start-db.bat
```

This starts a PostgreSQL container using the values in `docker/.env`. Leave it running while you develop.

### Step 2 — Set environment variables

Open a PowerShell window and run (substitute your actual bot token):

```powershell
$env:SPANNER_BOT_DB_URL      = "jdbc:postgresql://localhost:5432/spannerbot"
$env:SPANNER_BOT_DB_USERNAME = "spannerbot"
$env:SPANNER_BOT_DB_PASSWORD = "localpassword"
$env:SPANNER_BOT_TOKEN       = "your-bot-token-here"
```

These only apply to the current PowerShell session. You will need to set them again each time you open a new window.

### Step 3 — Run the app

```powershell
./mvnw spring-boot:run
```

On first startup, Flyway automatically creates all database tables. You will see migration logs like:

```
Flyway Community Edition ... by Redgate
Database: jdbc:postgresql://localhost:5432/spannerbot (PostgreSQL ...)
Successfully applied 4 migrations to schema "public"
```

The bot is ready when you see it come online in Discord.

### Stopping

- Stop the app with `Ctrl+C`
- Stop the database:

```powershell
cd docker
.\stop-db.bat
```

---

## Resetting the database

If you need a clean slate (e.g. after adding a new migration and wanting to test from scratch):

```powershell
cd docker
.\reset-db.bat
```

This deletes all data and restarts the container. Flyway will re-run all migrations on next app startup.

---

## Running the automated tests

Docker Desktop must be running. No environment variables needed — Testcontainers spins up its own database automatically.

```powershell
./mvnw test
```

---

## Workflow before raising a PR

1. Run `./mvnw test` — make sure all tests pass
2. Start the bot locally and manually test your changes in your test Discord server
3. Push your branch and open a PR

The production Heroku bot is unaffected until changes are merged to `main`.

---

## Bot commands

### Queue commands

| Command | Description |
|---|---|
| `/keen` or `/k` | Join the queue |
| `/unkeen` | Leave the queue |
| `/keeners` | Show current queue members |

### Spanner commands

| Command | Description |
|---|---|
| `/spanners` | Check your own spanner count |
| `/spanners user:@someone` | Check another user's spanner count |
| `/leaderboard` | Show the paginated spanner leaderboard |

### Ten-man poll

| Command | Description |
|---|---|
| `/ten-man start_date:yyyy-MM-dd end_date:yyyy-MM-dd` | Create an availability poll for a date range |
| `/ten-man start_date:yyyy-MM-dd end_date:yyyy-MM-dd time:8pm` | Same, but include a preferred time |
| `/ten-man-cancel` | Cancel the active poll in this channel |

#### Ten-man examples

Create a poll for a single day:
```
/ten-man start_date:2026-03-06 end_date:2026-03-06
```

Create a poll across a weekend:
```
/ten-man start_date:2026-03-06 end_date:2026-03-08
```

Create a poll across a weekend with a preferred time:
```
/ten-man start_date:2026-03-06 end_date:2026-03-08 time:8pm
```

#### What happens

1. A poll message appears in the channel with a button for each date
2. Players click a date button to mark themselves as available — clicking again removes them
3. When a date reaches **10 sign-ups**, the bot sends a follow-up message mentioning all 10 players:
   ```
   🎮  10-Man is on — Saturday 7 Mar at 8pm!
   @Alice @Bob @Charlie @Dave @Eve @Frank @Grace @Henry @Iris @Jack
   ```
4. Running `/ten-man` again in the same channel while a poll is active returns an ephemeral error

#### Constraints

- Date range must be **14 days or fewer**
- Start date must be on or before end date
- Dates must be in `yyyy-MM-dd` format (e.g. `2026-03-06`)
- Only one active poll per channel at a time
