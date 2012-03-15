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
package bdsup2sub.gui.color;

import javax.swing.*;
import java.awt.*;

public class ColorDialogModel {

    private Color selectedColors[];
    private Color defaultColors[];
    private ImageIcon colorIcons[];
    private String colorNames[];
    private String colorProfilePath;
    private boolean canceled = true;

    public Color[] getSelectedColors() {
        return selectedColors;
    }

    public void setSelectedColors(Color[] selectedColors) {
        this.selectedColors = selectedColors;
    }

    public Color[] getDefaultColors() {
        return defaultColors;
    }

    public void setDefaultColors(Color[] defaultColors) {
        this.defaultColors = defaultColors;
    }

    public ImageIcon[] getColorIcons() {
        return colorIcons;
    }

    public void setColorIcons(ImageIcon[] colorIcons) {
        this.colorIcons = colorIcons;
    }

    public String[] getColorNames() {
        return colorNames;
    }

    public void setColorNames(String[] colorNames) {
        this.colorNames = colorNames;
    }

    public String getColorProfilePath() {
        return colorProfilePath;
    }

    public void setColorProfilePath(String colorProfilePath) {
        this.colorProfilePath = colorProfilePath;
    }

    public boolean wasCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
