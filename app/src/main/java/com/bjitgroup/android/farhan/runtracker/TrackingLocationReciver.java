package com.bjitgroup.android.farhan.runtracker;

import android.content.Context;
import android.location.Location;

/**
 * Created by bjit on 10/12/15.
 */
public class TrackingLocationReciver extends LocationReceiver {

    protected void onLocationReceived(Context context, Location location) {
        RunManager.get(context).insertLocation(location);
    }
}
