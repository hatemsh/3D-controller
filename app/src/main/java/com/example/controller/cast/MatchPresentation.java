package com.example.controller.cast;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.example.controller.ui.MatchCanvasView;
import com.example.controller.R;
import com.example.controller.controller.MatrixCalculator;

/**
 * Created by Hatem on 11-Apr-16.
 */
public class MatchPresentation extends GamePresentation implements MatrixCalculator.OrientationListener {
    public static final String MATCH_GAME_HIGH_SCORE_PREF = "match_game_high_score";
    private MatchCanvasView canvasGame;

    public MatchPresentation(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected String getHighScorePrefKey() {
        return MATCH_GAME_HIGH_SCORE_PREF;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_layout_match_demo);
        initSharedViews();

        gameTip = getResources().getString(R.string.match_tip);
        canvasGame = (MatchCanvasView) findViewById(R.id.canvas_game);
        matrixCalculator.registerOrientationListener(this);
    }


    @Override
    public void onRestartGame() {
        canvasGame.generateNewOrientation();
        canvasGame.postInvalidate();
    }

    @Override
    void onTimerTick() {

    }

    @Override
    public void onButtonClick() {
        restartGame();
    }

    @Override
    public void onSecondButtonClick() {

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
    public void onOrientationChanged(int newOrientation) {
        if (isGameRunning()) {
            if (canvasGame.onOrientationChanged(newOrientation)) {
                incrementScore();
            }
        }
    }

    @Override
    public void onLaserPointChanged(float[] point) {

    }
}
