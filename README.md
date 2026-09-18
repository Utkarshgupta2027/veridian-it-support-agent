# Veridian IT Support Agent — 6-Hour MVP

Stack: React + Vite → Spring Boot → Agent Service → MySQL + LLM API.

## Included
- Employee request form
- Agent REST API
- Grounded knowledge retrieval
- Structured LLM integration (optional API key)
- Safe deterministic rules for the explicitly documented scenarios
- RESOLVE / FOLLOW_UP / ESCALATE / ROUTE_TO_OTHER_DEPARTMENT
- Ticket creation
- Audit trail
- Source display
- Request/ticket/audit APIs
- No authentication
- Demo UI

## Important
The supplied assignment says to use only the supplied Veridian source material and not invent policies. This MVP therefore includes only policy facts explicitly present in the supplied architecture/analysis:
KB-01 password lockout, KB-02 VPN credential renewal, KB-09 phishing/security escalation, and Finance ownership of expense-system access.

Before final submission, load the complete official KB-01..KB-10 + Asset Management Policy data pack. Do not invent missing policy text.

## Run
1. Create MySQL database:
```sql
CREATE DATABASE veridian_agent;
```

2. Configure:
`backend/src/main/resources/application.properties`

3. Backend:
```bat
cd backend
mvn spring-boot:run
```

4. Frontend:
```bat
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

## Optional LLM
Set:
```text
OPENAI_API_KEY=...
OPENAI_MODEL=gpt-4o-mini
OPENAI_BASE_URL=https://api.openai.com/v1
```

Without a key the app still works using safe deterministic/follow-up behavior.

## APIs
POST /api/agent/chat
GET /api/requests
GET /api/tickets
GET /api/tickets/{id}
GET /api/audit/{requestId}

## Demo
- Password lockout → RESOLVE → KB-01
- VPN credentials expired → RESOLVE → KB-02
- Phishing → ESCALATE → KB-09 → Security
- Expense access → ROUTE_TO_OTHER_DEPARTMENT → Finance
- Vague laptop issue → FOLLOW_UP
