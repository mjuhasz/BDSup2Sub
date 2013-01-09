/*
 * Copyright 2013 Miklos Juhasz (mjuhasz)
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

import bdsup2sub.core.OutputMode;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.ToolBox;

import java.awt.event.*;
import java.util.Collections;

class ExportDialogController {

    private final ExportDialogModel model;
    private final ExportDialogView view;

    public ExportDialogController(ExportDialogModel model, ExportDialogView view) {
        this.model = model;
        this.view = view;

        view.addWindowListener(new ExportDialogListener());
        
        view.addFilenameTextFieldActionListener(new FilenameTextFieldActionListener());
        view.addFilenameButtonActionListener(new FilenameButtonActionListener());
        view.addLanguageComboBoxItemListener(new LanguageComboBoxItemListener());
        view.addCancelButtonActionListener(new CancelButtonActionListener());
        view.addSaveButtonActionListener(new SaveButtonActionListener());
        view.addForcedCheckBoxItemListener(new ForcedCheckBoxItemListener());
        view.addWritePGCPaletteCheckBoxItemListener(new WritePGCPalCheckBoxItemListener());
    }


    private class FilenameTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String filename = view.getFilenameTextFieldText();
            if (filename != null) {
                model.setFilename(FilenameUtils.removeExtension(filename) + "." + model.getExtension());
            }
        }
    }

    private class FilenameButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String parent = FilenameUtils.getParent(model.getFilename());
            String defaultFilename = FilenameUtils.getName(model.getFilename());
            String filename = ToolBox.getFilename(parent, defaultFilename, Collections.singletonList(model.getExtension()), false, view);
            if (filename != null) {
                model.setFilename(FilenameUtils.removeExtension(filename) + "." + model.getExtension());
                view.setFilenameTextFieldText(model.getFilename());
            }
        }
    }

    private class LanguageComboBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            model.setLanguageIdx(view.getLanguageComboBoxSelectedItem());
        }
    }

    private class CancelButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            model.setCanceled(true);
            view.dispose();
        }
    }

    private class SaveButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String filename = view.getFilenameTextFieldText();
            if (filename != null) {
                model.setFilename(FilenameUtils.removeExtension(filename) + "." + model.getExtension());
            }
            model.storeExportForced();
            model.storeLanguageIdx();
            if (model.getOutputMode() == OutputMode.VOBSUB || model.getOutputMode() == OutputMode.SUPIFO) {
                model.storeWritePGCPal();
            }
            model.setCanceled(false);
            view.dispose();
        }
    }

    private class ForcedCheckBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            model.setExportForced(view.isForcedCheckBoxSelected());
        }
    }

    private class WritePGCPalCheckBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            model.setWritePGCPalette(view.isWritePGCPalCheckBoxSelected());
        }
    }
    
    private class ExportDialogListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent event) {
            model.setCanceled(true);
            view.dispose();
        }
    }
}
