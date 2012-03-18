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
package bdsup2sub.gui.help;

import java.awt.*;

public class Help {

    private HelpModel model;
    private HelpView view;
    private HelpController controller;

    public Help(Frame mainFrame) {
        model = new HelpModel();
        view = new HelpView(model, mainFrame);
        controller = new HelpController(model, view);
    }

    public void setVisible(boolean b) {
        view.setVisible(b);
    }
}
