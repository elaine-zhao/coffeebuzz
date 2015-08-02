package com.parse.starter;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;

public class ParseApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Initialize Crash Reporting.
    ParseCrashReporting.enable(this);

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    // Parse.initialize(this);
//    Parse.initialize(this, "LxwyMya38sfQC8NEBfkp1CWROtoJ64q1gq5vVjzZ", "GTr4QGDPmMcGfF0lFB4aVrtnZL6XEdOUTKi56GXR");
    Parse.initialize(this, "LxwyMya38sfQC8NEBfkp1CWROtoJ64q1gq5vVjzZ", "GTr4QGDPmMcGfF0lFB4aVrtnZL6XEdOUTKi56GXR");
    ParseInstallation.getCurrentInstallation().saveInBackground();

    // Subscribe application to push notifs on channel "coffeenotif" //TODO delete?
    ParsePush.subscribeInBackground("coffeenotif");

    ParseUser.enableAutomaticUser();
    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    // defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);
  }
}
