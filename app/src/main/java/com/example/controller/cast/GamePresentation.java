package com.example.controller.cast;

/**
 * Created by Hatem on 11-Apr-16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.controller.R;
import com.example.controller.controller.ControllerSensorManager;
import com.example.controller.controller.MatrixCalculator;
import com.google.android.gms.cast.CastPresentation;

/**
 * The presentation to show on the first screen (the TV).
 * <p>
 * Note that this display may have different metrics from the display on
 * which the main activity is showing so we must be careful to use the
 * presentation's own {@link Context} whenever we load resources.
 * </p>
 */
public abstract class GamePresentation extends CastPresentation implements CastPresentationController {
    private static final String TAG = "FirstScreenPresentation";
    private int mHighScore;
    protected String gameTip;
    private ProgressBar progressBar;
    private TextView mTipTextView;
    protected TextView mGameOverView;
    protected MatrixCalculator matrixCalculator;
    private TextView mTimerTextView;
    private TextView mScoreTextView;
    private TextView mHighScoreTextView;
    private long timeLeft;
    private ControllerSensorManager controllerSensorManager;
    private boolean gameRunning;
    private CountDownTimer timer;
    private int gameDuration;
    protected int mScore;
    private SoundPool soundPool;
    private int[] soundIds;
    private boolean loaded;
    private String gameDuratoinString;

    protected static final int CORRECT_SOUND_INDEX = 0;
    protected static final int SHOOT_SOUND_INDEX = 1;
    protected static final int GAME_END_SOUND_INDEX = 2;


    public GamePresentation(Context context, Display display) {
        super(context, display, R.style.AppTheme_NoActionBar);
    }

    protected boolean isGameRunning() {
        return gameRunning;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the sound

        gameDuration = getResources().getInteger(R.integer.game_duration_millis);
        gameDuratoinString = "" + (gameDuration / 1000);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        soundIds = new int[3];
        soundIds[CORRECT_SOUND_INDEX] = soundPool.load(getContext(), R.raw.correct_sound2,1);
        soundIds[GAME_END_SOUND_INDEX] = soundPool.load(getContext(), R.raw.game_end,1);
        soundIds[SHOOT_SOUND_INDEX] = soundPool.load(getContext(), R.raw.shoot,1);

        Log.d(TAG, "called on start in presentaion");
        matrixCalculator = new MatrixCalculator();
        controllerSensorManager = new ControllerSensorManager(getContext(), matrixCalculator);
        controllerSensorManager.start();
    }

    protected abstract String getHighScorePrefKey();

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        soundPool.release();
        soundPool = null;
        loaded = false;
        controllerSensorManager.stop();
    }

    protected void initSharedViews() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mTipTextView = (TextView) findViewById(R.id.tip_text_view);
        mGameOverView = (TextView) findViewById(R.id.game_over_view);

        mHighScore = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getHighScorePrefKey(), 0);

        ViewGroup highScoreGroup = (ViewGroup) findViewById(R.id.high_score_icon_text);
        ViewGroup timerGroup = (ViewGroup) findViewById(R.id.timer_icon_text);
        ViewGroup scoreGroup = (ViewGroup) findViewById(R.id.score_icon_text);

        mTimerTextView = (TextView) timerGroup.findViewById(R.id.text_view);
        mTimerTextView.setText(gameDuratoinString);

        ImageView timerIcon = (ImageView) timerGroup.findViewById(R.id.image_view);
        timerIcon.setImageResource(R.drawable.ic_timer_24dp);

        mScoreTextView = (TextView) scoreGroup.findViewById(R.id.text_view);
        mScoreTextView.setText("0");

        mHighScoreTextView = (TextView) highScoreGroup.findViewById(R.id.text_view);
        mHighScoreTextView.setText("" + mHighScore);

        ImageView highScoreIcon = (ImageView) highScoreGroup.findViewById(R.id.image_view);
        highScoreIcon.setImageResource(R.drawable.ic_trophy_24dp);

        timeLeft = 0;
    }

    public void restartGame() {
        gameRunning = false;
        if (timer != null) {
            timer.cancel();
        }
        mTipTextView.setVisibility(View.VISIBLE);
        mTimerTextView.setText(gameDuratoinString);
        mScoreTextView.setText("0");
        mTipTextView.setText(gameTip);
        onRestartGame();
        timeLeft = 0;
        playGame();
    }

    public abstract void onRestartGame();


    private void playGame() {

        mGameOverView.setVisibility(View.GONE);
        if (!gameRunning) {
            if (timeLeft == 0) {
                //reset game
                matrixCalculator.offset(); //TODO tell user to hold phone correctly
                timeLeft = gameDuration;
                mScore = -1;
                incrementScore();
            }
            timer = new CountDownTimer(timeLeft, 16) {

                public void onTick(long millisUntilFinished) {
                    progressBar.setProgress((int) (gameDuration - timeLeft));
                    timeLeft = millisUntilFinished;
                    if (timeLeft % 1000 < 20) {
                        mTimerTextView.setText(Long.toString(millisUntilFinished / 1000));
                    }
                    onTimerTick();
                }

                public void onFinish() {
                    progressBar.setProgress(gameDuration);
                    mTimerTextView.setText("0");
                    timeLeft = 0;
                    gameRunning = false;

                    mGameOverView.setText(getResources().getString(R.string.game_over));
                    mGameOverView.setVisibility(View.VISIBLE);
                    playSound(GAME_END_SOUND_INDEX);
                    updateSharedPrefHighScore();
                }
            }.start();

            gameRunning = true;
        }
    }

    private void updateSharedPrefHighScore() {
        if (mScore > mHighScore) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(getHighScorePrefKey(), mScore);
            editor.apply();
            mHighScore = mScore;
            mHighScoreTextView.setText("" + mHighScore);
        }
    }

    abstract void onTimerTick();

    public void incrementScore() {
        //PLAY sound here
        mScore += 1;
        playSound(CORRECT_SOUND_INDEX);
        mScoreTextView.post(new Runnable() {
            @Override
            public void run() {
                if (mScore == 3) {
                    mTipTextView.setVisibility(View.GONE);
                }
                if (mScore > mHighScore) {
                    mHighScoreTextView.setText("" + mScore);
                }
                mScoreTextView.setText("" + mScore);
            }
        });
    }

    protected void pauseGame() {
        if (mGameOverView != null && gameRunning) {
            updateSharedPrefHighScore();
            mGameOverView.setText(R.string.paused);
            mGameOverView.setVisibility(View.VISIBLE);
            timer.cancel();
            gameRunning = false;
        }
    }

    protected void resumeGame() {
        if (!gameRunning && timeLeft != 0) {
            playGame();
        }
    }

    protected void playSound(int soundIndex){
        if (loaded){
            soundPool.play(soundIds[soundIndex], 1, 1, 1, 0, 1f);
        }
    }

}
