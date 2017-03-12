package mobiledev.unb.clockin;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

/**
 * Created by Brent on 2017-02-13.
 */

public class ClockinFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    View inflatedView = null;
    Button clockin_button;
    //Button button_location;

    final String TAG = ClockinFragment.class.getSimpleName();

    ClockinFragmentScreenListener mCallback;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest locationRequest;
    //TextView  mLatitudeText;
    //TextView mLongitudeText;

    private TextView shift_time_textview;
    private TextView shift_day_textview;
    private TextView current_time_textview;
    private TextView shift_status;

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

        // Create the location client to start receiving updates
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.inflatedView = inflater.inflate(R.layout.clockin_fragment, container, false);

        //mLatitudeText = (TextView) inflatedView.findViewById(R.id.latitude);
        //mLongitudeText = (TextView) inflatedView.findViewById(R.id.longitude);

        shift_time_textview = (TextView) inflatedView.findViewById(R.id.shift_time_textview);
        shift_day_textview = (TextView) inflatedView.findViewById(R.id.shift_day_textview);
        current_time_textview = (TextView) inflatedView.findViewById(R.id.current_time_textview);
        shift_status = (TextView) inflatedView.findViewById(R.id.shift_status);

        clockin_button = (Button) inflatedView.findViewById(R.id.clockin_button);
        clockin_button.setEnabled(false);
        setCurrentShift();

//        button_location = (Button) inflatedView.findViewById(R.id.button_location);
//        if(button_location != null) {
//            button_location.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.i(TAG, "clockin_button activated. Get location");
//                    // Send the event to the host activity
//                    getCoarseLocation();
//                }
//            });
//        }

        // Inflate the layout for this fragment
        return inflatedView;
    }

    private void setCurrentShift() {
        JsonObjectRequest req = Rest.get(
                getActivity(),
                Rest.PATH_SHIFT_CURRENT,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Shifts loaded successfully!");
                            Log.d(TAG, response.toString());
                            /*{"scheduled_day":"Friday, Mar 10",
                                "scheduled_end":"11:14 PM",
                                "scheduled_start":"6:38 PM",
                                "actual_start":"6:26 PM Friday, Mar 10",
                                "progress":0.17500198240215395,
                                "shift_notes":"asdaasdfsfsdfasdfafaasdaasdfsfsdfasdaasdfsfsdfasdfafaaasdfdaasdfsfsdfasdfafaasdaasdfsfsdfasdfafasdfasdaasdfsfsdfasdfafaasdaasdfsfsdfasdfafaasdaasdfsfsdfasdfafaasdaasdfsfsdfasdfafaasdaasdfsfsdfasdfaf",
                                "actual_end":"--",
                                "current_time":"6:45 PM"}*/

                            shift_time_textview.setText(response.get("scheduled_start") + " - " + response.get("scheduled_end"));
                            shift_day_textview.setText((String)response.get("scheduled_day"));
                            current_time_textview.setText((String)response.get("current_time"));

                            shift_status.setText("Clock In");

                            clockin_button.setEnabled(true);
                            clockin_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i(TAG, "clockin_button activated. Go to in_shift");
                                    // Send the event to the host activity
                                    clockinCall();
                                }
                            });
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
                        shift_status.setText("No Shifts");
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    private void clockinCall() {
        JsonObjectRequest req = Rest.post(
                getActivity(),
                Rest.PATH_CLOCKIN,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Clockin successful!");

                            mCallback.onReplaceFragmentAction(new InShiftFragment());
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

    public void getCoarseLocation(){

        if ( ContextCompat.checkSelfPermission( getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }else{
            Log.i(TAG, "permission true ");
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if(mLastLocation != null) {
                Log.i(TAG, "Getting location = " + mLastLocation.toString());
            }else{
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, locationRequest, this);
            }
        }
        if (mLastLocation != null) {
            Log.i(TAG, "mLastLocation = " + mLastLocation.toString());
            Log.i(TAG, "String.valueOf(mLastLocation.getLongitude()) = " + String.valueOf(mLastLocation.getLongitude()));
            //if(mLongitudeText != null && mLatitudeText != null){
            //    mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            //    mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            //}
        }

    }
    @Override
    public void onConnected(Bundle connectionHint) {
        if ( ContextCompat.checkSelfPermission( getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            Log.i(TAG, "permission = " + ContextCompat.checkSelfPermission( getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) );
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended id = " + i);
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        // Disconnecting the client invalidates it.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed = " + connectionResult.getErrorMessage());
    }


}
