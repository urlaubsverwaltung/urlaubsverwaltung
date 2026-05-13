# Application for Leave — Workflow

## 1. Without Departments/Department Heads

```mermaid
stateDiagram-v2
    [*] --> WAITING : apply() — Applicant
    [*] --> ALLOWED : directAllow() — Office
    [*] --> ALLOWED : createFromConvertedSickNote() — Office

    WAITING --> ALLOWED : allow() — Boss
    WAITING --> REJECTED : reject() — Boss
    WAITING --> REVOKED : cancel() — Applicant

    ALLOWED --> ALLOWED_CANCELLATION_REQUESTED : cancel() — Applicant
    ALLOWED --> CANCELLED : cancel() — Office / Boss (APPLICATION_CANCEL)
    ALLOWED --> CANCELLED : directCancel() — Office / Boss (APPLICATION_CANCEL)

    ALLOWED_CANCELLATION_REQUESTED --> CANCELLED : cancel() — Office / Boss
    ALLOWED_CANCELLATION_REQUESTED --> ALLOWED : declineCancellationRequest() — Office / Boss

    REVOKED --> [*]
    REJECTED --> [*]
    CANCELLED --> [*]
```

## 2. Departments with Department Heads (single-stage authorization)

```mermaid
stateDiagram-v2
    [*] --> WAITING : apply() — Applicant
    [*] --> ALLOWED : directAllow() — Office
    [*] --> ALLOWED : createFromConvertedSickNote() — Office

    WAITING --> ALLOWED : allow() — Department Head
    WAITING --> REJECTED : reject() — Boss / Department Head
    WAITING --> REVOKED : cancel() — Applicant

    ALLOWED --> ALLOWED_CANCELLATION_REQUESTED : cancel() — Applicant
    ALLOWED --> CANCELLED : cancel() — Office / Boss / Department Head (APPLICATION_CANCEL)
    ALLOWED --> CANCELLED : directCancel() — Office / Boss / Department Head (APPLICATION_CANCEL)

    ALLOWED_CANCELLATION_REQUESTED --> CANCELLED : cancel() — Office / Boss / Department Head
    ALLOWED_CANCELLATION_REQUESTED --> ALLOWED : declineCancellationRequest() — Office / Boss / Department Head

    REVOKED --> [*]
    REJECTED --> [*]
    CANCELLED --> [*]
```

## 3. Departments with Department Head and Second Stage Authority (two-stage authorization)

```mermaid
stateDiagram-v2
    [*] --> WAITING : apply() — Applicant
    [*] --> ALLOWED : directAllow() — Office
    [*] --> ALLOWED : createFromConvertedSickNote() — Office

    WAITING --> TEMPORARY_ALLOWED : allow() — Department Head
    WAITING --> REJECTED : reject() — Boss / Department Head
    WAITING --> REVOKED : cancel() — Applicant

    TEMPORARY_ALLOWED --> ALLOWED : allow() — Boss / Second Stage Authority
    TEMPORARY_ALLOWED --> REJECTED : reject() — Boss / Second Stage Authority
    TEMPORARY_ALLOWED --> ALLOWED_CANCELLATION_REQUESTED : cancel() — Applicant
    TEMPORARY_ALLOWED --> CANCELLED : cancel() — Office / Boss / Department Head (APPLICATION_CANCEL)

    ALLOWED --> ALLOWED_CANCELLATION_REQUESTED : cancel() — Applicant
    ALLOWED --> CANCELLED : cancel() — Office / Boss / Department Head (APPLICATION_CANCEL)
    ALLOWED --> CANCELLED : directCancel() — Office / Boss / Department Head (APPLICATION_CANCEL)

    ALLOWED_CANCELLATION_REQUESTED --> CANCELLED : cancel() — Office / Boss / Department Head
    ALLOWED_CANCELLATION_REQUESTED --> ALLOWED : declineCancellationRequest() — Office / Boss / Department Head

    REVOKED --> [*]
    REJECTED --> [*]
    CANCELLED --> [*]
```

## States

| Status                           | Description                                                               |
|----------------------------------|---------------------------------------------------------------------------|
| `WAITING`                        | Application submitted, waiting for approval                               |
| `TEMPORARY_ALLOWED`              | Provisionally approved by department head in a two-stage approval process |
| `ALLOWED`                        | Fully approved                                                            |
| `ALLOWED_CANCELLATION_REQUESTED` | Approved, but applicant has requested cancellation                        |
| `REVOKED`                        | Withdrawn by applicant before approval                                    |
| `REJECTED`                       | Rejected by management                                                    |
| `CANCELLED`                      | Cancelled after approval                                                  |

## Notes

- **Boss** and **Office** are distinct roles: Boss approves and rejects; Office handles administrative actions (direct cancellation, declining cancellation requests) and can always do so without additional permissions.
- **APPLICATION_CANCEL** is an additional permission required for Boss or Department Head to cancel directly. Office can always cancel directly.
- **Side operations without status change**: `edit()` (Applicant / Office), `refer()` (Boss / Office), `remind()` (Applicant).
