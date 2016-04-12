package com.example.controller.cast;

/**
 * Created by Hatem on 11-Apr-16.
 */
public interface CastServiceController {
    int MODE_3D_VIEWER = 1;
    int MODE_MATCH_GAME = 2;
    int MODE_LASER_GAME = 3;

    void createPresentation(int mode);

    int getCurrentPresentationMode();

    void startPresentation();

    void onSecondaryButton();

    void pause();

    void resume();
}
