package bdsup2sub.gui;

import bdsup2sub.core.Core;

import javax.swing.*;
import java.awt.*;

public class MainFrameModel {

    static final Color ERROR_BACKGROUND = new Color(0xffe1acac);
    static final Color OK_BACKGROUND = UIManager.getColor("TextField.background");

    private String loadPath;
    private String saveFilename;
    private String savePath;
    private int subIndex;
    private boolean sourceFileSpecifiedOnCmdLine;

    private String colorProfilePath;

    public MainFrameModel() {
        this.loadPath = Core.props.get("loadPath", "");
        this.colorProfilePath = Core.props.get("colorPath", "");
    }

    public String getLoadPath() {
        return loadPath;
    }

    public void setLoadPath(String loadPath) {
        this.loadPath = loadPath;
        Core.props.set("loadPath", loadPath); //FIXME: use listener
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getSaveFilename() {
        return saveFilename;
    }

    public void setSaveFilename(String saveFilename) {
        this.saveFilename = saveFilename;
    }

    public int getSubIndex() {
        return subIndex;
    }

    public void setSubIndex(int subIndex) {
        this.subIndex = subIndex;
    }

    public boolean isSourceFileSpecifiedOnCmdLine() {
        return sourceFileSpecifiedOnCmdLine;
    }

    public void setSourceFileSpecifiedOnCmdLine(boolean sourceFileSpecified) {
        this.sourceFileSpecifiedOnCmdLine = sourceFileSpecified;
    }

    public String getColorProfilePath() {
        return colorProfilePath;
    }

    public void setColorProfilePath(String colorProfilePath) {
        this.colorProfilePath = colorProfilePath;
        Core.props.set("colorPath", colorProfilePath);  //FIXME: use listener
    }
}
