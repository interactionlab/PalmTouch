package io.interactionlab.trainingdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.MatrixUtils;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobDetector;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;

import java.util.ArrayList;
import java.util.List;

import io.interactionlab.trainingdemo.demo.DemoSettings;
import io.interactionlab.trainingdemo.demo.ModelDescription;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
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
    private TextView textViewMode;

    private BlobClassifier blobClassifier;
    private ModelDescription currentModel;

    private int consecPalmCounter;
    private final static int CONSECUTIVE_PALMS = 2;

    private ImageView imageView;

    boolean visState = false;

    private void setModel(ModelDescription modelDescription) {
        currentModel = modelDescription;
        blobClassifier.setModel(currentModel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        imageView = findViewById(R.id.imageView);
        movableWindow = (RelativeLayout) findViewById(R.id.movableScreen);
        blobClassifier = new BlobClassifier(this);

        LocalDeviceHandler localDeviceHandler = new LocalDeviceHandler();
        localDeviceHandler.setLocalCapImgListener(new LocalCapImgListener() {
            @Override
            public void onLocalCapImg(final CapacitiveImageTS capImg) { // called approximately every 50ms
                final List<BlobBoundingBox> blobBoundingBoxes = capImg.getBlobBoundaries();

                List<float[]> flattenedBlobs = new ArrayList<float[]>();
                int[][] matrix = capImg.getMatrix();

                for (BlobBoundingBox bbb : blobBoundingBoxes) {
                    flattenedBlobs.add(
                            MatrixUtils.flattenClipAndNormalizeMatrixFloat(
                                    BlobDetector.getBlobContentIn27x15(matrix, bbb), 0, 268, 268));
                }

                boolean palmFound = false;
                for (int i = 0; i < flattenedBlobs.size(); i++) {
                    ClassificationResult cr = blobClassifier.classify(flattenedBlobs.get(i));

                    if (cr.index == 1) {
                        palmFound = true;
                    }
                }

                handlePalmDetection(palmFound);
            }
        });
        localDeviceHandler.startHandler();

        imageView.setImageResource(R.mipmap.icon_closed);

        setModel(DemoSettings.models[0]);
    }



    private void handlePalmDetection(boolean palmAvailable) {
        if (palmAvailable) {
            consecPalmCounter++;
        } else {
            consecPalmCounter = 0;
        }

        if (consecPalmCounter == CONSECUTIVE_PALMS) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    visState = !visState;

                    if (visState) {
                        imageView.setImageResource(R.mipmap.icon_opened);
                    } else {
                        imageView.setImageResource(R.mipmap.icon_closed);
                    }
                }
            });
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
//        // Show the system bar
        movableWindow.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Schedule a runnable to display UI elements after a delay
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
