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
package bdsup2sub.gui.export;

import bdsup2sub.core.Core;
import bdsup2sub.core.OutputMode;
import bdsup2sub.gui.support.RequestFocusListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static bdsup2sub.core.Constants.LANGUAGES;
import static bdsup2sub.gui.support.GuiUtils.centerRelativeToOwner;

class ExportDialogView extends JDialog {

    private JPanel jContentPane;
    private JTextField jTextFieldFilename;
    private JButton jButtonFileName;
    private JComboBox jComboBoxLanguage;
    private JCheckBox jCheckBoxForced;
    private JCheckBox jCheckBoxWritePGCPalette;
    private JButton jButtonCancel;
    private JButton jButtonSave;

    private final ExportDialogModel model;

    public ExportDialogView(ExportDialogModel model, Frame owner) {
        super(owner, true);
        this.model = model;
        initialize();
    }

    private void initialize() {
        setPreferredSize(new Dimension(350, 180));
        setBounds(new Rectangle(0, 0, 350, 180));
        setMaximumSize(new Dimension(350, 180));
        setMinimumSize(new Dimension(350, 180));
        setResizable(false);
        setContentPane(getJContentPane());
        centerRelativeToOwner(this);
        setTitle(model.getDialogTitle());
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.gridwidth = 3;
            gridBagConstraints.gridy = 4;
            GridBagConstraints gridBagButtonSave = new GridBagConstraints();
            gridBagButtonSave.gridx = 2;
            gridBagButtonSave.insets = new Insets(2, 4, 2, 0);
            gridBagButtonSave.gridy = 7;
            GridBagConstraints gridBagButtonCancel = new GridBagConstraints();
            gridBagButtonCancel.gridx = 0;
            gridBagButtonCancel.insets = new Insets(2, 4, 2, 0);
            gridBagButtonCancel.gridy = 7;
            GridBagConstraints gridBagCheckBoxForced = new GridBagConstraints();
            gridBagCheckBoxForced.gridx = 0;
            gridBagCheckBoxForced.insets = new Insets(0, 0, 0, 0);
            gridBagCheckBoxForced.gridwidth = 2;
            gridBagCheckBoxForced.anchor = GridBagConstraints.NORTHWEST;
            gridBagCheckBoxForced.gridy = 3;
            GridBagConstraints gridBagComboLanguage = new GridBagConstraints();
            gridBagComboLanguage.fill = GridBagConstraints.NONE;
            gridBagComboLanguage.gridy = 1;
            gridBagComboLanguage.weightx = 1.0;
            gridBagComboLanguage.anchor = GridBagConstraints.WEST;
            gridBagComboLanguage.insets = new Insets(2, 4, 2, 4);
            gridBagComboLanguage.gridx = 1;
            GridBagConstraints gridBagLabelLanguage = new GridBagConstraints();
            gridBagLabelLanguage.gridx = 0;
            gridBagLabelLanguage.anchor = GridBagConstraints.WEST;
            gridBagLabelLanguage.insets = new Insets(2, 6, 2, 0);
            gridBagLabelLanguage.gridy = 1;
            JLabel jLabelLanguage = new JLabel();
            jLabelLanguage.setText("Language");
            GridBagConstraints gridBagButtonFileName = new GridBagConstraints();
            gridBagButtonFileName.gridx = 2;
            gridBagButtonFileName.anchor = GridBagConstraints.EAST;
            gridBagButtonFileName.insets = new Insets(2, 0, 2, 4);
            gridBagButtonFileName.gridy = 0;
            GridBagConstraints gridBagTextFileName = new GridBagConstraints();
            gridBagTextFileName.fill = GridBagConstraints.NONE;
            gridBagTextFileName.gridy = 0;
            gridBagTextFileName.weightx = 1.0D;
            gridBagTextFileName.anchor = GridBagConstraints.WEST;
            gridBagTextFileName.insets = new Insets(2, 4, 2, 4);
            gridBagTextFileName.ipadx = 200;
            gridBagTextFileName.gridx = 1;
            GridBagConstraints gridBagLabelFilename = new GridBagConstraints();
            gridBagLabelFilename.gridx = 0;
            gridBagLabelFilename.anchor = GridBagConstraints.WEST;
            gridBagLabelFilename.insets = new Insets(2, 6, 2, 0);
            gridBagLabelFilename.gridy = 0;
            JLabel jLabelFilename = new JLabel();
            jLabelFilename.setText("Filename");
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(jLabelFilename, gridBagLabelFilename);
            jContentPane.add(getJTextFieldFilename(), gridBagTextFileName);
            jContentPane.add(getJButtonFilename(), gridBagButtonFileName);
            jContentPane.add(jLabelLanguage, gridBagLabelLanguage);
            jContentPane.add(getJComboBoxLanguage(), gridBagComboLanguage);
            jContentPane.add(getJButtonCancel(), gridBagButtonCancel);
            jContentPane.add(getJButtonSave(), gridBagButtonSave);
            jContentPane.add(getJCheckBoxForced(), gridBagCheckBoxForced);
            jContentPane.add(getJCheckBoxWritePGCPalette(), gridBagConstraints);
        }
        return jContentPane;
    }

    private JTextField getJTextFieldFilename() {
        if (jTextFieldFilename == null) {
            jTextFieldFilename = new JTextField();
            jTextFieldFilename.setPreferredSize(new Dimension(200, 20));
            jTextFieldFilename.setHorizontalAlignment(JTextField.LEADING);
            jTextFieldFilename.setToolTipText("Set file name for export");
            jTextFieldFilename.setText(model.getFilename());
        }
        return jTextFieldFilename;
    }

    void addFilenameTextFieldActionListener(ActionListener actionListener) {
        jTextFieldFilename.addActionListener(actionListener);
    }
    
    String getFilenameTextFieldText() {
        return jTextFieldFilename.getText();
    }

    void setFilenameTextFieldText(String text) {
        jTextFieldFilename.setText(text);
    }

    private JButton getJButtonFilename() {
        if (jButtonFileName == null) {
            jButtonFileName = new JButton();
            jButtonFileName.setText("Browse");
            jButtonFileName.setMnemonic('b');
            jButtonFileName.setToolTipText("Open file dialog to select file name for export");
        }
        return jButtonFileName;
    }

    void addFilenameButtonActionListener(ActionListener actionListener) {
        jButtonFileName.addActionListener(actionListener);
    }

    private JComboBox getJComboBoxLanguage() {
        if (jComboBoxLanguage == null) {
            jComboBoxLanguage = new JComboBox();
            jComboBoxLanguage.setPreferredSize(new Dimension(200, 20));
            jComboBoxLanguage.setEditable(false);
            jComboBoxLanguage.setToolTipText("Set language identifier");
            jComboBoxLanguage.setEnabled(model.getOutputMode() != OutputMode.BDSUP);

            int n = (model.getOutputMode() == OutputMode.XML) ? 2 : 1;
            for (String[] language : LANGUAGES) {
                jComboBoxLanguage.addItem(language[0] + " (" + language[n] + ")");
            }
            jComboBoxLanguage.setSelectedIndex(model.getLanguageIdx());
        }
        return jComboBoxLanguage;
    }

    void addLanguageComboBoxItemListener(ItemListener itemListener) {
        jComboBoxLanguage.addItemListener(itemListener);
    }
    
    int getLanguageComboBoxSelectedItem() {
        return jComboBoxLanguage.getSelectedIndex();
    }

    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setText("Cancel");
            jButtonCancel.setToolTipText("Cancel export and return");
            jButtonCancel.setMnemonic('c');
        }
        return jButtonCancel;
    }

    void addCancelButtonActionListener(ActionListener actionListener) {
        jButtonCancel.addActionListener(actionListener);
    }

    private JButton getJButtonSave() {
        if (jButtonSave == null) {
            jButtonSave = new JButton();
            jButtonSave.setText("Save");
            jButtonSave.setToolTipText("Start creation of export stream");
            jButtonSave.setMnemonic('s');
            jButtonSave.addAncestorListener(new RequestFocusListener());
        }
        return jButtonSave;
    }

    void addSaveButtonActionListener(ActionListener actionListener) {
        jButtonSave.addActionListener(actionListener);
    }

    private JCheckBox getJCheckBoxForced() {
        if (jCheckBoxForced == null) {
            jCheckBoxForced = new JCheckBox();
            jCheckBoxForced.setToolTipText("Export only subpictures marked as 'forced'");
            jCheckBoxForced.setText("Export only forced");
            jCheckBoxForced.setMnemonic('f');
            if (Core.getNumForcedFrames() == 0) {
                jCheckBoxForced.setEnabled(false);
                model.setExportForced(false);
            }
            jCheckBoxForced.setSelected(model.getExportForced());
        }
        return jCheckBoxForced;
    }

    void addForcedCheckBoxItemListener(ItemListener itemListener) {
        jCheckBoxForced.addItemListener(itemListener);
    }

    boolean isForcedCheckBoxSelected() {
        return jCheckBoxForced.isSelected();
    }

    private JCheckBox getJCheckBoxWritePGCPalette() {
        if (jCheckBoxWritePGCPalette == null) {
            jCheckBoxWritePGCPalette = new JCheckBox();
            jCheckBoxWritePGCPalette.setToolTipText("Export palette in PGCEdit text format (RGB, 0..255)");
            String text = "Export palette in PGCEdit text format";
            jCheckBoxWritePGCPalette.setText(text);
            jCheckBoxWritePGCPalette.setMnemonic('p');
            jCheckBoxWritePGCPalette.setDisplayedMnemonicIndex(text.indexOf("PGCEdit"));
            if (model.getOutputMode() == OutputMode.VOBSUB || model.getOutputMode() == OutputMode.SUPIFO) {
                jCheckBoxWritePGCPalette.setEnabled(true);
                jCheckBoxWritePGCPalette.setSelected(model.getWritePGCPalette());
            } else {
                jCheckBoxWritePGCPalette.setEnabled(false);
            }
        }
        return jCheckBoxWritePGCPalette;
    }

    void addWritePGCPaletteCheckBoxItemListener(ItemListener itemListener) {
        jCheckBoxWritePGCPalette.addItemListener(itemListener);
    }

    public boolean isWritePGCPalCheckBoxSelected() {
        return jCheckBoxWritePGCPalette.isSelected();
    }
}
