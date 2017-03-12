package mobiledev.unb.clockin;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Created by Brent on 2017-02-13.
 */

public class InShiftFragment extends Fragment {
    View inflatedView = null;
    Button inshift_button;
    Button clockout_button;

    final String TAG = InShiftFragment.class.getSimpleName();

    ClockinFragmentScreenListener mCallback;

    private TextView shift_time_textview;
    private TextView shift_day_textview;
    private TextView clockin_time;
    private EditText shift_notes;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ClockinFragmentScreenListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ClockinScreenListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.inflatedView = inflater.inflate(R.layout.inshift_fragment, container, false);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.hide();

        Log.i(TAG, "onCreateView");

        shift_time_textview = (TextView) inflatedView.findViewById(R.id.shift_time_textview);
        shift_day_textview = (TextView) inflatedView.findViewById(R.id.shift_day_textview);
        clockin_time = (TextView) inflatedView.findViewById(R.id.clockin_time);
        shift_notes = (EditText) inflatedView.findViewById(R.id.shift_notes);

        inshift_button = (Button) inflatedView.findViewById(R.id.inshift_button);
        inshift_button.setEnabled(false);
        clockout_button = (Button) inflatedView.findViewById(R.id.clockout_button);
        clockout_button.setEnabled(false);
        setCurrentShift();

        // Inflate the layout for this fragment
        return inflatedView;
    }
    private void setCurrentShift() {
        JsonObjectRequest req = Rest.get(
                getActivity(),
                Rest.PATH_SHIFT_CURRENT,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Shifts loaded successfully!");
                            Log.d(TAG, response.toString());
                            /*{"scheduled_day":"Friday, Mar 10",
                                "scheduled_end":"11:14 PM",
                                "scheduled_start":"6:38 PM",
                                "actual_start":"6:26 PM Friday, Mar 10",
                                "progress":0.17500198240215395,
                                "shift_notes":"asdaasdfsfsdfasdfafaasdaasdfsfsdfasdaasdfsfsdfasdfafaaasdfdaasdfsfsdfasdfafaasdaasdfsfsdfasdfafasdfasdaasdfsfsdfasdfafaasdaasdfsfsdfasdfafaasdaasdfsfsdfasdfafaasdaasdfsfsdfasdfafaasdaasdfsfsdfasdfaf",
                                "actual_end":"--",
                                "current_time":"6:45 PM"}*/

                            shift_time_textview.setText(response.get("scheduled_start") + " - " + response.get("scheduled_end"));
                            shift_day_textview.setText((String)response.get("scheduled_day"));
                            clockin_time.setText((String)response.get("actual_start"));
                            shift_notes.setText((String)response.get("shift_notes"));

                            inshift_button.setEnabled(true);
                            clockout_button.setEnabled(true);
                            if(inshift_button != null) {
                                Log.i(TAG, "Creating inshift_button");
                                inshift_button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.i(TAG, "inshift_button activated. Go to on break");
                                        // Send the event to the host activity
                                        breakinCall();
                                    }
                                });
                            }

                            if(clockout_button != null) {
                                Log.i(TAG, "Creating clockout_button");
                                clockout_button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.i(TAG, "clockout_button activated. Clocking out...");
                                        // Send the event to the host activity
                                        clockoutCall();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                            Toast.makeText(getActivity(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getActivity(),
                                "Failed at this moment.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    private void breakinCall() {
        JsonObjectRequest req = Rest.post(
                getActivity(),
                Rest.PATH_BREAKIN,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Breakin successful!");

                            mCallback.onReplaceFragmentAction(new OnBreakFragment());
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                            Toast.makeText(getActivity(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getActivity(),
                                "Failed at this moment.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    private void clockoutCall() {
        JsonObjectRequest req = Rest.post(
                getActivity(),
                Rest.PATH_CLOCKOUT,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Clockout successful!");

                            mCallback.onReplaceFragmentAction(new ClockinFragment());
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                            Toast.makeText(getActivity(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getActivity(),
                                "Failed at this moment.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }
}