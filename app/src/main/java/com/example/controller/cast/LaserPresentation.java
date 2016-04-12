package com.example.controller.cast;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.example.controller.ui.LaserCanvasView;
import com.example.controller.R;

/**
 * Created by Hatem on 11-Apr-16.
 */
public class LaserPresentation extends GamePresentation {
    public static final String LASER_GAME_HIGH_SCORE_PREF = "laser_game_high_score";
    private LaserCanvasView canvasGame;

    public LaserPresentation(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected String getHighScorePrefKey() {
        return LASER_GAME_HIGH_SCORE_PREF;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_layout_laser_demo);
        initSharedViews();

        gameTip = getResources().getString(R.string.laser_tip);
        canvasGame = (LaserCanvasView) findViewById(R.id.canvas_game);
        matrixCalculator.registerOrientationListener(canvasGame);
    }

    @Override
    public void onRestartGame() {
        canvasGame.generateTargets();
    }


    @Override
    public void onButtonClick() {
        restartGame();
    }

    @Override
    public void onSecondButtonClick() {
        if (isGameRunning()) {
            playSound(SHOOT_SOUND_INDEX);
            if (canvasGame.shoot()) {
                incrementScore();
            }
        }
    }

    @Override
    public void pause() {
        pauseGame();
    }

    @Override
    public void resume() {
        resumeGame();
    }

    @Override
    void onTimerTick() {
        if (canvasGame != null) {
            canvasGame.postInvalidate();
        }
    }

}
