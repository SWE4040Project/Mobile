package mobiledev.unb.clockin;

/**
 * Created by Brent on 2017-02-27.
 */

    import android.app.Activity;
    import android.content.SharedPreferences;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.os.Bundle;
    import android.preference.PreferenceManager;
    import android.widget.ImageView;

    import java.io.File;

public class CameraActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String mCurrentImagePath = preferences.getString(CustomVar.CLOCKIN_IMAGE_URL, "");

        File imgFile = new File(mCurrentImagePath);

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            ImageView myImage = (ImageView) findViewById(R.id.imageViewClockin);

            myImage.setImageBitmap(myBitmap);

        }
    }


}


