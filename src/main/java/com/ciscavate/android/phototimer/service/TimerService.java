package com.ciscavate.android.phototimer.service;

import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.ciscavate.android.phototimer.PhotoTimer;
import com.ciscavate.android.phototimer.TimerAction;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class TimerService extends Service {
    
    private final IBinder _binder = new LocalBinder();

    private List<Timer> _timers = Lists.newArrayList();

    private Map<Timer, CountDownTimer> _countdowns = Maps.newHashMap();
    
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
        Log.i(PhotoTimer.TAG, _timers.size() + " timers in service.");
        sendTimerBroadcast(TimerAction.TIMER_ADDED);
    }
    
    public void removeTimer(Timer t) {
        stopTimer(t);
        if (_timers.remove(t)) {
            Log.d(PhotoTimer.TAG, "Removed timer: "+t);
        }
        sendTimerBroadcast(TimerAction.TIMER_REMOVED);
    }

    private void stopTimer(Timer t) {
        if (t.isRunning()) {
            toggleTimer(t.getId());
        }
    }

    public void toggleTimer(int id) {
        final Timer t = findTimer(id);
        t.toggleRunning();
        
        if (t.isRunning()) {
            // enable countdown timer...
            CountDownTimer cdTimer = new CountDownTimer(t.getDuration() * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    t.setRemaining(millisUntilFinished / 1000);
                    Log.i(PhotoTimer.TAG, "Countdown, timer: "+t.getId() + " " + t.getRemaining() + " remaining");
                    sendTimerBroadcast(TimerAction.TIMER_TICK);
                }
                
                @Override
                public void onFinish() {
                    Log.i(PhotoTimer.TAG, "BEEP BEEP BEEP");
                    // TODO fire alarm.
                    t.toggleRunning();                    
                    sendTimerBroadcast(TimerAction.ALARM_SOUNDING);
                }
            };
            _countdowns.put(t, cdTimer);
            cdTimer.start();
            sendTimerBroadcast(TimerAction.TIMER_STARTED);
        } else {
            // disable the running alarms...
            CountDownTimer cdTimer = _countdowns.get(t);
            cdTimer.cancel();
            
            _countdowns.remove(t);
            sendTimerBroadcast(TimerAction.TIMER_STOPPED);
        }
    }

    private Timer findTimer(int id) {
        for (Timer t : _timers) {
            if(t.getId() == id) {
                return t;
            }
        }
        throw new IllegalStateException("Timer does not exist: "+id);
    }

    private void sendTimerBroadcast(TimerAction action) {
        Intent tick = new Intent();
        tick.setAction(action.toString());
        sendBroadcast(tick);
    }
    
    
}
