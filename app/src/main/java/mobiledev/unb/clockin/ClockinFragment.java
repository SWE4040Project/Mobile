package mobiledev.unb.clockin;

import android.*;
import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DebugUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import at.markushi.ui.CircleButton;

/**
 * Created by Brent on 2017-02-13.
 */

public class ClockinFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    View inflatedView = null;
    CircleButton clockin_button;
    Button button_location;

    final String TAG = ClockinFragment.class.getSimpleName();

    ClockinFragmentScreenListener mCallback;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest locationRequest;
    TextView  mLatitudeText;
    TextView mLongitudeText;
    private ClockView mClock;
    protected String lng, lat;

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

        clockin_button = (CircleButton) inflatedView.findViewById(R.id.clockin_button);
        if(clockin_button != null) {
            clockin_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "clockin_button activated. Go to in_shift");

                    //getCoarseLocation(); ******Commented out for emulator testing purposes

                    // Send the event to the host activity
                    mCallback.onReplaceFragmentAction(new InShiftFragment());
                }
            });
        }

        mClock = (ClockView) inflatedView.findViewById(R.id.clock);

        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mClock.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mClock.pause();
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
            if(mLongitudeText != null && mLatitudeText != null){
                //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude())); *****Don't need at the moment. Stored as String below
                //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));   *****Don't need at the moment. Stored as String below

                lng = String.valueOf(mLastLocation.getLongitude());
                lat = String.valueOf(mLastLocation.getLatitude());
                Log.i(TAG, "lng = " + lng);
                Log.i(TAG, "lat = " + lat);


            }
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
