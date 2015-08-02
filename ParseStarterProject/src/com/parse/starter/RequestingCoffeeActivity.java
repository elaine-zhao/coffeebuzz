package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;

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
    private ArrayList<Double> latList = new ArrayList<>();
    private ArrayList<Double> longList = new ArrayList<>();
    private static GeoPoint gp;
    private int initial = 0;
    private int rank = 0;

    private String length = "0";
    private TextView textElement;
    private Handler uiCallback;
    private static String objectID;
    private static boolean nextable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requesting);
        textElement = (TextView) findViewById(R.id.testView3);
        textElement.setText(length);
        initGeoPoint();
        uiCallback = new Handler () {
            public void handleMessage (Message msg) {
                // do stuff with UI
                RequestingCoffeeActivity.this.textElement.setText(length);
            }
        };

        Thread line = new Thread() {
            public void run () {
                Log.e("RCA", "MC " + ParseStarterProjectActivity.getMC());
                while (ParseStarterProjectActivity.getMC()) {
                    // do stuff in a separate thread
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("RecipientList");
                    query.getInBackground(ParseStarterProjectActivity.getId(), new GetCallback<ParseObject>() {
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                RequestingCoffeeActivity.this.retrieved = object;
                                latList = (ArrayList) RequestingCoffeeActivity.this.retrieved.getList("latList");
                                int size = latList.size();
                                if (initial >= size) {
                                    rank = size;
                                } else {
                                    rank = initial;
                                }
                                length = "" + rank;

                            } else {
                                // something went wrong
                                Log.e("RCA", "ian babby");
                                e.printStackTrace();
                            }
                        }

                    });
                    Log.e("RCA", "Length " + length);

                    uiCallback.sendEmptyMessage(0);
                    try {
                        Thread.sleep(3000);    // sleep for 3 seconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        line.start();
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

    public static boolean getNextable() {
        return nextable;
    }

    public static void flipNextable() {
        nextable = false;
    }

    public void initGeoPoint() {
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

    private void startPositioning() {
        if (mIndoorAtlas != null) {
            android.util.Log.e("RCA",String.format("startPositioning, venueId: %s, floorId: %s, floorPlanId: %s",
                    mVenueId,
                    mFloorId,
                    mFloorPlanId));
            try {
                mIndoorAtlas.startPositioning(mVenueId, mFloorId, mFloorPlanId);
                android.util.Log.e("RCA", "there is no error");

            } catch (IndoorAtlasException e) {
                android.util.Log.e("RCA","startPositioning failed: " + e);
            }

        } else {
            android.util.Log.e("RCA", "calibration not ready, cannot start positioning");
        }
    }

     /* IndoorAtlasListener interface */

    /**
     * This is where you will handle location updates.
     */
    public void onServiceUpdate(ServiceState state) {
        latitude = state.getGeoPoint().getLatitude();
        longitude = state.getGeoPoint().getLongitude();
        android.util.Log.e("RCA"," This is latitude " + latitude);
        android.util.Log.e("RCA"," This is longitude " + longitude);
        gp = state.getGeoPoint();
        android.util.Log.e("RCA"," This is gp " + gp);
//        stopPositioning(state.getGeoPoint());
        stopPositioning();
    }

    private void stopPositioning() {
        mIsPositioning = false;

        if (mIndoorAtlas != null) {
            android.util.Log.e("RCA","Stop positioning");

            mIndoorAtlas.stopPositioning();
        }

    }

    public void sendToParse(View view) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("RecipientList");
        query.getInBackground(ParseStarterProjectActivity.getId(), new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    RequestingCoffeeActivity.this.retrieved = object;
                    Log.e("RCA", retrieved.getObjectId());
                    latList = (ArrayList) RequestingCoffeeActivity.this.retrieved.getList("latList");
                    longList = (ArrayList) RequestingCoffeeActivity.this.retrieved.getList("longList");
                    latList.add(latitude);
                    longList.add(longitude);
                    retrieved.put("latList", latList);
                    retrieved.put("longList", longList);
                    Log.e("RCA", "latList: " + (ArrayList) RequestingCoffeeActivity.this.retrieved.getList("latList"));
                    Log.e("RCA", "longList: " + (ArrayList) RequestingCoffeeActivity.this.retrieved.getList("longList"));
                    retrieved.saveInBackground();
                    Log.e("RCA", "bye parse");
                    objectID = retrieved.getObjectId();
                    initial = latList.size();
                } else {
                    // something went wrong
                    Log.e("RCA", "ian babby");
                    e.printStackTrace();
                }
            }
        });

    }
    
    public void cancelRequest(View view) {
        if (rank == 1) {
            nextable = true;
        }
        if (rank != 0) {
            latList.remove(rank - 1);
            longList.remove(rank - 1);
        }
        Intent intent = new Intent(this, ParseStarterProjectActivity.class);
        this.startActivity(intent);
    }

    public static String getObjectID() {
        return objectID;
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
