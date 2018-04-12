package io.interactionlab.palmtouchdemo.processing.blobdetection;

/**
 * Created by Huy on 07.01.2017.
 */

public class BlobCoordinates {
    public int x1;
    public int y1;

    public BlobCoordinates(int x, int y) {
        this.x1 = x;
        this.y1 = y;
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof BlobCoordinates) {
            BlobCoordinates ptr = (BlobCoordinates) v;

            if (ptr.x1 == this.x1 && ptr.y1 == this.y1) {
                retVal = true;
            } else {
                retVal = false;
            }
        }

        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 23;
        hash = hash * 31 + x1;
        hash = hash * 31 + y1;

        return hash;
    }
}
