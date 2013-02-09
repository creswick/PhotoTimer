package com.ciscavate.android.phototimer;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

public class PhotoTimer extends Activity {

    private static String TAG = "PhotoTimer";
    
    private Toast _addTimerToast;

    private AppState _appState;
    
    // XXX this makes me oh, so, so sad :(
    private float _last_x = 0;
    private float _last_y = 0;
    
    
    private PagerAdapter _pagerAdapter;
 
    
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
        _addTimerToast =
                Toast.makeText(this, R.string.add_timer_toast_txt, 
                        Toast.LENGTH_SHORT);
        
        setContentView(R.layout.main);
        
        _appState = new AppState();
        _pagerAdapter = new CiscavatePagerAdapter(this, _appState);
        _appState.onPagesChanged(new IPagesChangedListener() {
            @Override
            public void pagesChanged() {
                _pagerAdapter.notifyDataSetChanged();
            }
        });
        
        _appState.onTimersChanged(new IOnTimersChangedListener() {
            @Override
            public void timerAdded(PositionedTimer t) {
                Intent intent = new Intent(PhotoTimer.this, TimerService.class);
                intent.putExtra(getString(R.string.timerDuration), t.getTime());
                startService(intent);
            }

            @Override
            public void timerRemoved(PositionedTimer t) {
                // TODO Auto-generated method stub
            }
        });
        
        ViewPager pager = (ViewPager) findViewById(R.id.timerPager);
        pager.setAdapter(_pagerAdapter);
        pager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int idx) {
                Log.w(TAG, "onPageSelected fired.  idx="+idx);
                _appState.setSelectedPage(idx);
                // Android 3+: PhotoTimer.this.invalidateOptionsMenu();
            }
            
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {}
            
            @Override
            public void onPageScrollStateChanged(int idx) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.w(TAG, "onPrepareOptionsMenu");
        MenuItem addItem = menu.findItem(R.id.opt_menu_add_timer);
        addItem.setEnabled(_appState.isShowingTimerPage());
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.opt_menu_new_timer_camera:
                getPhoto();
                return true;
            case R.id.opt_menu_new_timer_gallery:
                getImage();
                return true;
            case R.id.opt_menu_add_timer:
                onOptMenuAddTimer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id, final Bundle b) {
        switch (id) {
        case R.integer.dialog_time_picker:
            OnTimeSetListener listener = new OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hours, int minutes) {
                    _appState.addTimer(_last_x, _last_y, hours, minutes);
                }
            };
            TimePickerDialog d = new TimePickerDialog(this, listener, 0, 10, true);
            return d;
        }
        return super.onCreateDialog(id);
    }

//    @Override
//    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
//        switch (id) {
//        case R.integer.dialog_time_picker:
//            // here, I could set the x,y coords properly, reading from the bundle.
//            // but the silly TimePickerDialog doesn't provide access to it's f-ing listener.
//            // I also can't create an f-ing dialog without passing a listener to the constructor
//            // so I'm screwed.
//            //
//            //  Happy days when I take the time to sit down and replace that POS,
//            //  or switch to DialogFragments and see if that's any better.
//            break;
//        }
//        super.onPrepareDialog(id, dialog, args);
//    }

    private void onOptMenuAddTimer() {
        _addTimerToast.show();
        
        //ViewPager pager = (ViewPager)findViewById(R.id.timerPager);
        View imageView = findViewById(R.id.imgView);
        
        if (null == imageView) {
            Log.e(TAG, "not looking at an image");
            return;
        }
        
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                Resources res = getResources();
                
                Log.d(TAG, "Touch event at: ("+x+","+y+")");
                Bundle b = new Bundle();
                b.putFloat(res.getString(R.string.coord_x), x);
                b.putFloat(res.getString(R.string.coord_y), y);
                
                showDialog(R.integer.dialog_time_picker,b);
                
                _last_x = x;
                _last_y = y;
                return false;
            }
        });
    }
    
    public void getImage() {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, R.integer.image_selected);
    }
    
    
    public void getPhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, R.integer.take_picture);
    }

    protected void onActivityResult(int requestCode, int resultCode,
            Intent itent) {
        super.onActivityResult(requestCode, resultCode, itent);

        Bitmap imageBmp = null;

        switch (requestCode) {
        case R.integer.image_selected:
            if (resultCode != RESULT_OK) {
                return;
            }
            Uri selectedImageUri = itent.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor =
                    getContentResolver().query(selectedImageUri,
                            filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            imageBmp = BitmapFactory.decodeFile(filePath);

            break;
        case R.integer.take_picture:
            if (resultCode != RESULT_OK) {
                return;
            }
            imageBmp = (Bitmap)itent.getExtras().get("data");
            break;
        }
        
        if (null == imageBmp) {
            Log.e(TAG, "Error loading image from camera or gallery");
        }
        // add new timer page
        _appState.addPage(new TimerPage(imageBmp));
    }
}

