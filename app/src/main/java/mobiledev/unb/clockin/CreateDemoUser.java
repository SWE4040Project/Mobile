package mobiledev.unb.clockin;

/**
 * Created by Brent on 2017-02-02.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;

public class CreateDemoUser extends Activity {
    private static final String TAG = "CreateDemoUser";
    private static final int REQUEST_SIGNUP = 0;

    EditText _usernameText;
    EditText _passwordText;
    Button _loginButton;
    TextView _createNewUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_demo);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean numCreated = preferences.getBoolean(CustomVar.NUM_CREATED,false);

        _usernameText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);
        _createNewUser = (TextView) findViewById(R.id.link_createnewuser);

        if(!numCreated) {
            _loginButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    createNewUser();
                }
            });
        }else{
            _loginButton.setText("Only one demo user can be created - 1/1");
            _loginButton.setEnabled(false);
        }

        _createNewUser.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                finish();
            }
        });
    }

    private void createNewUser() {

        if (!validate()) {
            onCreateFailed();
            return;
        }

        _loginButton.setEnabled(false);
        _createNewUser.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(CreateDemoUser.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating...");
        progressDialog.show();

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        final JSONObject params = new JSONObject();
        try{
            params.put("name", username);
            params.put("password", password);
        }catch(JSONException je){
            je.printStackTrace();
        }

        JsonObjectRequest req = Rest.post(
                this,
                Rest.PATH_CREATE,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "New User Created.");
                        progressDialog.hide();
                        try {
                            onCreateSuccess((String)params.get("name"));
                        }catch(JSONException je){
                            onCreateFailed();
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                progressDialog.hide();
                onCreateFailed();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);

    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onCreateSuccess(String name) {
        Toast.makeText(getBaseContext(),
                name + " successfully created!",
                Toast.LENGTH_LONG).show();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CustomVar.NUM_CREATED, true);
        editor.apply();

        finish();
    }

    public void onCreateFailed() {
        Toast.makeText(getBaseContext(), "Not completed. Be sure to fill out the username and password fields", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
        _createNewUser.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty()) {
            _usernameText.setError("cannot be empty.");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (password.isEmpty()) {
            _passwordText.setError("cannot be empty.");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
