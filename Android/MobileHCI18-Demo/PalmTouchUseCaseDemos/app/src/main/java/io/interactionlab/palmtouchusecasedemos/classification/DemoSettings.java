package io.interactionlab.palmtouchusecasedemos.classification;

import android.graphics.Color;

/**
 * Created by Huy on 28/06/2018.
 */

public class DemoSettings {
    public static ModelDescription[] models = new ModelDescription[]{
            new ModelDescription(
                    "PalmTouch",
                    "file:///android_asset/palmtouch.pb",
                    "input_tensor",
                    "output_tensor",
                    new long[]{1, 405},
                    new String[]{"Finger", "Palm"},
                    new int[]{Color.GREEN, Color.YELLOW})
    };
};


