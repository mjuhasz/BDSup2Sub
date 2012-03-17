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

    /** selected output resolution */
    private Resolution resolution;
    /** selected delay in 90kHz resolution */
    private int     delayPTS;
    /** selected minimum frame time in 90kHz resolution */
    private int     minTimePTS;
    /** flag that tells whether to convert the frame rate or not */
    private boolean changeFPS;
    /** flag that tells whether to convert the resolution or not */
    private boolean changeResolution;
    /** flag that tells whether to fix frames shorter than a minimum time */
    private boolean fixShortFrames;
    /** source frame rate */
    private double  fpsSrc;
    /** target frame rate */
    private double  fpsTrg;
    /** cancel state */
    private boolean cancel;
    /** semaphore to disable actions while changing component properties */
    private volatile boolean isReady;
    /** flag that tells whether to use free scaling or not */
    private boolean changeScale;
    /** X scaling factor */
    private double scaleX;
    /** Y scaling factor */
    private double scaleY;
    /** source fps is certain */
    private boolean fpsSrcCertain;
    /** clear/set all forced flags */
    private ForcedFlagState forcedState;
    /** apply move settings */
    private boolean moveCaptions;

    public ConversionDialogModel() {
        changeResolution = configuration.getConvertResolution();

        // fix output resolution in case that it should not be changed
        // change target resolution to source resolution if no conversion is needed
        if (!changeResolution && Core.getNumFrames() > 0) {
            resolution = Core.getResolution(Core.getSubPictureSrc(0).width, Core.getSubPictureSrc(0).height);
        } else {
            resolution = configuration.getOutputResolution();
        }

        moveCaptions = Core.getMoveCaptions();

        delayPTS = configuration.getDelayPTS();
        minTimePTS = (int) SubtitleUtils.syncTimePTS(configuration.getMinTimePTS(), configuration.getFPSTrg(), configuration.getFPSTrg());
        changeFPS = configuration.getConvertFPS();
        changeScale = configuration.getApplyFreeScale();
        fixShortFrames = configuration.getFixShortFrames();
        fpsSrc = configuration.getFPSSrc();
        fpsTrg = configuration.getFPSTrg();
        scaleX = configuration.getFreeScaleFactorX();
        scaleY = configuration.getFreeScaleFactorY();
        fpsSrcCertain = configuration.isFpsSrcCertain();
        forcedState = Core.getForceAll();
    }

    public void setConvertFPSConf(boolean convertFPS) {
        configuration.setConvertFPS(convertFPS);
    }

    public void setFPSSrcConf(double fpsSrc) {
        configuration.setFPSSrc(fpsSrc);
    }
    
    public double getFPSTrgConf() {
        return configuration.getFPSTrg();
    }

    public void setFPSTrgConf(double fpsTrg) {
        configuration.setFPSTrg(fpsTrg);
    }

    public void setDelayPTSConf(int delayPTS) {
        configuration.setDelayPTS(delayPTS);
    }

    public void setFixShortFramesConf(boolean fixShortFrames) {
        configuration.setFixShortFrames(fixShortFrames);
    }

    public void setMinTimePTSConf(int minTimePTS) {
        configuration.setMinTimePTS(minTimePTS);
    }

    public void setConvertResolutionConf(boolean convertResolution) {
        configuration.setConvertResolution(convertResolution);
    }

    public void setOutputResolutionConf(Resolution outputResolution) {
        configuration.setOutputResolution(outputResolution);
    }

    public void setFreeScaleFactorConf(double x, double y) {
        configuration.setFreeScaleFactor(x, y);
    }
    
    public void setApplyFreeScaleConf(boolean applyFreeScale) {
        configuration.setApplyFreeScale(applyFreeScale);
    }

////////////////////////////////////////////////

    public void storeConvertFPS(boolean convertFPS) {
        configuration.storeConvertFPS(convertFPS);
    }

    public void storeFPSSrc(double fpsSrc) {
        configuration.storeFPSSrc(fpsSrc);
    }

    public void storeFPSTrg(double fpsTrg) {
        configuration.storeFPSTrg(fpsTrg);
    }

    public void storeDelayPTS(int delayPTS) {
        configuration.storeDelayPTS(delayPTS);
    }

    public void storeFixShortFrames(boolean fixShortFrames) {
        configuration.storeFixShortFrames(fixShortFrames);
    }

    public void storeMinTimePTS(int minTimePTS) {
        configuration.storeMinTimePTS(minTimePTS);
    }

    public void storeConvertResolution(boolean convertResolution) {
        configuration.storeConvertResolution(convertResolution);
    }

    public void storeOutputResolution(Resolution outputResolution) {
        configuration.storeOutputResolution(outputResolution);
    }

    public void storeApplyFreeScale(boolean applyFreeScale) {
        configuration.storeApplyFreeScale(applyFreeScale);
    }

    public void storeFreeScaleFactor(double x, double y) {
        configuration.storeFreeScaleFactor(x, y);
    }

    public void storeConfig() {
        configuration.storeConfig();
    }

    ////////////////////////////////////////////////

    public boolean loadConvertResolution() {
        return configuration.loadConvertResolution();
    }

    public boolean loadConvertFPS() {
        return configuration.loadConvertFPS();
    }

    public int loadDelayPTS() {
        return configuration.loadDelayPTS();
    }

    public boolean loadFixShortFrames() {
        return configuration.loadFixShortFrames();
    }

    public int loadMinTimePTS() {
        return configuration.loadMinTimePTS();
    }

    public boolean loadApplyFreeScale() {
        return configuration.loadApplyFreeScale();
    }

    public double loadFreeScaleFactorX() {
        return configuration.loadFreeScaleFactorX();
    }

    public double loadFreeScaleFactorY() {
        return configuration.loadFreeScaleFactorY();
    }

    public double loadFpsSrc() {
        return configuration.loadFpsSrc();
    }

    public double loadFpsTrg() {
        return configuration.loadFpsTrg();
    }

    public Resolution loadOutputResolution() {
        return configuration.loadOutputResolution();
    }

    ////////////////////////////////////////////////

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public int getDelayPTS() {
        return delayPTS;
    }

    public void setDelayPTS(int delayPTS) {
        this.delayPTS = delayPTS;
    }

    public int getMinTimePTS() {
        return minTimePTS;
    }

    public void setMinTimePTS(int minTimePTS) {
        this.minTimePTS = minTimePTS;
    }

    public boolean isChangeFPS() {
        return changeFPS;
    }

    public void setChangeFPS(boolean changeFPS) {
        this.changeFPS = changeFPS;
    }

    public boolean isChangeResolution() {
        return changeResolution;
    }

    public void setChangeResolution(boolean changeResolution) {
        this.changeResolution = changeResolution;
    }

    public boolean isFixShortFrames() {
        return fixShortFrames;
    }

    public void setFixShortFrames(boolean fixShortFrames) {
        this.fixShortFrames = fixShortFrames;
    }

    public double getFpsSrc() {
        return fpsSrc;
    }

    public void setFpsSrc(double fpsSrc) {
        this.fpsSrc = fpsSrc;
    }

    public double getFpsTrg() {
        return fpsTrg;
    }

    public void setFpsTrg(double fpsTrg) {
        this.fpsTrg = fpsTrg;
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

    public boolean isChangeScale() {
        return changeScale;
    }

    public void setChangeScale(boolean changeScale) {
        this.changeScale = changeScale;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
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

    public boolean isMoveCaptions() {
        return moveCaptions;
    }

    public void setMoveCaptions(boolean moveCaptions) {
        this.moveCaptions = moveCaptions;
    }
}
