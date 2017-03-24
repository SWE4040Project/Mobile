package mobiledev.unb.clockin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_list_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        shiftList = new ArrayList<Shift>();
        populateShiftList();

        recyclerView = (RecyclerView) findViewById(R.id.course_list);
        assert recyclerView != null;

    }

    private void populateShiftList(){
        showpDialog();
        Log.i(TAG, "Show dialog");

        JsonRequest req = Rest.get(
                this,
                Rest.PATH_SCHEDULE + "?start=2017-02-26&end=2017-03-30",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Log.i(TAG,"Outputting response");
                            Log.i(TAG, response.toString());
                            for(int i=0; i<response.length(); i++){
                                JSONObject s = (JSONObject) response.get(i);
                                Shift shift = new Shift();
                                shift.setId(s.getInt("id"));
                                shift.setEmployee_id(s.getInt("employee_id"));
                                try{
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                                    Date parsedDate = dateFormat.parse(s.getString("start"));
                                    Timestamp startTimestamp = new java.sql.Timestamp(parsedDate.getTime());
                                    shift.setScheduled_start_time(startTimestamp);
                                }catch(Exception e){//this generic but you can control another types of exception
                                    e.printStackTrace();
                                }
                                try{
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                                    Date parsedDate = dateFormat.parse(s.getString("end"));
                                    Timestamp endTimestamp = new java.sql.Timestamp(parsedDate.getTime());
                                    shift.setScheduled_start_time(endTimestamp);
                                }catch(Exception e){//this generic but you can control another types of exception
                                    e.printStackTrace();
                                }

                                shiftList.add(shift);
                            }

                            setupRecyclerView(recyclerView);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return  true;
        }
        return super.onOptionsItemSelected(item);
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
            Log.i(TAG,"onCreateViewHolder");
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Log.i(TAG,"onBindViewHolder");
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(" "+mValues.get(position).getId());
            holder.mCompanyId.setText(" "+mValues.get(position).getEmployee_id());
            holder.mStartTime.setText("2:00 pm Feb 28th");
            holder.mEndTime.setText("10:00 pm Feb 28th");

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
            public final TextView mCompanyId;
            public final TextView mStartTime;
            public final TextView mEndTime;
            public Shift mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mCompanyId = (TextView) view.findViewById(R.id.companyId);
                mStartTime = (TextView) view.findViewById(R.id.start_time_id);
                mEndTime = (TextView) view.findViewById(R.id.end_time_id);
                Log.i(TAG,"ViewHolder constructor");
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mCompanyId.getText() + "'";
            }
        }
    }
}
