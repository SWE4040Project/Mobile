package mobiledev.unb.clockin;

/**
 * Created by Brent on 2017-02-27.
 */

    import android.content.Intent;
    import android.app.Activity;
    import android.net.Uri;
    import android.os.Bundle;
    import android.os.Environment;
    import android.provider.MediaStore;
    import android.support.v4.content.FileProvider;
    import android.util.Log;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.Button;

    import java.io.File;
    import java.io.IOException;
    import java.text.SimpleDateFormat;
    import java.util.Date;

public class CameraActivity extends Activity {

    String LOG_EAC = "CameraActivity ";

    Button camera_button;
    Button email_button;
    Button back_button;
    Button email_image_button;

    String mCurrentPhotoPath;

    boolean send_image_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        camera_button = (Button) findViewById(R.id.camera_button);
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        email_button = (Button) findViewById(R.id.email_button);
        email_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                String[] addresses = {"brent.simmons@unb.ca"};
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, "CS2063 Lab 3");
                intent.putExtra(Intent.EXTRA_TEXT, "This is a text email!");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        email_image_button = (Button) findViewById(R.id.email_image_button);
        email_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_image_flag = true;
                dispatchTakePictureIntent();
            }
        });
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.i(LOG_EAC,"photoFile created.");
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(LOG_EAC,ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.i(LOG_EAC,"photoFile not null.");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "mobiledev.unb.clockin.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i(LOG_EAC,"photoURI " + photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_TAKE_PHOTO) {
            Log.i(LOG_EAC,"REQUEST_TAKE_PHOTO");
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.i(LOG_EAC,"REQUEST_TAKE_PHOTO -> RESULT_OK");
                galleryAddPic();
            }
        }
    }

    private File createImageFile() throws IOException {
        Log.i(LOG_EAC,"createImageFile");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        Log.i(LOG_EAC,"storageDir " + storageDir.toURI());
        Log.i(LOG_EAC,"mCurrentPhotoPath " + mCurrentPhotoPath);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Log.i(LOG_EAC, "galleryAddPic");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        Log.i(LOG_EAC, "contentUri " + contentUri);
        this.sendBroadcast(mediaScanIntent);

        if(send_image_flag){
            sendEmailWithImage(contentUri.toString());
            send_image_flag = false;
        }
    }

    private void sendEmailWithImage(String uri){
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("application/image");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"brent.simmons@unb.ca"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Sending test image");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "From Lab #3 CS 2063 app");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

}


