package com.ciscavate.android.phototimer.service;

public final class Timer {
    private static int id_counter = 0;
    
    public static synchronized Timer newTimer(String name, int duration) {
        int id = id_counter ++;
        return new Timer(name, duration, id);
    }

    private final String _name;
    private final int _duration;
    private final int _id;
    private boolean _running = false;
    
    private Timer(String name, int duration, int id) {
        this._name = name;
        this._duration = duration;
        this._id = id;
    }

    public String getName() {
        return _name;
    }

    public int getDuration() {
        return _duration;
    }

    public int getId() {
        return _id;
    }

    public boolean isRunning() {
        return _running;
    }

    void toggleRunning() {
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
        int total = getDuration();

        int secondsInHour = 60 * 60;
        int hours = (int) Math.floor(total / secondsInHour);
        int min = (total % secondsInHour) / 60;
        int seconds = (total % 60);
        
        return String.format("%02d:%02d:%02d", hours, min, seconds);
    }
}
