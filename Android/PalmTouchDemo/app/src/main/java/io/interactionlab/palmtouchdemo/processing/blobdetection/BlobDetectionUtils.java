package io.interactionlab.palmtouchdemo.processing.blobdetection;

import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huy on 22/12/2017.
 */

/**
 * This is a custom implementation of a blob detection and can also be replaced with a blob
 * detection with OpenCV.
 */
public class BlobDetectionUtils {

    public static List<BlobBoundingBox> getBlobs(CapacitiveImageTS capImg) {
        int[][] matrix = capImg.getMatrix();

        ArrayList<BlobBoundingBox> blobs = new ArrayList<BlobBoundingBox>();
        for (int y = 0; y < matrix.length; y++) {
            for (int x = 0; x < matrix[0].length; x++) {
                // blob detection
                List<BlobCoordinates> found = new ArrayList<BlobCoordinates>();
                blobDetection(matrix, x, y, found);

                if (found.size() > 0) {
                    int x_min = Integer.MAX_VALUE, x_max = Integer.MIN_VALUE;
                    int y_min = Integer.MAX_VALUE, y_max = Integer.MIN_VALUE;
                    for (BlobCoordinates b : found) {
                        // mins
                        if (b.x1 < x_min) {
                            x_min = b.x1;
                        }

                        if (b.y1 < y_min) {
                            y_min = b.y1;
                        }

                        // maxs
                        if (b.x1 > x_max) {
                            x_max = b.x1;
                        }

                        if (b.y1 > y_max) {
                            y_max = b.y1;
                        }
                    }

                    BlobBoundingBox bbb = new BlobBoundingBox(x_min - 1, y_min - 1, x_max + 1, y_max + 1);
                    if (!blobs.contains(bbb) && ((x_max - x_min) * (y_max - y_min)) > 1) {
                        blobs.add(bbb);
                    }
                }
            }
        }

        return blobs;
    }

    public static int[][] getBlobContent(BlobBoundingBox bbb, CapacitiveImageTS capImg) {
        int[][] matrix = capImg.getMatrix();
        int[][] blob = new int[27][15];

        for (int y = bbb.y1; y < bbb.y2; y++) {
            for (int x = bbb.x1; x < bbb.x2; x++) {
                blob[y - bbb.y1][x - bbb.x1] = matrix[y][x];
            }
        }

        return blob;
    }

    private static void blobDetection(int[][] matrix, int x, int y, List<BlobCoordinates> found) {
        final int THRESHOLD = 30;

        if (x > 0 && x < matrix[0].length && y > 0 && y < matrix.length && matrix[y][x] > THRESHOLD && !found.contains(new BlobCoordinates(x, y))) {
            found.add(new BlobCoordinates(x, y));
            blobDetection(matrix, x + 1, y, found); // right
            blobDetection(matrix, x - 1, y, found); // left
            blobDetection(matrix, x, y + 1, found); // top
            blobDetection(matrix, x, y - 1, found); // down
        }
    }
}
