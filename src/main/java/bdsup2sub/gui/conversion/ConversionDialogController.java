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
package bdsup2sub.gui.conversion;

import bdsup2sub.core.ForcedFlagState;
import bdsup2sub.core.Resolution;
import bdsup2sub.utils.SubtitleUtils;
import bdsup2sub.utils.ToolBox;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

import static bdsup2sub.core.Configuration.*;
import static bdsup2sub.core.Configuration.MAX_FREE_SCALE_FACTOR;
import static bdsup2sub.core.Configuration.MIN_FREE_SCALE_FACTOR;

class ConversionDialogController {

    private final ConversionDialogModel model;
    private final ConversionDialogView view;

    public ConversionDialogController(ConversionDialogModel model, ConversionDialogView view) {
        this.model = model;
        this.view = view;

        view.addWindowListener(new ConversionDialogListener());
        view.addResolutionComboBoxItemListener(new ResolutionComboBoxItemListener());
        view.addFrameRateCheckBoxActionListener(new FrameRateCheckBoxActionListener());
        view.addResolutionCheckBoxActionListener(new ResolutionCheckBoxActionListener());
        view.addMoveCheckBoxActionListener(new MoveCheckBoxActionListener());
        view.addFpsSrcComboBoxActionListener(new FpsSrcComboBoxActionListener());
        view.addFpsSrcComboBoxDocumentListener(new FpsSrcComboBoxDocumentListener());
        view.addFpsTrgComboBoxActionListener(new FpsTrgComboBoxActionListener());
        view.addFpsTrgComboBoxDocumentListener(new FpsTrgComboBoxDocumentListener());
        view.addDelayTextFieldActionListener(new DelayTextFieldActionListener());
        view.addDelayTextFieldDocumentListener(new DelayTextFieldDocumentListener());

        view.addCancelButtonActionListener(new CancelButtonActionListener());
        view.addStoreButtonActionListener(new StoreButtonActionListener());
        view.addRestoreButtonActionListener(new RestoreButtonActionListener());
        view.addResetButtonActionListener(new ResetButtonActionListener());

        view.addScaleCheckBoxItemListener(new ScaleCheckBoxItemListener());
        view.addFixMinTimeCheckBoxItemListener(new FixMinTimeCheckBoxItemListener());

        view.addMinTimeTextFieldActionListener(new MinTimeTextFieldActionListener());
        view.addMinTimeTextFieldDocumentListener(new MinTimeTextFieldDocumentListener());

        view.addOkButtonActionListener(new OkButtonActionListener());

        view.addScaleXTextFieldActionListener(new ScaleXTextFieldActionListener());
        view.addScaleXTextFieldDocumentListener(new ScaleXTextFieldDocumentListener());
        view.addScaleYTextFieldActionListener(new ScaleYTextFieldActionListener());
        view.addScaleYTextFieldDocumentListener(new ScaleYTextFieldDocumentListener());

        view.addForcedComboBoxItemListener(new ForcedComboBoxItemListener());
    }

    private class ConversionDialogListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent event) {
            model.setCancel(true);
            view.dispose();
        }
    }

    private class ResolutionComboBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (model.isReady()) {
                int idx = view.getResolutionComboBoxSelectedIndex();
                for (Resolution resolution : Resolution.values()) {
                    if (idx == resolution.ordinal()) {
                        model.setOutputResolution(resolution);
                        if (!model.isKeepFps()) {
                            model.setFpsTrg(SubtitleUtils.getDefaultFramerateForResolution(resolution));
                        }
                        view.setFpsTrgComboBoxSelectedItem(ToolBox.formatDouble(model.getFpsTrg()));
                        break;
                    }
                }
            }
        }
    }

    private class FrameRateCheckBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                boolean changeFPS = view.isFrameRateCheckBoxSelected();
                model.setConvertFPS(changeFPS);
                view.setFpsSrcComboBoxEnabled(changeFPS);
            }
        }
    }

    private class ResolutionCheckBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                boolean changeResolution = view.isResolutionCheckBoxSelected();
                model.setConvertResolution(changeResolution);
                view.setResolutionComboBoxEnabled(changeResolution);
            }
        }
    }

    private class MoveCheckBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                model.setMoveCaptions(view.isMoveCheckBoxSelected());
            }
        }
    }

    private class FpsSrcComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                double fpsSrc = SubtitleUtils.getFps(view.getFpsSrcComboBoxSelectedItem());
                if (fpsSrc > 0) {
                    model.setFpsSrc(fpsSrc);
                }
                view.setFpsSrcComboBoxSelectedItem(ToolBox.formatDouble(model.getFpsSrc()));
                view.setFpsSrcComboBoxBackground(OK_BACKGROUND);
                model.setFpsSrcCertain(false);
            }
        }
    }

    private class FpsSrcComboBoxDocumentListener implements DocumentListener {
        private void check() {
            if (model.isReady()) {
                double fpsSrc = SubtitleUtils.getFps(view.getFpsSrcComboBoxText());
                Color color;
                if (fpsSrc > 0) {
                    color = OK_BACKGROUND;
                    model.setFpsSrc(fpsSrc);
                } else {
                    color = ERROR_BACKGROUND;
                }
                view.setFpsSrcComboBoxBackground(color);
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
    }

    private class FpsTrgComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                double d = SubtitleUtils.getFps(view.getFpsTrgComboBoxSelectedItem());
                if (d > 0) {
                    model.setFpsTrg(d);
                }
                view.setFpsTrgComboBoxSelectedItem(ToolBox.formatDouble(model.getFpsTrg()));
                view.setFpsTrgComboBoxBackground(OK_BACKGROUND);

                model.setDelayPTS((int)SubtitleUtils.syncTimePTS(model.getDelayPTS(), model.getFpsTrg(), model.getFpsTrgConf()));
                view.setDelayTextFieldText(ToolBox.formatDouble(model.getDelayPTS() / 90.0));

                model.setMinTimePTS((int)SubtitleUtils.syncTimePTS(model.getMinTimePTS(), model.getFpsTrg(), model.getFpsTrgConf()));
                view.setMinTimeTextFieldText(ToolBox.formatDouble(model.getMinTimePTS() / 90.0));
            }
        }
    }

    private class FpsTrgComboBoxDocumentListener implements DocumentListener {
        private void check() {
            if (model.isReady()) {
                double fpsTrg = SubtitleUtils.getFps(view.getFpsTrgComboBoxText());
                Color color;
                if (fpsTrg > 0) {
                    if ((int)SubtitleUtils.syncTimePTS(model.getDelayPTS(), model.getFpsTrg(), model.getFpsTrgConf()) != model.getDelayPTS() || model.getMinTimePTS() != (int)SubtitleUtils.syncTimePTS(model.getMinTimePTS(), model.getFpsTrg(), model.getFpsTrgConf())) {
                        color = WARN_BACKGROUND;
                    } else {
                        color = OK_BACKGROUND;
                    }
                    model.setFpsTrg(fpsTrg);
                } else {
                    color = ERROR_BACKGROUND;
                }
                view.setFpsTrgComboBoxBackground(color);
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
    }

    private class DelayTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                String s = view.getDelayTextFieldText();
                try {
                    // don't use getDouble as the value can be negative
                    model.setDelayPTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(s) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                } catch (NumberFormatException ex) {
                }
                view.setDelayTextFieldBoxBackground(OK_BACKGROUND);
                view.setDelayTextFieldText(ToolBox.formatDouble(model.getDelayPTS() / 90.0));
            }
        }
    }

    private class DelayTextFieldDocumentListener implements DocumentListener {
        private void check() {
            if (model.isReady()) {
                String delayTextFieldText = view.getDelayTextFieldText();
                try {
                    // don't use getDouble as the value can be negative
                    model.setDelayPTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(delayTextFieldText) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                    if (!delayTextFieldText.equalsIgnoreCase(ToolBox.formatDouble(model.getDelayPTS() / 90.0))) {
                        view.setDelayTextFieldBoxBackground(WARN_BACKGROUND);
                    } else {
                        view.setDelayTextFieldBoxBackground(OK_BACKGROUND);
                    }
                } catch (NumberFormatException ex) {
                    view.setDelayTextFieldBoxBackground(ERROR_BACKGROUND);
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
    }

    private class CancelButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.setCancel(true);
            view.dispose();
        }
    }

    private class StoreButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                // fps source
                model.storeConvertFPS();
                if (model.getConvertFPS()) {
                    double fpsSrc  = SubtitleUtils.getFps(view.getFpsSrcComboBoxSelectedItem());
                    if (fpsSrc > 0) {
                        model.setFpsSrc(fpsSrc);
                        model.storeFPSSrc();
                    }
                }
                // fps target
                double fpsTrg = SubtitleUtils.getFps(view.getFpsTrgComboBoxSelectedItem());
                if (fpsTrg > 0) {
                    model.setFpsTrg(fpsTrg);
                    model.storeFpsTrg();
                }
                // delay
                try {
                    model.setDelayPTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(view.getDelayTextFieldText()) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                    model.storeDelayPTS();
                } catch (NumberFormatException ex) {
                }
                // min time
                model.storeFixShortFrames();
                try {
                    model.setMinTimePTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(view.getMinTimeTextFieldText()) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                    model.storeMinTimePTS();
                } catch (NumberFormatException ex) {
                }
                // exit
                model.storeConvertResolution();
                if (model.getConvertResolution()) {
                    model.storeOutputResolution();
                }
                // scaleX
                double scaleX = ToolBox.getDouble(view.getScaleXTextFieldText());
                if (scaleX > 0) {
                    if (scaleX > MAX_FREE_SCALE_FACTOR) {
                        scaleX = MAX_FREE_SCALE_FACTOR;
                    } else if (scaleX < MIN_FREE_SCALE_FACTOR) {
                        scaleX = MIN_FREE_SCALE_FACTOR;
                    }
                    model.setFreeScaleFactorX(scaleX);
                }
                // scaleY
                double scaleY = ToolBox.getDouble(view.getScaleYTextFieldText());
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

                // forced flag
                model.storeForcedState();

                model.storeConfig();
            }
        }
    }

    private class RestoreButtonActionListener implements ActionListener {
        @Override
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
            view.fillDialog();
        }
    }

    private class ResetButtonActionListener implements ActionListener {
        @Override
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
            model.setForcedState(DEFAULT_FORCED_FLAG_STATE);
            view.fillDialog();
        }
    }

    private class ScaleCheckBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (model.isReady()) {
                boolean changeScale = view.isScaleCheckBoxSelected();
                model.setApplyFreeScale(changeScale);
                view.enableScaleXTextField(changeScale);
                view.enableScaleYTextField(changeScale);
            }
        }
    }

    private class FixMinTimeCheckBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (model.isReady()) {
                boolean fixShortFrames = view.isFixMinTimeCheckBoxSelected();
                model.setFixShortFrames(fixShortFrames);
                view.enableMinTimeTextField(fixShortFrames);
            }
        }
    }

    private class MinTimeTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                try {
                    model.setMinTimePTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(view.getMinTimeTextFieldText()) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                } catch (NumberFormatException ex) {
                }
                view.setMinTimeTextFieldBackground(OK_BACKGROUND);
                view.setMinTimeTextFieldText(ToolBox.formatDouble(model.getMinTimePTS() / 90.0));
            }
        }
    }

    private class MinTimeTextFieldDocumentListener implements DocumentListener {
        private void check() {
            if (model.isReady()) {
                String minTimeTextFieldText = view.getMinTimeTextFieldText();
                try {
                    model.setMinTimePTS((int)SubtitleUtils.syncTimePTS((long)(Double.parseDouble(minTimeTextFieldText) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                    if (!minTimeTextFieldText.equalsIgnoreCase(ToolBox.formatDouble(model.getMinTimePTS() / 90.0))) {
                        view.setMinTimeTextFieldBackground(WARN_BACKGROUND);
                    } else {
                        view.setMinTimeTextFieldBackground(OK_BACKGROUND);
                    }
                } catch (NumberFormatException ex) {
                    view.setMinTimeTextFieldBackground(ERROR_BACKGROUND);
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
    }

    private class OkButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                // fps source
                model.setConvertFPSConf(model.getConvertFPS());
                if (model.getConvertFPS()) {
                    double fpsSrc = SubtitleUtils.getFps(view.getFpsSrcComboBoxSelectedItem());
                    if (fpsSrc > 0) {
                        model.setFpsSrc(fpsSrc);
                        model.setFPSSrcConf(fpsSrc);
                    }
                }
                // fps target
                double fpsTrg = SubtitleUtils.getFps(view.getFpsTrgComboBoxSelectedItem());
                if (fpsTrg > 0) {
                    model.setFpsTrg(fpsTrg);
                    model.setFpsTrgConf(fpsTrg);
                }
                // delay
                try {
                    model.setDelayPTS((int) SubtitleUtils.syncTimePTS((long) (Double.parseDouble(view.getDelayTextFieldText()) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
                    model.setDelayPTSConf(model.getDelayPTS());
                } catch (NumberFormatException ex) {
                }
                // min time
                model.setFixShortFramesConf(model.getFixShortFrames());
                try {
                    model.setMinTimePTS((int) SubtitleUtils.syncTimePTS((long) (Double.parseDouble(view.getMinTimeTextFieldText()) * 90), model.getFpsTrg(), model.getFpsTrgConf()));
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
                double scaleX = ToolBox.getDouble(view.getScaleXTextFieldText());
                if (scaleX > 0) {
                    if (scaleX > MAX_FREE_SCALE_FACTOR) {
                        scaleX = MAX_FREE_SCALE_FACTOR;
                    } else if (scaleX < MIN_FREE_SCALE_FACTOR) {
                        scaleX = MIN_FREE_SCALE_FACTOR;
                    }
                    model.setFreeScaleFactorX(scaleX);
                }
                // scaleY
                double scaleY = ToolBox.getDouble(view.getScaleYTextFieldText());
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
                model.setForcedStateConf(model.getForcedState());
                // keep move settings
                if (view.isMoveCheckBoxEnabled()) {
                    model.storeMoveCaptions();
                }
                view.dispose();
            }
        }
    }

    private class ScaleXTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                double scaleX = ToolBox.getDouble(view.getScaleXTextFieldText());
                if (scaleX >0) {
                    if (scaleX > MAX_FREE_SCALE_FACTOR) {
                        scaleX = MAX_FREE_SCALE_FACTOR;
                    } else if (scaleX < MIN_FREE_SCALE_FACTOR) {
                        scaleX = MIN_FREE_SCALE_FACTOR;
                    }
                    model.setFreeScaleFactorX(scaleX);
                }
                view.setScaleXTextFieldText(ToolBox.formatDouble(model.getFreeScaleFactorX()));
                view.setScaleXTextFieldBackground(OK_BACKGROUND);
            }
        }
    }

    private class ScaleXTextFieldDocumentListener implements DocumentListener {
        private void check() {
            if (model.isReady()) {
                double scaleX = ToolBox.getDouble(view.getScaleXTextFieldText());
                if (scaleX >= MIN_FREE_SCALE_FACTOR && scaleX <= MAX_FREE_SCALE_FACTOR) {
                    model.setFreeScaleFactorX(scaleX);
                    view.setScaleXTextFieldBackground(OK_BACKGROUND);
                } else {
                    view.setScaleXTextFieldBackground(ERROR_BACKGROUND);
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
    }

    private class ScaleYTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isReady()) {
                double scaleY = ToolBox.getDouble(view.getScaleYTextFieldText());
                if (scaleY > 0) {
                    if (scaleY > MAX_FREE_SCALE_FACTOR) {
                        scaleY = MAX_FREE_SCALE_FACTOR;
                    } else if (scaleY < MIN_FREE_SCALE_FACTOR) {
                        scaleY = MIN_FREE_SCALE_FACTOR;
                    }
                    model.setFreeScaleFactorY(scaleY);
                }
                view.setScaleYTextFieldText(ToolBox.formatDouble(model.getFreeScaleFactorY()));
            }
        }
    }

    private class ScaleYTextFieldDocumentListener implements DocumentListener {
        private void check() {
            if (model.isReady()) {
                double scaleY = ToolBox.getDouble(view.getScaleYTextFieldText());
                if (scaleY >= MIN_FREE_SCALE_FACTOR && scaleY <= MAX_FREE_SCALE_FACTOR) {
                    model.setFreeScaleFactorY(scaleY);
                    view.setScaleYTextFieldBackground(OK_BACKGROUND);
                } else {
                    view.setScaleYTextFieldBackground(ERROR_BACKGROUND);
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
    }

    private class ForcedComboBoxItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (model.isReady()) {
                int idx = view.getForcedComboBoxSelectedIndex();
                for (ForcedFlagState forcedFlagState : ForcedFlagState.values()) {
                    if (idx == forcedFlagState.ordinal()) {
                        model.setForcedState(forcedFlagState);
                        break;
                    }
                }
            }
        }
    }
}
