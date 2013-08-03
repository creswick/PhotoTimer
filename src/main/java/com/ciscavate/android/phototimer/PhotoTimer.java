package com.ciscavate.android.phototimer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.List;

import android.R.drawable;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ciscavate.android.phototimer.dialogs.SpinnerPicker;
import com.ciscavate.android.phototimer.dialogs.SpinnerPicker.ITimePickerHandler;
import com.ciscavate.android.phototimer.service.Timer;
import com.ciscavate.android.phototimer.service.TimerService;
import com.ciscavate.android.phototimer.service.TimerService.LocalBinder;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public final class PhotoTimer extends Activity implements ITimePickerHandler {
    static final String TAG = "PhotoTimer";

    private static final String TIMER_STORAGE_FILE = "timeStorage.json";
    
    private TimerService mService;
    private boolean mBound = false;
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Binding to service...");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private ArrayAdapter<Timer> _timerListAdapter;

    private List<Timer> _timers = Lists.newArrayList();

    private BroadcastReceiver broadcastReceiver;
    

    protected void registerBroadcastReceiver() {
        IntentFilter ifilter = new IntentFilter();
        
        for(TimerActions action : TimerActions.values()) {
            ifilter.addAction(action.toString());
        }
        
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                
                Log.i(TAG, "received broadcast intent: "+intent);
                String json = intent.getStringExtra(Timer.class.toString());
                if (null == json) {
                    Log.e(TAG, "Received null json timer string");
                    return;
                }
                Timer timer = Timer.fromJSON(json);
                switch (TimerActions.valueOf(intent.getAction())){
                case TIMER_REMOVED:
                    _timers.remove(timer);
                    break;
                case TIMER_ADDED:
                    _timers.add(timer);
                    break;
                case TIMER_STOPPED:
                case TIMER_ALARM_STOPPED:
                case TIMER_STARTED:
                case TIMER_TICK:
                case ALARM_SOUNDING:
                    // yes, this is weird.  _timers will treat the timer object
                    // as equal to something that's in the list alread, but 
                    // other aspects of the timer object need updating, so we 
                    // bump it out & in, then fire a UI update:
                    if (_timers.remove(timer)) {
                        _timers.add(timer);
                    }
                    break;
                default:
                    Log.e(TAG, "unknown broadcast intent action: "+ intent.getAction());
                }
                updateTimerList();
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(ifilter));
    }
    
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate(...)");
        setContentView(R.layout.main);
        loadTimers();

        _timerListAdapter = new ArrayAdapter<Timer>(this, 
                        R.layout.row_layout, _timers) {

          @Override
          public View getView(int position, View convertView,
                              ViewGroup parent) {
              final Timer timer = _timers.get(position);
              // TODO refactor, per:
              //return new TimerRowView(timer, mService, this);
              
              LayoutInflater inflater = (LayoutInflater) PhotoTimer.this
                      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
              final View rowView = inflater.inflate(R.layout.row_layout, parent, false);
              
              TextView text = (TextView)rowView.findViewById(R.id.timerRowText);
              ImageButton delBtn = (ImageButton)rowView.findViewById(R.id.timerRowDelete);
              delBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeTimer(timer);
                }
              });
              
              ImageButton playPauseBtn = (ImageButton)rowView.findViewById(R.id.timerRowButton);
              text.setText(timer.getName() + " " + timer.getPrettyRemaining());

              final Drawable oldColor = rowView.getBackground();
              Drawable btnDrawable;
              if (timer.isAlarmOn()) {
                 btnDrawable = getResources().getDrawable(drawable.ic_popup_reminder);
                 rowView.setBackgroundColor(Color.RED);
              } else if (timer.isRunning()) {
                 btnDrawable = getResources().getDrawable(drawable.ic_media_pause);
              } else {
                 btnDrawable = getResources().getDrawable(drawable.ic_media_play);
              }
              
              
              playPauseBtn.setImageDrawable(btnDrawable);
              
              playPauseBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (timer.isAlarmOn()) {
                        mService.stopAlarm(timer);
                        rowView.setBackgroundDrawable(oldColor);
                    } else {
                        toggleTimerRunning(timer);
                    }
                }
              });
              
              return rowView;
          }
        };
        
        ListView listView = (ListView) findViewById(R.id.timerList);
        listView.setAdapter(_timerListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
        bindToService();
    }

    
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");

        // Start the timer service so it is not auto-collected when unbound.
        Intent startService = new Intent(this, TimerService.class);
        startService(startService);

        registerBroadcastReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        unregisterReceiver(broadcastReceiver);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
        unbindFromService();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        storeTimers();
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
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.opt_menu_new_timer_camera:
                //getPhoto();
                return true;
            case R.id.opt_menu_new_timer_gallery:
                //getImage();
                return true;
            case R.id.opt_menu_add_timer:
                onOptMenuAddTimer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void onOptMenuAddTimer() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        SpinnerPicker newFragment = new SpinnerPicker();
        newFragment.setTimePickerHandler(this);
        newFragment.show(ft, "dialog");
    }

    @Override
    public void onTimeSetHandler(String name, int hour, int min, int sec) {
        long duration = hour * (60 * 60);
        duration += (min * 60);
        duration += sec;
        
        newTimer(name, duration);
    }
    
    private void bindToService() {
        // Bind to TimerService
        Intent intent = new Intent(this, TimerService.class);
        boolean isBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (!isBound) {
            Log.e(TAG, "Could not bind service");
        } else {
            updateTimerList();
        }
    }
    
    private void unbindFromService() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private void toggleTimerRunning(Timer timer) {
        if (null != mService) {
            mService.toggleTimer(timer);
        }else {
            Log.d(TAG, "service is not bound");
        }
    }
    
    public void newTimer(String name, long duration) {
        Timer t = Timer.newTimer(_timers, name, duration);
        Log.i(TAG, "created timer: "+t);
        _timers.add(t);
        updateTimerList();
        Log.i(TAG, _timers.size() + " timers exist.");
    }
    
    private void updateTimerList() {
        _timerListAdapter.notifyDataSetChanged();
    }

    private void loadTimers() {
        _timers.clear();
        _timers.addAll(loadTimers(TIMER_STORAGE_FILE));
    }
    
    private void storeTimers() {
        storeTimers(_timers, TIMER_STORAGE_FILE);
    }
    
    private void storeTimers(List<Timer> timers, String file) {
        FileOutputStream out = null;  
        OutputStreamWriter outWriter = null;
        try {
            out = openFileOutput(file, Context.MODE_PRIVATE);
            
            Gson gson = new Gson();
            
            String json = gson.toJson(timers);
            outWriter = new OutputStreamWriter(out);
            
            outWriter.write(json);
        } catch (FileNotFoundException e) {
            Log.e(TAG, 
                    "Could not find file to store timers: "+e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, 
                    "Exception writing timers to file: "+e.getMessage());
        } finally {
            if (null != outWriter) {
                try {
                    outWriter.close();
                } catch (IOException e) {
                    Log.e(TAG,
                            "Exception closing writer: "+e.getMessage());
                }
            }
            
            if (null != out){
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG,
                            "IO Exception closing file after store: "+e.getMessage());
                }
            }
        }
    }

    private List<Timer> loadTimers(String file) {
        List<Timer> timers = null;
        FileInputStream in = null;
        try {
            in = openFileInput(file);
            Gson gson = new Gson();
            
            Type listType = new TypeToken<List<Timer>>() {}.getType();
            timers = gson.fromJson(new InputStreamReader(in), listType);
            
        } catch (FileNotFoundException e) {
            Log.d(TAG,
                    "could not find timers file to load: "+e.getMessage());
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG,
                            "IO Exception closing file after load: "+e.getMessage());
                }
            }
        }
        if (null == timers) {
            if (null != _timers) {
                timers = _timers;
            } else {
                timers = Lists.newArrayList();
            }
        }
        return timers;
    }

    private void removeTimer(final Timer timer) {
        // stop the alarm, just in case this timer is going off:
        mService.stopAlarm(timer);
        
        _timers.remove(timer);
        updateTimerList();
    }
    
}

