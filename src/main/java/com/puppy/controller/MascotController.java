package com.puppy.controller;

import com.puppy.config.ServiceRegistry;
import com.puppy.enums.MascotState;
import com.puppy.service.MascotStateService;
import com.puppy.ui.MascotAnimator;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Coordinates the mascot shell, its navigation and the cached feature panels.
 */
public class MascotController {

    private static final double COMPACT_WIDTH = 336;
    private static final double COMPACT_HEIGHT = 252;
    private static final double MENU_WIDTH = 380;
    private static final double MENU_HEIGHT = 360;
    private static final double STUDY_WIDTH = 380;
    private static final double STUDY_HEIGHT = 470;
    private static final double PANEL_WIDTH = 560;
    private static final double PANEL_HEIGHT = 450;
    private static final double SETTINGS_HEIGHT = 620;
    private static final double DRAG_THRESHOLD = 5;

    @FXML
    private VBox shellRoot;

    @FXML
    private StackPane panelHost;

    @FXML
    private StackPane menuHost;

    @FXML
    private HBox mainMenu;

    @FXML
    private StackPane mascotDock;

    @FXML
    private ImageView mascotView;

    @FXML
    private Label hoverHint;

    private final Map<Panel, Parent> cachedPanels = new EnumMap<>(Panel.class);
    private MascotStateService mascotStateService;
    private Consumer<MascotState> stateListener;
    private MascotAnimator animator;
    private ContextMenu contextMenu;
    private Stage stage;
    private ServiceRegistry services;
    private double pressScreenX;
    private double pressScreenY;
    private double pressStageX;
    private double pressStageY;
    private boolean draggedSincePress;
    private Panel activePanel;
    private ChatController chatController;
    private StudyController studyController;
    private SettingsController settingsController;
    private PauseTransition hoverReset;

    @FXML
    public void initialize() {
        animator = new MascotAnimator(mascotView);
        hoverReset = new PauseTransition(Duration.millis(800));
        hoverReset.setOnFinished(event -> {
            if (activePanel == null) {
                restoreMascotState();
            }
        });
        mascotDock.setOnMousePressed(this::onMousePressed);
        mascotDock.setOnMouseDragged(this::onMouseDragged);
        mascotDock.setOnMouseClicked(this::onMascotClicked);
        mascotDock.setOnMouseEntered(event -> onMascotHover(true));
        mascotDock.setOnMouseExited(event -> onMascotHover(false));
        shellRoot.addEventFilter(MouseEvent.MOUSE_CLICKED, this::dismissMenuOnOutsideClick);
    }

    /**
     * Connects the shell to application services and the frameless window.
     * Must be called on the JavaFX Application Thread.
     */
    public void attach(Stage stage, ServiceRegistry services) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.services = Objects.requireNonNull(services, "services");
        this.mascotStateService = services.mascotStateService();
        this.stateListener = this::onMascotStateChanged;
        mascotStateService.addListener(stateListener);
        animator.play(mascotStateService.getState());
        stage.setWidth(COMPACT_WIDTH);
        stage.setHeight(COMPACT_HEIGHT);
        stage.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (activePanel != null) {
                    collapseToMascot();
                } else {
                    closeMenu();
                }
                event.consume();
            }
        });
        shellRoot.setOnContextMenuRequested(event -> {
            contextMenu().show(shellRoot, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    @FXML
    private void openStudy() {
        openPanel(Panel.STUDY);
    }

    @FXML
    private void openChat() {
        openPanel(Panel.CHAT);
    }

    @FXML
    private void openMore() {
        if (menuHost.isVisible() && activePanel == null && menuHost.getChildren().size() > 0
                && menuHost.getChildren().get(0) != mainMenu) {
            showMainMenu();
            return;
        }
        activePanel = null;
        panelHost.getChildren().clear();
        panelHost.setVisible(false);
        panelHost.setManaged(false);
        menuHost.getChildren().setAll(createMoreMenu());
        menuHost.setManaged(true);
        menuHost.setVisible(true);
        resizeShell(MENU_WIDTH, MENU_HEIGHT);
    }

    private void openPanel(Panel panel) {
        closeMenu();
        try {
            Parent view = cachedPanels.computeIfAbsent(panel, this::loadPanel);
            panelHost.getChildren().setAll(view);
            panelHost.setManaged(true);
            panelHost.setVisible(true);
            activePanel = panel;
            // Panel windows must fit the panel above the mascot dock, not just the panel.
            switch (panel) {
                case STUDY -> resizeShell(STUDY_WIDTH, STUDY_HEIGHT);
                case SETTINGS -> resizeShell(PANEL_WIDTH, SETTINGS_HEIGHT);
                case CHAT -> resizeShell(PANEL_WIDTH, PANEL_HEIGHT);
            }
        } catch (PanelLoadException e) {
            throw new IllegalStateException("Could not open " + panel.name().toLowerCase() + " panel", e.getCause());
        }
    }

    private Parent loadPanel(Panel panel) {
        try {
            return switch (panel) {
                case CHAT -> loadChatPanel();
                case STUDY -> loadStudyPanel();
                case SETTINGS -> loadSettingsPanel();
            };
        } catch (IOException e) {
            throw new PanelLoadException(e);
        }
    }

    private Parent loadChatPanel() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
        Parent view = loader.load();
        chatController = loader.getController();
        chatController.attach(services.mockChatResponder(), services.threadManager().executor(),
                mascotStateService, this::closePanel, this::collapseToMascot);
        return view;
    }

    private Parent loadStudyPanel() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/study.fxml"));
        Parent view = loader.load();
        studyController = loader.getController();
        studyController.attach(mascotStateService, this::closePanel, this::collapseToMascot);
        return view;
    }

    private Parent loadSettingsPanel() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
        Parent view = loader.load();
        settingsController = loader.getController();
        settingsController.attach(services.config().appVersion(), stage.isAlwaysOnTop(),
                stage::setAlwaysOnTop, this::closePanel, this::collapseToMascot);
        return view;
    }

    private VBox createMoreMenu() {
        VBox card = new VBox(7);
        card.getStyleClass().add("more-menu");
        card.setAlignment(Pos.CENTER);
        card.getChildren().add(new Label("More"));
        card.getChildren().add(menuItem("Settings", () -> openPanel(Panel.SETTINGS)));
        card.getChildren().add(menuItem("Chat history", () -> {
            openPanel(Panel.CHAT);
            chatController.showHistory();
        }));
        card.getChildren().add(menuItem("Help / About", this::showAbout));
        card.getChildren().add(menuItem("Collapse to mascot", this::collapseToMascot));
        card.getChildren().add(menuItem("Quit Puppy", this::quit));
        return card;
    }

    private Button menuItem(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void showAbout() {
        VBox about = new VBox(10);
        about.getStyleClass().addAll("panel-card", "more-menu");
        about.setAlignment(Pos.CENTER);
        about.getChildren().add(new Label("Puppy AI"));
        about.getChildren().add(new Label("A little desktop companion for chat and focused study."));
        about.getChildren().add(new Label("Chat replies are a demo; AI and web research arrive later."));
        about.getChildren().add(menuItem("Back to menu", this::showMainMenu));
        menuHost.getChildren().setAll(about);
        resizeShell(MENU_WIDTH, MENU_HEIGHT);
    }

    private void showMainMenu() {
        menuHost.getChildren().setAll(mainMenu);
        menuHost.setVisible(true);
        menuHost.setManaged(true);
        panelHost.getChildren().clear();
        panelHost.setVisible(false);
        panelHost.setManaged(false);
        activePanel = null;
        resizeShell(MENU_WIDTH, MENU_HEIGHT);
    }

    private void closePanel() {
        collapseToMascot();
    }

    private void collapseToMascot() {
        activePanel = null;
        panelHost.getChildren().clear();
        panelHost.setVisible(false);
        panelHost.setManaged(false);
        menuHost.getChildren().setAll(mainMenu);
        menuHost.setVisible(false);
        menuHost.setManaged(false);
        resizeShell(COMPACT_WIDTH, COMPACT_HEIGHT);
        restoreMascotState();
    }

    private void closeMenu() {
        menuHost.getChildren().setAll(mainMenu);
        menuHost.setVisible(false);
        menuHost.setManaged(false);
        resizeShell(COMPACT_WIDTH, COMPACT_HEIGHT);
    }

    private void resizeShell(double width, double height) {
        double anchorRight = stage.getX() + stage.getWidth();
        double anchorBottom = stage.getY() + stage.getHeight();
        shellRoot.setPrefSize(width, height);
        // A non-resizable stage sizes itself back to its scene, so allow resizing
        // for the duration of the programmatic change.
        boolean resizable = stage.isResizable();
        stage.setResizable(true);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setResizable(resizable);
        stage.setX(anchorRight - stage.getWidth());
        stage.setY(anchorBottom - stage.getHeight());
    }

    private void onMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        pressScreenX = event.getScreenX();
        pressScreenY = event.getScreenY();
        pressStageX = stage.getX();
        pressStageY = stage.getY();
        draggedSincePress = false;
        event.consume();
    }

    private void onMouseDragged(MouseEvent event) {
        if (!event.isPrimaryButtonDown()) {
            return;
        }
        double offsetX = event.getScreenX() - pressScreenX;
        double offsetY = event.getScreenY() - pressScreenY;
        if (!draggedSincePress && Math.hypot(offsetX, offsetY) < DRAG_THRESHOLD) {
            return;
        }
        draggedSincePress = true;
        stage.setX(pressStageX + offsetX);
        stage.setY(pressStageY + offsetY);
        event.consume();
    }

    private void onMascotClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2
                && !draggedSincePress) {
            if (activePanel != null) {
                collapseToMascot();
            } else if (menuHost.isVisible()) {
                closeMenu();
            } else {
                showMainMenu();
            }
            event.consume();
        }
    }

    private void onMascotHover(boolean entered) {
        if (activePanel != null) {
            return;
        }
        if (entered) {
            hoverHint.setVisible(true);
            hoverHint.setManaged(true);
            mascotStateService.setState(MascotState.HAPPY);
            hoverReset.playFromStart();
        } else {
            hoverHint.setVisible(false);
            hoverHint.setManaged(false);
            hoverReset.stop();
            restoreMascotState();
        }
    }

    private void restoreMascotState() {
        if (studyController != null && studyController.isTimerRunning()) {
            mascotStateService.setState(MascotState.STUDYING);
        } else {
            mascotStateService.resetToIdle();
        }
    }

    private void dismissMenuOnOutsideClick(MouseEvent event) {
        if (!menuHost.isVisible()) {
            return;
        }
        Object target = event.getTarget();
        if (target instanceof Node node
                && !isWithin(node, menuHost)
                && !isWithin(node, mascotDock)) {
            closeMenu();
        }
    }

    private static boolean isWithin(Node node, Node ancestor) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current == ancestor) {
                return true;
            }
        }
        return false;
    }

    private void onMascotStateChanged(MascotState state) {
        if (Platform.isFxApplicationThread()) {
            animator.play(state);
        } else {
            Platform.runLater(() -> animator.play(state));
        }
    }

    private ContextMenu contextMenu() {
        if (contextMenu != null) {
            return contextMenu;
        }
        contextMenu = new ContextMenu();
        for (MascotState state : MascotState.values()) {
            MenuItem item = new MenuItem("State: " + state.name().toLowerCase());
            item.setOnAction(event -> mascotStateService.setState(state));
            contextMenu.getItems().add(item);
        }
        contextMenu.getItems().add(new SeparatorMenuItem());
        MenuItem quit = new MenuItem("Quit Puppy");
        quit.setOnAction(event -> quit());
        contextMenu.getItems().add(quit);
        return contextMenu;
    }

    private void quit() {
        stage.close();
        Platform.exit();
    }

    /** Stops UI timers and releases the mascot state listener during shutdown. */
    public void close() {
        if (hoverReset != null) {
            hoverReset.stop();
        }
        if (studyController != null) {
            studyController.close();
        }
        if (chatController != null) {
            chatController.close();
        }
        if (mascotStateService != null && stateListener != null) {
            mascotStateService.removeListener(stateListener);
        }
        if (animator != null) {
            animator.stop();
        }
    }

    private enum Panel {
        CHAT,
        STUDY,
        SETTINGS
    }

    private static final class PanelLoadException extends RuntimeException {

        private PanelLoadException(IOException cause) {
            super(cause);
        }
    }
}
