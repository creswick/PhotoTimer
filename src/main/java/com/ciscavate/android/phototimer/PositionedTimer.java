package com.ciscavate.android.phototimer;

public class PositionedTimer {
    private final int _xLoc;
    
    private final int _yLoc;
    
    private final int _time;

    /**
     * Unique ID for equality comparisons.
     */
    private final long _id;

    /** 
     * Counter for timer IDs
     */
    private static int _id_counter = 0;
    
    public PositionedTimer(int xLoc, int yLoc, int time) {
        super();
        _xLoc = xLoc;
        _yLoc = yLoc;
        _time = time;
        
        synchronized (PositionedTimer.class) {
            _id = ++_id_counter;
        }
    }

    public int getxLoc() {
        return _xLoc;
    }

    public int getyLoc() {
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
