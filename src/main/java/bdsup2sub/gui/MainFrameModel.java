package bdsup2sub.gui;

import bdsup2sub.core.Core;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrameModel {

    private static final int RECENT_FILE_COUNT = 5;
    static final Color ERROR_BACKGROUND = new Color(0xffe1acac);
    static final Color OK_BACKGROUND = UIManager.getColor("TextField.background");

    private String loadPath;
    private String saveFilename;
    private String savePath;
    private int subIndex;
    private boolean sourceFileSpecifiedOnCmdLine;
    private String colorProfilePath;
    private Dimension mainWindowSize;
    private Point mainWindowLocation;
    private List<String> recentFiles;

    public MainFrameModel() {
        this.loadPath = Core.props.get("loadPath", "");
        this.colorProfilePath = Core.props.get("colorPath", "");
        this.mainWindowSize = new Dimension(Core.props.get("frameWidth", 800), Core.props.get("frameHeight", 600));
        this.mainWindowLocation = new Point(Core.props.get("framePosX", -1), Core.props.get("framePosY", -1));
        loadRecentFiles();
    }

    private void loadRecentFiles() {
        recentFiles = new ArrayList<String>();
        int i = 0;
        String filename;
        while (i < RECENT_FILE_COUNT && (filename = Core.props.get("recent_" + i, "")).length() > 0) {
            recentFiles.add(filename);
            i++;
        }
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
        Core.props.set("colorPath", colorProfilePath);
    }

    public Dimension getMainWindowSize() {
        return mainWindowSize;
    }

    public void setMainWindowSize(Dimension dimension) {
        this.mainWindowSize = dimension;
        Core.props.set("frameWidth", dimension.width);
        Core.props.set("frameHeight", dimension.height);
    }

    public Point getMainWindowLocation() {
        return mainWindowLocation;
    }

    public void setMainWindowLocation(Point location) {
        this.mainWindowLocation = location;
        Core.props.set("framePosX", location.x);
        Core.props.set("framePosY", location.y);
    }

    public List<String> getRecentFiles() {
        return recentFiles;
    }

    public void addToRecentFiles(String filename) {
        int index = recentFiles.indexOf(filename);
        if (index != -1) {
            recentFiles.remove(index);
            recentFiles.add(0, filename);
        } else {
            recentFiles.add(0, filename);
            if (recentFiles.size() > RECENT_FILE_COUNT) {
                recentFiles.remove(recentFiles.size() - 1);
            }
        }
        for (int i=0; i < recentFiles.size(); i++) {
            Core.props.set("recent_" + i, recentFiles.get(i));
        }
    }
}
