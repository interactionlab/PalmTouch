package io.interactionlab.palmtouchdemo.processing.palmdetection;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Huy on 22/12/2017.
 */

/**
 * Classifying touches. 
 *
 * In the following, we use the TensorFlowInferenceInterface (available since TensorFlow 1.1) 
 * which is an easier way then the one we used in the paper. The older approach is based on 
 * this example: https://github.com/miyosuda/TensorFlowAndroidMNIST/
 */
public class PalmClassifier {
    private static TensorFlowInferenceInterface inferenceInterface;

    public PalmClassifier(String modelPath, Context context) {
        // Loading model from assets folder.
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), modelPath);
    }


    public int classify(float[] pixels) {
        // Node Names
        String inputName = "input_tensor";
        String outputName = "output_tensor";

        // Define output nodes
        String[] outputNodes = new String[]{outputName};
        float[] outputs = new float[10];

        // Feed image into the model and fetch the results.
        inferenceInterface.feed(inputName, pixels, 1, 405);
        inferenceInterface.run(outputNodes, false);
        inferenceInterface.fetch(outputName, outputs);

        // Convert one-hot encoded result to an int (= detected class)
        float max = Float.MIN_VALUE;
        int idx = -1;
        for (int i = 0; i < outputs.length; i++) {
            if (outputs[i] > max) {
                max = outputs[i];
                idx = i;
            }
        }

        return idx;
    }
}
