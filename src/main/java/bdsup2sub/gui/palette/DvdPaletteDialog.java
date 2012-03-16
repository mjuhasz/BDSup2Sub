/*
 * Copyright 2012 Volker Oth (0xdeadbeef) / Miklos Juhasz (mjuhasz)
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
import java.awt.*;

public class DvdPaletteDialog {

    private DvdPaletteDialogModel model;
    private DvdPaletteDialogView view;
    private DvdPaletteDialogController controller;

    public DvdPaletteDialog(Frame owner, String colorNames[], Color currentColors[], Color defaultColors[], String colorProfilePath) {
        model = new DvdPaletteDialogModel();
        initModel(colorNames, currentColors, defaultColors, colorProfilePath);

        view = new DvdPaletteDialogView(model, owner);
        controller = new DvdPaletteDialogController(model, view);
    }

    private void initModel(String[] colorNames, Color[] currentColors, Color[] defaultColors, String colorProfilePath) {
        model.setColorProfilePath(colorProfilePath);
        model.setColorNames(colorNames);
        model.setColorIcons(new ImageIcon[colorNames.length]);
        model.setDefaultColors(defaultColors);
        Color[] selectedColors = new Color[colorNames.length];
        for (int i=0; i < colorNames.length; i++) {
            selectedColors[i] = new Color(currentColors[i].getRGB());
        }
        model.setSelectedColors(selectedColors);
    }

    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    public boolean wasCanceled() {
        return model.wasCanceled();
    }

    public Color[] getColors() {
        return model.getSelectedColors();
    }

    public String getPath() {
        return model.getColorProfilePath();
    }
}