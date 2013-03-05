package com.ciscavate.android.phototimer;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class TimerService extends Service {
    
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	// TODO this should get a timer id to start / stop / etc...
    	// that id should be something it can lookup in the application's 
    	// sqlite db, and get the other details about it.
    	// Those details come into play here -- we probably deserialize into a
    	// PhotoTimer object on this side and process things here.  Every second
    	// we update the notification and the database entry.
    	
        int duration = intent.getIntExtra(getString(R.string.timerDuration), -1);
        
        if (-1 == duration) {
            Toast.makeText(getApplicationContext(), "Could not get duration", 
                    Toast.LENGTH_SHORT).show();
            return START_REDELIVER_INTENT;
        } else {
            Toast.makeText(getApplicationContext(), "Duration: " + duration, 
                    Toast.LENGTH_SHORT).show();
        }
        
        Notification notification = new Notification(R.drawable.icon, 
                "PhotoTimer: "+duration, System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, PhotoTimer.class);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, 
                notificationIntent, 0);
        notification.setLatestEventInfo(this, "Timer running",
                duration + " left.", pendingIntent);
        startForeground(42, notification);
        
        Timer t = new Timer();
        
        t.schedule(new TimerTask() {
			@Override
			public void run() {
				// update timer state.
			}
		}, 1000); // delay for one second.

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
}
