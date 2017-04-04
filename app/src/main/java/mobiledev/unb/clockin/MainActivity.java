package mobiledev.unb.clockin;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ClockinFragmentScreenListener {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        String name = preferences.getString(CustomVar.EMPLOYEE_NAME,"name");
        TextView empName = (TextView) navView.getHeaderView(0).findViewById(R.id.nav_header_name);
        TextView empEmail = (TextView) navView.getHeaderView(0).findViewById(R.id.nav_header_email);
        if(name!=null){
            empName.setText(name);
            empEmail.setText(name+"@tracker.ca");
        }
        setPushNotificationCall();
        loadClockinFragment();

    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } if (getFragmentManager().getBackStackEntryCount()!=0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void logout(Activity activity) {
        Log.i("Logout", "Logging out...");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CustomVar.AUTHORIZATION, null);
        editor.putString(CustomVar.XSRF_TOKEN, null);
        editor.apply();

        Intent intent = new Intent(activity, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Log.i(TAG, "onNavigationItemSelected: " + item.getTitle() + " : " + item.toString());

        if (id == R.id.nav_clockin) {
            Log.i(TAG, "Clock in here.");
            loadClockinFragment();
        } else if (id == R.id.nav_camera) {
            // Handle the camera action
            Log.i(TAG, "Camera screen.");
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_location) {
            Log.i(TAG, "Load my last clockin location");
            externalLoadLocation();
        } else if (id == R.id.nav_myshifts) {
            Log.i(TAG, "Load my shifts");
            Intent intent = new Intent(MainActivity.this, ScheduleListActivity.class);
            intent.putExtra("viewType", 2);
            startActivity(intent);
        } else if (id == R.id.nav_schedule) {
            Intent intent = new Intent(MainActivity.this, ScheduleListActivity.class);
            intent.putExtra("viewType", 0);
            startActivity(intent);
        } else if (id == R.id.nav_schedule_working) {
            Intent intent = new Intent(MainActivity.this, ScheduleListActivity.class);
            intent.putExtra("viewType", 1);
            startActivity(intent);
        }else if (id == R.id.nav_schedule_worked) {
            Intent intent = new Intent(MainActivity.this, ScheduleListActivity.class);
            intent.putExtra("viewType", 3);
            startActivity(intent);
        }else if (id == R.id.nav_manage) {
            Log.i(TAG, "Selecting nav_manage");
            Intent intent = new Intent(MainActivity.this, TestRestActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            onReplaceFragmentAction(new PushNotificationFragment());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void externalLoadLocation() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String lng = preferences.getString(CustomVar.LONGITUDE, "");
        final String lat = preferences.getString(CustomVar.LATITUDE, "");
        intent.setData(Uri.parse("geo:0,0?q=" + (lat+","+lng)));
        try {
            Log.i(TAG, "lng = " + lng + " && lat = " + lat);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadClockinFragment(){

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        JsonObjectRequest req = Rest.get(
                this,
                Rest.PATH_EMPLOYEE_STATE,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, response.toString());

                                Log.d(TAG, ""+response.getInt("state"));

                                // Create new fragment and transaction
                                Fragment newFragment = null;

                                int state = response.getInt("state");
                                switch(state){
                                case 0: newFragment = new ClockinFragment();
                                    Log.d(TAG, "Clockin Fragment");
                                    break;
                                case 1: newFragment = new InShiftFragment();
                                    Log.d(TAG, "InShift Fragment");
                                    break;
                                case 2: newFragment = new OnBreakFragment();
                                    Log.d(TAG, "OnBreak Fragment");
                                    break;
                                default: newFragment = new ClockinFragment();
                                    Log.d(TAG, "Default Fragment");
                                    break;
                            }

                            progressDialog.hide();
                            onReplaceFragmentAction(newFragment);

                        } catch (JSONException e) {
                            Log.i(TAG, e.getMessage());
                            Toast.makeText(getApplicationContext(),
                                    "Need to re-authenticate; logging out...",
                                    Toast.LENGTH_LONG).show();
                            MainActivity.logout(MainActivity.this);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.getMessage());
                progressDialog.hide();
                Toast.makeText(getApplicationContext(),
                        "Need to re-authenticate; logging out...",
                        Toast.LENGTH_LONG).show();
                MainActivity.logout(MainActivity.this);            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    private void setPushNotificationCall() {
        String tkn = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if( !preferences.getString(CustomVar.NOTIFICATION_TOKEN,"").equals(tkn)){
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(CustomVar.NOTIFICATION_TOKEN, tkn);
            Log.d(TAG,"Generating NEW push notification token");

            updateEmployeePushNotification();
        }
    }

    private void updateEmployeePushNotification(){

        String tkn = FirebaseInstanceId.getInstance().getToken();
        JSONObject params = new JSONObject();
        try{
            params.put("push_notification_token", tkn);
        }catch(JSONException je){
            je.printStackTrace();
        }
        JsonObjectRequest req = Rest.post(
                this,
                Rest.PATH_EMPLOYEE_TOKEN,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG,error.getStackTrace().toString());
                        VolleyLog.e(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                "Need to re-authenticate; logging out...",
                                Toast.LENGTH_LONG).show();
                        MainActivity.logout(MainActivity.this);
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    public void onReplaceFragmentAction(Fragment fragment) {
        // Transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and do not add the transaction to the back stack
        transaction.replace(R.id.content_frame, fragment);

        // Commit the transaction
        transaction.commit();
    }
}

