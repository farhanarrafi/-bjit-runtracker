package com.bjitgroup.android.farhan.runtracker;

import java.util.Date;

/**
 * Created by bjit on 10/8/15.
 */
public class Run {

    private long mId;
    private Date mStartDate;


    public Run() {
        mId = -1;
        mStartDate = new Date();
    }

    public static String formatDuration(int durationSeconds) {
        int seconds = durationSeconds % 60;
        int minutes = ((durationSeconds - seconds) / 60) % 60;
        int hours = (durationSeconds - (minutes * 60) - seconds) / 3600;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public int getDurationSeconds(long endMillis) {
        return (int) ((endMillis - mStartDate.getTime()) / 1000);
    }
}
