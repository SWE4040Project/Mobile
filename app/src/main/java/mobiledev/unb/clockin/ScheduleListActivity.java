package mobiledev.unb.clockin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Courses. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ScheduleDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ScheduleListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private final String TAG = "ScheduleListActivity";
    List<Shift> shiftList;
    private final String ARG_ITEM_ID = "item_id";
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_list_activity);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        shiftList = new ArrayList<Shift>();
        populateShiftList();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        View recyclerView = findViewById(R.id.course_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

    }

    private void populateShiftList(){
        showpDialog();
        Log.i(TAG, "Show dialog");

        JsonRequest req = Rest.get(
                this,
                Rest.PATH_SCHEDULE + "?start=2017-02-26&end=2017-03-30",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i(TAG,"Outputting response");
                            Log.i(TAG, response.toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                        hidepDialog();
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(shiftList));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Shift> mValues;

        public SimpleItemRecyclerViewAdapter(List<Shift> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedule_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).getId());
            holder.mContentView.setText(mValues.get(position).getEmployee_id());

            /** TODO 8:
             *
             * This onClick behavior will describe what should happen when a particular
             * course is selected from our scrolling RecyclerView list. Based on the
             * position in the scrolling list, an ARG_ITEM_ID will be passed to the
             * CourseDetailFragment along with the data we wish to display within the
             * Detail fragment portion of our application.
             *
             * The Master/Detail Flow template provides the necessary behavior
             * performance specific to the particular device type the application is
             * running on, we must only ensure we pass the appropriate information. This
             * is demonstrated in the if/else check; if mTwoPane is true we're running
             * on a tablet and we simply wish to pass a Bundle of arguments to the fragment.
             * Otherwise, we are running on a phone and must instead create an Intent, and pass
             * the appropriate arguments to that intent.
             *
             * For us, when an item is clicked we wish to display the description of
             * that particular Course item. Currently the argument being placed in
             * the Bundle is the mItem.id DummyContent Item id.
             *
             * Replace each mItem.id below so that a description of the Course item instance,
             * created below in TODO 6, is sent instead.
             *
             */
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ScheduleDetailActivity.class);
                intent.putExtra(ARG_ITEM_ID, holder.mItem.getWorked_notes());

                context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public Shift mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
