# Puppy AI

A desktop AI companion: a pixel-art puppy that lives on your desktop, chats with
you, researches the web and runs study sessions. JavaFX frontend with pure Java
services in a single desktop process — no Python, no separate server.

**Status: milestone 1 (Project foundation and JavaFX desktop shell).** The app
launches as a frameless, transparent, always-on-top puppy that can be dragged
around the desktop, and all six mascot animations play. Chat, AI, research and
study mode arrive in later milestones.

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
| Right-click | Menu: switch between the six animations, or quit |
| Quit Puppy | Closes the window and shuts services down cleanly |

There is deliberately no title bar, so right-click → **Quit Puppy** is the
supported way to exit.

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

`src/main/resources/sprites/mascot_spritesheet.png` is a **6 × 6 grid**: six
animation frames per row and one row per state, in the order shown above. The
current sheet uses 102 × 68 px cells, but the animator derives the frame size
from the image, so any sheet that stays on the 6 × 6 grid works without code
changes. Frames are played by moving the image view's viewport, which keeps
state switches cheap.

The animator cuts exact cells, so each frame must be drawn on the same anchor:
centred horizontally and with the paws on a common ground line, with effects
kept inside the cell. The current sheet does not follow that rule, which makes
animations shake and lets parts of one row appear in the next.

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
| `MascotController` | FXML controller: window dragging, right-click menu, state → animation |
| `MascotAnimator` | Frame timing and viewport switching |
| `MascotSprites` | Sheet layout and per-state animation metadata |
| `MascotStateService` | Thread-safe current state with listener notifications |
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
├── controller/   MascotController
├── enums/        MascotState
├── service/      MascotStateService, DefaultMascotStateService
├── ui/           MascotAnimator, MascotSprites
└── util/         ThreadManager
src/main/resources/
├── css/          puppy.css
├── fxml/         mascot.fxml
├── icons/        puppy.png
└── sprites/      mascot_spritesheet.png
src/test/java/com/puppy/
├── service/      DefaultMascotStateServiceTest
└── ui/           MascotSpritesTest
```

Planned packages from the implementation plan (`ai/`, `repository/`, `database/`,
`web/`, and the remaining models/services) are added in their milestones.

## Milestone 1 acceptance

| Check | Covered by |
| --- | --- |
| Application launches | `mvn javafx:run` |
| Transparent window (desktop visible behind the puppy) | `StageStyle.TRANSPARENT` + transparent scene fill |
| Always on top | `stage.setAlwaysOnTop` (configurable) |
| Drag the puppy | `MascotController` mouse handlers |
| All six animations play | Right-click menu, `MascotAnimator` |
| Clean exit | `Application.stop()` → `ServiceRegistry.close()` |

Known limitations at this milestone: the whole 240 × 180 window rectangle
receives mouse input (JavaFX does not hit-test transparent pixels), and the
mascot does not yet react to hover or double-click — that is milestone 2.
