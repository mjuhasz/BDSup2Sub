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

import bdsup2sub.core.Configuration;
import bdsup2sub.core.OutputMode;

class ExportDialogModel {

    private final Configuration configuration = Configuration.getInstance();

    private String filename = "";
    private String extension;
    private String dialogTitle;
    private boolean canceled;
    private int languageIdx;
    private boolean exportForced;
    private boolean writePGCPalette;

    public ExportDialogModel() {
        languageIdx = configuration.getLanguageIdx();
        exportForced = configuration.isExportForced();
        writePGCPalette = configuration.getWritePGCEditPalette();

        determineExtensionAndDialogTitle();
    }

    private void determineExtensionAndDialogTitle() {
        OutputMode outputMode = getOutputMode();
        dialogTitle = "Export ";
        if (outputMode == OutputMode.VOBSUB) {
            extension = "idx";
            dialogTitle += "SUB/IDX (VobSub)";
        } else if (outputMode == OutputMode.SUPIFO) {
            extension = "ifo";
            dialogTitle += "SUP/IFO (SUP DVD)";
        } else if (outputMode == OutputMode.BDSUP) {
            extension = "sup";
            dialogTitle += "BD SUP";
        } else {
            extension = "xml";
            dialogTitle += "XML (SONY BDN)";
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean wasCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public int getLanguageIdx() {
        return languageIdx;
    }

    public void setLanguageIdx(int languageIdx) {
        this.languageIdx = languageIdx;
    }

    public void storeLanguageIdx() {
        configuration.setLanguageIdx(languageIdx);
    }

    public boolean getExportForced() {
        return exportForced;
    }

    public void setExportForced(boolean exportForced) {
        this.exportForced = exportForced;
    }

    public void storeExportForced() {
        configuration.setExportForced(exportForced);
    }

    public boolean getWritePGCPalette() {
        return writePGCPalette;
    }

    public void setWritePGCPalette(boolean writePGCPalette) {
        this.writePGCPalette = writePGCPalette;
    }

    public void storeWritePGCPal() {
        configuration.setWritePGCEditPalette(writePGCPalette);
    }

    public OutputMode getOutputMode() {
        return configuration.getOutputMode();
    }

    public String getExtension() {
        return extension;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }
}
