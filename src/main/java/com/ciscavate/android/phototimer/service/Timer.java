package com.ciscavate.android.phototimer.service;

import java.util.Locale;

public final class Timer {
    private static int id_counter = 0;
    
    public static synchronized Timer newTimer(String name, long duration) {
        int id = id_counter ++;
        return new Timer(name, duration, id);
    }

    private final String _name;
    private final long _duration;
    private final int _id;
    private boolean _running = false;
    private long _remaining;
    private boolean _alarmOn = false;
    
    private Timer(String name, long duration, int id) {
        this._name = name;
        this._duration = duration;
        this._remaining = duration;
        this._id = id;
    }

    public String getName() {
        return _name;
    }

    public long getDuration() {
        return _duration;
    }

    public long getRemaining() {
        return _remaining;
    }

    void setRemaining(long remaining) {
        _remaining = remaining;        
    }
    
    public int getId() {
        return _id;
    }

    public boolean isRunning() {
        return _running;
    }
    
    public boolean isAlarmOn() {
        return _alarmOn;
    }

    void toggleRunning() {
        _remaining = _duration;
        _running = !isRunning();
    }
    
    @Override
    public String toString() {
        return "Timer [_name=" + _name + ", _duration=" + _duration + ", _id="
                + _id + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Timer other = (Timer) obj;
        if (_id != other._id)
            return false;
        return true;
    }

    public String getPrettyDuration() {
        return prettyTime(getDuration());
    }

    public String getPrettyRemaining() {
        return prettyTime(getRemaining());
    }
    
    private String prettyTime(long total) {
        long secondsInHour = 60 * 60;
        long hours = total / secondsInHour;
        long min = (total % secondsInHour) / 60;
        long seconds = (total % 60);
        
        return String.format(Locale.US,
                "%02d:%02d:%02d", hours, min, seconds);
    }

    void setAlarmOn(boolean isOn) {
        _alarmOn  = isOn;
    }
}
