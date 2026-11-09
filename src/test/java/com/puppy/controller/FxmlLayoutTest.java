package com.puppy.controller;

import com.puppy.config.AppConfig;
import com.puppy.config.ServiceRegistry;
import com.puppy.enums.MascotState;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxmlLayoutTest {

    @Test
    void allUiLayoutsLoadOnTheJavaFxThread() throws Exception {
        CountDownLatch loaded = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.startup(() -> Platform.runLater(() -> {
            Stage stage = new Stage();
            ServiceRegistry services = new ServiceRegistry(AppConfig.load());
            MascotController controller = null;
            try {
                for (String resource : List.of(
                        "/fxml/shell.fxml",
                        "/fxml/chat.fxml",
                        "/fxml/study.fxml",
                        "/fxml/settings.fxml")) {
                    new FXMLLoader(getClass().getResource(resource)).load();
                }
                FXMLLoader shellLoader = new FXMLLoader(getClass().getResource("/fxml/shell.fxml"));
                Parent shell = shellLoader.load();
                controller = shellLoader.getController();
                stage.setScene(new Scene(shell, 240, 180));
                stage.getScene().getStylesheets().add(
                        getClass().getResource("/css/puppy.css").toExternalForm());
                stage.setResizable(false);
                controller.attach(stage, services);
                stage.setX(300);
                stage.setY(300);
                stage.show();

                StackPane mascotDock = (StackPane) shell.lookup(".mascot-dock");
                double initialX = stage.getX();
                double initialY = stage.getY();
                Event.fireEvent(mascotDock, primaryPress(10, 10));
                Event.fireEvent(mascotDock, drag(25, 25));
                assertTrue(Math.abs(stage.getX() - initialX - 15) < 1
                                && Math.abs(stage.getY() - initialY - 15) < 1,
                        "Dragging the mascot should move the window");
                Event.fireEvent(mascotDock, doubleClick());
                assertFalse(shell.lookup(".menu-host").isVisible(),
                        "Dragging must not trigger the double-click menu");

                double expectedRight = stage.getX() + stage.getWidth();
                double expectedBottom = stage.getY() + stage.getHeight();
                Event.fireEvent(mascotDock, primaryPress(30, 30));
                Event.fireEvent(mascotDock, doubleClick());
                assertTrue(shell.lookup(".menu-host").isVisible(),
                        "Double-click should open the main menu");
                Event.fireEvent(shell, new KeyEvent(KeyEvent.KEY_PRESSED, "", "",
                        KeyCode.ESCAPE, false, false, false, false));
                assertFalse(shell.lookup(".menu-host").isVisible(),
                        "Escape should dismiss the menu");
                Event.fireEvent(mascotDock, doubleClick());
                double anchoredRight = stage.getX() + stage.getWidth();
                double anchoredBottom = stage.getY() + stage.getHeight();
                assertTrue(stage.getWidth() > 240, "The menu should expand the shell");
                double menuWidth = stage.getWidth();
                double menuHeight = stage.getHeight();
                assertTrue(Math.abs(anchoredRight - expectedRight) < 1,
                        "The expanded shell should preserve its right edge");
                assertTrue(Math.abs(anchoredBottom - expectedBottom) < 1,
                        "The expanded shell should preserve its bottom edge");
                HBox menu = (HBox) shell.lookup(".main-menu");

                ((Button) menu.getChildren().get(0)).fire();
                assertTrue(shell.lookup(".panel-host").isVisible(),
                        "Study should open in the panel host");
                assertEquals(menuWidth, stage.getWidth(),
                        "Study should use the same shell width as the menu");
                assertEquals(menuHeight, stage.getHeight(),
                        "Study should use the same shell height as the menu");
                assertEquals("Study Mode",
                        ((Label) shell.lookup(".study-header .panel-title")).getText(),
                        "The Study panel title should identify Study Mode");
                assertNull(shell.lookup(".study-footer"),
                        "The separate Study footer should be removed");
                Button studyToggle = (Button) shell.lookup("#startPauseButton");
                studyToggle.fire();
                assertTrue("Pause".equals(studyToggle.getText()),
                        "Starting a session should switch the button to Pause");
                assertTrue(services.mascotStateService().getState() == MascotState.STUDYING,
                        "Starting a session should animate the mascot as studying");
                studyToggle.fire();
                assertTrue("Resume".equals(studyToggle.getText()),
                        "Pausing a session should offer Resume");
                closeActivePanel(shell, 0);

                Event.fireEvent(mascotDock, doubleClick());
                ((Button) menu.getChildren().get(1)).fire();
                assertTrue(shell.lookup(".panel-host").isVisible(),
                        "Chat should open in the panel host");
                shell.applyCss();
                shell.layout();
                VBox messages = (VBox) shell.lookup("#messagesBox");
                TextField messageInput = (TextField) shell.lookup("#messageInput");
                int messageCount = messages.getChildren().size();
                messageInput.setText("   ");
                ((Button) shell.lookup("#sendButton")).fire();
                assertEquals(messageCount, messages.getChildren().size(),
                        "Blank chat input must not add a message");
                closeActivePanel(shell, 1);

                Event.fireEvent(mascotDock, doubleClick());
                ((Button) menu.getChildren().get(2)).fire();
                VBox moreMenu = (VBox) ((StackPane) shell.lookup(".menu-host")).getChildren().get(0);
                ((Button) moreMenu.getChildren().get(1)).fire();
                assertTrue(shell.lookup(".panel-host").isVisible(),
                        "Settings should open from More");
                CheckBox alwaysOnTop = (CheckBox) shell.lookup("#alwaysOnTopCheck");
                alwaysOnTop.fire();
                assertTrue(stage.isAlwaysOnTop(),
                        "The in-session always-on-top setting should update the stage");
                closeActivePanel(shell, 1);

                Event.fireEvent(mascotDock, doubleClick());
                ((Button) menu.getChildren().get(2)).fire();
                moreMenu = (VBox) ((StackPane) shell.lookup(".menu-host")).getChildren().get(0);
                ((Button) moreMenu.getChildren().get(2)).fire();
                assertTrue(shell.lookup("#historyPane").isVisible(),
                        "More should open session chat history");
                closeActivePanel(shell, 1);
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                if (controller != null) {
                    controller.close();
                }
                stage.close();
                services.close();
                loaded.countDown();
            }
        }));

        assertTrue(loaded.await(15, TimeUnit.SECONDS), "JavaFX did not load the FXML layouts in time");
        assertNull(failure.get(), "An FXML layout could not be loaded");
        Platform.exit();
    }

    private static MouseEvent doubleClick() {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED,
                10, 10, 10, 10, MouseButton.PRIMARY, 2,
                false, false, false, false,
                false, false, false, false, false, true, null);
    }

    private static MouseEvent primaryPress(double x, double y) {
        return new MouseEvent(MouseEvent.MOUSE_PRESSED,
                x, y, x, y, MouseButton.PRIMARY, 1,
                false, false, false, false,
                true, false, false, false, false, true, null);
    }

    private static MouseEvent drag(double x, double y) {
        return new MouseEvent(MouseEvent.MOUSE_DRAGGED,
                x, y, x, y, MouseButton.NONE, 1,
                false, false, false, false,
                true, false, false, false, false, false, null);
    }

    private static void closeActivePanel(Parent shell, int buttonIndex) {
        StackPane panelHost = (StackPane) shell.lookup(".panel-host");
        Parent panel = (Parent) panelHost.getChildren().get(0);
        List<Button> windowButtons = panel.lookupAll(".window-button").stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .toList();
        windowButtons.get(buttonIndex).fire();
        assertFalse(panelHost.isVisible(), "Closing a panel should return to the mascot");
    }
}
