# UprzejmieDonosze

Spring Boot web application where users file reports against other users for breaking defined rules ("paragraphs"), and administrators review them. Built as a team project for a Java web development course, with a focus on **authentication, authorization and secure configuration**.

Team: **Mateusz Żmiejko**, **Gabriel Kossakowski**. The UI is in Polish.

## Features

- **Accounts**: registration with validated input, login/logout, profile editing, password change
- **Reports**: create, edit and delete reports linked to a reported user and a rule; status flow `NEW → ACCEPTED / REJECTED`
- **Share links**: a report can be shared with a read-only link based on a token generated in the database
- **Admin panel**: dashboard with statistics, user management, rule ("paragraph") management
- **REST API** `/api/reports` (GET / PUT / DELETE) with JSON errors from a central exception handler
- **Database layer**: Oracle with PL/SQL triggers (share tokens, guarding rule deletion), a statistics view and indexes in [`src/main/resources/db`](src/main/resources/db)

## Security

- Passwords hashed with **BCrypt**
- **Role-based access** (`USER` / `ADMIN`) configured in Spring Security: `/admin/**`, `/users/**`, `/paragraphs/**` are admin-only, everything else except public pages requires login
- **Ownership checks**: a user can modify only their own reports, an admin can modify any (MVC and REST)
- **CSRF protection** (Spring Security default) on all forms
- **Bean Validation** on every form (`@NotBlank`, `@Size`, `@PastOrPresent`, …)
- Custom **403 / 404 / 500** pages, no stack traces shown to users
- **No secrets in the repository**: database and admin passwords come from environment variables or a local `.env` file, with no default passwords in the code
- Integration tests for access control (`SecurityEpicIntegrationTests`, `AdminDashboardIntegrationTests`, `ReportRestControllerIntegrationTests`)

### Known limitations
- Share tokens come from Oracle `SYS_GUID()`, which can be partly sequential; a random token from `SecureRandom` would be safer
- Minimum password length is 6 characters
- `spring.jpa.hibernate.ddl-auto=update` and SQL logging are meant for development only

## Tech stack

`Java 17` · `Spring Boot` · `Spring Security` · `Spring Data JPA / Hibernate` · `Thymeleaf` · `Bean Validation` · `Oracle DB` (PL/SQL) · `H2` (tests) · `JUnit 5` · `Mockito` · `Maven`

## Getting started

### Requirements
- JDK 17+
- Oracle Database (e.g. Oracle XE) with a user for the app

### Configuration
Copy the example file and set your own values:

```bash
cp .env.example .env
```

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:oracle:thin:@//localhost:1521/XEPDB1` |
| `DB_USERNAME` | database user | `UPRZEJMIE` |
| `DB_PASSWORD` | database password | **required** |
| `ADMIN_USERNAME` | admin login created on first start | `admin` |
| `ADMIN_PASSWORD` | admin password created on first start | **required** |

The admin account is created only when no admin exists yet. Environment variables set in the system override values from `.env`.

### Run

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

The app starts at http://localhost:8080.

### Tests

```bash
./mvnw test
```

Tests use an in-memory H2 database (`src/test/resources/application.properties`), so they don't need Oracle.

## Project structure

```
src/main/java/com/projekt/uprzejmiedonosze/
├── config/       Spring Security setup, admin account bootstrap
├── controller/   MVC controllers, api/ REST controller + exception handler
├── dto/          validated form objects
├── model/        JPA entities: AppUser, Report, Paragraph
├── repository/   Spring Data repositories
└── service/      business logic, statistics, share links
src/main/resources/
├── templates/    Thymeleaf views
└── db/           PL/SQL triggers, view and indexes
docs/             database schema (DBML, PDF)
```
