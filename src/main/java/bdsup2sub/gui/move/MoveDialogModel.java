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
package bdsup2sub.gui.move;

import bdsup2sub.core.CaptionMoveModeX;
import bdsup2sub.core.CaptionMoveModeY;
import bdsup2sub.core.Configuration;
import bdsup2sub.core.Core;
import bdsup2sub.supstream.SubPicture;

import java.awt.image.BufferedImage;

class MoveDialogModel {

    private final Configuration configuration = Configuration.getInstance();

    private BufferedImage image;
    private CaptionMoveModeX moveModeX = CaptionMoveModeX.KEEP_POSITION;
    private CaptionMoveModeY moveModeY = CaptionMoveModeY.KEEP_POSITION;
    private int originalX;
    private int originalY;
    private int currentSubtitleIndex;
    private SubPicture subPic;
    private int offsetY;
    private int offsetX;
    private double cinemascopeBarFactor = 5.0/42;
    private int cropOfsY;
    private double targetScreenAspectRatio = 21.0/9;
    private volatile boolean isReady = false;

    public MoveDialogModel() {
        cropOfsY = configuration.getCropOffsetY();

        offsetX = configuration.getMoveOffsetX();
        offsetY = configuration.getMoveOffsetY();
        moveModeX = configuration.getMoveModeX();
        moveModeY = configuration.getMoveModeY();
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public CaptionMoveModeX getMoveModeX() {
        return moveModeX;
    }

    public void setMoveModeX(CaptionMoveModeX moveModeX) {
        this.moveModeX = moveModeX;
    }

    public void storeMoveModeX() {
        configuration.setMoveModeX(moveModeX);
    }

    public CaptionMoveModeY getMoveModeY() {
        return moveModeY;
    }

    public void setMoveModeY(CaptionMoveModeY moveModeY) {
        this.moveModeY = moveModeY;
    }

    public void storeMoveModeY() {
        configuration.setMoveModeY(moveModeY);
    }

    public int getOriginalX() {
        return originalX;
    }

    public void setOriginalX(int originalX) {
        this.originalX = originalX;
    }

    public int getOriginalY() {
        return originalY;
    }

    public void setOriginalY(int originalY) {
        this.originalY = originalY;
    }

    public int getCurrentSubtitleIndex() {
        return currentSubtitleIndex;
    }

    public void setCurrentSubtitleIndex(int currentSubtitleIndex) {
        this.currentSubtitleIndex = currentSubtitleIndex;
    }

    public SubPicture getSubPic() {
        return subPic;
    }

    public void setSubPic(SubPicture subPic) {
        this.subPic = subPic;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public void storeMoveOffsetY() {
        configuration.setMoveOffsetY(offsetY);
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public void storeMoveOffsetX() {
        configuration.setMoveOffsetX(offsetX);
    }

    public double getCinemascopeBarFactor() {
        return cinemascopeBarFactor;
    }

    public void setCinemascopeBarFactor(double cinemascopeBarFactor) {
        this.cinemascopeBarFactor = cinemascopeBarFactor;
    }

    public void storeCinemascopeBarFactor() {
        configuration.setCineBarFactor(cinemascopeBarFactor);
    }

    public int getCropOfsY() {
        return cropOfsY;
    }

    public void setCropOfsY(int cropOfsY) {
        this.cropOfsY = cropOfsY;
    }

    public void storeCropOfsY() {
        configuration.setCropOffsetY(cropOfsY);
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public double getTargetScreenAspectRatio() {
        return targetScreenAspectRatio;
    }

    public void setTargetScreenAspectRatio(double targetScreenAspectRatio) {
        this.targetScreenAspectRatio = targetScreenAspectRatio;
    }
}
