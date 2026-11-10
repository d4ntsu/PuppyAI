# Milestone 3 Plan — Real services and SQLite persistence

Source of truth: `PuppyAI_ImplementationPlan.md`, *Day 3 — Pure Java backend services and
persistence* (milestone 3). This document turns that milestone into an ordered,
verifiable work list for the code that exists today.

Status: **planned, not started.** Milestones 1 and 2 are complete; the UI runs on
in-memory state.

## Goal

Replace the session-only state in the UI with a real Java service layer backed by
SQLite, without changing the mascot behaviour or the look of any panel.

Definition of done:

- Chat conversations and messages survive an application restart.
- Settings (starting with always-on-top) persist and are applied at startup.
- Pomodoro sessions are written to the database with a final status and duration.
- Controllers talk only to services (`ChatService`, `SettingsService`, `StudyService`);
  no controller contains SQL or JDBC code.
- Repository and service tests run against temporary databases and never touch the
  user's real data directory.
- `mvn test` is green; manual restart check passes.

## Current baseline (what the plan builds on)

| Area | Today | Gap |
| --- | --- | --- |
| `AppConfig` | Resolves `%LOCALAPPDATA%\PuppyAI` (or `~/.local/share/PuppyAI`) in `dataDirectory()`, but nothing writes there | No override for tests; no database file |
| `ServiceRegistry` | Owns `ThreadManager`, `MascotStateService`, `MockChatResponder`; `start()` is an empty hook, `close()` shuts the pool | No database, repositories or real services |
| Chat | `ChatController` keeps `Message`/`Conversation` as private records plus a `List<>` history; greeting is UI-only; mock replies via `MockChatResponder` on the `ThreadManager` pool | Nothing is persisted; history dies with the panel |
| Settings | `SettingsController` toggles `stage.setAlwaysOnTop` for the session; `AppConfig.alwaysOnTop` comes from system property/env only | No storage, no startup application of a saved value |
| Study | `StudyController` owns `StudyTimer`, drives the JavaFX ticker and calls `start/pause/reset/completePhase` | No `StudySession` record anywhere |
| Tests | 5 test classes, 28 tests, all pure/every-JavaFX | No persistence tests, no test data directory |

Dependencies: no JDBC driver in `pom.xml` yet.

## Architecture decisions

1. **Driver:** `org.xerial:sqlite-jdbc:3.54.1.0` (latest stable, October 2026). One
   embedded dependency, native libraries bundled. No server, no ORM, no migration
   framework — the schema is small and versioned with `PRAGMA user_version`.
2. **One connection, serialized access.** `Database` owns a single JDBC connection and
   exposes `transaction(...)`. The app is a single desktop process with low write
   volume, so a locked single connection is simpler and safer than a pool. Enable
   `journal_mode=WAL`, `foreign_keys=ON`, `busy_timeout=5000` at open.
3. **Timestamps as ISO-8601 UTC text** (`Instant.toString()`), so the database is
   readable and comparable as strings. UTC avoids DST ambiguity; the UI formats local
   time.
4. **IDs are random UUID strings** generated in the service layer, not by SQLite.
5. **Models are Java records** in `com.puppy.model`; enums for role and status. The
   richer AI models (`AIRequest`, `AIResponse`, `WebSource`) are **deferred to
   milestone 4** — nothing in milestone 3 uses them, and guessing their shape now
   would be churn.
6. **Repository interfaces + SQLite implementations**, so services can be tested and
   milestone 4 can swap persistence without touching controllers.
7. **Synchronous database calls on the JavaFX thread** for now. SQLite + WAL answers a
   local write in well under a millisecond; the existing mock-reply delay stays on the
   `ThreadManager` pool. When milestone 4 streams tokens, the service calls move to the
   worker pool and results are marshalled with `Platform.runLater`.
8. **Data directory override** (`-Dpuppy.dataDirectory=...` / `PUPPY_DATA_DIRECTORY`)
   added to `AppConfig`, so tests and a portable run never write to the real profile.
9. **Fail loud at startup, fail soft at runtime.** If the database cannot be opened or
   migrated, `ServiceRegistry.start()` throws and `PuppyApplication` shows an error
   dialog naming the file before exiting. A failed *message* write must not kill the
   app: the chat panel shows an inline error bubble and keeps working.

## Work breakdown

Each step ends with a compiling tree and passing tests, so the milestone can be
stopped between steps.

### Step 1 — Models (≈1 h)

New files in `src/main/java/com/puppy/model/`:

- `MessageRole` — `USER`, `ASSISTANT` (string form stored in `role`).
- `ChatMessage` — `id`, `conversationId`, `role`, `content`, `createdAt`.
- `Conversation` — `id`, `title`, `createdAt`, `updatedAt`, `messageCount`.
- `AppSettings` — `alwaysOnTop`, `AIMode aiMode`.
- `SessionStatus` — `RUNNING`, `PAUSED`, `COMPLETED`, `ABANDONED`.
- `StudySession` — `id`, `startedAt`, `completedAt` (nullable), `durationSeconds`,
  `status`.

Rules: compact constructors validate non-blank IDs/content and non-negative durations;
records are immutable, so updates replace the instance.

### Step 2 — Database and schema (≈1.5 h)

`com.puppy.persistence.Database`:

```java
public final class Database implements AutoCloseable {
    Database(Path file);            // creates parent dirs, opens jdbc:sqlite:<file>
    void initialize();              // pragmas + migrate()
    <T> T transaction(SqlWork<T> work);   // BEGIN / COMMIT / ROLLBACK
    void close();
}
```

`com.puppy.persistence.Schema` (v1, `PRAGMA user_version = 1`), matching the plan and
adding two indexes:

```sql
CREATE TABLE conversations (
    id TEXT PRIMARY KEY, title TEXT NOT NULL,
    created_at TEXT NOT NULL, updated_at TEXT NOT NULL);

CREATE TABLE messages (
    id TEXT PRIMARY KEY, conversation_id TEXT NOT NULL,
    role TEXT NOT NULL, content TEXT NOT NULL, created_at TEXT NOT NULL,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE);

CREATE TABLE settings (key TEXT PRIMARY KEY, value TEXT NOT NULL);

CREATE TABLE study_sessions (
    id TEXT PRIMARY KEY, started_at TEXT NOT NULL, completed_at TEXT,
    duration_seconds INTEGER NOT NULL, status TEXT NOT NULL);

CREATE INDEX idx_messages_conversation ON messages(conversation_id, created_at);
CREATE INDEX idx_conversations_updated ON conversations(updated_at DESC);
```

`migrate()` reads `PRAGMA user_version`, applies pending versions in a transaction,
and is safe to run on every startup. `RepositoryException extends RuntimeException`
wraps `SQLException` with the failing operation, never the connection string.

`Database` must be constructible against `@TempDir` paths for tests.

### Step 3 — Repositories (≈2 h)

Interfaces in `com.puppy.repository`, implementations in
`com.puppy.repository.sqlite`:

- `ConversationRepository` — `save(Conversation)`, `findById(String)`,
  `findRecent(int limit)`, `touch(String id, Instant updatedAt)`,
  `delete(String id)` (messages cascade).
- `MessageRepository` — `save(ChatMessage)`, `findByConversation(String id)`
  (ordered by `created_at`), `deleteByConversation(String id)`, `count(String id)`.
- `SettingsRepository` — `get(String key)`, `put(String key, String value)`,
  `getAll()`.
- `StudySessionRepository` — `save(StudySession)`, `update(StudySession)`,
  `findRecent(int limit)`, `findById(String)`.

All statements use `PreparedStatement` with bound parameters; no string-concatenated
SQL. Multi-row writes (message + conversation `updated_at`) go through one
`Database.transaction`.

### Step 4 — Services (≈2 h)

`ChatService` (com.puppy.service):

```java
Conversation createConversation();
List<Conversation> recentConversations(int limit);
List<ChatMessage> messages(String conversationId);
Conversation appendUserMessage(String conversationId, String text);
Conversation appendAssistantMessage(String conversationId, String text);
void deleteConversation(String conversationId);
```

- A conversation is created on the **first user message**, not when the panel opens, so
  empty chats never accumulate.
- Title = first user message trimmed to 32 characters with an ellipsis.
- Appending a message also updates `conversations.updated_at` in the same transaction.
- `ChatController` keeps calling `MockChatResponder` for the reply text; only storage
  moves into `ChatService`. Milestone 4 swaps the responder for the AI provider.

`SettingsService`:

```java
AppSettings load();                 // DB value, else defaults
void setAlwaysOnTop(boolean value);
void setAiMode(AIMode mode);        // stored now, used in milestone 4
```

Precedence: explicit `puppy.alwaysOnTop` / `PUPPY_ALWAYS_ON_TOP` override (developer
escape hatch) → stored value → `true`. Document this in `AppConfig`.

`StudyService`:

```java
StudyService(StudySessionRepository repository, Clock clock);
void start(); void pause(); void resume(); void reset();
void completePhase();               // called when the countdown reaches zero
Duration remaining(); StudyTimer.Phase phase(); boolean running();
void close();                       // marks a live session ABANDONED
```

- Wraps the existing `StudyTimer`; the controller stops owning timer state.
- Insert a `RUNNING` row on `start()`, update to `PAUSED`, then `COMPLETED` (with
  `completed_at` and `duration_seconds`) on `completePhase()`; `reset()` marks the row
  `ABANDONED`.
- `close()` marks any live row `ABANDONED` so a crashed/quit session is not left
  `RUNNING` forever.
- Tests inject a fake `Clock` and the existing package-private `StudyTimer` clock.

### Step 5 — Wiring (≈1.5 h)

- `ServiceRegistry`: add `database`, repositories, `chatService`, `settingsService`,
  `studyService`; `start()` opens the DB and migrates; `close()` closes the database
  **after** the controllers have shut down. Expose accessors; remove
  `mockChatResponder()` from the registry once `ChatController` takes `ChatService`
  (keep the responder inside `ChatService` until milestone 4).
- `PuppyApplication`: after `stage` exists, apply `settingsService.load().alwaysOnTop()`
  before `show()`. Pass the effective value through the existing
  `SettingsController.attach(...)` path.
- `MascotController`: pass `ChatService`, `SettingsService` and `StudyService` into the
  panels; `SettingsController` saves the toggle through `SettingsService`.
- `ChatController` refactor:
  - delete the private `Message` / `Conversation` records and the in-memory history;
  - load recent conversations lazily when the history pane opens;
  - greeting stays a UI-only assistant bubble for an empty conversation;
  - on send: persist the user message, then (after the existing delay) persist and show
    the assistant reply;
  - keep blank-input rejection, the thinking label, the `THINKING → TALKING → HAPPY`
    mascot sequence and `FxmlLayoutTest` behaviour;
  - a persistence failure shows an assistant bubble such as
    "I couldn't save that message." and re-enables the input.
- `StudyController`: keep the JavaFX `Timeline` ticker and all labels, but call
  `StudyService` for start/pause/reset/completion and read remaining time from it.
- `README.md` / `IMPLEMENTATION_STATUS.md`: update the limitation "Chat history is
  session-only" once this lands.

### Step 6 — Tests (≈2 h)

New test classes (all persistence tests use `@TempDir`, never `AppConfig.load()`'s real
directory):

- `DatabaseSchemaTest` — tables exist, `user_version = 1`, foreign keys are enforced,
  re-running `initialize()` is idempotent.
- `ConversationRepositoryTest` — save/find/recent ordering, cascade delete removes
  messages, `touch()` changes ordering.
- `MessageRepositoryTest` — ordered retrieval, count, delete by conversation.
- `SettingsRepositoryTest` — get missing key returns empty, put overwrites.
- `StudySessionRepositoryTest` — insert running → update completed; recent ordering.
- `ChatServiceTest` — first message creates the conversation and title; appending
  updates `updated_at`; messages round-trip; delete removes everything.
- `StudyServiceTest` — start/pause/resume/complete/reset produce the expected rows and
  statuses; `close()` abandons a live session.
- `PersistenceRestartTest` — write a conversation, close the database, reopen it and
  read the same messages back (the plan's "survives restart" requirement).
- `PersistenceBoundaryTest` — scans `src/main/java/com/puppy/controller` for `java.sql`,
  `javax.sql`, `jdbc:` or `SELECT `/`INSERT ` and fails if found, enforcing the
  "controllers do not execute SQL" rule.
- Update `FxmlLayoutTest` to build `ServiceRegistry` with a temporary data directory
  (via the new override) so the UI test writes nothing to the profile; keep the
  existing assertions.

### Step 7 — Manual verification (≈0.5 h)

1. Stop the running app, back up/delete any old `puppy.db` (none expected yet).
2. `mvn -o test`.
3. `run.bat`; send three chat messages, start a short study session, toggle
   always-on-top, quit via **More → Quit Puppy**.
4. Confirm `%LOCALAPPDATA%\PuppyAI\puppy.db` exists and has the expected rows
   (`sqlite3` or a scratch query).
5. Relaunch: history shows the previous conversation, the always-on-top state is
   restored, and a `study_sessions` row exists with `COMPLETED` or `ABANDONED`.
6. Relaunch a second instance while the first runs to confirm WAL/busy timeout does not
   throw (both may write; no corruption).

## Acceptance criteria (from the plan's Day 3 tests)

| Requirement | Covered by |
| --- | --- |
| Conversations can be created | `ChatServiceTest`, `ConversationRepositoryTest` |
| Messages save and load | `MessageRepositoryTest`, `ChatServiceTest` |
| History survives restart | `PersistenceRestartTest`, manual step 5 |
| Settings persist | `SettingsRepositoryTest`, manual step 5 |
| Study sessions recorded | `StudySessionRepositoryTest`, `StudyServiceTest` |
| Repository tests independent | all `*RepositoryTest` use `@TempDir` only |
| Controllers do not execute SQL | `PersistenceBoundaryTest` |

## Risks and mitigations

| Risk | Mitigation |
| --- | --- |
| `sqlite-jdbc` loads native code; Java 24 prints a restricted-method warning | Add `--enable-native-access=ALL-UNNAMED` to the `javafx-maven-plugin` options (alongside `javafx.graphics`) and confirm the driver opens a DB on this machine |
| Tests write into the real user profile | `puppy.dataDirectory` override, used by every test and by `FxmlLayoutTest` |
| Two app instances / the currently running app holding the file | WAL + `busy_timeout=5000`; stop the running instance before the first manual migration |
| Chat refactor regresses the existing UI flow | Keep `FxmlLayoutTest` assertions (blank input, history open, panel collapse) and add persistence assertions rather than replacing them |
| Timezone/DST bugs in session durations | Store UTC instants, compute duration from the service clock, display local time only in the UI |
| Database growth (no pruning) | Out of scope for milestone 3; note it as future work alongside searchable history |
| Startup failure would otherwise crash silently | Fail with a named-file error dialog, per decision 9 |

## Explicitly out of scope

- AI providers, streaming, Ollama, Tavily/web research (milestones 4–5).
- Encryption, multiple profiles, cloud sync, data export/import.
- Chat search, rename/pin, message editing, pagination beyond a recent-N list.
- Any redesign of the panels; this milestone must be invisible except for surviving
  restarts.
- Packaging/installer work (milestone 7).

## Effort

≈10 h total: models 1 h, database/schema 1.5 h, repositories 2 h, services 2 h,
wiring 1.5 h, tests 2 h. The plan's 7–9 h estimate assumes less UI rewiring; the extra
hour is the `ChatController`/`StudyController` refactor and the boundary test.

## Suggested commit sequence

1. `Add domain models for chat, settings and study sessions`
2. `Add SQLite database, schema migration and repositories with tests`
3. `Add chat, settings and study services backed by SQLite`
4. `Wire services into the UI and persist chat history, settings and study sessions`
5. `Update docs and status for milestone 3`
