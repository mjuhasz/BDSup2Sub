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
package bdsup2sub.gui.export;

import java.awt.*;

public class ExportDialog {

    private final ExportDialogModel model;
    private final ExportDialogView view;
    private final ExportDialogController controller;

    public ExportDialog(String path, Frame owner) {
        model = new ExportDialogModel();
        model.setFilename(path);
        view = new ExportDialogView(model, owner);
        controller = new ExportDialogController(model, view);
    }

    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }
    
    public String getFilename() {
        return model.getFilename();
    }

    public boolean wasCanceled() {
        return model.wasCanceled();
    }
}
