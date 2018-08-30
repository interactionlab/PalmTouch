package io.interactionlab.palmtouchusecasedemos;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.hcilab.libftsp.LocalDeviceHandler;
import org.hcilab.libftsp.capacitivematrix.MatrixUtils;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobDetector;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.hcilab.libftsp.listeners.LocalCapImgListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.interactionlab.palmtouchusecasedemos.classification.BlobClassifier;
import io.interactionlab.palmtouchusecasedemos.classification.ClassificationResult;
import io.interactionlab.palmtouchusecasedemos.classification.DemoSettings;
import io.interactionlab.palmtouchusecasedemos.classification.ModelDescription;

public class PalmTouchService extends Service implements View.OnTouchListener, View.OnClickListener {
    private final static int CONSECUTIVE_PALMS = 2;
    private final static int VIBRATION_DURATION = 10;
    private final static String TAG = PalmTouchService.class.getSimpleName();

    private BlobClassifier blobClassifier;
    private ModelDescription currentModel;

    private int consecPalmCounter = 0;

    private WindowManager wm;
    private ProcessManager processManager;
    private Handler handler;
    private NotificationManager notificationManager;

    private LocalDeviceHandler localDeviceHandler;

    private List<View> visibleViews = new ArrayList<View>();

    private int palmTouchAction = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    long testTimestamp;
    @Override
    public void onCreate() {
        final String msg = "PalmTouch Service started.";
        handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        this.palmTouchAction = 0;

        blobClassifier = new BlobClassifier(this);
        currentModel = DemoSettings.models[0];
        blobClassifier.setModel(currentModel);
        processManager = new ProcessManager(getApplicationContext());

        testTimestamp = System.currentTimeMillis();

        localDeviceHandler = new LocalDeviceHandler();
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

        showNotificationIcon();
    }

    public void showNotificationIcon() {
        Intent sendIntent = new Intent(getApplicationContext(), MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                sendIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setOngoing(true)
                        .setContentIntent(pendingIntent)
                        .setContentTitle("PalmTouch")
                        .setContentText("PalmTouch Service is running."); //Required on Gingerbread and below

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(748, mBuilder.build());
    }

    public void removeNotification(NotificationManager notificationManager) {
        notificationManager.cancel(748);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            this.palmTouchAction = intent.getIntExtra("action", 0);
            System.out.println("PalmTouch Action: " + this.palmTouchAction);
        }

        System.out.println("onStartCommand!");

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "PalmTouch Service stopped.", Toast.LENGTH_SHORT).show();
            }
        });

        localDeviceHandler.stopHandler();
        removeNotification(notificationManager);
    }

    private void handlePalmDetection(boolean palmAvailable) {
        if (palmAvailable) {
            consecPalmCounter++;
        } else {
            consecPalmCounter = 0;
        }

        if (consecPalmCounter == CONSECUTIVE_PALMS) {
            int idx = palmTouchAction;

            vibrate();

            switch (idx) {
                case 0:
                    if (processManager != null) {
                        String app = processManager.getVisibleApplication();
                        if (app != null && app.equals("com.cyanogenmod.trebuchet")) {
                            openNotificationBar();
                        } else {
                            showPieMenu();
                        }
                    } else {
                        System.out.println("ProcessManager is null.");
                    }
                    break;
                case 1:
                    // Pie Menu
                    showPieMenu();
                    break;
                case 2:
                    // Trigger notification bar
                    openNotificationBar();
                    break;
                default:
                    break;
            }
        }
    }

    private void showPieMenu() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

                if (visibleViews.size() > 0) {
                    return;
                }

                ImageView btn1 = new ImageView(PalmTouchService.this);
                btn1.setImageResource(R.mipmap.piemenu_1);
                btn1.setMinimumHeight(Constants.PIE_MENU_BTN_SIZE);
                btn1.setMaxHeight(Constants.PIE_MENU_BTN_SIZE);
                btn1.setMinimumWidth(Constants.PIE_MENU_BTN_SIZE);
                btn1.setMaxWidth(Constants.PIE_MENU_BTN_SIZE);
                btn1.setOnTouchListener(PalmTouchService.this);
                btn1.setOnClickListener(PalmTouchService.this);

                WindowManager.LayoutParams params1 = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
                params1.gravity = Gravity.LEFT | Gravity.TOP;
                params1.x = 0;
                params1.y = 1150;
                wm.addView(btn1, params1);
                visibleViews.add(btn1);

                // ---------------------

                ImageView btn2 = new ImageView(PalmTouchService.this);
                btn2.setImageResource(R.mipmap.piemenu_2);
                btn2.setMinimumHeight(Constants.PIE_MENU_BTN_SIZE);
                btn2.setMaxHeight(Constants.PIE_MENU_BTN_SIZE);
                btn2.setMinimumWidth(Constants.PIE_MENU_BTN_SIZE);
                btn2.setMaxWidth(Constants.PIE_MENU_BTN_SIZE);
                btn2.setOnTouchListener(PalmTouchService.this);
                btn2.setOnClickListener(PalmTouchService.this);

                WindowManager.LayoutParams params2 = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
                params2.gravity = Gravity.LEFT | Gravity.TOP;
                params2.x = 160;
                params2.y = 850;
                wm.addView(btn2, params2);
                visibleViews.add(btn2);

                // ---------------------

                ImageView btn3 = new ImageView(PalmTouchService.this);
                btn3.setImageResource(R.mipmap.piemenu_3);
                btn3.setMinimumHeight(Constants.PIE_MENU_BTN_SIZE);
                btn3.setMaxHeight(Constants.PIE_MENU_BTN_SIZE);
                btn3.setMinimumWidth(Constants.PIE_MENU_BTN_SIZE);
                btn3.setMaxWidth(Constants.PIE_MENU_BTN_SIZE);
                btn3.setOnTouchListener(PalmTouchService.this);
                btn3.setOnClickListener(PalmTouchService.this);

                WindowManager.LayoutParams params3 = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
                params3.gravity = Gravity.LEFT | Gravity.TOP;
                params3.x = 450;
                params3.y = 700;
                wm.addView(btn3, params3);
                visibleViews.add(btn3);

                // ---------------------

                ImageView btnClose = new ImageView(PalmTouchService.this);
                btnClose.setImageResource(R.mipmap.piemenu_close);
                btnClose.setMinimumHeight(Constants.PIE_MENU_BTN_SIZE);
                btnClose.setMaxHeight(Constants.PIE_MENU_BTN_SIZE);
                btnClose.setMinimumWidth(Constants.PIE_MENU_BTN_SIZE);
                btnClose.setMaxWidth(Constants.PIE_MENU_BTN_SIZE);
                btnClose.setOnTouchListener(PalmTouchService.this);
                btnClose.setOnClickListener(PalmTouchService.this);

                WindowManager.LayoutParams params4 = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
                params4.gravity = Gravity.LEFT | Gravity.TOP;
                params4.x = 450;
                params4.y = 1150;
                wm.addView(btnClose, params4);
                visibleViews.add(btnClose);

            }
        });
    }

    private void openApp(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        startActivity(launchIntent);
    }

    private void openNotificationBar() {
        Object sbservice = getSystemService("statusbar");
        Class<?> statusbarManager = null;
        try {
            statusbarManager = Class.forName("android.app.StatusBarManager");
            Method showsb;
            if (Build.VERSION.SDK_INT >= 17) {
                showsb = statusbarManager.getMethod("expandNotificationsPanel");
            } else {
                showsb = statusbarManager.getMethod("expand");
            }
            showsb.invoke(sbservice);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(VIBRATION_DURATION);
        }
    }

    @Override
    public void onClick(View v) {
        // Determine which button it is
        Log.i(TAG, "Button " + visibleViews.indexOf(v) + " pressed.");

        switch (visibleViews.indexOf(v)) {
            case 0:
                // Email
                openApp("com.android.mms");
                break;
            case 1:
                // Browser
                openApp("com.android.browser"); // vllt. zu chrome machen
                break;
            case 2:
                // Google Maps
                openApp("com.google.android.apps.maps");
                break;
            default:
                break;
        }

        vibrate();

        // Hide button afterwards
        for (View vv : visibleViews) {
            if (vv.getWindowToken() != null) {
                wm.removeView(vv);
            }
        }
        visibleViews.clear();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
}
