# Puppy AI — Implementation Status

Last updated: 2026-11-09

## Milestones

| Milestone | Scope | Status |
| --- | --- | --- |
| 1 | Project foundation and JavaFX desktop shell | ✅ Complete |
| 2 | Frontend interactions and UI | ⏳ Next |
| 3 | Pure Java services and SQLite persistence | Pending |
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

## Current implementation notes

- **JDK:** the plan targets Java 25; this machine has JDK 24, and JavaFX 25 needs
  JDK 23+, so `pom.xml` sets `java.version` to 24. Bump the property after
  installing JDK 25.
- **Sprites:** the sheet is a 6 × 6 grid (row per state, 6 frames per row,
  102 × 68 px cells today). Frame size is derived at runtime, so new art with the
  same grid shape requires no code change. `docs/SPRITESHEET_GUIDE.md` specifies
  the replacement grid (768 × 768, 128 × 128 px cells) and ships a trace template
  at `docs/spritesheet-guide.png`.
- **State priority:** `DefaultMascotStateService` currently lets the latest
  request win; the priority rules (study must not be interrupted by an idle
  refresh) belong to milestone 6.
- **Data directory:** `AppConfig` resolves `%LOCALAPPDATA%\PuppyAI` (or
  `~/.local/share/PuppyAI`) but nothing writes to it yet.

## Known bugs / limitations

- Mascot frames are not anchored to a common centre and ground line, and effects
  such as bubbles reach across cell borders. Animations therefore shake, and the
  TALKING row shows paws from the row above. This is artwork, not slicing: the
  animator cuts exact cells. Fix the sheet with `docs/SPRITESHEET_GUIDE.md`.
- The entire 240 × 180 window captures mouse input, including transparent
  pixels; per-pixel hit testing is not part of the MVP.
- No hover or double-click reactions yet (milestone 2).
- No database, AI provider or Tavily integration yet, so no network access is
  required to run the application.

## Pending next (milestone 2)

- Hover detection and hover animation; double-click detection that does not fire
  after a drag.
- Main menu visibility and closing behaviour.
- `chat.fxml` / `ChatController` with mock messages.
- `study.fxml` with a testable timer.
- `settings.fxml` with AI mode, provider configuration and window options.

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
