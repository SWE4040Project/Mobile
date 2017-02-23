package mobiledev.unb.clockin;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Brent on 2017-02-13.
 */

public class OnBreakFragment extends Fragment {
    View inflatedView = null;
    Button onbreak_button;

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

        onbreak_button = (Button) inflatedView.findViewById(R.id.onbreak_button);
        if(onbreak_button != null) {
            onbreak_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "clockin_button activated. Go to on break");
                    // Send the event to the host activity
                    mCallback.onReplaceFragmentAction(new InShiftFragment());
                }
            });
        }

        // Inflate the layout for this fragment
        return inflatedView;
    }
}