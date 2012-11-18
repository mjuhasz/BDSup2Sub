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
package bdsup2sub.gui.palette;

import bdsup2sub.core.Constants;
import bdsup2sub.core.CoreException;
import bdsup2sub.tools.Props;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Collections;

class DvdPaletteDialogController {

    private final DvdPaletteDialogModel model;
    private final DvdPaletteDialogView view;

    public DvdPaletteDialogController(DvdPaletteDialogModel model, DvdPaletteDialogView view) {
        this.model = model;
        this.view = view;

        view.addWindowListener(new ColorDialogWindowListener());
        view.setColorListCellRenderer(new ColorListCellRenderer());
        view.setColorListMouseListener(new ColorListMouseListener());

        view.addOkButtonActionListener(new OkButtonActionListener());
        view.addCancelButtonActionListener(new CancelButtonActionListener());
        view.addDefaultButtonActionListener(new DefaultButtonActionListener());
        view.addColorButtonActionListener(new ColorButtonActionListener());
        view.addSaveButtonActionListener(new SaveButtonActionListener());
        view.addLoadButtonActionListener(new LoadButtonActionListener());
    }

    private class ColorDialogWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            model.setSelectedColors(model.getDefaultColors());
        }
    }

    private class OkButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.setCanceled(false);
            view.dispose();
        }
    }

    private class CancelButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.setSelectedColors(model.getDefaultColors());
            view.dispose();
        }
    }

    private class DefaultButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i=0; i < model.getColorNames().length; i++) {
                model.getSelectedColors()[i] = new Color(model.getDefaultColors()[i].getRGB());
                view.paintIcon(model.getColorIcons()[i], model.getSelectedColors()[i]);
            }
            view.repaintColorList();
        }
    }

    private class ColorButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.changeColor(view.getSelectedColor());
        }
    }

    private class SaveButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String parent = FilenameUtils.getParent(model.getColorProfilePath());
            String defaultFilename = FilenameUtils.getName(model.getColorProfilePath());
            String filename = ToolBox.getFilename(parent, defaultFilename, Collections.singletonList("ini"), false, view);
            if (filename != null) {
                filename = FilenameUtils.removeExtension(filename) + ".ini";
                File file = new File(filename);
                try {
                    if (file.exists()) {
                        if ((file.exists() && !file.canWrite()) || (file.exists() && !file.canWrite())) {
                            throw new CoreException("Target is write protected.");
                        }
                        if (JOptionPane.showConfirmDialog(view, "Target exists! Overwrite?", "", JOptionPane.YES_NO_OPTION) == 1) {
                            throw new CoreException();
                        }
                    }
                    model.setColorProfilePath(filename);
                    Props colProps = new Props();
                    colProps.setHeader("COL - created by " + Constants.APP_NAME + " " + Constants.APP_VERSION);
                    for (int i=0; i<  model.getSelectedColors().length; i++) {
                        String s = String.valueOf(model.getSelectedColors()[i].getRed()) + "," +  model.getSelectedColors()[i].getGreen() + "," + model.getSelectedColors()[i].getBlue();
                        colProps.set("Color_" + i, s);
                    }
                    colProps.save(model.getColorProfilePath());
                } catch (CoreException ex) {
                    if (ex.getMessage() != null) {
                        JOptionPane.showMessageDialog(view, ex.getMessage(), "Error!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }

    private class LoadButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String parent = FilenameUtils.getParent(model.getColorProfilePath());
            String defaultFilename = FilenameUtils.getName(model.getColorProfilePath());
            String filename = ToolBox.getFilename(parent, defaultFilename, Collections.singletonList("ini"), true, view);
            if (filename != null) {
                File file = new File(filename);
                try {
                    if (file.exists()) {
                        byte id[] = ToolBox.getFileID(filename, 4);
                        if (id == null || id[0] != 0x23 || id[1] != 0x43 || id[2]!= 0x4F || id[3] != 0x4C) { //#COL
                            JOptionPane.showMessageDialog(view, "This is not a valid palette file", "Wrong format!", JOptionPane.WARNING_MESSAGE);
                            throw new CoreException();
                        }
                    } else {
                        throw new CoreException("File not found.");
                    }
                    Props colProps = new Props();
                    colProps.load(filename);
                    model.setColorProfilePath(filename);
                    for (int i=0; i < model.getSelectedColors().length; i++) {
                        String s = colProps.get("Color_"+i, "0,0,0");
                        String sp[] = s.split(",");
                        if (sp.length >= 3) {
                            int r = Integer.valueOf(sp[0].trim())&0xff;
                            int g = Integer.valueOf(sp[1].trim())&0xff;
                            int b = Integer.valueOf(sp[2].trim())&0xff;
                            model.getSelectedColors()[i] = new Color(r,g,b);
                            view.paintIcon(model.getColorIcons()[i], model.getSelectedColors()[i]);
                        }
                    }
                    view.repaintColorList();
                } catch (CoreException ex) {
                    if (ex.getMessage() != null) {
                        JOptionPane.showMessageDialog(view, ex.getMessage(), "Error!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }

    private class ColorListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,  boolean cellHasFocus) {
            Component retValue = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus);
            setText(model.getColorNames()[index]);
            setIcon(model.getColorIcons()[index]);
            return retValue;
        }
    }

    private class ColorListMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                view.changeColor(((JList) event.getSource()).locationToIndex(event.getPoint()));
            }
        }
    }
}
