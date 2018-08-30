package io.interactionlab.palmtouchusecasedemos.classification;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Huy on 05/09/2017.
 */

public class BlobClassifier {
    private static TensorFlowInferenceInterface inferenceInterface;
    private Context context;
    private ModelDescription modelDescription;

    public BlobClassifier(Context context) {
        // Loading model from assets folder.
        this.context = context;
    }

    public void setModel(ModelDescription modelDescription) {
        this.modelDescription = modelDescription;
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), modelDescription.modelPath);
    }


    public ClassificationResult classify(float[] pixels) {
        // Node Names
        String inputName = modelDescription.inputNode;
        String outputName = modelDescription.outputNode;


        // Define output nodes
        String[] outputNodes = new String[]{outputName};
        float[] outputs = new float[modelDescription.labels.length];

        // Feed image into the model and fetch the results.
        inferenceInterface.feed(inputName, pixels, modelDescription.inputDimensions);
        inferenceInterface.run(outputNodes, true);
        inferenceInterface.fetch(outputName, outputs);


        // Convert one-hot encoded result to an int (= detected class)
        float maxConf = Float.MIN_VALUE;
        int idx = -1;
        for (int i = 0; i < outputs.length; i++) {
            if (outputs[i] > maxConf) {
                maxConf = outputs[i];
                idx = i;
            }
        }

        ClassificationResult cr = new ClassificationResult();
        cr.index = idx;
        cr.label = modelDescription.labels[idx];
        cr.confidence = maxConf;
        cr.color = modelDescription.labelColor[idx];

        return cr;
    }
}
