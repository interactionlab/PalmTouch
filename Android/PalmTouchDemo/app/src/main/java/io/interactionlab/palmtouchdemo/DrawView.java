package io.interactionlab.palmtouchdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;

import java.util.List;

import io.interactionlab.palmtouchdemo.processing.blobdetection.BlobBoundingBox;

/**
 * Visualizes the capacitive image and the result of the palm classifier.
 *
 * @author Huy
 */
public class DrawView extends View {
    Paint paint = new Paint();

    private String TAG = DrawView.class.getSimpleName();

    private CapacitiveImageTS capacitiveImage;
    private List<BlobBoundingBox> bbbl;
    private List<String> labels;

    public DrawView(Context context) {
        super(context);
    }

    public void updateView(CapacitiveImageTS capacitiveImage, List<BlobBoundingBox> bbbl, List<String> labels) {
        this.capacitiveImage = capacitiveImage;
        this.bbbl = bbbl;
        this.labels = labels;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        int boxWidth = 1080 / 15;
        int boxHeight = 1920 / 27;

        if (capacitiveImage == null) {
            return;
        }

        paint.setStyle(Paint.Style.FILL);
        int[][] matrix = capacitiveImage.getMatrix();

        // Draw capacitive matrix
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                int val = matrix[i][j];

                if (val < 0)
                    val = 0;

                if (val > 255)
                    val = 255;

                // Draw rectangle
                paint.setColor(new Color().rgb(val, val, val));
                paint.setStyle(Paint.Style.FILL);
                Rect r = new Rect(j * boxWidth, (i) * boxHeight, (j + 1) * boxWidth, (i + 1) * boxHeight);
                canvas.drawRect(r, paint);

                // Write number
                paint.setTextSize(15);
                paint.setColor(new Color().rgb(255 - val, 255 - val, 255 - val));
                canvas.drawText(val + "", j * boxWidth + (int) (0.5 * boxWidth), i * boxHeight + (int) (0.5 * boxHeight), paint);
            }
        }


        if (bbbl != null) {
            // Draw Labels
            for (int i = 0; i < bbbl.size(); i++) {
                BlobBoundingBox bbb = bbbl.get(i);
                if (labels.get(i).equals("Finger")) {
                    paint.setColor(new Color().rgb(0, 255, 0));
                } else {
                    paint.setColor(Color.RED);
                }
                paint.setStyle(Paint.Style.STROKE);
                Rect r = new Rect(bbb.x1 * boxWidth, bbb.y1 * boxHeight, bbb.x2 * boxWidth, bbb.y2 * boxHeight);
                canvas.drawRect(r, paint);

                paint.setTextSize(45);
                canvas.drawText(labels.get(i), bbb.x1 * boxWidth - (int) (1.0 * boxWidth), bbb.y1 * boxHeight - (int) (1.0 * boxHeight), paint);
            }
        }

    }
}

