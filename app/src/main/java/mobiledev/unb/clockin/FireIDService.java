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
        Log.d(TAG,"NEW Token ["+tkn+"]");
    }
}
