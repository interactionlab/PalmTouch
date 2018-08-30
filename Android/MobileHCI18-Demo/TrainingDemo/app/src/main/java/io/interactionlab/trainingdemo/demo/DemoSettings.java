package io.interactionlab.trainingdemo.demo;

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
//            new ModelDescription(
//                    "Left vs. Right Thumb",
//                    "file:///android_asset/leftVsRightThumb.pb",
//                    "conv2d_1_input",
//                    "output_node0",
//                    new long[]{1, 27, 15, 1},
//                    new String[]{"Left Thumb", "Right Thumb"},
//                    new int[]{Color.YELLOW, Color.GREEN})

    };
};


