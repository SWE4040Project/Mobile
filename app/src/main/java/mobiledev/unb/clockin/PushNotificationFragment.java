package mobiledev.unb.clockin;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Brent on 2017-02-27.
 */
public class PushNotificationFragment extends Fragment {
    View inflatedView = null;
    Button push_notification_button;

    final String TAG = PushNotificationFragment.class.getSimpleName();

    ClockinFragmentScreenListener mCallback;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.inflatedView = inflater.inflate(R.layout.push_notification_fragment, container, false);

        push_notification_button = (Button) inflatedView.findViewById(R.id.push_notification_button);
        if(push_notification_button != null) {
            push_notification_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "push_notification_button activated. Send push notification");
                    // Send the event to the host activity
                    sendPushNotification();
                }
            });
        }

        // Inflate the layout for this fragment
        return inflatedView;
    }

    private void sendPushNotification() {

        String token = FirebaseInstanceId.getInstance().getToken();

        EditText et = (EditText) getActivity().findViewById(R.id.editText);
        String body = et.getText().toString();
        body = body.replaceAll("[^a-zA-Z0-9 ]", "");
        body = body.replaceAll("\\s","%20");

        JsonObjectRequest req = Rest.get(
                getActivity(),
                Rest.PATH_PUSH_NOTIFICATION + token+"&body="+body,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Sent Successfully!");

                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
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
                        Toast.makeText(getActivity(),
                                "Need to re-authenticate; logging out...",
                                Toast.LENGTH_LONG).show();
                        MainActivity.logout(getActivity());
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }
}
