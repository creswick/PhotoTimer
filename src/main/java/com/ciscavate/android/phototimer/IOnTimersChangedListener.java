package com.ciscavate.android.phototimer;

public interface IOnTimersChangedListener {
    void timerAdded(PositionedTimer t);
    
    void timerRemoved(PositionedTimer t);
}
