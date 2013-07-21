package com.ciscavate.android.phototimer.old;

public class PositionedTimer {
    private final float _xLoc;
    
    private final float _yLoc;
    
    private final int _time;

    /**
     * Unique ID for equality comparisons.
     */
    private final long _id;

    /** 
     * Counter for timer IDs
     */
    private static int _id_counter = 0;
    
    public PositionedTimer(float x, float y, int time) {
        super();
        _xLoc = x;
        _yLoc = y;

        _time = time;
        
        synchronized (PositionedTimer.class) {
            _id = ++_id_counter;
        }
    }
    public String toString () {
        return "[PosTimer: time="+_time+" loc: ("+_xLoc+","+_yLoc+")]";
    }
    
    public float getxLoc() {
        return _xLoc;
    }

    public float getyLoc() {
        return _yLoc;
    }

    public int getTime() {
        return _time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (_id ^ (_id >>> 32));
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
        PositionedTimer other = (PositionedTimer) obj;
        if (_id != other._id)
            return false;
        return true;
    }
}
