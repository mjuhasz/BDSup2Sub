/*
 * Copyright 2012 Miklos Juhasz (mjuhasz)
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
package bdsup2sub.gui.conversion;

import bdsup2sub.core.Configuration;
import bdsup2sub.core.Core;
import bdsup2sub.core.ForcedFlagState;
import bdsup2sub.core.Resolution;
import bdsup2sub.utils.SubtitleUtils;

class ConversionDialogModel {

    private final Configuration configuration = Configuration.getInstance();

    private Resolution outputResolution;
    private int delayPTS;
    private int minTimePTS;
    private boolean convertFPS;
    private boolean convertResolution;
    private boolean fixShortFrames;
    private double  fpsSrc;
    private double  fpsTrg;
    private boolean applyFreeScale;
    private double freeScaleFactorX;
    private double freeScaleFactorY;
    private boolean fpsSrcCertain;
    private ForcedFlagState forcedState;
    private boolean moveCaptions;

    private boolean cancel;
    private volatile boolean isReady;

    public ConversionDialogModel() {
        convertResolution = configuration.getConvertResolution();

        // fix output resolution in case that it should not be changed
        // change target resolution to source resolution if no conversion is needed
        if (!convertResolution && Core.getNumFrames() > 0) {
            outputResolution = SubtitleUtils.getResolutionForDimension(Core.getSubPictureSrc(0).getWidth(), Core.getSubPictureSrc(0).getHeight());
        } else {
            outputResolution = configuration.getOutputResolution();
        }

        moveCaptions = configuration.getMoveCaptions();

        delayPTS = configuration.getDelayPTS();
        minTimePTS = (int) SubtitleUtils.syncTimePTS(configuration.getMinTimePTS(), configuration.getFpsTrg(), configuration.getFpsTrg());
        convertFPS = configuration.getConvertFPS();
        applyFreeScale = configuration.getApplyFreeScale();
        fixShortFrames = configuration.getFixShortFrames();
        fpsSrc = configuration.getFPSSrc();
        fpsTrg = configuration.getFpsTrg();
        freeScaleFactorX = configuration.getFreeScaleFactorX();
        freeScaleFactorY = configuration.getFreeScaleFactorY();
        fpsSrcCertain = configuration.isFpsSrcCertain();
        forcedState = configuration.getForceAll();
    }

    public void storeConfig() {
        configuration.storeConfig();
    }

    public Resolution getOutputResolution() {
        return outputResolution;
    }

    public void setOutputResolution(Resolution outputResolution) {
        this.outputResolution = outputResolution;
    }

    public void setOutputResolutionConf(Resolution outputResolution) {
        configuration.setOutputResolution(outputResolution);
    }

    public void loadOutputResolution() {
        outputResolution = configuration.loadOutputResolution();
    }

    public void storeOutputResolution() {
        configuration.storeOutputResolution(outputResolution);
    }

    public int getDelayPTS() {
        return delayPTS;
    }

    public void setDelayPTS(int delayPTS) {
        this.delayPTS = delayPTS;
    }

    public void setDelayPTSConf(int delayPTS) {
        configuration.setDelayPTS(delayPTS);
    }

    public void loadDelayPTS() {
        delayPTS = configuration.loadDelayPTS();
    }

    public void storeDelayPTS() {
        configuration.storeDelayPTS(delayPTS);
    }

    public int getMinTimePTS() {
        return minTimePTS;
    }

    public void setMinTimePTS(int minTimePTS) {
        this.minTimePTS = minTimePTS;
    }

    public void setMinTimePTSConf(int minTimePTS) {
        configuration.setMinTimePTS(minTimePTS);
    }

    public void loadMinTimePTS() {
        minTimePTS = configuration.loadMinTimePTS();
    }

    public void storeMinTimePTS() {
        configuration.storeMinTimePTS(minTimePTS);
    }

    public boolean getConvertFPS() {
        return convertFPS;
    }

    public void setConvertFPS(boolean convertFPS) {
        this.convertFPS = convertFPS;
    }

    public void setConvertFPSConf(boolean convertFPS) {
        configuration.setConvertFPS(convertFPS);
    }

    public void loadConvertFPS() {
        convertFPS = configuration.loadConvertFPS();
    }

    public void storeConvertFPS() {
        configuration.storeConvertFPS(convertFPS);
    }

    public boolean getConvertResolution() {
        return convertResolution;
    }

    public void setConvertResolution(boolean convertResolution) {
        this.convertResolution = convertResolution;
    }

    public void setConvertResolutionConf(boolean convertResolution) {
        configuration.setConvertResolution(convertResolution);
    }

    public void loadConvertResolution() {
        convertResolution = configuration.loadConvertResolution();
    }

    public void storeConvertResolution() {
        configuration.storeConvertResolution(convertResolution);
    }

    public boolean getFixShortFrames() {
        return fixShortFrames;
    }

    public void setFixShortFrames(boolean fixShortFrames) {
        this.fixShortFrames = fixShortFrames;
    }

    public void setFixShortFramesConf(boolean fixShortFrames) {
        configuration.setFixShortFrames(fixShortFrames);
    }

    public void loadFixShortFrames() {
        fixShortFrames = configuration.loadFixShortFrames();
    }

    public void storeFixShortFrames() {
        configuration.storeFixShortFrames(fixShortFrames);
    }

    public double getFpsSrc() {
        return fpsSrc;
    }

    public void setFpsSrc(double fpsSrc) {
        this.fpsSrc = fpsSrc;
    }

    public void setFPSSrcConf(double fpsSrc) {
        configuration.setFpsSrc(fpsSrc);
    }

    public void loadFpsSrc() {
        fpsSrc = configuration.loadFpsSrc();
    }

    public void storeFPSSrc() {
        configuration.storeFPSSrc(fpsSrc);
    }

    public double getFpsTrg() {
        return fpsTrg;
    }

    public void setFpsTrg(double fpsTrg) {
        this.fpsTrg = fpsTrg;
    }

    public double getFpsTrgConf() {
        return configuration.getFpsTrg();
    }

    public void setFpsTrgConf(double fpsTrg) {
        configuration.setFpsTrg(fpsTrg);
    }

    public void loadFpsTrg() {
        fpsTrg = configuration.loadFpsTrg();
    }

    public void storeFpsTrg() {
        configuration.storeFpsTrg(fpsTrg);
    }

    public boolean getApplyFreeScale() {
        return applyFreeScale;
    }

    public void setApplyFreeScale(boolean applyFreeScale) {
        this.applyFreeScale = applyFreeScale;
    }

    public void setApplyFreeScaleConf(boolean applyFreeScale) {
        configuration.setApplyFreeScale(applyFreeScale);
    }

    public void loadApplyFreeScale() {
        applyFreeScale = configuration.loadApplyFreeScale();
    }

    public void storeApplyFreeScale() {
        configuration.storeApplyFreeScale(applyFreeScale);
    }

    public double getFreeScaleFactorX() {
        return freeScaleFactorX;
    }

    public double getFreeScaleFactorY() {
        return freeScaleFactorY;
    }

    public void setFreeScaleFactorX(double freeScaleFactorX) {
        this.freeScaleFactorX = freeScaleFactorX;
    }

    public void setFreeScaleFactorY(double freeScaleFactorY) {
        this.freeScaleFactorY = freeScaleFactorY;
    }

    public void setFreeScaleFactorConf(double x, double y) {
        configuration.setFreeScaleFactor(x, y);
    }

    public void loadFreeScaleFactorX() {
        freeScaleFactorX = configuration.loadFreeScaleFactorX();
    }

    public void loadFreeScaleFactorY() {
        freeScaleFactorY = configuration.loadFreeScaleFactorY();
    }

    public void storeFreeScaleFactor() {
        configuration.storeFreeScaleFactor(freeScaleFactorX, freeScaleFactorY);
    }

    public boolean isFpsSrcCertain() {
        return fpsSrcCertain;
    }

    public void setFpsSrcCertain(boolean fpsSrcCertain) {
        this.fpsSrcCertain = fpsSrcCertain;
    }

    public ForcedFlagState getForcedState() {
        return forcedState;
    }

    public void setForcedState(ForcedFlagState forcedState) {
        this.forcedState = forcedState;
    }

    public void loadForcedState() {
        forcedState = configuration.getForceAll();
    }

    public boolean getMoveCaptions() {
        return moveCaptions;
    }

    public void setMoveCaptions(boolean moveCaptions) {
        this.moveCaptions = moveCaptions;
    }

    public void storeMoveCaptions() {
        configuration.setMoveCaptions(moveCaptions);
    }

    public boolean wasCanceled() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isKeepFps() {
        return configuration.isKeepFps();
    }
}
