<<<<<<< HEAD
# Jobconnect
=======
# JobConnect

A full-stack job portal built with **Spring Boot**, **MongoDB**, and **Thymeleaf**, supporting role-based access for **Employers** and **Job Seekers**, JWT-based authentication, an SMS notification module, and a Dockerized deployment.

## Features

| Module | Description |
|---|---|
| **User Authentication & Role Management** | Registration/login secured with Spring Security + JWT. Users choose `JOB_SEEKER` or `EMPLOYER` at sign-up; roles gate access to the rest of the app. |
| **Job Posting (Employer)** | Employers can create, edit, and delete job listings (title, description, location, salary, deadline). Listings are visible to all job seekers. |
| **Job Search (Seeker)** | Job seekers browse/search listings by keyword and/or location, view full details, and apply. |
| **SMS Notification** | An SMS module (Twilio-backed, with an automatic MOCK/logging fallback when no credentials are configured) sends messages for registration confirmation, job posting alerts, and application updates. |
| **Docker** | Multi-stage `Dockerfile` + `docker-compose.yml` (app + MongoDB) for one-command startup. |
| **Testing** | JUnit 5 + Mockito unit tests for the service layer, and a `@WebMvcTest` slice test verifying role-based route security. |

## Tech Stack

- Java 17, Spring Boot 3.3
- Spring Web, Spring Security, Spring Data MongoDB, Thymeleaf
- JJWT (JSON Web Tokens)
- Twilio SDK (SMS)
- Lombok
- JUnit 5, Mockito, AssertJ, Spring Security Test

## Project Structure

```
src/main/java/com/jobconnect/
  config/        SecurityConfig, JwtAuthFilter
  controller/    AuthController, JobController, HomeController
  dto/           RegisterRequest, LoginRequest, JobRequest
  exception/     Custom exceptions + GlobalExceptionHandler
  model/         User, Role, Job, JobApplication
  repository/    Spring Data MongoDB repositories
  service/       UserService, JobService, SmsNotificationService (+ Twilio impl)
  util/          JwtUtil
src/main/resources/
  templates/     Thymeleaf pages (login, register, jobs/*)
  static/css/    Stylesheet
  application.yml
src/test/java/com/jobconnect/
  service/       UserServiceTest, JobServiceTest (JUnit + Mockito)
  controller/    JobControllerTest (MockMvc + Spring Security Test)
Dockerfile
docker-compose.yml
```

## Running Locally (without Docker)

1. Start a local MongoDB instance on `localhost:27017` (or point `MONGODB_URI` elsewhere).
2. Build and run:
   ```bash
   mvn clean package
   java -jar target/jobconnect.jar
   ```
3. Visit `http://localhost:8080`.

By default, SMS notifications run in **MOCK mode** — messages are written to the application log instead of being sent, so the whole flow works without any Twilio account. To send real SMS, set:
```bash
export SMS_PROVIDER=TWILIO
export TWILIO_ACCOUNT_SID=xxxx
export TWILIO_AUTH_TOKEN=xxxx
export TWILIO_FROM_NUMBER=+1xxxxxxxxxx
```

## Running with Docker

```bash
docker compose up --build
```

This starts MongoDB and the app together. The app will be available at `http://localhost:8080`. Data persists in the `mongo-data` named volume.

To enable real SMS delivery in Docker, add the Twilio environment variables to the `app` service in `docker-compose.yml`.

## Running Tests

```bash
mvn test
```

- `UserServiceTest` — registration success/duplicate-email paths, password hashing, SMS trigger, lookup-by-email.
- `JobServiceTest` — job posting/ownership checks, search filtering combinations, application submission + duplicate prevention, SMS notification triggers.
- `JobControllerTest` — verifies public routes are open, and that `/jobs/post` and `/jobs/apply/**` correctly enforce `EMPLOYER`/`JOB_SEEKER` roles via the real `SecurityConfig`.

## Key Routes

| Route | Method | Access | Purpose |
|---|---|---|---|
| `/register`, `/login` | GET/POST | Public | Account creation & authentication |
| `/jobs`, `/jobs/search` | GET | Public | Browse / search job listings |
| `/jobs/view/{id}` | GET | Public | Job details |
| `/jobs/post` | GET/POST | EMPLOYER | Create a job listing |
| `/jobs/edit/{id}`, `/jobs/delete/{id}` | GET/POST | EMPLOYER (owner only) | Edit/delete own listing |
| `/jobs/employer/jobs` | GET | EMPLOYER | Dashboard of own postings |
| `/jobs/employer/applications/{jobId}` | GET | EMPLOYER (owner only) | View applicants for a job |
| `/jobs/apply/{id}` | POST | JOB_SEEKER | Apply to a job |
| `/jobs/seeker/applications` | GET | JOB_SEEKER | View own applications |
| `/api/auth/register`, `/api/auth/login` | POST | Public | JSON API variants (returns a JWT) |

## Notes

- JWT is issued at login and stored as an **HttpOnly cookie** (`jwt`) so the Thymeleaf web UI and the JSON API share a single authentication mechanism (`JwtAuthFilter` reads from either the cookie or the `Authorization: Bearer` header).
- `SecurityConfig` is stateless (`SessionCreationPolicy.STATELESS`) — the JWT is the sole source of truth on every request.
- Passwords are hashed with BCrypt before being stored in MongoDB.
>>>>>>> 9f85d6c (Adding all required files)
