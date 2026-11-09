package com.puppy.controller;

import com.puppy.enums.AIMode;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.Objects;
import java.util.function.Consumer;

/** Settings that have a working in-session effect in the current milestone. */
public class SettingsController {

    @FXML
    private CheckBox alwaysOnTopCheck;

    @FXML
    private ComboBox<AIMode> aiModeCombo;

    @FXML
    private Label versionLabel;

    private Consumer<Boolean> alwaysOnTopChanged;
    private Runnable closeAction;
    private Runnable minimizeAction;

    @FXML
    public void initialize() {
        aiModeCombo.getItems().setAll(AIMode.values());
    }

    public void attach(String appVersion, boolean alwaysOnTop,
                       Consumer<Boolean> alwaysOnTopChanged,
                       Runnable closeAction, Runnable minimizeAction) {
        this.alwaysOnTopChanged = Objects.requireNonNull(alwaysOnTopChanged, "alwaysOnTopChanged");
        this.closeAction = Objects.requireNonNull(closeAction, "closeAction");
        this.minimizeAction = Objects.requireNonNull(minimizeAction, "minimizeAction");
        alwaysOnTopCheck.setSelected(alwaysOnTop);
        versionLabel.setText("Version " + appVersion);
    }

    @FXML
    private void onAlwaysOnTopChanged() {
        alwaysOnTopChanged.accept(alwaysOnTopCheck.isSelected());
    }

    @FXML
    private void onClose() {
        closeAction.run();
    }

    @FXML
    private void onMinimize() {
        minimizeAction.run();
    }
}
