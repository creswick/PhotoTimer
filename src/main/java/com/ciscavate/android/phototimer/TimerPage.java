package com.ciscavate.android.phototimer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.util.Log;

public class TimerPage {
    private final Bitmap _image;
    
    private final List<PositionedTimer> _timers = 
            new ArrayList<PositionedTimer>();

    private volatile IOnTimersChangedListener _timersChangedListener;

    public TimerPage(Bitmap image) {
        super();
        _image = image;
    }

    public Bitmap getImage() {
        return _image;
    }

    public List<PositionedTimer> getTimers() {
        return _timers;
    }
    
    public void addTimer(PositionedTimer timer) {
        Log.d("TimerPage", "Added: "+timer);
        _timers.add(timer);
        
        if (null != _timersChangedListener) {
            _timersChangedListener.timerAdded(timer);
        }
    }
    
    public void remTimer(PositionedTimer timer) {
        _timers.remove(timer);

        if (null != _timersChangedListener) {
            _timersChangedListener.timerRemoved(timer);
        }
    }

    public void setOnTimersChangedListener(
            IOnTimersChangedListener timersChangedListener) {
        _timersChangedListener = timersChangedListener;
    }

    public void clearOnTimersChangedListener() {
        _timersChangedListener = null;
    }
}
