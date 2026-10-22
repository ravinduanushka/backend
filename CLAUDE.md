# MentorConnect Backend

## Project
- 7 Spring Boot microservices + Eureka discovery server
- One SQLite file per service (`<service>.db`)
- Auth: WSO2 Asgardeo JWT. Storage: Cloudflare R2. Payments: Lemon Squeezy
- Ports: discovery 8761, user 8081, mentor 8082, booking 8083, payment 8084, mentorship 8085, notification 8086, analytic 8087
- Repo: github.com/ravinduanushka/backend, branch `dev`. Two developers (ravinduanushka, dasunisenanayake25)

## Workflow
- Follow the roadmap phase by phase, one step at a time. Do not jump ahead
- Give direct code with the file path and short steps. No zip files
- The user creates files in IntelliJ, pastes code and commits. Suggest commits only
- IntelliJ uses the VS Code keymap. Do not use shortcuts like Ctrl+K. Use menus (Version Control dropdown -> Commit / Push)
- Explain to the user in Sinhala. Keep code and identifiers in English

## Code style
- Comments: short, single line, only where essential (non-obvious rules). No banner or section comments
- Commit messages: single line, short, no body (e.g. `add booking schema`)

## Roadmap
1. Planning: DB schema + ER diagram (done), API specification (next), task split
2. Setup: project skeleton, DB connection, migrations
3. Core logic: entities, services, controllers
4. Testing: Postman collection and local tests
5. Auth and integrations: Asgardeo, R2, Lemon Squeezy
6. Final testing and deployment

## Database
- Schema files: `docs/database/*.sql` (one per service). ER diagrams: `docs/database/er-diagram.md`
- Phase 2: copy each SQL file to `<service>/src/main/resources/db/migration/V1__init.sql`
- Ids are UUID as TEXT. Timestamps are UTC ISO-8601 TEXT
- Enums are TEXT with CHECK. Money is stored in cents
- Ids that point to another service have no foreign key
- Roles: STUDENT, MENTOR, ADMIN
- SQLite has no row locks. Double booking is blocked by unique index `uq_bookings_active_slot` and `availability_slots.version` (@Version)