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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.controller.R;
import com.example.controller.ui.MainActivity;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.Status;

import java.util.Locale;

/**
 * <h3>CastRemoteDisplayActivity</h3>
 * <p>
 * This code shows how to create an activity that renders some content on a
 * Cast device using a {@link com.google.android.gms.cast.CastPresentation}.
 * </p>
 * <p>
 * The activity uses the {@link MediaRouter} API to select a cast route
 * using a menu item.
 * When a presentation display is available, we stop
 * showing content in the main activity and instead start a {@link CastRemoteDisplayLocalService}
 * that will create a {@link com.google.android.gms.cast.CastPresentation} to render content on the
 * cast remote display. When the cast remote display is removed, we revert to showing content in
 * the main activity. We also write information about displays and display-related events
 * to the Android log which you can read using <code>adb logcat</code>.
 * </p>
 */
public class CastRemoteDisplayActivity extends AppCompatActivity {

    private int experiment;

    public static final String EXTRA_EXPERIMENT = "experiment";

    private final String TAG = "CastRDisplayActivity";
    //TODO pass intent to this activity to show different callibirate screen

    // Second screen
    private Toolbar mToolbar;

    // MediaRouter
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;

    private CastDevice mCastDevice;
    private ListView listView;

    /**
     * Initialization of the Activity after it is first created. Must at least
     * call {@link android.app.Activity#setContentView setContentView()} to
     * describe what is to be displayed in the screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the hardware buttons to control the music
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.acitivity_cast_remote_display);
        setFullScreen();
        setupActionBar();

        Bundle extras = getIntent().getExtras();
        listView = (ListView) findViewById(R.id.list_view);
        final Button button = (Button) findViewById(R.id.start_button);
        final ImageView imageView = (ImageView) findViewById(R.id.image_view);

        String[] steps = null;
        int drawableResId = R.drawable.hold_up;

        if (extras != null) {
            experiment = extras.getInt(EXTRA_EXPERIMENT);
        }

        if (experiment == 0 && isRemoteDisplaying()) {
            experiment = getCurrentService().getCurrentPresentationMode();
        }

        switch (experiment) {
            case CastServiceController.MODE_3D_VIEWER:
                button.setText(R.string.start);
                steps = getResources().getStringArray(R.array.steps_3d_viewer);
                break;
            case CastServiceController.MODE_MATCH_GAME:
                steps = getResources().getStringArray(R.array.steps_match);
                break;
            case CastServiceController.MODE_LASER_GAME:
                drawableResId = R.drawable.hold_remotes;
                steps = getResources().getStringArray(R.array.steps_laser);
                break;
        }

        listView.setAdapter(new StepsAdapter(this, steps));
        Glide.with(this)
                .load(drawableResId)
                .placeholder(R.drawable.hero_placeholder)
                .fitCenter()
                .crossFade().into(imageView);


        // Local UI
        final View overlay = findViewById(R.id.overlay);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change the remote display animation color when the button is clicked
                CastServiceController presentationService = getCurrentService();
                if (presentationService != null) {
                    presentationService.startPresentation();
                }
            }
        });

        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change the remote display animation color when the button is clicked
                CastServiceController presentationService = getCurrentService();
                if (presentationService != null) {
                    presentationService.onSecondaryButton();
                }
            }
        });


        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getString(R.string.app_id)))
                .build();


        if (isRemoteDisplaying()) {
            // The Activity has been recreated and we have an active remote display session,
            // so we need to set the selected device instance
            CastDevice castDevice = CastDevice
                    .getFromBundle(mMediaRouter.getSelectedRoute().getExtras());
            mCastDevice = castDevice;
        } else {
            if (extras != null) {
                mCastDevice = extras.getParcelable(MainActivity.INTENT_EXTRA_CAST_DEVICE);
            }
        }

    }

    private CastServiceController getCurrentService() {
        return ((CastServiceController) CastRemoteDisplayLocalService.getInstance());
    }

    private class StepsAdapter extends BaseAdapter {
        private String[] array;
        private Context ctx;

        public StepsAdapter(Context ctx, String[] steps) {
            this.ctx = ctx;
            this.array = steps;
        }

        @Override
        public int getCount() {
            return array.length;
        }

        @Override
        public Object getItem(int position) {
            return array[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View newView;

            if (convertView == null) {
                newView = View.inflate(ctx, R.layout.item_step, null);
            } else {
                newView = convertView;
            }

            String txt = (String) getItem(position);

            ((TextView) newView.findViewById(R.id.step_num)).setText(String.format(Locale.US, "%d", position + 1));
            ((TextView) newView.findViewById(R.id.step_text)).setText(txt);

            return newView;
        }

    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
    }

    private void setFullScreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * Create the toolbar menu with the cast button.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_cast, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        // Return true to show the menu.
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        if (!isRemoteDisplaying()) {
            if (mCastDevice != null) {
                startCastService(mCastDevice);
            }
        } else {
            createPresentation(getCurrentService());
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (isRemoteDisplaying()
                && getCurrentService().getCurrentPresentationMode()
                == CastServiceController.MODE_LASER_GAME) { // game can't be played with screen of
            getCurrentService().pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRemoteDisplaying()
                && getCurrentService().getCurrentPresentationMode()
                == CastServiceController.MODE_MATCH_GAME) {
            getCurrentService().pause();
        }
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    private boolean isRemoteDisplaying() {
        return CastRemoteDisplayLocalService.getInstance() != null;
    }


    private void initError() {
        Toast toast = Toast.makeText(
                getApplicationContext(), R.string.init_error, Toast.LENGTH_SHORT);
        mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
        toast.show();
    }

    /**
     * Utility method to identify if the route information corresponds to the currently
     * selected device.
     *
     * @param info The route information
     * @return Whether the route information corresponds to the currently selected device.
     */
    private boolean isCurrentDevice(RouteInfo info) {
        if (mCastDevice == null) {
            // No device selected
            return false;
        }
        CastDevice device = CastDevice.getFromBundle(info.getExtras());
        if (!device.getDeviceId().equals(mCastDevice.getDeviceId())) {
            // The callback is for a different device
            return false;
        }
        return true;
    }

    private final MediaRouter.Callback mMediaRouterCallback =
            new MediaRouter.Callback() {
                @Override
                public void onRouteSelected(MediaRouter router, RouteInfo info) {
                    // Should not happen since this activity will be closed if there
                    // is no selected route
                }

                @Override
                public void onRouteUnselected(MediaRouter router, RouteInfo info) {
                    Log.d(TAG, "called onRouteUnselected");
                    if (isRemoteDisplaying()) {
                        CastRemoteDisplayLocalService.stopService();
                    }
                    mCastDevice = null;
                    CastRemoteDisplayActivity.this.finish();
                }
            };

    private void startCastService(CastDevice castDevice) {
        Intent intent = new Intent(CastRemoteDisplayActivity.this,
                CastRemoteDisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                CastRemoteDisplayActivity.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        CastRemoteDisplayLocalService.NotificationSettings settings =
                new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                        .setNotificationPendingIntent(notificationPendingIntent).build();

        CastRemoteDisplayLocalService.Callbacks callbacks = new CastRemoteDisplayLocalService.Callbacks() {
            @Override
            public void onServiceCreated(
                    CastRemoteDisplayLocalService service) {
                //TODO start a certain presentation here

                Log.d(TAG, "onServiceCreated");
            }

            @Override
            public void onRemoteDisplaySessionStarted(
                    CastRemoteDisplayLocalService service) {
                createPresentation((CastServiceController) service);
                Log.d(TAG, "onServiceStarted");
            }

            @Override
            public void onRemoteDisplaySessionError(Status errorReason) {
                int code = errorReason.getStatusCode();
                Log.d(TAG, "onServiceError: " + errorReason.getStatusCode());
                initError();

                mCastDevice = null;
                CastRemoteDisplayActivity.this.finish();
            }
        };

        CastRemoteDisplayLocalService.startService(CastRemoteDisplayActivity.this,
                RemoteDisplayService.class, getString(R.string.app_id),
                castDevice, settings, callbacks);
    }

    private void createPresentation(CastServiceController service) {
        if (service != null) {
            service.createPresentation(experiment);
        }
    }

}
