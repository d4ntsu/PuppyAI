package com.puppy.controller;

import com.puppy.enums.MascotState;
import com.puppy.service.MascotStateService;
import com.puppy.service.StudyTimer;
import com.puppy.ui.MascotAnimator;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Objects;

/** JavaFX presentation for the Pomodoro timer service. */
public class StudyController {

    @FXML
    private Label phaseLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label encouragementLabel;

    @FXML
    private Button startPauseButton;

    @FXML
    private ImageView studyMascotView;

    private StudyTimer timer;
    private Timeline ticker;
    private PauseTransition completionReaction;
    private MascotAnimator studyAnimator;
    private MascotStateService mascotStateService;
    private Runnable closeAction;
    private Runnable minimizeAction;

    @FXML
    public void initialize() {
        timer = new StudyTimer();
        studyAnimator = new MascotAnimator(studyMascotView);
        studyAnimator.play(MascotState.IDLE);
        ticker = new Timeline(new KeyFrame(Duration.millis(200), event -> updateTimer()));
        ticker.setCycleCount(Timeline.INDEFINITE);
        completionReaction = new PauseTransition(Duration.seconds(1.5));
        completionReaction.setOnFinished(event -> {
            if (!timer.isRunning()) {
                mascotStateService.resetToIdle();
                studyAnimator.play(MascotState.IDLE);
            }
        });
    }

    public void attach(MascotStateService mascotStateService,
                       Runnable closeAction, Runnable minimizeAction) {
        this.mascotStateService = Objects.requireNonNull(mascotStateService, "mascotStateService");
        this.closeAction = Objects.requireNonNull(closeAction, "closeAction");
        this.minimizeAction = Objects.requireNonNull(minimizeAction, "minimizeAction");
        ticker.play();
        updateTimer();
    }

    @FXML
    private void toggleTimer() {
        if (timer.isRunning()) {
            timer.pause();
            completionReaction.stop();
            startPauseButton.setText("Resume");
            encouragementLabel.setText("Paused. Take a breath.");
            mascotStateService.resetToIdle();
            studyAnimator.play(MascotState.IDLE);
        } else {
            completionReaction.stop();
            timer.start();
            startPauseButton.setText("Pause");
            encouragementLabel.setText("You have got this!");
            mascotStateService.setState(MascotState.STUDYING);
            studyAnimator.play(MascotState.STUDYING);
        }
        updateTimer();
    }

    @FXML
    private void resetTimer() {
        timer.reset();
        completionReaction.stop();
        startPauseButton.setText("Start");
        phaseLabel.setText("FOCUS TIME");
        encouragementLabel.setText("Ready when you are.");
        mascotStateService.resetToIdle();
        studyAnimator.play(MascotState.IDLE);
        updateTimer();
    }

    private void updateTimer() {
        if (timer.isFinished()) {
            StudyTimer.Phase completedPhase = timer.getPhase();
            timer.completePhase();
            startPauseButton.setText("Start");
            mascotStateService.setState(MascotState.HAPPY);
            studyAnimator.play(MascotState.HAPPY);
            completionReaction.playFromStart();
            encouragementLabel.setText(completedPhase == StudyTimer.Phase.FOCUS
                    ? "Focus complete. Take a break when ready."
                    : "Break complete. Ready for another focus session?");
        }
        phaseLabel.setText(timer.getPhase() == StudyTimer.Phase.FOCUS ? "FOCUS TIME" : "BREAK TIME");
        int seconds = timer.getRemainingSeconds();
        timeLabel.setText("%02d:%02d".formatted(seconds / 60, seconds % 60));
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
        if (ticker != null) {
            ticker.stop();
        }
        if (completionReaction != null) {
            completionReaction.stop();
        }
        if (studyAnimator != null) {
            studyAnimator.stop();
        }
    }

    public boolean isTimerRunning() {
        return timer != null && timer.isRunning();
    }
}
