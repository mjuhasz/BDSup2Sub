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

public class ConversionDialogModel {

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
            outputResolution = Core.getResolution(Core.getSubPictureSrc(0).width, Core.getSubPictureSrc(0).height);
        } else {
            outputResolution = configuration.getOutputResolution();
        }

        moveCaptions = Core.getMoveCaptions();

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
        forcedState = Core.getForceAll();
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

    public Resolution loadOutputResolution() {
        return configuration.loadOutputResolution();
    }

    public void storeOutputResolution(Resolution outputResolution) {
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

    public int loadDelayPTS() {
        return configuration.loadDelayPTS();
    }

    public void storeDelayPTS(int delayPTS) {
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

    public int loadMinTimePTS() {
        return configuration.loadMinTimePTS();
    }

    public void storeMinTimePTS(int minTimePTS) {
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

    public boolean loadConvertFPS() {
        return configuration.loadConvertFPS();
    }

    public void storeConvertFPS(boolean convertFPS) {
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

    public boolean loadConvertResolution() {
        return configuration.loadConvertResolution();
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

    public boolean loadFixShortFrames() {
        return configuration.loadFixShortFrames();
    }

    public void storeFixShortFrames(boolean fixShortFrames) {
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

    public double loadFpsSrc() {
        return configuration.loadFpsSrc();
    }

    public void storeFPSSrc(double fpsSrc) {
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

    public double loadFpsTrg() {
        return configuration.loadFpsTrg();
    }

    public void storeFpsTrg(double fpsTrg) {
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

    public boolean loadApplyFreeScale() {
        return configuration.loadApplyFreeScale();
    }

    public void storeApplyFreeScale(boolean applyFreeScale) {
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

    public double loadFreeScaleFactorX() {
        return configuration.loadFreeScaleFactorX();
    }

    public double loadFreeScaleFactorY() {
        return configuration.loadFreeScaleFactorY();
    }

    public void storeFreeScaleFactor(double x, double y) {
        configuration.storeFreeScaleFactor(x, y);
    }

    /////////////

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

    public boolean getMoveCaptions() {
        return moveCaptions;
    }

    public void setMoveCaptions(boolean moveCaptions) {
        this.moveCaptions = moveCaptions;
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
}
