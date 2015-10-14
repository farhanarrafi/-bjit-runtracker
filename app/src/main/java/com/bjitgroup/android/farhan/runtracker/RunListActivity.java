package com.bjitgroup.android.farhan.runtracker;

import android.support.v4.app.Fragment;

/**
 * Created by bjit on 10/12/15.
 */
public class RunListActivity extends SingleFragmentActivity {

    protected Fragment createFragment() {
        return new RunListFragment();
    }
}
