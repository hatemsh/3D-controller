package com.example.controller.ui;

import android.animation.ArgbEvaluator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.controller.R;
import com.example.controller.cast.CastRemoteDisplayActivity;
import com.example.controller.cast.CastServiceController;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private int[] colors = new int[3];
    private ArgbEvaluator argbEvaluator;

    public static final String INTENT_EXTRA_CAST_DEVICE = "CastDevice";
    private static final String TAG = "MainActivity";

    public static final String FIRST_TIME_USER_PREF = "first_time_user";

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouteButton mMediaRouteButton;
    private int mRouteCount = 0;
    private TabLayout tabLayout;
    private int[] heroDrawablesRes;
    private ImageView mHeroImageView;
    private boolean connectedToCast;
    private CastDevice mCastDevice;
    private boolean discoveredDevice;
    private MediaRouteActionProvider mMediaRouteActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkGooglePlayServices();

        setContentView(R.layout.activity_ui);

        argbEvaluator = new ArgbEvaluator();
        colors[0] = ContextCompat.getColor(this, R.color.page1);
        colors[1] = ContextCompat.getColor(this, R.color.page2Primary);
        colors[2] = ContextCompat.getColor(this, R.color.page3);

        heroDrawablesRes = new int[]{R.drawable.rotation_hero_cropped, R.drawable.match_hero_cropped, R.drawable.laser_hero_cropped};
        mHeroImageView = (ImageView) findViewById(R.id.hero_image);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
//
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

//        tabLayout.getTabAt(0).setIcon(R.drawable.ic_3d_rotation_white_24dp);
//        tabLayout.getTabAt(1).setIcon(R.drawable.ic_screen_rotation_white_24dp);
//        tabLayout.getTabAt(2).setIcon(R.drawable.ic_laser_phone_white_24dp);

        Glide.with(MainActivity.this)
                .load(heroDrawablesRes[0])
                .fitCenter()
                .crossFade().into(mHeroImageView);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Glide.with(MainActivity.this)
                        .load(heroDrawablesRes[position])
                        .placeholder(R.drawable.hero_placeholder)
                        .fitCenter()
                        .crossFade().into(mHeroImageView);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getString(R.string.app_id)))
                .build();
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstTimeUser = prefs.getBoolean(FIRST_TIME_USER_PREF, true);
        if (firstTimeUser) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(FIRST_TIME_USER_PREF, false);
            editor.commit();
            showAboutDialog();
        }

    }

    private void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        mMediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mMediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        // Return true to show the menu.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private int sectionNumber;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = inflater.inflate(R.layout.fragment_demo_info, container, false);
            TextView descriptionTextView = (TextView) rootView.findViewById(R.id.description);
            Button button = (Button) rootView.findViewById(R.id.start_experiment_button);
            int descriptionRes;
            int buttonRes;
            switch (sectionNumber) {
                case 1:
                    descriptionRes = R.string.match_description;
                    buttonRes = R.string.start_match_button;
                    break;
                case 2:
                    descriptionRes = R.string.laser_description;
                    buttonRes = R.string.start_laser_button;
                    break;
                default:
                    descriptionRes = R.string.rotate_description;
                    buttonRes = R.string.start_3d_button;
                    break;
            }

            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
            descriptionTextView.setTypeface(typeface);

            descriptionTextView.setText(descriptionRes);
            button.setText(buttonRes);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).onStartButtonClicked();
                }
            });
            return rootView;
        }
    }


    private void onStartButtonClicked() {
        mMediaRouteButton = mMediaRouteActionProvider.getMediaRouteButton();
        if (mMediaRouteButton != null) {
            mCastDevice = getCastDevice(mMediaRouter.getSelectedRoute());
            if (mCastDevice != null) {
                Log.d(TAG, "called start cast activity");
                startCastActivity();
            } else {
                mMediaRouteButton.performClick();
            }
        } else {
            //TODO ask to retry
            Toast.makeText(MainActivity.this, "Unable to detect Google Cast device", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("About");
        builder.setMessage(getString(R.string.about_content));
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private CastDevice getCastDevice(MediaRouter.RouteInfo info) {
        return CastDevice.getFromBundle(info.getExtras());
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "3D VIEWER";
                case 1:
                    return "DETECTOR";
                case 2:
                    return "POINTER";
            }
            return null;
        }
    }


    private final MediaRouter.Callback mMediaRouterCallback =
            new MediaRouter.Callback() {
                @Override
                public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
                    if (++mRouteCount == 1) {
                        // Show the button when a device is discovered.
                        mMediaRouteButton = mMediaRouteActionProvider.getMediaRouteButton();
                        Log.d(TAG, "mMediaRouteButton: " + mMediaRouteButton);
                    }
                }

                @Override
                public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
                    if (--mRouteCount == 0) {
                        // Hide the button if there are no devices discovered.
                        mMediaRouteButton = null;
                    }
                }

                @Override
                public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRouteSelected");
                    mCastDevice = getCastDevice(info);
                    startCastActivity();
                }

                @Override
                public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
                    mCastDevice = null;
                }
            };

    /*
    * when clicking button if no case device found show snackbar to try and find
    * if cast device found but not connected connect to cast then start experiment
    * if cast device found and connected start experiment*/

    private void startCastActivity() {
        if (mCastDevice != null) {
            Intent intent = null;
            switch (mViewPager.getCurrentItem()) {
                case 0:
                    intent = new Intent(MainActivity.this, CastRemoteDisplayActivity.class);
                    intent.putExtra(CastRemoteDisplayActivity.EXTRA_EXPERIMENT,
                            CastServiceController.MODE_3D_VIEWER);
                    break;
                case 1:
                    intent = new Intent(MainActivity.this, CastRemoteDisplayActivity.class);
                    intent.putExtra(CastRemoteDisplayActivity.EXTRA_EXPERIMENT,
                            CastServiceController.MODE_MATCH_GAME);
                    break;
                case 2:
                    intent = new Intent(MainActivity.this, CastRemoteDisplayActivity.class);
                    intent.putExtra(CastRemoteDisplayActivity.EXTRA_EXPERIMENT,
                            CastServiceController.MODE_LASER_GAME);
                    break;
            }

            //TODO start game of current item activity
            if (intent != null) {
                intent.putExtra(INTENT_EXTRA_CAST_DEVICE, mCastDevice);
                startActivity(intent);
            }
        }
    }

    /**
     * A utility method to validate that the appropriate version of the Google Play Services is
     * available on the device. If not, it will open a dialog to address the issue. The dialog
     * displays a localized message about the error and upon user confirmation (by tapping on
     * dialog) will direct them to the Play Store if Google Play services is out of date or
     * missing, or to system settings if Google Play services is disabled on the device.
     */
    private boolean checkGooglePlayServices() {
        int googlePlayServicesCheck = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (googlePlayServicesCheck == ConnectionResult.SUCCESS) {
            return true;
        }
        GoogleApiAvailability.getInstance()
                .showErrorDialogFragment(this, googlePlayServicesCheck, 0, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        return false;
    }

}
