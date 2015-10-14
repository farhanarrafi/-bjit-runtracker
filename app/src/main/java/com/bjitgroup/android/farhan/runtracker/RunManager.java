package com.bjitgroup.android.farhan.runtracker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by bjit on 10/8/15.
 */
public class RunManager {
    private static final String PREFS_FILE = "runs";
    public static final String PREFS_CURRENT_RUN_ID = "RunManager.currentRunId";
    public static final String ACTION_LOCATION = "com.bjitgroup.android.farhan.runtracker.ACTION_LOCATION";
    private static final String TAG = "RunManager";
    private static final String TEST_PROVIDER = "TEST_PROVIDER";
    private static RunManager sRunManager;
    private Context mContext;
    private LocationManager mLocationManager;
    private RunDatabaseHelper mHelper;
    private SharedPreferences mPrefs;
    private long mCurrentRunId;


    private RunManager(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        mHelper = new RunDatabaseHelper(mContext);
        mPrefs = mContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentRunId = mPrefs.getLong(PREFS_CURRENT_RUN_ID, -1);
    }

    public static RunManager get(Context context) {
        if (sRunManager == null) {
            sRunManager = new RunManager(context.getApplicationContext());
        }
        return sRunManager;
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mContext, 0, broadcast, flags);
    }

    public boolean isTrackingRun() {
        return getLocationPendingIntent(false) != null;
    }

    public boolean isTrackingRun(Run run) {
        return run != null && run.getId() == mCurrentRunId;
    }

    public void startlocationUpdates() throws SecurityException {
        String provider = LocationManager.GPS_PROVIDER;
        if (mLocationManager.getProvider(TEST_PROVIDER) != null &&
                mLocationManager.isProviderEnabled(TEST_PROVIDER)) {
            provider = TEST_PROVIDER;
        }
        Log.d(TAG, "Using Provider" + provider);

        Location lastKnown = mLocationManager.getLastKnownLocation(provider);

        if (lastKnown != null) {
            lastKnown.setTime(System.currentTimeMillis());
            broadcastlocation(lastKnown);
        }

        PendingIntent pi = getLocationPendingIntent(true);
        mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
    }

    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            mLocationManager.removeUpdates(pi);
            pi.cancel();
        }
    }

    private void broadcastlocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        mContext.sendBroadcast(broadcast);
    }


    public Run startNewRun() {
        Run run = insertRun();
        startTrackingRun(run);
        return run;
    }

    public void stopRun() {
        stopLocationUpdates();
        mCurrentRunId = -1;
        mPrefs.edit().remove(PREFS_CURRENT_RUN_ID).commit();
    }

    public void startTrackingRun(Run run) {
        mCurrentRunId = run.getId();
        mPrefs.edit().putLong(PREFS_CURRENT_RUN_ID, mCurrentRunId).commit();
        startlocationUpdates();
    }

    private Run insertRun() {
        Run run = new Run();
        run.setId(mHelper.insertRun(run));
        return run;
    }

    public void insertLocation(Location location) {
        if (mCurrentRunId != -1) {
            mHelper.insertLocation(mCurrentRunId, location);
        } else {
            Log.e(TAG, "Location received with no tracking run; IGNORING.");
        }
    }

    public RunDatabaseHelper.RunCursor queryRuns() {
        return mHelper.queryRuns();
    }

    public Run getRun(long id) {
        Run run = null;
        RunDatabaseHelper.RunCursor cursor = mHelper.queryRun(id);
        cursor.moveToFirst();
        if (!cursor.isAfterLast())
            run = cursor.getRun();
        cursor.close();
        return run;
    }

    public Location getLastLocationForRun(long runId) {
        Location location = null;
        RunDatabaseHelper.LocationCursor cursor = mHelper
                .queryLastLocationForRun(runId);
        cursor.moveToFirst();
        if (!cursor.isAfterLast())
            location = cursor.getLocation();
        cursor.close();
        return location;
    }


}