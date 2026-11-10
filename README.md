# Puppy AI

A desktop AI companion: a pixel-art puppy that lives on your desktop, chats with
you, researches the web and runs study sessions. JavaFX frontend with pure Java
services in a single desktop process — no Python, no separate server.

**Status: milestone 2 frontend complete.** The app launches as a frameless,
transparent, always-on-top puppy. Hover gives a brief reaction, double-click
opens Study / Chat / More, and the shell expands around the mascot while keeping
its screen anchor. Chat uses mock replies and session-only history; Study uses
the Pomodoro timer. AI providers and persistent chat history are future work.

## Requirements

| Tool | Version |
| --- | --- |
| JDK | 24+ (JavaFX 25 requires JDK 23+) |
| Maven | 3.9+ |

The plan targets Java 25. This machine has JDK 24, so the project compiles with
`--release 24` and runs JavaFX 25.0.5, which works on JDK 23+. To move to the
plan's target, install JDK 25 and change `java.version` in `pom.xml` to `25` —
nothing else needs to change.

Because Maven may pick a different JDK than your `PATH`, point `JAVA_HOME` at
your JDK 24 installation before building:

```bash
export JAVA_HOME="C:/path/to/jdk-24"   # Git Bash
# or: set JAVA_HOME=C:\path\to\jdk-24  # cmd.exe
```

## Build and run

```bash
mvn clean compile     # compile
mvn test              # run unit tests
mvn javafx:run        # launch Puppy
```

## Controls

| Action | Result |
| --- | --- |
| Drag with left mouse button | Move the puppy around the desktop |
| Hover | Brief greeting and happy animation |
| Double-click the puppy | Open or close the Study / Chat / More menu |
| Study / Chat | Open the matching panel above the puppy; close or minimize returns to the puppy |
| More | Open Settings, session chat history, Help / About, collapse, or quit |
| Escape | Close the current panel/menu and return to the puppy |
| Right-click | Development menu: switch mascot states or quit |
| Quit Puppy | Closes the window and shuts services down cleanly |

There is deliberately no title bar, so use **More → Quit Puppy** (or the
right-click development menu) to exit.

## Mascot states

| State | Sheet row | Purpose |
| --- | --- | --- |
| `IDLE` | 1 | Default desktop state |
| `HAPPY` | 2 | Positive reactions |
| `THINKING` | 3 | Processing requests |
| `TALKING` | 4 | Delivering responses |
| `STUDYING` | 5 | Study mode |
| `SLEEPING` | 6 | Idle for an extended period |

`MascotSprites` owns the animation timing, `MascotAnimator` drives the frames,
and `MascotStateService` is the single place the rest of the application asks
for a state change.

## Sprite sheet

`src/main/resources/sprites/new_spritesheet.png` is a **6 × 6 grid**: six
animation frames per row and one row per state, in the order shown above. The
current sheet uses 128 × 128 px cells, but the animator derives the frame size
from the image, so any sheet that stays on the 6 × 6 grid works without code
changes. Frames are played by moving the image view's viewport, which keeps
state switches cheap.

The animator cuts exact cells, so each frame must be drawn on the same anchor:
centred horizontally and with the paws on a common ground line, with effects
kept inside the cell. Breaking that rule makes animations shake and lets parts
of one row appear in the next.

Use `docs/spritesheet-guide.png` (768 × 768, 128 × 128 px cells) as a trace layer
and follow [docs/SPRITESHEET_GUIDE.md](docs/SPRITESHEET_GUIDE.md) for the anchor
rules, row order, an AI prompt and a troubleshooting table.

`src/main/resources/icons/puppy.png` is the window/taskbar icon, cropped from
the idle frame of the sheet.

## Architecture

```
JavaFX presentation   controllers, FXML, CSS, sprite rendering
        │  Java interfaces / method calls
Application services  MascotStateService (Chat, Study, AI, research later)
        │
Infrastructure        configuration, thread pool, repositories, HTTP clients
```

| Component | Current implementation |
| --- | --- |
| `PuppyApplication` | JavaFX entry point, builds the transparent always-on-top stage |
| `ServiceRegistry` | Creates and shuts down application services |
| `AppConfig` | App name, version, always-on-top flag, data directory |
| `MascotController` | Expandable shell, mascot interactions, panel navigation and drag-safe movement |
| `MascotAnimator` | Frame timing and viewport switching |
| `MascotSprites` | Sheet layout and per-state animation metadata |
| `MascotStateService` | Thread-safe current state with listener notifications |
| `StudyTimer` | Timestamp-based Pomodoro countdown (25:00 focus / 5:00 break, test mode) |
| `MockChatResponder` | Canned chat replies until the AI providers arrive |
| `ChatController` / `StudyController` | Mock conversation UI and Pomodoro panel |
| `SettingsController` | In-session desktop settings and future-feature notices |
| `ThreadManager` | Daemon worker pool reserved for background work |

Rules that keep the project clean:

- Controllers never touch databases, HTTP or AI providers; they call services.
- `ServiceRegistry` is the only place services are constructed.
- Background work will never run on the JavaFX Application Thread.
- Runtime data will live in `%LOCALAPPDATA%\PuppyAI`, not inside the project.

## Project layout

```
pom.xml
src/main/java/com/puppy/
├── PuppyApplication.java
├── config/       AppConfig, ServiceRegistry
├── controller/   MascotController, ChatController, StudyController,
│                 SettingsController
├── enums/        MascotState, AIMode
├── service/      MascotStateService, DefaultMascotStateService,
│                 StudyTimer, MockChatResponder
├── ui/           MascotAnimator, MascotSprites
└── util/         ThreadManager
src/main/resources/
├── css/          puppy.css
├── fxml/         shell.fxml, chat.fxml, study.fxml, settings.fxml
├── icons/        puppy.png
└── sprites/      new_spritesheet.png
src/test/java/com/puppy/
├── controller/   FxmlLayoutTest
├── service/      DefaultMascotStateServiceTest, StudyTimerTest,
│                 MockChatResponderTest
└── ui/           MascotSpritesTest
```

Planned packages from the implementation plan (`ai/`, `repository/`, `database/`,
`web/`, and the remaining models/services) are added in their milestones.

## Milestone 2 frontend and backend

The app now follows the mascot → menu → Chat / Study / More flow. The expandable
shell caches the Chat, Study and Settings panels; closing or minimizing a panel
returns to the mascot. The current UI includes:

- Chat with blank-input rejection, delayed mock replies, a new-conversation
  action, and session-only conversation history.
- Study with 25-minute focus / 5-minute break defaults, start/pause/reset and
  focus/break completion feedback.
- Settings for the in-session always-on-top option. AI mode/provider controls
  are visibly marked as unavailable until AI integration arrives.
- More with Settings, session chat history, Help / About, collapse-to-mascot
  and Quit.

Persistent history and provider credentials are not implemented or requested by
this UI. A true hide-to-tray option is deferred; “Hide/show” safely collapses
the expanded shell to the mascot.

`StudyTimer` remains pure Java; `StudyController` owns the JavaFX ticker and
calls `completePhase()` when a phase finishes. The chat panel uses the
`MockChatResponder` from `ServiceRegistry` and runs its demo delay on the shared
worker pool.

## Milestone 1 acceptance

| Check | Covered by |
| --- | --- |
| Application launches | `mvn javafx:run` |
| Transparent window (desktop visible behind the puppy) | `StageStyle.TRANSPARENT` + transparent scene fill |
| Always on top | `stage.setAlwaysOnTop` (configurable) |
| Drag the puppy | `MascotController` mouse handlers |
| All six animations play | Right-click menu, `MascotAnimator` |
| Clean exit | `Application.stop()` → `ServiceRegistry.close()` |

Known limitations: JavaFX receives mouse input across the full window rectangle,
including transparent pixels. Chat history is session-only, and provider
configuration, persistent settings, a system tray and true hide/show behavior
are future work.
