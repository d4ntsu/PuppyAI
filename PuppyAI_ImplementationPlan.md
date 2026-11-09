# **Puppy AI — Complete Implementation Plan**

7-day MVP  
JavaFX \+ Pure Java  
Windows Desktop

We're going to commit to one architecture:

JavaFX frontend \+ pure Java backend services, running inside a single desktop application.

No Python, no Spring Boot, no separate backend server.

The application will use Java for its UI controllers, business logic, AI orchestration, web research, persistence, and concurrency. External AI providers will be accessed through HTTP APIs.

I would approach this as a small, modular desktop application with clearly separated responsibilities, rather than trying to build a miniature enterprise backend.

## **1\. Final product scope**

By the end of seven days, Puppy AI should have the following capabilities.

Puppy — Desktop Companion

The central character and entry point to the application.

AI Chat

Conversations, streaming responses, history and online/offline AI.

MVP

Internet Research

Search the internet, retrieve webpages and return cited summaries.

MVP

Study Mode

Pomodoro timer, focus sessions and mascot reactions.

MVP

Settings

AI provider, model selection, desktop behavior and preferences.

MVP

### **Features deliberately postponed**

* Voice recognition and text-to-speech.  
* Long-term conversational memory.  
* PDF and DOCX knowledge extraction.  
* Autonomous computer interaction.  
* Multiple AI agents.  
* Custom mascot editor.  
* Cloud synchronization.

These are potential version 2 features, not requirements for the first release.

# **2\. Final technology decisions**

| Layer | Technology | Decision |
| ----- | ----- | ----- |
| Language | Java 25 | Primary language |
| Desktop UI | JavaFX 25 | Frontend |
| UI layout | FXML \+ Java | Presentation |
| Styling | JavaFX CSS | Pixel-art interface |
| Build | Maven | Dependency management |
| Backend | Pure Java services | In-process |
| HTTP | Java HttpClient | External communication |
| JSON | Jackson | Serialization |
| Database | SQLite | Local persistence |
| Database access | JDBC | Persistence layer |
| Web search | Tavily API | Search |
| HTML parsing | Jsoup | Content extraction |
| Local AI | Ollama | Offline inference |
| Online AI | Cloud LLM API | Online inference |
| Concurrency | ExecutorService \+ JavaFX Task | Background operations |
| Testing | JUnit 5 | Unit and integration tests |
| Packaging | jpackage | Windows distribution |

I recommend Java 25 with a matching JavaFX 25 release. If a dependency causes compatibility problems, Java 21 is a reasonable fallback.

Useful documentation:

* [JavaFX](https://openjfx.io/?utm_source=chatgpt.com)  
* [Maven](https://maven.apache.org/?utm_source=chatgpt.com)  
* [Java HttpClient](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpClient.html?utm_source=chatgpt.com)  
* [Jsoup](https://jsoup.org/?utm_source=chatgpt.com)  
* [Ollama](https://ollama.com/?utm_source=chatgpt.com)  
* [Tavily](https://tavily.com/?utm_source=chatgpt.com)

## **3\. Architecture**

The most important architectural decision is that JavaFX and the backend services live in the same process.

You do not need REST endpoints for internal communication.

## **Application architecture**

## **JavaFX Presentation Layer**

FXML · Controllers · CSS · Sprite Renderer

Mascot

Chat

Study

Settings

Menu

History

Java interfaces / method calls

## **Application Services**

ChatService  
StudyService  
AIOrchestrator  
WebResearchService

### **Infrastructure Layer**

AI Providers  
HTTP Clients  
Repositories  
Configuration

Cloud AI

Ollama

Web APIs

One Java process, with external HTTP integrations.

### **Responsibility rules**

This is how I would prevent the project from becoming messy:

| Component | Responsibility |
| ----- | ----- |
| JavaFX Controller | Handles UI interactions |
| Service | Implements application logic |
| AIOrchestrator | Chooses AI provider and tools |
| AIProvider | Defines common AI operations |
| Repository | Reads/writes persistent data |
| HTTP Client | Handles external API communication |
| Model | Represents application data |

For example, `ChatController` should never directly call Ollama.

It should call `ChatService`, which delegates to `AIOrchestrator`.

That separation is worth maintaining even in a one-week project.

# **4\. Project directory structure**

This is the structure I would actually create.

```
puppy-ai/
│
├── pom.xml
├── README.md
├── .gitignore
├── .env.example
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/puppy/
│   │   │
│   │   │       ├── PuppyApplication.java
│   │   │       │
│   │   │       ├── config/
│   │   │       │   ├── AppConfig.java
│   │   │       │   └── ServiceRegistry.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── MascotController.java
│   │   │       │   ├── ChatController.java
│   │   │       │   ├── StudyController.java
│   │   │       │   └── SettingsController.java
│   │   │       │
│   │   │       ├── model/
│   │   │       │   ├── ChatMessage.java
│   │   │       │   ├── Conversation.java
│   │   │       │   ├── AIRequest.java
│   │   │       │   ├── AIResponse.java
│   │   │       │   ├── WebSource.java
│   │   │       │   └── StudySession.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── ChatService.java
│   │   │       │   ├── AIOrchestrator.java
│   │   │       │   ├── StudyService.java
│   │   │       │   ├── WebResearchService.java
│   │   │       │   └── MascotStateService.java
│   │   │       │
│   │   │       ├── ai/
│   │   │       │   ├── AIProvider.java
│   │   │       │   ├── CloudAIProvider.java
│   │   │       │   ├── OllamaProvider.java
│   │   │       │   └── AIProviderFactory.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── ChatRepository.java
│   │   │       │   ├── SettingsRepository.java
│   │   │       │   └── StudyRepository.java
│   │   │       │
│   │   │       ├── database/
│   │   │       │   └── DatabaseManager.java
│   │   │       │
│   │   │       ├── web/
│   │   │       │   ├── TavilyClient.java
│   │   │       │   └── PageExtractor.java
│   │   │       │
│   │   │       ├── util/
│   │   │       │   ├── JsonUtil.java
│   │   │       │   └── ThreadManager.java
│   │   │       │
│   │   │       └── enums/
│   │   │           ├── MascotState.java
│   │   │           ├── AIMode.java
│   │   │           └── MessageRole.java
│   │   │
│   │   └── resources/
│   │       ├── fxml/
│   │       ├── css/
│   │       ├── sprites/
│   │       └── icons/
│   │
│   └── test/
│       └── java/com/puppy/
│           ├── service/
│           ├── ai/
│           ├── repository/
│           └── web/
│
└── data/
```

A small clarification: `data/` should be the runtime storage location, not necessarily a directory inside the packaged application. Store the database under the user's application-data directory on Windows.

## **Maven dependencies**

Start with these:

```
<dependencies>

    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>25</version>
    </dependency>

    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>25</version>
    </dependency>

    <!-- JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>...</version>
    </dependency>

    <!-- SQLite -->
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>...</version>
    </dependency>

    <!-- HTML extraction -->
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <version>...</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>...</version>
        <scope>test</scope>
    </dependency>

</dependencies>
```

The ellipses are intentional: resolve current stable versions through Maven Central when creating the project rather than pinning potentially outdated versions in this plan.

Use `javafx-maven-plugin` to run the application during development.

# **5\. Implementation phases and daily goals**

The seven-day schedule below is designed around working vertical slices. Each day should produce something executable and testable.

## **DAY 1 — Project foundation and JavaFX desktop shell**

Milestone 1: Puppy launches as a desktop application

Estimated effort: 6–8 hours

### **Objectives**

* Initialize the Maven project.  
* Configure JavaFX.  
* Establish the application entry point.  
* Create a transparent desktop window.  
* Load the mascot sprites.  
* Establish basic service initialization.

### **Task breakdown**

1\. Create the Maven project

Configure:

* JDK 25\.  
* JavaFX 25\.  
* Maven.  
* JavaFX Maven Plugin.  
* JUnit 5\.

Create `PuppyApplication.java`.

2\. Create the desktop window

The initial window should:

* Have no title bar.  
* Have a transparent background.  
* Stay on top of other windows.  
* Display the mascot in the center.  
* Allow the user to drag the mascot around the desktop.

Use JavaFX `StageStyle.TRANSPARENT`.

3\. Load mascot assets

Organize the six animations:

| State | Purpose |
| ----- | ----- |
| IDLE | Default desktop state |
| HAPPY | Positive reactions |
| THINKING | Processing requests |
| TALKING | Delivering responses |
| STUDYING | Study mode |
| SLEEPING | Idle for an extended period |

Implement `MascotState` as an enum:

```
public enum MascotState {
    IDLE,
    HAPPY,
    THINKING,
    TALKING,
    STUDYING,
    SLEEPING
}
```

Implement a `MascotAnimator` that handles frame switching and animation speed.

4\. Create application initialization

Create a `ServiceRegistry` responsible for initializing and providing the application services.

Don't instantiate your database, AI provider, and repositories directly inside JavaFX controllers.

### **Day 1 tests**

| Test | Expected result |
| ----- | ----- |
| Launch application | Puppy appears |
| Transparent window | Desktop visible behind puppy |
| Always on top | Puppy stays above other windows |
| Drag puppy | Puppy changes position |
| Change mascot state | Correct animation plays |
| Close application | Application exits cleanly |

Definition of done

JavaFX launches successfully.  
Transparent window works.  
All six mascot states can be displayed.  
Mascot can be dragged around.  
No backend or AI dependency is required.

Deliverable: A standalone interactive desktop puppy.

## **DAY 2 — Frontend interactions and UI**

Milestone 2: Complete desktop interface

Estimated effort: 6–8 hours

### **Objectives**

Implement the interaction flow we previously designed.

### **Task 1: Mascot interaction**

Implement:

* Hover detection.  
* Hover animation.  
* Double-click detection.  
* Main menu visibility.  
* Menu closing behavior.  
* Dragging without accidentally activating double-click.

The flow should be:

```
             IDLE
               |
             HOVER
               |
          Subtle reaction
               |
          DOUBLE CLICK
               |
       ┌───────┼───────┐
       |       |       |
     STUDY    CHAT    MORE
```

### **Task 2: Chat interface**

Create `chat.fxml` and `ChatController.java`.

UI components:

* Conversation header.  
* Mascot avatar.  
* Message list.  
* User message bubble.  
* Assistant message bubble.  
* Text input.  
* Send button.  
* Loading indicator.  
* New conversation button.

Initially, use mock responses.

### **Task 3: Study interface**

Create `study.fxml`.

Implement:

* 25-minute countdown.  
* Start/Pause.  
* Reset.  
* Focus/break status.  
* Mascot studying animation.  
* Session completion message.

Use a shortened one-minute timer for testing.

### **Task 4: Settings interface**

Create `settings.fxml`.

Initially include:

* AI mode selector.  
* Cloud AI configuration.  
* Ollama configuration.  
* Always-on-top setting.  
* Mascot visibility.  
* Application information.

Don't spend too long on visual polish yet.

### **Day 2 tests**

* Hover reaction works.  
* Double-click opens menu.  
* Study opens correctly.  
* Chat opens correctly.  
* More opens correctly.  
* Chat can display mock messages.  
* Timer can start, pause and reset.  
* Settings controls work.  
* Switching panels doesn't crash the application.

Deliverable: A fully navigable JavaFX desktop application with mock functionality.

## **DAY 3 — Pure Java backend services and persistence**

Milestone 3: Real application logic

Estimated effort: 7–9 hours

This is where we establish the backend architecture.

### **Task 1: Create domain models**

Start with these classes:

```
model/
├── ChatMessage.java
├── Conversation.java
├── AIRequest.java
├── AIResponse.java
├── WebSource.java
├── AppSettings.java
└── StudySession.java
```

For example:

```
public class ChatMessage {

    private String id;
    private String conversationId;
    private MessageRole role;
    private String content;
    private LocalDateTime createdAt;

}
```

Use constructors, getters, setters or Java records where appropriate.

### **Task 2: Create the service layer**

Implement:

ChatService

* Send messages.  
* Retrieve conversation history.  
* Create conversations.  
* Delete conversations.

StudyService

* Start sessions.  
* Pause sessions.  
* Resume sessions.  
* Complete sessions.

SettingsService

* Read settings.  
* Update settings.  
* Persist preferences.

### **Task 3: Implement SQLite**

Create these tables:

```
CREATE TABLE conversations (
    id TEXT PRIMARY KEY,
    title TEXT,
    created_at TEXT,
    updated_at TEXT
);

CREATE TABLE messages (
    id TEXT PRIMARY KEY,
    conversation_id TEXT,
    role TEXT,
    content TEXT,
    created_at TEXT,
    FOREIGN KEY (conversation_id)
        REFERENCES conversations(id)
);

CREATE TABLE settings (
    key TEXT PRIMARY KEY,
    value TEXT
);

CREATE TABLE study_sessions (
    id TEXT PRIMARY KEY,
    started_at TEXT,
    completed_at TEXT,
    duration_seconds INTEGER,
    status TEXT
);
```

Use prepared statements, transactions where appropriate, and enable SQLite foreign-key enforcement.

### **Task 4: Implement repositories**

Repositories should encapsulate all database operations.

For example:

```
public interface ChatRepository {

    void saveMessage(ChatMessage message);

    List<ChatMessage> findMessages(
        String conversationId
    );

    void deleteConversation(String id);
}
```

The implementation would be:

```
SQLiteChatRepository.java
```

### **Task 5: Connect frontend to services**

Replace the mock chat history with data retrieved from SQLite.

When sending a message:

1. Create the user message.  
2. Save it.  
3. Display it.  
4. Generate a mock assistant response.  
5. Save the assistant response.  
6. Display the response.

### **Day 3 tests**

* Conversations can be created.  
* Messages can be saved.  
* Messages can be retrieved.  
* Conversation history survives restart.  
* Settings persist.  
* Study sessions are recorded.  
* Repository tests pass independently.  
* JavaFX controllers do not execute SQL directly.

Deliverable: A functioning desktop application with a real Java service layer and SQLite persistence.

## **DAY 4 — AI integration: Cloud and local models**

Milestone 4: Puppy can actually talk

Estimated effort: 8–10 hours

This is the most important backend development phase.

### **Task 1: Define the AI provider abstraction**

Create:

```
public interface AIProvider {

    String getName();

    boolean isAvailable();

    CompletableFuture<Void> stream(
        AIRequest request,
        Consumer<String> onToken,
        Consumer<Throwable> onError,
        Runnable onComplete
    );
}
```

This interface allows the application to communicate with different AI providers without knowing their internal implementation.

The interface is deliberately asynchronous.

### **Task 2: Implement OllamaProvider**

Responsibilities:

* Check Ollama availability.  
* Retrieve installed models.  
* Send chat requests.  
* Stream generated tokens.  
* Handle timeouts and connection failures.

Use:

```
http://localhost:11434/api/chat
```

Example request:

```
{
  "model": "llama3.2",
  "messages": [
    {
      "role": "user",
      "content": "Hello Puppy!"
    }
  ],
  "stream": true
}
```

Start with a small model appropriate for the available RAM and hardware.

### **Task 3: Implement CloudAIProvider**

Choose one cloud provider initially.

Use its official API or an OpenAI-compatible API.

Responsibilities:

* API key configuration.  
* Model configuration.  
* Chat completion.  
* Streaming responses.  
* Error handling.

Store credentials outside the project repository. For a desktop application, environment variables or a local configuration file are acceptable for development, but a packaged application should use OS-protected credential storage where possible.

### **Task 4: Implement AIOrchestrator**

This class is the central intelligence coordinator.

Its responsibilities:

* Select the AI provider.  
* Determine the requested mode.  
* Manage conversation context.  
* Handle provider failures.  
* Route requests to web research when appropriate.

Define:

```
public enum AIMode {
    ONLINE,
    OFFLINE,
    AUTO
}
```

Suggested behavior:

| Mode | Behavior |
| ----- | ----- |
| ONLINE | Cloud model |
| OFFLINE | Ollama |
| AUTO | Cloud first, local fallback |

For the MVP, use simple routing rules rather than an agent framework.

Explicit user privacy choices must take precedence over automatic fallback.

### **Task 5: Connect mascot states**

The flow:

```
User sends message
       |
       v
    THINKING
       |
       v
  AI generates tokens
       |
       v
    TALKING
       |
       v
 Response completed
       |
       v
     HAPPY
       |
       v
      IDLE
```

One important detail: keep animation transitions controlled by the application state, not by individual token callbacks. Otherwise, the mascot might flicker between states during streaming.

### **Day 4 tests**

* Ollama connection works.  
* Installed models can be listed.  
* Local chat works.  
* Cloud chat works.  
* Both support streaming.  
* Online/offline switching works.  
* Auto routing works.  
* Conversation history is passed to the model.  
* AI errors do not crash JavaFX.  
* Mascot animation follows generation state.

Deliverable: Fully functional online/offline AI chatbot.

## **DAY 5 — Internet research and webpage extraction**

Milestone 5: Puppy can research the internet

Estimated effort: 7–9 hours

This phase adds the feature that makes Puppy more than a conventional chatbot.

### **Task 1: Implement TavilyClient**

Create:

```
public class TavilyClient {

    public List<WebSource> search(String query) {
        // Call Tavily API
        // Parse search results
        // Return normalized sources
    }
}
```

Use Tavily's search endpoint.

Retrieve the top three relevant sources initially.

### **Task 2: Implement PageExtractor**

Use Jsoup to retrieve and parse webpage content.

```
public class PageExtractor {

    public String extract(String url) {

        Document document = Jsoup.connect(url)
            .userAgent("PuppyAI/1.0")
            .timeout(10000)
            .get();

        return document
            .select("article")
            .text();
    }
}
```

This is only a starting point. Some websites do not use article tags, and JavaScript-heavy websites may require browser rendering.

For the MVP, support ordinary accessible webpages.

Also implement:

* URL validation.  
* Blocking localhost and private IP addresses.  
* Redirect limits.  
* Download size limits.  
* Request timeouts.  
* Graceful extraction failures.

### **Task 3: Implement WebResearchService**

This class coordinates the entire research workflow.

```
WebResearchService
       |
       v
TavilyClient
       |
       v
Search Results
       |
       v
PageExtractor
       |
       v
Extracted Content
       |
       v
AIOrchestrator
       |
       v
Research Summary
       |
       v
Web Sources
```

### **Task 4: Implement source citations**

Create `WebSource.java`:

```
public record WebSource(
    String title,
    String url,
    String snippet
) {}
```

The AI should receive the extracted content with source identifiers.

The final response should include source references that the UI renders as clickable links.

Do not allow the model to invent URLs or citations. Keep the original source metadata separately and associate citations with the retrieved source IDs.

### **Task 5: Add research mode to chat**

For example:

```
User:

"What are the latest features in Java 25?"

Puppy:

Let me look that up for you!

[THINKING animation]

Searching the web...

[STUDYING animation]

I found three relevant sources.

Here is what changed...

Sources:
1. Oracle Java Documentation
2. OpenJDK
3. Java Community News

[TALKING animation]
```

For the MVP, an explicit Research button or a simple current-information detection rule is sufficient. Don't spend the day developing sophisticated intent classification.

### **Day 5 tests**

* Tavily returns search results.  
* Three relevant sources can be retrieved.  
* Static webpages can be extracted.  
* Extraction failures are handled.  
* Research summaries use retrieved content.  
* Citations reference actual retrieved sources.  
* Source links open in the browser.  
* Research status updates mascot animations.  
* Ordinary offline chat does not require Tavily.

Deliverable: An AI assistant capable of internet research with citations.

## **DAY 6 — Study mode, settings and complete integration**

Milestone 6: All MVP features work together

Estimated effort: 6–8 hours

### **Task 1: Finish Pomodoro**

Default configuration:

| Setting | Value |
| ----- | ----- |
| Focus | 25 minutes |
| Short break | 5 minutes |
| Long break | 15 minutes |
| Long break interval | 4 sessions |

For the MVP, focus and short breaks are mandatory. Long breaks can be a stretch goal.

Use `ScheduledExecutorService` or JavaFX animation utilities for updating the display, but calculate the actual remaining time from a target timestamp rather than relying on the number of timer ticks.

### **Task 2: Finish settings**

Implement:

* Online/offline/auto mode.  
* Cloud model configuration.  
* Local model configuration.  
* API key management.  
* Always-on-top.  
* Start on boot, if time permits.  
* Mascot visibility.  
* Theme selection.

### **Task 3: Application-wide state management**

Create a central `MascotStateService`.

It should receive events from different parts of the application.

For example:

| Event | Mascot state |
| ----- | ----- |
| Application idle | IDLE |
| User opens chat | HAPPY |
| AI is generating | THINKING |
| AI is streaming | TALKING |
| Study session active | STUDYING |
| Extended inactivity | SLEEPING |

Be careful about competing states. If the puppy is studying, an ordinary application refresh should not change it back to IDLE.

A simple priority system or explicit state transition rules will solve this.

### **Task 4: Error handling and integration**

Create a centralized error-handling mechanism.

The UI should distinguish:

* Internet unavailable.  
* Cloud provider unavailable.  
* Ollama unavailable.  
* API key invalid.  
* Search failed.  
* Page extraction failed.  
* Database error.

Never show raw stack traces to the user.

### **Day 6 tests**

* Pomodoro works.  
* Break mode works.  
* Settings persist after restart.  
* AI model selection works.  
* Mascot states don't conflict.  
* Chat and research work together.  
* Application handles missing services gracefully.  
* No blocking operations run on the JavaFX Application Thread.

Deliverable: All major features integrated into one application.

## **DAY 7 — Testing, optimization and packaging**

Milestone 7: Puppy AI v1.0

Estimated effort: 6–8 hours

### **Task 1: Functional testing**

Run through the complete workflow:

| Feature | Test |
| ----- | ----- |
| Desktop pet | Launch and move puppy |
| Hover | Trigger reaction |
| Double-click | Open main menu |
| Chat | Send and receive messages |
| Streaming | Display tokens incrementally |
| Online AI | Generate cloud response |
| Offline AI | Generate Ollama response |
| Auto mode | Choose available provider |
| Research | Search and summarize |
| Citations | Open original source |
| Study | Complete a focus session |
| Persistence | Restart and restore history |
| Settings | Change and restore preferences |

### **Task 2: Failure testing**

Test:

* Ollama not running.  
* Invalid cloud credentials.  
* Internet disconnecting mid-response.  
* Failed webpage extraction.  
* Empty user messages.  
* Multiple requests submitted rapidly.  
* User closing a panel during generation.  
* Database unavailable.  
* Application shutdown while an AI request is running.

### **Task 3: Packaging**

Use `jpackage` to create a Windows application installer.

Example command structure:

```
jpackage \
  --name PuppyAI \
  --input target \
  --main-jar puppy-ai.jar \
  --type exe \
  --dest release
```

This is illustrative; your actual command needs the JavaFX runtime modules and any required dependencies configured correctly.

For local AI, do not bundle the model. Let the user install Ollama and download a model separately.

### **Task 4: Documentation**

Your README should contain:

1. Project introduction.  
2. Features.  
3. Architecture.  
4. Technology stack.  
5. Development setup.  
6. Ollama installation.  
7. Cloud AI configuration.  
8. Tavily configuration.  
9. Build instructions.  
10. Known limitations.

### **Day 7 acceptance criteria**

* All critical tests pass.  
* No major UI freezes.  
* No major crashes.  
* Application works with online AI.  
* Application works with offline AI.  
* Application works without internet in offline mode.  
* Installer is generated.  
* README is complete.

Deliverable: Puppy AI v1.0 Windows release.

# **6\. Important technical implementation contracts**

I would establish these interfaces early, so that different parts of the project can be developed independently.

### **A. AIProvider**

```
public interface AIProvider {

    String getName();

    boolean isAvailable();

    CompletableFuture<Void> stream(
        AIRequest request,
        Consumer<String> onToken,
        Consumer<Throwable> onError,
        Runnable onComplete
    );
}
```

Both cloud and local providers must implement this contract.

### **B. ChatService**

```
public interface ChatService {

    Conversation createConversation();

    CompletableFuture<Void> sendMessage(
        String conversationId,
        String message,
        Consumer<String> onToken
    );

    List<ChatMessage> getHistory(
        String conversationId
    );
}
```

### **C. StudyService**

```
public interface StudyService {

    void startSession(int durationSeconds);

    void pauseSession();

    void resumeSession();

    void resetSession();

    int getRemainingSeconds();

    boolean isRunning();
}
```

### **D. WebResearchService**

```
public interface WebResearchService {

    CompletableFuture<ResearchResult> research(
        String query
    );
}
```

### **E. MascotStateService**

```
public interface MascotStateService {

    void setState(MascotState state);

    MascotState getState();

    void resetToIdle();
}
```

These are contracts, not necessarily the final implementations. You can modify them as you discover requirements.

# **7\. Concurrency and JavaFX rules**

This deserves its own section because it's one of the easiest places to introduce serious bugs.

Your application will perform several operations simultaneously:

* JavaFX renders the UI.  
* Cloud AI generates a response.  
* Ollama may generate another response.  
* Web research retrieves webpages.  
* SQLite reads and writes data.  
* The study timer runs.

The JavaFX Application Thread must remain responsive.

I'd use:

| Operation | Execution |
| ----- | ----- |
| UI rendering | JavaFX Application Thread |
| AI generation | ExecutorService |
| Web retrieval | ExecutorService |
| Database access | Background task |
| Timer calculation | ScheduledExecutorService |
| UI updates | Platform.runLater |

Example:

```
ExecutorService executor =
    Executors.newFixedThreadPool(4);

executor.submit(() -> {

    String response = aiService.generate(request);

    Platform.runLater(() -> {
        chatController.displayResponse(response);
    });

});
```

For streaming, each received chunk should be passed to the UI safely. Batch UI updates if necessary to avoid flooding the JavaFX Application Thread.

Also implement:

* Cancellation tokens or interruption-aware requests.  
* Connection and read timeouts.  
* Graceful executor shutdown.  
* A limit on simultaneous AI requests.

One request per active conversation is a sensible initial constraint.

# **8\. Feature dependencies and implementation order**

This is important when vibe coding.

Some features depend on others. If you implement them in the wrong order, you'll spend time debugging multiple systems simultaneously.

Here is the recommended dependency graph:

\#chatgpt-mermaid-\_r\_1cv\_{font-family:-apple-system-body,ui-sans-serif,-apple-system,system-ui,Segoe UI,Helvetica,Apple Color Emoji,Arial,sans-serif,Segoe UI Emoji,Segoe UI Symbol;font-size:16px;fill:rgb(237, 237, 237);}@keyframes edge-animation-frame{from{stroke-dashoffset:0;}}@keyframes dash{to{stroke-dashoffset:0;}}\#chatgpt-mermaid-\_r\_1cv\_ .edge-animation-slow{stroke-dasharray:9,5\!important;stroke-dashoffset:900;animation:dash 50s linear infinite;stroke-linecap:round;}\#chatgpt-mermaid-\_r\_1cv\_ .edge-animation-fast{stroke-dasharray:9,5\!important;stroke-dashoffset:900;animation:dash 20s linear infinite;stroke-linecap:round;}\#chatgpt-mermaid-\_r\_1cv\_ .error-icon{fill:rgb(48, 48, 48);}\#chatgpt-mermaid-\_r\_1cv\_ .error-text{fill:rgb(237, 237, 237);stroke:rgb(237, 237, 237);}\#chatgpt-mermaid-\_r\_1cv\_ .edge-thickness-normal{stroke-width:1px;}\#chatgpt-mermaid-\_r\_1cv\_ .edge-thickness-thick{stroke-width:3.5px;}\#chatgpt-mermaid-\_r\_1cv\_ .edge-pattern-solid{stroke-dasharray:0;}\#chatgpt-mermaid-\_r\_1cv\_ .edge-thickness-invisible{stroke-width:0;fill:none;}\#chatgpt-mermaid-\_r\_1cv\_ .edge-pattern-dashed{stroke-dasharray:3;}\#chatgpt-mermaid-\_r\_1cv\_ .edge-pattern-dotted{stroke-dasharray:2;}\#chatgpt-mermaid-\_r\_1cv\_ .marker{fill:rgb(175, 175, 175);stroke:rgb(175, 175, 175);}\#chatgpt-mermaid-\_r\_1cv\_ .marker.cross{stroke:rgb(175, 175, 175);}\#chatgpt-mermaid-\_r\_1cv\_ svg{font-family:-apple-system-body,ui-sans-serif,-apple-system,system-ui,Segoe UI,Helvetica,Apple Color Emoji,Arial,sans-serif,Segoe UI Emoji,Segoe UI Symbol;font-size:16px;}\#chatgpt-mermaid-\_r\_1cv\_ p{margin:0;}\#chatgpt-mermaid-\_r\_1cv\_ .label{font-family:-apple-system-body,ui-sans-serif,-apple-system,system-ui,Segoe UI,Helvetica,Apple Color Emoji,Arial,sans-serif,Segoe UI Emoji,Segoe UI Symbol;color:rgb(237, 237, 237);}\#chatgpt-mermaid-\_r\_1cv\_ .cluster-label text{fill:rgb(237, 237, 237);}\#chatgpt-mermaid-\_r\_1cv\_ .cluster-label span{color:rgb(237, 237, 237);}\#chatgpt-mermaid-\_r\_1cv\_ .cluster-label span p{background-color:transparent;}\#chatgpt-mermaid-\_r\_1cv\_ .label text,\#chatgpt-mermaid-\_r\_1cv\_ span{fill:rgb(237, 237, 237);color:rgb(237, 237, 237);}\#chatgpt-mermaid-\_r\_1cv\_ .node rect,\#chatgpt-mermaid-\_r\_1cv\_ .node circle,\#chatgpt-mermaid-\_r\_1cv\_ .node ellipse,\#chatgpt-mermaid-\_r\_1cv\_ .node polygon,\#chatgpt-mermaid-\_r\_1cv\_ .node path{fill:rgb(9, 23, 44);stroke:rgb(31, 78, 148);stroke-width:1px;}\#chatgpt-mermaid-\_r\_1cv\_ .rough-node .label text,\#chatgpt-mermaid-\_r\_1cv\_ .node .label text,\#chatgpt-mermaid-\_r\_1cv\_ .image-shape .label,\#chatgpt-mermaid-\_r\_1cv\_ .icon-shape .label{text-anchor:middle;}\#chatgpt-mermaid-\_r\_1cv\_ .node .katex path{fill:\#000;stroke:\#000;stroke-width:1px;}\#chatgpt-mermaid-\_r\_1cv\_ .rough-node .label,\#chatgpt-mermaid-\_r\_1cv\_ .node .label,\#chatgpt-mermaid-\_r\_1cv\_ .image-shape .label,\#chatgpt-mermaid-\_r\_1cv\_ .icon-shape .label{text-align:center;}\#chatgpt-mermaid-\_r\_1cv\_ .node.clickable{cursor:pointer;}\#chatgpt-mermaid-\_r\_1cv\_ .root .anchor path{fill:rgb(175, 175, 175)\!important;stroke-width:0;stroke:rgb(175, 175, 175);}\#chatgpt-mermaid-\_r\_1cv\_ .arrowheadPath{fill:rgb(175, 175, 175);}\#chatgpt-mermaid-\_r\_1cv\_ .edgePath .path{stroke:rgb(175, 175, 175);stroke-width:1px;}\#chatgpt-mermaid-\_r\_1cv\_ .flowchart-link{stroke:rgb(175, 175, 175);fill:none;}\#chatgpt-mermaid-\_r\_1cv\_ .edgeLabel{background-color:rgb(0, 0, 0);text-align:center;}\#chatgpt-mermaid-\_r\_1cv\_ .edgeLabel p{background-color:rgb(0, 0, 0);}\#chatgpt-mermaid-\_r\_1cv\_ .edgeLabel rect{opacity:0.5;background-color:rgb(0, 0, 0);fill:rgb(0, 0, 0);}\#chatgpt-mermaid-\_r\_1cv\_ .labelBkg{background-color:rgba(0, 0, 0, 0.5);}\#chatgpt-mermaid-\_r\_1cv\_ .cluster rect{fill:rgb(48, 48, 48);stroke:rgba(255, 255, 255, 0.15);stroke-width:1px;}\#chatgpt-mermaid-\_r\_1cv\_ .cluster text{fill:rgb(237, 237, 237);}\#chatgpt-mermaid-\_r\_1cv\_ .cluster span{color:rgb(237, 237, 237);}\#chatgpt-mermaid-\_r\_1cv\_ div.mermaidTooltip{position:absolute;text-align:center;max-width:200px;padding:2px;font-family:-apple-system-body,ui-sans-serif,-apple-system,system-ui,Segoe UI,Helvetica,Apple Color Emoji,Arial,sans-serif,Segoe UI Emoji,Segoe UI Symbol;font-size:12px;background:rgb(48, 48, 48);border:1px solid rgba(255, 255, 255, 0.15);border-radius:2px;pointer-events:none;z-index:100;}\#chatgpt-mermaid-\_r\_1cv\_ .flowchartTitleText{text-anchor:middle;font-size:18px;fill:rgb(237, 237, 237);}\#chatgpt-mermaid-\_r\_1cv\_ rect.text{fill:none;stroke-width:0;}\#chatgpt-mermaid-\_r\_1cv\_ .icon-shape,\#chatgpt-mermaid-\_r\_1cv\_ .image-shape{background-color:rgb(0, 0, 0);text-align:center;}\#chatgpt-mermaid-\_r\_1cv\_ .icon-shape p,\#chatgpt-mermaid-\_r\_1cv\_ .image-shape p{background-color:rgb(0, 0, 0);padding:2px;}\#chatgpt-mermaid-\_r\_1cv\_ .icon-shape .label rect,\#chatgpt-mermaid-\_r\_1cv\_ .image-shape .label rect{opacity:0.5;background-color:rgb(0, 0, 0);fill:rgb(0, 0, 0);}\#chatgpt-mermaid-\_r\_1cv\_ .label-icon{display:inline-block;height:1em;overflow:visible;vertical-align:-0.125em;}\#chatgpt-mermaid-\_r\_1cv\_ .node .label-icon path{fill:currentColor;stroke:revert;stroke-width:revert;}\#chatgpt-mermaid-\_r\_1cv\_ .node .neo-node{stroke:rgb(31, 78, 148);}\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].node rect,\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].cluster rect,\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].node polygon{stroke:url(\#chatgpt-mermaid-\_r\_1cv\_-gradient);filter:drop-shadow( 1px 2px 2px rgba(185,185,185,1));}\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].swimlane.cluster rect{filter:none;}\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].node path{stroke:url(\#chatgpt-mermaid-\_r\_1cv\_-gradient);stroke-width:1px;}\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].node .outer-path{filter:drop-shadow( 1px 2px 2px rgba(185,185,185,1));}\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].node .neo-line path{stroke:rgb(31, 78, 148);filter:none;}\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].node circle{stroke:url(\#chatgpt-mermaid-\_r\_1cv\_-gradient);filter:drop-shadow( 1px 2px 2px rgba(185,185,185,1));}\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].node circle .state-start{fill:\#000000;}\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].icon-shape .icon{fill:url(\#chatgpt-mermaid-\_r\_1cv\_-gradient);filter:drop-shadow( 1px 2px 2px rgba(185,185,185,1));}\#chatgpt-mermaid-\_r\_1cv\_ \[data-look="neo"\].icon-shape .icon-neo path{stroke:url(\#chatgpt-mermaid-\_r\_1cv\_-gradient);filter:drop-shadow( 1px 2px 2px rgba(185,185,185,1));}\#chatgpt-mermaid-\_r\_1cv\_ .node text{font-size:14px;font-weight:600;letter-spacing:normal;fill:rgb(153, 206, 255);}\#chatgpt-mermaid-\_r\_1cv\_ .edgeLabels text{font-size:13px;font-weight:600;letter-spacing:-0.08px;fill:rgb(153, 206, 255);}\#chatgpt-mermaid-\_r\_1cv\_ .node tspan\[font-weight="normal"\],\#chatgpt-mermaid-\_r\_1cv\_ .edgeLabels tspan\[font-weight="normal"\]{font-weight:600;}\#chatgpt-mermaid-\_r\_1cv\_ .edgeLabel .label rect{opacity:1;rx:13px;ry:13px;fill:rgb(0, 14, 26);stroke:rgb(26, 62, 95);stroke-width:1px;}\#chatgpt-mermaid-\_r\_1cv\_ .node rect,\#chatgpt-mermaid-\_r\_1cv\_ .node circle,\#chatgpt-mermaid-\_r\_1cv\_ .node ellipse,\#chatgpt-mermaid-\_r\_1cv\_ .node polygon,\#chatgpt-mermaid-\_r\_1cv\_ .node path{fill:rgb(0, 40, 77);stroke:rgba(255, 255, 255, 0.1);stroke-width:1px;}\#chatgpt-mermaid-\_r\_1cv\_ .node rect{rx:16px;ry:16px;}\#chatgpt-mermaid-\_r\_1cv\_ .mermaid-decision .label-container{fill:rgb(0, 14, 26);stroke:rgb(26, 62, 95);stroke-dasharray:2px,2px;}\#chatgpt-mermaid-\_r\_1cv\_ .edgePaths .flowchart-link{stroke:rgb(175, 175, 175);stroke-width:1px;stroke-linecap:round;stroke-linejoin:round;}\#chatgpt-mermaid-\_r\_1cv\_ .marker{fill:rgb(175, 175, 175);stroke:rgb(175, 175, 175);}\#chatgpt-mermaid-\_r\_1cv\_ :root{--mermaid-font-family:-apple-system-body,ui-sans-serif,-apple-system,system-ui,Segoe UI,Helvetica,Apple Color Emoji,Arial,sans-serif,Segoe UI Emoji,Segoe UI Symbol;}Maven \+ JavaFX SetupDesktop WindowMascot RendererDesktop InteractionsChat / Study / Settings UIJava Service LayerSQLite PersistenceAI Provider InterfaceOllama ProviderCloud AI ProviderAI OrchestratorChat IntegrationStreaming UIWeb ResearchTavily SearchJsoup ExtractionCited AI ResponsesPomodoro IntegrationMascot State IntegrationTestingjpackage Release

Important: Frontend development and backend development can proceed in parallel after Day 1, provided their interfaces are agreed upon.

For example, the frontend developer can build Chat using a mock `ChatService`, while the backend implementation is being developed.

# **9\. Suggested Git workflow**

I would use feature branches even if you're working alone. It makes vibe coding easier to control.

```
main
│
├── feature/javafx-foundation
├── feature/mascot-animation
├── feature/desktop-interaction
├── feature/chat-ui
├── feature/sqlite-persistence
├── feature/ai-provider
├── feature/ollama
├── feature/cloud-ai
├── feature/web-research
├── feature/study-mode
└── feature/packaging
```

Suggested commit milestones:

| Commit | Expected result |
| ----- | ----- |
| `init` | Maven project created |
| `desktop-shell` | JavaFX transparent window |
| `mascot-animation` | Six mascot states |
| `desktop-menu` | Hover and double-click |
| `chat-ui` | Functional chat interface |
| `persistence` | SQLite integration |
| `ai-abstraction` | AIProvider interface |
| `ollama-integration` | Offline AI |
| `cloud-integration` | Online AI |
| `web-research` | Search and citations |
| `study-mode` | Pomodoro |
| `release` | Packaged application |

Don't let an AI coding tool modify the entire project in one go. Ask it to implement one component, inspect the changes, compile, test, and commit.

# **10\. Risk management: What could derail the project?**

These are the things I would pay particular attention to.

| Risk | Impact | Mitigation |
| ----- | ----- | ----- |
| JavaFX transparent window issues | High | Test on Day 1 |
| Sprite animation problems | Medium | Build renderer early |
| Ollama model too large | High | Use small model |
| AI streaming bugs | High | Test independently |
| Cloud API incompatibility | Medium | Choose provider early |
| Websites block extraction | Medium | Use search snippets as fallback |
| JavaFX UI freezes | High | Enforce background execution |
| SQLite concurrency | Medium | Centralize database access |
| Packaging JavaFX dependencies | High | Test packaging on Day 7, preferably a small smoke build earlier |
| Scope creep | Very high | Freeze MVP after Day 2 |

One particularly important point: don't build your own web scraper infrastructure. Tavily already handles search discovery. Jsoup is enough for basic page extraction.

Likewise, don't build your own LLM inference engine. Ollama already handles local inference.

Your Java application should orchestrate these capabilities.

# **11\. MVP completion checklist**

This is the final checklist I'd use to determine whether the project is actually finished.

Project completion

# **0%**

0 of 30 requirements completed

1\. Desktop companion

Transparent JavaFX desktop window  
Mascot can be dragged  
Hover reaction  
Double-click menu  
Six mascot animations  
Always-on-top behavior

2\. AI chat

Chat interface  
Cloud AI integration  
Ollama integration  
Online/offline/auto modes  
Streaming responses  
Conversation history  
SQLite persistence

3\. Internet research

Tavily search  
Webpage extraction  
AI summarization  
Clickable source citations  
Failed-page handling  
URL and network security checks

4\. Study mode

25-minute focus timer  
Pause/resume/reset  
Short break  
Completion notification  
Studying and happy animations

5\. Release

Settings persistence  
Error handling  
Application shutdown handling  
JUnit tests  
Windows installer  
README and setup instructions  
Reset checklist

# **12\. What I would actually prioritize if time runs out**

The one-week deadline means we need a fallback plan.

Must ship

Core identity of Puppy AI

* Desktop puppy.  
* Interactive mascot.  
* Chat interface.  
* One working cloud AI provider.  
* Ollama offline mode.  
* Basic Pomodoro.

Ship if possible

Differentiating features

* Web research.  
* Source citations.  
* Conversation history.  
* Automatic AI routing.  
* Settings.

Version 2

Long-term product features

* Voice.  
* Long-term memory.  
* Document understanding.  
* Advanced research.  
* Autonomous actions.  
* Customizable mascot behavior.

My preferred fallback is to sacrifice automatic routing and advanced web extraction before sacrificing the desktop mascot, functional chat, or offline AI.

# **13\. How I would approach the actual coding process**

Since you're planning to vibe code, I would use a very specific development loop:

1. Define: Ask the coding assistant to implement one well-defined feature.  
2. Inspect: Review the generated files and architecture.  
3. Compile: Run Maven compilation.  
4. Test: Run the relevant JUnit tests or manual acceptance tests.  
5. Integrate: Connect the feature to the existing application.  
6. Commit: Save a working checkpoint.

Useful commands:

```
# Compile
mvn clean compile

# Run tests
mvn test

# Launch application
mvn javafx:run

# Package
mvn package
```

I would also create a `docs/IMPLEMENTATION_STATUS.md` file and update it every day. It should contain:

* Completed tasks.  
* Current implementation.  
* Known bugs.  
* Pending tasks.  
* Important architectural decisions.

This gives your coding assistant a reliable project context instead of making it rediscover the architecture every time.

## **My final recommendation**

I think the biggest advantage of this architecture is that you get to build something that demonstrates several important Java skills:

| Java topic | Where you'll use it |
| ----- | ----- |
| OOP and interfaces | AIProvider, services, repositories |
| JavaFX | Desktop GUI |
| JavaFX CSS/FXML | UI design |
| Multithreading | AI generation and web retrieval |
| Java I/O | Configuration and persistence |
| HTTP networking | Cloud AI, Ollama, Tavily |
| JSON processing | API communication |
| JDBC | SQLite |
| Exception handling | External service failures |
| Collections and Streams | Conversation and search-result processing |

You're not just wrapping an API in a GUI. You're building a Java desktop application with a real application architecture.

If I were building this, I would make Day 1 and Day 2 non-negotiable: get the puppy looking and behaving exactly as intended before worrying about AI. That's the product's identity. The AI makes it useful; the puppy makes people want to keep it on their desktop.

The next logical step is to generate the actual Maven starter project, including the JavaFX entry point, service registry, FXML files, and initial interfaces, so implementation can begin immediately.

Daily Puppy AI build check-in  
