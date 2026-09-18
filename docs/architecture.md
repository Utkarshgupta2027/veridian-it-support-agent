# Architecture

```text
EMPLOYEE
   |
   v
React + Vite
   | REST/JSON
   v
Spring Boot API
   |
   v
AgentService
   |
   +--> Employee Request
   +--> Knowledge Retrieval (KB-01..KB-10 + Asset Management)
   +--> Ticket History Retrieval (closed and active context)
   +--> Deterministic Policy Matching
   |       |
   |       +--> Resolve / Follow Up / Route / Escalate
   |       +--> No-ticket outcome where policy says no IT ticket
   |
   +--> Optional grounded LLM fallback for unknown language
   |
   +--> Ticket Creation when required
   +--> Audit Logging
   +--> Structured Response: decision, reason, source, department,
       ticket, next action, history context
```

Principle: **Application rules control actions; the database supplies policy/history evidence; the optional LLM only helps interpret unknown language and cannot select an un-retrieved policy.**

No authentication, microservices, vector DB, external ticketing, Slack, mobile app, or admin dashboard are included because they are outside the supplied 6-hour MVP scope.
