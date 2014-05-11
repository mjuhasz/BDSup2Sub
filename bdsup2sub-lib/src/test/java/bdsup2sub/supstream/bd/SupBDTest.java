package bdsup2sub.supstream.bd;

import bdsup2sub.BDSup2SubManagerBase;
import bdsup2sub.bitmap.ErasePatch;
import bdsup2sub.core.CoreException;
import bdsup2sub.supstream.ImageObject;
import bdsup2sub.supstream.ImageObjectFragment;
import bdsup2sub.supstream.PaletteInfo;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SupBDTest {

    private SubPictureBD subPictureBD;

    @Before
    public void setUp() {
        subPictureBD = new SubPictureBD();
        subPictureBD.setObjectID(0);
        subPictureBD.setWindowWidth(1920);
        subPictureBD.setWindowHeight(125);
        subPictureBD.setXWindowOffset(0);
        subPictureBD.setYWindowOffset(931);
        subPictureBD.setType(16);
        subPictureBD.setImageWidth(0);
        subPictureBD.setImageHeight(0);
        subPictureBD.setOfsX(0);
        subPictureBD.setOfsY(0);
        subPictureBD.setWidth(1920);
        subPictureBD.setHeight(1080);
        subPictureBD.setStartTime(2781531);
        subPictureBD.setEndTime(2980480);
        subPictureBD.setForced(false);
        subPictureBD.setCompositionNumber(0);
        subPictureBD.setWasDecoded(false);
        subPictureBD.setExcluded(false);
        subPictureBD.setErasePatch(new ArrayList<ErasePatch>());

        List<PaletteInfo> paletteInfos = subPictureBD.getPalettes().get(0);
        paletteInfos.add(new PaletteInfo(70, 16));

        List<ImageObject> imageObjectList = subPictureBD.getImageObjectList();
        ImageObject imageObject = new ImageObject();
        imageObject.setPaletteID(0);
        imageObject.setBufferSize(28390);
        imageObject.setWidth(1920);
        imageObject.setHeight(125);
        imageObject.setXOffset(0);
        imageObject.setYOffset(931);
        ImageObjectFragment imageObjectFragment = new ImageObjectFragment(174, 28390);
        imageObject.getFragmentList().add(imageObjectFragment);
        imageObjectList.add(imageObject);
    }

    @Test
    public void shouldParseBDSupStream() throws CoreException, IOException, URISyntaxException {
        SupBD supBD = new SupBD(new File(ClassLoader.getSystemResource("test.sup").toURI()).getAbsolutePath(), new BDSup2SubManagerBase());

        SubPictureBD actual = (SubPictureBD) supBD.getSubPicture(0);
        SubPictureBD expected = subPictureBD;
        assertEqualSubPictureBDs(expected, actual);
    }

    private void assertEqualSubPictureBDs(SubPictureBD first, SubPictureBD second) {
        assertEquals(first.getObjectID(), second.getObjectID());
        assertEquals(first.getWindowWidth(), second.getWindowWidth());
        assertEquals(first.getWindowHeight(), second.getWindowHeight());
        assertEquals(first.getXWindowOffset(), second.getXWindowOffset());
        assertEquals(first.getYWindowOffset(), second.getYWindowOffset());
        assertEquals(first.getType(), second.getType());
        assertEquals(first.getImageWidth(), second.getImageWidth());
        assertEquals(first.getImageHeight(), second.getImageHeight());
        assertEquals(first.getXOffset(), second.getXOffset());
        assertEquals(first.getYOffset(), second.getYOffset());
        assertEquals(first.getWidth(), second.getWidth());
        assertEquals(first.getHeight(), second.getHeight());
        assertEquals(first.getStartTime(), second.getStartTime());
        assertEquals(first.getEndTime(), second.getEndTime());
        assertEquals(first.isForced(), second.isForced());
        assertEquals(first.isWasDecoded(), second.isWasDecoded());
        assertEquals(first.isExcluded(), second.isExcluded());
        assertEquals(first.getErasePatch(), second.getErasePatch());
        assertEquals(first.getCompositionNumber(), second.getCompositionNumber());
        assertEquals(first.getPalettes(), second.getPalettes());
        assertEquals(first.getImageObjectList(), second.getImageObjectList());
    }
}
