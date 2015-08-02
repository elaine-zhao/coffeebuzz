package com.parse.starter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.indooratlas.android.GeoPoint;
import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class ParseStarterProjectActivity extends Activity {
	private static boolean mc = false;
	private static boolean firstOneOnTheList = false; //only when this is false maker can press next.
	private ArrayList<Double> latList = new ArrayList<>();
	private ArrayList<Double> longList = new ArrayList<>();
	private ParseObject ps;
    private static String id;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.e("PSPA", "Hi " + RequestingCoffeeActivity.getObjectID());
		if (RequestingCoffeeActivity.getObjectID() != null && !MakeCoffeeActivity.shouldDelete()) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("RecipientList");
			query.getInBackground(RequestingCoffeeActivity.getObjectID(), new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
					if (e == null) {
						ParseStarterProjectActivity.this.ps = object;

						ParseAnalytics.trackAppOpenedInBackground(getIntent());
						ps.put("latList", latList);
						ps.put("longList", longList);
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
					} else {
						// something went wrong
						Log.e("PSPA", "ian babby");
						e.printStackTrace();
					}
				}
			});

		} else {
			if (MakeCoffeeActivity.shouldDelete()) {
				mc = false;
			}
			ps = new ParseObject("RecipientList");

			ParseAnalytics.trackAppOpenedInBackground(getIntent());
			ps.put("latList", latList);
			ps.put("longList", longList);
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
		}


//		Log.e("PSPA", ps.getObjectId());
	}

	public void makeCoffee(View view) {
		if (mc) {
			return;
		}

		// send push notif that coffee is being made
		ParsePush push = new ParsePush();
		push.setChannel("coffeenotif");
		push.setMessage("Someone is making a fresh pot of coffee. Hit \"Request\" to request a coffee delivery.");
		push.sendInBackground();

		mc = true;
		Intent intent = new Intent(this, MakeCoffeeActivity.class);
		this.startActivity(intent);
	}

    public static String getId() {
		return id;
    }

	public static boolean getMC() {
		return mc;
	}

	public static boolean getFirst() {return firstOneOnTheList;}

	public void requestCoffee(View view) {

		Intent intent = new Intent(this, RequestingCoffeeActivity.class);
		this.startActivity(intent);
	}
}
