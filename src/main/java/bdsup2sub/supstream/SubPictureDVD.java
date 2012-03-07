package bdsup2sub.supstream;

import java.util.ArrayList;

/**
 * @author 0xdeadbeef
 * Extends SubPicture to store information read from DVD (SUB/IDX or SUP/IFO)
 */
public class SubPictureDVD extends SubPicture implements Cloneable {

    /** offset to information in SUB file */
    long offset;
    /** size of RLE buffer */
    int rleSize;
    /** even line offset (inside RLE buffer) */
    int evenOfs;
    /** odd line offset (inside RLE buffer) */
    int oddOfs;
    /** list of RLE fragments */
    ArrayList<ImageObjectFragment> rleFragments;
    /** uncropped bitmap width */
    int originalWidth;
    /** uncropped bitmap height */
    int originalHeight;
    /** original x offset of uncropped bitmap */
    int originalX;
    /** original y offset of uncropped bitmap */
    int originalY;
    /** 4 original alpha values */
    int originalAlpha[];
    /** 4 original palette values*/
    int originalPal[];
    /** 4 alpha values */
    public int alpha[];
    /** 4 palette values */
    public int pal[];

    /* (non-Javadoc)
     * @see SubPicture#clone()
     */
    @Override
    public SubPictureHD clone() {
        return (SubPictureHD)super.clone();
    }

    /**
     * store original sizes and offsets
     */
    void setOriginal() {
        originalWidth = getImageWidth();
        originalHeight = getImageHeight();
        originalX = getOfsX();
        originalY = getOfsY();

        originalAlpha = new int[4];
        originalPal = new int[4];
        for (int i=0; i < 4; i++) {
            originalAlpha[i] = alpha[i];
            originalPal[i] = pal[i];
        }
    }

    /**
     * Copy info of given generic subpicture into this DVD subpicture
     * Used to copy the edited info (position, forced flags etc. into a DVD SubPicture for writing
     * @param pic
     */
    public void copyInfo(final SubPicture pic) {
        width = pic.width;
        height = pic.height;
        startTime = pic.startTime;
        endTime = pic.endTime;
        isforced = pic.isforced;
        compNum = pic.compNum;
        setImageWidth(pic.getImageWidth());
        setImageHeight(pic.getImageHeight());
        setOfsX(pic.getOfsX());
        setOfsY(pic.getOfsY());
    }
}
