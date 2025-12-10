# Database Setup Guide

This guide explains how the `consumer_finance` database is created and initialized.

## Automatic Database Creation

The application now supports **automatic database creation** through multiple mechanisms:

### 1. JDBC URL Parameter (Recommended)
The JDBC connection URL includes `createDatabaseIfNotExist=true` which automatically creates the database when the application starts:

```
jdbc:mysql://localhost:3306/consumer_finance?createDatabaseIfNotExist=true&...
```

This works for both:
- **Local development**: `application.yml`
- **Docker deployment**: `docker-compose.yml`

### 2. Docker Initialization Script
For Docker deployments, the database is also created via the initialization script:
- **Location**: `docker/mysql/init/01-create-database.sql`
- **When it runs**: Automatically on first MySQL container start
- **What it does**: Creates `consumer_finance` database with UTF-8 character set

## Setup Methods

### Method 1: Using Docker Compose (Recommended)

The easiest way to get started with a clean database:

```bash
# Stop and remove existing containers and volumes
docker compose down -v

# Start fresh containers (database will be auto-created)
docker compose up -d

# Check logs to verify database creation
docker compose logs mysql | grep "consumer_finance"
```

### Method 2: Local MySQL Setup

If you're running MySQL locally (not in Docker):

#### Option A: Automatic Creation
Just start the application! The JDBC URL parameter will auto-create the database:

```bash
# Start Spring Boot application
mvn spring-boot:run
```

#### Option B: Manual Creation
If you prefer to create the database manually:

```bash
# Connect to MySQL
mysql -u root -p

# Run the commands
CREATE DATABASE IF NOT EXISTS consumer_finance
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

# Verify
SHOW DATABASES LIKE 'consumer_finance';
```

Or use the initialization script:

```bash
# Run the init script directly
mysql -u root -p < docker/mysql/init/01-create-database.sql
```

## Troubleshooting

### Error: "Unknown database 'consumer_finance'"

This should no longer occur with the new configuration. If you still see this error:

1. **Check MySQL is running**:
   ```bash
   # For Docker
   docker compose ps

   # For local MySQL
   sudo systemctl status mysql  # Linux
   brew services list | grep mysql  # macOS
   ```

2. **Verify connection details**:
   - Host: `localhost` (local) or `mysql` (Docker)
   - Port: `3306` (local) or `3307` (Docker mapped port)
   - Username: `root` (local) or `financeuser` (Docker)
   - Password: Check `application.yml` or `.env` file

3. **Force database recreation**:
   ```bash
   # For Docker - complete reset
   docker compose down -v
   docker compose up -d

   # For local MySQL
   mysql -u root -p -e "DROP DATABASE IF EXISTS consumer_finance; CREATE DATABASE consumer_finance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   ```

### Error: "Access denied for user"

If you see permission errors:

```bash
# For local MySQL, grant privileges
mysql -u root -p
GRANT ALL PRIVILEGES ON consumer_finance.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

For Docker, the user is created automatically with proper permissions.

## Database Schema Initialization

After the database is created, **Flyway migrations** automatically create all tables:

1. Database created (via methods above)
2. Application starts
3. Flyway checks `src/main/resources/db/migration/`
4. Runs migrations in order: V1, V2, V3, ... V10
5. Tables and indexes are created
6. Sample data is loaded (V5_seed_vendor_data.sql)

### Current Migrations

- **V1**: Create vendor table
- **V2**: Create consumer table
- **V3**: Create principal_account table
- **V4**: Create loan tables
- **V5**: Seed vendor sample data
- **V6**: Update encryption configurations
- **V7**: Create vendor_linked_accounts table
- **V8**: Add loan decision metadata
- **V10**: Drop pan_number column (latest)

To check migration status:

```bash
# In application logs, look for:
# "Flyway Community Edition ... by Redgate"
# "Successfully applied X migrations"

# Or query the database
mysql -u root -p consumer_finance -e "SELECT * FROM flyway_schema_history;"
```

## Configuration Files

### Local Development
**File**: `src/main/resources/application.yml`
```yaml
datasource:
  url: jdbc:mysql://localhost:3306/consumer_finance?createDatabaseIfNotExist=true&...
  username: root
  password: root@123
```

### Docker Deployment
**File**: `docker-compose.yml`
```yaml
environment:
  MYSQL_DATABASE: consumer_finance
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/consumer_finance?createDatabaseIfNotExist=true&...
```

**File**: `.env` (optional overrides)
```env
MYSQL_DATABASE=consumer_finance
MYSQL_USER=financeuser
MYSQL_PASSWORD=financepass
MYSQL_ROOT_PASSWORD=rootpassword
```

## Quick Verification

After setup, verify everything is working:

```bash
# 1. Check database exists
mysql -u root -p -e "SHOW DATABASES LIKE 'consumer_finance';"

# 2. Check tables were created by Flyway
mysql -u root -p consumer_finance -e "SHOW TABLES;"

# 3. Check sample vendor data was loaded
mysql -u root -p consumer_finance -e "SELECT COUNT(*) FROM vendors;"

# 4. Test application health endpoint
curl http://localhost:8080/api/v1/health
```

## Need Help?

If you're still having issues:

1. Check Docker logs: `docker compose logs -f mysql`
2. Check application logs: `docker compose logs -f app`
3. Verify MySQL connection: `mysql -u root -p -h localhost -P 3307` (Docker) or `mysql -u root -p` (local)
4. Review Flyway migration errors in application logs
