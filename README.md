Veridian IT Support - Internal Service Agent

An internal IT helpdesk agent for Veridian Corp. The application uses deterministic, policy-grounded rules for the assignment scenarios and an optional LLM fallback only when the request cannot be resolved by a known rule.

Workflow

Employee Request
       ↓
Knowledge Retrieval
       ↓
Policy Matching
       ↓
Agent Decision
       ↓
Ticket Creation
       ↓
Audit Logging
       ↓
Response

The backend seeds KB-01 through KB-10, the Asset Management Policy, REQ-01 through REQ-15, and the existing TK-1042 through TK-1051 history queue. Historical tickets are surfaced as context and do not replace a new decision.

Requirements

Java 21

Maven

Node.js 18+

MySQL 8+

Configuration

Set the variables from backend/.env.example in your shell, IDE launch configuration, or local environment file. Spring Boot does not load .env files automatically. No secrets are committed to the repository.

Required database variables

DB_URL=jdbc:mysql://localhost:3306/veridian_agent?createDatabaseIfNotExist=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your-local-password
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173

The URL and username have safe local defaults. DB_PASSWORD must be supplied when your MySQL user requires a password; it is intentionally never stored in the repository.

Optional LLM variables

OPENAI_API_KEY=
OPENAI_MODEL=gpt-4o-mini
OPENAI_BASE_URL=https://api.openai.com/v1

The app works without an LLM key. LLM requests are policy-grounded, JSON-constrained, and time-limited; failures fall back to a safe follow-up response.

Run

Create the database if it does not already exist:

CREATE DATABASE veridian_agent;

Start the backend with the default local H2 database:

cd backend
mvn spring-boot:run

The default profile stores its local database under backend/data/, which is ignored by Git. For MySQL, set DB_URL, DB_USERNAME, and DB_PASSWORD before running the same command.

You can also use the explicit H2 development profile:

cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

Use the MySQL variables when running against a shared or production database.

Start the frontend in another terminal:

cd frontend
npm install
npm run dev

Open http://localhost:5173.

API

POST /api/agent/chat

GET /api/requests

GET /api/tickets

GET /api/tickets/{id}

GET /api/audit/{requestId}

Invalid request payloads return HTTP 400 with a safe validation message. Unexpected backend failures return a generic HTTP 500 response without stack traces or implementation details.

Tests

Run all backend tests:

cd backend
mvn clean test

The assignment test suite covers all 15 employee requests plus guest Wi-Fi no-ticket behavior, phishing safety, ambiguous requests, and historical ticket context. Build the frontend with:

cd frontend
npm run build

Policy behavior highlights

Guest Wi-Fi is resolved without creating an IT ticket.

Security incidents escalate to Security and explicitly prohibit forwarding suspicious email.

Contractor VPN access routes for manager approval.

Non-catalog software and browser extensions route to Security review.

Hardware replacement never receives an unsupported automatic approval; IT verification and Finance & Assets review are required where the two refresh policies overlap.

Expense access is owned by Finance; IT only handles technical login issues after an account exists.

Ambiguous requests ask for the missing service, device, error, and expected behavior.