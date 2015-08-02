

package com.parse.starter;

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
        import com.indooratlas.android.GeoPoint;
        import com.indooratlas.android.IndoorAtlas;
        import com.indooratlas.android.IndoorAtlasException;
        import com.indooratlas.android.IndoorAtlasFactory;
        import com.indooratlas.android.IndoorAtlasListener;
        import com.indooratlas.android.ServiceState;
        import com.parse.GetCallback;
        import com.parse.ParseException;
        import com.parse.ParseObject;
        import com.parse.ParseQuery;

        import java.util.ArrayList;

public class RequestingCoffeeActivity extends Activity implements IndoorAtlasListener {
    private ListView mLogView;
    private LogAdapter mLogAdapter;

    private IndoorAtlas mIndoorAtlas;
    private boolean mIsPositioning;
    private StringBuilder mSharedBuilder = new StringBuilder();

    private String mApiKey = "4bf151c1-885e-4515-b7a4-5994e111b1d9";
    private String mApiSecret = "dHXsL8h05&)0N5%DeFU56XpVb%!xYrowi7oSr8ipu0dPom9RVSP&3iyNYp!keJajTqJ4GULrQsLDdrA8D(E)pDXzO)jak(6JGUPWfC&Pw039)C(&73TaEskrfFpzWSLy";

    private String mVenueId = "348f0134-4cd7-465a-8bdd-c6fba513abc9";
    private String mFloorId = "c03c18f7-db50-4607-a666-2b57b98c0ea4";
    private String mFloorPlanId = "2580fd13-f01b-48dd-9121-56b73407b2f4";

    private double longitude;
    private double latitude;
    private ParseObject retrieved;
    private ArrayList<GeoPoint> coordList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requesting);
//        mLogAdapter = new LogAdapter(this);
//        mLogView.setAdapter(mLogAdapter);
        sendGeoPointToParse();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_requesting_coffee, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendGeoPointToParse() {
        // obtain instance to positioning service, note that calibrating might begin instantly
        try {
            mIndoorAtlas = IndoorAtlasFactory.createIndoorAtlas(
                    getApplicationContext(),
                    this, // IndoorAtlasListener
                    mApiKey,
                    mApiSecret);
        } catch (IndoorAtlasException e) {
            e.printStackTrace();
        }
        startPositioning();


    }

//    private void log(final String msg) {
//        Log.d("hello", msg);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mLogAdapter.add(msg);
//                mLogAdapter.notifyDataSetChanged();
//            }
//        });
//    }

    private void startPositioning() {
        if (mIndoorAtlas != null) {
            android.util.Log.e("MCA",String.format("startPositioning, venueId: %s, floorId: %s, floorPlanId: %s",
                    mVenueId,
                    mFloorId,
                    mFloorPlanId));
            try {
                mIndoorAtlas.startPositioning(mVenueId, mFloorId, mFloorPlanId);
                android.util.Log.e("Th", "there is no error");

            } catch (IndoorAtlasException e) {
                android.util.Log.e("MCA","startPositioning failed: " + e);
            }

        } else {
            android.util.Log.e("MCA", "calibration not ready, cannot start positioning");
        }
    }

     /* IndoorAtlasListener interface */

    /**
     * This is where you will handle location updates.
     */
    public void onServiceUpdate(ServiceState state) {

//        mSharedBuilder.setLength(0);
//        mSharedBuilder.append("Location: ")
//                .append("\n\troundtrip : ").append(state.getRoundtrip()).append("ms")
//                .append("\n\tlat : ").append(state.getGeoPoint().getLatitude())
//                .append("\n\tlon : ").append(state.getGeoPoint().getLongitude())
//                .append("\n\tX [meter] : ").append(state.getMetricPoint().getX())
//                .append("\n\tY [meter] : ").append(state.getMetricPoint().getY())
//                .append("\n\tI [pixel] : ").append(state.getImagePoint().getI())
//                .append("\n\tJ [pixel] : ").append(state.getImagePoint().getJ())
//                .append("\n\theading : ").append(state.getHeadingDegrees())
//                .append("\n\tuncertainty: ").append(state.getUncertainty());

//        android.util.Log.e(mSharedBuilder.toString());
        latitude = state.getGeoPoint().getLatitude();
        longitude = state.getGeoPoint().getLongitude();
        android.util.Log.e("MCA"," This is latitude " + latitude);
        android.util.Log.e("MCA"," This is longitude " + longitude);
        stopPositioning(state.getGeoPoint());
    }

    private void stopPositioning(GeoPoint g) {
        mIsPositioning = false;
        if (mIndoorAtlas != null) {
            android.util.Log.e("MCA","Stop positioning");
            mIndoorAtlas.stopPositioning();
        }
        sendToParse(g);
    }

    private void sendToParse(GeoPoint g) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("RecipientList");
        query.getInBackground(ParseStarterProjectActivity.getId(), new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    RequestingCoffeeActivity.this.retrieved = object;

                } else {
                    // something went wrong
                    e.printStackTrace();
                }
            }
        });
        coordList = (ArrayList) retrieved.getList("coordList");
        coordList.add(g);
        retrieved.put("coordList", coordList);
        retrieved.saveInBackground();
        Log.e("RCA", retrieved.getObjectId());
    }

    @Override
    public void onServiceFailure(int errorCode, String reason) {
        android.util.Log.e("MCA", "onServiceFailure: reason : " + reason);
    }

    @Override
    public void onServiceInitializing() {
        android.util.Log.e("MCA","onServiceInitializing");
    }

    @Override
    public void onServiceInitialized() {
        android.util.Log.e("MCA","onServiceInitialized");
    }

    @Override
    public void onInitializationFailed(final String reason) {
        android.util.Log.e("MCA","onInitializationFailed: " + reason);
    }

    @Override
    public void onServiceStopped() {
        android.util.Log.e("MCA","onServiceStopped");
    }

    @Override
    public void onCalibrationStatus(CalibrationState calibrationState) {
        android.util.Log.e("MCA","onCalibrationStatus, percentage: " + calibrationState.getPercentage());
    }

    /**
     * Notification that calibration has reached level of quality that provides best possible
     * positioning accuracy.
     */
    @Override
    public void onCalibrationReady() {
        android.util.Log.e("MCA","onCalibrationReady");
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
