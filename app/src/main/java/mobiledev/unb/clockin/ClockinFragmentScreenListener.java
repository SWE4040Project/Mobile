package mobiledev.unb.clockin;

import android.app.Fragment;

/**
 * Created by Brent on 2017-02-13.
 */

// Container Activity must implement this interface
public interface ClockinFragmentScreenListener {
    public void onReplaceFragmentAction(Fragment fragment);
}
