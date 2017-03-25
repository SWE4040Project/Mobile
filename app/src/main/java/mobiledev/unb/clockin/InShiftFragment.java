package mobiledev.unb.clockin;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import at.markushi.ui.CircleButton;

/**
 * Created by Brent on 2017-02-13.
 */

public class InShiftFragment extends Fragment {
    View inflatedView = null;
    View promptView = null;
    CircleButton inshift_button;
    CircleButton clockout_button;
    CircleButton addnote_button;
    private TextView clockedNotes;
    private TextView timesClocked;
    private TextView shiftStart;
    private TextView shiftEnd;
    private TextView shift;
    private ProgressDialog pDialog;
    private String notes;
    private ProgressBar progressBar;

    final String TAG = InShiftFragment.class.getSimpleName();

    ClockinFragmentScreenListener mCallback;

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

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        progressBar = (ProgressBar)inflatedView.findViewById(R.id.progressBar);

        timesClocked =(TextView)inflatedView.findViewById(R.id.timesClocked);
        clockedNotes = (TextView)inflatedView.findViewById(R.id.clockedNotes);
        shift = (TextView) inflatedView.findViewById(R.id.shift);
        shiftStart = (TextView) inflatedView.findViewById(R.id.shiftStart);
        shiftEnd = (TextView) inflatedView.findViewById(R.id.shiftEnd);

        Log.i(TAG, "onCreateView");

        inshift_button = (CircleButton) inflatedView.findViewById(R.id.inshift_button);
        inshift_button.setEnabled(false);
        inshift_button.bringToFront();
        clockout_button = (CircleButton) inflatedView.findViewById(R.id.clockout_button);
        clockout_button.setEnabled(false);
        clockout_button.bringToFront();
        addnote_button = (CircleButton) inflatedView.findViewById(R.id.addnote_button);
        addnote_button.setEnabled(false);
        showpDialog();
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

                            shift.setText(response.get("scheduled_start") + " - " + response.get("scheduled_end")
                                    + "\n" + (String)response.get("scheduled_day"));
                            shiftStart.setText((String)response.get("scheduled_start"));
                            shiftEnd.setText((String)response.get("scheduled_end"));
                            int progress = 0;
                            try{
                                Log.d(TAG, ""+response.get("progress"));
                                double d = (double)response.get("progress");
                                progress = (int) d;
                                Log.d(TAG, ""+progress);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            progressBar.setProgress(progress);
                            timesClocked.setText("Clocked in at\n"+(String)response.get("actual_start"));
                            String currentNotes = "No Notes";
                            if(response.get("shift_notes") != null){
                                currentNotes = (String)response.get("shift_notes");
                                notes = currentNotes;
                            }
                            clockedNotes.setText(currentNotes);

                            hidepDialog();
                            inshift_button.setEnabled(true);
                            clockout_button.setEnabled(true);
                            addnote_button.setEnabled(true);
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

                            if(addnote_button != null){
                                Log.i(TAG, "Creating addnote_button");
                                addnote_button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.i(TAG, "addnote_button activated. Creating dialog");
                                        LayoutInflater inflater2 = getActivity().getLayoutInflater();
                                        promptView = inflater2.inflate(R.layout.prompt, null);
                                        EditText editText = (EditText) promptView.findViewById(R.id.editTextDialogUserInput);
                                        if(editText != null){
                                            editText.setText(notes);
                                        }
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                        alertDialogBuilder.setView(promptView);

                                        final EditText userInput = (EditText) promptView.findViewById(R.id.editTextDialogUserInput);
                                        alertDialogBuilder.setCancelable(false).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                notes = userInput.getText().toString();
                                                Log.i(TAG, "notes = "+notes);
                                                addNotesCall(notes);
                                            }
                                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                            }
                                        });

                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                            hidepDialog();
                            Toast.makeText(getActivity(),
                                    "Need to re-authenticate; logging out...",
                                    Toast.LENGTH_LONG).show();
                            MainActivity.logout(getActivity());
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, "Error: " + error.getMessage());
                        hidepDialog();
                        Toast.makeText(getActivity(),
                                "Need to re-authenticate; logging out...",
                                Toast.LENGTH_LONG).show();
                        MainActivity.logout(getActivity());
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    private void breakinCall() {
        showpDialog();
        JsonObjectRequest req = Rest.post(
                getActivity(),
                Rest.PATH_BREAKIN,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Breakin successful!");
                            hidepDialog();
                            mCallback.onReplaceFragmentAction(new OnBreakFragment());
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                            hidepDialog();
                            Toast.makeText(getActivity(),
                                    "Need to re-authenticate; logging out...",
                                    Toast.LENGTH_LONG).show();
                            MainActivity.logout(getActivity());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, "Error: " + error.getMessage());
                        hidepDialog();
                        Toast.makeText(getActivity(),
                                "Need to re-authenticate; logging out...",
                                Toast.LENGTH_LONG).show();
                        MainActivity.logout(getActivity());
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    private void clockoutCall() {
        showpDialog();
        JsonObjectRequest req = Rest.post(
                getActivity(),
                Rest.PATH_CLOCKOUT,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Clockout successful!");
                            hidepDialog();
                            mCallback.onReplaceFragmentAction(new ClockinFragment());
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                            hidepDialog();
                            Toast.makeText(getActivity(),
                                    "Need to re-authenticate; logging out...",
                                    Toast.LENGTH_LONG).show();
                            MainActivity.logout(getActivity());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, "Error: " + error.getMessage());
                        hidepDialog();
                        Toast.makeText(getActivity(),
                                "Need to re-authenticate; logging out...",
                                Toast.LENGTH_LONG).show();
                        MainActivity.logout(getActivity());
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    private void addNotesCall(final String notes) {
        showpDialog();

        JSONObject params = new JSONObject();
        try {
            params.put("workedNote", notes);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = Rest.post(
                getActivity(),
                Rest.PATH_ADDSHIFTNOTE,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Add notes successful!");
                            hidepDialog();
                            clockedNotes.setText(notes);
                            Toast.makeText(getActivity(),
                                    "Note added.",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                            hidepDialog();
                            Toast.makeText(getActivity(),
                                    "Need to re-authenticate; logging out...",
                                    Toast.LENGTH_LONG).show();
                            MainActivity.logout(getActivity());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, "Error: " + error.getMessage());
                        hidepDialog();
                        Toast.makeText(getActivity(),
                                "Need to re-authenticate; logging out...",
                                Toast.LENGTH_LONG).show();
                        MainActivity.logout(getActivity());
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
}