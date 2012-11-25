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

import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.*;
import bdsup2sub.gui.conversion.ConversionDialog;
import bdsup2sub.gui.move.MoveDialog;
import bdsup2sub.gui.palette.DvdPaletteDialog;
import bdsup2sub.gui.edit.EditDialog;
import bdsup2sub.gui.export.ExportDialog;
import bdsup2sub.gui.palette.FramePaletteDialog;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.StreamUtils;
import bdsup2sub.utils.ToolBox;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static bdsup2sub.core.Configuration.*;
import static bdsup2sub.core.Constants.DEFAULT_DVD_PALETTE;

class MainFrameController {

    private final MainFrameView view;
    private final MainFrameModel model;
    private final Object threadSemaphore = new Object();
    private final ApplicationListener applicationListener = new MacOSXApplicationListener();

    public MainFrameController(MainFrameModel model, MainFrameView view) {
        this.view = view;
        this.model = model;

        view.addWindowListener(new MainWindowListener());

        addFileMenuActionListeners();
        addEditMenuActionListeners();
        addSettingsMenuActionListeners();
        addAboutMenuActionListeners();

        addComboBoxActionListeners();
        addComboBoxDocumentListeners();

        addPopupMenuActionListeners();

        view.addLayoutPaneMouseListener(new LayoutPaneMouseListener());

        view.addTransferHandler(new DragAndDropTransferHandler());

        if (model.isSourceFileSpecifiedOnCmdLine()) {
            load(model.getLoadPath());
        }
    }

    public ApplicationListener getApplicationListener() {
        return applicationListener;
    }

    private void addFileMenuActionListeners() {
        view.addLoadMenuItemActionListener(new LoadMenuItemActionListener());
        view.addRecentFilesMenuItemActionListener(new RecentMenuItemActionListener());
        view.addSaveMenuItemActionListener(new SaveMenuItemActionListener());
        view.addCloseMenuItemActionListener(new CloseMenuItemActionListener());
        view.addQuitMenuItemActionListener(new QuitMenuItemActionListener());
    }

    private void addEditMenuActionListeners() {
        view.addEditFrameMenuItemActionListener(new EditFrameMenuItemActionListener());
        view.addEditDefaultDvdPaletteMenuItemActionListener(new EditDefaultDvdPaletteMenuItemActionListener());
        view.addEditImportedDvdPaletteMenuItemActionListener(new EditImportedDvdPaletteMenuItemActionListener());
        view.addEditDvdFramePaletteMenuItemActionListener(new EditDvdFramePaletteMenuItemActionListener());
        view.addMoveAllMenuItemActionListener(new MoveAllMenuItemActionListener());
        view.addResetCropOffsetMenuItemActionListener(new ResetCropOffsetMenuItemActionListener());
    }

    private void addSettingsMenuActionListeners() {
        view.addConversionSettingsMenuItemActionListener(new ConversionSettingsMenuItemActionListener());
        view.addSwapCrCbMenuItemActionListener(new SwapCrCbMenuItemActionListener());
        view.addFixInvisibleFramesMenuItemActionListener(new FixInvisibleFramesMenuItemActionListener());
        view.addVerboseOutputMenuItemActionListener(new VerboseOutputMenuItemActionListener());
    }

    private void addAboutMenuActionListeners() {
        view.addHelpMenuItemActionListener(new HelpMenuItemActionListener());
        view.addAboutMenuItemActionListener(new AboutMenuItemActionListener());
    }

    private void addComboBoxActionListeners() {
        view.addSubNumComboBoxActionListener(new SubNumComboBoxActionListener());
        view.addAlphaThresholdComboBoxActionListener(new AlphaThresholdComboBoxActionListener());
        view.addMedLowThresholdComboBoxActionListener(new MedLowThresholdComboBoxActionListener());
        view.addHiMedThresholdComboBoxActionListener(new HiMedThresholdComboBoxActionListener());
        view.addOutputFormatComboBoxActionListener(new OutputFormatComboBoxActionListener());
        view.addPaletteComboBoxActionListener(new PaletteComboBoxActionListener());
        view.addFilterComboBoxActionListener(new FilterComboBoxActionListener());
    }

    private void addComboBoxDocumentListeners() {
        view.addSubNumComboBoxDocumentListener(new SubNumComboBoxDocumentListener());
        view.addAlphaThresholdComboBoxDocumentListener(new AlphaThresholdComboBoxDocumentListener());
        view.addMedLowThresholdComboBoxDocumentListener(new MedLowThresholdComboBoxDocumentListener());
        view.addHiMedThresholdComboBoxDocumentListener(new HiMedThresholdComboBoxDocumentListener());
    }

    private void addPopupMenuActionListeners() {
        view.addCopyPopupMenuItemActionListener(new CopyPopupMenuItemActionListener());
        view.addClearPopupMenuItemActionListener(new ClearPopupMenuItemActionListener());
        view.addConsoleMouseListener(new MouseListener());
    }

    private class LoadMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            loadFile();
        }
    }

    private void loadFile() {
        List<String> extensions = Arrays.asList(new String[]{"idx", "ifo", "sub", "sup", "xml"});
        view.setConsoleText("");
        String parent = FilenameUtils.getParent(model.getLoadPath());
        String defaultFilename = FilenameUtils.getName(model.getLoadPath());
        final String filename = ToolBox.getFilename(parent, defaultFilename, extensions, true, view);
        (new Thread() {
            @Override
            public void run() {
                load(filename);
            } }).start();
    }

    private class RecentMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            view.setConsoleText("");
            final String fname = event.getActionCommand();
            (new Thread() {
                @Override
                public void run() {
                    load(fname);
                } }).start();
        }
    }

    private class SaveMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            boolean showException = true;
            OutputMode outputMode = model.getOutputMode();
            String path;
            try {
                path = model.getSavePath() + File.separatorChar + model.getSaveFilename() + "_exp.";
                if (outputMode == OutputMode.VOBSUB) {
                    path += "idx";
                } else if (outputMode == OutputMode.SUPIFO) {
                    path += "ifo";
                } else if (outputMode == OutputMode.BDSUP) {
                    path += "sup";
                } else {
                    path += "xml";
                }

                ExportDialog exportDialog = new ExportDialog(path, view);
                exportDialog.setVisible(true);

                String fn = exportDialog.getFilename();
                if (!exportDialog.wasCanceled() && !fn.isEmpty()) {
                    model.setSavePath(FilenameUtils.getParent(fn));
                    model.setSaveFilename(FilenameUtils.removeExtension(FilenameUtils.getName(fn)).replaceAll("_exp$",""));
                    //
                    File fi,fs;
                    if (outputMode == OutputMode.VOBSUB) {
                        fi = new File(FilenameUtils.removeExtension(fn) + ".idx");
                        fs = new File(FilenameUtils.removeExtension(fn) + ".sub");
                    } else if (outputMode == OutputMode.SUPIFO) {
                        fi = new File(FilenameUtils.removeExtension(fn) + ".ifo");
                        fs = new File(FilenameUtils.removeExtension(fn) + ".sup");
                    } else {
                        fs = new File(FilenameUtils.removeExtension(fn) + ".sup");
                        fi = fs; // we don't need the idx file
                    }
                    if (fi.exists() || fs.exists()) {
                        showException = false;
                        if ((fi.exists() && !fi.canWrite()) || (fs.exists() && !fs.canWrite())) {
                            throw new CoreException("Target is write protected.");
                        }
                        if (JOptionPane.showConfirmDialog(view, "Target exists! Overwrite?", "", JOptionPane.YES_NO_OPTION) == 1) {
                            throw new CoreException("Target exists. Aborted by user.");
                        }
                        showException = true;
                    }
                    // start conversion
                    Core.createSubThreaded(fn, view);
                    view.warningDialog();
                }
            } catch (CoreException ex) {
                if (showException) {
                    view.error(ex.getMessage());
                }
            } catch (Exception ex) {
                ToolBox.showException(ex);
                view.exit(4);
            } finally {
                view.flushConsole();
            }
        }
    }

    private class CloseMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            Core.close();
            view.closeSub();
        }
    }
    private class QuitMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            exit();
        }
    }

    private void exit() {
        view.exit(0);
    }

    private class DragAndDropTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            Transferable t = support.getTransferable();
            try {
                List<File> flist = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
                load(flist.get(0).getAbsolutePath());
            } catch (UnsupportedFlavorException ex) {
                return false;
            } catch (IOException ex) {
                return false;
            }
            return true;
        }
    }

    private void load(String fname) {
        if (fname != null) {
            if (!new File(fname).exists()) {
                JOptionPane.showMessageDialog(view, "File '" + fname + "' does not exist", "File not found!", JOptionPane.WARNING_MESSAGE);
            } else {
                synchronized (threadSemaphore) {
                    boolean xml = FilenameUtils.getExtension(fname).equalsIgnoreCase("xml");
                    boolean idx = FilenameUtils.getExtension(fname).equalsIgnoreCase("idx");
                    boolean ifo = FilenameUtils.getExtension(fname).equalsIgnoreCase("ifo");
                    byte id[] = ToolBox.getFileID(fname, 4);
                    StreamID sid = (id == null) ? StreamID.UNKNOWN : StreamUtils.getStreamID(id);
                    if (idx || xml || ifo || sid != StreamID.UNKNOWN) {
                        view.setTitle(Constants.APP_NAME + " " + Constants.APP_VERSION + " - " + fname);
                        model.setSubIndex(0);
                        model.setLoadPath(fname);
                        String loadPath = model.getLoadPath();
                        model.setSaveFilename(FilenameUtils.removeExtension(FilenameUtils.getName(loadPath)));
                        model.setSavePath(FilenameUtils.getParent(loadPath));
                        view.enableCoreComponents(false);
                        view.enableVobsubBits(false);
                        try {
                            Core.readStreamThreaded(loadPath, view, sid);
                            view.warningDialog();
                            int num = Core.getNumFrames();
                            Core.setReady(false);
                            view.initSubNumComboBox(num);
                            view.initAlphaThresholdComboBoxSelectedIndices();
                            //
                            if (model.getCropOffsetY() > 0) {
                                if (JOptionPane.showConfirmDialog(view, "Reset Crop Offset?",
                                        "", JOptionPane.YES_NO_OPTION) == 0) {
                                    model.setCropOffsetY(0);
                                }
                            }

                            ConversionDialog conversionDialog = new ConversionDialog(view);
                            conversionDialog.enableOptionMove(model.getMoveCaptions());
                            conversionDialog.setVisible(true);
                            if (!conversionDialog.wasCanceled()) {
                                Core.scanSubtitles();
                                if (model.getMoveCaptions()) {
                                    Core.moveAllThreaded(view);
                                }
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                Core.setReady(true);
                                view.setQuitMenuItemEnabled(true);
                                view.refreshSrcFrame(subIndex);
                                view.refreshTrgFrame(subIndex);
                                view.enableCoreComponents(true);
                                if (model.getOutputMode() == OutputMode.VOBSUB || Core.getInputMode() == InputMode.SUPIFO) {
                                    view.enableVobsubBits(true);
                                }
                                model.addToRecentFiles(loadPath);
                                view.updateRecentFilesMenu();
                            } else {
                                view.closeSub();
                                view.printWarn("Loading cancelled by user.");
                                Core.close();
                            }
                        } catch (CoreException ex) {
                            view.setLoadMenuItemEnabled(true);
                            view.updateRecentFilesMenu();
                            view.setComboBoxOutFormatEnabled(true);
                            view.error(ex.getMessage());
                        } catch (Exception ex) {
                            ToolBox.showException(ex);
                            view.exit(4);
                        } finally {
                            view.flushConsole();
                        }
                    } else {
                        JOptionPane.showMessageDialog(view, "This is not a supported SUP stream", "Wrong format!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }

    private class EditFrameMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (Core.isReady()) {
                EditDialog ed = new EditDialog(view);
                ed.setIndex(model.getSubIndex());
                ed.setVisible(true);
                model.setSubIndex(ed.getIndex());
                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                view.refreshSrcFrame(subIndex);
                                view.refreshTrgFrame(subIndex);
                                view.setSubNumComboBoxSelectedIndex(subIndex);
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }
                        }
                    }
                }).start();
            }
        }
    }

    private class EditDefaultDvdPaletteMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            final String colorNames[] = {
                    "white","light gray","dark gray",
                    "Color 1 light","Color 1 dark",
                    "Color 2 light","Color 2 dark",
                    "Color 3 light","Color 3 dark",
                    "Color 4 light","Color 4 dark",
                    "Color 5 light","Color 5 dark",
                    "Color 6 light","Color 6 dark"
            };
            Color currentColors[] = new Color[15];
            Color defaultColors[] = new Color[15];
            for (int i=0; i < currentColors.length; i++) {
                currentColors[i] = Core.getCurrentDVDPalette().getColor(i+1);
                defaultColors[i] = DEFAULT_DVD_PALETTE.getColor(i+1);
            }
            DvdPaletteDialog dvdPaletteDialog = new DvdPaletteDialog(view, colorNames, currentColors, defaultColors, model.getColorProfilePath());
            dvdPaletteDialog.setVisible(true);
            if (!dvdPaletteDialog.wasCanceled()) {
                currentColors = dvdPaletteDialog.getColors();
                model.setColorProfilePath(dvdPaletteDialog.getPath());
                for (int i=0; i < currentColors.length; i++) {
                    Core.getCurrentDVDPalette().setColor(i+1, currentColors[i]);
                }

                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                if (Core.isReady()) {
                                    int subIndex = model.getSubIndex();
                                    Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                    view.refreshTrgFrame(subIndex);
                                }
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }

                        }
                    }
                }).start();
            }
        }
    }

    private class EditImportedDvdPaletteMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            final String colorNames[] = {
                    "Color 0", "Color 1", "Color 2", "Color 3",
                    "Color 4", "Color 5", "Color 6", "Color 7",
                    "Color 8", "Color 9", "Color 10", "Color 11",
                    "Color 12", "Color 13", "Color 14", "Color 15",
            };
            Color currentColors[] = new Color[16];
            Color defaultColors[] = new Color[16];
            for (int i=0; i < currentColors.length; i++) {
                currentColors[i] = Core.getCurSrcDVDPalette().getColor(i);
                defaultColors[i] = Core.getDefSrcDVDPalette().getColor(i);
            }
            DvdPaletteDialog dvdPaletteDialog = new DvdPaletteDialog(view, colorNames, currentColors, defaultColors, model.getColorProfilePath());
            dvdPaletteDialog.setVisible(true);
            if (!dvdPaletteDialog.wasCanceled()) {
                currentColors = dvdPaletteDialog.getColors();
                model.setColorProfilePath(dvdPaletteDialog.getPath());
                Palette p = new Palette(currentColors.length, true);
                for (int i=0; i<currentColors.length; i++) {
                    p.setColor(i, currentColors[i]);
                }
                Core.setCurSrcDVDPalette(p);

                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                if (Core.isReady()) {
                                    int subIndex = model.getSubIndex();
                                    Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                    view.refreshSrcFrame(subIndex);
                                    view.refreshTrgFrame(subIndex);
                                }
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }

                        }
                    }
                }).start();
            }
        }
    }

    private class EditDvdFramePaletteMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            FramePaletteDialog framePaletteDialog = new FramePaletteDialog(view, model.getSubIndex());
            framePaletteDialog.setVisible(true);

            (new Thread() {
                @Override
                public void run() {
                    synchronized (threadSemaphore) {
                        try {
                            if (Core.isReady()) {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                view.refreshSrcFrame(subIndex);
                                view.refreshTrgFrame(subIndex);
                            }
                        } catch (CoreException ex) {
                            view.error(ex.getMessage());
                        } catch (Exception ex) {
                            ToolBox.showException(ex);
                            view.exit(4);
                        }

                    }
                }
            }).start();
        }
    }

    private class MoveAllMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (Core.isReady()) {
                MoveDialog moveDialog = new MoveDialog(view);
                moveDialog.setCurrentSubtitleIndex(model.getSubIndex());
                moveDialog.setVisible(true);
                if (model.getMoveCaptions()) {
                    try {
                        Core.moveAllThreaded(view);
                    } catch (CoreException ex) {
                        view.error(ex.getMessage());
                    } catch (Exception ex) {
                        ToolBox.showException(ex);
                        view.exit(4);
                    }
                }
                model.setSubIndex(moveDialog.getCurrentSubtitleIndex());
                view.setLayoutPaneAspectRatio(moveDialog.getTrgRatio());
                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                view.refreshSrcFrame(subIndex);
                                view.refreshTrgFrame(subIndex);
                                view.setSubNumComboBoxSelectedIndex(subIndex);
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }
                        }
                    }
                }).start();
            }
        }
    }

    private class ResetCropOffsetMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            model.setCropOffsetY(0);
            view.setLayoutPaneCropOffsetY(model.getCropOffsetY());
            view.repaintLayoutPane();
        }
    }

    private class ConversionSettingsMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            final Resolution rOld = model.getOutputResolution();
            final double fpsTrgOld = model.getFPSTrg();
            final boolean changeFpsOld = model.getConvertFPS();
            final int delayOld = model.getDelayPTS();
            final double fsXOld;
            final double fsYOld;
            if (model.getApplyFreeScale()) {
                fsXOld = model.getFreeScaleX();
                fsYOld = model.getFreeScaleY();
            } else {
                fsXOld = 1.0;
                fsYOld = 1.0;
            }
            // show dialog
            ConversionDialog trans = new ConversionDialog(view);
            trans.enableOptionMove(false);
            trans.setVisible(true);

            if (!trans.wasCanceled()) {
                // create and show image
                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                if (Core.isReady()) {
                                    int subIndex = model.getSubIndex();
                                    Core.reScanSubtitles(rOld, fpsTrgOld, delayOld, changeFpsOld, fsXOld, fsYOld);
                                    Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                    view.refreshTrgFrame(subIndex);
                                }
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }
                        }
                    }
                }).start();
            }
        }
    }

    private class SwapCrCbMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            boolean selected = view.isSwapCrCbSelected();
            model.setSwapCrCb(selected);
            // create and show image
            (new Thread() {
                @Override
                public void run() {
                    synchronized (threadSemaphore) {
                        try {
                            if (Core.isReady()) {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                view.refreshSrcFrame(subIndex);
                                view.refreshTrgFrame(subIndex);
                            }
                        } catch (CoreException ex) {
                            view.error(ex.getMessage());
                        } catch (Exception ex) {
                            ToolBox.showException(ex);
                            view.exit(4);
                        }

                    }
                }
            }).start();
        }
    }

    private class FixInvisibleFramesMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            boolean selected = view.isFixInvisibleFramesSelected();
            model.setFixZeroAlpha(selected);
        }
    }

    private class VerboseOutputMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            boolean selected = view.isVerboseOutputSelected();
            model.setVerbose(selected);
        }
    }

    private class HelpMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                Desktop.getDesktop().browse(URI.create("http://github.com/mjuhasz/BDSup2Sub/wiki"));
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private class AboutMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            showAboutDialog();
        }

    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(view,
                Constants.APP_NAME + " v" + Constants.APP_VERSION + "\n"
                        + "Created by " + Constants.DEVELOPERS + "\n"
                        + "\n"
                        + "Built on " + Constants.BUILD_DATE + "\n"
                        + "Copyright © 2009 Volker Oth, 2011-2012 Miklos Juhasz",
                Constants.APP_NAME,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(getClass().getClassLoader().getResource("icons/bdsup2sub_32.png")));
    }

    private class SubNumComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (Core.isReady()) {
                int num = Core.getNumFrames();
                int idx;
                try {
                    idx = Integer.parseInt(view.getSubNumComboBoxSelectedItem().toString()) - 1;
                } catch (NumberFormatException ex) {
                    idx = model.getSubIndex(); // invalid number -> keep old value
                }

                if (idx < 0) {
                    idx = 0;
                }
                if (idx >= num) {
                    idx = num-1;
                }
                model.setSubIndex(idx);
                view.setSubNumComboBoxSelectedIndex(model.getSubIndex());

                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex +1, Core.getNumFrames());
                                view.refreshSrcFrame(subIndex);
                                view.refreshTrgFrame(subIndex);
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }

                        }
                    }
                }).start();
            }
        }
    }

    private class SubNumComboBoxDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            check();
        }

        private void check() {
            if (Core.isReady()) {
                int idx = ToolBox.getInt(view.getSubNumComboBoxText()) - 1;
                if (idx < 0 || idx >= Core.getNumFrames()) {
                    view.setSubNumComboBoxBackground(ERROR_BACKGROUND);
                } else {
                    model.setSubIndex(idx);
                    (new Thread() {
                        @Override
                        public void run() {
                            synchronized (threadSemaphore) {
                                try {
                                    int subIndex = model.getSubIndex();
                                    Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                    view.refreshSrcFrame(subIndex);
                                    view.refreshTrgFrame(subIndex);
                                } catch (CoreException ex) {
                                    view.error(ex.getMessage());
                                } catch (Exception ex) {
                                    ToolBox.showException(ex);
                                    view.exit(4);
                                }

                            }
                        }
                    }).start();
                    view.setSubNumComboBoxBackground(OK_BACKGROUND);
                }
            }
        }
    }

    private class AlphaThresholdComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (Core.isReady()) {
                int idx;
                try {
                    idx = Integer.parseInt(view.getAlphaThresholdComboBoxSelectedItem().toString());
                } catch (NumberFormatException ex) {
                    idx = model.getAlphaThreshold(); // invalid number -> keep old value
                }

                if (idx < 0) {
                    idx = 0;
                }
                if (idx > 255) {
                    idx = 255;
                }

                model.setAlphaThreshold(idx);
                view.setAlphaThresholdComboBoxSelectedIndex(model.getAlphaThreshold());

                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                view.refreshTrgFrame(subIndex);
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }
                        }
                    }
                }).start();
            }
        }
    }

    private class AlphaThresholdComboBoxDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            check();
        }

        private void check() {
            if (Core.isReady()) {
                int idx = ToolBox.getInt(view.getAlphaThresholdComboBoxText());
                if (idx < 0 || idx > 255) {
                    view.setAlphaThresholdComboBoxBackground(ERROR_BACKGROUND);
                } else {
                    model.setAlphaThreshold(idx);
                    (new Thread() {
                        @Override
                        public void run() {
                            synchronized (threadSemaphore) {
                                try {
                                    int subIndex = model.getSubIndex();
                                    Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                    view.refreshTrgFrame(subIndex);
                                } catch (CoreException ex) {
                                    view.error(ex.getMessage());
                                } catch (Exception ex) {
                                    ToolBox.showException(ex);
                                    view.exit(4);
                                }
                            }
                        }
                    }).start();
                    view.setAlphaThresholdComboBoxBackground(OK_BACKGROUND);
                }
            }
        }
    }

    private class MedLowThresholdComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (Core.isReady()) {
                int lumThr[] = model.getLuminanceThreshold();
                int idx;
                try {
                    idx = Integer.parseInt(view.getMedLowThresholdComboBoxSelectedItem().toString());
                } catch (NumberFormatException ex) {
                    idx = lumThr[1]; // invalid number -> keep old value
                }

                if (idx >= lumThr[0]) { // must be smaller than med/high threshold
                    idx = lumThr[0] - 1;
                }

                if (idx < 0) {
                    idx = 0;
                }
                if (idx > 255) {
                    idx = 255;
                }

                lumThr[1] = idx;
                model.setLuminanceThreshold(lumThr);

                final int index = idx;
                view.setMedLowThresholdComboBoxSelectedIndex(index);

                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                view.refreshTrgFrame(subIndex);
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }

                        }
                    }
                }).start();
            }
        }
    }

    private class MedLowThresholdComboBoxDocumentListener implements  DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            check();
        }

        private void check() {
            if (Core.isReady()) {
                int lumThr[] = model.getLuminanceThreshold();
                int idx = ToolBox.getInt(view.getMedLowThresholdComboBoxText());
                if (idx < 0 || idx > 255 | idx >= lumThr[0])
                    view.setMedLowThresholdComboBoxBackground(ERROR_BACKGROUND);
                else {
                    lumThr[1] = idx;
                    model.setLuminanceThreshold(lumThr);
                    (new Thread() {
                        @Override
                        public void run() {
                            synchronized (threadSemaphore) {
                                try {
                                    int subIndex = model.getSubIndex();
                                    Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                    view.refreshTrgFrame(subIndex);
                                } catch (CoreException ex) {
                                    view.error(ex.getMessage());
                                } catch (Exception ex) {
                                    ToolBox.showException(ex);
                                    view.exit(4);
                                }

                            } } }).start();
                    view.setMedLowThresholdComboBoxBackground(OK_BACKGROUND);
                }
            }
        }
    }

    private class HiMedThresholdComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (Core.isReady()) {
                int lumThr[] = model.getLuminanceThreshold();
                int idx;
                try {
                    idx = Integer.parseInt(view.getHiMedThresholdComboBoxSelectedItem().toString());
                } catch (NumberFormatException ex) {
                    idx = lumThr[0]; // invalid number -> keep old value
                }

                if (idx <= lumThr[1]) { // must be greater than med/low threshold
                    idx = lumThr[1] + 1;
                }

                if (idx < 0) {
                    idx = 0;
                }
                if (idx > 255) {
                    idx = 255;
                }

                lumThr[0] = idx;
                model.setLuminanceThreshold(lumThr);
                view.setHiMedThresholdComboBoxSelectedIndex(model.getLuminanceThreshold()[0]);

                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                view.refreshTrgFrame(subIndex);
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }

                        }
                    }
                }).start();
            }
        }
    }

    private class HiMedThresholdComboBoxDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            check();
        }

        private void check() {
            if (Core.isReady()) {
                int lumThr[] = model.getLuminanceThreshold();
                int idx = ToolBox.getInt(view.getHiMedThresholdComboBoxText());
                if (idx < 0 || idx > 255 | idx <= lumThr[1]) {
                    view.setHiMedThresholdComboBoxBackground(ERROR_BACKGROUND);
                } else {
                    lumThr[0] = idx;
                    model.setLuminanceThreshold(lumThr);
                    (new Thread() {
                        @Override
                        public void run() {
                            synchronized (threadSemaphore) {
                                try {
                                    int subIndex = model.getSubIndex();
                                    Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                    view.refreshTrgFrame(subIndex);
                                } catch (CoreException ex) {
                                    view.error(ex.getMessage());
                                } catch (Exception ex) {
                                    ToolBox.showException(ex);
                                    view.exit(4);
                                }

                            } } }).start();
                    view.setHiMedThresholdComboBoxBackground(OK_BACKGROUND);
                }
            }
        }
    }

    private class OutputFormatComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (Core.isReady()) {
                int idx = view.getOutputFormatComboBoxSelectedIndex();
                for (OutputMode m : OutputMode.values()) {
                    if (idx == m.ordinal()) {
                        model.setOutputMode(m);
                        break;
                    }
                }

                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                view.refreshTrgFrame(subIndex);
                                if (model.getOutputMode() == OutputMode.VOBSUB || model.getOutputMode() == OutputMode.SUPIFO) {
                                    view.enableVobsubBits(true);
                                } else {
                                    view.enableVobsubBits(false);
                                }
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }

                        }
                    }
                }).start();
            }
        }
    }

    private class PaletteComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (Core.isReady()) {
                int idx = view.getPaletteComboBoxSelectedIndex();
                for (PaletteMode m : PaletteMode.values()) {
                    if (idx == m.ordinal()) {
                        model.setPaletteMode(m);
                        break;
                    }
                }

                view.enableVobSubMenuCombo();

                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                view.refreshTrgFrame(subIndex);
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }

                        }
                    }
                }).start();
            }
        }
    }

    private class FilterComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (Core.isReady()) {
                int idx = view.getFilterComboBoxSelectedIndex();
                for (ScalingFilter s : ScalingFilter.values()) {
                    if (idx == s.ordinal()) {
                        model.setScalingFilter(s);
                        break;
                    }
                }

                (new Thread() {
                    @Override
                    public void run() {
                        synchronized (threadSemaphore) {
                            try {
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                view.refreshTrgFrame(subIndex);
                            } catch (CoreException ex) {
                                view.error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                view.exit(4);
                            }

                        }
                    }
                }).start();
            }
        }
    }

    private class CopyPopupMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String selectedText = view.getConsoleSelectedText();
            try {
                if (selectedText != null) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), view);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(selectedText), view);
                }
            } catch (OutOfMemoryError ex) {
                JOptionPane.showMessageDialog(view, "Out of heap! Use -Xmx256m to increase heap!" , "Error!", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private class ClearPopupMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            view.setConsoleText("");
        }
    }

    private class MouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent event) {
            showPopupIfApplicable(event);
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            showPopupIfApplicable(event);
        }

        private void showPopupIfApplicable(MouseEvent event) {
            if (event.isPopupTrigger()) {
                boolean canCopy = (view.getConsoleSelectedText() != null);
                view.setCopyPopupMenuItemEnabled(canCopy);
                view.showPopupMenu(event.getX(), event.getY());
            }
        }
    }

    private class MainWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            exit();
        }
    }

    private class LayoutPaneMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (Core.isReady()) {
                    EditDialog ed = new EditDialog(view);
                    ed.setIndex(model.getSubIndex());
                    ed.setVisible(true);
                    model.setSubIndex(ed.getIndex());
                    (new Thread() {
                        @Override
                        public void run() {
                            synchronized (threadSemaphore) {
                                try {
                                    int subIndex = model.getSubIndex();
                                    Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                    view.refreshSrcFrame(subIndex);
                                    view.refreshTrgFrame(subIndex);
                                    view.setSubNumComboBoxSelectedIndex(subIndex);
                                } catch (CoreException ex) {
                                    view.error(ex.getMessage());
                                } catch (Exception ex) {
                                    ToolBox.showException(ex);
                                    view.exit(4);
                                }

                            }
                        }
                    }).start();
                }
            }
        }
    }

    public class MacOSXApplicationListener implements ApplicationListener {

        private void handle(ApplicationEvent event, String message) {
            event.setHandled(true);
            JOptionPane.showMessageDialog(view,
                    message,
                    Constants.APP_NAME,
                    JOptionPane.INFORMATION_MESSAGE,
                    new ImageIcon(getClass().getClassLoader().getResource("icons/bdsup2sub_32.png")));
        }

        public void handleAbout(ApplicationEvent event) {
            event.setHandled(true);
            showAboutDialog();
        }

        public void handleOpenApplication(ApplicationEvent event) {
            event.setHandled(true);
            // application open, nothing to do
        }

        public void handleOpenFile(ApplicationEvent event) {
            event.setHandled(true);
            loadFile();
        }

        public void handlePreferences(ApplicationEvent event) {
            event.setHandled(true);
            handle(event, "Preferences not implemented");
        }

        public void handlePrintFile(ApplicationEvent event) {
            event.setHandled(true);
            handle(event, "Printing not implemented");
        }

        public void handleQuit(ApplicationEvent event) {
            event.setHandled(true);
            exit();
        }

        public void handleReOpenApplication(ApplicationEvent event) {
            event.setHandled(true);
            view.setVisible(true);
        }
    }
}
