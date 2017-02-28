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

        getPushNotificationToken();
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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String token = preferences.getString(CustomVar.NOTIFICATION_TOKEN,"");
        Log.i(TAG, "Sent token: " + token);

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

    private void getPushNotificationToken(){

        String tkn = FirebaseInstanceId.getInstance().getToken();
        Toast.makeText(getActivity(), "Current token ["+tkn+"]",
                Toast.LENGTH_LONG).show();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CustomVar.NOTIFICATION_TOKEN, tkn);
        editor.apply();
        Log.d(TAG, "Token ["+tkn+"]");

    }
}
