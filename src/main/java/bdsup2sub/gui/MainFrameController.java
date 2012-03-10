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
package bdsup2sub.gui;

import bdsup2sub.core.*;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static bdsup2sub.core.Constants.APP_NAME_AND_VERSION;

public class MainFrameController {

    private MainFrameView view;
    private MainFrameModel model;

    public MainFrameController(MainFrameModel model, MainFrameView view) {
        this.view = view;
        this.model = model;

        addActionListeners();
        view.addTransferHandler(new DragAndDropTransferHandler());

        if (model.isSourceFileSpecifiedOnCmdLine()) {
            load(model.getLoadPath());
        }
    }

    private void addActionListeners() {
        view.addLoadMenuActionListener(new LoadMenuActionListener());
        view.addRecentMenuActionListener(new RecentMenuActionListener());
    }

    private class LoadMenuActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String[] ext = new String[] {"idx", "ifo", "sub", "sup", "xml"};
            view.setConsoleText("");
            String p = FilenameUtils.getParent(model.getLoadPath());
            String fn = FilenameUtils.getName(model.getLoadPath());
            final String fname = ToolBox.getFileName(p, fn, ext, true, view);
            (new Thread() {
                @Override
                public void run() {
                    load(fname);
                } }).start();
        }
    }

    private class RecentMenuActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.setConsoleText("");
            final String fname = e.getActionCommand();
            (new Thread() {
                @Override
                public void run() {
                    load(fname);
                } }).start();
        }
    }

    private void load(String fname) {
        if (fname != null) {
            if (!new File(fname).exists()) {
                JOptionPane.showMessageDialog(view, "File '" + fname + "' does not exist",
                        "File not found!", JOptionPane.WARNING_MESSAGE);
            } else {
                synchronized (view.threadSemaphore) {
                    boolean xml = FilenameUtils.getExtension(fname).equalsIgnoreCase("xml");
                    boolean idx = FilenameUtils.getExtension(fname).equalsIgnoreCase("idx");
                    boolean ifo = FilenameUtils.getExtension(fname).equalsIgnoreCase("ifo");
                    byte id[] = ToolBox.getFileID(fname, 4);
                    StreamID sid = (id == null) ? StreamID.UNKNOWN : Core.getStreamID(id);
                    if (idx || xml || ifo || sid != StreamID.UNKNOWN) {
                        view.setTitle(APP_NAME_AND_VERSION + " - " + fname);
                        model.setSubIndex(0);
                        model.setLoadPath(fname);
                        String loadPath = model.getLoadPath();
                        model.setSaveFilename(FilenameUtils.removeExtension(FilenameUtils.getName(loadPath)));
                        model.setSavePath(FilenameUtils.getParent(loadPath));
                        view.enableCoreComponents(false);
                        view.enableVobsubStuff(false);
                        try {
                            Core.readStreamThreaded(loadPath, view, sid);
                            view.warningDialog();
                            int num = Core.getNumFrames();
                            Core.setReady(false);
                            view.initComboBoxSubNum(num);
                            view.initComboBoxThrSelectedIndices();
                            //
                            if (Core.getCropOfsY() > 0) {
                                if (JOptionPane.showConfirmDialog(view, "Reset Crop Offset?",
                                        "", JOptionPane.YES_NO_OPTION) == 0) {
                                    Core.setCropOfsY(0);
                                }
                            }

                            ConversionDialog trans = new ConversionDialog(view);
                            trans.enableOptionMove(Core.getMoveCaptions());
                            trans.setVisible(true);
                            if (!trans.wasCanceled()) {
                                Core.scanSubtitles();
                                if (Core.getMoveCaptions()) {
                                    Core.moveAllThreaded(view);
                                }
                                int subIndex = model.getSubIndex();
                                Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                Core.setReady(true);
                                view.setMenuItemExitEnabled(true);
                                view.refreshSrcFrame(subIndex);
                                view.refreshTrgFrame(subIndex);
                                view.enableCoreComponents(true);
                                if (Core.getOutputMode() == OutputMode.VOBSUB || Core.getInputMode() == InputMode.SUPIFO) {
                                    view.enableVobsubStuff(true);
                                }
                                // tell the core that a stream was loaded via the GUI
                                Core.loadedHook();
                                Core.addRecent(loadPath);
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
                            view.flush();
                        }
                    } else {
                        JOptionPane.showMessageDialog(view, "This is not a supported SUP stream",
                                "Wrong format!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }

    private class DragAndDropTransferHandler extends TransferHandler {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support))
                return false;

            Transferable t = support.getTransferable();

            try {
                List<File> flist = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
                load(flist.get(0).getAbsolutePath());
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
            return true;
        }
    };
}
