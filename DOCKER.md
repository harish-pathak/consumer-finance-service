# 🐳 Docker Compose Setup Guide

Run the **entire Consumer Finance Service stack** in Docker containers without installing MySQL, Java, or Maven locally.

## 📋 Prerequisites

**Only Docker is required:**
- [Docker Desktop](https://www.docker.com/products/docker-desktop) (includes Docker Compose)
  - macOS / Windows: Install Docker Desktop
  - Linux: Install Docker Engine + Docker Compose plugin

**Verify Installation:**
```bash
docker --version          # Should be 20.10+
docker-compose --version  # Should be 2.0+
```

---

## 🚀 Quick Start

### 1. Configure Environment

Copy the example environment file and customize if needed:

```bash
cp .env.example .env
```

**Default configuration works out of the box!** You can skip editing `.env` for local development.

### 2. Start the Stack

```bash
docker-compose up -d
```

This command will:
- ✅ Pull MySQL 8.0 image
- ✅ Build the application Docker image
- ✅ Create a dedicated network
- ✅ Start MySQL container
- ✅ Wait for MySQL to be healthy
- ✅ Start the application container
- ✅ Run Flyway database migrations
- ✅ Insert sample vendor data
- ✅ Initialize encryption service

### 3. Verify Services

```bash
# Check container status
docker-compose ps

# View application logs
docker-compose logs -f app

# View MySQL logs
docker-compose logs -f mysql

# Check application health
curl http://localhost:8080/api/v1/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "message": "Consumer Finance Service is running",
  "timestamp": "2025-12-07T12:00:00.000000",
  "version": "1.0.0"
}
```

---

## 📊 Access Points

| Service | URL | Description |
|---------|-----|-------------|
| **Application** | http://localhost:8080 | Main API |
| **Health Check** | http://localhost:8080/api/v1/health | Service health status |
| **Database Health** | http://localhost:8080/api/v1/health/db | MySQL connection status |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | API documentation |
| **OpenAPI Docs** | http://localhost:8080/v3/api-docs | OpenAPI JSON spec |
| **MySQL** | localhost:3306 | Database (user: financeuser, pass: financepass123) |

---

## 🗄️ Database Information

### Connection Details (from .env)

```properties
Host: localhost (or 'mysql' from inside containers)
Port: 3306
Database: consumer_finance
Username: financeuser
Password: financepass123
Root Password: rootpassword123
```

### Pre-loaded Data

The database is automatically initialized with:

1. **Schema** (via Flyway migrations):
   - ✅ `consumers` table
   - ✅ `principal_accounts` table
   - ✅ `vendor_linked_accounts` table
   - ✅ `vendors` table
   - ✅ `loan_applications` table
   - ✅ `loan_application_decisions` table

2. **Sample Vendor Data** (V4 migration):
   - Amazon (ACTIVE)
   - Flipkart (ACTIVE)
   - Myntra (ACTIVE)
   - Swiggy (DISABLED for testing)

### Connect to MySQL

```bash
# Using Docker
docker-compose exec mysql mysql -ufinanceuser -pfinancepass123 consumer_finance

# Using local MySQL client
mysql -h127.0.0.1 -P3306 -ufinanceuser -pfinancepass123 consumer_finance
```

**Useful Queries:**
```sql
-- View all vendors
SELECT * FROM vendors;

-- View consumers
SELECT id, first_name, last_name, email, status FROM consumers;

-- Check Flyway migration history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

---

## 🔐 Security Configuration

### Encryption

The application uses **AES-256-GCM encryption** for sensitive PII data.

**Configure encryption key** (production):
```bash
# Generate a secure key
openssl rand -base64 32

# Update .env
ENCRYPTION_KEY=your_generated_key_here
```

**Encrypted Fields:**
- `national_id`
- `document_number`
- `employer_name`
- `income_source`

### OAuth2 Authentication

By default, all protected endpoints require OAuth2 Bearer tokens.

**Configure Auth0** (production):
```bash
# Update .env
OAUTH2_ISSUER_URI=https://your-tenant.auth0.com/
OAUTH2_JWK_SET_URI=https://your-tenant.auth0.com/.well-known/jwks.json
```

**Test without OAuth2:**
Protected endpoints will return `401 Unauthorized` without a valid token.

---

## 🛠️ Common Commands

### Start Services

```bash
# Start in background
docker-compose up -d

# Start with logs visible
docker-compose up

# Start specific service
docker-compose up -d mysql
```

### Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (deletes database data!)
docker-compose down -v

# Stop without removing containers
docker-compose stop
```

### View Logs

```bash
# All services
docker-compose logs -f

# Application only
docker-compose logs -f app

# MySQL only
docker-compose logs -f mysql

# Last 100 lines
docker-compose logs --tail=100 app
```

### Rebuild Application

After code changes:

```bash
# Rebuild and restart
docker-compose up -d --build app

# Full rebuild (no cache)
docker-compose build --no-cache app
docker-compose up -d app
```

### Execute Commands in Containers

```bash
# Access application container shell
docker-compose exec app sh

# Access MySQL shell
docker-compose exec mysql mysql -uroot -p

# Run Maven tests
docker-compose exec app mvn test

# Check Java version
docker-compose exec app java -version
```

### Database Operations

```bash
# Backup database
docker-compose exec mysql mysqldump -uroot -prootpassword123 consumer_finance > backup.sql

# Restore database
docker-compose exec -T mysql mysql -uroot -prootpassword123 consumer_finance < backup.sql

# Reset database (re-run migrations)
docker-compose down -v
docker-compose up -d
```

---

## 🧹 Cleanup

### Remove Everything

```bash
# Stop and remove containers, networks, and volumes
docker-compose down -v

# Remove Docker images
docker rmi consumer-finance-service:latest
docker rmi mysql:8.0

# Clean up system
docker system prune -a
```

### Persistent Data

Data is stored in named Docker volumes:
- `consumer-finance-mysql-data` - Database files
- `consumer-finance-app-logs` - Application logs

**List volumes:**
```bash
docker volume ls | grep consumer-finance
```

**Remove volumes:**
```bash
docker volume rm consumer-finance-mysql-data
docker volume rm consumer-finance-app-logs
```

---

## 🐛 Troubleshooting

### Application won't start

**Check logs:**
```bash
docker-compose logs app
```

**Common issues:**
- MySQL not ready → Wait 30s for healthcheck
- Port 8080 busy → Change `APP_PORT` in `.env`
- Build failures → Run `docker-compose build --no-cache`

### Database connection errors

**Check MySQL health:**
```bash
docker-compose ps mysql
docker-compose logs mysql
```

**Verify connectivity:**
```bash
docker-compose exec app ping -c 3 mysql
```

### Port conflicts

**Change ports in `.env`:**
```bash
APP_PORT=8081      # Change from 8080
MYSQL_PORT=3307    # Change from 3306
```

Then restart:
```bash
docker-compose down
docker-compose up -d
```

### View encrypted data

Encrypted fields are stored as Base64-encoded ciphertext:

```sql
-- Encrypted data (example)
SELECT national_id, document_number FROM consumers LIMIT 1;
-- Result: Base64 gibberish

-- To decrypt, query via API with valid OAuth2 token
```

---

## 📝 Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | rootpassword123 | MySQL root password |
| `MYSQL_DATABASE` | consumer_finance | Database name |
| `MYSQL_USER` | financeuser | Application database user |
| `MYSQL_PASSWORD` | financepass123 | Application user password |
| `MYSQL_PORT` | 3306 | MySQL exposed port |
| `APP_PORT` | 8080 | Application exposed port |
| `SPRING_PROFILES_ACTIVE` | docker | Spring Boot profile |
| `ENCRYPTION_KEY` | default... | AES-256 encryption key (32 bytes) |
| `OAUTH2_ISSUER_URI` | https://dev-dummy... | OAuth2 issuer URI |
| `OAUTH2_JWK_SET_URI` | https://dev-dummy... | JWK set URI |
| `LOG_LEVEL` | INFO | Root log level |
| `APP_LOG_LEVEL` | DEBUG | Application log level |

---

## 🎯 Next Steps

### 1. Test API Endpoints

```bash
# Health check (public)
curl http://localhost:8080/api/v1/health

# Database health (public)
curl http://localhost:8080/api/v1/health/db

# Test endpoint (public)
curl http://localhost:8080/api/v1/test/ping

# Protected endpoint (requires OAuth2 token)
curl http://localhost:8080/api/v1/consumers
# Expected: 401 Unauthorized
```

### 2. Explore Swagger UI

Open http://localhost:8080/swagger-ui.html in your browser to explore and test all API endpoints interactively.

### 3. Configure OAuth2

Update `.env` with your Auth0 credentials to test protected endpoints with real authentication.

### 4. Load Test Data

Use the API or SQL scripts to populate the database with test consumers, accounts, and loan applications.

---

## 📚 Additional Resources

- **Application Repository**: https://github.com/harish-pathak/consumer-finance-service
- **Docker Documentation**: https://docs.docker.com
- **Docker Compose Reference**: https://docs.docker.com/compose/compose-file/
- **Spring Boot Docker Guide**: https://spring.io/guides/gs/spring-boot-docker/

---

## ✅ Success Checklist

- [ ] Docker Desktop installed and running
- [ ] `.env` file created from `.env.example`
- [ ] Services started with `docker-compose up -d`
- [ ] Application healthy: `curl http://localhost:8080/api/v1/health`
- [ ] Database healthy: `curl http://localhost:8080/api/v1/health/db`
- [ ] Sample vendors loaded: `SELECT COUNT(*) FROM vendors;` (should be 4)
- [ ] Swagger UI accessible: http://localhost:8080/swagger-ui.html

---

🎉 **Congratulations!** Your Consumer Finance Service is running in Docker containers!
