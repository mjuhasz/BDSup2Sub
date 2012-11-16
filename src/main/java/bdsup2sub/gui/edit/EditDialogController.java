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

import bdsup2sub.bitmap.ErasePatch;
import bdsup2sub.core.Core;
import bdsup2sub.supstream.SubPicture;
import bdsup2sub.utils.SubtitleUtils;
import bdsup2sub.utils.ToolBox;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import static bdsup2sub.core.Configuration.*;
import static bdsup2sub.gui.support.EditPane.*;
import static bdsup2sub.utils.TimeUtils.ptsToTimeStr;
import static bdsup2sub.utils.TimeUtils.timeStrToPTS;

public class EditDialogController {

    private final EditDialogModel model;
    private final EditDialogView view;

    public EditDialogController(EditDialogModel model, EditDialogView view) {
        this.model = model;
        this.view = view;

        addListenersToView(view);
    }

    private void addListenersToView(EditDialogView view) {
        view.addWindowListener(new EditDialogWindowListener());
        view.addPreviewPanelSelectListener(new PreviewPanelSelectListener());

        view.addPrevButtonActionListener(new PrevButtonActionListener());
        view.addNextButtonActionListener(new NextButtonActionListener());
        view.addVerticalSliderChangeListener(new VerticalSliderChangeListener());
        view.addHorizontalSliderChangeListener(new HorizontalSliderChangeListener());
        view.addCancelButtonActionListener(new CancelButtonActionListener());
        view.addOkButtonActionListener(new OkButtonActionListener());
        view.addXTextFieldActionListener(new XTextFieldActionListener());
        view.addXTextFieldDocumentListener(new XTextFieldDocumentListener());
        view.addYTextFieldActionListener(new YTextFieldActionListener());
        view.addYTextFieldDocumentListener(new YTextFieldDocumentListener());
        view.addCenterButtonActionListener(new CenterButtonActionListener());
        view.addStartTextFieldActionListener(new StartTextFieldActionListener());
        view.addStartTextFieldDocumentListener(new StartTextFieldDocumentListener());
        view.addEndTextFieldActionListener(new EndTextFieldActionListener());
        view.addEndTextFieldDocumentListener(new EndTextFieldDocumentListener());
        view.addDurationTextFieldActionListener(new DurationTextFieldActionListener());
        view.addDurationTextFieldDocumentListener(new DurationTextFieldDocumentListener());
        view.addMinButtonActionListener(new MinButtonActionListener());
        view.addMaxButtonActionListener(new MaxButtonActionListener());
        view.addTopButtonActionListener(new TopButtonActionListener());
        view.addBottomButtonActionListener(new BottomButtonActionListener());
        view.addStoreButtonActionListener(new StoreButtonActionListener());
        view.addForcedCheckBoxActionListener(new ForcedCheckBoxActionListener());
        view.addExcludeCheckBoxActionListener(new ExcludeCheckBoxActionListener());
        view.addAddPatchButtonActionListener(new AddPatchButtonActionListener());
        view.addUndoPatchButtonActionListener(new UndoPatchButtonActionListener());
        view.addUndoAllPatchesButtonActionListener(new UndoAllPatchesButtonActionListener());
        view.addStoreNextButtonActionListener(new StoreNextButtonActionListener());
        view.addStorePrevButtonActionListener(new StorePrevButtonActionListener());
    }

    private void setEdited(boolean edited) {
        model.setEdited(edited);
        view.enableStoreButton(edited);
    }

    private void store() {
        SubPicture subPic = model.getSubPic();
        SubPicture s = Core.getSubPictureTrg(model.getIndex());
        s.setEndTime(subPic.getEndTime());
        s.setStartTime(subPic.getStartTime());
        s.setOfsX(subPic.getXOffset());
        s.setOfsY(subPic.getYOffset());
        s.setForced(subPic.isForced());
        s.setExcluded(subPic.isExcluded());
        s.setErasePatch(subPic.getErasePatch());
    }

    private class EditDialogWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent event) {
            view.dispose();
        }
    }

    private class PrevButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.getIndex() > 0) {
                view.setIndex(model.getIndex() - 1);
                setEdited(false);
            }
        }
    }

    private class NextButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.getIndex() < Core.getNumFrames() - 1) {
                view.setIndex(model.getIndex() + 1);
                setEdited(false);
            }
        }
    }

    private class VerticalSliderChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent event) {
            if (model.isEnableSliders()) {
                SubPicture subPic = model.getSubPic();
                int y = subPic.getHeight() - view.getVerticalSliderValue();

                if (y < model.getCropOffsetY()) {
                    y = model.getCropOffsetY();
                } else if (y > subPic.getHeight() - subPic.getImageHeight() - model.getCropOffsetY()) {
                    y = subPic.getHeight() - subPic.getImageHeight() - model.getCropOffsetY();
                }

                if (y != subPic.getYOffset()) {
                    subPic.setOfsY(y);
                    view.setYTextFieldText(String.valueOf(subPic.getYOffset()));
                    view.setPreviewPanelOffsets(subPic.getXOffset(), subPic.getYOffset());
                    view.setPreviewPanelAspectRatio(21.0 / 9);
                    view.repaintPreviewPanel();
                    setEdited(true);
                }
            }
        }
    }
    private class HorizontalSliderChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent event) {
            if (model.isEnableSliders()) {
                SubPicture subPic = model.getSubPic();
                int x = view.getHorizontalSliderValue();

                if (x < 0) {
                    x = 0;
                } else if (x > subPic.getWidth() - subPic.getImageWidth()) {
                    x = subPic.getWidth() - subPic.getImageWidth();
                }

                if (x != subPic.getXOffset()) {
                    subPic.setOfsX(x);
                    view.setXTextFieldText(String.valueOf(subPic.getXOffset()));
                    view.setPreviewPanelOffsets(subPic.getXOffset(), subPic.getYOffset());
                    view.repaintPreviewPanel();
                    setEdited(true);
                }
            }
        }
    }

    private class CancelButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            view.dispose();
        }
    }

    private class OkButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isEdited()) {
                store();
            }
            view.dispose();
        }
    }

    private class XTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isReady()) {
                SubPicture subPic = model.getSubPic();
                int x = ToolBox.getInt(view.getXTextFieldText());
                if (x == -1) {
                    x = subPic.getXOffset(); // invalid value -> keep old one
                } else if (x < 0) {
                    x = 0;
                } else if (x > subPic.getWidth() - subPic.getImageWidth()) {
                    x = subPic.getWidth() - subPic.getImageWidth();
                }

                if (x != subPic.getXOffset() ) {
                    model.setEnableSliders(false);
                    subPic.setOfsX(x);
                    view.setHorizontalSliderValue(subPic.getXOffset());
                    view.setPreviewPanelOffsets(subPic.getXOffset(), subPic.getYOffset());
                    view.repaintPreviewPanel();
                    setEdited(true);
                    model.setEnableSliders(true);
                }
                view.setXTextFieldText(String.valueOf(subPic.getXOffset()));
                view.setXTextFieldBackground(OK_BACKGROUND);
            }
        }
    }

    private class XTextFieldDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            check();
        }

        private void check() {
            if (model.isReady()) {
                SubPicture subPic = model.getSubPic();
                int x = ToolBox.getInt(view.getXTextFieldText());
                if (x < 0 || x > subPic.getWidth() - subPic.getImageWidth()) {
                    view.setXTextFieldBackground(ERROR_BACKGROUND);
                } else {
                    if (x != subPic.getXOffset() ) {
                        model.setEnableSliders(false);
                        subPic.setOfsX(x);
                        view.setHorizontalSliderValue(subPic.getXOffset());
                        view.setPreviewPanelOffsets(subPic.getXOffset(), subPic.getYOffset());
                        view.repaintPreviewPanel();
                        setEdited(true);
                        model.setEnableSliders(true);
                    }
                    view.setXTextFieldBackground(OK_BACKGROUND);
                }
            }
        }
    }

    private class YTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SubPicture subPic = model.getSubPic();
            int y = ToolBox.getInt(view.getYTextFieldText());
            if (y == -1) {
                y = subPic.getYOffset(); // invalid value -> keep old one
            } else if (y < model.getCropOffsetY()) {
                y = model.getCropOffsetY();
            } else if (y > subPic.getHeight() - subPic.getImageHeight() - model.getCropOffsetY()) {
                y = subPic.getHeight() - subPic.getImageHeight() - model.getCropOffsetY();
            }
            if (y != subPic.getYOffset()) {
                model.setEnableSliders(false);
                subPic.setOfsY(y);
                view.setVerticalSliderValue(subPic.getHeight() - subPic.getYOffset());
                view.setPreviewPanelOffsets(subPic.getXOffset(), subPic.getYOffset());
                view.repaintPreviewPanel();
                setEdited(true);
                model.setEnableSliders(true);
            }
            view.setYTextFieldText(String.valueOf(subPic.getYOffset()));
            view.setYTextFieldBackground(OK_BACKGROUND);
        }
    }

    private class YTextFieldDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            check();
        }

        private void check() {
            if (model.isReady()) {
                SubPicture subPic = model.getSubPic();
                int y = ToolBox.getInt(view.getYTextFieldText());
                if (y < model.getCropOffsetY() || y > subPic.getHeight() - subPic.getImageHeight() - model.getCropOffsetY()) {
                    view.setYTextFieldBackground(ERROR_BACKGROUND);
                } else {
                    if (y != subPic.getYOffset()) {
                        model.setEnableSliders(false);
                        subPic.setOfsY(y);
                        view.setVerticalSliderValue(subPic.getHeight() - subPic.getYOffset());
                        view.setPreviewPanelOffsets(subPic.getXOffset(), subPic.getYOffset());
                        view.repaintPreviewPanel();
                        setEdited(true);
                        model.setEnableSliders(true);
                    }
                    view.setYTextFieldBackground(OK_BACKGROUND);
                }
            }
        }
    }

    private class CenterButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SubPicture subPic = model.getSubPic();
            subPic.setOfsX((subPic.getWidth() -subPic.getImageWidth())/2);
            model.setEnableSliders(false);
            view.setHorizontalSliderValue(subPic.getXOffset());
            view.setPreviewPanelOffsets(subPic.getXOffset(), subPic.getYOffset());
            view.repaintPreviewPanel();
            view.setXTextFieldText(String.valueOf(subPic.getXOffset()));
            setEdited(true);
            model.setEnableSliders(true);
        }
    }

    private class StartTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isReady()) {
                model.setReady(false);
                SubPicture subPic = model.getSubPic();
                long t = SubtitleUtils.syncTimePTS(timeStrToPTS(view.getStartTextFieldText()), model.getFPSTrg(), model.getFPSTrg());
                if (t >= subPic.getEndTime()) {
                    t = subPic.getEndTime() -model.getFrameTime();
                }
                SubPicture subPicPrev = model.getSubPicPrev();
                if (subPicPrev != null && subPicPrev.getEndTime() > t) {
                    t = subPicPrev.getEndTime() +model.getFrameTime();
                }
                if (t >= 0) {
                    subPic.setStartTime(SubtitleUtils.syncTimePTS(t, model.getFPSTrg(), model.getFPSTrg()));
                    view.setDurationTextFieldText(ToolBox.formatDouble((subPic.getEndTime() - subPic.getStartTime()) / 90.0));
                    setEdited(true);
                }
                view.setStartTextFieldText(ptsToTimeStr(subPic.getStartTime()));
                view.setStartTextFieldBackground(OK_BACKGROUND);
                model.setReady(true);
            }
        }
    }

    private class StartTextFieldDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            check();
        }

        private void check() {
            if (model.isReady()) {
                model.setReady(false);
                SubPicture subPic = model.getSubPic();
                long t = SubtitleUtils.syncTimePTS(timeStrToPTS(view.getStartTextFieldText()), model.getFPSTrg(), model.getFPSTrg());
                if (t < 0 || t >= subPic.getEndTime() || model.getSubPicPrev() != null && model.getSubPicPrev().getEndTime() > t) {
                    view.setStartTextFieldBackground(ERROR_BACKGROUND);
                } else {
                    subPic.setStartTime(t);
                    view.setDurationTextFieldText(ToolBox.formatDouble((subPic.getEndTime() - subPic.getStartTime()) / 90.0));
                    if (!view.getStartTextFieldText().equalsIgnoreCase(ptsToTimeStr(subPic.getStartTime()))) {
                        view.setStartTextFieldBackground(WARN_BACKGROUND);
                    } else {
                        view.setStartTextFieldBackground(OK_BACKGROUND);
                    }
                    setEdited(true);
                }
                model.setReady(true);
            }
        }
    }

    private class EndTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isReady()) {
                model.setReady(false);
                SubPicture subPic = model.getSubPic();
                long t = SubtitleUtils.syncTimePTS(timeStrToPTS(view.getEndTextFieldText()), model.getFPSTrg(), model.getFPSTrg());
                if (t <= subPic.getStartTime()) {
                    t = subPic.getStartTime() + model.getFrameTime();
                }

                SubPicture subPicNext = model.getSubPicNext();
                if (subPicNext != null && subPicNext.getStartTime() < t) {
                    t = subPicNext.getStartTime();
                }
                if (t >= 0) {
                    subPic.setEndTime(SubtitleUtils.syncTimePTS(t, model.getFPSTrg(), model.getFPSTrg()));
                    view.setDurationTextFieldText(ToolBox.formatDouble((subPic.getEndTime() - subPic.getStartTime()) / 90.0));
                    setEdited(true);
                }
                view.setEndTextFieldText(ptsToTimeStr(subPic.getEndTime()));
                view.setEndTextFieldBackground(OK_BACKGROUND);
                model.setReady(true);
            }
        }
    }

    private class EndTextFieldDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            check();
        }

        private void check() {
            if (model.isReady()) {
                model.setReady(false);
                SubPicture subPic = model.getSubPic();
                long t = SubtitleUtils.syncTimePTS(timeStrToPTS(view.getEndTextFieldText()), model.getFPSTrg(), model.getFPSTrg());
                if (t < 0 || t <= subPic.getStartTime() || model.getSubPicNext() != null && model.getSubPicNext().getStartTime() < t) {
                    view.setEndTextFieldBackground(ERROR_BACKGROUND);
                } else {
                    subPic.setEndTime(t);
                    view.setDurationTextFieldText(ToolBox.formatDouble((subPic.getEndTime() - subPic.getStartTime()) / 90.0));
                    if (!view.getEndTextFieldText().equalsIgnoreCase(ptsToTimeStr(subPic.getEndTime()))) {
                        view.setEndTextFieldBackground(WARN_BACKGROUND);
                    } else {
                        view.setEndTextFieldBackground(OK_BACKGROUND);
                    }
                    setEdited(true);
                }
                model.setReady(true);
            }
        }
    }

    private class DurationTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
        if (model.isReady()) {
            model.setReady(false);
            SubPicture subPic = model.getSubPic();
            long t = (long)(ToolBox.getDouble(view.getDurationTextFieldText()) * 90);
            if (t >= 0 && t < model.getFrameTime()) {
                t = model.getFrameTime();
            }
            if (t > 0) {
                t += subPic.getStartTime();
                SubPicture subPicNext = model.getSubPicNext();
                if (subPicNext != null && subPicNext.getStartTime() < t) {
                    t = subPicNext.getStartTime();
                }
                subPic.setEndTime(SubtitleUtils.syncTimePTS(t, model.getFPSTrg(), model.getFPSTrg()));
                view.setEndTextFieldText(ptsToTimeStr(subPic.getEndTime()));
                setEdited(true);
            }
            view.setDurationTextFieldText(ToolBox.formatDouble((subPic.getEndTime() - subPic.getStartTime()) / 90.0));
            view.setDurationTextFieldBackground(OK_BACKGROUND);
            model.setReady(true);
            }
        }
    }

    private class DurationTextFieldDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            check();
        }

        private void check() {
            if (model.isReady()) {
                model.setReady(false);
                long t = (long)(ToolBox.getDouble(view.getDurationTextFieldText()) * 90);
                if (t < model.getFrameTime()) {
                    view.setDurationTextFieldBackground(ERROR_BACKGROUND);
                } else {
                    SubPicture subPic = model.getSubPic();
                    t += subPic.getStartTime();
                    SubPicture subPicNext = model.getSubPicNext();
                    if (subPicNext != null && subPicNext.getStartTime() < t) {
                        t = subPicNext.getStartTime();
                    }
                    subPic.setEndTime(SubtitleUtils.syncTimePTS(t, model.getFPSTrg(), model.getFPSTrg()));
                    view.setEndTextFieldText(ptsToTimeStr(subPic.getEndTime()));
                    setEdited(true);
                    if (!view.getDurationTextFieldText().equalsIgnoreCase(ToolBox.formatDouble((subPic.getEndTime() - subPic.getStartTime()) / 90.0))) {
                        view.setDurationTextFieldBackground(WARN_BACKGROUND);
                    } else {
                        view.setDurationTextFieldBackground(OK_BACKGROUND);
                    }
                    setEdited(true);
                }
                model.setReady(true);
            }
        }
    }

    private class MinButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SubPicture subPic = model.getSubPic();
            long t = model.getMinTimePTS();
            if (t >= 0) {
                t += subPic.getStartTime();
                SubPicture subPicNext = model.getSubPicNext();
                if (subPicNext != null && subPicNext.getStartTime() < t) {
                    t = subPicNext.getStartTime();
                }
                subPic.setEndTime(SubtitleUtils.syncTimePTS(t, model.getFPSTrg(), model.getFPSTrg()));
                view.setEndTextFieldText(ptsToTimeStr(subPic.getEndTime()));
                setEdited(true);
            }
            view.setDurationTextFieldText(ToolBox.formatDouble((subPic.getEndTime() - subPic.getStartTime()) / 90.0));
        }
    }

    private class MaxButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SubPicture subPic = model.getSubPic();
            SubPicture subPicNext = model.getSubPicNext();
            long t;
            if (subPicNext != null) {
                t = subPicNext.getStartTime();
            } else {
                t = subPic.getEndTime() + 10000 * 90; // 10 seconds
            }
            subPic.setEndTime(SubtitleUtils.syncTimePTS(t, model.getFPSTrg(), model.getFPSTrg()));
            view.setEndTextFieldText(ptsToTimeStr(subPic.getEndTime()));
            view.setDurationTextFieldText(ToolBox.formatDouble((subPic.getEndTime() - subPic.getStartTime()) / 90.0));
            setEdited(true);
        }
    }

    private class TopButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SubPicture subPic = model.getSubPic();
            int cineH = subPic.getHeight() *5/42;
            int y = cineH-subPic.getImageHeight();
            if (y < 10) {
                y = 10;
            }
            if (y < model.getCropOffsetY()) {
                y = model.getCropOffsetY();
            }
            model.setEnableSliders(false);
            subPic.setOfsY(y);
            view.setVerticalSliderValue(subPic.getHeight() - subPic.getYOffset());
            view.setPreviewPanelOffsets(subPic.getXOffset(), subPic.getYOffset());
            view.repaintPreviewPanel();
            view.setYTextFieldText(String.valueOf(subPic.getYOffset()));
            setEdited(true);
            model.setEnableSliders(true);
        }
    }

    private class BottomButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SubPicture subPic = model.getSubPic();
            int cineH = subPic.getHeight() *5/42;
            int y = subPic.getHeight() -cineH;
            if (y+subPic.getImageHeight() > subPic.getHeight() - model.getCropOffsetY()) {
                y = subPic.getHeight() - subPic.getImageHeight() - 10;
            }
            model.setEnableSliders(false);
            subPic.setOfsY(y);
            view.setVerticalSliderValue(subPic.getHeight() - subPic.getYOffset());
            view.setPreviewPanelOffsets(subPic.getXOffset(), subPic.getYOffset());
            view.repaintPreviewPanel();
            view.setYTextFieldText(String.valueOf(subPic.getYOffset()));
            setEdited(true);
            model.setEnableSliders(true);
        }
    }

    private class StoreButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            store();
            setEdited(false);
        }
    }

    private class ForcedCheckBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            model.getSubPic().setForced(view.isForcedCheckBoxSelected());
            setEdited(true);
        }
    }

    private class ExcludeCheckBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SubPicture subPic = model.getSubPic();
            subPic.setExcluded(view.isExcludeCheckBoxSelected());
            view.setPreviewPanelExcluded(subPic.isExcluded());
            view.repaintPreviewPanel();
            setEdited(true);
        }
    }
    
    private class AddPatchButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SubPicture subPic = model.getSubPic();
            int sel[] = view.getPreviewPanelSelection();
            if (sel != null) {
                ErasePatch ep = new ErasePatch(sel[0], sel[1], sel[2]-sel[0]+1, sel[3]-sel[1]+1);
                subPic.getErasePatch().add(ep);

                view.setUndoPatchButtonEnabled(true);
                view.setUndoAllPatchesButtonEnabled(true);

                model.setImage(Core.getTrgImagePatched(subPic));
                view.setPreviewPanelImage(model.getImage(), subPic.getImageWidth(), subPic.getImageHeight());

                setEdited(true);
            }
            view.setAddPatchButtonEnabled(false);
            view.removePreviewPanelSelection();
            view.repaintPreviewPanel();
        }
    }

    private class UndoPatchButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SubPicture subPic = model.getSubPic();
            if (!subPic.getErasePatch().isEmpty()) {
                subPic.getErasePatch().remove(subPic.getErasePatch().size() - 1);
                if (subPic.getErasePatch().isEmpty()) {
                    view.setUndoPatchButtonEnabled(false);
                    view.setUndoAllPatchesButtonEnabled(false);
                }
                model.setImage(Core.getTrgImagePatched(subPic));
                view.setPreviewPanelImage(model.getImage(), subPic.getImageWidth(), subPic.getImageHeight());
                view.repaintPreviewPanel();
                setEdited(true);
            }
        }
    }

    private class UndoAllPatchesButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SubPicture subPic = model.getSubPic();
            if (!subPic.getErasePatch().isEmpty()) {
                subPic.getErasePatch().clear();
                model.setImage(Core.getTrgImagePatched(subPic));
                view.setPreviewPanelImage(model.getImage(), subPic.getImageWidth(), subPic.getImageHeight());
                view.repaintPreviewPanel();
                setEdited(true);
            }
            view.setUndoPatchButtonEnabled(false);
            view.setUndoAllPatchesButtonEnabled(false);
        }
    }

    private class StoreNextButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isEdited()) {
                store();
            }
            if (model.getIndex() < Core.getNumFrames()-1) {
                view.setIndex(model.getIndex() + 1);
                setEdited(false);
            }
        }
    }

    private class StorePrevButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isEdited()) {
                store();
            }
            if (model.getIndex() > 0) {
                view.setIndex(model.getIndex() - 1);
                setEdited(false);
            }
        }
    }

    private class PreviewPanelSelectListener implements SelectListener {
        @Override
        public void selectionPerformed(boolean validSelection) {
            view.setAddPatchButtonEnabled(validSelection);
        }
    }
}
