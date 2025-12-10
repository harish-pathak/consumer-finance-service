# Consumer Finance Service - Complete Codebase Context

**Document Version:** 1.0
**Last Updated:** December 10, 2025
**Project Version:** 1.0.0

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Development Environment](#development-environment)
4. [Database Architecture](#database-architecture)
5. [Application Configuration](#application-configuration)
6. [Security & Authentication](#security--authentication)
7. [API Architecture](#api-architecture)
8. [Project Structure](#project-structure)
9. [Build & Deployment](#build--deployment)
10. [Git Repository Information](#git-repository-information)
11. [Testing Framework](#testing-framework)
12. [Quick Reference Commands](#quick-reference-commands)

---

## Project Overview

**Name:** Consumer Finance Service
**Description:** Production-grade Spring Boot microservice for consumer lending operations
**Type:** RESTful API Microservice
**Architecture:** Layered (Controller → Service → Repository → Domain)

### Key Features
- Consumer onboarding with PII encryption
- Principal account management
- Vendor account linking
- Loan application processing
- Event-driven workflow automation
- OAuth2/Auth0 authentication
- Comprehensive API documentation (Swagger/OpenAPI)
- Docker containerization with health checks

### Project Metrics
- **Production Code:** ~7,934 lines
- **Test Files:** 13 comprehensive test classes
- **API Endpoints:** 6 main controllers
- **Database Tables:** 7 core tables
- **Build Artifact:** consumer-finance-service-1.0.0.jar (57MB)

---

## Technology Stack

### Core Platform
| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 17 (LTS) |
| **Framework** | Spring Boot | 3.2.0 |
| **Build Tool** | Apache Maven | 3.9.6 |
| **Container Runtime** | Docker | Latest |
| **Database** | MySQL | 8.0 |

### Spring Boot Ecosystem

#### Web & API
- `spring-boot-starter-web` - REST API with embedded Tomcat
- `springdoc-openapi-starter-webmvc-ui` (v2.1.0) - OpenAPI 3.0 / Swagger UI

#### Data & Persistence
- `spring-boot-starter-data-jpa` - JPA/Hibernate ORM
- `mysql-connector-j` - MySQL 8.0 JDBC driver
- `flyway-core` + `flyway-mysql` - Database versioning and migration

#### Security
- `spring-boot-starter-security` - Core security framework
- `spring-boot-starter-oauth2-resource-server` - OAuth2 JWT validation
- `jjwt` (v0.12.3) - DEPRECATED: Legacy JWT support (to be removed)

#### Validation & Processing
- `spring-boot-starter-validation` - JSR-380 Bean Validation
- `jackson-databind` - JSON serialization
- `commons-codec` - Base64 encoding for encryption

#### Development Tools
- `lombok` (v1.18.30) - Boilerplate code reduction
- `spring-boot-devtools` - Hot reload during development
- `spring-boot-configuration-processor` - Configuration metadata

### Testing Stack
- **JUnit 5** (Jupiter) - Unit testing framework
- **Mockito** - Mocking framework
- **MockMvc** - REST API integration testing
- **Spring Security Test** - Security testing utilities
- **H2 Database** - In-memory database for tests
- **Hamcrest** - Fluent assertion matchers

### Infrastructure
- **Docker** - Multi-stage containerization
- **Docker Compose** (v3.8) - Multi-container orchestration
- **Amazon Corretto 17 Alpine** - Minimal JVM runtime
- **GitHub Container Registry** - Container image hosting

---

## Development Environment

### Maven Configuration

**Maven Installation Path:**
```
/Users/harish.pathak/Projects/apache-maven-3.9.6/bin
```

**Project Coordinates:**
```xml
<groupId>com.example</groupId>
<artifactId>consumer-finance-service</artifactId>
<version>1.0.0</version>
<packaging>jar</packaging>
```

**Maven Properties:**
```xml
<java.version>17</java.version>
<maven.compiler.source>17</maven.compiler.source>
<maven.compiler.target>17</maven.compiler.target>
<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
```

### Local Development Setup

**Prerequisites:**
- Java 17 (Amazon Corretto or OpenJDK)
- Maven 3.9.6
- MySQL 8.0
- Docker & Docker Compose (for containerized deployment)

**Working Directory:**
```
/Users/harish.pathak/consumer-finance-service
```

---

## Database Architecture

### Connection Details

#### Local Development
```yaml
URL: jdbc:mysql://localhost:3306/consumer_finance?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
Username: root
Password: root@123
Driver: com.mysql.cj.jdbc.Driver
```

#### Docker Deployment
```yaml
URL: jdbc:mysql://mysql:3306/consumer_finance
Username: financeuser (configurable via MYSQL_USER)
Password: financepass (configurable via MYSQL_PASSWORD)
Host: mysql (Docker service name)
Port: 3307 (host) → 3306 (container)
```

#### Connection Pool (HikariCP)
```yaml
Local:
  maximum-pool-size: 20
  minimum-idle: 5

Docker:
  maximum-pool-size: 10
  minimum-idle: 5
  connection-timeout: 30000ms
  idle-timeout: 600000ms
  max-lifetime: 1800000ms
```

### Database Schema

**Database Name:** `consumer_finance`
**Character Set:** utf8mb4
**Collation:** utf8mb4_unicode_ci
**Engine:** InnoDB
**Migration Tool:** Flyway

#### Schema Version History

| Version | Migration File | Description |
|---------|---------------|-------------|
| V1 | V1__Create_consumers_table.sql | Consumer profiles with PII |
| V2 | V2__Create_principal_accounts_table.sql | Principal accounts (1:1 with consumers) |
| V3 | V3__Create_vendors_table.sql | Vendor master data |
| V4 | V4__Create_vendor_linked_accounts_table.sql | Consumer-vendor account links |
| V5 | V5__Insert_sample_vendors.sql | Sample vendor seed data |
| V6 | V6__Create_loan_applications_table.sql | Loan application submissions |
| V7 | V7__Create_loan_application_decisions_table.sql | Loan decision audit trail |
| V8 | V8__Update_encrypted_field_lengths.sql | Encrypted field size adjustments |

### Detailed Table Schemas

#### 1. consumers
**Purpose:** Core consumer profile with personal, identity, employment, and financial information

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| id | VARCHAR(36) | PRIMARY KEY | UUID identifier |
| first_name | VARCHAR(100) | NOT NULL | Consumer first name |
| last_name | VARCHAR(100) | NOT NULL | Consumer last name |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Email (unique for duplicate detection) |
| phone | VARCHAR(20) | UNIQUE | Phone number |
| date_of_birth | DATE | | Date of birth |
| national_id | VARCHAR(255) | UNIQUE | National ID (ENCRYPTED) |
| document_type | VARCHAR(50) | | Document type (PASSPORT, NATIONAL_ID, etc.) |
| document_number | VARCHAR(255) | UNIQUE | Document number (ENCRYPTED) |
| employer_name | VARCHAR(255) | | Employer name (ENCRYPTED) |
| position | VARCHAR(100) | | Job position/title |
| employment_type | VARCHAR(50) | | Employment type (FULL_TIME, PART_TIME, etc.) |
| years_of_experience | BIGINT | | Years of work experience |
| industry | VARCHAR(100) | | Industry sector |
| monthly_income | DECIMAL(15,2) | | Monthly income (ENCRYPTED) |
| annual_income | DECIMAL(15,2) | | Annual income (ENCRYPTED) |
| income_source | VARCHAR(255) | | Income source |
| currency | VARCHAR(3) | DEFAULT 'USD' | Currency code |
| status | VARCHAR(50) | NOT NULL, DEFAULT 'ACTIVE' | Account status (ACTIVE, DISABLED, ARCHIVED) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |
| created_by | VARCHAR(100) | | Creator user/system |
| updated_by | VARCHAR(100) | | Last updater user/system |

**Indexes:**
- `idx_email` (email)
- `idx_national_id` (national_id)
- `idx_status` (status)
- `idx_created_at` (created_at)
- `idx_email_national_id` UNIQUE (email, national_id)

**Constraints:**
- Email format validation: `email LIKE '%@%'`
- Positive income check: `monthly_income >= 0`

**Encrypted Fields:** national_id, document_number, employer_name, monthly_income, annual_income, income_source

---

#### 2. principal_accounts
**Purpose:** Primary account for each consumer (1:1 relationship)

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| id | VARCHAR(36) | PRIMARY KEY | UUID identifier |
| consumer_id | VARCHAR(36) | NOT NULL, UNIQUE, FK | Foreign key to consumers.id |
| account_type | VARCHAR(50) | DEFAULT 'PRIMARY' | Account type |
| status | VARCHAR(50) | NOT NULL, DEFAULT 'ACTIVE' | Status (ACTIVE, INACTIVE, ARCHIVED, SUSPENDED) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |
| created_by | VARCHAR(100) | | Creator user/system |
| updated_by | VARCHAR(100) | | Last updater user/system |

**Foreign Keys:**
- `fk_principal_account_consumer` → consumers(id) ON DELETE CASCADE

**Indexes:**
- `uk_principal_account_consumer_id` UNIQUE (consumer_id) - Enforce 1:1 relationship
- `idx_status` (status)
- `idx_created_at` (created_at)

---

#### 3. vendors
**Purpose:** Vendor/partner organizations master data

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| id | VARCHAR(36) | PRIMARY KEY | UUID identifier |
| name | VARCHAR(100) | NOT NULL, UNIQUE | Vendor name |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | Vendor status (ACTIVE, INACTIVE) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Indexes:**
- `idx_vendor_status` (status)
- `idx_vendor_name` (name)

---

#### 4. vendor_linked_accounts
**Purpose:** Consumer-vendor account linkages (many-to-many with constraints)

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| id | VARCHAR(36) | PRIMARY KEY | UUID identifier |
| consumer_id | VARCHAR(36) | NOT NULL, FK | Foreign key to consumers.id |
| vendor_id | VARCHAR(36) | NOT NULL, FK | Foreign key to vendors.id |
| principal_account_id | VARCHAR(36) | FK | Reference to principal_accounts.id |
| status | VARCHAR(50) | NOT NULL, DEFAULT 'ACTIVE' | Status (ACTIVE, DISABLED, ARCHIVED) |
| external_account_ref | VARCHAR(255) | | External vendor system reference |
| linkage_id | VARCHAR(100) | | Internal linkage identifier |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |
| created_by | VARCHAR(100) | | Creator user/system |
| updated_by | VARCHAR(100) | | Last updater user/system |

**Foreign Keys:**
- `fk_vendor_linked_account_consumer` → consumers(id) ON DELETE CASCADE
- `fk_vendor_linked_account_vendor` → vendors(id) ON DELETE RESTRICT
- `fk_vendor_linked_account_principal` → principal_accounts(id) ON DELETE SET NULL

**Unique Constraints:**
- `uk_consumer_vendor_link` UNIQUE (consumer_id, vendor_id) - One link per consumer-vendor pair

**Indexes:**
- `idx_consumer_id` (consumer_id)
- `idx_vendor_id` (vendor_id)
- `idx_principal_account_id` (principal_account_id)
- `idx_status` (status)
- `idx_consumer_status` (consumer_id, status)

---

#### 5. loan_applications
**Purpose:** Consumer loan application submissions

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| id | VARCHAR(36) | PRIMARY KEY | UUID identifier |
| consumer_id | VARCHAR(36) | NOT NULL, FK | Foreign key to consumers.id |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | Status (PENDING, APPROVED, REJECTED, CANCELLED) |
| requested_amount | DECIMAL(15,2) | NOT NULL | Loan amount requested |
| term_in_months | INT | | Loan term in months |
| purpose | VARCHAR(255) | | Purpose of the loan |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Submission timestamp |
| updated_at | TIMESTAMP | NOT NULL, ON UPDATE CURRENT_TIMESTAMP | Last update timestamp |

**Foreign Keys:**
- `fk_loan_app_consumer` → consumers(id) ON DELETE CASCADE

**Indexes:**
- `idx_app_consumer` (consumer_id)
- `idx_app_status` (status)
- `idx_app_created_at` (created_at)
- `idx_app_consumer_status` (consumer_id, status)

---

#### 6. loan_application_decisions
**Purpose:** Immutable audit trail of loan approval/rejection decisions

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| id | VARCHAR(36) | PRIMARY KEY | UUID identifier |
| application_id | VARCHAR(36) | NOT NULL, FK | Foreign key to loan_applications.id |
| decision | ENUM('APPROVED', 'REJECTED') | NOT NULL | Decision outcome |
| staff_id | VARCHAR(100) | NOT NULL | Staff member ID who made decision |
| reason | VARCHAR(500) | | Reason for the decision |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Decision timestamp |

**Foreign Keys:**
- `fk_decision_app` → loan_applications(id) ON DELETE CASCADE

**Unique Constraints:**
- `uq_app_decision` UNIQUE (application_id, decision) - Prevent duplicate decisions

**Indexes:**
- `idx_decision_app` (application_id)
- `idx_decision_staff` (staff_id)
- `idx_decision_created` (created_at)
- `idx_decision_status` (decision)

---

#### 7. loans (Planned - Not yet implemented in migrations)
**Purpose:** Active loan records post-approval

#### 8. repayments (Planned - Not yet implemented in migrations)
**Purpose:** Loan repayment tracking

---

### Entity Relationship Diagram (ERD)

```
consumers (1) ──────────── (1) principal_accounts
    │
    │ (1)
    │
    ├────────── (N) vendor_linked_accounts (N) ────────── (1) vendors
    │                      │
    │                      │ (1)
    │                      │
    │                      └───────── (1) principal_accounts
    │
    │ (1)
    │
    └────────── (N) loan_applications
                      │
                      │ (1)
                      │
                      └───────── (N) loan_application_decisions
```

---

## Application Configuration

### Configuration Profiles

#### 1. Default Profile (Local Development)
**File:** `src/main/resources/application.yml`

```yaml
spring.application.name: consumer-finance-service
server.port: 8080

# Database
datasource:
  url: jdbc:mysql://localhost:3306/consumer_finance
  username: root
  password: root@123

# OAuth2
security.oauth2.resourceserver.jwt.issuer-uri: https://dev-nhmhs0r5umunbtao.us.auth0.com/

# Flyway
flyway.enabled: true
flyway.baseline-on-migrate: false

# Logging
logging.level:
  root: INFO
  com.infobeans.consumerfinance: DEBUG
```

#### 2. Docker Profile
**File:** `src/main/resources/application-docker.yml`

```yaml
# All configurations use environment variables with defaults
datasource.url: ${SPRING_DATASOURCE_URL:jdbc:mysql://mysql:3306/consumer_finance}
datasource.username: ${SPRING_DATASOURCE_USERNAME:financeuser}
datasource.password: ${SPRING_DATASOURCE_PASSWORD:financepass}

# OAuth2
security.oauth2.resourceserver.jwt.issuer-uri: ${SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI}

# Encryption
encryption.key: ${ENCRYPTION_KEY}

# Actuator
management.endpoints.web.exposure.include: health,info,metrics

# Graceful shutdown
server.shutdown: graceful

# File logging
logging.file.name: /app/logs/application.log
```

#### 3. Test Profile
**File:** `src/test/resources/application.yml`

```yaml
# OAuth2 disabled for tests
security.oauth2.resourceserver.jwt.issuer-uri: ""

# Flyway disabled
flyway.enabled: false

# Minimal logging
logging.level:
  root: WARN
  com.infobeans.consumerfinance: INFO
```

### Environment Variables

**Required for Docker Deployment:**

```bash
# Database
MYSQL_ROOT_PASSWORD=rootpassword123
MYSQL_DATABASE=consumer_finance
MYSQL_USER=financeuser
MYSQL_PASSWORD=financepass123
MYSQL_PORT=3307

# Application
APP_PORT=8080
SPRING_PROFILES_ACTIVE=docker

# Security (CRITICAL - Change in production!)
ENCRYPTION_KEY=<32-byte Base64 key>  # Generate: openssl rand -base64 32

# OAuth2
OAUTH2_ISSUER_URI=https://dev-nhmhs0r5umunbtao.us.auth0.com/
OAUTH2_JWK_SET_URI=https://dev-nhmhs0r5umunbtao.us.auth0.com/.well-known/jwks.json

# Logging
LOG_LEVEL=INFO
APP_LOG_LEVEL=DEBUG
```

---

## Security & Authentication

### OAuth2 / Auth0 Configuration

**Authentication Model:** OAuth2 Resource Server (JWT Bearer Tokens)

#### Auth0 Tenant Details
```
Domain: dev-nhmhs0r5umunbtao.us.auth0.com
Issuer URI: https://dev-nhmhs0r5umunbtao.us.auth0.com/
JWK Set URI: https://dev-nhmhs0r5umunbtao.us.auth0.com/.well-known/jwks.json
```

#### Public Endpoints (No Authentication Required)
```
/api/v1/health/**          - Health checks
/api/v1/test/**            - Test endpoints (disable in production!)
/swagger-ui/**             - API documentation
/v3/api-docs/**            - OpenAPI JSON
/actuator/health           - Actuator health endpoint
```

#### Protected Endpoints (OAuth2 JWT Required)
```
/api/v1/consumers/**              - Consumer operations
/api/v1/principal-accounts/**     - Principal account operations
/api/v1/vendor-accounts/**        - Vendor account operations
/api/v1/loan-applications/**      - Loan application operations
```

### Data Encryption

**Algorithm:** AES-256-GCM (Galois/Counter Mode)
**Key Size:** 256 bits (32 bytes)
**Encoding:** Base64 (for database storage)

**Implementation:**
```java
// Service: com.infobeans.consumerfinance.util.EncryptionService
// Converter: com.infobeans.consumerfinance.converter.EncryptedFieldConverter
```

**Encrypted Fields:**
- consumers.national_id
- consumers.document_number
- consumers.employer_name
- consumers.monthly_income
- consumers.annual_income
- consumers.income_source

**Key Management:**
- Development: Configured in application.yml
- Docker: Environment variable `ENCRYPTION_KEY`
- Production: Generate using `openssl rand -base64 32`

---

## API Architecture

### REST Controllers

#### 1. ConsumerController
**Base Path:** `/api/v1/consumers`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/consumers | Onboard new consumer | OAuth2 |
| GET | /api/v1/consumers/{id} | Get consumer by ID | OAuth2 |

**Features:**
- Request validation (JSR-380)
- Duplicate detection (email, national ID, phone, document number)
- Automatic PII encryption
- Event publishing (OnboardingCompletedEvent)

#### 2. PrincipalAccountController
**Base Path:** `/api/v1/principal-accounts`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/principal-accounts | Create principal account | OAuth2 |
| GET | /api/v1/principal-accounts/{id} | Get account by ID | OAuth2 |

#### 3. VendorLinkedAccountController
**Base Path:** `/api/v1/vendor-accounts`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/vendor-accounts | Link vendor account | OAuth2 |
| GET | /api/v1/vendor-accounts/{id} | Get linked account | OAuth2 |
| PATCH | /api/v1/vendor-accounts/{id}/status | Update account status | OAuth2 |

#### 4. LoanApplicationController
**Base Path:** `/api/v1/loan-applications`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/loan-applications | Submit loan application | OAuth2 |
| GET | /api/v1/loan-applications/{id} | Get application by ID | OAuth2 |
| POST | /api/v1/loan-applications/{id}/decision | Submit approval/rejection | OAuth2 |

#### 5. HealthController
**Base Path:** `/api/v1/health`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/health | Basic health check | Public |

#### 6. TestController
**Base Path:** `/api/v1/test`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/test | Test endpoint | Public |

**⚠️ WARNING:** Disable TestController in production!

### API Documentation

**Swagger UI:** http://localhost:8080/swagger-ui.html
**OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## Project Structure

```
consumer-finance-service/
├── src/
│   ├── main/
│   │   ├── java/com/infobeans/consumerfinance/
│   │   │   ├── ConsumerFinanceServiceApplication.java    # Main entry point
│   │   │   ├── controller/                                # REST controllers (6 files)
│   │   │   │   ├── ConsumerController.java
│   │   │   │   ├── PrincipalAccountController.java
│   │   │   │   ├── VendorLinkedAccountController.java
│   │   │   │   ├── LoanApplicationController.java
│   │   │   │   ├── HealthController.java
│   │   │   │   └── TestController.java
│   │   │   ├── service/                                   # Business logic
│   │   │   │   ├── ConsumerService.java
│   │   │   │   ├── PrincipalAccountService.java
│   │   │   │   ├── VendorLinkedAccountService.java
│   │   │   │   ├── LoanApplicationService.java
│   │   │   │   ├── LoanDecisionService.java
│   │   │   │   └── impl/                                  # Service implementations
│   │   │   ├── repository/                                # JPA repositories (6 files)
│   │   │   │   ├── ConsumerRepository.java
│   │   │   │   ├── PrincipalAccountRepository.java
│   │   │   │   ├── VendorLinkedAccountRepository.java
│   │   │   │   ├── VendorRepository.java
│   │   │   │   ├── LoanApplicationRepository.java
│   │   │   │   └── LoanApplicationDecisionRepository.java
│   │   │   ├── domain/                                    # JPA entities (8 files)
│   │   │   │   ├── Consumer.java
│   │   │   │   ├── PrincipalAccount.java
│   │   │   │   ├── VendorLinkedAccount.java
│   │   │   │   ├── Vendor.java
│   │   │   │   ├── LoanApplication.java
│   │   │   │   ├── LoanApplicationDecision.java
│   │   │   │   ├── Loan.java
│   │   │   │   ├── Repayment.java
│   │   │   │   ├── enums/                                 # Enums (6 files)
│   │   │   │   └── embedded/                              # Embedded types (2 files)
│   │   │   ├── dto/                                       # Data Transfer Objects
│   │   │   │   ├── request/                               # Request DTOs (9 files)
│   │   │   │   └── response/                              # Response DTOs (9 files)
│   │   │   ├── exception/                                 # Exception handling (7 files)
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ValidationException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── BusinessRuleException.java
│   │   │   │   ├── UnauthorizedException.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── config/                                    # Configuration
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── event/                                     # Event listeners (4 files)
│   │   │   │   ├── OnboardingCompletedEvent.java
│   │   │   │   ├── PrincipalAccountCreationListener.java
│   │   │   │   ├── VendorAccountCreationListener.java
│   │   │   │   └── LoanApplicationApprovedEvent.java
│   │   │   ├── converter/                                 # JPA converters (2 files)
│   │   │   │   ├── EncryptedFieldConverter.java
│   │   │   │   └── UUIDConverter.java
│   │   │   └── util/                                      # Utility classes (4 files)
│   │   │       ├── EncryptionService.java
│   │   │       ├── ValidationUtil.java
│   │   │       ├── DateUtil.java
│   │   │       └── UUIDUtil.java
│   │   └── resources/
│   │       ├── application.yml                            # Main configuration
│   │       ├── application-docker.yml                     # Docker profile
│   │       └── db/migration/                              # Flyway migrations (8 files)
│   │           ├── V1__Create_consumers_table.sql
│   │           ├── V2__Create_principal_accounts_table.sql
│   │           ├── V3__Create_vendors_table.sql
│   │           ├── V4__Create_vendor_linked_accounts_table.sql
│   │           ├── V5__Insert_sample_vendors.sql
│   │           ├── V6__Create_loan_applications_table.sql
│   │           ├── V7__Create_loan_application_decisions_table.sql
│   │           └── V8__Update_encrypted_field_lengths.sql
│   └── test/
│       ├── java/com/infobeans/consumerfinance/
│       │   ├── controller/                                # Controller tests (5 files)
│       │   ├── service/                                   # Service tests (5 files)
│       │   ├── exception/                                 # Exception tests (1 file)
│       │   ├── event/                                     # Event tests (1 file)
│       │   └── config/
│       │       └── TestSecurityConfig.java                # Test security config
│       └── resources/
│           └── application.yml                            # Test configuration
├── docker/
│   └── mysql/
│       └── init/                                          # MySQL init scripts
├── target/                                                # Maven build output
│   ├── classes/                                           # Compiled classes
│   ├── test-classes/                                      # Compiled test classes
│   ├── surefire-reports/                                  # Test reports
│   └── consumer-finance-service-1.0.0.jar                 # Executable JAR
├── .claude/                                               # Claude AI config
├── pom.xml                                                # Maven configuration
├── Dockerfile                                             # Multi-stage build
├── docker-compose.yml                                     # Container orchestration
├── .env.example                                           # Environment template
├── .gitignore                                             # Git ignore patterns
└── Documentation/                                         # Project documentation
    ├── PROJECT_SETUP.md
    ├── AUTH0_SETUP.md
    ├── DOCKER.md
    ├── CONSUMER_ONBOARDING_IMPLEMENTATION.md
    ├── API_TEST_RESULTS.md
    ├── APPLICATION_STARTUP_SUCCESS.md
    ├── CONVERSATION_SUMMARY.md
    ├── PRODUCTION_REFACTORING.md
    ├── GIT_SETUP_GUIDE.md
    └── GIT_SETUP_COMMANDS.md
```

---

## Build & Deployment

### Maven Build Configuration

**POM Location:** `/Users/harish.pathak/consumer-finance-service/pom.xml`

**Main Class:** `com.infobeans.consumerfinance.ConsumerFinanceServiceApplication`

**Maven Plugins:**
- `spring-boot-maven-plugin` - Spring Boot packaging
- `maven-compiler-plugin` v3.11.0 - Java 17 compilation with Lombok
- `maven-surefire-plugin` v3.1.2 - Test execution
- `maven-jar-plugin` v3.3.0 - JAR creation

### Build Commands

#### 1. Clean Build with Tests
```bash
/Users/harish.pathak/Projects/apache-maven-3.9.6/bin/mvn clean package
```

#### 2. Build without Tests (faster)
```bash
/Users/harish.pathak/Projects/apache-maven-3.9.6/bin/mvn clean package -DskipTests
```

#### 3. Run Tests Only
```bash
/Users/harish.pathak/Projects/apache-maven-3.9.6/bin/mvn test
```

#### 4. Run Specific Test
```bash
/Users/harish.pathak/Projects/apache-maven-3.9.6/bin/mvn test -Dtest=ConsumerControllerTest
```

#### 5. Install to Local Maven Repository
```bash
/Users/harish.pathak/Projects/apache-maven-3.9.6/bin/mvn clean install
```

### Docker Build & Deployment

#### Dockerfile (Multi-stage Build)

**Stage 1: Builder**
- Base Image: `maven:3.9-amazoncorretto-17`
- Dependency caching optimization
- Build JAR with tests skipped

**Stage 2: Runtime**
- Base Image: `amazoncorretto:17-alpine`
- Non-root user: `appuser`
- JVM Options: `-Xms512m -Xmx1024m`, G1GC
- Health Check: `/api/v1/health`

#### Build Docker Image
```bash
docker build -t consumer-finance-service:latest .
```

#### Tag for GitHub Container Registry
```bash
docker tag consumer-finance-service:latest ghcr.io/harish-pathak/consumer-finance-service:latest
```

#### Push to Registry
```bash
docker push ghcr.io/harish-pathak/consumer-finance-service:latest
```

### Docker Compose Deployment

#### 1. Create .env file
```bash
cp .env.example .env
# Edit .env with your configuration
```

#### 2. Start Services
```bash
docker-compose up -d
```

#### 3. View Logs
```bash
docker-compose logs -f app
docker-compose logs -f mysql
```

#### 4. Stop Services
```bash
docker-compose down
```

#### 5. Rebuild and Restart
```bash
docker-compose up -d --build
```

#### 6. Clean Everything
```bash
docker-compose down -v  # Removes volumes (data loss!)
```

### Local Development Run

#### Using Maven
```bash
/Users/harish.pathak/Projects/apache-maven-3.9.6/bin/mvn spring-boot:run
```

#### Using Java
```bash
java -jar target/consumer-finance-service-1.0.0.jar
```

#### With Profile
```bash
java -jar -Dspring.profiles.active=docker target/consumer-finance-service-1.0.0.jar
```

### Health Checks

**Application Health:**
```bash
curl http://localhost:8080/api/v1/health
```

**Actuator Health:**
```bash
curl http://localhost:8080/actuator/health
```

**Database Connection Test:**
```bash
mysql -h localhost -P 3307 -u root -p
# Enter password: root@123
```

---

## Git Repository Information

### Repository Details

**Remote URL (SSH):**
```
git@github.com:harish-pathak/consumer-finance-service.git
```

**Remote URL (HTTPS):**
```
https://github.com/harish-pathak/consumer-finance-service.git
```

### Git Configuration

```
User Name: harish-pathak
User Email: harish.pathak@infobeans.com
Remote Name: origin
```

### Branch Information

**Main Branch:** `main`
**Current Branch:** `fix/dependency-injection-and-test-fixes`

**Branch Tracking:**
```
main → origin/main
fix/dependency-injection-and-test-fixes → origin/fix/dependency-injection-and-test-fixes
```

### Recent Commits (on current branch)

```
250a38f - test: Add comprehensive unit tests for ConsumerController
bef178f - fix: Update Auth0 configuration in Docker setup for fresh installations
a565483 - feat: Add Docker Compose setup with complete containerization
37c14cf - feat: Enable application-level encryption and remove deprecated auth
b588995 - fix: Add default values to deprecated JWT components and fix test
```

### Git Status (at conversation start)
```
Working Directory: Clean (no uncommitted changes)
Branch: fix/dependency-injection-and-test-fixes
```

---

## Testing Framework

### Test Configuration

**Test Framework:** JUnit 5 (Jupiter)
**Mocking:** Mockito
**Integration Testing:** MockMvc + Spring Boot Test
**Test Database:** H2 in-memory

**Security:** Disabled via `TestSecurityConfig` (all requests permitted)
**Flyway:** Disabled in test profile
**OAuth2:** Disabled (empty issuer-uri)

### Test Coverage

**Total Test Files:** 13

#### Controller Tests (6 files)
1. `ConsumerControllerTest` - Consumer onboarding endpoint tests
2. `PrincipalAccountControllerTest` - Account management tests
3. `VendorLinkedAccountControllerTest` - Vendor account tests
4. `LoanApplicationControllerTest` - Loan application tests
5. `LoanDecisionControllerTest` - Decision workflow tests

#### Service Tests (5 files)
1. `ConsumerServiceImplTest` - Consumer service logic
2. `PrincipalAccountServiceImplTest` - Account service logic
3. `VendorLinkedAccountServiceImplTest` - Vendor service logic
4. `LoanApplicationServiceImplTest` - Loan application logic
5. `LoanDecisionServiceImplTest` - Decision service logic

#### Other Tests (2 files)
1. `GlobalExceptionHandlerTest` - Exception handling verification
2. `PrincipalAccountCreationListenerTest` - Event listener tests

### Test Patterns

**Controller Test Structure:**
```java
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Controller Integration Tests")
class ControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private Service service;

    @Test
    @WithMockUser
    @DisplayName("Test description")
    void testMethod() throws Exception {
        // Test implementation
    }
}
```

**Service Test Structure:**
```java
@ExtendWith(MockitoExtension.class)
class ServiceImplTest {
    @Mock private Repository repository;
    @InjectMocks private ServiceImpl service;

    @Test
    void testMethod() {
        // Mock setup
        when(repository.findById(any())).thenReturn(Optional.of(entity));

        // Execute
        Result result = service.method(params);

        // Verify
        verify(repository).findById(any());
        assertNotNull(result);
    }
}
```

### Test Reports

**Location:** `/Users/harish.pathak/consumer-finance-service/target/surefire-reports/`

**Format:** JUnit XML reports for all test classes

---

## Quick Reference Commands

### Maven Commands

```bash
# Set Maven path (add to .bashrc or .zshrc)
export PATH="/Users/harish.pathak/Projects/apache-maven-3.9.6/bin:$PATH"

# Build
mvn clean package                    # Full build with tests
mvn clean package -DskipTests        # Build without tests
mvn clean install                    # Install to local repo

# Test
mvn test                             # Run all tests
mvn test -Dtest=ClassName            # Run specific test
mvn test -Dtest=ClassName#methodName # Run specific test method

# Run
mvn spring-boot:run                  # Run application

# Clean
mvn clean                            # Clean target directory
```

### Docker Commands

```bash
# Build
docker build -t consumer-finance-service:latest .

# Run standalone
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/consumer_finance \
  consumer-finance-service:latest

# Docker Compose
docker-compose up -d                 # Start services
docker-compose down                  # Stop services
docker-compose logs -f app           # Follow app logs
docker-compose logs -f mysql         # Follow MySQL logs
docker-compose ps                    # List services
docker-compose restart app           # Restart app service
docker-compose down -v               # Remove volumes (data loss!)

# Image management
docker images                        # List images
docker rmi consumer-finance-service  # Remove image
docker system prune -a               # Clean everything
```

### Database Commands

```bash
# Connect to local MySQL
mysql -h localhost -P 3306 -u root -p

# Connect to Docker MySQL
mysql -h localhost -P 3307 -u root -p

# Inside MySQL
USE consumer_finance;
SHOW TABLES;
DESCRIBE consumers;
SELECT * FROM consumers;

# Flyway info
SELECT * FROM flyway_schema_history;
```

### Application URLs

```bash
# Health Check
curl http://localhost:8080/api/v1/health

# Swagger UI
open http://localhost:8080/swagger-ui.html

# OpenAPI JSON
curl http://localhost:8080/v3/api-docs

# Actuator Health
curl http://localhost:8080/actuator/health
```

### Git Commands

```bash
# Status
git status
git log --oneline -10

# Branch
git branch -a
git checkout main
git checkout -b feature/new-feature

# Commit
git add .
git commit -m "feat: Add new feature"

# Push
git push origin branch-name

# Pull
git pull origin main
```

---

## Appendix

### Key Files Reference

| File | Purpose | Location |
|------|---------|----------|
| pom.xml | Maven configuration | `/Users/harish.pathak/consumer-finance-service/pom.xml` |
| application.yml | Main config | `src/main/resources/application.yml` |
| application-docker.yml | Docker config | `src/main/resources/application-docker.yml` |
| Dockerfile | Container build | `/Users/harish.pathak/consumer-finance-service/Dockerfile` |
| docker-compose.yml | Orchestration | `/Users/harish.pathak/consumer-finance-service/docker-compose.yml` |
| SecurityConfig.java | Security config | `src/main/java/.../config/SecurityConfig.java` |
| GlobalExceptionHandler | Error handling | `src/main/java/.../exception/GlobalExceptionHandler.java` |

### Port Usage

| Service | Port | Description |
|---------|------|-------------|
| Application | 8080 | Spring Boot REST API |
| MySQL (Local) | 3306 | Local MySQL instance |
| MySQL (Docker) | 3307→3306 | Docker MySQL mapped port |

### Common Issues & Solutions

**Issue:** `Could not find or load main class`
**Solution:** Run `mvn clean package` to rebuild

**Issue:** `Communications link failure` (MySQL)
**Solution:** Verify MySQL is running and port is correct

**Issue:** `Flyway validation failed`
**Solution:** Check migration file checksums, or use `flyway.baseline-on-migrate=true`

**Issue:** OAuth2 authentication fails
**Solution:** Verify Auth0 issuer-uri is correct and accessible

**Issue:** Encrypted field decryption fails
**Solution:** Ensure ENCRYPTION_KEY matches the key used to encrypt data

---

## Document Maintenance

**Created:** December 10, 2025
**Last Updated:** December 10, 2025
**Maintainer:** Development Team

**Update This Document When:**
- New features are added
- Database schema changes
- Configuration changes
- New dependencies added
- Deployment process changes
- Security configuration updates

---

**End of Document**
