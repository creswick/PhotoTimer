package com.ciscavate.android.phototimer.service;

import java.util.List;

import com.ciscavate.android.phototimer.PhotoTimer;
import com.google.common.collect.Lists;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public final class TimerService extends Service {
    
    private final IBinder _binder = new LocalBinder();

    private List<Timer> _timers = Lists.newArrayList();
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public TimerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }
    
    public List<Timer> getTimers() {
        return _timers;
    }
    
    public void newTimer(String name, int duration) {
        Timer t = Timer.newTimer(name, duration);
        Log.i(PhotoTimer.TAG, "created timer: "+t);
        _timers.add(t);
    }
    
    public void removeTimer(Timer t) {
        if (_timers.remove(t)) {
            Log.d(PhotoTimer.TAG, "Removed timer: "+t);
        }
    }

    public void toggleTimer(int id) {
        Timer t = findTimer(id);
        t.toggleRunning();
    }

    private Timer findTimer(int id) {
        for (Timer t : _timers) {
            if(t.getId() == id) {
                return t;
            }
        }
        throw new IllegalStateException("Timer does not exist: "+id);
    }
    
    
}
