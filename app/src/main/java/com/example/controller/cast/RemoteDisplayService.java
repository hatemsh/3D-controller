/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.controller.cast;

import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.cast.CastPresentation;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;

/**
 * Service to keep the remote display running even when the app goes into the background
 */
public class RemoteDisplayService extends CastRemoteDisplayLocalService implements CastServiceController {

    private static final String TAG = "PresentationService";

    // First screen
    private CastPresentation mPresentation;
    private int currentPresentationMode = 0;

    @Override
    public void onCreatePresentation(Display display) {
        //createPresentation(display);
    }

    @Override
    public void onDismissPresentation() {
        dismissPresentation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void dismissPresentation() {
        if (mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    private void createPresentation(Display display, int mode) {
        if (mode == currentPresentationMode) {
            resume();
            return;
        }
        currentPresentationMode = mode;

        dismissPresentation();
        switch (mode) {
            case CastServiceController.MODE_3D_VIEWER:
                mPresentation = new ModelViewerPresentation(this, display);
                break;
            case CastServiceController.MODE_MATCH_GAME:
                mPresentation = new MatchPresentation(this, display);
                break;
            case CastServiceController.MODE_LASER_GAME:
                mPresentation = new LaserPresentation(this, display);
                break;
        }

        try {
            mPresentation.show();
        } catch (WindowManager.InvalidDisplayException ex) {
            Log.e(TAG, "Unable to show presentation, display was removed.", ex);
            dismissPresentation();
        }
    }

    @Override
    public void createPresentation(int mode) {
        createPresentation(getDisplay(), mode);
    }

    @Override
    public int getCurrentPresentationMode() {
        return currentPresentationMode;
    }

    @Override
    public void startPresentation() {
        if (mPresentation != null) {
            ((CastPresentationController) mPresentation).onButtonClick();
        }
    }

    @Override
    public void onSecondaryButton() {
        if (mPresentation != null) {
            ((CastPresentationController) mPresentation).onSecondButtonClick();
        }
    }

    @Override
    public void pause() {
        if (mPresentation != null){
            ((CastPresentationController) mPresentation).pause();
        }
    }

    @Override
    public void resume() {
        if (mPresentation != null){
            ((CastPresentationController) mPresentation).resume();
        }
    }
}
