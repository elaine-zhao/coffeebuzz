package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

/*
Not sure how to properly cite other people's work, but compass code from Mike Dalisay tutorial
https://www.codeofaninja.com/2013/08/android-compass-code-example.html
 */
//compass imports
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;



import java.util.ArrayList;

public class MakeCoffeeActivity extends Activity implements IndoorAtlasListener, SensorEventListener{
    private ListView mLogView;
    private LogAdapter mLogAdapter;
    private static boolean deleteAll;

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

    //compass vars
    private ImageView image;
    // record the compass picture angle turned
    private float currentDegree = 0f;
    // device sensor manager
    private SensorManager mSensorManager;
    TextView tvHeading;


    private ParseObject retrieved;
    private ArrayList<Double> latList = new ArrayList<>();
    private ArrayList<Double> longList = new ArrayList<>();
    private TextView txt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_coffee);

        //compass
        image = (ImageView) findViewById(R.id.imageViewCompass); //TODO determine id
        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);
        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


//        mLogAdapter = new LogAdapter(this);
//        mLogView.setAdapter(mLogAdapter);
        deleteAll = false;
        txt = (TextView) findViewById(R.id.textView3);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_make_coffee, menu);
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

    public void updateLocation(View view) {
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

    public void quit(View view) {
        deleteAll = true;
        //need to clean up the ParseObject's lists
        ParseQuery<ParseObject> query = ParseQuery.getQuery("RecipientList");
        query.getInBackground(ParseStarterProjectActivity.getId(), new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    MakeCoffeeActivity.this.retrieved = object;
                    retrieved.put("latList", new ArrayList<>());
                    retrieved.put("longList", new ArrayList<>());
                    retrieved.saveInBackground();
                } else {
                    // something went wrong
                    Log.e("MCA", "ian babby");
                    e.printStackTrace();
                }
            }
        });
        Intent intent = new Intent(this, ParseStarterProjectActivity.class);
        this.startActivity(intent);
    }

    public static boolean shouldDelete() {
        return deleteAll;
    }

    public void calculate() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("RecipientList");
        query.getInBackground(ParseStarterProjectActivity.getId(), new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    MakeCoffeeActivity.this.retrieved = object;
                    latList = (ArrayList) MakeCoffeeActivity.this.retrieved.getList("latList");
                    longList = (ArrayList) MakeCoffeeActivity.this.retrieved.getList("longList");
                    double otherlat = 0;
                    double otherlong = 0;
                    Log.e("MCA", "latList: " + (ArrayList)  MakeCoffeeActivity.this.retrieved.getList("latList"));
                    Log.e("MCA", "longList: " + (ArrayList) MakeCoffeeActivity.this.retrieved.getList("longList"));
                    if (latList.size() != 0 ) {
                        otherlat = latList.get(0);
                        otherlong = longList.get(0);
                    } else {
                        txt.setText("Nobody wants coffee");
                        return;
                    }
                    Log.e("MCA", (latitude - otherlat) + " fasdfasdf");
                    Log.e("MCA", (longitude - otherlong) + " asdfasd");
                    double horizontalDist = latitude - otherlat;
                    double verticalDist = longitude -otherlong;
                    txt.setText(calculateDirection(horizontalDist, verticalDist));
                } else {
                    // something went wrong
                    Log.e("MCA", "ian babby");
                    e.printStackTrace();
                }
            }
        });

    }

    private String calculateDirection(double hd, double vd) {
        if (Math.abs(hd) > Math.abs(2*vd)) {
            if (hd > 0) {
                return "Go East";
            } else {
                return "Go West";
            }
        } else if (Math.abs(vd) > Math.abs(2*hd)) {
            if (vd > 0) {
                return "Go North";
            } else {
                return "Go South";
            }
        } else if (hd > 0 && vd > 0) {
            return "Go Northeast";
        } else if (hd < 0 && vd > 0) {
            return "Go Northwest";
        } else if (hd > 0 && vd < 0) {
            return "Go Southeast";
        } else {
            return "Go Southwest";
        }
    }

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
        latitude = state.getGeoPoint().getLatitude();
        longitude = state.getGeoPoint().getLongitude();
        android.util.Log.e("MCA"," This is latitude " + latitude);
        android.util.Log.e("MCA"," This is longitude " + longitude);
        stopPositioning();
    }

    private void stopPositioning() {
        mIsPositioning = false;
        if (mIndoorAtlas != null) {
            android.util.Log.e("MCA","Stop positioning");
            mIndoorAtlas.stopPositioning();
        }
        calculate();

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

    // compass methods
    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
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

//    public void next(View view) {
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("RecipientList");
//        query.getInBackground(ParseStarterProjectActivity.getId(), new GetCallback<ParseObject>() {
//            public void done(ParseObject object, ParseException e) {
//                if (e == null) {
//                    MakeCoffeeActivity.this.retrieved = object;
//                    latList = (ArrayList) MakeCoffeeActivity.this.retrieved.getList("latList");
//                    longList = (ArrayList) MakeCoffeeActivity.this.retrieved.getList("longList");
//                    retrieved.saveInBackground();
//                    if (RequestingCoffeeActivity.getNextable()) {
//                        Intent intent = new Intent(this, MakeCoffeeActivity.class);
//                    }
//                } else {
//                    // something went wrong
//                    Log.e("RCA", "ian babby");
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
}
