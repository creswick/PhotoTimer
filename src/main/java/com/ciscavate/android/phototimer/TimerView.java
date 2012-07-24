package com.ciscavate.android.phototimer;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TimerView extends SurfaceView implements SurfaceHolder.Callback {

    private final TimerPage _timerPage;
    private final TimerViewThread _thread;
    private final Rect _imageRect;
    private final Rect _canvasRect;
    
    public TimerView(PhotoTimer context, TimerPage timerPage) {
        super(context);
        // set an ID so we can look this up later.
        this.setId(R.id.imgView);
        
        // wire up the surface holder and the thread:
        getHolder().addCallback(this);
        _thread = new TimerViewThread(getHolder(), this);
        
        _timerPage = timerPage;
        
        int right = _timerPage.getImage().getWidth();
        int bottom = _timerPage.getImage().getHeight();
        _imageRect = new Rect(0, 0, right, bottom);
        Rect displayRect = getScreenDimensions(context);

        int scRight = 0;
        int scBottom = 0;
        
        if ( right / bottom >= 1) {
            scRight = displayRect.right;
            scBottom = right / displayRect.right * bottom;
        } else {
            scBottom = displayRect.bottom;
            scRight = bottom / displayRect.bottom * right;
        }
        _canvasRect = new Rect(0, 0, scRight, scBottom);
    }
    
    private Rect getScreenDimensions(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return new Rect(0, 0, metrics.widthPixels, metrics.heightPixels);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(_timerPage.getImage(), _imageRect, _canvasRect, null);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        _thread.setRunning(true);
        _thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        _thread.setRunning(false);
        while (retry) {
            try {
                // TODO this could cause deadlock if something in the thread
                // is messed up.  better to have a timeout eventually.
                _thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }        
    }
    
    private static class TimerViewThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private TimerView _panel;
        private boolean _run = false;
     
        public TimerViewThread(SurfaceHolder surfaceHolder, TimerView panel) {
            _surfaceHolder = surfaceHolder;
            _panel = panel;
            
        }
     
        public void setRunning(boolean run) {
            _run = run;
        }
     
        @Override
        public void run() {
            Canvas c;
            while (_run) {
                c = null;
                try {
                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {
                        _panel.onDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
                
            }
        }
    }
}
