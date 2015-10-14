package com.bjitgroup.android.farhan.runtracker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by bjit on 10/8/15.
 */
public class RunFragment extends Fragment {
    private static final String TAG = "RunFragment";
    private static final int PERMISSION_GRANTED = 1;
    private static final int NOTIFICATION_ID = 3;
    private static final String ARG_RUN_ID = "RUN_ID";


    private Run mRun;
    private RunManager mRunManager;
    private Location mLastLocation;
    private TextView mTextViewStartTime, mTextViewDuration, mTextViewLatitide, mTextViewLongitude, mTextViewAltitude;
    private Button mStartButton, mStopButton;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager notiManager;

    private BroadcastReceiver mLocationReceiver = new LocationReceiver() {
        @Override
        protected void onLocationReceived(Context context, Location location) {
            if (!mRunManager.isTrackingRun(mRun))
                return;
            mLastLocation = location;
            if (isVisible())
                updateUI();
        }

        @Override
        protected void onProviderEnabledChanged(boolean enabled) {
            int toastText = enabled ? R.string.gps_enabled : R.string.gps_disabled;
            Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
        }
    };

    public static RunFragment newInstance(long runId) {
        Bundle args = new Bundle();
        args.putLong(ARG_RUN_ID, runId);
        RunFragment rf = new RunFragment();
        rf.setArguments(args);
        return rf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mRunManager = RunManager.get(getActivity());


        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GRANTED);
        }

        Bundle args = getArguments();
        if (args != null) {
            long runId = args.getLong(ARG_RUN_ID, -1);
            if (runId != -1) {
                mRun = mRunManager.getRun(runId);
                mLastLocation = mRunManager.getLastLocationForRun(runId);
            }
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);

        mTextViewStartTime = (TextView) view.findViewById(R.id.textView_startTime);
        mTextViewDuration = (TextView) view.findViewById(R.id.textView_duration);
        mTextViewLatitide = (TextView) view.findViewById(R.id.textView_latitude);
        mTextViewLongitude = (TextView) view.findViewById(R.id.textView_longitude);
        mTextViewAltitude = (TextView) view.findViewById(R.id.textView_altitude);

        mStartButton = (Button) view.findViewById(R.id.button_start);
        mStopButton = (Button) view.findViewById(R.id.button_stop);


        mStartButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if (mRun == null) {
                    mRun = mRunManager.startNewRun();
                } else {
                    mRunManager.startTrackingRun(mRun);
                }
                createNotification();
                notiManager.notify(NOTIFICATION_ID, mBuilder.build());
                updateUI();
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNotification();
                notiManager.cancel(NOTIFICATION_ID);
                mRunManager.stopRun();
                updateUI();
            }
        });

        updateUI();
        return view;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void createNotification() {
        Intent intent = new Intent(getActivity(), RunActivity.class);

        Bundle args = new Bundle();
        args.putLong(RunActivity.EXTRA_RUN_ID, mRun.getId());
        PendingIntent pi = PendingIntent
                .getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT, args);

        mBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Run Update Notification")
                .setContentText("Run Update of : " + mRun.getStartDate())
                .setContentIntent(pi);

        notiManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mLocationReceiver,
                new IntentFilter(RunManager.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(mLocationReceiver);
        super.onStop();
    }

 /*   @Override
    public void onPause() {
        super.onPause();
        if (mRunManager.isTrackingRun(mRun)) {
            createNotification();
            if(notiManager != null)
                notiManager.notify(NOTIFICATION_ID, mBuilder.build());
        }

    }*/


    private void updateUI() {
        boolean started = mRunManager.isTrackingRun();
        boolean trackingThisRun = mRunManager.isTrackingRun(mRun);
        if (mRun != null)
            mTextViewStartTime.setText(mRun.getStartDate().toString());
        int durationSeconds = 0;
        if (mRun != null && mLastLocation != null) {
            durationSeconds = mRun.getDurationSeconds(mLastLocation.getTime());
            mTextViewLatitide.setText(Double.toString(mLastLocation.getLatitude()));
            mTextViewLongitude.setText(Double.toString(mLastLocation.getLongitude()));
            mTextViewAltitude.setText(Double.toString(mLastLocation.getAltitude()));
        }
        mTextViewDuration.setText(Run.formatDuration(durationSeconds));

        mStartButton.setEnabled(!started);
        mStopButton.setEnabled(started && trackingThisRun);
    }


}