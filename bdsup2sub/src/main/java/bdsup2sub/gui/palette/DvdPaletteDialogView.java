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
package bdsup2sub.gui.palette;

import bdsup2sub.gui.support.RequestFocusListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import static bdsup2sub.gui.support.GuiUtils.centerRelativeToOwner;

class DvdPaletteDialogView extends JDialog {

    private JPanel jContentPane;
    private JScrollPane jScrollPane;
    private JList jListColor;
    private JButton jButtonOk;
    private JButton jButtonCancel;
    private JButton jButtonDefault;
    private JButton jButtonColor;
    private JButton jButtonSave;
    private JButton jButtonLoad;

    private final DvdPaletteDialogModel model;

    public DvdPaletteDialogView(DvdPaletteDialogModel model, Frame frame) {
        super(frame, "Choose Colors", true);
        this.model = model;
        initialize();
    }

    private void initialize() {
        setContentPane(getJContentPane());
        setSize(372, 231);
        setResizable(false);
        centerRelativeToOwner(this);
    }

    private JPanel getJContentPane() {
        if(jContentPane == null) {
            JLabel lblColor = new JLabel();
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            lblColor.setText("Choose Color");
            lblColor.setBounds(15, 9, 80, 16);
            jContentPane.add(lblColor, null);
            jContentPane.add(getJButtonOk(), null);
            jContentPane.add(getJButtonCancel(), null);
            jContentPane.add(getJButtonDefault(), null);
            jContentPane.add(getJButtonColor(), null);
            jContentPane.add(getJButtonSave(), null);
            jContentPane.add(getJButtonLoad(), null);
            jContentPane.add(getJScrollPane(), null);
            initColorList();
        }
        return jContentPane;
    }

    private JButton getJButtonOk() {
        if (jButtonOk == null) {
            jButtonOk = new JButton();
            jButtonOk.setBounds(280, 162, 66, 20);
            jButtonOk.setText("OK");
            jButtonOk.setToolTipText("Apply changes and return");
            jButtonOk.setMnemonic('o');
            jButtonOk.addAncestorListener(new RequestFocusListener());
        }
        return jButtonOk;
    }

    void addOkButtonActionListener(ActionListener actionListener) {
        jButtonOk.addActionListener(actionListener);
    }

    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setBounds(175, 161, 70, 20);
            jButtonCancel.setText("Cancel");
            jButtonCancel.setToolTipText("Lose changes and return");
            jButtonCancel.setMnemonic('c');
        }
        return jButtonCancel;
    }

    void addCancelButtonActionListener(ActionListener actionListener) {
        jButtonCancel.addActionListener(actionListener);
    }

    private JButton getJButtonDefault() {
        if (jButtonDefault == null) {
            jButtonDefault = new JButton();
            jButtonDefault.setBounds(175, 60, 170, 20);
            jButtonDefault.setText("Restore default Colors");
            jButtonDefault.setToolTipText("Revert to default colors");
            jButtonDefault.setMnemonic('r');
        }
        return jButtonDefault;
    }

    void addDefaultButtonActionListener(ActionListener actionListener) {
        jButtonDefault.addActionListener(actionListener);
    }

    private JButton getJButtonColor() {
        if (jButtonColor == null) {
            jButtonColor = new JButton();
            jButtonColor.setBounds(175, 30, 170, 20);
            jButtonColor.setText("Change Color");
            jButtonColor.setToolTipText("Edit the selected color");
            jButtonColor.setMnemonic('h');
        }
        return jButtonColor;
    }

    void addColorButtonActionListener(ActionListener actionListener) {
        jButtonColor.addActionListener(actionListener);
    }

    private JButton getJButtonSave() {
        if (jButtonSave == null) {
            jButtonSave = new JButton();
            jButtonSave.setBounds(new Rectangle(175, 92, 170, 20));
            jButtonSave.setText("Save Palette");
            jButtonSave.setToolTipText("Save the current palette settings in an INI file");
            jButtonSave.setMnemonic('s');
        }
        return jButtonSave;
    }

    void addSaveButtonActionListener(ActionListener actionListener) {
        jButtonSave.addActionListener(actionListener);
    }

    private JButton getJButtonLoad() {
        if (jButtonLoad == null) {
            jButtonLoad = new JButton();
            jButtonLoad.setBounds(new Rectangle(178, 126, 168, 18));
            jButtonLoad.setText("Load Palette");
            jButtonLoad.setToolTipText("Load palette settings from an INI file");
            jButtonLoad.setMnemonic('l');
        }
        return jButtonLoad;
    }

    void addLoadButtonActionListener(ActionListener actionListener) {
        jButtonLoad.addActionListener(actionListener);
    }

    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setBounds(15, 30, 150, 152);
        }
        return jScrollPane;
    }

    private void initColorList() {
        jListColor = new JList(model.getColorNames());
        jListColor.setSelectedIndex(0);
        jListColor.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i=0; i < model.getColorNames().length; i++) {
            model.getColorIcons()[i] = new ImageIcon(new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB));
            paintIcon(model.getColorIcons()[i], model.getSelectedColors()[i]);
        }
        jScrollPane.setViewportView(jListColor);
    }

    void setColorListMouseListener(MouseListener mouseListener) {
        jListColor.addMouseListener(mouseListener);
    }

    void setColorListCellRenderer(DefaultListCellRenderer cellRenderer) {
        jListColor.setCellRenderer(cellRenderer);
    }

    void repaintColorList() {
        jListColor.repaint();
    }

    int getSelectedColor() {
        return jListColor.getSelectedIndex();
    }

    void paintIcon(ImageIcon icon, Color color) {
        Graphics g = icon.getImage().getGraphics();
        g.setColor(color);
        g.setPaintMode();
        g.fillRect(0, 0,icon.getIconWidth(), icon.getIconHeight());
    }

    void changeColor(int index) {
        Color color = JColorChooser.showDialog( null, "Input Color " +  model.getColorNames()[index], model.getSelectedColors()[index] );
        if (color != null) {
            model.getSelectedColors()[index] = color;
        }
        paintIcon(model.getColorIcons()[index], model.getSelectedColors()[index]);
        jListColor.repaint();
    }
}
