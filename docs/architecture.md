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
   |------------------|
   v                  v
MySQL              LLM API
KB/requests/       structured JSON
tickets/audit
   \                  /
    \                /
     v              v
       Decision
      /    |     \
 Resolve Follow-up Escalate/Route
      \    |     /
       Create Ticket
             |
         Audit Log
             |
      Response + Source
```

Principle: **LLM for understanding; application code for controlled actions; database for evidence/state; audit log for traceability.**

No authentication, microservices, vector DB, external ticketing, Slack, mobile app, or admin dashboard are included because they are outside the supplied 6-hour MVP scope.
