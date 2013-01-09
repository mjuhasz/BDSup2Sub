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
package bdsup2sub.gui.palette;

import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.Core;
import bdsup2sub.gui.support.RequestFocusListener;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import static bdsup2sub.gui.palette.FramePaletteDialogModel.COLOR_NAME;
import static bdsup2sub.gui.support.GuiUtils.centerRelativeToOwner;

class FramePaletteDialogView extends JDialog {

    private JPanel jContentPane;

    private JComboBox jComboBoxColor1;
    private JComboBox jComboBoxColor2;
    private JComboBox jComboBoxColor3;
    private JComboBox jComboBoxColor4;

    private JComboBox jComboBoxAlpha1;
    private JComboBox jComboBoxAlpha2;
    private JComboBox jComboBoxAlpha3;
    private JComboBox jComboBoxAlpha4;

    private JButton jButtonOk;
    private JButton jButtonCancel;
    private JButton jButtonSetAll;
    private JButton jButtonResetAll;
    private JButton jButtonReset;

    private final FramePaletteDialogModel model;


    public FramePaletteDialogView(FramePaletteDialogModel model, Frame frame) {
        super(frame, "Edit Frame Palette", true);
        this.model = model;

        initialize();
    }

    private void initialize() {
        setSize(294, 209);
        setContentPane(getJContentPane());
        centerRelativeToOwner(this);
        setResizable(false);

        initComboBoxColorPreviewIcons(model);
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            JLabel jLabelAlpha1 = new JLabel();
            jLabelAlpha1.setBounds(new Rectangle(155, 10, 41, 16));
            jLabelAlpha1.setText("Alpha 1");
            JLabel jLabelAlpha2 = new JLabel();
            jLabelAlpha2.setBounds(new Rectangle(155, 35, 41, 16));
            jLabelAlpha2.setText("Alpha 2");
            JLabel jLabelAlpha3 = new JLabel();
            jLabelAlpha3.setBounds(new Rectangle(155, 60, 41, 16));
            jLabelAlpha3.setText("Alpha 3");
            JLabel jLabelAlpha4 = new JLabel();
            jLabelAlpha4.setBounds(new Rectangle(155, 85, 41, 16));
            jLabelAlpha4.setText("Alpha 4");
            JLabel jLabelColor1 = new JLabel();
            jLabelColor1.setBounds(new Rectangle(15, 10, 46, 16));
            jLabelColor1.setText("Color 1");
            JLabel jLabelColor2 = new JLabel();
            jLabelColor2.setBounds(new Rectangle(15, 35, 46, 16));
            jLabelColor2.setText("Color 2");
            JLabel jLabelColor3 = new JLabel();
            jLabelColor3.setBounds(new Rectangle(15, 60, 46, 16));
            jLabelColor3.setText("Color 3");
            JLabel jLabelColor4 = new JLabel();
            jLabelColor4.setBounds(new Rectangle(15, 85, 45, 16));
            jLabelColor4.setText("Color 4");

            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(getJButtonOk(), null);
            jContentPane.add(getJButtonCancel(), null);
            jContentPane.add(jLabelColor1, null);
            jContentPane.add(jLabelColor2, null);
            jContentPane.add(jLabelColor3, null);
            jContentPane.add(jLabelColor4, null);
            jContentPane.add(jLabelAlpha1, null);
            jContentPane.add(jLabelAlpha2, null);
            jContentPane.add(jLabelAlpha3, null);
            jContentPane.add(jLabelAlpha4, null);
            jContentPane.add(getJComboBoxColor1(), null);
            jContentPane.add(getJComboBoxColor2(), null);
            jContentPane.add(getJComboBoxColor3(), null);
            jContentPane.add(getJComboBoxColor4(), null);
            jContentPane.add(getJComboBoxAlpha1(), null);
            jContentPane.add(getJComboBoxAlpha2(), null);
            jContentPane.add(getJComboBoxAlpha3(), null);
            jContentPane.add(getJComboBoxAlpha4(), null);
            jContentPane.add(getJButtonSetAll(), null);
            jContentPane.add(getJButtonResetAll(), null);
            jContentPane.add(getJButtonReset(), null);
        }
        return jContentPane;
    }

    private JComboBox getJComboBoxColor1() {
        if (jComboBoxColor1 == null) {
            jComboBoxColor1 = new JComboBox();
            jComboBoxColor1.setBounds(new Rectangle(70, 10, 61, 16));
            jComboBoxColor1.setEditable(false);
            jComboBoxColor1.setToolTipText("Set palette index of frame color 1");
        }
        return jComboBoxColor1;
    }

    void addColor1ComboBoxActionListener(ActionListener actionListener) {
        jComboBoxColor1.addActionListener(actionListener);
    }

    private JComboBox getJComboBoxColor2() {
        if (jComboBoxColor2 == null) {
            jComboBoxColor2 = new JComboBox();
            jComboBoxColor2.setBounds(new Rectangle(70, 35, 61, 16));
            jComboBoxColor2.setEditable(false);
            jComboBoxColor2.setToolTipText("Set palette index of frame color 2");
        }
        return jComboBoxColor2;
    }

    void addColor2ComboBoxActionListener(ActionListener actionListener) {
        jComboBoxColor2.addActionListener(actionListener);
    }

    private JComboBox getJComboBoxColor3() {
        if (jComboBoxColor3 == null) {
            jComboBoxColor3 = new JComboBox();
            jComboBoxColor3.setBounds(new Rectangle(70, 60, 61, 16));
            jComboBoxColor3.setEditable(false);
            jComboBoxColor3.setToolTipText("Set palette index of frame color 3");
        }
        return jComboBoxColor3;
    }

    void addColor3ComboBoxActionListener(ActionListener actionListener) {
        jComboBoxColor3.addActionListener(actionListener);
    }

    private JComboBox getJComboBoxColor4() {
        if (jComboBoxColor4 == null) {
            jComboBoxColor4 = new JComboBox();
            jComboBoxColor4.setBounds(new Rectangle(70, 85, 61, 16));
            jComboBoxColor4.setEditable(false);
            jComboBoxColor4.setToolTipText("Set palette index of frame color 4");
        }
        return jComboBoxColor4;
    }

    void addColor4ComboBoxActionListener(ActionListener actionListener) {
        jComboBoxColor4.addActionListener(actionListener);
    }

    private JComboBox getJComboBoxAlpha1() {
        if (jComboBoxAlpha1 == null) {
            jComboBoxAlpha1 = new JComboBox();
            jComboBoxAlpha1.setBounds(new Rectangle(215, 10, 56, 16));
            jComboBoxAlpha1.setEditable(false);
            jComboBoxAlpha1.setToolTipText("Set alpha value of frame color 1");
        }
        return jComboBoxAlpha1;
    }

    void addAlpha1ComboBoxActionListener(ActionListener actionListener) {
        jComboBoxAlpha1.addActionListener(actionListener);
    }

    private JComboBox getJComboBoxAlpha2() {
        if (jComboBoxAlpha2 == null) {
            jComboBoxAlpha2 = new JComboBox();
            jComboBoxAlpha2.setBounds(new Rectangle(215, 35, 56, 16));
            jComboBoxAlpha2.setEditable(false);
            jComboBoxAlpha2.setToolTipText("Set alpha value of frame color 2");
        }
        return jComboBoxAlpha2;
    }

    void addAlpha2ComboBoxActionListener(ActionListener actionListener) {
        jComboBoxAlpha2.addActionListener(actionListener);
    }

    private JComboBox getJComboBoxAlpha3() {
        if (jComboBoxAlpha3 == null) {
            jComboBoxAlpha3 = new JComboBox();
            jComboBoxAlpha3.setBounds(new Rectangle(215, 60, 56, 16));
            jComboBoxAlpha3.setEditable(false);
            jComboBoxAlpha3.setToolTipText("Set alpha value of frame color 3");
        }
        return jComboBoxAlpha3;
    }

    void addAlpha3ComboBoxActionListener(ActionListener actionListener) {
        jComboBoxAlpha3.addActionListener(actionListener);
    }

    private JComboBox getJComboBoxAlpha4() {
        if (jComboBoxAlpha4 == null) {
            jComboBoxAlpha4 = new JComboBox();
            jComboBoxAlpha4.setBounds(new Rectangle(215, 85, 56, 16));
            jComboBoxAlpha4.setEditable(false);
            jComboBoxAlpha4.setToolTipText("Set alpha value of frame color 4");
        }
        return jComboBoxAlpha4;
    }

    void addAlpha4ComboBoxActionListener(ActionListener actionListener) {
        jComboBoxAlpha4.addActionListener(actionListener);
    }

    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setName("jButtonCancel");
            jButtonCancel.setBounds(new Rectangle(10, 145, 75, 21));
            jButtonCancel.setText("Cancel");
            jButtonCancel.setToolTipText("Lose changes and return");
            jButtonCancel.setMnemonic('c');
        }
        return jButtonCancel;
    }

    void addCancelButtonActionListener(ActionListener actionListener) {
        jButtonCancel.addActionListener(actionListener);
    }

    private JButton getJButtonOk() {
        if (jButtonOk == null) {
            jButtonOk = new JButton();
            jButtonOk.setName("jButtonOk");
            jButtonOk.setBounds(new Rectangle(200, 145, 75, 21));
            jButtonOk.setText("Ok");
            jButtonOk.setToolTipText("Use current settings and return");
            jButtonOk.setMnemonic('o');
            jButtonOk.addAncestorListener(new RequestFocusListener());
        }
        return jButtonOk;
    }

    void addOkButtonActionListener(ActionListener actionListener) {
        jButtonOk.addActionListener(actionListener);
    }

    private JButton getJButtonSetAll() {
        if (jButtonSetAll == null) {
            jButtonSetAll = new JButton();
            jButtonSetAll.setBounds(new Rectangle(200, 115, 75, 21));
            jButtonSetAll.setText("Set All");
            jButtonSetAll.setToolTipText("Apply these settings for whole stream and return");
            jButtonSetAll.setMnemonic('s');
        }
        return jButtonSetAll;
    }

    void addSetAllButtonActionListener(ActionListener actionListener) {
        jButtonSetAll.addActionListener(actionListener);
    }

    private JButton getJButtonResetAll() {
        if (jButtonResetAll == null) {
            jButtonResetAll = new JButton();
            jButtonResetAll.setBounds(new Rectangle(105, 115, 75, 23));
            jButtonResetAll.setText("Reset All");
            jButtonResetAll.setToolTipText("Revert to original frame palettes for whole stream and return");
            jButtonResetAll.setMnemonic('a');
        }
        return jButtonResetAll;
    }

    void addResetAllButtonActionListener(ActionListener actionListener) {
        jButtonResetAll.addActionListener(actionListener);
    }

    private JButton getJButtonReset() {
        if (jButtonReset == null) {
            jButtonReset = new JButton();
            jButtonReset.setBounds(new Rectangle(105, 145, 75, 23));
            jButtonReset.setText("Reset");
            jButtonReset.setToolTipText("Revert to original frame palette");
            jButtonReset.setMnemonic('r');
        }
        return jButtonReset;
    }

    void addResetButtonActionListener(ActionListener actionListener) {
        jButtonReset.addActionListener(actionListener);
    }

    private void initComboBoxColorPreviewIcons(FramePaletteDialogModel model) {
        Palette palette = Core.getCurSrcDVDPalette();
        model.setColorPreviewIcon(new ImageIcon[16]);
        for (int i=0; i < palette.getSize(); i++) {
            model.getColorPreviewIcon()[i] = new ImageIcon(new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB));
            Color c = new Color (palette.getARGB(i) | 0xff000000); // make fully opaque
            paintIcon(model.getColorPreviewIcon()[i], c);
            jComboBoxColor1.addItem(COLOR_NAME[i]);
            jComboBoxColor2.addItem(COLOR_NAME[i]);
            jComboBoxColor3.addItem(COLOR_NAME[i]);
            jComboBoxColor4.addItem(COLOR_NAME[i]);
            jComboBoxAlpha1.addItem(COLOR_NAME[i]);
            jComboBoxAlpha2.addItem(COLOR_NAME[i]);
            jComboBoxAlpha3.addItem(COLOR_NAME[i]);
            jComboBoxAlpha4.addItem(COLOR_NAME[i]);
        }
    }

    private void paintIcon(ImageIcon icon, Color color) {
        Graphics graphics = icon.getImage().getGraphics();
        graphics.setColor(color);
        graphics.setPaintMode();
        graphics.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
    }

    void addComboBoxCellRenderers(ListCellRenderer listCellRenderer) {
        jComboBoxColor1.setRenderer(listCellRenderer);
        jComboBoxColor2.setRenderer(listCellRenderer);
        jComboBoxColor3.setRenderer(listCellRenderer);
        jComboBoxColor4.setRenderer(listCellRenderer);
        jComboBoxColor1.repaint();
        jComboBoxColor2.repaint();
        jComboBoxColor3.repaint();
        jComboBoxColor4.repaint();
    }

    void updateComboBoxSelections() {
        int[] alpha = model.getAlpha();
        jComboBoxAlpha1.setSelectedIndex(alpha[0]);
        jComboBoxAlpha2.setSelectedIndex(alpha[1]);
        jComboBoxAlpha3.setSelectedIndex(alpha[2]);
        jComboBoxAlpha4.setSelectedIndex(alpha[3]);

        int[] palette = model.getPalette();
        jComboBoxColor1.setSelectedIndex(palette[0]);
        jComboBoxColor2.setSelectedIndex(palette[1]);
        jComboBoxColor3.setSelectedIndex(palette[2]);
        jComboBoxColor4.setSelectedIndex(palette[3]);
    }
}
