package com.ciscavate.android.phototimer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

public class TimerPage {
    private final Bitmap _image;
    
    private final List<PositionedTimer> _timers = 
            new ArrayList<PositionedTimer>();

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
        _timers.add(timer);
    }
    
    public void remTimer(PositionedTimer timer) {
        _timers.remove(timer);
    }
}
