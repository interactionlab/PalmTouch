package io.interactionlab.palmtouchusecasedemos;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private Switch palmTouchSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        palmTouchSwitch = findViewById(R.id.swPalmTouch);
        palmTouchSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (palmTouchSwitch.isChecked()) {
                    startPalmTouch();
                } else {
                    stopPalmTouch();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateButtonStatus();
    }

    /**
     * @return True if PalmTouch Service was successfully started. False if it was already started.
     */
    protected boolean startPalmTouch() {
        if (Constants.palmDetectionService == null) {
            Constants.palmDetectionService = new Intent(this, PalmTouchService.class);
        }

        if (!isServiceRunning(PalmTouchService.class)) {
            Constants.palmDetectionService.putExtra("action", getPalmTouchAction());
            startService(Constants.palmDetectionService);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return True if PalmTouch Service was successfully stopped. False if it was already stopped.
     */
    protected boolean stopPalmTouch() {
        if (isServiceRunning(PalmTouchService.class)) {
            Intent intent = new Intent(MainActivity.this, PalmTouchService.class);
            stopService(intent);
            return true;
        } else {
            return false;
        }
    }

    private int getPalmTouchAction() {
        RadioGroup radioGroup = findViewById(R.id.rgAction);
        RadioButton checkedRb = this.findViewById(radioGroup.getCheckedRadioButtonId());
        return radioGroup.indexOfChild(checkedRb);
    }

    private void updateButtonStatus() {
        boolean palmTouchRunning = isServiceRunning(PalmTouchService.class);
        palmTouchSwitch.setChecked(palmTouchRunning);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
