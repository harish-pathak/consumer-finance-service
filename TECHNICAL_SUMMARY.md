# Consumer Finance Service - Technical Implementation Summary

**Document Version:** 1.0
**Last Updated:** December 10, 2025
**Purpose:** Technical implementation guide for developers

---

## Table of Contents

1. [Technology Stack Overview](#technology-stack-overview)
2. [Project Structure & Architecture](#project-structure--architecture)
3. [Database Schema Creation Strategy](#database-schema-creation-strategy)
4. [Database Migration Management (Flyway)](#database-migration-management-flyway)
5. [Vendor Sample Data Seeding](#vendor-sample-data-seeding)
6. [OAuth2 Authentication Implementation](#oauth2-authentication-implementation)
7. [Request Validation Framework](#request-validation-framework)
8. [Docker Multi-Stage Build Architecture](#docker-multi-stage-build-architecture)
9. [Exception Handling Strategy](#exception-handling-strategy)
10. [Testing Framework & Configuration](#testing-framework--configuration)

---

## Technology Stack Overview

### Core Technologies

```yaml
Platform:
  Language: Java 17 (LTS)
  Runtime: Amazon Corretto JDK 17
  Build Tool: Apache Maven 3.9.6
  Maven Path: /Users/harish.pathak/Projects/apache-maven-3.9.6/bin

Framework:
  Core: Spring Boot 3.2.0
  Parent: spring-boot-starter-parent 3.2.0

Database:
  Production: MySQL 8.0 (InnoDB, UTF8MB4)
  Testing: H2 in-memory
  Migration: Flyway Core + Flyway MySQL

Container Platform:
  Build: Docker Multi-stage
  Orchestration: Docker Compose v3.8
  Base Image (Build): maven:3.9-amazoncorretto-17
  Base Image (Runtime): amazoncorretto:17-alpine
```

### Spring Boot Dependencies

#### Web Layer
```xml
<!-- REST API with embedded Tomcat -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- OpenAPI 3.0 / Swagger UI Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.1.0</version>
</dependency>
```

#### Data Access Layer
```xml
<!-- JPA/Hibernate ORM -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- MySQL JDBC Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Database Migrations -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

#### Security Layer
```xml
<!-- Spring Security Core -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Resource Server (JWT Validation) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- DEPRECATED: JJWT (backward compatibility) -->
<!-- Will be removed in future release -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
```

#### Validation & Processing
```xml
<!-- JSR-380 Bean Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Encryption Support -->
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
</dependency>
```

#### Development Tools
```xml
<!-- Boilerplate Reduction -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <optional>true</optional>
</dependency>

<!-- Hot Reload -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### Testing Stack

```xml
<!-- Spring Boot Test (includes JUnit 5, Mockito, AssertJ, etc.) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Security Testing -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- H2 In-Memory Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- JUnit 5 Jupiter -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

**Testing Framework Components:**
- **JUnit 5 (Jupiter)** - Test execution and lifecycle
- **Mockito** - Mocking and stubbing
- **MockMvc** - REST API integration testing
- **@SpringBootTest** - Full application context testing
- **@WebMvcTest** - Controller slice testing
- **@WithMockUser** - Security context simulation
- **H2 Database** - Fast in-memory database for tests
- **Hamcrest Matchers** - Fluent assertions

---

## Project Structure & Architecture

### Layered Architecture Pattern

```
┌─────────────────────────────────────────────────┐
│           Presentation Layer                     │
│  (REST Controllers - API Endpoints)              │
│  - ConsumerController                            │
│  - LoanApplicationController                     │
│  - PrincipalAccountController                    │
└──────────────────┬──────────────────────────────┘
                   │ DTOs (Request/Response)
                   ↓
┌─────────────────────────────────────────────────┐
│             Service Layer                        │
│  (Business Logic & Orchestration)                │
│  - ConsumerService                               │
│  - LoanApplicationService                        │
│  - LoanDecisionService                           │
└──────────────────┬──────────────────────────────┘
                   │ Domain Models
                   ↓
┌─────────────────────────────────────────────────┐
│          Repository Layer                        │
│  (Data Access - Spring Data JPA)                 │
│  - ConsumerRepository                            │
│  - LoanApplicationRepository                     │
│  - PrincipalAccountRepository                    │
└──────────────────┬──────────────────────────────┘
                   │ JPA/Hibernate
                   ↓
┌─────────────────────────────────────────────────┐
│            Database Layer                        │
│  (MySQL 8.0 with InnoDB Engine)                  │
│  - consumers, loan_applications, etc.            │
└─────────────────────────────────────────────────┘
```

### Cross-Cutting Concerns

```
┌─────────────────────────────────────────────────┐
│         Security Layer (OAuth2 JWT)              │
│  - SecurityConfig (OAuth2 Resource Server)       │
│  - JWT validation via Auth0 JWK Set             │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│      Exception Handling (@RestControllerAdvice)  │
│  - GlobalExceptionHandler                        │
│  - Custom exceptions with HTTP status mapping    │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│         Event-Driven Workflow                    │
│  - OnboardingCompletedEvent                      │
│  - PrincipalAccountCreationListener              │
│  - LoanApplicationApprovedEvent                  │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│         Data Encryption (AES-256-GCM)            │
│  - EncryptionService                             │
│  - EncryptedFieldConverter (JPA)                 │
└─────────────────────────────────────────────────┘
```

### Directory Structure

```
src/main/java/com/infobeans/consumerfinance/
│
├── ConsumerFinanceServiceApplication.java  ← Entry point (@SpringBootApplication)
│
├── controller/                              ← REST endpoints
│   ├── ConsumerController.java             ← POST /api/v1/consumers
│   ├── PrincipalAccountController.java     ← Account management
│   ├── VendorLinkedAccountController.java  ← Vendor linking
│   ├── LoanApplicationController.java      ← Loan operations
│   ├── HealthController.java               ← Health checks
│   └── TestController.java                 ← Test endpoints
│
├── service/                                 ← Business logic
│   ├── ConsumerService.java                ← Interface
│   ├── PrincipalAccountService.java
│   ├── VendorLinkedAccountService.java
│   ├── LoanApplicationService.java
│   ├── LoanDecisionService.java
│   └── impl/                                ← Implementations
│       ├── ConsumerServiceImpl.java        ← @Service, @Transactional
│       └── ...
│
├── repository/                              ← Data access (Spring Data JPA)
│   ├── ConsumerRepository.java             ← extends JpaRepository<Consumer, String>
│   ├── PrincipalAccountRepository.java
│   ├── VendorLinkedAccountRepository.java
│   ├── VendorRepository.java
│   ├── LoanApplicationRepository.java
│   └── LoanApplicationDecisionRepository.java
│
├── domain/                                  ← JPA entities
│   ├── Consumer.java                        ← @Entity, @Table("consumers")
│   ├── PrincipalAccount.java
│   ├── VendorLinkedAccount.java
│   ├── Vendor.java
│   ├── LoanApplication.java
│   ├── LoanApplicationDecision.java
│   ├── Loan.java
│   ├── Repayment.java
│   ├── enums/                               ← Enumerations
│   │   ├── AccountStatus.java              ← ACTIVE, DISABLED, ARCHIVED
│   │   ├── LoanApplicationStatus.java      ← PENDING, APPROVED, REJECTED
│   │   └── ...
│   └── embedded/                            ← Embeddable types
│       ├── EmploymentDetails.java          ← @Embeddable
│       └── IncomeDetails.java              ← @Embeddable
│
├── dto/                                     ← Data Transfer Objects
│   ├── request/                             ← API request DTOs
│   │   ├── CreateConsumerOnboardingRequest.java  ← @Valid, JSR-380
│   │   ├── CreateLoanApplicationRequest.java
│   │   └── ...
│   └── response/                            ← API response DTOs
│       ├── ConsumerOnboardingResponse.java
│       ├── LoanApplicationResponse.java
│       └── ...
│
├── exception/                               ← Custom exceptions
│   ├── GlobalExceptionHandler.java         ← @RestControllerAdvice
│   ├── ValidationException.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── BusinessRuleException.java
│   ├── UnauthorizedException.java
│   └── ErrorResponse.java                  ← Standard error response
│
├── config/                                  ← Configuration classes
│   └── SecurityConfig.java                 ← @Configuration, OAuth2 setup
│
├── event/                                   ← Event-driven architecture
│   ├── OnboardingCompletedEvent.java       ← Application event
│   ├── PrincipalAccountCreationListener.java  ← @EventListener
│   ├── VendorAccountCreationListener.java
│   └── LoanApplicationApprovedEvent.java
│
├── converter/                               ← JPA converters
│   ├── EncryptedFieldConverter.java        ← @Converter, encrypt/decrypt
│   └── UUIDConverter.java
│
└── util/                                    ← Utility classes
    ├── EncryptionService.java              ← AES-256-GCM encryption
    ├── ValidationUtil.java
    ├── DateUtil.java
    └── UUIDUtil.java
```

---

## Database Schema Creation Strategy

### Hibernate DDL-Auto Strategy: `validate`

**Configuration:**
```yaml
# src/main/resources/application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # ← PRODUCTION SAFE: Never auto-creates/modifies schema
```

### How Schema Creation Works

#### Strategy: **Migration-Only Schema Creation**

The application uses a **defensive schema management strategy**:

1. **Hibernate does NOT create/modify schema** (`ddl-auto: validate`)
2. **Schema is ONLY created via Flyway migrations**
3. **Schema validation happens on startup**

#### Step-by-Step Process

```
Application Startup
│
├─ Step 1: Flyway Migration Check
│  │
│  ├─ Checks: Does table 'flyway_schema_history' exist?
│  │   ├─ NO  → Creates 'flyway_schema_history' table
│  │   └─ YES → Reads migration history
│  │
│  ├─ Compares: Filesystem migrations vs Database history
│  │   ├─ New migrations found → Executes in order (V1, V2, V3...)
│  │   └─ No new migrations → Skips
│  │
│  └─ Executes: Each SQL migration transactionally
│      ├─ V1__Create_consumers_table.sql
│      ├─ V2__Create_principal_accounts_table.sql
│      ├─ ... (all migrations)
│      └─ Records each success in flyway_schema_history
│
├─ Step 2: Hibernate Schema Validation (ddl-auto: validate)
│  │
│  ├─ Scans: All @Entity classes
│  ├─ Compares: Entity mappings vs Actual database schema
│  │   ├─ MATCH    → Application starts successfully ✅
│  │   └─ MISMATCH → Application FAILS to start ❌
│  │
│  └─ Validation checks:
│      ├─ Table exists?
│      ├─ Column names match?
│      ├─ Column types compatible?
│      ├─ Constraints match?
│      └─ Relationships valid?
│
└─ Step 3: Application Ready
   └─ API endpoints become available
```

### Schema Creation "Only If Not Present" Mechanism

#### Flyway's Idempotent Approach

All migration scripts use **idempotent SQL**:

```sql
-- V1__Create_consumers_table.sql
CREATE TABLE IF NOT EXISTS consumers (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    -- ... other columns
);
```

**Key Feature: `IF NOT EXISTS`**

- **First Run:** Table doesn't exist → Creates table
- **Subsequent Runs:** Table exists → Skips creation (no error)
- **Flyway Tracking:** Records migration as executed in `flyway_schema_history`

#### Flyway Migration Tracking

**Table: `flyway_schema_history`**

```sql
SELECT * FROM flyway_schema_history;
```

| installed_rank | version | description | type | script | checksum | installed_on | success |
|----------------|---------|-------------|------|--------|----------|--------------|---------|
| 1 | 1 | Create consumers table | SQL | V1__Create_consumers_table.sql | -123456789 | 2025-12-10 10:30:00 | true |
| 2 | 2 | Create principal accounts table | SQL | V2__Create_principal_accounts_table.sql | -987654321 | 2025-12-10 10:30:01 | true |

**Migration Logic:**

1. **Check:** Is migration V1 in `flyway_schema_history`?
   - **NO** → Execute V1__Create_consumers_table.sql
   - **YES** → Skip (already executed)

2. **Checksum Validation:** Ensures migration files haven't been modified
   - **Match** → Migration is valid
   - **Mismatch** → ERROR: Migration file changed! (integrity violation)

### Configuration Options

#### Local Development (application.yml)
```yaml
spring:
  flyway:
    enabled: true                  # Enable Flyway
    locations: classpath:db/migration
    baseline-on-migrate: false     # Strict mode: require clean database
    validate-on-migrate: false     # Skip checksum validation (dev only)
```

#### Docker Deployment (application-docker.yml)
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true      # Lenient: handle existing databases
    locations: classpath:db/migration
```

**`baseline-on-migrate: true`** allows Flyway to manage databases that already have tables (useful for Docker fresh starts).

### Why This Approach?

✅ **Production Safety:** Never accidental schema changes
✅ **Version Control:** Schema is code (SQL files in Git)
✅ **Rollback Capability:** Can track and revert schema changes
✅ **Team Collaboration:** Everyone gets same schema version
✅ **Audit Trail:** Full history in `flyway_schema_history`
✅ **Idempotent:** Safe to run multiple times

❌ **What We Avoid:**
- Hibernate `ddl-auto: create` (drops tables on restart!)
- Hibernate `ddl-auto: update` (unpredictable schema changes)
- Manual SQL scripts (error-prone, no tracking)

---

## Database Migration Management (Flyway)

### Flyway Configuration

**Maven Dependency:**
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

**Spring Boot Configuration:**
```yaml
spring:
  flyway:
    enabled: true                       # Enable Flyway
    locations: classpath:db/migration   # Migration scripts location
    baseline-on-migrate: false          # Local: strict mode
    validate-on-migrate: false          # Skip validation in dev
```

### Migration File Structure

**Location:** `src/main/resources/db/migration/`

**Naming Convention:** `V{version}__{description}.sql`

```
db/migration/
├── V1__Create_consumers_table.sql
├── V2__Create_principal_accounts_table.sql
├── V3__Create_vendors_table.sql
├── V4__Create_vendor_linked_accounts_table.sql
├── V5__Insert_sample_vendors.sql              ← Data migration
├── V6__Create_loan_applications_table.sql
├── V7__Create_loan_application_decisions_table.sql
└── V8__Update_encrypted_field_lengths.sql     ← Schema update
```

**File Naming Rules:**
- **Prefix:** `V` (versioned migration) or `R` (repeatable)
- **Version:** Integer (e.g., 1, 2, 3) or semantic (e.g., 1.0, 1.1)
- **Separator:** `__` (double underscore)
- **Description:** Snake_case description
- **Extension:** `.sql`

### Migration Types

#### 1. Schema Creation (DDL)

**Example: V1__Create_consumers_table.sql**
```sql
-- Create table with IF NOT EXISTS for idempotency
CREATE TABLE IF NOT EXISTS consumers (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT 'Unique consumer identifier (UUID)',
    first_name VARCHAR(100) NOT NULL COMMENT 'Consumer first name',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT 'Consumer email',
    -- ... other columns

    -- Indexes for performance
    INDEX idx_email (email),
    INDEX idx_status (status),

    -- Constraints
    CONSTRAINT check_email_format CHECK (email LIKE '%@%')
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Consumer profiles with onboarding data';
```

#### 2. Schema Modification (ALTER)

**Example: V8__Update_encrypted_field_lengths.sql**
```sql
-- Increase column length for encrypted fields (Base64 expansion)
ALTER TABLE consumers MODIFY COLUMN national_id VARCHAR(500);
ALTER TABLE consumers MODIFY COLUMN document_number VARCHAR(500);
ALTER TABLE consumers MODIFY COLUMN employer_name VARCHAR(500);
```

#### 3. Data Migration (DML)

**Example: V5__Insert_sample_vendors.sql**
```sql
-- Insert sample vendors for testing
INSERT INTO vendors (id, name, status, created_at, updated_at)
VALUES
('550e8400-e29b-41d4-a716-446655440111', 'Bank of America', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440222', 'Wells Fargo', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440333', 'Chase Bank', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440444', 'Citibank', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440555', 'PayPal', 'INACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE status=status;  -- Idempotent: skip if exists
```

**Key Feature: `ON DUPLICATE KEY UPDATE status=status`**
- If vendor UUID already exists → No error, skip insertion
- Makes migration safe to run multiple times

### Migration Execution Flow

```
┌─────────────────────────────────────────────────────────────┐
│ Application Startup                                          │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 1: Flyway Initialization                                │
│ - Connect to database                                        │
│ - Check if flyway_schema_history exists                     │
│   - NO  → Create flyway_schema_history table                │
│   - YES → Continue                                           │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 2: Scan Migration Files                                │
│ - Read all files in classpath:db/migration/                 │
│ - Parse version numbers (V1, V2, V3, ...)                   │
│ - Calculate checksum for each file                          │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 3: Compare with Database History                       │
│ - Query: SELECT * FROM flyway_schema_history                │
│ - Identify pending migrations (not in history)              │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 4: Execute Pending Migrations (Transactional)          │
│                                                              │
│ FOR EACH pending migration:                                 │
│   BEGIN TRANSACTION                                          │
│     1. Execute SQL script                                    │
│     2. INSERT INTO flyway_schema_history                     │
│        (version, description, checksum, success)             │
│   COMMIT                                                     │
│                                                              │
│ If any migration fails → ROLLBACK, stop application         │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 5: Validation (Optional)                               │
│ - Verify checksums match                                     │
│ - Ensure no out-of-order migrations                         │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 6: Application Continues Startup                       │
│ - Hibernate validates schema                                │
│ - Application becomes ready                                 │
└─────────────────────────────────────────────────────────────┘
```

### Adding New Migrations

#### Process:

1. **Create new SQL file** with next version number
   ```bash
   touch src/main/resources/db/migration/V9__Add_credit_score_column.sql
   ```

2. **Write SQL script** (idempotent if possible)
   ```sql
   ALTER TABLE consumers ADD COLUMN credit_score INT;
   ALTER TABLE consumers ADD INDEX idx_credit_score (credit_score);
   ```

3. **Restart application** (or wait for next deployment)
   - Flyway detects new migration
   - Executes V9 migration
   - Records in `flyway_schema_history`

4. **Commit to Git**
   ```bash
   git add src/main/resources/db/migration/V9__Add_credit_score_column.sql
   git commit -m "feat: Add credit score tracking"
   ```

### Migration Best Practices

✅ **DO:**
- Use `IF NOT EXISTS` for CREATE TABLE
- Use `ON DUPLICATE KEY UPDATE` for INSERT
- Test migrations on development database first
- Keep migrations small and focused
- Add descriptive comments
- Version migrations sequentially

❌ **DON'T:**
- Modify existing migration files (breaks checksum)
- Use `DROP TABLE` without backup
- Put complex logic in migrations
- Skip version numbers
- Delete migration files from filesystem

---

## Vendor Sample Data Seeding

### Implementation: V5__Insert_sample_vendors.sql

**Location:** `src/main/resources/db/migration/V5__Insert_sample_vendors.sql`

**Purpose:** Pre-populate vendor master data for testing and development

### SQL Script

```sql
-- Insert sample vendors for testing vendor account creation
INSERT INTO vendors (id, name, status, created_at, updated_at)
VALUES
('550e8400-e29b-41d4-a716-446655440111', 'Bank of America', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440222', 'Wells Fargo', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440333', 'Chase Bank', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440444', 'Citibank', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440555', 'PayPal', 'INACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE status=status;
```

### Key Features

#### 1. Idempotent Insertion

**`ON DUPLICATE KEY UPDATE status=status`**

This clause makes the migration **idempotent** (safe to run multiple times):

```
First Run:
  - Vendors don't exist → INSERT executes → 5 rows added

Second Run:
  - Vendors exist (primary key conflict) → UPDATE executes → No change
  - Status updated to same value (no-op)
  - No error thrown

Nth Run:
  - Same as second run → No error, no changes
```

#### 2. UUID-Based Primary Keys

Fixed UUIDs ensure consistency across environments:

```
Development:  Vendor 'Bank of America' = '550e8400-e29b-41d4-a716-446655440111'
Testing:      Vendor 'Bank of America' = '550e8400-e29b-41d4-a716-446655440111'
Production:   Vendor 'Bank of America' = '550e8400-e29b-41d4-a716-446655440111'
```

Benefits:
- Test data uses same UUIDs across environments
- Foreign key relationships remain consistent
- Migration scripts can reference these UUIDs

#### 3. Execution Order

Flyway executes migrations in version order:

```
V3__Create_vendors_table.sql        ← Creates vendors table
   ↓
V4__Create_vendor_linked_accounts_table.sql  ← Creates FK to vendors
   ↓
V5__Insert_sample_vendors.sql       ← Populates vendor data
```

**Dependency Chain:**
- V5 depends on V3 (table must exist)
- V4 depends on V3 (foreign key constraint)
- Flyway ensures correct order by version number

### Seeded Vendor Data

| ID | Name | Status |
|----|------|--------|
| 550e8400-e29b-41d4-a716-446655440111 | Bank of America | ACTIVE |
| 550e8400-e29b-41d4-a716-446655440222 | Wells Fargo | ACTIVE |
| 550e8400-e29b-41d4-a716-446655440333 | Chase Bank | ACTIVE |
| 550e8400-e29b-41d4-a716-446655440444 | Citibank | ACTIVE |
| 550e8400-e29b-41d4-a716-446655440555 | PayPal | INACTIVE |

### Testing Usage

**API Test Example:**
```bash
# Link consumer to Bank of America
POST /api/v1/vendor-accounts
{
  "consumerId": "c1234567-89ab-cdef-0123-456789abcdef",
  "vendorId": "550e8400-e29b-41d4-a716-446655440111",  ← Known UUID
  "principalAccountId": "p9876543-21fe-dcba-9876-543210fedcba"
}
```

### Adding More Vendors

To add more vendors in future:

**Create new migration:** V10__Insert_additional_vendors.sql
```sql
INSERT INTO vendors (id, name, status, created_at, updated_at)
VALUES
('550e8400-e29b-41d4-a716-446655440666', 'American Express', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440777', 'Stripe', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE status=status;
```

---

## OAuth2 Authentication Implementation

### Architecture: OAuth2 Resource Server

**Pattern:** Stateless JWT-based authentication using Auth0 as Identity Provider (IdP)

```
┌──────────────┐                    ┌──────────────┐
│   Client     │                    │    Auth0     │
│  (Postman,   │                    │  (Identity   │
│   Mobile,    │                    │   Provider)  │
│   Web App)   │                    │              │
└──────┬───────┘                    └──────┬───────┘
       │                                   │
       │ 1. Request Access Token           │
       │ POST /oauth/token                 │
       │ (username, password, client_id)   │
       ├──────────────────────────────────>│
       │                                   │
       │ 2. Return JWT Access Token        │
       │    (signed with Auth0 private key)│
       │<──────────────────────────────────┤
       │                                   │
       │                                   │
       │ 3. API Request with JWT           │
       │ Authorization: Bearer <token>     │
       ├─────────────────────┐             │
       │                     ▼             │
       │            ┌──────────────────┐   │
       │            │  Consumer Finance │   │
       │            │     Service       │   │
       │            │  (Spring Boot)    │   │
       │            └────────┬──────────┘   │
       │                     │              │
       │                     │ 4. Validate JWT
       │                     │ - Fetch JWK Set from Auth0
       │                     │ - Verify signature
       │                     │ - Check issuer
       │                     │ - Verify expiration
       │                     ├─────────────>│
       │                     │              │
       │                     │ 5. Return JWK Set
       │                     │<──────────────┤
       │                     │              │
       │                     │ 6. Extract claims
       │ 7. Return API Response             │
       │<────────────────────┤              │
       │                                   │
```

### SecurityConfig Implementation

**File:** `src/main/java/com/infobeans/consumerfinance/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String oauth2IssuerUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (stateless API with JWT)
            .csrf(csrf -> csrf.disable())

            // Stateless session management
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules
            .authorizeHttpRequests(authz -> authz
                // ===== PUBLIC ENDPOINTS =====
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/api/v1/test/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // ===== PROTECTED ENDPOINTS (JWT REQUIRED) =====
                .requestMatchers("/api/v1/consumers/**").authenticated()
                .requestMatchers("/api/v1/loan-applications/**").authenticated()
                .requestMatchers("/api/v1/principal-accounts/**").authenticated()
                .requestMatchers("/api/v1/vendor-accounts/**").authenticated()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            );

        // Configure OAuth2 Resource Server (JWT validation)
        if (StringUtils.hasText(oauth2IssuerUri)) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {
                    // Auto-configured from issuer-uri:
                    // - Fetches JWK Set from Auth0
                    // - Validates signature, issuer, expiration
                    log.debug("OAuth2 JWT Resource Server configured");
                })
            );
            log.info("✅ PRODUCTION MODE: OAuth2 enabled with Auth0");
        } else {
            log.warn("⚠️  TEST MODE: OAuth2 disabled");
        }

        return http.build();
    }
}
```

### Configuration

**application.yml (Local Development):**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://dev-nhmhs0r5umunbtao.us.auth0.com/
          # JWK Set URI auto-configured: {issuer-uri}/.well-known/jwks.json
```

**application-docker.yml (Docker):**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI}
          jwk-set-uri: ${SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI}
```

### JWT Validation Process

#### Automatic Validation by Spring Security

```
┌─────────────────────────────────────────────────────────────┐
│ Incoming Request: POST /api/v1/consumers                    │
│ Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...│
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 1: Extract JWT from Authorization Header               │
│ - Parse "Bearer <token>"                                     │
│ - Extract token: eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...    │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 2: Fetch JWK Set from Auth0 (Cached)                   │
│ - URL: https://dev-nhmhs0r5umunbtao.us.auth0.com/          │
│        .well-known/jwks.json                                │
│ - Contains public keys for signature verification           │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 3: Verify JWT Signature                                │
│ - Decode JWT header to get 'kid' (key ID)                   │
│ - Find matching public key in JWK Set                       │
│ - Verify signature using RSA-256 algorithm                  │
│ - Result: VALID or INVALID                                  │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 4: Validate JWT Claims                                 │
│ - Issuer (iss): Must match configured issuer-uri            │
│ - Expiration (exp): Must be in the future                   │
│ - Not Before (nbf): Must be in the past (optional)          │
│ - Audience (aud): Optional validation                       │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 5: Extract User Information                            │
│ - Subject (sub): User ID                                    │
│ - Email (email): User email                                 │
│ - Roles (roles): User roles/permissions                     │
│ - Custom claims: Any additional data                        │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 6: Set Security Context                                │
│ - Create Authentication object                              │
│ - Store in SecurityContextHolder                            │
│ - Available via @AuthenticationPrincipal                    │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 7: Proceed to Controller                               │
│ - User is authenticated                                     │
│ - Business logic executes                                   │
│ - Response returned                                         │
└─────────────────────────────────────────────────────────────┘
```

### Error Handling

**Invalid/Missing Token:**
```json
HTTP 401 Unauthorized

{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed",
  "timestamp": "2025-12-10T10:30:00",
  "path": "/api/v1/consumers"
}
```

**Handled by:** `GlobalExceptionHandler.handleAuthenticationException()`

### Public vs Protected Endpoints

#### Public (No Token Required)
```
GET  /api/v1/health/**           ← Health checks
GET  /api/v1/test/**             ← Test endpoints (disable in prod!)
GET  /swagger-ui/**              ← API documentation
GET  /v3/api-docs/**             ← OpenAPI JSON
GET  /actuator/health            ← Actuator health
```

#### Protected (JWT Required)
```
POST /api/v1/consumers/**              ← Consumer onboarding
GET  /api/v1/consumers/{id}            ← Get consumer
POST /api/v1/loan-applications/**      ← Loan applications
GET  /api/v1/principal-accounts/**     ← Account operations
POST /api/v1/vendor-accounts/**        ← Vendor linking
```

### Testing with OAuth2

**Get Token from Auth0:**
```bash
curl --request POST \
  --url https://dev-nhmhs0r5umunbtao.us.auth0.com/oauth/token \
  --header 'content-type: application/json' \
  --data '{
    "client_id": "YOUR_CLIENT_ID",
    "client_secret": "YOUR_CLIENT_SECRET",
    "audience": "https://consumer-finance-api",
    "grant_type": "client_credentials"
  }'
```

**Use Token in API Request:**
```bash
curl -X POST http://localhost:8080/api/v1/consumers \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    ...
  }'
```

---

## Request Validation Framework

### JSR-380 Bean Validation

**Standard:** Jakarta Bean Validation (JSR-380)
**Implementation:** Hibernate Validator (included in Spring Boot)

### Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### Validation Flow

```
┌────────────────────────────────────────────────────────────┐
│ HTTP Request: POST /api/v1/consumers                       │
│ Body: { "firstName": "", "email": "invalid-email" }        │
└───────────────┬────────────────────────────────────────────┘
                │
                ▼
┌────────────────────────────────────────────────────────────┐
│ Step 1: Controller Method (with @Valid)                    │
│                                                             │
│ @PostMapping("/consumers")                                 │
│ public ResponseEntity<?> onboard(                          │
│     @Valid @RequestBody CreateConsumerOnboardingRequest req│
│ ) {                                                         │
│     ...                                                     │
│ }                                                           │
└───────────────┬────────────────────────────────────────────┘
                │
                ▼
┌────────────────────────────────────────────────────────────┐
│ Step 2: Spring Validation Interceptor                      │
│ - Triggered by @Valid annotation                           │
│ - Invokes Hibernate Validator                              │
└───────────────┬────────────────────────────────────────────┘
                │
                ▼
┌────────────────────────────────────────────────────────────┐
│ Step 3: Validate Each Field                                │
│                                                             │
│ firstName: ""                                               │
│   @NotBlank → ❌ FAIL: "First name is required"            │
│                                                             │
│ email: "invalid-email"                                      │
│   @Email → ❌ FAIL: "Email must be a valid email address"  │
└───────────────┬────────────────────────────────────────────┘
                │
                ▼
┌────────────────────────────────────────────────────────────┐
│ Step 4: Throw MethodArgumentNotValidException              │
│ - Contains all validation errors                           │
│ - Includes field names and error messages                  │
└───────────────┬────────────────────────────────────────────┘
                │
                ▼
┌────────────────────────────────────────────────────────────┐
│ Step 5: GlobalExceptionHandler Catches Exception           │
│                                                             │
│ @ExceptionHandler(MethodArgumentNotValidException.class)   │
│ public ResponseEntity<ErrorResponse> handle(...) {         │
│     // Extract field errors                                │
│     Map<String, String> errors = ...                       │
│     return ErrorResponse with 400 Bad Request              │
│ }                                                           │
└───────────────┬────────────────────────────────────────────┘
                │
                ▼
┌────────────────────────────────────────────────────────────┐
│ HTTP Response: 400 Bad Request                             │
│                                                             │
│ {                                                           │
│   "status": 400,                                            │
│   "error": "Validation Error",                             │
│   "message": "Request validation failed",                  │
│   "details": "{firstName=First name is required,           │
│                email=Email must be a valid email address}" │
│ }                                                           │
└────────────────────────────────────────────────────────────┘
```

### DTO Validation Example

**File:** `CreateConsumerOnboardingRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConsumerOnboardingRequest {

    // ========== PERSONAL INFORMATION ==========

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$",
             message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$",
             message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = "^[+]?[0-9]{10,20}$",
             message = "Phone number must be a valid format (10-20 digits, optional + prefix)")
    private String phone;

    @PastOrPresent(message = "Date of birth cannot be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    // ========== IDENTITY INFORMATION ==========

    @NotBlank(message = "National ID is required")
    @Size(min = 5, max = 50, message = "National ID must be between 5 and 50 characters")
    private String nationalId;

    @NotBlank(message = "Document type is required")
    @Size(max = 50, message = "Document type must not exceed 50 characters")
    private String documentType;

    @Size(max = 100, message = "Document number must not exceed 100 characters")
    private String documentNumber;

    // ========== EMPLOYMENT INFORMATION ==========

    @Size(max = 255, message = "Employer name must not exceed 255 characters")
    private String employerName;

    @Size(max = 100, message = "Position must not exceed 100 characters")
    private String position;

    @Size(max = 50, message = "Employment type must not exceed 50 characters")
    private String employmentType;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 80, message = "Years of experience must be realistic")
    private Integer yearsOfExperience;

    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    // ========== FINANCIAL INFORMATION ==========

    @DecimalMin(value = "0.0", inclusive = false,
                message = "Monthly income must be greater than 0")
    @Digits(integer = 15, fraction = 2,
            message = "Monthly income must be a valid monetary amount")
    private BigDecimal monthlyIncome;

    @DecimalMin(value = "0.0", inclusive = false,
                message = "Annual income must be greater than 0")
    @Digits(integer = 15, fraction = 2,
            message = "Annual income must be a valid monetary amount")
    private BigDecimal annualIncome;

    @Size(max = 255, message = "Income source must not exceed 255 characters")
    private String incomeSource;

    @Size(min = 3, max = 3, message = "Currency code must be a 3-letter ISO 4217 code")
    private String currency;
}
```

### Validation Annotations Reference

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@NotNull` | Field must not be null | `@NotNull private String id` |
| `@NotBlank` | String must not be null, empty, or whitespace | `@NotBlank private String firstName` |
| `@NotEmpty` | Collection/array must not be empty | `@NotEmpty private List<String> items` |
| `@Size` | String/collection size constraints | `@Size(min=2, max=100)` |
| `@Min` / `@Max` | Numeric range validation | `@Min(0) @Max(150)` |
| `@Email` | Email format validation | `@Email private String email` |
| `@Pattern` | Regex validation | `@Pattern(regexp="^[A-Z0-9]+$")` |
| `@Past` / `@Future` | Date must be in past/future | `@Past private LocalDate dob` |
| `@PastOrPresent` | Date must be in past or today | `@PastOrPresent` |
| `@DecimalMin` / `@DecimalMax` | Decimal range | `@DecimalMin("0.0")` |
| `@Digits` | Numeric precision | `@Digits(integer=15, fraction=2)` |
| `@Positive` / `@Negative` | Positive/negative numbers | `@Positive private Integer age` |
| `@Valid` | Cascade validation to nested objects | `@Valid private Address address` |

### Controller Integration

```java
@RestController
@RequestMapping("/api/v1/consumers")
public class ConsumerController {

    @PostMapping
    public ResponseEntity<ConsumerOnboardingResponse> onboard(
        @Valid @RequestBody CreateConsumerOnboardingRequest request  // ← @Valid triggers validation
    ) {
        // Validation already passed if we reach here
        ConsumerOnboardingResponse response = consumerService.onboardConsumer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### Exception Handler

**File:** `GlobalExceptionHandler.java`

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, WebRequest request) {

    log.warn("Validation failed on request body");

    // Extract field-level validation errors
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
        if (error instanceof FieldError) {
            FieldError fieldError = (FieldError) error;
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
    });

    ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Request validation failed")
            .error("Validation Error")
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .details(fieldErrors.toString())
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
}
```

### Validation Error Response

**Request:**
```json
POST /api/v1/consumers
{
  "firstName": "",
  "lastName": "D",
  "email": "invalid-email",
  "phone": "123",
  "nationalId": "AB",
  "monthlyIncome": -1000
}
```

**Response: 400 Bad Request**
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Request validation failed",
  "timestamp": "2025-12-10T10:30:00",
  "path": "/api/v1/consumers",
  "details": "{firstName=First name is required, lastName=Last name must be between 2 and 100 characters, email=Email must be a valid email address, phone=Phone number must be a valid format (10-20 digits, optional + prefix), nationalId=National ID must be between 5 and 50 characters, monthlyIncome=Monthly income must be greater than 0}"
}
```

---

## Docker Multi-Stage Build Architecture

### Dockerfile Structure

**File:** `Dockerfile`

```dockerfile
# ==============================================================================
# Stage 1: Build Stage (maven:3.9-amazoncorretto-17)
# ==============================================================================
FROM maven:3.9-amazoncorretto-17 AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration first (layer caching optimization)
COPY pom.xml .

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Stage 2: Runtime Stage (amazoncorretto:17-alpine)
# ==============================================================================
FROM amazoncorretto:17-alpine

# Metadata labels
LABEL maintainer="consumer-finance-team"
LABEL application="consumer-finance-service"
LABEL version="1.0.0"
LABEL description="Spring Boot microservice for consumer lending operations"

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user (security best practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/consumer-finance-service-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap-dump.hprof"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Multi-Stage Build Benefits

#### Stage 1: Builder (maven:3.9-amazoncorretto-17)

**Size:** ~800 MB
**Purpose:** Compile and package application

```
Includes:
- Maven 3.9
- Amazon Corretto JDK 17 (full JDK)
- Build tools
- All Maven dependencies
```

**Build Process:**
```bash
1. Copy pom.xml
2. Download dependencies → mvn dependency:go-offline
   ↓ (This layer is cached if pom.xml doesn't change)
3. Copy source code
4. Build JAR → mvn clean package -DskipTests
   ↓ (Produces: target/consumer-finance-service-1.0.0.jar)
```

#### Stage 2: Runtime (amazoncorretto:17-alpine)

**Size:** ~200 MB (significantly smaller!)
**Purpose:** Run application in production

```
Includes ONLY:
- Amazon Corretto JRE 17 (runtime only, no compiler)
- Alpine Linux (minimal OS)
- curl (for health checks)
- Application JAR
```

**What's NOT included:**
- Maven
- Source code
- Build tools
- Test dependencies
- Maven local repository

### Layer Caching Optimization

```
Docker Build Layers:
┌──────────────────────────────────────────┐
│ Layer 1: Base image (maven:3.9-corretto) │  ← Cached (rarely changes)
├──────────────────────────────────────────┤
│ Layer 2: COPY pom.xml                    │  ← Cached (if pom.xml unchanged)
├──────────────────────────────────────────┤
│ Layer 3: RUN mvn dependency:go-offline   │  ← Cached (if pom.xml unchanged)
├──────────────────────────────────────────┤
│ Layer 4: COPY src                        │  ← Rebuilt (if code changed)
├──────────────────────────────────────────┤
│ Layer 5: RUN mvn clean package           │  ← Rebuilt (if code changed)
└──────────────────────────────────────────┘

Build Time:
- First build: ~5 minutes (download all dependencies)
- Subsequent builds (code change only): ~30 seconds (cached dependencies)
```

### Security Best Practices

#### 1. Non-Root User

```dockerfile
# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser
```

**Why?**
- Prevents privilege escalation attacks
- Limits container permissions
- Follows principle of least privilege

#### 2. Minimal Base Image (Alpine Linux)

```dockerfile
FROM amazoncorretto:17-alpine
```

**Benefits:**
- Small attack surface (fewer packages)
- Reduced vulnerabilities
- Faster image pulls

#### 3. Health Checks

```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/health || exit 1
```

**Purpose:**
- Container orchestrators (Docker, Kubernetes) can detect unhealthy containers
- Automatic restart of failed containers
- Load balancers can route traffic only to healthy instances

### JVM Production Configuration

```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap-dump.hprof"
```

| Option | Purpose |
|--------|---------|
| `-Xms512m` | Initial heap size: 512 MB |
| `-Xmx1024m` | Maximum heap size: 1 GB |
| `-XX:+UseG1GC` | Use G1 garbage collector (low latency) |
| `-XX:MaxGCPauseMillis=200` | Target GC pause time: 200ms |
| `-XX:+HeapDumpOnOutOfMemoryError` | Generate heap dump on OOM |
| `-XX:HeapDumpPath=/app/logs/heap-dump.hprof` | Heap dump location |

### Build Commands

#### 1. Build Docker Image
```bash
docker build -t consumer-finance-service:latest .
```

#### 2. Tag for Registry
```bash
docker tag consumer-finance-service:latest \
  ghcr.io/harish-pathak/consumer-finance-service:latest
```

#### 3. Push to GitHub Container Registry
```bash
docker push ghcr.io/harish-pathak/consumer-finance-service:latest
```

#### 4. Run Container
```bash
docker run -d \
  --name consumer-finance-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/consumer_finance \
  -e ENCRYPTION_KEY=your-encryption-key \
  consumer-finance-service:latest
```

---

## Exception Handling Strategy

### Centralized Exception Handling

**Pattern:** `@RestControllerAdvice` (Spring's global exception handler)

**File:** `GlobalExceptionHandler.java`

```java
@RestControllerAdvice  // ← Applied to ALL @RestController classes
@Slf4j
public class GlobalExceptionHandler {

    // Custom business exceptions
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(...) { }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(...) { }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(...) { }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(...) { }

    // JSR-380 validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(...) { }

    // Database constraint violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(...) { }

    // Spring Security authentication errors
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(...) { }

    // Catch-all for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(...) { }
}
```

### Exception Mapping

| Exception | HTTP Status | Use Case |
|-----------|-------------|----------|
| `DuplicateResourceException` | 409 Conflict | Duplicate email, national ID, etc. |
| `ResourceNotFoundException` | 404 Not Found | Entity not found by ID |
| `ValidationException` | 400 Bad Request | Business logic validation failure |
| `UnauthorizedException` | 401 Unauthorized | Auth/authorization failure |
| `MethodArgumentNotValidException` | 400 Bad Request | JSR-380 @Valid failure |
| `DataIntegrityViolationException` | 409 Conflict | Database constraint violation |
| `AuthenticationException` | 401 Unauthorized | OAuth2/JWT validation failure |
| `Exception` (catch-all) | 500 Internal Server Error | Unexpected errors |

### StandardError Response Format

```java
@Data
@Builder
public class ErrorResponse {
    private int status;           // HTTP status code
    private String error;         // Error category
    private String message;       // Human-readable message
    private String details;       // Additional details (optional)
    private LocalDateTime timestamp;  // When error occurred
    private String path;          // Request path
}
```

**Example:**
```json
{
  "status": 409,
  "error": "Duplicate Resource",
  "message": "A consumer with this email already exists",
  "details": "Email must be unique",
  "timestamp": "2025-12-10T10:30:00",
  "path": "/api/v1/consumers"
}
```

---

## Testing Framework & Configuration

### Test Structure

```
src/test/java/com/infobeans/consumerfinance/
├── controller/              ← Integration tests (MockMvc)
│   ├── ConsumerControllerTest.java
│   ├── PrincipalAccountControllerTest.java
│   └── ...
├── service/                 ← Unit tests (Mockito)
│   ├── ConsumerServiceImplTest.java
│   ├── PrincipalAccountServiceImplTest.java
│   └── ...
├── exception/               ← Exception handler tests
│   └── GlobalExceptionHandlerTest.java
├── event/                   ← Event listener tests
│   └── PrincipalAccountCreationListenerTest.java
└── config/
    └── TestSecurityConfig.java  ← Disable security for tests
```

### Test Configuration

**File:** `src/test/resources/application.yml`

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ""  # ← Disable OAuth2 for tests

  flyway:
    enabled: false        # ← Disable Flyway (use H2 schema auto-creation)

  datasource:
    url: jdbc:h2:mem:testdb  # ← H2 in-memory database

logging:
  level:
    root: WARN
    com.infobeans.consumerfinance: INFO
```

**File:** `TestSecurityConfig.java`

```java
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary  // ← Overrides production SecurityConfig
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()  // ← Allow all requests (no auth)
            );
        return http.build();
    }
}
```

### Controller Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ConsumerController Integration Tests")
class ConsumerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsumerService consumerService;

    @Test
    @WithMockUser  // ← Simulates authenticated user
    @DisplayName("Should successfully onboard consumer")
    void testSuccessfulOnboarding() throws Exception {
        // Arrange
        CreateConsumerOnboardingRequest request = CreateConsumerOnboardingRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .nationalId("SSN123456789")
                .documentType("NATIONAL_ID")
                .build();

        ConsumerOnboardingResponse response = ConsumerOnboardingResponse.builder()
                .consumerId("c123")
                .message("Consumer onboarded successfully")
                .build();

        when(consumerService.onboardConsumer(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/consumers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consumerId").value("c123"))
                .andExpect(jsonPath("$.message").value("Consumer onboarded successfully"));

        verify(consumerService, times(1)).onboardConsumer(any());
    }
}
```

### Service Test Example

```java
@ExtendWith(MockitoExtension.class)
class ConsumerServiceImplTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Test
    void testOnboardConsumer_Success() {
        // Arrange
        CreateConsumerOnboardingRequest request = /* ... */;
        Consumer savedConsumer = /* ... */;

        when(consumerRepository.existsByEmail(anyString())).thenReturn(false);
        when(consumerRepository.save(any(Consumer.class))).thenReturn(savedConsumer);

        // Act
        ConsumerOnboardingResponse response = consumerService.onboardConsumer(request);

        // Assert
        assertNotNull(response);
        assertEquals(savedConsumer.getId(), response.getConsumerId());

        verify(consumerRepository).existsByEmail(request.getEmail());
        verify(consumerRepository).save(any(Consumer.class));
        verify(eventPublisher).publishEvent(any(OnboardingCompletedEvent.class));
    }

    @Test
    void testOnboardConsumer_DuplicateEmail_ThrowsException() {
        // Arrange
        CreateConsumerOnboardingRequest request = /* ... */;
        when(consumerRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
            () -> consumerService.onboardConsumer(request));

        verify(consumerRepository).existsByEmail(request.getEmail());
        verify(consumerRepository, never()).save(any());
    }
}
```

---

## Conclusion

This document provides a comprehensive technical overview of the Consumer Finance Service implementation, covering:

✅ Complete technology stack with testing frameworks
✅ Layered architecture and project structure
✅ Database schema creation with Flyway (idempotent migrations)
✅ OAuth2/Auth0 authentication implementation
✅ JSR-380 validation framework
✅ Docker multi-stage build optimization
✅ Centralized exception handling
✅ Comprehensive testing strategy

**For detailed API documentation:** http://localhost:8080/swagger-ui.html
**For complete codebase context:** See CODEBASE_CONTEXT.md

---

**Document Maintained By:** Development Team
**Last Updated:** December 10, 2025
