package deadbeef.supstream;
import java.util.ArrayList;

import deadbeef.bitmap.ErasePatch;

/**
 * Stores information about one subpicture frame.
 *
 * Note: image related members are private and need getters to allow more complex access functions
 * for BD-SUPs. Indeed the class SubPictureBD doesn't access the image width/height and offsets of the
 * parent class at all. Only when the copy function is used to create a SubPicture copy from a
 * SubPictureBD instance, these members are filled correctly.<br>
 * This also means that the setter functions for these members are pretty much useless as they
 * only change the members of the parent class, but don't influence the values returned by the getters.
 * This is a little unclean but by design to not allow write access to the internal structures.
 *
 * @author 0xdeadbeef
 */
public class SubPicture implements Cloneable {

	/** with of subtitle image */
	private int imageWidth;
	/** height of subtitle image */
	private int imageHeight;
	/** upper left corner of subtitle x */
	private int xOfs;
	/** upper left corner of subtitle y */
	private int yOfs;

	/* public */

	/** screen width */
	public int width;
	/** screen height */
	public int height;
	/** start time in milliseconds */
	public long startTime;
	/** end time in milliseconds */
	public long endTime;
	/** if true, this is a forced subtitle */
	public boolean isforced;
	/** composition number - increased at start and end PCS */
	public int compNum;
	/** frame was already decoded */
	public boolean wasDecoded;

	/* the following fields are really only needed for editing */

	/** exclude from export? */
	public boolean exclude;

	/** list of erase patches */
	public ArrayList<ErasePatch> erasePatch;

	/**
	 * Allows to get a clone of the parent object even for SubPictureBD objects.
	 * @return clone of the parent object
	 */
	public SubPicture copy() {
		SubPicture sp = new SubPicture();
		sp.width = width;
		sp.height = height;
		sp.startTime = startTime;
		sp.endTime = endTime;
		sp.isforced = isforced;
		sp.compNum = compNum;

		/* Note that by using the getter functions
		 * the internal values of a SubPictureBD are
		 * copied into the plain members of the
		 * SubPicture object.
		 */
		sp.setImageWidth(getImageWidth());
		sp.setImageHeight(getImageHeight());
		sp.setOfsX(getOfsX());
		sp.setOfsY(getOfsY());

		sp.exclude = exclude;
		sp.wasDecoded = wasDecoded;
		if (erasePatch != null && erasePatch.size()>0) {
			ArrayList<ErasePatch> epl = new ArrayList<ErasePatch>();
			for (ErasePatch ep : erasePatch) {
				epl.add(ep);
			}
			sp.erasePatch = epl;
		}
		return sp;
	}

	/**
	 * get image width
	 * @return image width in pixels
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * get image height
	 * @return image height in pixels
	 */
	public int getImageHeight() {
		return imageHeight;
	}

	/**
	 * get image x offset
	 * @return image x offset in pixels
	 */
	public int getOfsX() {
		return xOfs;
	}

	/**
	 * get image y offset
	 * @return image y offset in pixels
	 */
	public int getOfsY() {
		return yOfs;
	}

	/**
	 * Set image width
	 * @param w width in pixels
	 */
	public void setImageWidth(int w) {
		imageWidth = w;
	}

	/**
	 * Set image height
	 * @param h height in pixels
	 */
	public void setImageHeight(int h) {
		imageHeight = h;
	}

	/**
	 * Set image x offset
	 * @param ofs offset in pixels
	 */
	public void setOfsX(int ofs) {
		xOfs = ofs;
	}

	/**
	 * Set image y offset
	 * @param ofs offset in pixels
	 */
	public void setOfsY(int ofs) {
		yOfs = ofs;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SubPicture clone() {
		try {
			return (SubPicture)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}

/**
 * @author 0xdeadbeef
 * Extends SubPicture to store information read from BD SUP
 */
class SubPictureBD extends SubPicture implements Cloneable {
	/** objectID used in decoded object */
	int objectID;
	/** list of ODS packets containing image info */
	ArrayList<ImageObject> imageObjectList;
	/** width of subtitle window (might be larger than image) */
	int winWidth;
	/** height of subtitle window (might be larger than image) */
	int winHeight;
	/** upper left corner of subtitle window x */
	int xWinOfs;
	/** upper left corner of subtitle window y */
	int yWinOfs;
	/** FPS type (e.g. 0x10 = 24p) */
	int type;
	/** list of (list of) palette info - there are up to 8 palettes per epoch, each can be updated several times */
	ArrayList<ArrayList<PaletteInfo>> palettes;

	/* (non-Javadoc)
	 * @see SubPicture#clone()
	 */
	@Override
	public SubPictureBD clone() {
		return (SubPictureBD)super.clone();
	}

	/**
	 * Create clone of this object, but featuring a deep copy of the palettes
	 * and image object information.
	 * Note that the ODS fragments are only a flat copy, since they are never
	 * updated, only overwritten.
	 * @return clone of this object
	 */
	SubPictureBD deepCopy() {
		SubPictureBD c = this.clone();
		// deep copy palettes
		if (palettes != null) {
			c.palettes = new ArrayList<ArrayList<PaletteInfo>>();
			for (ArrayList<PaletteInfo> pi : palettes) {
				ArrayList<PaletteInfo> cpi = new ArrayList<PaletteInfo>();
				c.palettes.add(cpi);
				for (PaletteInfo p : pi) {
					cpi.add(p.clone());
				}
			}
		}
		// (not so) deep copy of objects (cloning of the fragment lists is not needed)
		if (imageObjectList != null) {
			c.imageObjectList = new ArrayList<ImageObject>();
			for (ImageObject io : imageObjectList) {
				c.imageObjectList.add(io.clone());
			}
		}
		return c;
	}

	/* setters / getters */

	/**
	 * get image width
	 * @return image width in pixels
	 */
	@Override
	public int getImageWidth() {
		return imageObjectList.get(objectID).width;
	}

	/**
	 * get image height
	 * @return image height in pixels
	 */
	@Override
	public int getImageHeight() {
		return imageObjectList.get(objectID).height;
	}

	/**
	 * get image x offset
	 * @return image x offset in pixels
	 */
	@Override
	public int getOfsX() {
		return imageObjectList.get(objectID).xOfs;
	}

	/**
	 * get image y offset
	 * @return image y offset in pixels
	 */
	@Override
	public int getOfsY() {
		return imageObjectList.get(objectID).yOfs;
	}

	/**
	 * Get image object containing RLE data
	 * @param index index of subtitle
	 * @return image object containing RLE data
	 */
	ImageObject getImgObj(int index) {
		return imageObjectList.get(index);
	}

	/**
	 * Get image object containing RLE data
	 * @return image object containing RLE data
	 */
	ImageObject getImgObj() {
		return imageObjectList.get(objectID);
	}
}

/**
 * @author 0xdeadbeef
 * Extends SubPicture to store information read from HD-DVD SUP
 */
class SubPictureHD extends SubPicture implements Cloneable {

	/** offset to palette info for this subpicture in SUP file */
	int paletteOfs;
	/** offset to alpha info for this subpicture in SUP file */
	int alphaOfs;
	/** size of RLE buffer (odd and even part)*/
	int imageBufferSize;
	/** offset to even part of RLE buffer in SUP file*/
	int imageBufferOfsEven;
	/** offset to odd part of RLE buffer in SUP file*/
	int imageBufferOfsOdd;

	/* member functions */

	/* (non-Javadoc)
	 * @see SubPicture#clone()
	 */
	@Override
	public SubPictureHD clone() {
		return (SubPictureHD)super.clone();
	}
}

class ImageObject implements Cloneable  {
	/** list of ODS packets containing image info */
	ArrayList<ImageObjectFragment> fragmentList;
	/** palette identifier */
	int paletteID;
	/** overall size of RLE buffer (might be spread over several packages) */
	int bufferSize;
	/** with of subtitle image */
	int width;
	/** height of subtitle image */
	int height;
	/** upper left corner of subtitle x */
	int xOfs;
	/** upper left corner of subtitle y */
	int yOfs;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ImageObject clone() {
		try {
			return (ImageObject)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}

/**
 * @author 0xdeadbeef
 * contains offset and size of one fragment containing (parts of the) RLE buffer
 */
class ImageObjectFragment implements Cloneable  {
	/** offset to RLE buffer in SUP file */
	long imageBufferOfs;
	/** size of this part of the RLE buffer */
	int imagePacketSize;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ImageObjectFragment clone() {
		try {
			return (ImageObjectFragment)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}

/**
 * @author 0xdeadbeef
 * contains offset and size of one update of a palette
 */
class PaletteInfo implements Cloneable {
	/** offset to palette info in SUP file */
	int paletteOfs;
	/** number of palette entries */
	int paletteSize;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PaletteInfo clone() {
		try {
			return (PaletteInfo)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}

/**
 * @author 0xdeadbeef
 * Extends SubPicture to store information read from Xml
 */
class SubPictureXml extends SubPicture implements Cloneable {
	/** original x offset of uncropped bitmap */
	int originalX;
	/** original y offset of uncropped bitmap */
	int originalY;
	/** file name of Xml file */
	String fileName;

	/* (non-Javadoc)
	 * @see SubPicture#clone()
	 */
	@Override
	public SubPictureXml clone() {
		return (SubPictureXml)super.clone();
	}

	/**
	 * store original offsets
	 */
	void setOriginal() {
		originalX = getOfsX();
		originalY = getOfsY();
	}
}
