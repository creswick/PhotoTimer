package com.ciscavate.android.phototimer.service;

import java.io.IOException;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.ciscavate.android.phototimer.TimerActions;
import com.google.common.collect.Maps;

public final class TimerService extends Service {
    
    static final String TAG = "TimerService";
    
    private final IBinder _binder = new LocalBinder();

    private Map<Timer, CountDownTimer> _countdowns = Maps.newHashMap();

    private MediaPlayer _player = new MediaPlayer();

    private Vibrator _vibrator;

    private final long[] _vibratorPattern = new long[] { 0l, 200l, 500l };
    
    @Override
    public void onCreate(){
        Log.d(TAG, "onCreate()");
        _vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy()");
    }

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
    public int onStartCommand(Intent intent, int flags, int startid) {
        return Service.START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }
    
    private void alarmTimer(Timer t) {
        t.setAlarmOn(true);
    }
    
    public void stopAlarm(Timer timer) {
        if (timer.isRunning()) {
            toggleTimer(timer);
        }
        
        if (null != _player) {
            _player.stop();
        }
        
        if (null != _vibrator) {
            _vibrator.cancel();
        }
        
        timer.setAlarmOn(false);
        sendTimerBroadcast(TimerActions.TIMER_ALARM_STOPPED, timer);
    }

    public void toggleTimer(final Timer t) {
        t.toggleRunning();
        
        if (t.isRunning()) {
            // enable countdown timer...
            CountDownTimer cdTimer = new CountDownTimer(t.getDuration() * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    t.setRemaining(millisUntilFinished / 1000);
                    Log.i(TAG, "Countdown, timer: "+t.getId() + " " + t.getRemaining() + " remaining");
                    sendTimerBroadcast(TimerActions.TIMER_TICK, t);
                }
                
                @Override
                public void onFinish() {
                    Log.i(TAG, "BEEP BEEP BEEP");
                    
                    playAlarm(t);
                    
                    t.toggleRunning();                    
                    sendTimerBroadcast(TimerActions.ALARM_SOUNDING, t);
                }
            };
            _countdowns.put(t, cdTimer);
            cdTimer.start();
            sendTimerBroadcast(TimerActions.TIMER_STARTED, t);
        } else {
            // disable the running alarms...
            CountDownTimer cdTimer = _countdowns.get(t);
            if (null != cdTimer) {
                cdTimer.cancel();
            }
            _countdowns.remove(t);
            sendTimerBroadcast(TimerActions.TIMER_STOPPED, t);
        }
    }

    protected void playAlarm(Timer timer) {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if(alert == null){
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            if(alert == null){  // I can't see this ever being null (as always have a default notification) but just incase
                // alert backup is null, using 2nd backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
            }
        }
        
        // set the data store value:
        alarmTimer(timer);
        // ping the UI:
        sendTimerBroadcast(TimerActions.ALARM_SOUNDING, timer);
        
        // NOW start being obnoxious:
        //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alert);

        try {
            _player.setDataSource(getApplicationContext(), alert);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        _player.setAudioStreamType(AudioManager.STREAM_ALARM);
        // keep playing the sound for ever
        _player.setLooping(true);
        try {
            _player.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        _player.start();
        _vibrator.vibrate(_vibratorPattern, 0);
    }
    
    private void sendTimerBroadcast(TimerActions action, Timer timer) {
        Intent tick = new Intent();
        tick.putExtra(Timer.class.toString(), timer.toJSON());
        tick.setAction(action.toString());
        sendBroadcast(tick);
    }
    
    
}
