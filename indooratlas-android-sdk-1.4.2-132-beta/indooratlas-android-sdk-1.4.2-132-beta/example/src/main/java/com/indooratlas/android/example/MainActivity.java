package com.indooratlas.android.example;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.indooratlas.android.CalibrationState;
import com.indooratlas.android.IndoorAtlas;
import com.indooratlas.android.IndoorAtlasException;
import com.indooratlas.android.IndoorAtlasFactory;
import com.indooratlas.android.IndoorAtlasListener;
import com.indooratlas.android.ServiceState;

import java.util.ArrayList;

/**
 * <p>Activity to demonstrate basic use of IndoorAtlas SDK. If there are no public maps around your
 * location, you can create a map of your own and upload it to IndoorAtlas servers. Read more on how
 * to do this from: http://developer.indooratlas.com.</p>
 * <p/>
 * <p>To run this demo, you will also need your applications API key/secret and identifiers for the
 * floor plan from http://developer.indooratlas.com.</p>
 * <p/>
 */
public class MainActivity extends Activity implements IndoorAtlasListener {

    private static final String TAG = "MainActivity";

    private ListView mLogView;
    private LogAdapter mLogAdapter;

    private IndoorAtlas mIndoorAtlas;
    private boolean mIsPositioning;
    private StringBuilder mSharedBuilder = new StringBuilder();

    private String mApiKey = "api-key-from-developer-website-here";
    private String mApiSecret = "api-secret-from-developer-website-here";

    private String mVenueId = "venue-id-from-developer-website-here";
    private String mFloorId = "floor-id-from-developer-website-here";
    private String mFloorPlanId = "floor-plan-id-from-developer-website-here";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogView = (ListView) findViewById(R.id.list);
        mLogAdapter = new LogAdapter(this);
        mLogView.setAdapter(mLogAdapter);

        initIndoorAtlas();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tearDown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_log:
                mLogAdapter.clear();
                return true;
            case R.id.action_toggle_positioning:
                togglePositioning();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void tearDown() {
        if (mIndoorAtlas != null) {
            mIndoorAtlas.tearDown();
        }
    }


    private void stopPositioning() {
        mIsPositioning = false;
        if (mIndoorAtlas != null) {
            log("Stop positioning");
            mIndoorAtlas.stopPositioning();
        }
    }

    private void startPositioning() {
        if (mIndoorAtlas != null) {
            log(String.format("startPositioning, venueId: %s, floorId: %s, floorPlanId: %s",
                    mVenueId,
                    mFloorId,
                    mFloorPlanId));
            try {
                mIndoorAtlas.startPositioning(mVenueId, mFloorId, mFloorPlanId);
                mIsPositioning = true;
            } catch (IndoorAtlasException e) {
                log("startPositioning failed: " + e);
            }
        } else {
            log("calibration not ready, cannot start positioning");
        }
    }

    private void togglePositioning() {
        if (mIsPositioning) {
            stopPositioning();
        } else {
            startPositioning();
        }
    }

    private void initIndoorAtlas() {

        try {

            log("Connecting with IndoorAtlas, apiKey: " + mApiKey);

            // obtain instance to positioning service, note that calibrating might begin instantly
            mIndoorAtlas = IndoorAtlasFactory.createIndoorAtlas(
                    getApplicationContext(),
                    this, // IndoorAtlasListener
                    mApiKey,
                    mApiSecret);

            log("IndoorAtlas instance created");
            togglePositioning();

        } catch (IndoorAtlasException ex) {
            Log.e("IndoorAtlas", "init failed", ex);
            log("init IndoorAtlas failed, " + ex.toString());
        }

    }


    private void log(final String msg) {
        Log.d(TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogAdapter.add(msg);
                mLogAdapter.notifyDataSetChanged();
            }
        });
    }

    /* IndoorAtlasListener interface */

    /**
     * This is where you will handle location updates.
     */
    public void onServiceUpdate(ServiceState state) {

        mSharedBuilder.setLength(0);
        mSharedBuilder.append("Location: ")
                .append("\n\troundtrip : ").append(state.getRoundtrip()).append("ms")
                .append("\n\tlat : ").append(state.getGeoPoint().getLatitude())
                .append("\n\tlon : ").append(state.getGeoPoint().getLongitude())
                .append("\n\tX [meter] : ").append(state.getMetricPoint().getX())
                .append("\n\tY [meter] : ").append(state.getMetricPoint().getY())
                .append("\n\tI [pixel] : ").append(state.getImagePoint().getI())
                .append("\n\tJ [pixel] : ").append(state.getImagePoint().getJ())
                .append("\n\theading : ").append(state.getHeadingDegrees())
                .append("\n\tuncertainty: ").append(state.getUncertainty());

        log(mSharedBuilder.toString());
    }


    @Override
    public void onServiceFailure(int errorCode, String reason) {
        log("onServiceFailure: reason : " + reason);
    }

    @Override
    public void onServiceInitializing() {
        log("onServiceInitializing");
    }

    @Override
    public void onServiceInitialized() {
        log("onServiceInitialized");
    }

    @Override
    public void onInitializationFailed(final String reason) {
        log("onInitializationFailed: " + reason);
    }

    @Override
    public void onServiceStopped() {
        log("onServiceStopped");
    }

    @Override
    public void onCalibrationStatus(CalibrationState calibrationState) {
        log("onCalibrationStatus, percentage: " + calibrationState.getPercentage());
    }

    /**
     * Notification that calibration has reached level of quality that provides best possible
     * positioning accuracy.
     */
    @Override
    public void onCalibrationReady() {
        log("onCalibrationReady");
    }

    @Override
    public void onNetworkChangeComplete(boolean success) {
    }

    /**
     * @deprecated this callback is deprecated as of version 1.4
     */
    @Override
    public void onCalibrationInvalid() {
    }

    /**
     * @deprecated this callback is deprecated as of version 1.4
     */
    @Override
    public void onCalibrationFailed(String reason) {
    }

    static class LogAdapter extends BaseAdapter {

        private ArrayList<String> mLines = new ArrayList<String>();
        private LayoutInflater mInflater;

        public LogAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mLines.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView text = (TextView) convertView;
            if (convertView == null) {
                text = (TextView) mInflater.inflate(android.R.layout.simple_list_item_1, parent,
                        false);
            }
            text.setText(mLines.get(position));
            return text;
        }

        public void add(String line) {
            mLines.add(0, line);
        }

        public void clear() {
            mLines.clear();
            notifyDataSetChanged();
        }
    }

}
