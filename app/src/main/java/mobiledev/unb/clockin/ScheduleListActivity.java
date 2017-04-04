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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    int lastPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_list_activity);

        int viewType = (int) getIntent().getExtras().getInt("viewType");

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
        //start
        cal.add(Calendar.DAY_OF_MONTH, -2);
        String startTime = timeFormat.format(cal.getTime());
        String titleStart = new SimpleDateFormat("dd").format(cal.getTime());
        //end
        cal.add(Calendar.DAY_OF_MONTH, 7);
        String endTime = timeFormat.format(cal.getTime());
        String titleEnd = new SimpleDateFormat("dd").format(cal.getTime());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String title;
        switch(viewType){
            case 1: title = "Currently Working";
                break;
            case 2: title = "My Shifts";
                break;
            case 3: title = "Worked Shifts";
                break;
            default: title = "Company Schedule";
        }
        toolbar.setTitle(title + "  " + titleStart +" - " +titleEnd);
        setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        shiftList = new ArrayList<>();
        populateShiftList(viewType, startTime, endTime);

        recyclerView = (RecyclerView) findViewById(R.id.course_list);
        assert recyclerView != null;

    }

    private void populateShiftList(final int viewType, String start, String end){
        showpDialog();

        JsonRequest req = Rest.get(
                this,
                Rest.PATH_SCHEDULE + "?start="+start+"&end="+end+"&viewType="+viewType,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
                            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
                            SimpleDateFormat hourFormat = new SimpleDateFormat("hh:mm a");
                            Calendar cal = Calendar.getInstance();

                            for(int i=0; i<response.length(); i++){
                                JSONObject s = (JSONObject) response.get(i);
                                Log.i(TAG,s.toString());
                                /*
                                      {"id":122,
                                      "employee_id":18,
                                      "title":"josh123  12:00 PM - 08:00 PM",
                                      "allDay":"true",
                                      "start":"2017-03-24T12:00:00Z",
                                      "end":"2017-03-24T20:00:00Z",
                                      "color":"orange",
                                      "editable":"true",
                                      "real_start":"",
                                      "real_end":""}
                                */
                                Shift shift = new Shift();
                                try {
                                    shift.setEmployee_id(s.getInt("employee_id"));
                                    String[] arr = s.getString("title").split(" ");
                                    shift.setTitle(arr[0]);
                                    String notes = s.getString("note") != null ? s.getString("note") : "No notes";
                                    shift.setWorked_notes(notes);
                                }catch(Exception e){
                                    e.printStackTrace();
                                }

                                try{
                                    Date parsedDate = dateFormat.parse(s.getString("start"));
                                    cal.setTime(parsedDate);
                                    shift.setDay(""+cal.get(Calendar.DAY_OF_MONTH));
                                    shift.setMonth(monthFormat.format(cal.getTime()));
                                    shift.setScheduled_start_time(hourFormat.format(cal.getTime()) + " -");
                                }catch(Exception e){//this generic but you can control another types of exception
                                    e.printStackTrace();
                                }
                                try{
                                    Date parsedDate = dateFormat.parse(s.getString("end"));
                                    cal.setTime(parsedDate);
                                    shift.setScheduled_end_time(hourFormat.format(cal.getTime()));
                                }catch(Exception e){//this generic but you can control another types of exception
                                    e.printStackTrace();
                                }
                                try{
                                    Date parsedDate = dateFormat.parse(s.getString("real_start"));
                                    cal.setTime(parsedDate);
                                    shift.setReal_start_time(hourFormat.format(cal.getTime()));
                                }catch(Exception e){//this generic but you can control another types of exception
                                    e.printStackTrace();
                                }
                                try{
                                    Date parsedDate = dateFormat.parse(s.getString("real_end"));
                                    cal.setTime(parsedDate);
                                    shift.setReal_end_time(hourFormat.format(cal.getTime()));
                                }catch(Exception e){//this generic but you can control another types of exception
                                    e.printStackTrace();
                                }

                                shiftList.add(shift);
                            }

                            setupRecyclerView(recyclerView);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Need to re-authenticate; logging out...",
                                    Toast.LENGTH_LONG).show();
                            MainActivity.logout(ScheduleListActivity.this);
                        }
                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                        hidepDialog();
                        Toast.makeText(getApplicationContext(),
                                "Need to re-authenticate; logging out...",
                                Toast.LENGTH_LONG).show();
                        MainActivity.logout(ScheduleListActivity.this);
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
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Log.i(TAG,"onBindViewHolder");

            try {
                holder.mIdView.setText(mValues.get(position).getTitle());
                holder.mStartTime.setText(mValues.get(position).getScheduled_start_time());
                holder.mEndTime.setText(mValues.get(position).getScheduled_end_time());
                holder.mDateBox.setText(getSuffix(mValues.get(position).getDay()));//Pull date from string
                holder.mMonth.setText(mValues.get(position).getMonth());
            }catch(NullPointerException e){
                e.printStackTrace();
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ScheduleDetailActivity.class);
                intent.putExtra("start", mValues.get(position).getScheduled_start_time());
                intent.putExtra("end", mValues.get(position).getScheduled_end_time());
                intent.putExtra("real_start", mValues.get(position).getReal_start_time());
                intent.putExtra("real_end", mValues.get(position).getReal_end_time());
                intent.putExtra("note", mValues.get(position).getWorked_notes());

                context.startActivity(intent);
                }
            });

            setAnimation(holder.mView, position);

        }

        private String getSuffix(String s){
            int n = Integer.parseInt(s);
            switch(n % 10){
                case 1: s = s + "st";
                    break;
                case 2: s = s + "nd";
                    break;
                case 3: s = s + "rd";
                    break;
                default: s = s + "th";
                    break;
            }
            return s;
        }

        private void setAnimation(View viewToAnimate, int position)
        {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition)
            {
                Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(),R.anim.push_left_in);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mStartTime;
            public final TextView mEndTime;
            public final TextView mDateBox;
            public final TextView mMonth;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.name);
                mStartTime = (TextView) view.findViewById(R.id.start_time_id);
                mEndTime = (TextView) view.findViewById(R.id.end_time_id);
                mDateBox = (TextView) view.findViewById(R.id.day);
                mMonth = (TextView) view.findViewById(R.id.month);
            }
        }
    }
}
