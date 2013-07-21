package com.ciscavate.android.phototimer.old;

public interface IOnTimersChangedListener {
    void timerAdded(PositionedTimer t);
    
    void timerRemoved(PositionedTimer t);
}
