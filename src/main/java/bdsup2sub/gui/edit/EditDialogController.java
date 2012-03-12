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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class EditDialogController {

    private EditDialogModel model;
    private EditDialogView view;

    public EditDialogController(EditDialogModel model, EditDialogView view) {
        this.model = model;
        this.view = view;

        view.addWindowListener(new EditDialogWindowListener());
    }

    private class EditDialogWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            view.dispose();
        }
    }
}
