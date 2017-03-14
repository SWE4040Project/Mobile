package mobiledev.unb.clockin;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import at.markushi.ui.CircleButton;

import static mobiledev.unb.clockin.InShiftFragment.notes;

/**
 * Created by Brent on 2017-02-13.
 */

public class OnBreakFragment extends Fragment {
    View inflatedView = null;
    View promptView = null;
    CircleButton onbreak_button;
    CircleButton addnote_button2;
    CircleButton clockout_button2;
    private Calendar calendar= Calendar.getInstance();
    private static String breakString = "";
    private TextView breakResult;
    private TextView clockedBreak;
    private ClockView mClock;
    private SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");

    final String TAG = OnBreakFragment.class.getSimpleName();

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

        this.inflatedView = inflater.inflate(R.layout.onbreak_fragment, container, false);

        final Context context = inflater.getContext();

        calendar = Calendar.getInstance();
        breakString =breakString + mdformat.format(calendar.getTime())+"\n";
        clockedBreak = (TextView) inflatedView.findViewById(R.id.clockedBreak);
        clockedBreak.setText(breakString);

        breakResult = (TextView)inflatedView.findViewById(R.id.breakResult);
        breakResult.setText(notes); //same string from inshift

        mClock = (ClockView) inflatedView.findViewById(R.id.clock3);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.hide();

        onbreak_button = (CircleButton) inflatedView.findViewById(R.id.onbreak_button);
        if(onbreak_button != null) {
            onbreak_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Save break start time:" +breakString);
                    breakString = "";
                    Log.i(TAG, "clockin_button activated. Go to on in shift");

                    // Send the event to the host activity
                    mCallback.onReplaceFragmentAction(new InShiftFragment());
                }
            });
        }

        clockout_button2 = (CircleButton) inflatedView.findViewById(R.id.clockout_button2);
        if(clockout_button2 != null) {
            clockout_button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CharSequence text = "Must clock back in first";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            });
        }

        addnote_button2 = (CircleButton) inflatedView.findViewById(R.id.addnote_button);
        if(addnote_button2 != null){
            Log.i(TAG, "Creating addnote_button");
            addnote_button2.setOnClickListener(new View.OnClickListener() {
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
                            breakResult.setText(notes);
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