/*
 * Copyright 2013 Miklos Juhasz (mjuhasz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bdsup2sub.gui.main;

import bdsup2sub.core.*;

import java.awt.*;
import java.util.List;

class MainFrameModel {

    private final Configuration configuration = Configuration.getInstance();

    private String saveFilename;
    private String savePath;
    private int subIndex;
    private boolean sourceFileSpecifiedOnCmdLine;

    public String getLoadPath() {
        return configuration.getLoadPath();
    }

    public void setLoadPath(String loadPath) {
        configuration.setLoadPath(loadPath);
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
        return configuration.getColorProfilePath();
    }

    public void setColorProfilePath(String colorProfilePath) {
        configuration.setColorProfilePath(colorProfilePath);
    }

    public Dimension getMainWindowSize() {
        return configuration.getMainWindowSize();
    }

    public void setMainWindowSize(Dimension dimension) {
        configuration.setMainWindowSize(dimension);
    }

    public Point getMainWindowLocation() {
        return configuration.getMainWindowLocation();
    }

    public void setMainWindowLocation(Point location) {
        configuration.setMainWindowLocation(location);
    }

    public List<String> getRecentFiles() {
        return configuration.getRecentFiles();
    }

    public void addToRecentFiles(String filename) {
        configuration.addToRecentFiles(filename);
    }

    public boolean isVerbose() {
        return configuration.isVerbose();
    }

    public void setVerbose(boolean verbose) {
        configuration.setVerbose(verbose);
    }

    public ScalingFilter getScalingFilter() {
        return configuration.getScalingFilter();
    }

    public void setScalingFilter(ScalingFilter scalingFilter) {
        configuration.setScalingFilter(scalingFilter);
    }

    public boolean getFixZeroAlpha() {
        return configuration.getFixZeroAlpha();
    }

    public void setFixZeroAlpha(boolean fixZeroAlpha) {
        configuration.setFixZeroAlpha(fixZeroAlpha);
    }

    public PaletteMode getPaletteMode() {
        return configuration.getPaletteMode();
    }

    public void setPaletteMode(PaletteMode paletteMode) {
        configuration.setPaletteMode(paletteMode);
    }

    public OutputMode getOutputMode() {
        return configuration.getOutputMode();
    }

    public void setOutputMode(OutputMode outputMode) {
        configuration.setOutputMode(outputMode);
    }

    public boolean getConvertFPS() {
        return configuration.getConvertFPS();
    }

    public int getDelayPTS() {
        return configuration.getDelayPTS();
    }

    public boolean getApplyFreeScale() {
        return configuration.getApplyFreeScale();
    }

    public double getFreeScaleX() {
        return configuration.getFreeScaleFactorX();
    }

    public double getFreeScaleY() {
        return configuration.getFreeScaleFactorY();
    }

    public double getFPSTrg() {
        return configuration.getFpsTrg();
    }

    public Resolution getOutputResolution() {
        return configuration.getOutputResolution();
    }

    public int getAlphaThreshold() {
        return configuration.getAlphaThreshold();
    }

    public void setAlphaThreshold(int alphaThreshold) {
        configuration.setAlphaThreshold(alphaThreshold);
    }

    public int[] getLuminanceThreshold() {
        return configuration.getLuminanceThreshold();
    }

    public void setLuminanceThreshold(int[] luminanceThreshold) {
        configuration.setLuminanceThreshold(luminanceThreshold);
    }

    public int getCropOffsetY() {
        return configuration.getCropOffsetY();
    }

    public void setCropOffsetY(int cropOffsetY) {
        configuration.setCropOffsetY(cropOffsetY);
    }

    public void setSwapCrCb(boolean swapCrCb) {
        configuration.setSwapCrCb(swapCrCb);
    }

    public boolean getMoveCaptions() {
        return configuration.getMoveCaptions();
    }
}
