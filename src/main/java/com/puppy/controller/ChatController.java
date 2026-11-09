package com.puppy.controller;

import com.puppy.enums.MascotState;
import com.puppy.service.MascotStateService;
import com.puppy.service.MockChatResponder;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/** Chat panel backed by deterministic responses until the AI milestone. */
public class ChatController {

    private static final long MOCK_THINKING_DELAY_MILLIS = 650;

    @FXML
    private VBox messagesBox;

    @FXML
    private VBox historyPane;

    @FXML
    private VBox historyItems;

    @FXML
    private TextField messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private ScrollPane messageScroll;

    @FXML
    private Label thinkingLabel;

    @FXML
    private ImageView chatMascotView;

    private final List<Conversation> history = new ArrayList<>();
    private final List<Message> currentMessages = new ArrayList<>();
    private MockChatResponder responder;
    private ExecutorService executor;
    private MascotStateService mascotStateService;
    private Runnable closeAction;
    private Runnable minimizeAction;
    private boolean responding;
    private int selectedHistoryIndex = -1;
    private int responseGeneration;
    private PauseTransition talkingTransition;
    private PauseTransition happyTransition;

    @FXML
    public void initialize() {
        loadChatMascot();
        addMessage("assistant", MockChatResponder.GREETING);
    }

    private void loadChatMascot() {
        try (InputStream stream = getClass().getResourceAsStream("/icons/puppy.png")) {
            if (stream == null) {
                throw new IllegalStateException("Missing chat mascot icon: /icons/puppy.png");
            }
            chatMascotView.setImage(new Image(stream));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the chat mascot icon", e);
        }
    }

    public void attach(MockChatResponder responder, ExecutorService executor,
                       MascotStateService mascotStateService,
                       Runnable closeAction, Runnable minimizeAction) {
        this.responder = Objects.requireNonNull(responder, "responder");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.mascotStateService = Objects.requireNonNull(mascotStateService, "mascotStateService");
        this.closeAction = Objects.requireNonNull(closeAction, "closeAction");
        this.minimizeAction = Objects.requireNonNull(minimizeAction, "minimizeAction");
        talkingTransition = new PauseTransition(Duration.millis(650));
        talkingTransition.setOnFinished(event -> {
            mascotStateService.setState(MascotState.HAPPY);
            happyTransition.playFromStart();
        });
        happyTransition = new PauseTransition(Duration.millis(700));
        happyTransition.setOnFinished(event -> mascotStateService.resetToIdle());
    }

    @FXML
    private void sendMessage() {
        String text = messageInput.getText();
        if (text == null || text.isBlank() || responding) {
            return;
        }

        String message = text.trim();
        messageInput.clear();
        stopMascotReaction();
        addMessage("user", message);
        responding = true;
        thinkingLabel.setManaged(true);
        thinkingLabel.setVisible(true);
        sendButton.setDisable(true);
        mascotStateService.setState(MascotState.THINKING);
        int generation = ++responseGeneration;
        try {
            executor.execute(() -> createMockReply(message, generation));
        } catch (RejectedExecutionException e) {
            responding = false;
            thinkingLabel.setManaged(false);
            thinkingLabel.setVisible(false);
            sendButton.setDisable(false);
            addMessage("assistant", "I couldn't start a reply because the application is shutting down.");
            mascotStateService.resetToIdle();
        }
    }

    private void createMockReply(String message, int generation) {
        try {
            Thread.sleep(MOCK_THINKING_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        String reply = responder.replyTo(message);
        Platform.runLater(() -> {
            if (!responding || generation != responseGeneration) {
                return;
            }
            addMessage("assistant", reply);
            responding = false;
            thinkingLabel.setManaged(false);
            thinkingLabel.setVisible(false);
            sendButton.setDisable(false);
            mascotStateService.setState(MascotState.TALKING);
            talkingTransition.playFromStart();
        });
    }

    @FXML
    private void toggleHistory() {
        boolean show = !historyPane.isVisible();
        historyPane.setManaged(show);
        historyPane.setVisible(show);
        if (show) {
            refreshHistory();
        }
    }

    public void showHistory() {
        historyPane.setManaged(true);
        historyPane.setVisible(true);
        refreshHistory();
    }

    @FXML
    private void newConversation() {
        stopMascotReaction();
        responseGeneration++;
        responding = false;
        thinkingLabel.setManaged(false);
        thinkingLabel.setVisible(false);
        sendButton.setDisable(false);
        if (selectedHistoryIndex < 0) {
            archiveCurrentConversation();
        }
        selectedHistoryIndex = -1;
        currentMessages.clear();
        messagesBox.getChildren().clear();
        addMessage("assistant", MockChatResponder.GREETING);
        refreshHistory();
        messageInput.requestFocus();
    }

    private void archiveCurrentConversation() {
        if (currentMessages.stream().noneMatch(message -> "user".equals(message.role()))) {
            return;
        }
        String title = currentMessages.stream()
                .filter(message -> "user".equals(message.role()))
                .findFirst()
                .map(Message::text)
                .map(text -> text.length() > 28 ? text.substring(0, 25) + "..." : text)
                .orElse("Conversation");
        history.add(new Conversation(title, List.copyOf(currentMessages)));
    }

    private void selectConversation(int index) {
        if (index < 0 || index >= history.size()) {
            return;
        }
        stopMascotReaction();
        responseGeneration++;
        responding = false;
        thinkingLabel.setManaged(false);
        thinkingLabel.setVisible(false);
        sendButton.setDisable(false);
        if (selectedHistoryIndex < 0) {
            archiveCurrentConversation();
        } else {
            updateSelectedConversation();
        }
        selectedHistoryIndex = index;
        currentMessages.clear();
        currentMessages.addAll(history.get(index).messages());
        renderMessages();
        refreshHistory();
    }

    private void refreshHistory() {
        historyItems.getChildren().clear();
        if (history.isEmpty()) {
            Label empty = new Label("No saved chats yet.");
            empty.getStyleClass().add("muted-copy");
            empty.setWrapText(true);
            historyItems.getChildren().add(empty);
            return;
        }
        for (int i = 0; i < history.size(); i++) {
            int index = i;
            Button item = new Button(history.get(i).title());
            item.getStyleClass().add("history-item");
            item.setMaxWidth(Double.MAX_VALUE);
            item.setOnAction(event -> selectConversation(index));
            historyItems.getChildren().add(item);
        }
    }

    private void addMessage(String role, String text) {
        Message message = new Message(role, text);
        currentMessages.add(message);
        updateSelectedConversation();
        addMessageNode(message);
        messageScroll.setVvalue(1.0);
    }

    private void updateSelectedConversation() {
        if (selectedHistoryIndex < 0 || selectedHistoryIndex >= history.size()) {
            return;
        }
        Conversation existing = history.get(selectedHistoryIndex);
        history.set(selectedHistoryIndex, new Conversation(existing.title(), List.copyOf(currentMessages)));
    }

    private void stopMascotReaction() {
        if (talkingTransition != null) {
            talkingTransition.stop();
        }
        if (happyTransition != null) {
            happyTransition.stop();
        }
    }

    private void renderMessages() {
        messagesBox.getChildren().clear();
        for (Message message : currentMessages) {
            addMessageNode(message);
        }
        messageScroll.setVvalue(1.0);
    }

    private void addMessageNode(Message message) {
        Label bubble = new Label(message.text());
        bubble.setWrapText(true);
        bubble.setMaxWidth(360);
        bubble.getStyleClass().addAll("message-bubble",
                "user".equals(message.role()) ? "user-bubble" : "assistant-bubble");
        HBox row = new HBox(bubble);
        row.setAlignment("user".equals(message.role()) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messagesBox.getChildren().add(row);
    }

    @FXML
    private void onClose() {
        closeAction.run();
    }

    @FXML
    private void onMinimize() {
        minimizeAction.run();
    }

    public void close() {
        responseGeneration++;
        responding = false;
        stopMascotReaction();
    }

    private record Message(String role, String text) {
    }

    private record Conversation(String title, List<Message> messages) {
    }
}
