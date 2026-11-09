package com.puppy.controller;

import com.puppy.enums.MascotState;
import com.puppy.service.MascotStateService;
import com.puppy.ui.MascotAnimator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Presentation controller for the desktop mascot.
 *
 * <p>Owns nothing but UI concerns: it forwards mascot state changes to the
 * animator and turns mouse input into window movement. All application state
 * lives in services handed to {@link #attach(MascotStateService)}.</p>
 */
public class MascotController {

    @FXML
    private StackPane mascotRoot;

    @FXML
    private ImageView mascotView;

    private MascotStateService mascotStateService;
    private MascotAnimator animator;
    private ContextMenu contextMenu;

    private Stage stage;
    private double dragOffsetX;
    private double dragOffsetY;

    @FXML
    public void initialize() {
        animator = new MascotAnimator(mascotView);
    }

    /**
     * Connects the controller to the mascot service and starts rendering the
     * current state. Safe to call on the JavaFX Application Thread only.
     */
    public void attach(MascotStateService mascotStateService) {
        this.mascotStateService = Objects.requireNonNull(mascotStateService, "mascotStateService");
        mascotStateService.addListener(this::onMascotStateChanged);
        onMascotStateChanged(mascotStateService.getState());
    }

    /**
     * Makes the mascot draggable and adds a right-click menu to switch states and
     * to quit, since the window has no title bar.
     */
    public void installWindowBehaviour(Stage stage) {
        this.stage = Objects.requireNonNull(stage, "stage");
        mascotRoot.setOnMousePressed(this::onMousePressed);
        mascotRoot.setOnMouseDragged(this::onMouseDragged);
        mascotRoot.setOnContextMenuRequested(event -> {
            contextMenu().show(mascotRoot, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    private void onMascotStateChanged(MascotState state) {
        if (Platform.isFxApplicationThread()) {
            animator.play(state);
        } else {
            Platform.runLater(() -> animator.play(state));
        }
    }

    private void onMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        dragOffsetX = event.getScreenX() - stage.getX();
        dragOffsetY = event.getScreenY() - stage.getY();
        event.consume();
    }

    private void onMouseDragged(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        stage.setX(event.getScreenX() - dragOffsetX);
        stage.setY(event.getScreenY() - dragOffsetY);
        event.consume();
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
        quit.setOnAction(event -> {
            stage.close();
            Platform.exit();
        });
        contextMenu.getItems().add(quit);
        return contextMenu;
    }
}
