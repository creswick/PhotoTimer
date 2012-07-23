package com.ciscavate.android.phototimer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class PhotoTimer extends Activity {

    private static String TAG = "PhotoTimer";

    private final List<TimerPage> _pages = new ArrayList<TimerPage>();
    
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        
        setContentView(R.layout.main);
        
        PagerAdapter awesomeAdapter = new CiscavatePagerAdapter(this, _pages);
        ViewPager awesomePager = (ViewPager) findViewById(R.id.timerPager);
        awesomePager.setAdapter(awesomeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_timer_camera:
                // TODO
                return true;
            case R.id.new_timer_gallery:
                getImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void getImage() {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, R.integer.image_selected);
    }
    
//    private void registerTouchHandlers(ImageView view) {
////        view.setOnTouchListener(new OnT)
//        
//        // see ViewPager: http://developer.android.com/reference/android/support/v4/view/ViewPager.html
//    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

        switch(requestCode) { 
        case R.integer.image_selected:
            if(resultCode == RESULT_OK){  
                Uri selectedImageUri = imageReturnedIntent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                Bitmap selectedImagebmp = BitmapFactory.decodeFile(filePath);
                
                // add new timer page
                synchronized (_pages) {
                    _pages.add(new TimerPage(selectedImagebmp));                    
                }
            }
        }
    }
}

