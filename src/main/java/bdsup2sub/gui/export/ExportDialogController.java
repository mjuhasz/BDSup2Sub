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
package bdsup2sub.gui.export;

import bdsup2sub.core.OutputMode;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.ToolBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ExportDialogController {

    private ExportDialogModel model;
    private ExportDialogView view;

    public ExportDialogController(ExportDialogModel model, ExportDialogView view) {
        this.model = model;
        this.view = view;

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
        public void actionPerformed(ActionEvent e) {
            String filename = view.getFilenameTextFieldText();
            if (filename != null) {
                model.setFilename(FilenameUtils.removeExtension(filename) + "." + model.getExtension());
            }
        }
    }

    private class FilenameButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String parent = FilenameUtils.getParent(model.getFilename());
            String defaultFilename = FilenameUtils.getName(model.getFilename());
            String filename = ToolBox.getFilename(parent, defaultFilename, new String[]{model.getExtension()}, false, view.getOwner());
            if (filename != null) {
                model.setFilename(FilenameUtils.removeExtension(filename) + "." + model.getExtension());
                view.setFilenameTextFieldText(model.getFilename());
            }
        }
    }

    private class LanguageComboBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            model.setLanguageIdx(view.getLanguageComboBoxSelectedItem());
        }
    }

    private class CancelButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.setCanceled(true);
            view.dispose();
        }
    }

    private class SaveButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
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
        public void itemStateChanged(ItemEvent e) {
            model.setExportForced(view.isForcedCheckBoxSelected());
        }
    }

    private class WritePGCPalCheckBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            model.setWritePGCPalette(view.isWritePGCPalCheckBoxSelected());
        }
    }
}
