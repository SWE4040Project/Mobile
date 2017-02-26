package mobiledev.unb.clockin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Brent on 2017-02-12.
 */

public class Rest {

    private static final String TAG = Rest.class.getSimpleName();

    public static final String BASE_URL = "http://192.168.2.15:8080/rest/"; //https://swe4040.herokuapp.com/rest/

    public static final String PATH_CLOCKIN = BASE_URL + "clockin/clockin";
    public static final String PATH_CLOCKOUT = BASE_URL + "clockin/clockout";
    public static final String PATH_BREAKIN = BASE_URL + "clockin/breakin";
    public static final String PATH_BREAKOUT = BASE_URL + "clockin/breakout";
    public static final String PATH_ADDSHIFTNOTE = BASE_URL + "clockin/addshiftnote";
    public static final String PATH_EMPLOYEE_STATE = BASE_URL + "clockin/employee/state";
    public static final String PATH_LOGIN = BASE_URL + "login";
    public static final String PATH_TEST_AUTH = BASE_URL + "clockin/testauth";
    public static final String PATH_JSON = BASE_URL + "json";
    public static final String PATH_SCHEDULE = BASE_URL + "calendar/load";


    public static JsonObjectRequest get(Context context, String url, JSONObject params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String authToken = preferences.getString(CustomVar.AUTHORIZATION, "");
        final String xToken = preferences.getString(CustomVar.XSRF_TOKEN, "");
        if(authToken.equalsIgnoreCase("") || xToken.equalsIgnoreCase("")) {
            Log.i(TAG, "Empty authToken or xToken");
        }

        JsonObjectRequest req = new JsonObjectRequest(
                url,
                params,
                successListener,
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put(CustomVar.AUTHORIZATION, authToken);
                params.put(CustomVar.XSRF_TOKEN, xToken);

                return params;
            }
        };

        return req;
    }
}
