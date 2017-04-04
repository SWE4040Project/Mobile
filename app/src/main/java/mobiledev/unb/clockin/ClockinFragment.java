package mobiledev.unb.clockin;

import android.Manifest;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.markushi.ui.CircleButton;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Brent on 2017-02-13.
 */

public class ClockinFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    View inflatedView = null;
    CircleButton clockin_button;
    CircleButton clockin_button_disabled;
    Button button_location;
    private TextView shift;

    private ProgressDialog pDialog;

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

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        showpDialog();

        shift = (TextView) inflatedView.findViewById(R.id.shift);
        clockin_button = (CircleButton) inflatedView.findViewById(R.id.clockin_button);
        clockin_button_disabled = (CircleButton) inflatedView.findViewById(R.id.clockin_button_disabled);
        clockin_button.setEnabled(false);
        clockin_button_disabled.setEnabled(false);
        clockin_button.setVisibility(View.GONE);
        setCurrentShift();

        mClock = (ClockView) inflatedView.findViewById(R.id.clock);

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

                            shift.setText(response.get("scheduled_start") + " - " + response.get("scheduled_end")
                                    + "\n" + (String)response.get("scheduled_day"));
                            //current_time_textview.setText((String)response.get("current_time"));

                            clockin_button.setEnabled(true);
                            clockin_button_disabled.setVisibility(View.VISIBLE);
                            clockin_button_disabled.setVisibility(View.GONE);
                            clockin_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i(TAG, "clockin_button activated. Go to in_shift");
                                    // Send the event to the host activity
                                    setCoarseLocation();
                                }
                            });
                            hidepDialog();
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                            hidepDialog();
                            Toast.makeText(getActivity(),
                                    "Need to re-authenticate; logging out...",
                                    Toast.LENGTH_LONG).show();
                            MainActivity.logout(getActivity());
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hidepDialog();
                        shift.setText("No shifts to clock in for at this time");
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
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

    private void clockinCall() {

        showpDialog();
        JsonObjectRequest req = Rest.post(
                getActivity(),
                Rest.PATH_CLOCKIN,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Clockin successful!");

                            hidepDialog();
                            mCallback.onReplaceFragmentAction(new InShiftFragment());
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                            hidepDialog();
                            Toast.makeText(getActivity(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, "Error: " + error.getMessage());
                        hidepDialog();
                        Toast.makeText(getActivity(),
                                "Need to re-authenticate; logging out...",
                                Toast.LENGTH_LONG).show();
                        MainActivity.logout(getActivity());
                    }
                });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    public void setCoarseLocation(){

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
            Log.i(TAG, "mLastLocation = " + mLastLocation.toString());;
            try {
                lng = String.valueOf(mLastLocation.getLongitude());
                lat = String.valueOf(mLastLocation.getLatitude());
                if (lng == null || lat == null) {
                    Log.e(TAG, "Location could not be read at this time!!");
                    return;
                }
                Log.i(TAG, "lng = " + lng);
                Log.i(TAG, "lat = " + lat);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                final SharedPreferences.Editor editor = preferences.edit();
                editor.putString(CustomVar.LONGITUDE, String.valueOf(mLastLocation.getLongitude()));
                editor.putString(CustomVar.LATITUDE, String.valueOf(mLastLocation.getLatitude()));
                editor.apply();

                dispatchTakePictureIntent();

            }catch(Exception e){
                Log.e(TAG,e.getMessage());
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
        Log.i(TAG, "onStop called. mGoogleApiClient stopped.");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed = " + connectionResult.getErrorMessage());
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }



    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;

    public void dispatchTakePictureIntent() {
        Log.i(TAG,"Camera - dispatchTakePictureIntent");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.i(TAG,"photoFile created.");
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG,ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.i(TAG,"photoFile not null.");
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "mobiledev.unb.clockin.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i(TAG,"photoURI " + photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_TAKE_PHOTO) {
            Log.i(TAG,"REQUEST_TAKE_PHOTO");
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.i(TAG,"REQUEST_TAKE_PHOTO -> RESULT_OK");
                saveCurrentImageURL();
            }
        }
    }

    private File createImageFile() throws IOException {
        Log.i(TAG,"createImageFile");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        Log.i(TAG,"storageDir " + storageDir.toURI());
        Log.i(TAG,"mCurrentPhotoPath " + mCurrentPhotoPath);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void saveCurrentImageURL() {
        Log.i(TAG, "displayImage");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CustomVar.CLOCKIN_IMAGE_URL, mCurrentPhotoPath);
        editor.apply();
        Log.i(TAG, "IMAGE URL: " + mCurrentPhotoPath);
        Toast.makeText(getActivity(),"Clockin Picture Successful.",Toast.LENGTH_SHORT);

        clockinCall();
    }
}
