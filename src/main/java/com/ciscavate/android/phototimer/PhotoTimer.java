package com.ciscavate.android.phototimer;

import java.util.List;

import android.R.drawable;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.widget.TimePicker;

import com.ciscavate.android.phototimer.service.Timer;
import com.ciscavate.android.phototimer.service.TimerService;
import com.ciscavate.android.phototimer.service.TimerService.LocalBinder;
import com.google.common.collect.Lists;

public final class PhotoTimer extends Activity {
    public static final String TAG = "PhotoTimer";

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

    private final List<Timer> _timers = Lists.newArrayList();

    private BroadcastReceiver broadcastReceiver;
    

    protected void registerBroadcastReceiver() {
        IntentFilter ifilter = new IntentFilter();
        
        for(TimerAction action : TimerAction.values()) {
            ifilter.addAction(action.toString());
        }
        
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "received broadcast intent: "+intent);
                switch (TimerAction.valueOf(intent.getAction())){
                case TIMER_ADDED:
                case TIMER_REMOVED:
                case TIMER_STOPPED:
                case TIMER_ALARM_STOPPED:
                case TIMER_STARTED:
                case TIMER_TICK:
                case ALARM_SOUNDING:
                    updateTimerList();
                    break;
                default:
                    Log.e(TAG, "unknown broadcast intent action: "+ intent.getAction());
                }
                
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
              View rowView = inflater.inflate(R.layout.row_layout, parent, false);
              
              TextView text = (TextView)rowView.findViewById(R.id.timerRowText);
              ImageButton delBtn = (ImageButton)rowView.findViewById(R.id.timerRowDelete);
              delBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mService.removeTimer(timer);
                }
              });
              
              ImageButton playPauseBtn = (ImageButton)rowView.findViewById(R.id.timerRowButton);
              text.setText(timer.getName() + " " + timer.getPrettyRemaining());
              
              Drawable btnDrawable;
              if (timer.isRunning()) {
                 btnDrawable = getResources().getDrawable(drawable.ic_media_pause);
              } else {
                 btnDrawable = getResources().getDrawable(drawable.ic_media_play);
              }
              playPauseBtn.setImageDrawable(btnDrawable);
              
              playPauseBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleTimerRunning(timer);
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
        showDialog(R.integer.dialog_time_picker);
    }

    @Override
    protected Dialog onCreateDialog(int id, final Bundle b) {
        switch (id) {
        case R.integer.dialog_time_picker:
            OnTimeSetListener listener = new OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hours, int minutes) {
                    int duration = hours * 60 * 60 + minutes * 60;
                    mService.newTimer("new timer", duration);
                }
            };
            TimePickerDialog d = new TimePickerDialog(this, listener, 0, 10, true);
            return d;
        }
        return super.onCreateDialog(id);
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
            mService.toggleTimer(timer.getId());
        }else {
            Log.d(TAG, "service is not bound");
        }
    }

    private void updateTimerList() {
        if (null != mService) {
            Log.d(TAG, "Updating timers");
            _timers.clear();
            _timers.addAll(mService.getTimers());
            _timerListAdapter.notifyDataSetChanged();
        } else {
            Log.d(TAG, "sevrice is not bound");
        }
    }
}

