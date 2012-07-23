package com.ciscavate.android.phototimer;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class CiscavatePagerAdapter extends PagerAdapter {

    private final List<TimerPage> _pages;
    private final PhotoTimer _context;
    
    private final View.OnClickListener _onClickListener =
            new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.camera_button:
                // _context.getImage();    
                break;
            case R.id.gallery_button:
                _context.getImage();
                break;
            default:
                // do nothing.
                break;
            }
        }
    };
    
    private final LayoutInflater _inflater;
    
    public CiscavatePagerAdapter(PhotoTimer ctx, List<TimerPage> pages) {
        super();
        _context = ctx;
        _pages = pages;
        
        _inflater = (LayoutInflater)_context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        synchronized (_pages) {
            if (_pages.size() == 0) {
                return 1;
            } else {
                return _pages.size() + 2;
            }
        }
    }
    
    @Override
    public Object instantiateItem(View collection, int position) {
        View view;
        synchronized (_pages) {
            // If this is the first or last position, show buttons:
            if (position == 0 || position > _pages.size()) {
                view = createButtonView();
            } else {
                view = createTimerView(_context, _pages.get(position - 1));
            }
        }

        ((ViewPager) collection).addView(view, 0);
        return view;
    }
    
    private View createTimerView(PhotoTimer context, TimerPage timerPage) {
        View v = _inflater.inflate(R.layout.timer_view, null);
        ImageView img = (ImageView)v.findViewById(R.id.imgView);
        
        img.setImageBitmap(timerPage.getImage());
        return v;
    }

    private View createButtonView() {
        View v = _inflater.inflate(R.layout.button_view, null);

        ImageButton btn = (ImageButton) v.findViewById(R.id.camera_button);
        btn.setOnClickListener(_onClickListener);
        btn = (ImageButton) v.findViewById(R.id.gallery_button);
        btn.setOnClickListener(_onClickListener);

        return v;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
            ((ViewPager) collection).removeView((View) view);
    }
    
    @Override
    public boolean isViewFromObject(View view, Object object) {
            return view == (View)object;
    }
}
