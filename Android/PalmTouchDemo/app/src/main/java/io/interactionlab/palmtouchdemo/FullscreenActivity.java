package io.interactionlab.palmtouchdemo;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;



//import android.support.v7.app.ActionBar;
import androidx.appcompat.app.ActionBar;

//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;


import android.view.View;
import android.widget.RelativeLayout;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;

import java.util.ArrayList;
import java.util.List;

import io.interactionlab.palmtouchdemo.processing.blobdetection.BlobBoundingBox;
import io.interactionlab.palmtouchdemo.processing.blobdetection.BlobDetectionUtils;
import io.interactionlab.palmtouchdemo.processing.palmdetection.PalmClassifier;


public class FullscreenActivity extends AppCompatActivity {
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            movableWindow.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    private static final String TAG = FullscreenActivity.class.getSimpleName();
    private RelativeLayout movableWindow;
    private DrawView drawView;

    PalmClassifier palmClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        movableWindow = (RelativeLayout) findViewById(R.id.movableScreen);
        palmClassifier = new PalmClassifier("palmtouch.pb", this);

        LocalDeviceHandler localDeviceHandler = new LocalDeviceHandler();
        localDeviceHandler.setLocalCapImgListener(new LocalCapImgListener() {
            @Override
            public void onLocalCapImg(final CapacitiveImageTS capImg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final List<BlobBoundingBox> bbbl = BlobDetectionUtils.getBlobs(capImg);
                        int detection = -1;
                        final List<String> labels = new ArrayList<String>();
                        for (int i = 0; i < bbbl.size(); i++) {
                            int[][] blobContent = BlobDetectionUtils.getBlobContent(bbbl.get(i), capImg);

                            float[] arr = new float[405];
                            int index = 0;
                            for (int[] row : blobContent) {
                                for (int val : row) {
                                    arr[index++] = (float) val;
                                }
                            }

                            detection = palmClassifier.classify(arr);
                            final String predString = detection == -1 ? "No Touch Input" : (detection == 0 ? "Finger" : "Palm");
                            labels.add(predString);
                        }
                        drawView.updateView(capImg, bbbl, labels);
                    }
                });
            }
        });
        localDeviceHandler.startHandler();

        // fill the whole screen.
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        drawView = new DrawView(this);
        movableWindow.removeAllViews();
        movableWindow.addView(drawView, params);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        movableWindow.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
