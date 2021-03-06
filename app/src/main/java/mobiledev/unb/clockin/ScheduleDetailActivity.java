package mobiledev.unb.clockin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * An activity representing a single Course detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ScheduleListActivity}.
 */
public class ScheduleDetailActivity extends AppCompatActivity {

    private final String ARG_ITEM_ID = "item_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_detail_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView details = (TextView) findViewById(R.id.detail_view);
        String a = "Shift: "+ getIntent().getExtras().getString("start")+" "+getIntent().getExtras().getString("end") + "\n\n";
        a += "Clockin start: "+ getIntent().getExtras().getString("real_start") + "\n\n";
        a += "Clockin end: "+ getIntent().getExtras().getString("real_end") + "\n\n";
        a += "Shift notes: "+ getIntent().getExtras().getString("notes");

        details.setText(a);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
//            Bundle arguments = new Bundle();
//            arguments.putString(ARG_ITEM_ID,
//                    getIntent().getStringExtra(ARG_ITEM_ID));
//            CourseDetailFragment fragment = new CourseDetailFragment();
//            fragment.setArguments(arguments);
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.course_detail_container, fragment)
//                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
