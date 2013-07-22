package com.ciscavate.android.phototimer;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

public class TimePickerDialogFragment extends DialogFragment {

    public static interface ITimePickerHandler {
        void onTimeSetHandler(String name, int hour, int min, int sec);
    }
    

    private ITimePickerHandler _handler;
    
    /**
     * required dumb constructor.
     */
    public TimePickerDialogFragment() {    }
    
    public void setTimePickerHandler(ITimePickerHandler handler) {
        this._handler = handler;
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_picker, container);
        
        final EditText timerName = (EditText) view.findViewById(R.id.timerNameField);
        
        final NumberPicker hourPicker = (NumberPicker)view.findViewById(R.id.hourPicker);
        final NumberPicker minPicker = (NumberPicker)view.findViewById(R.id.minPicker);
        final NumberPicker secPicker = (NumberPicker)view.findViewById(R.id.secPicker);
        
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(99);
        hourPicker.setWrapSelectorWheel(true);
        
        minPicker.setMinValue(0);
        minPicker.setMaxValue(59);
        minPicker.setWrapSelectorWheel(true);
        
        secPicker.setMinValue(0);
        secPicker.setMaxValue(59);
        secPicker.setWrapSelectorWheel(true);

        Button saveBtn = (Button)view.findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if (null != _handler) {
                    String name = timerName.getText().toString();
                    int hour = hourPicker.getValue();
                    int min = minPicker.getValue();
                    int sec = secPicker.getValue();
                    _handler.onTimeSetHandler(name, hour, min, sec);
                }
                TimePickerDialogFragment.this.dismiss();
            }
        });
        
        Button cancelBtn = (Button)view.findViewById(R.id.cancelButton);
        cancelBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                TimePickerDialogFragment.this.dismiss();
            }
        });
        
        return view;
    }
}
