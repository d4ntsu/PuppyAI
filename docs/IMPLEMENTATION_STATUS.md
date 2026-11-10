# Puppy AI — Implementation Status

Last updated: 2026-11-09

## Milestones

| Milestone | Scope | Status |
| --- | --- | --- |
| 1 | Project foundation and JavaFX desktop shell | ✅ Complete |
| 2 | Frontend interactions and UI | ✅ Complete |
| 3 | Pure Java services and SQLite persistence | Planned — see `docs/MILESTONE_3_PLAN.md` |
| 4 | AI integration (cloud + Ollama) | Pending |
| 5 | Internet research and extraction | Pending |
| 6 | Study mode, settings, integration | Pending |
| 7 | Testing, optimization, packaging | Pending |

## Completed (milestone 1)

- Maven project: JDK 24 target, JavaFX 25.0.5, JavaFX Maven Plugin, JUnit 5.14.4.
- `PuppyApplication` entry point with frameless (`StageStyle.TRANSPARENT`),
  transparent, always-on-top window positioned in the bottom-right screen corner.
- Draggable mascot with clean shutdown through `Application.stop()`.
- Six mascot states (`MascotState`) driven by `DefaultMascotStateService`.
- `MascotAnimator` plays the supplied 6 × 6 spritesheet through an ImageView
  viewport, with per-state frame timing and a global speed multiplier.
- `ServiceRegistry` builds and owns services; `ThreadManager` provides a daemon
  worker pool for later milestones and is closed on exit.
- Right-click menu to switch animations and quit (the window has no title bar).
- Placeholder-friendly assets: spritesheet at `resources/sprites/`, icon at
  `resources/icons/`.
- Tests: spritesheet grid/artwork contract and mascot state service behaviour.

## Completed (milestone 2)

- `StudyTimer` — pure-Java Pomodoro countdown: 25:00 focus / 5:00 break, plus
  one-minute test durations. Remaining time is calculated from a target
  timestamp (not tick counts), supports start/pause/reset and phase completion,
  and takes an injectable nano clock so it is tested without waiting.
- `MockChatResponder` — deterministic canned replies (greeting, study and help
  cues, rotating fallbacks) for the chat UI until the real AI lands. Exposed as
  `ServiceRegistry.mockChatResponder()`.
- `AIMode` enum (`ONLINE`, `OFFLINE`, `AUTO`) backing the future settings panel.
- Tests: `StudyTimerTest` (11 cases, fake clock) and `MockChatResponderTest`.

- Expandable JavaFX shell with hover greeting, happy hover reaction,
  drag-safe double-click menu and anchored resize behavior.
- Cached Chat, Study and Settings panels plus More menu, Escape dismissal,
  collapse/minimize behavior and a right-click development fallback.
- Chat uses mock replies on the shared worker pool, rejects blank input and
  offers new conversations with session-only history.
- Study UI drives the existing 25/5 timer with start/pause/reset, phase labels
  and completion feedback.
- Settings exposes the functional in-session always-on-top switch and clearly
  marks AI provider controls as future work; no credentials are collected.
- `FxmlLayoutTest` loads all layouts and verifies the drag-safe menu, Study /
  Chat / Settings navigation, panel collapse, and blank-chat rejection.

## Services used by the UI

| Class | How the UI should use it |
| --- | --- |
| `StudyTimer` | `StudyController` owns the JavaFX ticker and calls `start()` / `pause()` / `reset()` and `completePhase()` when a phase finishes. |
| `MockChatResponder` | `ChatController` gets the responder from `ServiceRegistry`, rejects blank input, and runs its demo delay on the shared worker pool. |
| `AIMode` | Reserved for AI integration; its settings control is visibly unavailable until provider routing exists. |

## Current implementation notes

- **JDK:** the plan targets Java 25; this machine has JDK 24, and JavaFX 25 needs
  JDK 23+, so `pom.xml` sets `java.version` to 24. Bump the property after
  installing JDK 25.
- **Sprites:** the sheet is a 6 × 6 grid (row per state, 6 frames per row,
  768 × 768 with 128 × 128 px cells today). Frame size is derived at runtime, so
  new art with the same grid shape requires no code change.
  `docs/SPRITESHEET_GUIDE.md` specifies that grid and ships a trace template at
  `docs/spritesheet-guide.png`.
- **State priority:** `DefaultMascotStateService` currently lets the latest
  request win; the priority rules (study must not be interrupted by an idle
  refresh) belong to milestone 6.
- **Data directory:** `AppConfig` resolves `%LOCALAPPDATA%\PuppyAI` (or
  `~/.local/share/PuppyAI`) but nothing writes to it yet.

## Known bugs / limitations

- The previous mascot sheet was not anchored to a common centre and ground line,
  and its effects crossed cell borders, so animations shook and the TALKING row
  showed paws from the row above. The current sheet follows the anchor rules in
  `docs/SPRITESHEET_GUIDE.md`; the animator cuts exact cells either way.
- The entire 336 × 252 window captures mouse input, including transparent
  pixels; per-pixel hit testing is not part of the MVP.
- The Chat history is session-only; SQLite persistence is a later milestone.
- True hide-to-tray is deferred until a reliable restore path is implemented.
- No database, AI provider or Tavily integration yet, so no network access is
  required to run the application.

## Follow-on UI ideas

- Add study presets (e.g. 25/5 and 50/10), with custom durations later.
- Add persistent/searchable chat history after SQLite persistence is ready.
- Add true hide-to-tray only with a reliable restore path.
- Add keyboard navigation and a first-run interaction walkthrough.

## Architectural decisions

1. Single Java process: JavaFX and services communicate through Java interfaces,
   with no internal REST layer.
2. Controllers receive services from `ServiceRegistry`; they never construct
   infrastructure themselves.
3. The mascot's visual state is application state, owned by
   `MascotStateService`, not by animation callbacks — this prevents flicker when
   token streaming is added in milestone 4.
4. Animations are played by moving an ImageView viewport over one loaded sheet,
   which avoids per-frame image loading and keeps switching cheap.
5. Background work is confined to the `ThreadManager` pool; UI updates will be
   marshalled with `Platform.runLater`.
6. Long-running timers calculate the deadline once (`StudyTimer`) instead of
   decrementing a counter, so UI stalls or a slow tick cannot make them drift.
7. The chat UI gets its stand-in replies from `MockChatResponder` through
   `ServiceRegistry`, keeping controllers free of response logic until the real
   `ChatService` replaces it.
