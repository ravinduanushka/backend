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


## Rules for Claude Code

- Do NOT run `git commit`, `git push`, `git merge`, or create branches. The developer does all Git work in IntelliJ
- Do one task at a time. Stop after each task and say what to test
- Follow `docs/api` (endpoints, JSON) and `docs/database` (tables) exactly. If they conflict with the task, ask first
- Stack: Java 17, Spring Boot 4.0.8, Spring Cloud 2025.1.3, SQLite, Flyway. Use Boot 4 starter names (`spring-boot-starter-webmvc`, `spring-boot-starter-flyway`)
- Folders per service: `config`, `controller`, `dto`, `entity` (not `model`), `exception`, `repository`, `service`, `client`, `scheduler` as needed. `resources` is under `src/main`, next to `java`
- Comments: short, single line, only where needed
- After editing, build with Maven and fix errors before reporting done
- Never touch another developer's service without being asked
- Never commit secrets. Use `.env` and `.env.example`
- Maven (IntelliJ bundled), use if `mvn` is not found: `"D:\Program Files\JetBrains\IntelliJ IDEA\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"`. Run it from the module folder, for example `backend-parent\user-service`

## Rules for Claude Code
- Do NOT run `git commit`, `git push`, `git merge`, or create branches. The developer does all Git work in IntelliJ
- Do one task at a time. Stop after each task and say what to test
- Follow `docs/api` (endpoints, JSON) and `docs/database` (tables) exactly. If they conflict with the task, ask first. Never edit `docs/api` or `docs/database` without being asked
- Stack: Java 17, Spring Boot 4.0.8, Spring Cloud 2025.1.3, SQLite, Flyway. Use Boot 4 starter names (`spring-boot-starter-webmvc`, `spring-boot-starter-flyway`)
- Folders per service: `config`, `controller`, `dto`, `entity` (not `model`), `exception`, `repository`, `service`, `client`, `scheduler` as needed. `resources` is under `src/main`, next to `java`
- Comments: short, single line, only where needed
- After editing, build with Maven and fix errors before reporting done
- Never touch another developer's service without being asked
- Never commit secrets. Use `.env` and `.env.example`
- Maven (IntelliJ bundled), use if `mvn` is not found: "D:\Program Files\JetBrains\IntelliJ IDEA\plugins\maven-plugin\lib\maven3\bin\mvn.cmd". Run it from the module folder, for example `backend-parent\user-service`
- **Errors use the one shape in `docs/api/00-conventions.md` (timestamp, status, error, message, path, fieldErrors)**
- **Until Phase 5 (Asgardeo), auth is a dev filter reading headers `X-Dev-User-Id`, `X-Dev-Email`, `X-Dev-Role`. Missing `X-Dev-User-Id` gives 401. Keep it in `config` so JWT can replace it**
- **Do not add features, endpoints, or libraries that the task did not ask for**

## API check rules
- After building an endpoint, tell me the Postman request to add (method, URL, headers, body, expected status for success, 400, 403, 404/409)
- After building an endpoint, set `Built` to ✅ in `docs/api/API-CHECKS.md`. Never mark the test columns. The developer marks them after running the requests