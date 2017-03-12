package mobiledev.unb.clockin;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Log.i(TAG, "onNavigationItemSelected: " + item.getTitle() + " : " + item.toString());

        if (id == R.id.nav_clockin) {
            Log.i(TAG, "Clock in here.");
            onReplaceFragmentAction(new ClockinFragment());

        } else if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_myshifts) {
            Log.i(TAG, "Load my shifts");
            Intent intent = new Intent(MainActivity.this, ScheduleListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_schedule) {
            Log.i(TAG, "Load weekly schedule");
            Intent intent = new Intent(MainActivity.this, ScheduleListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Log.i(TAG, "Selecting nav_manage");
            Intent intent = new Intent(MainActivity.this, TestRestActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_location) {
            Log.i(TAG, "Getting location");
            onReplaceFragmentAction(new ClockinFragment());
        } else if (id == R.id.nav_send) {
            getPushNotificationToken();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getPushNotificationToken(){

            String tkn = FirebaseInstanceId.getInstance().getToken();
            Toast.makeText(MainActivity.this, "Current token ["+tkn+"]",
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "Token ["+tkn+"]");

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

                                Log.d(TAG, ""+response.getInt("State"));

                                // Create new fragment and transaction
                                Fragment newFragment = null;

                                int state = response.getInt("State");
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

                            onReplaceFragmentAction(newFragment);

                        } catch (JSONException e) {
                            Log.i(TAG, e.getMessage());
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                        progressDialog.hide();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    public void onReplaceFragmentAction(Fragment fragment) {
        // Transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}
