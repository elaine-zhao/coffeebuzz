package com.parse.starter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.indooratlas.android.GeoPoint;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class ParseStarterProjectActivity extends Activity {
	private boolean mc = false;
	private ArrayList<GeoPoint> coordList = new ArrayList<>();
	private ParseObject ps = new ParseObject("RecipientList");
    private static String id;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ParseAnalytics.trackAppOpenedInBackground(getIntent());
		ps.put("coordList", coordList);
		ps.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // if null, it means the save has succeeded
                    ParseStarterProjectActivity.this.id = ps.getObjectId();
                    Log.e("PSPA", ParseStarterProjectActivity.this.id);// Here you go
                } else {
                    // the save call was not successful.
                    e.printStackTrace();
                }
            }
        });
		Log.e("PSPA", "ok");
//		Log.e("PSPA", ps.getObjectId());
	}

	public void makeCoffee(View view) {
		if (mc) {
			return;
		}

		mc = true;
		Intent intent = new Intent(this, MakeCoffeeActivity.class);
		this.startActivity(intent);
	}

    public static String getId() {
        return id;
    }

	public void requestCoffee(View view) {

		Intent intent = new Intent(this, RequestingCoffeeActivity.class);
		this.startActivity(intent);
	}
}
