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
package bdsup2sub.gui.edit;

import bdsup2sub.core.Core;
import bdsup2sub.core.CoreException;
import bdsup2sub.core.Logger;
import bdsup2sub.gui.support.EditPane;
import bdsup2sub.gui.support.RequestFocusListener;
import bdsup2sub.supstream.SubPicture;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import static bdsup2sub.gui.support.EditPane.SelectListener;
import static bdsup2sub.gui.support.GuiUtils.centerRelativeToOwner;
import static bdsup2sub.utils.TimeUtils.ptsToTimeStr;

class EditDialogView extends JDialog {

    private static final Logger logger = Logger.getInstance();

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

    private final EditDialogModel model;


    public EditDialogView(EditDialogModel model, Frame owner) {
        super(owner, true);
        this.model = model;
        initialize();
    }

    private void initialize() {
        setMinimumDimension();
        setSize(model.getMinWidth() + 36, model.getMinHeight() + 280);
        setContentPane(getJContentPane());
        centerRelativeToOwner(this);
        setResizable(false);
    }

    private void setMinimumDimension() {
        switch (model.getOutputResolution()) {
            case PAL:
            case NTSC:
                model.setMinWidth(720);
                model.setMinHeight(405);
                break;
            case HD_1080:
            case HD_1440x1080:
            case HD_720:
            default:
                model.setMinWidth(640);
                model.setMinHeight(320);
                break;
        }
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

    private JButton getJButtonPrev() {
        if (jButtonPrev == null) {
            jButtonPrev = new JButton();
            jButtonPrev.setText("  <  ");
            jButtonPrev.setMnemonic(KeyEvent.VK_LEFT);
            jButtonPrev.setToolTipText("Lose changes and skip to previous frame");
        }
        return jButtonPrev;
    }

    void addPrevButtonActionListener(ActionListener actionListener) {
        jButtonPrev.addActionListener(actionListener);
    }

    private JButton getJButtonNext() {
        if (jButtonNext == null) {
            jButtonNext = new JButton();
            jButtonNext.setText("  >  ");
            jButtonNext.setMnemonic(KeyEvent.VK_RIGHT);
            jButtonNext.setToolTipText("Lose changes and skip to next frame");
        }
        return jButtonNext;
    }

    void addNextButtonActionListener(ActionListener actionListener) {
        jButtonNext.addActionListener(actionListener);
    }

    private EditPane getJPanelPreview() {
        if (jPanelPreview == null) {
            jPanelPreview = new EditPane();
            jPanelPreview.setLayout(new GridBagLayout());
            Dimension dim = new Dimension(model.getMinWidth(), model.getMinHeight());
            jPanelPreview.setPreferredSize(dim);
            jPanelPreview.setSize(dim);
            jPanelPreview.setMinimumSize(dim);
            jPanelPreview.setMaximumSize(dim);
            jPanelPreview.setSelectionAllowed(true);
        }
        return jPanelPreview;
    }

    void addPreviewPanelSelectListener(SelectListener selectListener) {
        jPanelPreview.addSelectListener(selectListener);
    }

    void setPreviewPanelAspectRatio(double aspectRatio) {
        jPanelPreview.setAspectRatio(aspectRatio);
    }

    void setPreviewPanelOffsets(int x, int y) {
        jPanelPreview.setSubtitleOffsets(x, y);
    }

    void setPreviewPanelExcluded(boolean excluded) {
        jPanelPreview.setExcluded(excluded);
    }

    void repaintPreviewPanel() {
        jPanelPreview.repaint();
    }

    int[] getPreviewPanelSelection() {
        return jPanelPreview.getSelection();
    }

    void removePreviewPanelSelection() {
        jPanelPreview.removeSelection();
    }

    void setPreviewPanelImage(BufferedImage image, int width, int height) {
        jPanelPreview.setImage(image, width, height);
    }

    private JSlider getJSliderVertical() {
        if (jSliderVertical == null) {
            jSliderVertical = new JSlider();
            jSliderVertical.setOrientation(JSlider.VERTICAL);
            jSliderVertical.setToolTipText("Move subtitle vertically");
        }
        return jSliderVertical;
    }

    void addVerticalSliderChangeListener(ChangeListener changeListener) {
        jSliderVertical.addChangeListener(changeListener);
    }

    int getVerticalSliderValue() {
        return jSliderVertical.getValue();
    }

    void setVerticalSliderValue(int value) {
        jSliderVertical.setValue(value);
    }

    private JSlider getJSliderHorizontal() {
        if (jSliderHorizontal == null) {
            jSliderHorizontal = new JSlider();
            jSliderHorizontal.setToolTipText("Move subtitle horizontally");
        }
        return jSliderHorizontal;
    }

    void addHorizontalSliderChangeListener(ChangeListener changeListener) {
        jSliderHorizontal.addChangeListener(changeListener);
    }

    int getHorizontalSliderValue() {
        return jSliderHorizontal.getValue();
    }

    void setHorizontalSliderValue(int value) {
        jSliderHorizontal.setValue(value);
    }

    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setText("Cancel");
            jButtonCancel.setMnemonic('c');
            jButtonCancel.setToolTipText("Lose changes and return");
        }
        return jButtonCancel;
    }

    void addCancelButtonActionListener(ActionListener actionListener) {
        jButtonCancel.addActionListener(actionListener);
    }

    private JButton getJButtonOk() {
        if (jButtonOk == null) {
            jButtonOk = new JButton();
            jButtonOk.setText("  Ok  ");
            jButtonOk.setMnemonic('o');
            jButtonOk.setToolTipText("Save changes and return");
            jButtonOk.addAncestorListener(new RequestFocusListener());
        }
        return jButtonOk;
    }

    void addOkButtonActionListener(ActionListener actionListener) {
        jButtonOk.addActionListener(actionListener);
    }

    private JTextField getJTextFieldX() {
        if (jTextFieldX == null) {
            jTextFieldX = new JTextField();
            jTextFieldX.setPreferredSize(new Dimension(80, 20));
            jTextFieldX.setMinimumSize(new Dimension(80, 20));
            jTextFieldX.setToolTipText("Set X coordinate of upper left corner of subtitle");
        }
        return jTextFieldX;
    }

    void addXTextFieldActionListener(ActionListener actionListener) {
        jTextFieldX.addActionListener(actionListener);
    }

    void addXTextFieldDocumentListener(DocumentListener documentListener) {
        jTextFieldX.getDocument().addDocumentListener(documentListener);
    }

    String getXTextFieldText() {
        return jTextFieldX.getText();
    }

    void setXTextFieldText(String text) {
        jTextFieldX.setText(text);
    }

    void setXTextFieldBackground(Color color) {
        jTextFieldX.setBackground(color);
    }

    private JTextField getJTextFieldY() {
        if (jTextFieldY == null) {
            jTextFieldY = new JTextField();
            jTextFieldY.setPreferredSize(new Dimension(80, 20));
            jTextFieldY.setMinimumSize(new Dimension(80, 20));
            jTextFieldY.setToolTipText("Set Y coordinate of upper left corner of subtitle");
        }
        return jTextFieldY;
    }

    void addYTextFieldActionListener(ActionListener actionListener) {
        jTextFieldY.addActionListener(actionListener);
    }

    void addYTextFieldDocumentListener(DocumentListener documentListener) {
        jTextFieldY.getDocument().addDocumentListener(documentListener);
    }

    String getYTextFieldText() {
        return jTextFieldY.getText();
    }

    void setYTextFieldText(String text) {
        jTextFieldY.setText(text);
    }

    void setYTextFieldBackground(Color color) {
        jTextFieldY.setBackground(color);
    }

    private JButton getJButtonCenter() {
        if (jButtonCenter == null) {
            jButtonCenter = new JButton();
            jButtonCenter.setText("Center");
            jButtonCenter.setMnemonic('r');
            jButtonCenter.setToolTipText("Center subpicture horizontally");
        }
        return jButtonCenter;
    }

    void addCenterButtonActionListener(ActionListener actionListener) {
        jButtonCenter.addActionListener(actionListener);
    }

    private JTextField getJTextFieldStart() {
        if (jTextFieldStart == null) {
            jTextFieldStart = new JTextField();
            jTextFieldStart.setPreferredSize(new Dimension(80, 20));
            jTextFieldStart.setMinimumSize(new Dimension(80, 20));
            jTextFieldStart.setToolTipText("Set start time of subtitle");
        }
        return jTextFieldStart;
    }

    void addStartTextFieldActionListener(ActionListener actionListener) {
        jTextFieldStart.addActionListener(actionListener);
    }

    void addStartTextFieldDocumentListener(DocumentListener documentListener) {
        jTextFieldStart.getDocument().addDocumentListener(documentListener);
    }

    String getStartTextFieldText() {
        return jTextFieldStart.getText();
    }

    void setStartTextFieldText(String text) {
        jTextFieldStart.setText(text);
    }

    void setStartTextFieldBackground(Color color) {
        jTextFieldStart.setBackground(color);
    }

    private JTextField getJTextFieldEnd() {
        if (jTextFieldEnd == null) {
            jTextFieldEnd = new JTextField();
            jTextFieldEnd.setPreferredSize(new Dimension(80, 20));
            jTextFieldEnd.setMinimumSize(new Dimension(80, 20));
            jTextFieldEnd.setToolTipText("Set end time of subtitle");
        }
        return jTextFieldEnd;
    }

    void addEndTextFieldActionListener(ActionListener actionListener) {
        jTextFieldEnd.addActionListener(actionListener);
    }

    void addEndTextFieldDocumentListener(DocumentListener documentListener) {
        jTextFieldEnd.getDocument().addDocumentListener(documentListener);
    }

    String getEndTextFieldText() {
        return jTextFieldEnd.getText();
    }

    void setEndTextFieldText(String text) {
        jTextFieldEnd.setText(text);
    }

    void setEndTextFieldBackground(Color color) {
        jTextFieldEnd.setBackground(color);
    }

    private JTextField getJTextFieldDuration() {
        if (jTextFieldDuration == null) {
            jTextFieldDuration = new JTextField();
            jTextFieldDuration.setPreferredSize(new Dimension(80, 20));
            jTextFieldDuration.setMinimumSize(new Dimension(80, 20));
            jTextFieldDuration.setToolTipText("Set display duration of subtitle in milliseconds");
        }
        return jTextFieldDuration;
    }

    void addDurationTextFieldActionListener(ActionListener actionListener) {
        jTextFieldDuration.addActionListener(actionListener);
    }

    void addDurationTextFieldDocumentListener(DocumentListener documentListener) {
        jTextFieldDuration.getDocument().addDocumentListener(documentListener);
    }

    String getDurationTextFieldText() {
        return jTextFieldDuration.getText();
    }

    void setDurationTextFieldText(String text) {
        jTextFieldDuration.setText(text);
    }

    void setDurationTextFieldBackground(Color color) {
        jTextFieldDuration.setBackground(color);
    }

    private JButton getJButtonMin() {
        if (jButtonMin == null) {
            jButtonMin = new JButton();
            jButtonMin.setText("   Min   ");
            jButtonMin.setMnemonic('n');
            jButtonMin.setToolTipText("Set minimum duration");
        }
        return jButtonMin;
    }

    void addMinButtonActionListener(ActionListener actionListener) {
        jButtonMin.addActionListener(actionListener);
    }

    private JButton getJButtonMax() {
        if (jButtonMax == null) {
            jButtonMax = new JButton();
            jButtonMax.setText("   Max  ");
            jButtonMax.setMnemonic('m');
            jButtonMax.setToolTipText("Set maximum duration");
        }
        return jButtonMax;
    }

    void addMaxButtonActionListener(ActionListener actionListener) {
        jButtonMax.addActionListener(actionListener);
    }

    private JButton getJButtonTop() {
        if (jButtonTop == null) {
            jButtonTop = new JButton();
            jButtonTop.setText("   Top  ");
            jButtonTop.setMnemonic('t');
            jButtonTop.setToolTipText("Move to upper cinemascope bar");
        }
        return jButtonTop;
    }

    void addTopButtonActionListener(ActionListener actionListener) {
        jButtonTop.addActionListener(actionListener);
    }

    private JButton getJButtonBottom() {
        if (jButtonBottom == null) {
            jButtonBottom = new JButton();
            jButtonBottom.setText("Bottom");
            jButtonBottom.setMnemonic('b');
            jButtonBottom.setToolTipText("Move to lower cinemascope bar");
        }
        return jButtonBottom;
    }

    void addBottomButtonActionListener(ActionListener actionListener) {
        jButtonBottom.addActionListener(actionListener);
    }

    private JButton getJButtonStore() {
        if (jButtonStore == null) {
            jButtonStore = new JButton();
            jButtonStore.setText("Save Changes");
            jButtonStore.setMnemonic('s');
            jButtonStore.setEnabled(false);
            jButtonStore.setToolTipText("Save changes and continue editing");
        }
        return jButtonStore;
    }

    void addStoreButtonActionListener(ActionListener actionListener) {
        jButtonStore.addActionListener(actionListener);
    }

    void enableStoreButton(boolean enabled) {
        jButtonStore.setEnabled(enabled);
    }

    private JCheckBox getJCheckBoxForced() {
        if (jCheckBoxForced == null) {
            jCheckBoxForced = new JCheckBox();
            jCheckBoxForced.setText("Forced Caption");
            jCheckBoxForced.setMnemonic('f');
            jCheckBoxForced.setToolTipText("Force display of this subtitle");
        }
        return jCheckBoxForced;
    }

    void addForcedCheckBoxActionListener(ActionListener actionListener) {
        jCheckBoxForced.addActionListener(actionListener);
    }

    boolean isForcedCheckBoxSelected() {
        return jCheckBoxForced.isSelected();
    }

    private JCheckBox getJCheckBoxExclude() {
        if (jCheckBoxExclude == null) {
            jCheckBoxExclude = new JCheckBox();
            jCheckBoxExclude.setText("Exclude from export");
            jCheckBoxExclude.setMnemonic('x');
            jCheckBoxExclude.setToolTipText("Exclude this subtitle from export");
        }
        return jCheckBoxExclude;
    }

    void addExcludeCheckBoxActionListener(ActionListener actionListener) {
        jCheckBoxExclude.addActionListener(actionListener);
    }

    boolean isExcludeCheckBoxSelected() {
        return jCheckBoxExclude.isSelected();
    }

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

    private JButton getJButtonAddPatch() {
        if (jButtonAddPatch == null) {
            jButtonAddPatch = new JButton();
            jButtonAddPatch.setText("Erase");
            jButtonAddPatch.setMnemonic('e');
            jButtonAddPatch.setToolTipText("Add erase patch to make the selected area transparent");
            jButtonAddPatch.setEnabled(false);
        }
        return jButtonAddPatch;
    }

    void addAddPatchButtonActionListener(ActionListener actionListener) {
        jButtonAddPatch.addActionListener(actionListener);
    }

    void setAddPatchButtonEnabled(boolean enabled) {
        jButtonAddPatch.setEnabled(enabled);
    }

    private JButton getJButtonUndoPatch() {
        if (jButtonUndoPatch == null) {
            jButtonUndoPatch = new JButton();
            jButtonUndoPatch.setText("Undo Erase");
            jButtonUndoPatch.setMnemonic('u');
            jButtonUndoPatch.setToolTipText("Remove one erase patch from the stack (undo one delete step)");
            jButtonUndoPatch.setEnabled(false);
        }
        return jButtonUndoPatch;
    }

    void addUndoPatchButtonActionListener(ActionListener actionListener) {
        jButtonUndoPatch.addActionListener(actionListener);
    }

    void setUndoPatchButtonEnabled(boolean enabled) {
        jButtonUndoPatch.setEnabled(enabled);
    }

    private JButton getJButtonUndoAllPatches() {
        if (jButtonUndoAllPatches == null) {
            jButtonUndoAllPatches = new JButton();
            jButtonUndoAllPatches.setText("Undo All");
            jButtonUndoAllPatches.setMnemonic('a');
            jButtonUndoAllPatches.setEnabled(false);
            jButtonUndoAllPatches.setToolTipText("Remove all erase patches from the stack (undo all delete steps)");
        }
        return jButtonUndoAllPatches;
    }

    void addUndoAllPatchesButtonActionListener(ActionListener actionListener) {
        jButtonUndoAllPatches.addActionListener(actionListener);
    }

    void setUndoAllPatchesButtonEnabled(boolean enabled) {
        jButtonUndoAllPatches.setEnabled(enabled);
    }

    private JButton getJButtonStoreNext() {
        if (jButtonStoreNext == null) {
            jButtonStoreNext = new JButton();
            jButtonStoreNext.setText("<html><font color=\"red\"><b>&nbsp;&gt;&nbsp;</b></font></html>");
            jButtonStoreNext.setToolTipText("Store changes and skip to next frame");
        }
        return jButtonStoreNext;
    }

    void addStoreNextButtonActionListener(ActionListener actionListener) {
        jButtonStoreNext.addActionListener(actionListener);
    }

    private JButton getJButtonStorePrev() {
        if (jButtonStorePrev == null) {
            jButtonStorePrev = new JButton();
            jButtonStorePrev.setText("<html><font color=\"red\"><b>&nbsp;&lt;&nbsp;</b></font></html>");
            jButtonStorePrev.setToolTipText("Store changes and skip to previous frame");
        }
        return jButtonStorePrev;
    }

    void addStorePrevButtonActionListener(ActionListener actionListener) {
        jButtonStorePrev.addActionListener(actionListener);
    }

    void error(String message) {
        logger.error(message);
        JOptionPane.showMessageDialog(this, message, "Error!", JOptionPane.WARNING_MESSAGE);
    }

    public void setIndex(int idx) {
        model.setReady(false);
        model.setIndex(idx); //TODO: use the observer pattern to run this method when index changes in the model
        // get prev and next
        model.setSubPicPrev(idx > 0 ? Core.getSubPictureTrg(idx-1) : null);
        model.setSubPicNext(idx < Core.getNumFrames()-1 ? Core.getSubPictureTrg(idx+1) : null);

        // update components
        try {
            Core.convertSup(idx, idx + 1, Core.getNumFrames());
            model.setSubPic(Core.getSubPictureTrg(idx).copy());
            SubPicture subPic = model.getSubPic();
            model.setImage(Core.getTrgImagePatched(subPic));

            if (subPic.getErasePatch() != null && subPic.getErasePatch().size() > 0) {
                jButtonUndoPatch.setEnabled(true);
                jButtonUndoAllPatches.setEnabled(true);
            }

            model.setEnableSliders(false);
            jSliderHorizontal.setMaximum(subPic.getWidth());
            jSliderHorizontal.setValue(subPic.getXOffset());
            jSliderVertical.setMaximum(subPic.getHeight());
            jSliderVertical.setValue(subPic.getHeight() - subPic.getYOffset());
            model.setEnableSliders(true);

            jLabelInfo.setText("Frame " + (idx+1) + " of " + Core.getNumFrames());
            jTextFieldStart.setText(ptsToTimeStr(subPic.getStartTime()));
            jTextFieldEnd.setText(ptsToTimeStr(subPic.getEndTime()));
            jTextFieldDuration.setText(ToolBox.formatDouble((subPic.getEndTime() - subPic.getStartTime()) / 90.0));

            jTextFieldX.setText(String.valueOf(subPic.getXOffset()));
            jTextFieldY.setText(String.valueOf(subPic.getYOffset()));

            jPanelPreview.setSubtitleOffsets(subPic.getXOffset(), subPic.getYOffset());
            jPanelPreview.setScreenDimension(subPic.getWidth(), subPic.getHeight());
            jPanelPreview.setCropOffsetY(model.getCropOffsetY());
            jPanelPreview.setImage(model.getImage(), subPic.getImageWidth(), subPic.getImageHeight());
            jPanelPreview.repaint();
            jPanelPreview.setExcluded(subPic.isExcluded());

            jCheckBoxExclude.setSelected(subPic.isExcluded());
            jCheckBoxForced.setSelected(subPic.isForced());

            model.setReady(true);

        } catch (CoreException ex) {
            error(ex.getMessage());
        } catch (Exception ex) {
            ToolBox.showException(ex);
            Core.exit();
            System.exit(4);
        }
    }
}
