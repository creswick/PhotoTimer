package com.ciscavate.android.phototimer;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class AppState {

    private final List<TimerPage> _pages = new ArrayList<TimerPage>();
    
    /**
     * The current timer page.
     * 
     * null if showing a "bumper" page, with buttons.
     */
    private TimerPage _currentPage = null;

    private int _currentPageIdx = -1;
    
    private volatile IPagesChangedListener _pagesChangedListener;

    private volatile IOnTimersChangedListener _timersChangedListener;
    
    public synchronized boolean isShowingTimerPage() {
        return null != _currentPage;
    }
    
    private void pagesChanged() {
        if (null != _pagesChangedListener) {
            _pagesChangedListener.pagesChanged();
        }
    }
    
    public synchronized boolean addPage(TimerPage page) {
        boolean res = _pages.add(page);
        page.setOnTimersChangedListener(_timersChangedListener);
        pagesChanged();
        return res;
    }
    
    public synchronized boolean removePage(TimerPage page) {
        boolean res = _pages.remove(page);
        page.clearOnTimersChangedListener();
        pagesChanged();
        return res;
    }
    
    public synchronized int getPageCount() {
        
        if (_pages.size() == 0) {
            return 1;
        } else {
            return _pages.size() + 2;
        }
    }

    /**
     * 1-indexed request.
     */
    public synchronized TimerPage getPage(int position) {
        Log.w("AppState", "getting page, pos="+position);
        return _pages.get(position - 1);
    }

    public synchronized void onPagesChanged(IPagesChangedListener pageChangedListener) {
        _pagesChangedListener = pageChangedListener;
    }
    
    public void onTimersChanged(IOnTimersChangedListener onTimersChangedListener) {
        _timersChangedListener = onTimersChangedListener;
    }
    
    /**
     * 0-indexed
     * 
     * @param idx
     */
    public synchronized void setSelectedPage(int idx) {
        if (idx == 0 || idx > _pages.size()) {
            _currentPage = null;
            _currentPageIdx = -1;
        } else {
            _currentPage = getPage(idx);
            _currentPageIdx = idx - 1;
        }
    }

    public synchronized void addTimer(float x, float y, int hours, int minutes) {
        Log.d("AppState", "adding Timer loc: ("+x+","+y+"), time = " + (hours * 60 + minutes));
        if (null == _currentPage) {
            Log.e("AppState", "We let the user add a timer to a bumper page");
        }

        _currentPage.addTimer(new PositionedTimer(x, y, hours * 60 + minutes));
    }


}
