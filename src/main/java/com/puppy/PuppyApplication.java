package com.puppy;

import com.puppy.config.AppConfig;
import com.puppy.config.ServiceRegistry;
import com.puppy.controller.MascotController;
import com.puppy.ui.MascotSprites;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;
import java.util.Objects;

/**
 * Entry point for the Puppy AI desktop application.
 *
 * <p>Milestone 1 shows a frameless, transparent, always-on-top window containing
 * the animated mascot, which can be dragged around the desktop.</p>
 */
public final class PuppyApplication extends Application {

    private static final double SCENE_WIDTH = 240;
    private static final double SCENE_HEIGHT = 180;
    private static final double SCREEN_MARGIN = 32;

    private ServiceRegistry services;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        services = new ServiceRegistry(AppConfig.load());
        services.start();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(PuppyApplication.class.getResource("/fxml/mascot.fxml"));
        Parent root = loader.load();
        MascotController controller = loader.getController();

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(Objects.requireNonNull(
                PuppyApplication.class.getResource("/css/puppy.css")).toExternalForm());

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle(services.config().appName());
        stage.setResizable(false);
        stage.setAlwaysOnTop(services.config().alwaysOnTop());
        Image icon = loadIcon();
        if (icon != null) {
            stage.getIcons().add(icon);
        }
        stage.setScene(scene);

        controller.installWindowBehaviour(stage);
        controller.attach(services.mascotStateService());

        positionInScreenCorner(stage);
        stage.show();
    }

    @Override
    public void stop() {
        if (services != null) {
            services.close();
        }
    }

    private void positionInScreenCorner(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMaxX() - SCENE_WIDTH - SCREEN_MARGIN);
        stage.setY(bounds.getMaxY() - SCENE_HEIGHT - SCREEN_MARGIN);
    }

    private Image loadIcon() {
        try (InputStream stream = PuppyApplication.class.getResourceAsStream(MascotSprites.ICON_RESOURCE)) {
            return stream == null ? null : new Image(stream);
        } catch (Exception e) {
            return null;
        }
    }
}
