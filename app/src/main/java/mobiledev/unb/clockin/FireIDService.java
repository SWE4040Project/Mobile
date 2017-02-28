package mobiledev.unb.clockin;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Brent on 2017-02-17.
 */

public class FireIDService extends FirebaseInstanceIdService {

    private final String TAG = "FireIDService";

    @Override
    public void onTokenRefresh() {
        String tkn = FirebaseInstanceId.getInstance().getToken();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if( !preferences.getString(CustomVar.NOTIFICATION_TOKEN,"").equals(tkn)){
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(CustomVar.NOTIFICATION_TOKEN, tkn);
            Log.d(TAG,"Generating NEW push notification token");
        }
        Log.d(TAG,"Token ["+tkn+"]");
    }
}
