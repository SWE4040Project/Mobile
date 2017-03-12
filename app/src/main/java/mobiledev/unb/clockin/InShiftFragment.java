package mobiledev.unb.clockin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    TextView result;
    private ClockView mClock;
    private Calendar calendar= Calendar.getInstance();
    private SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");
    private TextView timesClocked;
    private static String timeString ="";
    protected String clockedTimes;
    protected static String notes = "";
    protected String clockedNotes;




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

        final Context context = inflater.getContext();

        timesClocked =(TextView)inflatedView.findViewById(R.id.timesClocked);



        calendar = Calendar.getInstance();
        timeString =timeString + mdformat.format(calendar.getTime())+"\n";
        Log.i(TAG, "Setting clock-in time- "+ timeString);
        timesClocked.setMovementMethod(ScrollingMovementMethod.getInstance());

        timesClocked.setText(timeString);//FOR SOME REASON WON'T DISPLAY FIRST TIME ANYMORE

        Log.i(TAG, "timesClocked.getText()= "+ timesClocked.getText());




        result = (TextView)inflatedView.findViewById(R.id.result);
        result.setText(notes);

        mClock = (ClockView) inflatedView.findViewById(R.id.clock2);



        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.hide();

        Log.i(TAG, "onCreateView");

        inshift_button = (CircleButton) inflatedView.findViewById(R.id.inshift_button);
        if(inshift_button != null) {
            Log.i(TAG, "Creating inshift_button");
            inshift_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "inshift_button activated. Go to on break");
                    // Send the event to the host activity
                    mCallback.onReplaceFragmentAction(new OnBreakFragment());
                }
            });
        }

        clockout_button = (CircleButton) inflatedView.findViewById(R.id.clockout_button);
        if(clockout_button != null) {
            Log.i(TAG, "Creating clockout_button");
            clockout_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "clockout_button activated. Clocking out...");
                    // Send the event to the host activity
                    mCallback.onReplaceFragmentAction(new ClockinFragment());
                    clockedTimes = timeString;
                    Log.i(TAG, "The user clocked in at:\n"+clockedTimes);
                    timesClocked.setText("");
                    timeString ="";
                    clockedNotes = notes;
                    Log.i(TAG, "The user's clock-in notes are:\n"+clockedNotes);
                    result.setText("");
                    notes = "";

                }
            });
        }

        addnote_button = (CircleButton) inflatedView.findViewById(R.id.addnote_button);
        if(addnote_button != null){
            Log.i(TAG, "Creating addnote_button");
            addnote_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "addnote_button activated. Creating dialog");
                    LayoutInflater inflater2 = getActivity().getLayoutInflater();
                    promptView = inflater2.inflate(R.layout.prompt, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                    alertDialogBuilder.setView(promptView);

                    final EditText userInput = (EditText) promptView.findViewById(R.id.editTextDialogUserInput);


                    alertDialogBuilder.setCancelable(false).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            notes = notes + "- " + userInput.getText()+"\n";
                            Log.i(TAG, "notes = "+notes);
                            result.setText(notes);
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

        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mClock.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mClock.pause();
    }
}