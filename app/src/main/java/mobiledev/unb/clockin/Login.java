package mobiledev.unb.clockin;

/**
 * Created by Brent on 2017-02-02.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import android.content.Intent;
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

public class Login extends Activity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    EditText _usernameText;
    EditText _passwordText;
    Button _loginButton;
    TextView _createNewUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        _usernameText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);
        _createNewUser = (TextView) findViewById(R.id.link_createnewuser);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _createNewUser.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                //createNewUser();
                Intent intent = new Intent(Login.this, CreateDemoUser.class);
                startActivity(intent);
            }
        });
    }

   public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(Login.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        //authenticate with server
        Log.i(TAG, "username = " + username + " email = " + password);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();

        JSONObject params = new JSONObject();
        try{
            params.put("username", username);
            params.put("password", password);
        }catch(JSONException je){
            je.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(
            Request.Method.POST,
            Rest.PATH_LOGIN,
            params,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        VolleyLog.v("Response:%n %s", response.toString(4));

                        String jwtToken = response.getString(CustomVar.AUTHORIZATION);
                        String xToken = response.getString(CustomVar.XSRF_TOKEN);

                        Log.i(TAG,CustomVar.AUTHORIZATION +" = "+ jwtToken);
                        Log.i(TAG,CustomVar.XSRF_TOKEN +" = "+ xToken);

                        editor.putString(CustomVar.AUTHORIZATION, jwtToken);
                        editor.putString(CustomVar.XSRF_TOKEN, xToken);
                        editor.apply();

                        int i = jwtToken.lastIndexOf('.');
                        String withoutSignature = jwtToken.substring(0, i+1);
                        Jwt<Header,Claims> readToken = Jwts.parser().parseClaimsJwt(withoutSignature);
                        Claims claims = readToken.getBody();
                        String name = (String) claims.get(CustomVar.EMPLOYEE_NAME);
                        editor.putString(CustomVar.EMPLOYEE_NAME, name);
                        editor.apply();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    progressDialog.hide();
                    onLoginSuccess();
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                progressDialog.hide();
                onLoginFailed();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);

        //open MainActivity class
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public void onCreateSuccess(String name) {
        Toast.makeText(getBaseContext(),
                name + " successfully created!",
                Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
        _createNewUser.setEnabled(true);
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
