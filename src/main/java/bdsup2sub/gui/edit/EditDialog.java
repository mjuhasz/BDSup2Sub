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
package bdsup2sub.gui.edit;

import java.awt.*;

public class EditDialog {

    private EditDialogModel model;
    private EditDialogView view;
    private EditDialogController controller;

    public EditDialog(Frame owner) {
        model = new EditDialogModel();
        view = new EditDialogView(model, owner);
        controller = new EditDialogController(model, view);
    }

    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    //FIXME: remove this method created while making the mvc transition
    public int getIndex() {
        return view.getIndex();
    }

    //FIXME: remove this method created while making the mvc transition
    public void setIndex(int index) {
        view.setIndex(index);
    }
}
