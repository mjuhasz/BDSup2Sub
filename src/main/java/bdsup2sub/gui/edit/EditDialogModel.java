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
package bdsup2sub.gui.edit;

import bdsup2sub.core.Configuration;
import bdsup2sub.core.Resolution;
import bdsup2sub.supstream.SubPicture;

import java.awt.image.BufferedImage;

public class EditDialogModel {

    private final Configuration configuration = Configuration.getInstance();

    private BufferedImage image;
    private SubPicture subPic;
    private SubPicture subPicNext;
    private SubPicture subPicPrev;
    private int index;
    private int frameTime;
    private int minWidth = 768;
    private int minHeight = 432;

    private volatile boolean isReady; //TODO: this is UI related, does not belong to the model
    private volatile boolean edited;
    private boolean enableSliders; //TODO: this is UI related, does not belong to the model

    public EditDialogModel() {
        frameTime = (int)(90000/getFPSTrg());
    }

    public Resolution getOutputResolution() {
        return configuration.getOutputResolution();
    }

    public double getFPSTrg() {
        return configuration.getFPSTrg();
    }

    public long getMinTimePTS() {
        return configuration.getMinTimePTS();
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public SubPicture getSubPic() {
        return subPic;
    }

    public void setSubPic(SubPicture subPic) {
        this.subPic = subPic;
    }

    public SubPicture getSubPicNext() {
        return subPicNext;
    }

    public void setSubPicNext(SubPicture subPicNext) {
        this.subPicNext = subPicNext;
    }

    public SubPicture getSubPicPrev() {
        return subPicPrev;
    }

    public void setSubPicPrev(SubPicture subPicPrev) {
        this.subPicPrev = subPicPrev;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getFrameTime() {
        return frameTime;
    }

    public void setFrameTime(int frameTime) {
        this.frameTime = frameTime;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isEnableSliders() {
        return enableSliders;
    }

    public void setEnableSliders(boolean enableSliders) {
        this.enableSliders = enableSliders;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}
