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
package bdsup2sub.gui.main;

import java.io.File;

public class MainFrame {

    private final MainFrameModel model;
    private final MainFrameView view;
    private final MainFrameController controller;

    public MainFrame(File inputFile) {
        model = new MainFrameModel();
        if (inputFile != null) {
            model.setLoadPath(inputFile.getAbsolutePath());
            model.setSourceFileSpecifiedOnCmdLine(true);
        }
        view = new MainFrameView(model);
        controller = new MainFrameController(model, view);
    }

    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }
}
