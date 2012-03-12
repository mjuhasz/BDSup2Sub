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
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static bdsup2sub.core.Constants.LANGUAGES;
import static bdsup2sub.gui.support.GuiUtils.centerRelativeToParent;

public class ExportDialogView extends JDialog {

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane;
    private JTextField jTextFieldFileName;
    private JButton jButtonFileName;
    private JComboBox jComboBoxLanguage;
    private JCheckBox jCheckBoxForced;
    private JCheckBox jCheckBoxWritePGCPal;
    private JButton jButtonCancel;
    private JButton jButtonSave;


    /** semaphore to disable actions while changing component properties */
//    private volatile boolean isReady;
    /** file extension */
    private String extension;

    private ExportDialogModel model;


    public ExportDialogView(ExportDialogModel model, Frame owner) {
        super(owner, true);
        this.model = model;

        initialize();


        // init components
        OutputMode outputMode = model.getOutputMode();
        for (String[] language : LANGUAGES) {
            int n;
            if (outputMode == OutputMode.XML) {
                n = 2;
            } else {
                n = 1;
            }
            jComboBoxLanguage.addItem(language[0] + " (" + language[n] + ")");
        }
        jComboBoxLanguage.setSelectedIndex(model.getLanguageIdx());
        if (outputMode == OutputMode.BDSUP) {
            jComboBoxLanguage.setEnabled(false);
        }

        if (Core.getNumForcedFrames() > 0) {
            setForced(true, model.getExportForced());
        } else {
            setForced(false, false);
        }

        String sTitle;

        if (outputMode == OutputMode.VOBSUB) {
            extension = "idx";
            sTitle = "SUB/IDX (VobSub)";
        } else if (outputMode == OutputMode.SUPIFO) {
            extension = "ifo";
            sTitle = "SUP/IFO (SUP DVD)";
        } else if (outputMode == OutputMode.BDSUP) {
            extension = "sup";
            sTitle = "BD SUP";
        } else {
            extension = "xml";
            sTitle = "XML (SONY BDN)";
        }

        setTitle("Export " + sTitle);
    }

    private void initialize() {
        setPreferredSize(new Dimension(350, 180));
        setBounds(new Rectangle(0, 0, 350, 180));
        setMaximumSize(new Dimension(350, 180));
        setMinimumSize(new Dimension(350, 180));
        setResizable(false);
        setContentPane(getJContentPane());
        centerRelativeToParent(this, getOwner());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                model.setCanceled(true);
                dispose();
            }
        });
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
            jContentPane.add(getJTextFieldFileName(), gridBagTextFileName);
            jContentPane.add(getJButtonFileName(), gridBagButtonFileName);
            jContentPane.add(jLabelLanguage, gridBagLabelLanguage);
            jContentPane.add(getJComboBoxLanguage(), gridBagComboLanguage);
            jContentPane.add(getJButtonCancel(), gridBagButtonCancel);
            jContentPane.add(getJButtonSave(), gridBagButtonSave);
            jContentPane.add(getJCheckBoxForced(), gridBagCheckBoxForced);
            jContentPane.add(getJCheckBoxWritePGCPal(), gridBagConstraints);
        }
        return jContentPane;
    }

    private JTextField getJTextFieldFileName() {
        if (jTextFieldFileName == null) {
            jTextFieldFileName = new JTextField();
            jTextFieldFileName.setPreferredSize(new Dimension(200, 20));
            jTextFieldFileName.setHorizontalAlignment(JTextField.LEADING);
            jTextFieldFileName.setToolTipText("Set file name for export");
            jTextFieldFileName.setText(model.getFilename());
            jTextFieldFileName.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String s = jTextFieldFileName.getText();
                    if (s != null) {
                        model.setFilename(FilenameUtils.removeExtension(s) + "." + extension);
                    }
                }
            });
        }
        return jTextFieldFileName;
    }

    private JButton getJButtonFileName() {
        if (jButtonFileName == null) {
            jButtonFileName = new JButton();
            jButtonFileName.setText("Browse");
            jButtonFileName.setMnemonic('b');
            jButtonFileName.setToolTipText("Open file dialog to select file name for export");
            jButtonFileName.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String[] ext = new String[1];
                    ext[0] = extension;
                    String p = FilenameUtils.getParent(model.getFilename());
                    String fn = FilenameUtils.getName(model.getFilename());
                    String fname = ToolBox.getFileName(p, fn, ext, false, getOwner());
                    if (fname != null) {
                        model.setFilename(FilenameUtils.removeExtension(fname) + "." + extension);
                        jTextFieldFileName.setText(model.getFilename());
                    }
                }
            });
        }
        return jButtonFileName;
    }

    private JComboBox getJComboBoxLanguage() {
        if (jComboBoxLanguage == null) {
            jComboBoxLanguage = new JComboBox();
            jComboBoxLanguage.setPreferredSize(new Dimension(200, 20));
            jComboBoxLanguage.setEditable(false);
            jComboBoxLanguage.setToolTipText("Set language identifier");
            jComboBoxLanguage.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    model.setLanguageIdx(jComboBoxLanguage.getSelectedIndex());
                }
            });
        }
        return jComboBoxLanguage;
    }

    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setText("Cancel");
            jButtonCancel.setToolTipText("Cancel export and return");
            jButtonCancel.setMnemonic('c');
            jButtonCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    model.setCanceled(true);
                    dispose();
                }
            });
        }
        return jButtonCancel;
    }

    private JButton getJButtonSave() {
        if (jButtonSave == null) {
            jButtonSave = new JButton();
            jButtonSave.setText("Save");
            jButtonSave.setToolTipText("Start creation of export stream");
            jButtonSave.setMnemonic('s');
            jButtonSave.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // read values of editable boxes
                    String s;
                    // file name
                    s = jTextFieldFileName.getText();
                    if (s != null) {
                        model.setFilename(FilenameUtils.removeExtension(s) + "." + extension);
                    }
                    // exit
                    model.storeExportForced();
                    model.storeLanguageIdx();
                    if (model.getOutputMode() == OutputMode.VOBSUB || model.getOutputMode() == OutputMode.SUPIFO) {
                        model.storeWritePGCPal();
                    }
                    model.setCanceled(false);
                    dispose();
                }
            });
        }
        return jButtonSave;
    }

    private void setForced(boolean enable, boolean state) {
        model.setExportForced(state);
        jCheckBoxForced.setEnabled(enable);
        jCheckBoxForced.setSelected(state);
    }

    private JCheckBox getJCheckBoxForced() {
        if (jCheckBoxForced == null) {
            jCheckBoxForced = new JCheckBox();
            jCheckBoxForced.setToolTipText("Export only subpictures marked as 'forced'");
            jCheckBoxForced.setText("                 Export only forced");
            jCheckBoxForced.setMnemonic('f');
            jCheckBoxForced.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    model.setExportForced(jCheckBoxForced.isSelected());
                }
            });
        }
        return jCheckBoxForced;
    }

    private JCheckBox getJCheckBoxWritePGCPal() {
        if (jCheckBoxWritePGCPal == null) {
            jCheckBoxWritePGCPal = new JCheckBox();
            jCheckBoxWritePGCPal.setToolTipText("Export palette in PGCEdit text format (RGB, 0..255)");
            jCheckBoxWritePGCPal.setText("                 Export palette in PGCEdit text format");
            jCheckBoxWritePGCPal.setMnemonic('p');
            jCheckBoxWritePGCPal.setDisplayedMnemonicIndex(24);
            if (model.getOutputMode() == OutputMode.VOBSUB || model.getOutputMode() == OutputMode.SUPIFO) {
                jCheckBoxWritePGCPal.setEnabled(true);
                jCheckBoxWritePGCPal.setSelected(model.getWritePGCPal());
            } else {
                jCheckBoxWritePGCPal.setEnabled(false);
            }
            jCheckBoxWritePGCPal.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    model.setWritePGCPal(jCheckBoxWritePGCPal.isSelected());
                }
            });
        }
        return jCheckBoxWritePGCPal;
    }
}
