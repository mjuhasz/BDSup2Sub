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
package bdsup2sub.gui.color;

import java.awt.*;

public class ColorDialog {

    private ColorDialogModel model;
    private ColorDialogView view;
    private ColorDialogController controller;

    public ColorDialog(Frame owner) {
        model = new ColorDialogModel();
        view = new ColorDialogView(model, owner);
        controller = new ColorDialogController(model, view);
    }

    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    public void setParameters(String[] cName, Color[] cColor, Color[] cColorDefault) {
        view.setParameters(cName, cColor, cColorDefault);
    }

    public void setPath(String colorProfilePath) {
        view.setPath(colorProfilePath);
    }

    public boolean wasCanceled() {
        return view.wasCanceled();
    }

    public Color[] getColors() {
        return view.getColors();
    }

    public String getPath() {
        return view.getPath();
    }
}