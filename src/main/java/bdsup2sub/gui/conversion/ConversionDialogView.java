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
package bdsup2sub.gui.conversion;

import bdsup2sub.core.Core;
import bdsup2sub.core.ForcedFlagState;
import bdsup2sub.core.Resolution;
import bdsup2sub.gui.support.RequestFocusListener;
import bdsup2sub.utils.SubtitleUtils;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.*;

import static bdsup2sub.core.Configuration.*;
import static bdsup2sub.core.Configuration.MAX_FREE_SCALE_FACTOR;
import static bdsup2sub.core.Configuration.MIN_FREE_SCALE_FACTOR;
import static bdsup2sub.gui.support.GuiUtils.centerRelativeToOwner;

class ConversionDialogView extends JDialog {

    private static final Dimension DIM_LABEL = new Dimension(80, 20);
    private static final String[] FRAMERATES = {"23.975", "23.976", "24", "25", "29.97", "50", "59.94"};

    private JPanel jContentPane;
    private JPanel jPanelResolution;
    private JComboBox jComboBoxResolution;
    private JCheckBox jCheckBoxFrameRate;
    private JCheckBox jCheckBoxResolution;
    private JPanel jPanelFps;
    private JComboBox jComboBoxFpsSrc;
    private JComboBox jComboBoxFpsTrg;
    private JPanel jPanelMove;
    private JCheckBox jCheckBoxMove;
    private JPanel jPanelTimes;
    private JTextField jTextFieldDelay;
    private JCheckBox jCheckBoxFixMinTime;
    private JTextField jTextFieldMinTime;
    private JPanel jPanelDefaults;
    private JButton jButtonStore;
    private JButton jButtonRestore;
    private JButton jButtonReset;
    private JPanel jPanelButtons;
    private JButton jButtonOk;
    private JButton jButtonCancel;
    private JPanel jPanelScale;
    private JCheckBox jCheckBoxScale;
    private JTextField jTextFieldScaleX;
    private JTextField jTextFieldScaleY;
    private JPanel jPanelForced;
    private JComboBox jComboBoxForced;

    private final ConversionDialogModel model;

    public ConversionDialogView(ConversionDialogModel model, Frame owner) {
        super(owner, "Conversion Options", true);
        this.model = model;

        initialize();
        fillDialog();
        model.setReady(true);
    }

    private void fillDialog() {
        jComboBoxResolution.setSelectedIndex(model.getOutputResolution().ordinal());
        jComboBoxResolution.setEnabled(model.getConvertResolution());
        jCheckBoxResolution.setSelected(model.getConvertResolution());

        jTextFieldDelay.setText(ToolBox.formatDouble(model.getDelayPTS() / 90.0));

        jCheckBoxFrameRate.setSelected(model.getConvertFPS());
        jComboBoxFpsSrc.setSelectedItem(ToolBox.formatDouble(model.getFpsSrc()));
        jComboBoxFpsSrc.setEnabled(model.getConvertFPS());
        jComboBoxFpsTrg.setSelectedItem(ToolBox.formatDouble(model.getFpsTrg()));
        jComboBoxFpsTrg.setEnabled(true);

        jTextFieldMinTime.setText(ToolBox.formatDouble(model.getMinTimePTS() / 90.0));
        jCheckBoxFixMinTime.setEnabled(true);
        jCheckBoxFixMinTime.setSelected(model.getFixShortFrames());


        jCheckBoxFixMinTime.setSelected(model.getFixShortFrames());
        jTextFieldMinTime.setEnabled(model.getFixShortFrames());

        jCheckBoxScale.setSelected(model.getApplyFreeScale());
        jTextFieldScaleX.setText(ToolBox.formatDouble(model.getFreeScaleFactorX()));
        jTextFieldScaleX.setEnabled(model.getApplyFreeScale());
        jTextFieldScaleY.setText(ToolBox.formatDouble(model.getFreeScaleFactorY()));
        jTextFieldScaleY.setEnabled(model.getApplyFreeScale());

        jComboBoxForced.setSelectedIndex(model.getForcedState().ordinal());
    }

    private void initialize() {
        setSize(500, 350);
        setPreferredSize(new Dimension(500, 350));
        setContentPane(getJContentPane());
        centerRelativeToOwner(this);
        setResizable(false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                model.setCancel(true);
                dispose();
            }
        });
    }

    private JPanel getJPanelResolution() {
        if (jPanelResolution == null) {
            GridBagConstraints gridBagCheckBoxResolution = new GridBagConstraints();
            gridBagCheckBoxResolution.gridx = 0;
            gridBagCheckBoxResolution.gridy = 0;
            gridBagCheckBoxResolution.anchor = GridBagConstraints.WEST;
            gridBagCheckBoxResolution.gridwidth = 2;

            GridBagConstraints gridBagLabelResolution = new GridBagConstraints();
            gridBagLabelResolution.gridx = 0;
            gridBagLabelResolution.gridy = 1;
            gridBagLabelResolution.anchor = GridBagConstraints.WEST;
            gridBagLabelResolution.insets = new Insets(2, 6, 2, 0);

            GridBagConstraints gridBagComboResolution = new GridBagConstraints();
            gridBagComboResolution.gridx = 1;
            gridBagComboResolution.gridy = 1;
            gridBagComboResolution.weightx = 1.0;
            gridBagComboResolution.anchor = GridBagConstraints.WEST;
            gridBagComboResolution.insets = new Insets(2, 4, 2, 4);

            jPanelResolution = new JPanel();
            jPanelResolution.setLayout(new GridBagLayout());
            jPanelResolution.setBorder(BorderFactory.createTitledBorder(null, "Resolution", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelResolution.setMinimumSize(new Dimension(200, 70));
            jPanelResolution.setPreferredSize(new Dimension(200, 70));

            JLabel label = new JLabel("Resolution");
            label.setMinimumSize(DIM_LABEL);
            jPanelResolution.add(getJCheckBoxResolution(), gridBagCheckBoxResolution);
            jPanelResolution.add(label, gridBagLabelResolution);
            jPanelResolution.add(getJComboBoxResolution(), gridBagComboResolution);

        }
        return jPanelResolution;
    }

    private JPanel getJPanelMove() {
        if (jPanelMove == null) {
            GridBagConstraints gridBagCheckBoxMove = new GridBagConstraints();
            gridBagCheckBoxMove.gridx = 0;
            gridBagCheckBoxMove.gridy = 0;
            gridBagCheckBoxMove.anchor = GridBagConstraints.WEST;
            gridBagCheckBoxMove.weightx = 1.0;
            //gridBagCheckBoxMove.insets = new Insets(2, 6, 2, 0);

            jPanelMove = new JPanel();
            jPanelMove.setLayout(new GridBagLayout());
            jPanelMove.setBorder(BorderFactory.createTitledBorder(null, "Move", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelMove.setMinimumSize(new Dimension(200, 50));
            jPanelMove.setPreferredSize(new Dimension(200, 50));

            jPanelMove.add(getJCheckBoxMove(), gridBagCheckBoxMove);

        }
        return jPanelMove;
    }


    private JPanel getJPanelFps() {
        if (jPanelFps == null) {
            GridBagConstraints gridBagCheckBoxFrameRate = new GridBagConstraints();
            gridBagCheckBoxFrameRate.gridx = 0;
            gridBagCheckBoxFrameRate.gridy = 0;
            gridBagCheckBoxFrameRate.anchor = GridBagConstraints.WEST;
            gridBagCheckBoxFrameRate.gridwidth = 2;

            GridBagConstraints gridBagLabelFpsSrc = new GridBagConstraints();
            gridBagLabelFpsSrc.gridx = 0;
            gridBagLabelFpsSrc.gridy = 1;
            gridBagLabelFpsSrc.anchor = GridBagConstraints.WEST;
            gridBagLabelFpsSrc.insets = new Insets(2, 6, 2, 0);

            GridBagConstraints gridBagComboFpsSrc = new GridBagConstraints();
            gridBagComboFpsSrc.gridx = 1;
            gridBagComboFpsSrc.gridy = 1;
            //gridBagComboFpsSrc.fill = GridBagConstraints.NONE;
            gridBagComboFpsSrc.weightx = 1.0;
            gridBagComboFpsSrc.anchor = GridBagConstraints.WEST;
            gridBagComboFpsSrc.insets = new Insets(2, 4, 2, 4);

            GridBagConstraints gridBagLabelFpsTrg = new GridBagConstraints();
            gridBagLabelFpsTrg.gridx = 0;
            gridBagLabelFpsTrg.gridy = 2;
            gridBagLabelFpsTrg.anchor = GridBagConstraints.WEST;
            gridBagLabelFpsTrg.insets = new Insets(2, 6, 2, 0);

            GridBagConstraints gridBagComboFpsTrg = new GridBagConstraints();
            //gridBagComboFpsTrg.fill = GridBagConstraints.VERTICAL;
            gridBagComboFpsTrg.gridx = 1;
            gridBagComboFpsTrg.gridy = 2;
            gridBagComboFpsTrg.weightx = 1.0;
            gridBagComboFpsTrg.anchor = GridBagConstraints.WEST;
            gridBagComboFpsTrg.insets = new Insets(2, 4, 2, 4);

            jPanelFps = new JPanel();
            jPanelFps.setLayout(new GridBagLayout());
            jPanelFps.setBorder(BorderFactory.createTitledBorder(null, "Framerate", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelFps.setMinimumSize(new Dimension(200, 100));
            jPanelFps.setPreferredSize(new Dimension(200, 100));

            jPanelFps.add(getJCheckBoxFrameRate(), gridBagCheckBoxFrameRate);
            JLabel label = new JLabel("FPS Source");
            label.setMinimumSize(DIM_LABEL);
            jPanelFps.add(label, gridBagLabelFpsSrc);
            label = new JLabel("FPS Target");
            label.setMinimumSize(DIM_LABEL);
            jPanelFps.add(label, gridBagLabelFpsTrg);
            jPanelFps.add(getJComboBoxFpsSrc(), gridBagComboFpsSrc);
            jPanelFps.add(getJComboBoxFpsTrg(), gridBagComboFpsTrg);
        }
        return jPanelFps;
    }

    private JPanel getJPanelTimes() {
        if (jPanelTimes == null) {
            GridBagConstraints gridBagLabelDelay = new GridBagConstraints();
            gridBagLabelDelay.gridx = 0;
            gridBagLabelDelay.gridy = 0;
            gridBagLabelDelay.anchor = GridBagConstraints.WEST;
            gridBagLabelDelay.insets = new Insets(2, 6, 2, 0);

            GridBagConstraints gridBagTextDelay = new GridBagConstraints();
            gridBagTextDelay.gridx = 1;
            gridBagTextDelay.gridy = 0;
            gridBagTextDelay.weightx = 1.0D;
            gridBagTextDelay.anchor = GridBagConstraints.WEST;
            gridBagTextDelay.insets = new Insets(2, 4, 2, 4);
            gridBagTextDelay.ipadx = 100;

            GridBagConstraints gridBagCheckBoxFixMinTime = new GridBagConstraints();
            gridBagCheckBoxFixMinTime.gridx = 0;
            gridBagCheckBoxFixMinTime.gridy = 1;
            gridBagCheckBoxFixMinTime.anchor = GridBagConstraints.WEST;
            gridBagCheckBoxFixMinTime.gridwidth = 2;
            gridBagCheckBoxFixMinTime.insets = new Insets(0, 0, 0, 0);

            GridBagConstraints gridBagLabelMinTime = new GridBagConstraints();
            gridBagLabelMinTime.gridx = 0;
            gridBagLabelMinTime.gridy = 2;
            gridBagLabelMinTime.anchor = GridBagConstraints.WEST;
            gridBagLabelMinTime.insets = new Insets(2, 6, 2, 2);

            GridBagConstraints gridBagTextMinTime = new GridBagConstraints();
            gridBagTextMinTime.gridx = 1;
            gridBagTextMinTime.gridy = 2;
            gridBagTextMinTime.anchor = GridBagConstraints.WEST;
            gridBagTextMinTime.weightx = 1.0;
            gridBagTextMinTime.ipadx = 100;
            gridBagTextMinTime.insets = new Insets(2, 4, 2, 4);

            jPanelTimes = new JPanel();
            jPanelTimes.setLayout(new GridBagLayout());
            jPanelTimes.setBorder(BorderFactory.createTitledBorder(null, "Times", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelTimes.setMinimumSize(new Dimension(200, 100));
            jPanelTimes.setPreferredSize(new Dimension(200, 100));

            JLabel label = new JLabel("Delay (ms)");
            label.setMinimumSize(DIM_LABEL);
            jPanelTimes.add(label, gridBagLabelDelay);
            jPanelTimes.add(getJTextFieldDelay(), gridBagTextDelay);
            jPanelTimes.add(getJCheckBoxFixMinTime(), gridBagCheckBoxFixMinTime);
            jPanelTimes.add(getJTextFieldMinTime(), gridBagTextMinTime);
            label = new JLabel("Min Time (ms)");
            label.setMinimumSize(DIM_LABEL);
            jPanelTimes.add(label, gridBagLabelMinTime);
        }
        return jPanelTimes;
    }

    private JPanel getJPanelScale() {
        if (jPanelScale == null) {
            GridBagConstraints gridBagCheckBoxScale = new GridBagConstraints();
            gridBagCheckBoxScale.gridx = 0;
            gridBagCheckBoxScale.gridy = 0;
            gridBagCheckBoxScale.anchor = GridBagConstraints.WEST;
            gridBagCheckBoxScale.gridwidth = 2;

            GridBagConstraints gridBagLabelScaleX = new GridBagConstraints();
            gridBagLabelScaleX.gridx = 0;
            gridBagLabelScaleX.gridy = 1;
            gridBagLabelScaleX.anchor = GridBagConstraints.WEST;
            gridBagLabelScaleX.insets = new Insets(2, 6, 2, 2);

            GridBagConstraints gridBagTextScaleX = new GridBagConstraints();
            gridBagTextScaleX.gridx = 1;
            gridBagTextScaleX.gridy = 1;
            gridBagTextScaleX.anchor = GridBagConstraints.WEST;
            gridBagTextScaleX.insets = new Insets(2, 4, 2, 4);
            gridBagTextScaleX.weightx = 1.0;
            gridBagTextScaleX.ipadx = 100;

            GridBagConstraints gridBagLabelScaleY = new GridBagConstraints();
            gridBagLabelScaleY.gridx = 0;
            gridBagLabelScaleY.gridy = 2;
            gridBagLabelScaleY.anchor = GridBagConstraints.WEST;
            gridBagLabelScaleY.insets = new Insets(2, 6, 2, 2);

            GridBagConstraints gridBagTextScaleY = new GridBagConstraints();
            gridBagTextScaleY.gridx = 1;
            gridBagTextScaleY.gridy = 2;
            gridBagTextScaleY.anchor = GridBagConstraints.WEST;
            gridBagTextScaleY.insets = new Insets(2, 4, 2, 4);
            gridBagTextScaleY.weightx = 1.0;
            gridBagTextScaleY.ipadx = 100;

            jPanelScale = new JPanel();
            jPanelScale.setLayout(new GridBagLayout());
            jPanelScale.setBorder(BorderFactory.createTitledBorder(null, "Scale", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelScale.setMinimumSize(new Dimension(200, 100));
            jPanelScale.setPreferredSize(new Dimension(200, 100));

            jPanelScale.add(getJCheckBoxScale(), gridBagCheckBoxScale);
            JLabel label = new JLabel("Scale X");
            label.setMinimumSize(DIM_LABEL);
            jPanelScale.add(label, gridBagLabelScaleX);
            jPanelScale.add(getJTextFieldScaleX(), gridBagTextScaleX);
            label = new JLabel("Scale Y");
            label.setMinimumSize(DIM_LABEL);
            jPanelScale.add(label, gridBagLabelScaleY);
            jPanelScale.add(getJTextFieldScaleY(), gridBagTextScaleY);
        }
        return jPanelScale;
    }

    private JPanel getJPanelDefaults() {
        if (jPanelDefaults == null) {
            GridBagConstraints gridBagButtonStore = new GridBagConstraints();
            gridBagButtonStore.gridx = 0;
            gridBagButtonStore.gridy = 0;
            gridBagButtonStore.anchor = GridBagConstraints.WEST;
            gridBagButtonStore.weightx = 1.0;
            gridBagButtonStore.insets = new Insets(2, 0, 2, 0);

            GridBagConstraints gridBagButtonRestore = new GridBagConstraints();
            gridBagButtonRestore.gridx = 1;
            gridBagButtonRestore.gridy = 0;
            gridBagButtonRestore.anchor = GridBagConstraints.CENTER;
            gridBagButtonRestore.weightx = 1.0;
            gridBagButtonRestore.insets = new Insets(2, 0, 2, 0);

            GridBagConstraints gridBagButtonReset = new GridBagConstraints();
            gridBagButtonReset.gridx = 2;
            gridBagButtonReset.gridy = 0;
            gridBagButtonReset.anchor = GridBagConstraints.EAST;
            gridBagButtonReset.weightx = 1.0;
            gridBagButtonReset.insets = new Insets(2, 4, 2, 0);

            jPanelDefaults = new JPanel();
            jPanelDefaults.setLayout(new GridBagLayout());
            jPanelDefaults.setBorder(BorderFactory.createTitledBorder(null, "Defaults", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelDefaults.setMinimumSize(new Dimension(200, 60));
            jPanelDefaults.setPreferredSize(new Dimension(200, 60));

            jPanelDefaults.add(getJButtonStore(), gridBagButtonStore);
            jPanelDefaults.add(getJButtonRestore(), gridBagButtonRestore);
            jPanelDefaults.add(getJButtonReset(), gridBagButtonReset);
        }
        return jPanelDefaults;
    }

    private JPanel getJPanelForced() {
        if (jPanelForced == null) {

            GridBagConstraints gridBagLabelForced = new GridBagConstraints();
            gridBagLabelForced.gridx = 0;
            gridBagLabelForced.gridy = 0;
            gridBagLabelForced.anchor = GridBagConstraints.WEST;
            gridBagLabelForced.insets = new Insets(2, 6, 2, 0);

            GridBagConstraints gridBagComboForced = new GridBagConstraints();
            gridBagComboForced.gridx = 1;
            gridBagComboForced.gridy = 0;
            gridBagComboForced.weightx = 1.0;
            gridBagComboForced.anchor = GridBagConstraints.WEST;
            gridBagComboForced.insets = new Insets(2, 4, 2, 4);

            jPanelForced = new JPanel();
            jPanelForced.setLayout(new GridBagLayout());
            jPanelForced.setBorder(BorderFactory.createTitledBorder(null, "Global forced flags", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelForced.setMinimumSize(new Dimension(200, 70));
            jPanelForced.setPreferredSize(new Dimension(200, 70));

            JLabel label = new JLabel("Force all");
            label.setMinimumSize(DIM_LABEL);
            jPanelForced.add(label, gridBagLabelForced);
            jPanelForced.add(getJComboBoxForced(), gridBagComboForced);

        }
        return jPanelForced;
    }

    private JPanel getJPanelButtons() {
        if (jPanelButtons == null) {
            GridBagConstraints gridBagButtonCancel = new GridBagConstraints();
            gridBagButtonCancel.gridx = 0;
            gridBagButtonCancel.gridy = 0;
            gridBagButtonCancel.anchor = GridBagConstraints.WEST;
            gridBagButtonCancel.weightx = 1.0;
            gridBagButtonCancel.insets = new Insets(2, 8, 4, 0);

            GridBagConstraints gridBagButtonOk = new GridBagConstraints();
            gridBagButtonOk.gridx = 3;
            gridBagButtonOk.gridy = 0;
            gridBagButtonOk.anchor = GridBagConstraints.EAST;
            gridBagButtonOk.weightx = 1.0;
            gridBagButtonOk.insets = new Insets(2, 4, 4, 8);

            jPanelButtons = new JPanel();
            jPanelButtons.setLayout(new GridBagLayout());
            jPanelButtons.setMinimumSize(new Dimension(200, 30));
            jPanelButtons.setPreferredSize(new Dimension(200, 30));

            jPanelButtons.add(getJButtonOk(), gridBagButtonOk);
            jPanelButtons.add(getJButtonCancel(), gridBagButtonCancel);
        }
        return jPanelButtons;
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            GridBagConstraints gridBagPanelResolution = new GridBagConstraints();
            gridBagPanelResolution.gridx = 0;
            gridBagPanelResolution.gridy = 0;
            gridBagPanelResolution.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelResolution.weightx = 1.0;
            gridBagPanelResolution.weighty = 1.0;
            gridBagPanelResolution.fill = GridBagConstraints.BOTH;

            GridBagConstraints gridBagPanelMove = new GridBagConstraints();
            gridBagPanelMove.gridx = 0;
            gridBagPanelMove.gridy = 1;
            gridBagPanelMove.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelMove.weightx = 1.0;
            gridBagPanelMove.weighty = 1.0;
            gridBagPanelMove.fill = GridBagConstraints.BOTH;

            GridBagConstraints gridBagPanelScale = new GridBagConstraints();
            gridBagPanelScale.gridx = 1;
            gridBagPanelScale.gridy = 0;
            gridBagPanelScale.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelScale.weightx = 1.0;
            gridBagPanelScale.weighty = 1.0;
            gridBagPanelScale.gridheight = 2;
            gridBagPanelScale.fill = GridBagConstraints.BOTH;

            GridBagConstraints gridBagPanelFPS = new GridBagConstraints();
            gridBagPanelFPS.gridx = 0;
            gridBagPanelFPS.gridy = 2;
            gridBagPanelFPS.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelFPS.weightx = 1.0;
            gridBagPanelFPS.weighty = 1.0;
            gridBagPanelFPS.fill = GridBagConstraints.BOTH;

            GridBagConstraints gridBagPanelTimes = new GridBagConstraints();
            gridBagPanelTimes.gridx = 1;
            gridBagPanelTimes.gridy = 2;
            gridBagPanelTimes.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelTimes.weightx = 1.0;
            gridBagPanelTimes.weighty = 1.0;
            gridBagPanelTimes.fill = GridBagConstraints.BOTH;

            GridBagConstraints gridBagPanelForced = new GridBagConstraints();
            gridBagPanelForced.gridx = 0;
            gridBagPanelForced.gridy = 3;
            gridBagPanelForced.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelForced.weightx = 1.0;
            gridBagPanelForced.weighty = 1.0;
            gridBagPanelForced.fill = GridBagConstraints.BOTH;

            GridBagConstraints gridBagPanelDefaults = new GridBagConstraints();
            gridBagPanelDefaults.gridx = 1;
            gridBagPanelDefaults.gridy = 3;
            gridBagPanelDefaults.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelDefaults.weightx = 1.0;
            gridBagPanelDefaults.weighty = 1.0;
            gridBagPanelDefaults.fill = GridBagConstraints.BOTH;

            GridBagConstraints gridBagPanelButtons = new GridBagConstraints();
            gridBagPanelButtons.gridx = 0;
            gridBagPanelButtons.gridy = 4;
            gridBagPanelButtons.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelButtons.weightx = 1.0;
            gridBagPanelButtons.weighty = 1.0;
            gridBagPanelButtons.gridwidth = 2;
            gridBagPanelButtons.fill = GridBagConstraints.HORIZONTAL;

            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());

            jContentPane.add(getJPanelResolution(), gridBagPanelResolution);
            jContentPane.add(getJPanelMove(), gridBagPanelMove);
            jContentPane.add(getJPanelFps(), gridBagPanelFPS);
            jContentPane.add(getJPanelTimes(), gridBagPanelTimes);
            jContentPane.add(getJPanelScale(), gridBagPanelScale);
            jContentPane.add(getJPanelForced(), gridBagPanelForced);
            jContentPane.add(getJPanelDefaults(), gridBagPanelDefaults);
            jContentPane.add(getJPanelButtons(), gridBagPanelButtons);
        }
        return jContentPane;
    }

    private JComboBox getJComboBoxResolution() {
        if (jComboBoxResolution == null) {
            jComboBoxResolution = new JComboBox();
            jComboBoxResolution.setPreferredSize(new Dimension(200, 20));
            jComboBoxResolution.setMinimumSize(new Dimension(150, 20));
            jComboBoxResolution.setEditable(false);
            jComboBoxResolution.setToolTipText("Select the target resolution");
            for (Resolution resolution : Resolution.values()) {
                jComboBoxResolution.addItem(resolution.toString());
            }
            jComboBoxResolution.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (model.isReady()) {
                        int idx = jComboBoxResolution.getSelectedIndex();
                        for (Resolution resolution : Resolution.values()) {
                            if (idx == resolution.ordinal()) {
                                model.setOutputResolution(resolution);
                                if (!model.isKeepFps()) {
                                    model.setFpsTrg(SubtitleUtils.getDefaultFramerateForResolution(resolution));
                                }
                                jComboBoxFpsTrg.setSelectedItem(ToolBox.formatDouble(model.getFpsTrg()));
                                break;
                            }
                        }
                    }
                }
            });
        }
        return jComboBoxResolution;
    }

    private JCheckBox getJCheckBoxFrameRate() {
        if (jCheckBoxFrameRate == null) {
            jCheckBoxFrameRate = new JCheckBox();
            jCheckBoxFrameRate.setToolTipText("Convert frame rate from FPS Source to FPS target");
            jCheckBoxFrameRate.setText("Change frame rate");
            jCheckBoxFrameRate.setMnemonic('f');
            jCheckBoxFrameRate.setFocusable(false);
            jCheckBoxFrameRate.setIconTextGap(10);
            jCheckBoxFrameRate.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        boolean changeFPS = jCheckBoxFrameRate.isSelected();
                        model.setConvertFPS(changeFPS);
                        jComboBoxFpsSrc.setEnabled(changeFPS);
                    }
                }
            });
        }
        return jCheckBoxFrameRate;
    }

    private JCheckBox getJCheckBoxResolution() {
        if (jCheckBoxResolution == null) {
            jCheckBoxResolution = new JCheckBox();
            jCheckBoxResolution.setToolTipText("Convert resolution");
            String text = "Convert resolution";
            jCheckBoxResolution.setText(text);
            jCheckBoxResolution.setMnemonic('r');
            jCheckBoxResolution.setDisplayedMnemonicIndex(text.indexOf("resolution"));
            jCheckBoxResolution.setFocusable(false);
            jCheckBoxResolution.setIconTextGap(10);
            jCheckBoxResolution.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        boolean changeResolution = jCheckBoxResolution.isSelected();
                        model.setConvertResolution(changeResolution);
                        jComboBoxResolution.setEnabled(changeResolution);
                    }
                }
            });
        }
        return jCheckBoxResolution;
    }

    private JCheckBox getJCheckBoxMove() {
        if (jCheckBoxMove == null) {
            jCheckBoxMove = new JCheckBox();
            jCheckBoxMove.setToolTipText("Apply settings for moving captions");
            jCheckBoxMove.setText("Apply 'move all' settings");
            jCheckBoxMove.setMnemonic('v');
            jCheckBoxMove.setFocusable(false);
            jCheckBoxMove.setIconTextGap(10);
            jCheckBoxMove.setEnabled(false);
            jCheckBoxMove.setSelected(model.getMoveCaptions());
            jCheckBoxMove.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        model.setMoveCaptions(jCheckBoxMove.isSelected());
                    }
                }
            });
        }
        return jCheckBoxMove;
    }

    private JComboBox getJComboBoxFpsSrc() {
        if (jComboBoxFpsSrc == null) {
            jComboBoxFpsSrc = new JComboBox();
            jComboBoxFpsSrc.setPreferredSize(new Dimension(200, 20));
            jComboBoxFpsSrc.setMinimumSize(new Dimension(150, 20));
            jComboBoxFpsSrc.setEditable(true);
            jComboBoxFpsSrc.setEnabled(false);
            jComboBoxFpsSrc.setToolTipText("Set the source frame rate (only needed for frame rate conversion)");
            for (String fps : FRAMERATES) {
                jComboBoxFpsSrc.addItem(fps);
            }
            jComboBoxFpsSrc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        String s = (String) jComboBoxFpsSrc.getSelectedItem();
                        double fpsSrc = SubtitleUtils.getFps(s);
                        if (fpsSrc > 0) {
                            model.setFpsSrc(fpsSrc);
                        }
                        jComboBoxFpsSrc.setSelectedItem(ToolBox.formatDouble(model.getFpsSrc()));
                        jComboBoxFpsSrc.getEditor().getEditorComponent().setBackground(OK_BACKGROUND);
                        model.setFpsSrcCertain(false);
                    }
                }
            });
            final JTextField fpsSrcEditor = (JTextField) jComboBoxFpsSrc.getEditor().getEditorComponent();
            fpsSrcEditor.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (model.isReady()) {
                        String s = fpsSrcEditor.getText();
                        double fpsSrc = SubtitleUtils.getFps(s);
                        Color color;
                        if (fpsSrc > 0) {
                            color = OK_BACKGROUND;
                            model.setFpsSrc(fpsSrc);
                        } else {
                            color = ERROR_BACKGROUND;
                        }
                        fpsSrcEditor.setBackground(color);
                        model.setFpsSrcCertain(false);
                    }
                }

                public void insertUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    check();
                }
            });
        }
        return jComboBoxFpsSrc;
    }

    private JComboBox getJComboBoxFpsTrg() {
        if (jComboBoxFpsTrg == null) {
            jComboBoxFpsTrg = new JComboBox();
            jComboBoxFpsTrg.setPreferredSize(new Dimension(200, 20));
            jComboBoxFpsTrg.setMinimumSize(new Dimension(150, 20));
            jComboBoxFpsTrg.setEditable(true);
            jComboBoxFpsTrg.setEnabled(false);
            jComboBoxFpsTrg.setToolTipText("Set the target frame rate");
            for (String fps : FRAMERATES) {
                jComboBoxFpsTrg.addItem(fps);
            }
            jComboBoxFpsTrg.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        String s = (String) jComboBoxFpsTrg.getSelectedItem();
                        double d = SubtitleUtils.getFps(s);
                        if (d > 0) {
                            model.setFpsTrg(d);
                        }
                        jComboBoxFpsTrg.setSelectedItem(ToolBox.formatDouble(model.getFpsTrg()));
                        jComboBoxFpsTrg.getEditor().getEditorComponent().setBackground(OK_BACKGROUND);

                        model.setDelayPTS((int)SubtitleUtils.syncTimePTS(model.getDelayPTS(), model.getFpsTrg(), model.getFpsTrgConf()));
                        jTextFieldDelay.setText(ToolBox.formatDouble(model.getDelayPTS() / 90.0));

                        model.setMinTimePTS((int)SubtitleUtils.syncTimePTS(model.getMinTimePTS(), model.getFpsTrg(), model.getFpsTrgConf()));
                        jTextFieldMinTime.setText(ToolBox.formatDouble(model.getMinTimePTS() / 90.0));
                    }
                }
            });
            final JTextField fpsTrgEditor = (JTextField) jComboBoxFpsTrg.getEditor().getEditorComponent();
            fpsTrgEditor.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (model.isReady()) {
                        String s = fpsTrgEditor.getText();
                        double fpsTrg = SubtitleUtils.getFps(s);
                        Color c;
                        if (fpsTrg > 0) {
                            if ((int)SubtitleUtils.syncTimePTS(model.getDelayPTS(), model.getFpsTrg(), model.getFpsTrgConf()) != model.getDelayPTS() || model.getMinTimePTS() != (int)SubtitleUtils.syncTimePTS(model.getMinTimePTS(), model.getFpsTrg(), model.getFpsTrgConf())) {
                                c = WARN_BACKGROUND;
                            } else {
                                c = OK_BACKGROUND;
                            }
                            model.setFpsTrg(fpsTrg);
                        } else {
                            c = ERROR_BACKGROUND;
                        }
                        fpsTrgEditor.setBackground(c);
                    }
                }

                public void insertUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    check();
                }
            });
        }
        return jComboBoxFpsTrg;
    }

    private JTextField getJTextFieldDelay() {
        if (jTextFieldDelay == null) {
            jTextFieldDelay = new JTextField();
            jTextFieldDelay.setPreferredSize(new Dimension(200, 20));
            jTextFieldDelay.setToolTipText("Set global delay (in milliseconds) added to all timestamps");
            jTextFieldDelay.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        String s = jTextFieldDelay.getText();
                        try {
                            // don't use getDouble as the value can be negative
                            model.setDelayPTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(s) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                        } catch (NumberFormatException ex) {
                        }
                        jTextFieldDelay.setBackground(OK_BACKGROUND);
                        jTextFieldDelay.setText(ToolBox.formatDouble(model.getDelayPTS() / 90.0));
                    }
                }
            });
            jTextFieldDelay.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (model.isReady()) {
                        String s = jTextFieldDelay.getText();
                        try {
                            // don't use getDouble as the value can be negative
                            model.setDelayPTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(s) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                            if (!s.equalsIgnoreCase(ToolBox.formatDouble(model.getDelayPTS() / 90.0))) {
                                jTextFieldDelay.setBackground(WARN_BACKGROUND);
                            } else {
                                jTextFieldDelay.setBackground(OK_BACKGROUND);
                            }
                        } catch (NumberFormatException ex) {
                            jTextFieldDelay.setBackground(ERROR_BACKGROUND);
                        }
                    }
                }

                public void insertUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    check();
                }
            });
        }
        return jTextFieldDelay;
    }



    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setText("Cancel");
            jButtonCancel.setToolTipText("Lose all changes and use the default values");
            jButtonCancel.setMnemonic('c');
            jButtonCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    model.setCancel(true);
                    dispose();
                }
            });
        }
        return jButtonCancel;
    }

    private JButton getJButtonStore() {
        if (jButtonStore == null) {
            jButtonStore = new JButton();
            jButtonStore.setText("Store");
            jButtonStore.setToolTipText("Store current settings as default");
            jButtonStore.setMnemonic('s');
            jButtonStore.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        // fps source
                        model.storeConvertFPS();
                        if (model.getConvertFPS()) {
                            double fpsSrc  = SubtitleUtils.getFps((String) jComboBoxFpsSrc.getSelectedItem());
                            if (fpsSrc > 0) {
                                model.setFpsSrc(fpsSrc);
                                model.storeFPSSrc();
                            }
                        }
                        // fps target
                        double fpsTrg = SubtitleUtils.getFps((String) jComboBoxFpsTrg.getSelectedItem());
                        if (fpsTrg > 0) {
                            model.setFpsTrg(fpsTrg);
                            model.storeFpsTrg();
                        }
                        // delay
                        try {
                            model.setDelayPTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(jTextFieldDelay.getText()) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                            model.storeDelayPTS();
                        } catch (NumberFormatException ex) {
                        }
                        // min time
                        model.storeFixShortFrames();
                        try {
                            model.setMinTimePTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(jTextFieldMinTime.getText()) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                            model.storeMinTimePTS();
                        } catch (NumberFormatException ex) {
                        }
                        // exit
                        model.storeConvertResolution();
                        if (model.getConvertResolution()) {
                            model.storeOutputResolution();
                        }
                        // scaleX
                        double scaleX = ToolBox.getDouble(jTextFieldScaleX.getText());
                        if (scaleX > 0) {
                            if (scaleX > MAX_FREE_SCALE_FACTOR) {
                                scaleX = MAX_FREE_SCALE_FACTOR;
                            } else if (scaleX < MIN_FREE_SCALE_FACTOR) {
                                scaleX = MIN_FREE_SCALE_FACTOR;
                            }
                            model.setFreeScaleFactorX(scaleX);
                        }
                        // scaleY
                        double scaleY = ToolBox.getDouble(jTextFieldScaleY.getText());
                        if (scaleY > 0) {
                            if (scaleY > MAX_FREE_SCALE_FACTOR) {
                                scaleY = MAX_FREE_SCALE_FACTOR;
                            } else if (scaleY < MIN_FREE_SCALE_FACTOR) {
                                scaleY = MIN_FREE_SCALE_FACTOR;
                            }
                            model.setFreeScaleFactorY(scaleY);
                        }
                        // set scale X/Y
                        model.storeApplyFreeScale();
                        if (model.getApplyFreeScale()) {
                            model.storeFreeScaleFactor();
                        }
                        // forceAll is not stored
                        model.storeConfig();
                    }
                }
            });
        }
        return jButtonStore;
    }

    private JButton getJButtonRestore() {
        if (jButtonRestore == null) {
            jButtonRestore = new JButton();
            jButtonRestore.setText("Restore");
            jButtonRestore.setToolTipText("Restore last default settings");
            jButtonRestore.setMnemonic('e');
            jButtonRestore.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    model.loadConvertResolution();
                    if (model.getConvertResolution()) {
                        model.loadOutputResolution();
                    }
                    model.loadConvertFPS();
                    if (model.getConvertFPS() && !model.isFpsSrcCertain()) {
                        model.loadFpsSrc();
                    }
                    model.loadFpsTrg();
                    model.loadDelayPTS();
                    model.loadFixShortFrames();
                    model.loadMinTimePTS();
                    model.loadApplyFreeScale();
                    if (model.getApplyFreeScale()) {
                        model.loadFreeScaleFactorX();
                        model.loadFreeScaleFactorY();
                    }
                    model.loadForcedState();
                    fillDialog();
                }
            });
        }
        return jButtonRestore;
    }

    private JButton getJButtonReset() {
        if (jButtonReset == null) {
            jButtonReset = new JButton();
            jButtonReset.setText("Reset");
            jButtonReset.setToolTipText("Reset defaults");
            jButtonReset.setMnemonic('t');
            jButtonReset.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    boolean convertResolution = CONVERT_RESOLUTION_BY_DEFAULT;
                    model.setConvertResolution(convertResolution);
                    if (convertResolution) {
                        model.setOutputResolution(DEFAULT_TARGET_RESOLUTION);
                    }
                    model.setConvertFPS(CONVERT_FRAMERATE_BY_DEFAULT);
                    if (model.getConvertFPS()) {
                        if (!model.isFpsSrcCertain()) {
                            model.setFpsSrc(DEFAULT_SOURCE_FRAMERATE);
                        }
                        model.setFpsTrg(DEFAULT_TARGET_FRAMERATE);
                    } else {
                        model.setFpsTrg(model.getFpsSrc());
                    }
                    model.setDelayPTS(DEFAULT_PTS_DELAY);
                    model.setFixShortFrames(FIX_SHORT_FRAMES_BY_DEFAULT);
                    model.setMinTimePTS(DEFAULT_MIN_DISPLAY_TIME_PTS);
                    model.setApplyFreeScale(APPLY_FREE_SCALE_BY_DEFAULT);
                    if (model.getApplyFreeScale()) {
                        model.setFreeScaleFactorX(DEFAULT_FREE_SCALE_FACTOR_X);
                        model.setFreeScaleFactorY(DEFAULT_FREE_SCALE_FACTOR_Y);
                    }
                    model.setForcedState(ForcedFlagState.KEEP);
                    fillDialog();
                }
            });
        }
        return jButtonReset;
    }

    private JCheckBox getJCheckBoxScale() {
        if (jCheckBoxScale == null) {
            jCheckBoxScale = new JCheckBox();
            jCheckBoxScale.setToolTipText("Allow free scaling of subtitles in X and Y direction");
            jCheckBoxScale.setText("Apply free scaling");
            jCheckBoxScale.setMnemonic('a');
            jCheckBoxScale.setFocusable(false);
            jCheckBoxScale.setIconTextGap(10);
            jCheckBoxScale.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (model.isReady()) {
                        boolean changeScale = jCheckBoxScale.isSelected();
                        model.setApplyFreeScale(changeScale);
                        jTextFieldScaleX.setEnabled(changeScale);
                        jTextFieldScaleY.setEnabled(changeScale);
                    }
                }
            });
        }
        return jCheckBoxScale;
    }

    private JCheckBox getJCheckBoxFixMinTime() {
        if (jCheckBoxFixMinTime == null) {
            jCheckBoxFixMinTime = new JCheckBox();
            jCheckBoxFixMinTime.setToolTipText("Force a minimum display duration of 'Min Time'");
            jCheckBoxFixMinTime.setText("Fix too short frames");
            jCheckBoxFixMinTime.setMnemonic('x');
            jCheckBoxFixMinTime.setFocusable(false);
            jCheckBoxFixMinTime.setIconTextGap(10);
            jCheckBoxFixMinTime.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (model.isReady()) {
                        boolean fixShortFrames = jCheckBoxFixMinTime.isSelected();
                        model.setFixShortFrames(fixShortFrames);
                        jTextFieldMinTime.setEnabled(fixShortFrames);
                    }
                }
            });
        }
        return jCheckBoxFixMinTime;
    }

    private JTextField getJTextFieldMinTime() {
        if (jTextFieldMinTime == null) {
            jTextFieldMinTime = new JTextField();
            jTextFieldMinTime.setEditable(true);
            jTextFieldMinTime.setPreferredSize(new Dimension(200, 20));
            jTextFieldMinTime.setEnabled(false);
            jTextFieldMinTime.setToolTipText("Set minimum display time for a subtitle");
            jTextFieldMinTime.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        String s = jTextFieldMinTime.getText();
                        try {
                            model.setMinTimePTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(s) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                        } catch (NumberFormatException ex) {
                        }
                        jTextFieldMinTime.setBackground(OK_BACKGROUND);
                        jTextFieldMinTime.setText(ToolBox.formatDouble(model.getMinTimePTS() / 90.0));
                    }
                }
            });
            jTextFieldMinTime.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (model.isReady()) {
                        String s = jTextFieldMinTime.getText();
                        try {
                            model.setMinTimePTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(s) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                            if (!s.equalsIgnoreCase(ToolBox.formatDouble(model.getMinTimePTS() / 90.0))) {
                                jTextFieldMinTime.setBackground(WARN_BACKGROUND);
                            } else {
                                jTextFieldMinTime.setBackground(OK_BACKGROUND);
                            }
                        } catch (NumberFormatException ex) {
                            jTextFieldMinTime.setBackground(ERROR_BACKGROUND);
                        }
                    }
                }

                public void insertUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    check();
                }
            });
        }
        return jTextFieldMinTime;
    }

    private JButton getJButtonOk() {
        if (jButtonOk == null) {
            jButtonOk = new JButton();
            jButtonOk.setText("  Ok  ");
            jButtonOk.setMnemonic('o');
            jButtonOk.setToolTipText("Use current values and continue");
            jButtonOk.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        // fps source
                        model.setConvertFPSConf(model.getConvertFPS());
                        if (model.getConvertFPS()) {
                            double fpsSrc = SubtitleUtils.getFps((String) jComboBoxFpsSrc.getSelectedItem());
                            if (fpsSrc > 0) {
                                model.setFpsSrc(fpsSrc);
                                model.setFPSSrcConf(fpsSrc);
                            }
                        }
                        // fps target
                        double fpsTrg = SubtitleUtils.getFps((String) jComboBoxFpsTrg.getSelectedItem());
                        if (fpsTrg > 0) {
                            model.setFpsTrg(fpsTrg);
                            model.setFpsTrgConf(fpsTrg);
                        }
                        // delay
                        try {
                            model.setDelayPTS((int) SubtitleUtils.syncTimePTS((long) (Double.parseDouble(jTextFieldDelay.getText()) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                            model.setDelayPTSConf(model.getDelayPTS());
                        } catch (NumberFormatException ex) {
                        }
                        // min time
                        model.setFixShortFramesConf(model.getFixShortFrames());
                        try {
                            model.setMinTimePTS((int) SubtitleUtils.syncTimePTS((long) (Double.parseDouble(jTextFieldMinTime.getText()) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                            model.setMinTimePTSConf(model.getMinTimePTS());
                        } catch (NumberFormatException ex) {
                        }
                        // exit
                        boolean convertResolution = model.getConvertResolution();
                        model.setConvertResolutionConf(convertResolution);
                        if (convertResolution) {
                            model.setOutputResolutionConf(model.getOutputResolution());
                        }
                        // scaleX
                        double scaleX = ToolBox.getDouble(jTextFieldScaleX.getText());
                        if (scaleX > 0) {
                            if (scaleX > MAX_FREE_SCALE_FACTOR) {
                                scaleX = MAX_FREE_SCALE_FACTOR;
                            } else if (scaleX < MIN_FREE_SCALE_FACTOR) {
                                scaleX = MIN_FREE_SCALE_FACTOR;
                            }
                            model.setFreeScaleFactorX(scaleX);
                        }
                        // scaleY
                        double scaleY = ToolBox.getDouble(jTextFieldScaleY.getText());
                        if (scaleY > 0) {
                            if (scaleY > MAX_FREE_SCALE_FACTOR) {
                                scaleY = MAX_FREE_SCALE_FACTOR;
                            } else if (scaleY < MIN_FREE_SCALE_FACTOR) {
                                scaleY = MIN_FREE_SCALE_FACTOR;
                            }
                            model.setFreeScaleFactorY(scaleY);
                        }
                        // set scale X/Y
                        model.setApplyFreeScaleConf(model.getApplyFreeScale());
                        if (model.getApplyFreeScale()) {
                            model.setFreeScaleFactorConf(model.getFreeScaleFactorX(), model.getFreeScaleFactorY());
                        }
                        model.setCancel(false);
                        // forced state
                        Core.setForceAll();
                        // keep move settings
                        if (jCheckBoxMove.isEnabled()) {
                            model.storeMoveCaptions();
                        }
                        //
                        dispose();
                    }
                }
            });
            jButtonOk.addAncestorListener(new RequestFocusListener());
        }
        return jButtonOk;
    }

    private JTextField getJTextFieldScaleX() {
        if (jTextFieldScaleX == null) {
            jTextFieldScaleX = new JTextField();
            jTextFieldScaleX.setPreferredSize(new Dimension(200, 20));
            jTextFieldScaleX.setToolTipText("Set free scaling factor in X direction");
            jTextFieldScaleX.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        String s = jTextFieldScaleX.getText();
                        double scaleX = ToolBox.getDouble(s);
                        if (scaleX >0) {
                            if (scaleX > MAX_FREE_SCALE_FACTOR) {
                                scaleX = MAX_FREE_SCALE_FACTOR;
                            } else if (scaleX < MIN_FREE_SCALE_FACTOR) {
                                scaleX = MIN_FREE_SCALE_FACTOR;
                            }
                            model.setFreeScaleFactorX(scaleX);
                        }
                        jTextFieldScaleX.setText(ToolBox.formatDouble(model.getFreeScaleFactorX()));
                        jTextFieldScaleX.setBackground(OK_BACKGROUND);
                    }
                }
            });
            jTextFieldScaleX.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (model.isReady()) {
                        double scaleX = ToolBox.getDouble(jTextFieldScaleX.getText());
                        if (scaleX >= MIN_FREE_SCALE_FACTOR && scaleX <= MAX_FREE_SCALE_FACTOR) {
                            model.setFreeScaleFactorX(scaleX);
                            jTextFieldScaleX.setBackground(OK_BACKGROUND);
                        } else {
                            jTextFieldScaleX.setBackground(ERROR_BACKGROUND);
                        }
                    }
                }

                public void insertUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    check();
                }
            });
        }
        return jTextFieldScaleX;
    }

    private JTextField getJTextFieldScaleY() {
        if (jTextFieldScaleY == null) {
            jTextFieldScaleY = new JTextField();
            jTextFieldScaleY.setPreferredSize(new Dimension(200, 20));
            jTextFieldScaleY.setToolTipText("Set free scaling factor in Y direction");
            jTextFieldScaleY.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (model.isReady()) {
                        double scaleY = ToolBox.getDouble(jTextFieldScaleY.getText());
                        if (scaleY > 0) {
                            if (scaleY > MAX_FREE_SCALE_FACTOR) {
                                scaleY = MAX_FREE_SCALE_FACTOR;
                            } else if (scaleY < MIN_FREE_SCALE_FACTOR) {
                                scaleY = MIN_FREE_SCALE_FACTOR;
                            }
                            model.setFreeScaleFactorY(scaleY);
                        }
                        jTextFieldScaleY.setText(ToolBox.formatDouble(model.getFreeScaleFactorY()));
                    }
                }
            });
            jTextFieldScaleY.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (model.isReady()) {
                        double scaleY = ToolBox.getDouble(jTextFieldScaleY.getText());
                        if (scaleY >= MIN_FREE_SCALE_FACTOR && scaleY <= MAX_FREE_SCALE_FACTOR) {
                            model.setFreeScaleFactorY(scaleY);
                            jTextFieldScaleY.setBackground(OK_BACKGROUND);
                        } else {
                            jTextFieldScaleY.setBackground(ERROR_BACKGROUND);
                        }
                    }
                }

                public void insertUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    check();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    check();
                }
            });
        }
        return jTextFieldScaleY;
    }

    private JComboBox getJComboBoxForced() {
        if (jComboBoxForced == null) {
            jComboBoxForced = new JComboBox();
            jComboBoxForced.setPreferredSize(new Dimension(200, 20));
            jComboBoxForced.setMinimumSize(new Dimension(150, 20));
            jComboBoxForced.setEditable(false);
            jComboBoxForced.setToolTipText("Select the target resolution");
            for (ForcedFlagState state : ForcedFlagState.values()) {
                jComboBoxForced.addItem(state.toString());
            }
            jComboBoxForced.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (model.isReady()) {
                        int idx = jComboBoxForced.getSelectedIndex();
                        for (ForcedFlagState forcedFlagState : ForcedFlagState.values()) {
                            if (idx == forcedFlagState.ordinal()) {
                                model.setForcedState(forcedFlagState);
                                break;
                            }
                        }
                    }
                }
            });
        }
        return jComboBoxForced;
    }

    public void enableOptionMove(boolean e) {
        jCheckBoxMove.setEnabled(e);
    }
}
