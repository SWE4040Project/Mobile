package mobiledev.unb.clockin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Brent on 2017-02-12.
 */

public class Rest {

    private static final String TAG = Rest.class.getSimpleName();

    public static final String BASE_URL = "https://swe4040.herokuapp.com/rest/"; //https://swe4040.herokuapp.com/rest/

    public static final String PATH_CLOCKIN = BASE_URL + "clockin/clockin";
    public static final String PATH_CLOCKOUT = BASE_URL + "clockin/clockout";
    public static final String PATH_BREAKIN = BASE_URL + "clockin/breakin";
    public static final String PATH_BREAKOUT = BASE_URL + "clockin/breakout";
    public static final String PATH_ADDSHIFTNOTE = BASE_URL + "clockin/addshiftnote";
    public static final String PATH_SHIFT_CURRENT = BASE_URL + "shifts/current";
    public static final String PATH_EMPLOYEE_STATE = BASE_URL + "employee/state";
    public static final String PATH_LOGIN = BASE_URL + "login";
    public static final String PATH_CREATE = BASE_URL + "create/employee";
    public static final String PATH_TEST_AUTH = BASE_URL + "clockin/testauth";
    public static final String PATH_JSON = BASE_URL + "json";
    public static final String PATH_SCHEDULE = BASE_URL + "calendar/load";
    public static final String PATH_PUSH_NOTIFICATION = BASE_URL + "pushnote?token=";
    public static final String PATH_EMPLOYEE_TOKEN = BASE_URL + "employee/pushnote/edit";


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

    public static JsonObjectRequest post(Context context, String url, JSONObject params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String authToken = preferences.getString(CustomVar.AUTHORIZATION, "");
        final String xToken = preferences.getString(CustomVar.XSRF_TOKEN, "");
        if(authToken.equalsIgnoreCase("") || xToken.equalsIgnoreCase("")) {
            Log.i(TAG, "Empty authToken or xToken");
        }

        JSONObject jsonParams = params;
        if(jsonParams == null){
            jsonParams = new JSONObject();
            try {
                jsonParams.put("mobile","ok");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonParams,
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

    public static JsonArrayRequest get(Context context, String url, Response.Listener<JSONArray> successListener, Response.ErrorListener errorListener){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String authToken = preferences.getString(CustomVar.AUTHORIZATION, "");
        final String xToken = preferences.getString(CustomVar.XSRF_TOKEN, "");
        if(authToken.equalsIgnoreCase("") || xToken.equalsIgnoreCase("")) {
            Log.i(TAG, "Empty authToken or xToken");
        }

        JsonArrayRequest req = new JsonArrayRequest(
                url,
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
