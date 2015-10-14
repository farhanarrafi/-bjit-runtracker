package com.bjitgroup.android.farhan.runtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by bjit on 10/12/15.
 */
public class RunListFragment extends ListFragment {
    private static int ACTIVE_LIST_ITEM_COLOR = 0;
    private static int INACTIVE_LIST_ITEM_COLOR = 0;
    private static final int REQUEST_NEW_RUN = 0;
    private RunDatabaseHelper.RunCursor mCursor;

    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCursor = RunManager.get(getActivity()).queryRuns();
        context = getActivity().getApplicationContext();
        RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), mCursor);
        setListAdapter(adapter);
        ACTIVE_LIST_ITEM_COLOR = getActivity().getResources().getColor(android.R.color.holo_red_light);
        INACTIVE_LIST_ITEM_COLOR = getActivity().getResources().getColor(android.R.color.background_dark);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCursor.close();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.run_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_run:
                Intent intent = new Intent(getActivity(), RunActivity.class);
                startActivityForResult(intent, REQUEST_NEW_RUN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_RUN) {
            mCursor.requery();

            ((RunCursorAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Intent intent = new Intent(getActivity(), RunActivity.class);
        intent.putExtra(RunActivity.EXTRA_RUN_ID, id);
        startActivity(intent);
    }

    private static class RunCursorAdapter extends CursorAdapter {
        private RunDatabaseHelper.RunCursor mRunCursor;

        public RunCursorAdapter(Context context, RunDatabaseHelper.RunCursor cursor) {
            super(context, cursor, 0);
            mRunCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.
                    inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Run run = mRunCursor.getRun();
            TextView textViewStartDate = (TextView) view;
            String cellText = context
                    .getString(R.string.cell_text, run.getStartDate());
            SharedPreferences pref = context.getSharedPreferences(RunManager.PREFS_FILE, Context.MODE_PRIVATE);
            if( pref.getLong(RunManager.PREFS_CURRENT_RUN_ID, -1) == run.getId())
                textViewStartDate.setBackgroundColor(ACTIVE_LIST_ITEM_COLOR);
            textViewStartDate.setBackgroundColor(INACTIVE_LIST_ITEM_COLOR);
            textViewStartDate.setText(cellText + " at: "+ run.getStartDate());
        }
    }
}
