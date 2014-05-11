/*
 * Copyright 2014 Miklos Juhasz (mjuhasz)
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
package bdsup2sub.gui.palette;

import javax.swing.*;

class FramePaletteDialogModel {

    static final String[] COLOR_NAME = { "00", "01", "02", "03", "04", "05", "06" ,"07", "08", "09", "10", "11", "12", "13", "14", "15"};

    private int currentSubtitleIndex;
    private ImageIcon colorPreviewIcon[];
    private volatile boolean isReady;
    private int alpha[];
    private int palette[];

    public int getCurrentSubtitleIndex() {
        return currentSubtitleIndex;
    }

    public void setCurrentSubtitleIndex(int currentSubtitleIndex) {
        this.currentSubtitleIndex = currentSubtitleIndex;
    }

    public ImageIcon[] getColorPreviewIcon() {
        return colorPreviewIcon;
    }

    public void setColorPreviewIcon(ImageIcon[] colorPreviewIcon) {
        this.colorPreviewIcon = colorPreviewIcon;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public int[] getAlpha() {
        return alpha;
    }

    public void setAlpha(int[] alpha) {
        this.alpha = alpha;
    }

    public int[] getPalette() {
        return palette;
    }

    public void setPalette(int[] palette) {
        this.palette = palette;
    }
}
