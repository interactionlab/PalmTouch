package io.interactionlab.palmtouchdemo.processing.blobdetection;

/**
 * Created by Huy on 08.01.2017.
 */

public class BlobBoundingBox {
    public int x1;
    public int y1;
    public int x2;
    public int y2;

    public BlobBoundingBox(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof BlobBoundingBox) {
            BlobBoundingBox ptr = (BlobBoundingBox) v;

            if (ptr.x1 == this.x1 && ptr.y1 == this.y1 && ptr.x2 == this.x2 && ptr.y2 == this.y2) {
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
        hash = hash * 31 + x2;
        hash = hash * 31 + y2;

        return hash;
    }

    @Override
    public String toString() {
        return "x1:" + this.x1 + "; y1:" + this.y1 + "; x2:" + this.x2 + "; y2:" + this.y2 + ";";
    }
}
