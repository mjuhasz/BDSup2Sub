package bdsup2sub.gui;


import bdsup2sub.bitmap.ErasePatch;
import bdsup2sub.core.Core;
import bdsup2sub.core.CoreException;
import bdsup2sub.core.Resolution;
import bdsup2sub.supstream.SubPicture;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static bdsup2sub.gui.GuiUtils.centerRelativeToParent;
import static bdsup2sub.utils.TimeUtils.ptsToTimeStr;
import static bdsup2sub.utils.TimeUtils.timeStrToPTS;

/*
 * Copyright 2009 Volker Oth (0xdeadbeef)
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

/**
 * Edit dialog - part of BDSup2Sub GUI classes.
 *
 * @author 0xdeadbeef
 */
public class EditDialog extends JDialog implements SelectListener {

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane;

    private JPanel jPanelUp;

    private JPanel jPanelLayout;

    private JPanel jPanelOffsets;

    private JPanel jPanelTimes;

    private JPanel jPanelButtons;

    private JPanel jPanelCheck;

    private JLabel jLabelInfo;

    private JButton jButtonPrev;

    private JButton jButtonNext;

    private JButton jButtonStoreNext;

    private JButton jButtonStorePrev;

    private EditPane jPanelPreview;

    private JSlider jSliderVertical;

    private JSlider jSliderHorizontal;

    private JButton jButtonCancel;

    private JButton jButtonOk;

    private JTextField jTextFieldX;

    private JTextField jTextFieldY;

    private JButton jButtonCenter;

    private JTextField jTextFieldStart;

    private JTextField jTextFieldEnd;

    private JTextField jTextFieldDuration;

    private JButton jButtonMin;

    private JButton jButtonMax;

    private JButton jButtonTop;

    private JButton jButtonBottom;

    private JButton jButtonStore;

    private JCheckBox jCheckBoxForced;

    private JCheckBox jCheckBoxExclude;

    private JPanel jPanelPatches;

    private JButton jButtonAddPatch;

    private JButton jButtonUndoPatch;

    private JButton jButtonUndoAllPatches;


    /** width of preview pane */
    private static int miniWidth = 768;
    /** height of preview pane */
    private static int miniHeight = 432;

    /** background color for errors */
    private Color errBgnd = new Color(0xffe1acac);
    /** background color for warnings */
    private Color warnBgnd = new Color(0xffffffc0);
    /** background color for ok */
    private Color okBgnd = UIManager.getColor("TextField.background");

    /** image of subpicture to display in preview pane */
    private BufferedImage image;
    /** semaphore to disable slider events when setting the slider values */
    private boolean enableSliders;
    /** current subtitle index */
    private int index;
    /** current subpicture */
    private SubPicture subPic;
    /** next subpicture (or null if none) */
    private SubPicture subPicNext;
    /** previous subpicture (or null if none) */
    private SubPicture subPicPrev;
    /** time of one (target) frame in 90kHz resolution */
    private int frameTime;
    /** dirty flag that tells if any value might have been changed */
    private volatile boolean edited;
    /** semaphore to disable actions while changing component properties */
    private volatile boolean isReady;


    /**
     * Constructor
     * @param owner parent frame
     *
     */
    public EditDialog(Frame owner) {
        super(owner, true);

        Resolution r = Core.getOutputResolution();
        switch (r) {
            case PAL:
            case NTSC:
                miniWidth = 720;
                miniHeight = 405;
                break;
            case HD_1080:
            case HD_1440x1080:
            case HD_720:
            default:
                miniWidth = 640;
                miniHeight = 320;
                break;
        }

        initialize();
        centerRelativeToParent(this, owner);
        setResizable(false);
        // determine frame time
        frameTime = (int)(90000/Core.getFPSTrg());
        // allow selection
        jPanelPreview.setAllowSelection(true);
        jPanelPreview.addSelectListener(this);
    }

    /**
     * This method initializes this dialog
     */
    private void initialize() {
        this.setSize(miniWidth+36, miniHeight+280);
        this.setContentPane(getJContentPane());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    /**
     * sets dirty flag and enables/disables the store button accordingly
     * @param e true: was edited
     */
    private void setEdited(boolean e) {
        edited = e;
        jButtonStore.setEnabled(e);
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            GridBagConstraints gridBagPanelUp = new GridBagConstraints();
            gridBagPanelUp.gridx = 0;
            gridBagPanelUp.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelUp.weightx = 1.0;
            gridBagPanelUp.weighty = 1.0;
            gridBagPanelUp.gridwidth = 2;
            gridBagPanelUp.fill = GridBagConstraints.HORIZONTAL;
            gridBagPanelUp.gridy = 0;

            GridBagConstraints gridBagPanelLayout = new GridBagConstraints();
            gridBagPanelLayout.gridx = 0;
            gridBagPanelLayout.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelLayout.weightx = 1.0;
            gridBagPanelLayout.weighty = 1.0;
            gridBagPanelLayout.fill = GridBagConstraints.HORIZONTAL;
            gridBagPanelLayout.gridy = 1;
            gridBagPanelLayout.gridwidth = 2;

            GridBagConstraints gridBagPanelTimes = new GridBagConstraints();
            gridBagPanelTimes.gridx = 0;
            gridBagPanelTimes.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelTimes.weightx = 1.0;
            gridBagPanelTimes.weighty = 1.0;
            gridBagPanelTimes.fill = GridBagConstraints.HORIZONTAL;
            gridBagPanelTimes.gridy = 2;

            GridBagConstraints gridBagPanelOffsets = new GridBagConstraints();
            gridBagPanelOffsets.gridx = 1;
            gridBagPanelOffsets.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelOffsets.weightx = 1.0;
            gridBagPanelOffsets.weighty = 1.0;
            gridBagPanelOffsets.fill = GridBagConstraints.BOTH;
            gridBagPanelOffsets.gridy = 2;


            GridBagConstraints gridBagPanelCheck = new GridBagConstraints();
            gridBagPanelCheck.gridx = 0;
            gridBagPanelCheck.fill = GridBagConstraints.BOTH;
            gridBagPanelCheck.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelCheck.weightx = 1.0;
            gridBagPanelCheck.weighty = 1.0;
            gridBagPanelCheck.gridy = 3;

            GridBagConstraints gridBagPanelPatches = new GridBagConstraints();
            gridBagPanelPatches.gridx = 1;
            gridBagPanelPatches.fill = GridBagConstraints.BOTH;
            gridBagPanelPatches.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelPatches.weightx = 1.0;
            gridBagPanelPatches.weighty = 1.0;
            gridBagPanelPatches.gridy = 3;

            GridBagConstraints gridBagPanelButtons = new GridBagConstraints();
            gridBagPanelButtons.gridx = 0;
            gridBagPanelButtons.fill = GridBagConstraints.HORIZONTAL;
            gridBagPanelButtons.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelButtons.weightx = 1.0;
            gridBagPanelButtons.weighty = 1.0;
            gridBagPanelButtons.gridy = 4;
            gridBagPanelButtons.gridwidth = 2;

            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(getJPanelUp(), gridBagPanelUp);
            jContentPane.add(getJPanelLayout(), gridBagPanelLayout);
            jContentPane.add(getJPanelOffsets(), gridBagPanelOffsets);
            jContentPane.add(getJPanelTimes(), gridBagPanelTimes);
            jContentPane.add(getJPanelButtons(), gridBagPanelButtons);
            jContentPane.add(getJPanelCheck(), gridBagPanelCheck);
            jContentPane.add(getJPanelPatches(), gridBagPanelPatches);
        }
        return jContentPane;
    }

    /**
     * This method initializes jPanelUp
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelUp() {
        if (jPanelUp == null) {
            GridBagConstraints gridBagButtonStorePrev = new GridBagConstraints();
            gridBagButtonStorePrev.gridx = 1;
            gridBagButtonStorePrev.gridy = 0;
            gridBagButtonStorePrev.anchor = GridBagConstraints.NORTHWEST;
            gridBagButtonStorePrev.insets = new Insets(2, 4, 2, 6);
            GridBagConstraints gridBagButtonStoreNext = new GridBagConstraints();
            gridBagButtonStoreNext.gridx = 2;
            gridBagButtonStoreNext.gridy = 0;
            gridBagButtonStoreNext.anchor = GridBagConstraints.NORTHWEST;
            gridBagButtonStoreNext.insets = new Insets(2, 4, 2, 12);
            GridBagConstraints gridBagButtonNext = new GridBagConstraints();
            gridBagButtonNext.gridx = 4;
            gridBagButtonNext.anchor = GridBagConstraints.NORTHWEST;
            gridBagButtonNext.insets = new Insets(2, 4, 2, 6);
            gridBagButtonNext.gridy = 0;
            GridBagConstraints gridBagButtonPrev = new GridBagConstraints();
            gridBagButtonPrev.gridx = 3;
            gridBagButtonPrev.anchor = GridBagConstraints.NORTHWEST;
            gridBagButtonPrev.insets = new Insets(2, 4, 2, 4);
            gridBagButtonPrev.gridy = 0;
            GridBagConstraints gridBagInfo = new GridBagConstraints();
            gridBagInfo.weightx = 1.0;
            gridBagInfo.anchor = GridBagConstraints.WEST;
            gridBagInfo.insets = new Insets(4, 6, 0, 4);
            gridBagInfo.weighty = 1.0;
            jLabelInfo = new JLabel();
            jLabelInfo.setText("Info");
            jPanelUp = new JPanel();
            jPanelUp.setPreferredSize(new Dimension(400, 25));
            jPanelUp.setMinimumSize(new Dimension(400, 25));
            jPanelUp.setLayout(new GridBagLayout());

            jPanelUp.add(jLabelInfo, gridBagInfo);
            jPanelUp.add(getJButtonPrev(), gridBagButtonPrev);
            jPanelUp.add(getJButtonNext(), gridBagButtonNext);
            jPanelUp.add(getJButtonStoreNext(), gridBagButtonStoreNext);
            jPanelUp.add(getJButtonStorePrev(), gridBagButtonStorePrev);
        }
        return jPanelUp;
    }

    /**
     * This method initializes jPanelLayout
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelLayout() {
        if (jPanelLayout == null) {
            GridBagConstraints gridBagSliderHorizontal = new GridBagConstraints();
            gridBagSliderHorizontal.fill = GridBagConstraints.HORIZONTAL;
            gridBagSliderHorizontal.gridy = 1;
            gridBagSliderHorizontal.weightx = 1.0;
            gridBagSliderHorizontal.gridx = 0;
            GridBagConstraints gridBagSliderVertical = new GridBagConstraints();
            gridBagSliderVertical.fill = GridBagConstraints.VERTICAL;
            gridBagSliderVertical.gridy = 0;
            gridBagSliderVertical.weightx = 1.0;
            gridBagSliderVertical.insets = new Insets(0, 0, 0, 2);
            gridBagSliderVertical.gridx = 1;
            GridBagConstraints gridBagPanelPreview = new GridBagConstraints();
            gridBagPanelPreview.gridx = 0;
            gridBagPanelPreview.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelPreview.weighty = 0.0;
            gridBagPanelPreview.gridy = 0;
            gridBagPanelPreview.insets = new Insets(0, 4, 0, 0);
            jPanelLayout = new JPanel();
            jPanelLayout.setLayout(new GridBagLayout());
            jPanelLayout.add(getJPanelPreview(), gridBagPanelPreview);
            jPanelLayout.add(getJSliderVertical(), gridBagSliderVertical);
            jPanelLayout.add(getJSliderHorizontal(), gridBagSliderHorizontal);
        }
        return jPanelLayout;
    }

    /**
     * This method initializes jPanelOffsets
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelOffsets() {
        if (jPanelOffsets == null) {
            GridBagConstraints gridBagButtonBottom = new GridBagConstraints();
            gridBagButtonBottom.gridx = 3;
            gridBagButtonBottom.anchor = GridBagConstraints.NORTHWEST;
            gridBagButtonBottom.weightx = 120.0;
            gridBagButtonBottom.insets = new Insets(1, 0, 0, 0);
            gridBagButtonBottom.weighty = 10.0;
            gridBagButtonBottom.gridy = 1;
            GridBagConstraints gridBagButtonTop = new GridBagConstraints();
            gridBagButtonTop.gridx = 2;
            gridBagButtonTop.anchor = GridBagConstraints.NORTHWEST;
            gridBagButtonTop.weightx = 10.0;
            gridBagButtonTop.insets = new Insets(1, 0, 0, 0);
            gridBagButtonTop.weighty = 10.0;
            gridBagButtonTop.gridy = 1;
            GridBagConstraints gridBagButtonCenter = new GridBagConstraints();
            gridBagButtonCenter.gridx = 2;
            gridBagButtonCenter.anchor = GridBagConstraints.NORTHWEST;
            gridBagButtonCenter.insets = new Insets(0, 0, 2, 0);
            gridBagButtonCenter.weightx = 10.0;
            gridBagButtonCenter.gridy = 0;
            GridBagConstraints gridBagTextY = new GridBagConstraints();
            gridBagTextY.fill = GridBagConstraints.NONE;
            gridBagTextY.gridy = 1;
            gridBagTextY.weightx = 6.0;
            gridBagTextY.anchor = GridBagConstraints.NORTHWEST;
            gridBagTextY.insets = new Insets(2, 0, 0, 0);
            gridBagTextY.weighty = 10.0;
            gridBagTextY.gridx = 1;
            GridBagConstraints gridBagTextX = new GridBagConstraints();
            gridBagTextX.fill = GridBagConstraints.NONE;
            gridBagTextX.gridy = 0;
            gridBagTextX.weightx = 6.0;
            gridBagTextX.anchor = GridBagConstraints.NORTHWEST;
            gridBagTextX.insets = new Insets(1, 0, 2, 0);
            gridBagTextX.weighty = 0.0;
            gridBagTextX.gridx = 1;
            GridBagConstraints gridBagLabelY = new GridBagConstraints();
            gridBagLabelY.gridx = 0;
            gridBagLabelY.anchor = GridBagConstraints.NORTHWEST;
            gridBagLabelY.weightx = 0.0;
            gridBagLabelY.weighty = 10.0;
            gridBagLabelY.insets = new Insets(4, 6, 0, 4);
            gridBagLabelY.gridy = 1;
            JLabel jLabelY = new JLabel();
            jLabelY.setText("Y Offset  ");
            jLabelY.setPreferredSize(new Dimension(80, 14));
            jLabelY.setMinimumSize(new Dimension(80, 14));
            GridBagConstraints gridBagLabelX = new GridBagConstraints();
            gridBagLabelX.gridx = 0;
            gridBagLabelX.anchor = GridBagConstraints.NORTHWEST;
            gridBagLabelX.weightx = 0.0;
            gridBagLabelX.weighty = 0.0;
            gridBagLabelX.insets = new Insets(4, 6, 2, 4);
            gridBagLabelX.gridy = 0;
            JLabel jLabelX = new JLabel();
            jLabelX.setText("X Offset  ");
            jLabelX.setPreferredSize(new Dimension(80, 14));
            jLabelX.setMinimumSize(new Dimension(80, 14));
            jPanelOffsets = new JPanel();
            jPanelOffsets.setLayout(new GridBagLayout());
            jPanelOffsets.setBorder(BorderFactory.createTitledBorder(null, "Position", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelOffsets.add(jLabelX, gridBagLabelX);
            jPanelOffsets.add(jLabelY, gridBagLabelY);
            jPanelOffsets.add(getJTextFieldX(), gridBagTextX);
            jPanelOffsets.add(getJTextFieldY(), gridBagTextY);
            jPanelOffsets.add(getJButtonCenter(), gridBagButtonCenter);
            jPanelOffsets.add(getJButtonTop(), gridBagButtonTop);
            jPanelOffsets.add(getJButtonBottom(), gridBagButtonBottom);
        }
        return jPanelOffsets;
    }

    /**
     * This method initializes jPanelTimes
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelTimes() {
        if (jPanelTimes == null) {
            GridBagConstraints gridBagButtonMax = new GridBagConstraints();
            gridBagButtonMax.gridx = 3;
            gridBagButtonMax.weightx = 120.0;
            gridBagButtonMax.anchor = GridBagConstraints.WEST;
            gridBagButtonMax.gridy = 2;
            GridBagConstraints gridBagButtonMin = new GridBagConstraints();
            gridBagButtonMin.gridx = 2;
            gridBagButtonMin.weightx = 10.0;
            gridBagButtonMin.anchor = GridBagConstraints.WEST;
            gridBagButtonMin.gridy = 2;
            GridBagConstraints gridBagTextDuration = new GridBagConstraints();
            gridBagTextDuration.fill = GridBagConstraints.NONE;
            gridBagTextDuration.gridy = 2;
            gridBagTextDuration.weightx = 6.0;
            gridBagTextDuration.anchor = GridBagConstraints.WEST;
            gridBagTextDuration.insets = new Insets(2, 0, 0, 0);
            gridBagTextDuration.gridx = 1;
            GridBagConstraints gridBagTextEnd = new GridBagConstraints();
            gridBagTextEnd.fill = GridBagConstraints.NONE;
            gridBagTextEnd.gridy = 1;
            gridBagTextEnd.weightx = 6.0;
            gridBagTextEnd.insets = new Insets(2, 0, 2, 0);
            gridBagTextEnd.anchor = GridBagConstraints.WEST;
            gridBagTextEnd.gridx = 1;
            GridBagConstraints gridBagTextStart = new GridBagConstraints();
            gridBagTextStart.fill = GridBagConstraints.NONE;
            gridBagTextStart.gridy = 0;
            gridBagTextStart.weightx = 6.0;
            gridBagTextStart.anchor = GridBagConstraints.WEST;
            gridBagTextStart.insets = new Insets(2, 0, 2, 0);
            gridBagTextStart.gridx = 1;
            GridBagConstraints gridBagLabelDuration = new GridBagConstraints();
            gridBagLabelDuration.gridx = 0;
            gridBagLabelDuration.anchor = GridBagConstraints.WEST;
            gridBagLabelDuration.insets = new Insets(0, 6, 0, 4);
            gridBagLabelDuration.weightx = 0.0;
            gridBagLabelDuration.weighty = 0.0;
            gridBagLabelDuration.gridy = 2;
            JLabel jLabelDuration = new JLabel();
            jLabelDuration.setText("Duration (ms)");
            jLabelDuration.setPreferredSize(new Dimension(80, 14));
            jLabelDuration.setMinimumSize(new Dimension(80, 14));
            GridBagConstraints gridBagLabelEnd = new GridBagConstraints();
            gridBagLabelEnd.gridx = 0;
            gridBagLabelEnd.anchor = GridBagConstraints.WEST;
            gridBagLabelEnd.insets = new Insets(0, 6, 2, 4);
            gridBagLabelEnd.weightx = 0.0;
            gridBagLabelEnd.weighty = 0.0;
            gridBagLabelEnd.gridy = 1;
            JLabel jLabelEnd = new JLabel();
            jLabelEnd.setText("End Time");
            jLabelEnd.setPreferredSize(new Dimension(80, 14));
            jLabelEnd.setMinimumSize(new Dimension(80, 14));
            GridBagConstraints gridBagLabelStart = new GridBagConstraints();
            gridBagLabelStart.gridx = 0;
            gridBagLabelStart.anchor = GridBagConstraints.WEST;
            gridBagLabelStart.insets = new Insets(0, 6, 2, 4);
            gridBagLabelStart.weightx = 0.0;
            gridBagLabelStart.weighty = 0.0;
            gridBagLabelStart.gridy = 0;
            JLabel jLabelStart = new JLabel();
            jLabelStart.setText("Start Time");
            jLabelStart.setPreferredSize(new Dimension(80, 14));
            jLabelStart.setMinimumSize(new Dimension(80, 14));
            jPanelTimes = new JPanel();
            jPanelTimes.setLayout(new GridBagLayout());
            jPanelTimes.setBorder(BorderFactory.createTitledBorder(null, "Times", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelTimes.add(jLabelStart, gridBagLabelStart);
            jPanelTimes.add(jLabelEnd, gridBagLabelEnd);
            jPanelTimes.add(jLabelDuration, gridBagLabelDuration);
            jPanelTimes.add(getJTextFieldStart(), gridBagTextStart);
            jPanelTimes.add(getJTextFieldEnd(), gridBagTextEnd);
            jPanelTimes.add(getJTextFieldDuration(), gridBagTextDuration);
            jPanelTimes.add(getJButtonMin(), gridBagButtonMin);
            jPanelTimes.add(getJButtonMax(), gridBagButtonMax);
        }
        return jPanelTimes;
    }

    /**
     * This method initializes jPanelCheck
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelCheck() {
        if (jPanelCheck == null) {
            GridBagConstraints gridBagCheckExclude = new GridBagConstraints();
            gridBagCheckExclude.gridx = 0;
            gridBagCheckExclude.gridy = 1;
            gridBagCheckExclude.weighty = 1.0;
            gridBagCheckExclude.anchor = GridBagConstraints.NORTHWEST;
            gridBagCheckExclude.weightx = 1.0;
            gridBagCheckExclude.insets = new Insets(0, 6, 0, 4);
            GridBagConstraints gridBagCheckForced = new GridBagConstraints();
            gridBagCheckForced.gridx = 0;
            gridBagCheckForced.gridy = 0;
            gridBagCheckForced.anchor = GridBagConstraints.NORTHWEST;
            gridBagCheckForced.weighty = 1.0;
            gridBagCheckForced.weightx = 1.0;
            gridBagCheckForced.insets = new Insets(0, 6, 0, 4);
            jPanelCheck = new JPanel();
            jPanelCheck.setLayout(new GridBagLayout());
            jPanelCheck.setPreferredSize(new Dimension(400, 23));
            jPanelCheck.setBorder(BorderFactory.createTitledBorder(null, "Flags", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelCheck.add(getJCheckBoxForced(), gridBagCheckForced);
            jPanelCheck.add(getJCheckBoxExclude(), gridBagCheckExclude);
        }
        return jPanelCheck;
    }

    /**
     * This method initializes jPanelButtons
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelButtons() {
        if (jPanelButtons == null) {
            GridBagConstraints gridBagButtonStore = new GridBagConstraints();
            gridBagButtonStore.gridx = 1;
            gridBagButtonStore.weightx = 30.0;
            gridBagButtonStore.insets = new Insets(0, 0, 2, 0);
            gridBagButtonStore.gridy = 0;
            GridBagConstraints gridBagButtonOk = new GridBagConstraints();
            gridBagButtonOk.insets = new Insets(0, 0, 2, 9);
            gridBagButtonOk.anchor = GridBagConstraints.EAST;
            gridBagButtonOk.gridx = 2;
            GridBagConstraints gridBagButtonCancel = new GridBagConstraints();
            gridBagButtonCancel.anchor = GridBagConstraints.WEST;
            gridBagButtonCancel.insets = new Insets(0, 6, 2, 0);
            gridBagButtonCancel.weightx = 1.0;
            jPanelButtons = new JPanel();
            jPanelButtons.setLayout(new GridBagLayout());
            jPanelButtons.setPreferredSize(new Dimension(400, 23));
            jPanelButtons.add(getJButtonCancel(), gridBagButtonCancel);
            jPanelButtons.add(getJButtonOk(), gridBagButtonOk);
            jPanelButtons.add(getJButtonStore(), gridBagButtonStore);
        }
        return jPanelButtons;
    }

    /**
     * This method initializes jButtonPrev
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonPrev() {
        if (jButtonPrev == null) {
            jButtonPrev = new JButton();
            jButtonPrev.setText("  <  ");
            jButtonPrev.setMnemonic(KeyEvent.VK_LEFT);
            jButtonPrev.setToolTipText("Lose changes and skip to previous frame");
            jButtonPrev.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (index > 0) {
                        setIndex(index-1);
                        setEdited(false);
                    }
                }
            });
        }
        return jButtonPrev;
    }

    /**
     * This method initializes jButtonNext
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonNext() {
        if (jButtonNext == null) {
            jButtonNext = new JButton();
            jButtonNext.setText("  >  ");
            jButtonNext.setMnemonic(KeyEvent.VK_RIGHT);
            jButtonNext.setToolTipText("Lose changes and skip to next frame");
            jButtonNext.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (index < Core.getNumFrames()-1) {
                        setIndex(index+1);
                        setEdited(false);
                    }
                }
            });
        }
        return jButtonNext;
    }

    /**
     * This method initializes jPanelPreview
     *
     * @return javax.swing.JPanel
     */
    private EditPane getJPanelPreview() {
        if (jPanelPreview == null) {
            jPanelPreview = new EditPane();
            jPanelPreview.setLayout(new GridBagLayout());
            Dimension dim = new Dimension(miniWidth, miniHeight);
            jPanelPreview.setPreferredSize(dim);
            jPanelPreview.setSize(dim);
            jPanelPreview.setMinimumSize(dim);
            jPanelPreview.setMaximumSize(dim);
        }
        return jPanelPreview;
    }

    /**
     * This method initializes jSliderVertical
     *
     * @return javax.swing.JSlider
     */
    private JSlider getJSliderVertical() {
        if (jSliderVertical == null) {
            jSliderVertical = new JSlider();
            jSliderVertical.setOrientation(JSlider.VERTICAL);
            jSliderVertical.setToolTipText("Move subtitle vertically");
            jSliderVertical.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (enableSliders) {
                        int y = subPic.height-jSliderVertical.getValue();

                        if (y < Core.getCropOfsY()) {
                            y = Core.getCropOfsY();
                        } else if (y > subPic.height - subPic.getImageHeight() - Core.getCropOfsY()) {
                            y = subPic.height - subPic.getImageHeight() - Core.getCropOfsY();
                        }

                        if (y != subPic.getOfsY()) {
                            subPic.setOfsY(y);
                            jTextFieldY.setText(""+subPic.getOfsY());
                            jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
                            jPanelPreview.setAspectRatio(21.0/9);
                            jPanelPreview.repaint();
                            setEdited(true);
                        }
                    }
                }
            });
        }
        return jSliderVertical;
    }

    /**
     * This method initializes jSliderHorizontal
     *
     * @return javax.swing.JSlider
     */
    private JSlider getJSliderHorizontal() {
        if (jSliderHorizontal == null) {
            jSliderHorizontal = new JSlider();
            jSliderHorizontal.setToolTipText("Move subtitle horizontally");
            jSliderHorizontal.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (enableSliders) {
                        int x = jSliderHorizontal.getValue();

                        if (x < 0) {
                            x = 0;
                        } else if (x > subPic.width - subPic.getImageWidth()) {
                            x = subPic.width - subPic.getImageWidth();
                        }

                        if (x != subPic.getOfsX()) {
                            subPic.setOfsX(x);
                            jTextFieldX.setText(""+subPic.getOfsX());
                            jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
                            jPanelPreview.repaint();
                            setEdited(true);
                        }
                    }
                }
            });
        }
        return jSliderHorizontal;
    }

    /**
     * This method initializes jButtonCancel
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setText("Cancel");
            jButtonCancel.setMnemonic('c');
            jButtonCancel.setToolTipText("Lose changes and return");
            jButtonCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        }
        return jButtonCancel;
    }

    /**
     * This method initializes jButtonOk
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonOk() {
        if (jButtonOk == null) {
            jButtonOk = new JButton();
            jButtonOk.setText("  Ok  ");
            jButtonOk.setMnemonic('o');
            jButtonOk.setToolTipText("Save changes and return");
            jButtonOk.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (edited) {
                        store();
                    }
                    dispose();
                }
            });
        }
        return jButtonOk;
    }

    /**
     * This method initializes jTextFieldX
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldX() {
        if (jTextFieldX == null) {
            jTextFieldX = new JTextField();
            jTextFieldX.setPreferredSize(new Dimension(80, 20));
            jTextFieldX.setMinimumSize(new Dimension(80, 20));
            jTextFieldX.setToolTipText("Set X coordinate of upper left corner of subtitle");
            jTextFieldX.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        int x = ToolBox.getInt(jTextFieldX.getText());
                        if (x == -1) {
                            x = subPic.getOfsX(); // invalid value -> keep old one
                        } else if (x < 0) {
                            x = 0;
                        } else if (x > subPic.width - subPic.getImageWidth()) {
                            x = subPic.width - subPic.getImageWidth();
                        }

                        if (x != subPic.getOfsX() ) {
                            enableSliders = false;
                            subPic.setOfsX(x);
                            jSliderHorizontal.setValue(subPic.getOfsX());
                            jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
                            jPanelPreview.repaint();
                            setEdited(true);
                            enableSliders = true;
                        }
                        jTextFieldX.setText(""+subPic.getOfsX());
                        jTextFieldX.setBackground(okBgnd);
                    }
                }
            });
            jTextFieldX.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (isReady) {
                        int x = ToolBox.getInt(jTextFieldX.getText());
                        if (x < 0 || x > subPic.width - subPic.getImageWidth()) {
                            jTextFieldX.setBackground(errBgnd);
                        } else {
                            if (x != subPic.getOfsX() ) {
                                enableSliders = false;
                                subPic.setOfsX(x);
                                jSliderHorizontal.setValue(subPic.getOfsX());
                                jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
                                jPanelPreview.repaint();
                                setEdited(true);
                                enableSliders = true;
                            }
                            jTextFieldX.setBackground(okBgnd);
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
        return jTextFieldX;
    }

    /**
     * This method initializes jTextFieldY
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldY() {
        if (jTextFieldY == null) {
            jTextFieldY = new JTextField();
            jTextFieldY.setPreferredSize(new Dimension(80, 20));
            jTextFieldY.setMinimumSize(new Dimension(80, 20));
            jTextFieldY.setToolTipText("Set Y coordinate of upper left corner of subtitle");
            jTextFieldY.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int y = ToolBox.getInt(jTextFieldY.getText());
                    if (y == -1) {
                        y = subPic.getOfsY(); // invalid value -> keep old one
                    } else if (y < Core.getCropOfsY()) {
                        y = Core.getCropOfsY();
                    } else if (y > subPic.height - subPic.getImageHeight() - Core.getCropOfsY()) {
                        y = subPic.height - subPic.getImageHeight() - Core.getCropOfsY();
                    }
                    if (y != subPic.getOfsY()) {
                        enableSliders = false;
                        subPic.setOfsY(y);
                        jSliderVertical.setValue(subPic.height-subPic.getOfsY());
                        jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
                        jPanelPreview.repaint();
                        setEdited(true);
                        enableSliders = true;
                    }
                    jTextFieldY.setText(""+subPic.getOfsY());
                    jTextFieldY.setBackground(okBgnd);
                }
            });
            jTextFieldY.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (isReady) {
                        int y = ToolBox.getInt(jTextFieldY.getText());
                        if (y < Core.getCropOfsY() || y > subPic.height - subPic.getImageHeight() - Core.getCropOfsY()) {
                            jTextFieldY.setBackground(errBgnd);
                        } else {
                            if (y != subPic.getOfsY()) {
                                enableSliders = false;
                                subPic.setOfsY(y);
                                jSliderVertical.setValue(subPic.height-subPic.getOfsY());
                                jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
                                jPanelPreview.repaint();
                                setEdited(true);
                                enableSliders = true;
                            }
                            jTextFieldY.setBackground(okBgnd);
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
        return jTextFieldY;
    }

    /**
     * This method initializes jButtonCenter
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonCenter() {
        if (jButtonCenter == null) {
            jButtonCenter = new JButton();
            jButtonCenter.setText("Center");
            jButtonCenter.setMnemonic('c');
            jButtonCenter.setToolTipText("Center subpicture horizontally");
            jButtonCenter.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    subPic.setOfsX((subPic.width-subPic.getImageWidth())/2);
                    enableSliders = false;
                    jSliderHorizontal.setValue(subPic.getOfsX());
                    jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
                    jPanelPreview.repaint();
                    jTextFieldX.setText(""+subPic.getOfsX());
                    setEdited(true);
                    enableSliders = true;
                }
            });
        }
        return jButtonCenter;
    }

    /**
     * This method initializes jTextFieldStart
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldStart() {
        if (jTextFieldStart == null) {
            jTextFieldStart = new JTextField();
            jTextFieldStart.setPreferredSize(new Dimension(80, 20));
            jTextFieldStart.setMinimumSize(new Dimension(80, 20));
            jTextFieldStart.setToolTipText("Set start time of subtitle");
            jTextFieldStart.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        isReady = false;
                        long t = Core.syncTimePTS(timeStrToPTS(jTextFieldStart.getText()), Core.getFPSTrg());
                        if (t >= subPic.endTime) {
                            t = subPic.endTime-frameTime;
                        }
                        if (subPicPrev != null && subPicPrev.endTime > t) {
                            t = subPicPrev.endTime+frameTime;
                        }
                        if (t >= 0) {
                            subPic.startTime = Core.syncTimePTS(t, Core.getFPSTrg());
                            jTextFieldDuration.setText(ToolBox.formatDouble((subPic.endTime-subPic.startTime)/90.0));
                            setEdited(true);
                        }
                        jTextFieldStart.setText(ptsToTimeStr(subPic.startTime));
                        jTextFieldStart.setBackground(okBgnd);
                        isReady = true;
                    }
                }
            });
            jTextFieldStart.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (isReady) {
                        isReady = false;
                        long t = Core.syncTimePTS(timeStrToPTS(jTextFieldStart.getText()), Core.getFPSTrg());
                        if (t < 0 || t >= subPic.endTime || subPicPrev != null && subPicPrev.endTime > t) {
                            jTextFieldStart.setBackground(errBgnd);
                        } else {
                            subPic.startTime = t;
                            jTextFieldDuration.setText(ToolBox.formatDouble((subPic.endTime-subPic.startTime)/90.0));
                            if (!jTextFieldStart.getText().equalsIgnoreCase(ptsToTimeStr(subPic.startTime))) {
                                jTextFieldStart.setBackground(warnBgnd);
                            } else {
                                jTextFieldStart.setBackground(okBgnd);
                            }
                            setEdited(true);
                        }
                        isReady = true;
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
        return jTextFieldStart;
    }

    /**
     * This method initializes jTextFieldEnd
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldEnd() {
        if (jTextFieldEnd == null) {
            jTextFieldEnd = new JTextField();
            jTextFieldEnd.setPreferredSize(new Dimension(80, 20));
            jTextFieldEnd.setMinimumSize(new Dimension(80, 20));
            jTextFieldEnd.setToolTipText("Set end time of subtitle");
            jTextFieldEnd.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        isReady = false;
                        long t = Core.syncTimePTS(timeStrToPTS(jTextFieldEnd.getText()), Core.getFPSTrg());
                        if (t <= subPic.startTime) {
                            t = subPic.startTime + frameTime;
                        }

                        if (subPicNext != null && subPicNext.startTime < t) {
                            t = subPicNext.startTime;
                        }
                        if (t >= 0) {
                            subPic.endTime = Core.syncTimePTS(t, Core.getFPSTrg());
                            jTextFieldDuration.setText(ToolBox.formatDouble((subPic.endTime-subPic.startTime)/90.0));
                            setEdited(true);
                        }
                        jTextFieldEnd.setText(ptsToTimeStr(subPic.endTime));
                        jTextFieldEnd.setBackground(okBgnd);
                        isReady = true;
                    }
                }
            });
            jTextFieldEnd.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (isReady) {
                        isReady = false;
                        long t = Core.syncTimePTS(timeStrToPTS(jTextFieldEnd.getText()), Core.getFPSTrg());
                        if (t < 0 || t <= subPic.startTime || subPicNext != null && subPicNext.startTime < t) {
                            jTextFieldEnd.setBackground(errBgnd);
                        } else {
                            subPic.endTime = t;
                            jTextFieldDuration.setText(ToolBox.formatDouble((subPic.endTime-subPic.startTime)/90.0));
                            if (!jTextFieldEnd.getText().equalsIgnoreCase(ptsToTimeStr(subPic.endTime))) {
                                jTextFieldEnd.setBackground(warnBgnd);
                            } else {
                                jTextFieldEnd.setBackground(okBgnd);
                            }
                            setEdited(true);
                        }
                        isReady = true;
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
        return jTextFieldEnd;
    }

    /**
     * This method initializes jTextFieldDuration
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldDuration() {
        if (jTextFieldDuration == null) {
            jTextFieldDuration = new JTextField();
            jTextFieldDuration.setPreferredSize(new Dimension(80, 20));
            jTextFieldDuration.setMinimumSize(new Dimension(80, 20));
            jTextFieldDuration.setToolTipText("Set display duration of subtitle in milliseconds");
            jTextFieldDuration.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        isReady = false;
                        long t = (long)(ToolBox.getDouble(jTextFieldDuration.getText())*90);
                        if (t >= 0 && t < frameTime) {
                            t = frameTime;
                        }
                        if (t > 0) {
                            t += subPic.startTime;
                            if (subPicNext != null && subPicNext.startTime < t) {
                                t = subPicNext.startTime;
                            }
                            subPic.endTime = Core.syncTimePTS(t, Core.getFPSTrg());
                            jTextFieldEnd.setText(ptsToTimeStr(subPic.endTime));
                            setEdited(true);
                        }
                        jTextFieldDuration.setText(ToolBox.formatDouble((subPic.endTime-subPic.startTime)/90.0));
                        jTextFieldDuration.setBackground(okBgnd);
                        isReady = true;
                    }
                }
            });
            jTextFieldDuration.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (isReady) {
                        isReady = false;
                        long t = (long)(ToolBox.getDouble(jTextFieldDuration.getText())*90);
                        if (t < frameTime) {
                            jTextFieldDuration.setBackground(errBgnd);
                        } else {
                            t += subPic.startTime;
                            if (subPicNext != null && subPicNext.startTime < t) {
                                t = subPicNext.startTime;
                            }
                            subPic.endTime = Core.syncTimePTS(t, Core.getFPSTrg());
                            jTextFieldEnd.setText(ptsToTimeStr(subPic.endTime));
                            setEdited(true);
                            if (!jTextFieldDuration.getText().equalsIgnoreCase(ToolBox.formatDouble((subPic.endTime-subPic.startTime)/90.0))) {
                                jTextFieldDuration.setBackground(warnBgnd);
                            } else {
                                jTextFieldDuration.setBackground(okBgnd);
                            }
                            setEdited(true);
                        }
                        isReady = true;
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
        return jTextFieldDuration;
    }

    /**
     * This method initializes jButtonMin
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonMin() {
        if (jButtonMin == null) {
            jButtonMin = new JButton();
            jButtonMin.setText("   Min   ");
            jButtonMin.setMnemonic('n');
            jButtonMin.setToolTipText("Set minimum duration");
            jButtonMin.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    long t = Core.getMinTimePTS();
                    if (t >= 0) {
                        t += subPic.startTime;
                        if (subPicNext != null && subPicNext.startTime < t) {
                            t = subPicNext.startTime;
                        }
                        subPic.endTime = Core.syncTimePTS(t, Core.getFPSTrg());
                        jTextFieldEnd.setText(ptsToTimeStr(subPic.endTime));
                        setEdited(true);
                    }
                    jTextFieldDuration.setText(ToolBox.formatDouble((subPic.endTime-subPic.startTime)/90.0));
                }
            });
        }
        return jButtonMin;
    }

    /**
     * This method initializes jButtonMax
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonMax() {
        if (jButtonMax == null) {
            jButtonMax = new JButton();
            jButtonMax.setText("   Max  ");
            jButtonMax.setMnemonic('m');
            jButtonMax.setToolTipText("Set maximum duration");
            jButtonMax.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    long t;
                    if (subPicNext != null) {
                        t = subPicNext.startTime;
                    } else {
                        t = subPic.endTime + 10000*90; // 10 seconds
                    }
                    subPic.endTime = Core.syncTimePTS(t, Core.getFPSTrg());
                    jTextFieldEnd.setText(ptsToTimeStr(subPic.endTime));
                    jTextFieldDuration.setText(ToolBox.formatDouble((subPic.endTime-subPic.startTime)/90.0));
                    setEdited(true);
                }
            });
        }
        return jButtonMax;
    }

    /**
     * This method initializes jButtonTop
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonTop() {
        if (jButtonTop == null) {
            jButtonTop = new JButton();
            jButtonTop.setText("   Top  ");
            jButtonTop.setMnemonic('t');
            jButtonTop.setToolTipText("Move to upper cinemascope bar");
            jButtonTop.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int cineH = subPic.height*5/42;
                    int y = cineH-subPic.getImageHeight();
                    if (y < 10) {
                        y = 10;
                    }
                    if (y < Core.getCropOfsY()) {
                        y = Core.getCropOfsY();
                    }
                    enableSliders = false;
                    subPic.setOfsY(y);
                    jSliderVertical.setValue(subPic.height-subPic.getOfsY());
                    jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
                    jPanelPreview.repaint();
                    jTextFieldY.setText(""+subPic.getOfsY());
                    setEdited(true);
                    enableSliders = true;
                }
            });
        }
        return jButtonTop;
    }

    /**
     * This method initializes jButtonBottom
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonBottom() {
        if (jButtonBottom == null) {
            jButtonBottom = new JButton();
            jButtonBottom.setText("Bottom");
            jButtonBottom.setMnemonic('b');
            jButtonBottom.setToolTipText("Move to lower cinemascope bar");
            jButtonBottom.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int cineH = subPic.height*5/42;
                    int y = subPic.height-cineH;
                    if (y+subPic.getImageHeight() > subPic.height - Core.getCropOfsY()) {
                        y = subPic.height - subPic.getImageHeight() - 10;
                    }
                    enableSliders = false;
                    subPic.setOfsY(y);
                    jSliderVertical.setValue(subPic.height-subPic.getOfsY());
                    jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
                    jPanelPreview.repaint();
                    jTextFieldY.setText(""+subPic.getOfsY());
                    setEdited(true);
                    enableSliders = true;
                }
            });
        }
        return jButtonBottom;
    }

    /**
     * error handler
     * @param s error string to display
     */
    public void error (String s) {
        Core.printErr(s);
        JOptionPane.showMessageDialog(this,s,"Error!", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * get current subtitle index
     * @return current subtitle index
     */
    public int getIndex() {
        return index;
    }

    /** stores the local edits in the real subpicture */
    private void store() {
        SubPicture s = Core.getSubPictureTrg(index);
        s.endTime = subPic.endTime;
        s.startTime = subPic.startTime;
        s.setOfsX(subPic.getOfsX());
        s.setOfsY(subPic.getOfsY());
        s.isforced = subPic.isforced;
        s.exclude = subPic.exclude;
        s.erasePatch = subPic.erasePatch;
    }

    /**
     * set current subtitle index, update all components
     * @param idx subtitle index
     */
    public void setIndex(int idx) {
        isReady = false;
        index = idx;
        // get prev and next
        if (idx > 0) {
            subPicPrev = Core.getSubPictureTrg(idx-1);
        } else {
            subPicPrev = null;
        }
        if (idx < Core.getNumFrames()-1) {
            subPicNext = Core.getSubPictureTrg(idx+1);
        } else {
            subPicNext = null;
        }

        // update components
        try {
            Core.convertSup(idx, idx+1, Core.getNumFrames());
            subPic = Core.getSubPictureTrg(idx).copy();
            image = Core.getTrgImagePatched(subPic);

            if (subPic.erasePatch != null && subPic.erasePatch.size()>0) {
                jButtonUndoPatch.setEnabled(true);
                jButtonUndoAllPatches.setEnabled(true);
            }

            enableSliders = false;
            jSliderHorizontal.setMaximum(subPic.width);
            jSliderHorizontal.setValue(subPic.getOfsX());
            jSliderVertical.setMaximum(subPic.height);
            jSliderVertical.setValue(subPic.height-subPic.getOfsY());
            enableSliders = true;

            jLabelInfo.setText("Frame "+(idx+1)+" of "+Core.getNumFrames());
            jTextFieldStart.setText(ptsToTimeStr(subPic.startTime));
            jTextFieldEnd.setText(ptsToTimeStr(subPic.endTime));
            jTextFieldDuration.setText(ToolBox.formatDouble((subPic.endTime-subPic.startTime)/90.0));

            jTextFieldX.setText(""+subPic.getOfsX());
            jTextFieldY.setText(""+subPic.getOfsY());

            jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
            jPanelPreview.setDim(subPic.width, subPic.height);
            jPanelPreview.setCropOfsY(Core.getCropOfsY());
            jPanelPreview.setImage(image, subPic.getImageWidth(), subPic.getImageHeight());
            jPanelPreview.repaint();
            jPanelPreview.setExcluded(subPic.exclude);

            jCheckBoxExclude.setSelected(subPic.exclude);
            jCheckBoxForced.setSelected(subPic.isforced);

            isReady = true;

        } catch (CoreException ex) {
            error(ex.getMessage());
        } catch (Exception ex) {
            ToolBox.showException(ex);
            Core.exit();
            System.exit(4);
        }
    }

    /**
     * This method initializes jButtonStore
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonStore() {
        if (jButtonStore == null) {
            jButtonStore = new JButton();
            jButtonStore.setText("Save Changes");
            jButtonStore.setMnemonic('s');
            jButtonStore.setEnabled(false);
            jButtonStore.setToolTipText("Save changes and continue editing");
            jButtonStore.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    store();
                    setEdited(false);
                }
            });
        }
        return jButtonStore;
    }

    /**
     * This method initializes jCheckBoxForced
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBoxForced() {
        if (jCheckBoxForced == null) {
            jCheckBoxForced = new JCheckBox();
            jCheckBoxForced.setText("Forced Caption");
            jCheckBoxForced.setMnemonic('f');
            jCheckBoxForced.setToolTipText("Force display of this subtitle");
            jCheckBoxForced.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    subPic.isforced = jCheckBoxForced.isSelected();
                    setEdited(true);
                }
            });
        }
        return jCheckBoxForced;
    }

    /**
     * This method initializes jCheckBoxExclude
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBoxExclude() {
        if (jCheckBoxExclude == null) {
            jCheckBoxExclude = new JCheckBox();
            jCheckBoxExclude.setText("Exclude from export");
            jCheckBoxExclude.setMnemonic('x');
            jCheckBoxExclude.setToolTipText("Exclude this subtitle from export");
            jCheckBoxExclude.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    subPic.exclude = jCheckBoxExclude.isSelected();
                    jPanelPreview.setExcluded(subPic.exclude);
                    jPanelPreview.repaint();
                    setEdited(true);
                }
            });
        }
        return jCheckBoxExclude;
    }

    @Override
    public void selectionPerformed(final boolean valid) {
        jButtonAddPatch.setEnabled(valid);
    }

    /**
     * This method initializes jPanelPatches
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelPatches() {
        if (jPanelPatches == null) {
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.weighty = 1.0;
            gridBagConstraints2.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints2.weightx = 10.0;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.weighty = 1.0;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            jPanelPatches = new JPanel();
            jPanelPatches.setLayout(new GridBagLayout());
            jPanelPatches.setBorder(BorderFactory.createTitledBorder(null, "Erase Patches", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
            jPanelPatches.add(getJButtonAddPatch(), gridBagConstraints);
            jPanelPatches.add(getJButtonUndoPatch(), gridBagConstraints1);
            jPanelPatches.add(getJButtonUndoAllPatches(), gridBagConstraints2);
        }
        return jPanelPatches;
    }

    /**
     * This method initializes jButtonAddPatch
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonAddPatch() {
        if (jButtonAddPatch == null) {
            jButtonAddPatch = new JButton();
            jButtonAddPatch.setText("Erase");
            jButtonAddPatch.setMnemonic('e');
            jButtonAddPatch.setToolTipText("Add erase patch to make the selected area transparent");
            jButtonAddPatch.setEnabled(false);
            jButtonAddPatch.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int sel[] = jPanelPreview.getSelection();
                    if (sel != null) {
                        if (subPic.erasePatch == null) {
                            subPic.erasePatch = new ArrayList<ErasePatch>();
                        }
                        ErasePatch ep = new ErasePatch(sel[0], sel[1], sel[2]-sel[0]+1, sel[3]-sel[1]+1);
                        subPic.erasePatch.add(ep);

                        jButtonUndoPatch.setEnabled(true);
                        jButtonUndoAllPatches.setEnabled(true);

                        image = Core.getTrgImagePatched(subPic);
                        jPanelPreview.setImage(image, subPic.getImageWidth(), subPic.getImageHeight());

                        setEdited(true);
                    }
                    jButtonAddPatch.setEnabled(false);
                    jPanelPreview.removeSelection();
                    jPanelPreview.repaint();
                }
            });
        }
        return jButtonAddPatch;
    }

    /**
     * This method initializes jButtonUndoPatch
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonUndoPatch() {
        if (jButtonUndoPatch == null) {
            jButtonUndoPatch = new JButton();
            jButtonUndoPatch.setText("Undo Erase");
            jButtonUndoPatch.setMnemonic('u');
            jButtonUndoPatch.setToolTipText("Remove one erase patch from the stack (undo one delete step)");
            jButtonUndoPatch.setEnabled(false);
            jButtonUndoPatch.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (subPic.erasePatch != null && subPic.erasePatch.size() > 0) {
                        subPic.erasePatch.remove(subPic.erasePatch.size()-1);
                        if (subPic.erasePatch.size() == 0) {
                            subPic.erasePatch = null;
                            jButtonUndoPatch.setEnabled(false);
                            jButtonUndoAllPatches.setEnabled(false);
                        }
                        image = Core.getTrgImagePatched(subPic);
                        jPanelPreview.setImage(image, subPic.getImageWidth(), subPic.getImageHeight());
                        jPanelPreview.repaint();
                        setEdited(true);
                    }
                }
            });
        }
        return jButtonUndoPatch;
    }

    /**
     * This method initializes jButtonUndoAllPatches
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonUndoAllPatches() {
        if (jButtonUndoAllPatches == null) {
            jButtonUndoAllPatches = new JButton();
            jButtonUndoAllPatches.setText("Undo All");
            jButtonUndoAllPatches.setMnemonic('a');
            jButtonUndoAllPatches.setEnabled(false);
            jButtonUndoAllPatches.setToolTipText("Remove all erase patches from the stack (undo all delete steps)");
            jButtonUndoAllPatches.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (subPic.erasePatch != null) {
                        subPic.erasePatch.clear();
                        subPic.erasePatch = null;
                        image = Core.getTrgImagePatched(subPic);
                        jPanelPreview.setImage(image, subPic.getImageWidth(), subPic.getImageHeight());
                        jPanelPreview.repaint();
                        setEdited(true);
                    }
                    jButtonUndoPatch.setEnabled(false);
                    jButtonUndoAllPatches.setEnabled(false);
                }
            });
        }
        return jButtonUndoAllPatches;
    }

    /**
     * This method initializes jButtonStoreNext
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonStoreNext() {
        if (jButtonStoreNext == null) {
            jButtonStoreNext = new JButton();
            jButtonStoreNext.setText("<html><font color=\"red\"><b>&nbsp;&gt;&nbsp;</b></font></html>");
            jButtonStoreNext.setToolTipText("Store changes and skip to next frame");
            jButtonStoreNext.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (edited) {
                        store();
                    }
                    if (index < Core.getNumFrames()-1) {
                        setIndex(index+1);
                        setEdited(false);
                    }
                }
            });

        }
        return jButtonStoreNext;
    }

    /**
     * This method initializes jButtonStorePrev
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonStorePrev() {
        if (jButtonStorePrev == null) {
            jButtonStorePrev = new JButton();
            jButtonStorePrev.setText("<html><font color=\"red\"><b>&nbsp;&lt;&nbsp;</b></font></html>");
            jButtonStorePrev.setToolTipText("Store changes and skip to previous frame");
            jButtonStorePrev.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (edited) {
                        store();
                    }
                    if (index > 0) {
                        setIndex(index-1);
                        setEdited(false);
                    }
                }
            });
        }
        return jButtonStorePrev;
    }
}
