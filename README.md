# Car Insurance Project

## 1) Requirements (Software)

- **JDK 21**
- **Maven 3.9+**
- Any IDE (IntelliJ IDEA / VS Code with Java extension / Eclipse)
- Optional: **curl** or **Postman** for API testing

## 2) How to Run

Default ports and tools:
- API base URL: `http://localhost:8080`

### Sample requests

List cars with owners:
```bash
curl http://localhost:8080/api/cars
```

Check insurance validity (returns `{"carId":1,"date":"2025-01-01","valid":true|false}`):
```bash
curl "http://localhost:8080/api/cars/1/insurance-valid?date=2025-06-01"
```

Run tests:
```bash
mvn -q -DskipTests=false test
```

## 3) Business Description

The API manages **car insurance** information. Core concepts:

- **Owner** — a person who owns cars.
- **Car** — a vehicle associated with one Owner at a time (current design uses a simple `owner_id` on the `CAR` table).
- **InsurancePolicy** — a policy attached to a specific car and valid within a date interval `[startDate, endDate]` (inclusive). At most one policy may be **active** on a given date for a given car (not enforced yet).

Implemented features:
- List all cars with their owners.
- Check if a policy is active for a car on a given date.


## 4) Database Tables (Current & Proposed)

**OWNER**
- `ID` (BIGINT, PK, auto)
- `NAME` (VARCHAR, not null)
- `EMAIL` (VARCHAR, nullable)

**CAR**
- `ID` (BIGINT, PK, auto)
- `VIN` (VARCHAR, not null)
- `MAKE` (VARCHAR, null ok)
- `MODEL` (VARCHAR, null ok)
- `YEAR_OF_MANUFACTURE` (INT)
- `OWNER_ID` (BIGINT, FK → OWNER.ID, not null)

**INSURANCEPOLICY**
- `ID` (BIGINT, PK, auto)
- `CAR_ID` (BIGINT, FK → CAR.ID, not null)
- `PROVIDER` (VARCHAR, null ok)
- `START_DATE` (DATE, not null)
- `END_DATE` (DATE)

## 5) Candidate Tasks

Please treat these as production-quality changes: add validation, return proper HTTP status codes, and include minimal tests.

### A) Change existing code: Enforce policy end dates

Ensure all insurance policies have an **end date** and that this is enforced at both:
1. **API/JPA validation level** and
2. **Database level**

Acceptance criteria:
- Creating/updating a policy without `endDate` fails with 4xx and a helpful message.
- Existing open-ended sample data is fixed (use a default validity period of 1 year for existing policies).

### B) Add new code: Create two new functionalities

1. **Register an insurance claim** for a car
    - Endpoint suggestion: `POST /api/cars/{carId}/claims`
    - Request body: `claimDate`, `description`, `amount`
    - Returns created claim with 201 and location header. Validate all fields.

2. **Get the history of a car (regardless of owner)**
    - Endpoint suggestion: `GET /api/cars/{carId}/history`
    - Response is a chronological list of relevant events for that car.

Acceptance criteria:
- Clear, documented JSON shapes.
- Returns 404 when `carId` does not exist.

### C) Add validation: Protect the insurance validity check against invalid values

Harden `GET /api/cars/{carId}/insurance-valid`:

- Validate `carId` exists; return **404** if not.
- Validate `date` format (ISO `YYYY-MM-DD`); return **400** with a clear error if invalid.
- Reject **impossible** dates (e.g., outside supported ranges) with **400**.
- Add tests for these cases.

### D) Add a cron that logs within 1 hour after a policy expires

- Enable scheduling and implement a scheduled task that runs periodically.
- The task should log a message **at most 1 hour after** a policy’s `endDate` passes (e.g., “Policy {id} for car {carId} expired on {endDate}”).
- Keep it simple: assume `endDate` is a **date** (no time); consider “expired at midnight”.
- Make sure repeated runs do **not** spam logs for the same policy.

Acceptance criteria:
- When a policy expires, a log line appears once within an hour of expiry.

## 6) Expectations

- Have code committed to Github in your private repository. It will be shown and discussed at the technical discussion.

## 7) Demo (end-to-end)

## Pornește aplicația într-un terminal
mvn -q clean spring-boot:run
## API base URL: http://localhost:8080
### A) Enforce Policy End Date (creare / validări)

## Create policy (valid) → 201 Created (observă header-ul Location)
curl -i -X POST http://localhost:8080/api/cars/1/policies -H "Content-Type: application/json" -d "{\"provider\":\"TestCo\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-12-31\"}"

## Create fără endDate → 400 Bad Request
curl -i -X POST http://localhost:8080/api/cars/1/policies -H "Content-Type: application/json" -d "{\"provider\":\"BadCo\",\"startDate\":\"2026-01-01\"}"

## Create cu startDate > endDate → 400 Bad Request
curl -i -X POST http://localhost:8080/api/cars/1/policies -H "Content-Type: application/json" -d "{\"provider\":\"X\",\"startDate\":\"2026-12-31\",\"endDate\":\"2026-01-01\"}"

## Create pe mașină inexistentă → 404 Not Found
curl -i -X POST http://localhost:8080/api/cars/999/policies -H "Content-Type: application/json" -d "{\"provider\":\"X\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-12-31\"}"

## Update policy (înlocuiește {ID} cu valoarea din Location) → 200 OK
curl -i -X PUT http://localhost:8080/api/policies/{ID} -H "Content-Type: application/json" -d "{\"provider\":\"Y\",\"startDate\":\"2026-02-01\",\"endDate\":\"2026-12-31\"}"

### B) Claims & History

## Register claim (valid) → 201 Created
curl -i -X POST http://localhost:8080/api/cars/1/claims -H "Content-Type: application/json" -d "{\"claimDate\":\"2025-07-01\",\"description\":\"Rear bumper damage\",\"amount\":950.00}"

## Register claim pe mașină inexistentă → 404 Not Found
curl -i -X POST http://localhost:8080/api/cars/999/claims -H "Content-Type: application/json" -d "{\"claimDate\":\"2025-07-01\",\"description\":\"x\",\"amount\":10}"

## Register claim invalid (dată greșită) → 400 Bad Request
curl -i -X POST http://localhost:8080/api/cars/1/claims -H "Content-Type: application/json" -d "{\"claimDate\":\"2025-99-99\",\"description\":\"x\",\"amount\":10}"

## Istoricul mașinii (cronologic: POLICY_START/END + CLAIM) → 200 OK
curl -s http://localhost:8080/api/cars/1/history

### C) Validări GET /insurance-valid

## Car existent + dată validă → 200 OK
curl -i "http://localhost:8080/api/cars/1/insurance-valid?date=2025-06-01"

## Car inexistent → 404 Not Found
curl -i "http://localhost:8080/api/cars/999/insurance-valid?date=2025-06-01"

## Dată invalidă (format) → 400 Bad Request
curl -i "http://localhost:8080/api/cars/1/insurance-valid?date=2025-99-99"

## Dată în afara intervalului suportat → 400 Bad Request
curl -i "http://localhost:8080/api/cars/1/insurance-valid?date=1799-01-01"

### D) Scheduler: log „o singură dată” după expirarea poliței

## 1. Deschide H2 Console: http://localhost:8080/h2-console
##    JDBC URL: jdbc:h2:mem:carins
##    User: sa  (fără parolă)

## 2. Rulează în H2 (marchează polița 1 ca „expirată ieri” și nelogată)
UPDATE insurancepolicy SET end_date = CURRENT_DATE - 1, expiry_logged = FALSE WHERE id = 1;
SELECT id, end_date, expiry_logged FROM insurancepolicy WHERE id = 1;

## 3. Observă log-ul în terminalul unde rulează serverul — apare o singură dată
##    Policy 1 for car 1 expired on YYYY-MM-DD

## 4. Confirmă marcarea
SELECT id, end_date, expiry_logged FROM insurancepolicy WHERE id = 1;
-- expiry_logged = TRUE

## Notă: pentru demo rapid, aplicația are un mod de testare (policy.expiry.testMode=true)
## care permite observarea logului fără a aștepta fereastra 00:00–01:00.


**Good luck — and have fun!**
