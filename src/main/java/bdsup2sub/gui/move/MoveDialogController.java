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
package bdsup2sub.gui.move;

import bdsup2sub.core.CaptionMoveModeX;
import bdsup2sub.core.CaptionMoveModeY;
import bdsup2sub.core.Core;
import bdsup2sub.core.CoreException;
import bdsup2sub.supstream.SubPicture;
import bdsup2sub.utils.ToolBox;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static bdsup2sub.core.Configuration.ERROR_BACKGROUND;
import static bdsup2sub.core.Configuration.OK_BACKGROUND;

class MoveDialogController {
    private static final double SCREEN_ASPECT_RATIO = 16.0/9;

    private final MoveDialogModel model;
    private final MoveDialogView view;

    public MoveDialogController(MoveDialogModel model, MoveDialogView view) {
        this.model = model;
        this.view = view;

        view.addWindowListener(new MoveDialogListener());

        view.addPrevButtonActionListener(new PrevButtonActionListener());
        view.addNextButtonActionListener(new NextButtonActionListener());
        view.addCancelButtonActionListener(new CancelButtonActionListener());
        view.addOkButtonActionListener(new OkButtonActionListener());

        view.addRatioTextFieldActionListener(new RatioTextFieldActionListener());
        view.addRatioTextFieldDocumentListener(new RatioTextFieldDocumentListener());
        view.addOffsetYTextFieldActionListener(new OffsetYTextFieldActionListener());
        view.addOffsetYTextFieldDocumentListener(new OffsetYTextFieldDocumentListener());

        view.add_21_9_ButtonActionListener(new RatioButtonActionListener(21.0 / 9));
        view.add_240_1_ButtonActionListener(new RatioButtonActionListener(2.4));
        view.add_235_1_ButtonActionListener(new RatioButtonActionListener(2.35));

        view.addInsideRadioButtonActionListener(new InsideRadioButtonActionListener());
        view.addOutsideRadioButtonActionListener(new OutsideRadioButtonActionListener());
        view.addKeepYRadioButtonActionListener(new KeepYRadioButtonActionListener());

        view.addCropOfsYTextFieldActionListener(new CropOfsYTextFieldActionListener());
        view.addCropOfsYTextFieldDocumentListener(new CropOfsYTextFieldDocumentListener());

        view.addCropBarsButtonActionListener(new CropBarsButtonActionListener());

        view.addKeepXRadioButtonActionListener(new KeepXRadioButtonActionListener());
        view.addLeftRadioButtonActionListener(new LeftRadioButtonActionListener());
        view.addRightRadioButtonActionListener(new RightRadioButtonActionListener());
        view.addCenterRadioButtonActionListener(new CenterRadioButtonActionListener());

        view.addOffsetXTextFieldActionListener(new OffsetXTextFieldActionListener());
        view.addOffsetXTextFieldDocumentListener(new OffsetXTextFieldDocumentListener());
    }

    private class MoveDialogListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent event) {
            view.dispose();
        }
    }

    private class PrevButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.getCurrentSubtitleIndex() > 0) {
                setCurrentSubtitleIndex(model.getCurrentSubtitleIndex() - 1);
            }
        }
    }

    private class NextButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.getCurrentSubtitleIndex() < Core.getNumFrames()-1) {
                setCurrentSubtitleIndex(model.getCurrentSubtitleIndex() + 1);
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
            model.storeCropOfsY();
            model.storeMoveModeX();
            model.storeMoveModeY();
            model.storeMoveOffsetX();
            model.storeMoveOffsetY();
            model.storeCinemascopeBarFactor();
            // moving is done in MainFrame
            view.dispose();
        }
    }

    private class RatioTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isReady()) {
                double targetScreenAspectRatio = ToolBox.getDouble(view.getRatioTextFieldText());
                if (targetScreenAspectRatio == -1.0 ) {
                    targetScreenAspectRatio = model.getTargetScreenAspectRatio(); // invalid number -> keep old value
                } else if (targetScreenAspectRatio > 4.0) {
                    targetScreenAspectRatio = 4.0;
                } else if (targetScreenAspectRatio < SCREEN_ASPECT_RATIO) {
                    targetScreenAspectRatio = SCREEN_ASPECT_RATIO;
                }
                if (targetScreenAspectRatio != model.getTargetScreenAspectRatio()) {
                    model.setTargetScreenAspectRatio(targetScreenAspectRatio);
                    setRatio(targetScreenAspectRatio);
                }
                view.setRatioTextFieldText(ToolBox.formatDouble(targetScreenAspectRatio));
            }
        }
    }

    private class RatioTextFieldDocumentListener implements DocumentListener {
        private void check() {
            if (model.isReady()) {
                double targetScreenAspectRatio = ToolBox.getDouble(view.getRatioTextFieldText());
                if (targetScreenAspectRatio < SCREEN_ASPECT_RATIO || targetScreenAspectRatio > 4.0 ) {
                    view.setRatioTextFieldBackground(ERROR_BACKGROUND);
                } else {
                    if (!ToolBox.formatDouble(targetScreenAspectRatio).equalsIgnoreCase(ToolBox.formatDouble(model.getTargetScreenAspectRatio()))) {
                        model.setTargetScreenAspectRatio(targetScreenAspectRatio);
                        setRatio(targetScreenAspectRatio);
                    }
                    view.setRatioTextFieldBackground(OK_BACKGROUND);
                }
            }
        }

        @Override
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

    private class OffsetYTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isReady()) {
                int y = ToolBox.getInt(view.getOffsetYTextFieldText());

                if (y == -1) {
                    y = model.getOffsetY();  // invalid number -> keep old value
                } else if (y < 0) {
                    y = 0;
                } else if (y > model.getSubPic().getHeight() /3) {
                    y = model.getSubPic().getHeight() /3;
                }

                if ( y != model.getOffsetY() ) {
                    model.setOffsetY(y);
                    setRatio(model.getTargetScreenAspectRatio());
                }
                view.setOffsetYTextFieldText(String.valueOf(model.getOffsetY()));
            }
        }
    }

    private class OffsetYTextFieldDocumentListener implements DocumentListener {
        private void check() {
            if (model.isReady()) {
                int y = ToolBox.getInt(view.getOffsetYTextFieldText());

                if ( y < 0 || y > model.getSubPic().getHeight() /3 ) {
                    view.setOffsetYTextFieldBackground(ERROR_BACKGROUND);
                } else {
                    if (y != model.getOffsetY()) {
                        model.setOffsetY(y);
                        setRatio(model.getTargetScreenAspectRatio());
                    }
                    view.setOffsetYTextFieldBackground(OK_BACKGROUND);
                }
            }
        }

        @Override
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

    private void setRatio(double targetScreenAspectRatio) {
        if (!ToolBox.formatDouble(model.getTargetScreenAspectRatio()).equalsIgnoreCase(ToolBox.formatDouble(targetScreenAspectRatio))) {
            view.setRatioTextFieldText(ToolBox.formatDouble(targetScreenAspectRatio));
        }
        model.setTargetScreenAspectRatio(targetScreenAspectRatio);
        model.setCinemascopeBarFactor((1.0 - SCREEN_ASPECT_RATIO / targetScreenAspectRatio) / 2.0);
        move();
        view.setPreviewPanelAspectRatio(targetScreenAspectRatio);
        view.setPreviewPanelSubtitleOffsets(model.getSubPic().getXOffset(), model.getSubPic().getYOffset());
        view.repaintPreviewPanel();
    }

    private void move() {
        Core.moveToBounds(model.getSubPic(), model.getCurrentSubtitleIndex() + 1, model.getCinemascopeBarFactor(), model.getOffsetX(), model.getOffsetY(), model.getMoveModeX(), model.getMoveModeY(), model.getCropOfsY());
    }

    void setCurrentSubtitleIndex(int idx) {
        model.setReady(false);
        model.setCurrentSubtitleIndex(idx);
        // update components
        try {
            Core.convertSup(idx, idx+1, Core.getNumFrames());
            SubPicture subPic = new SubPicture(Core.getSubPictureTrg(idx));
            model.setSubPic(subPic);
            model.setImage(Core.getTrgImagePatched(subPic));

            model.setOriginalX(subPic.getXOffset());
            model.setOriginalY(subPic.getYOffset());

            view.setInfoLabelText("Frame " + (idx + 1) + " of " + Core.getNumFrames());
            move();
            view.setPreviewPanelSubtitleOffsets(subPic.getXOffset(), subPic.getYOffset());
            view.setPreviewPanelScreenDimension(subPic.getWidth(), subPic.getHeight());
            view.setPreviewPanelImage(model.getImage(), subPic.getImageWidth(), subPic.getImageHeight());
            view.setPreviewPanelAspectRatio(model.getTargetScreenAspectRatio());
            view.setPreviewPanelCropOffsetY(model.getCropOfsY());
            view.setPreviewPanelExcluded(subPic.isExcluded());
            view.repaintPreviewPanel();
            model.setReady(true);

        } catch (CoreException ex) {
            view.error(ex.getMessage());
        } catch (Exception ex) {
            ToolBox.showException(ex);
            Core.exit();
            System.exit(4);
        }
    }

    private class RatioButtonActionListener implements ActionListener {
        private final double ratio;

        private RatioButtonActionListener(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            setRatio(ratio);
        }
    }

    private class InsideRadioButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            model.setMoveModeY(CaptionMoveModeY.MOVE_INSIDE_BOUNDS);
            setRatio(model.getTargetScreenAspectRatio());
        }
    }

    private class OutsideRadioButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            model.setMoveModeY(CaptionMoveModeY.MOVE_OUTSIDE_BOUNDS);
            setRatio(model.getTargetScreenAspectRatio());
        }
    }

    private class KeepYRadioButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            model.setMoveModeY(CaptionMoveModeY.KEEP_POSITION);
            model.getSubPic().setOfsY(model.getOriginalY());
            setRatio(model.getTargetScreenAspectRatio());
        }
    }

    private class CropOfsYTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isReady()) {
                int cropOffsetY = ToolBox.getInt(view.getCropOfsYTextFieldText());

                if (cropOffsetY == -1) {
                    cropOffsetY = model.getCropOfsY();   // invalid number -> keep old value
                } else if (cropOffsetY < 0) {
                    cropOffsetY = 0;
                } else if (cropOffsetY > model.getSubPic().getHeight() /3) {
                    cropOffsetY = model.getSubPic().getHeight() /3;
                }

                if (cropOffsetY != model.getCropOfsY()) {
                    model.setCropOfsY(cropOffsetY);
                    view.setPreviewPanelCropOfsY(cropOffsetY);
                    setRatio(model.getTargetScreenAspectRatio());
                }
                view.setCropOfsYTextFieldText(String.valueOf(cropOffsetY));
            }
        }
    }

    private class CropOfsYTextFieldDocumentListener implements DocumentListener {
        private void check() {
            if (model.isReady()) {
                int cropOffsetY = ToolBox.getInt(view.getCropOfsYTextFieldText());

                if (cropOffsetY < 0 || cropOffsetY > model.getSubPic().getHeight() /3) {
                    view.setCropOfsYTextFieldBackground(ERROR_BACKGROUND);
                } else {
                    if (cropOffsetY != model.getCropOfsY()) {
                        model.setCropOfsY(cropOffsetY);
                        view.setPreviewPanelCropOfsY(cropOffsetY);
                        setRatio(model.getTargetScreenAspectRatio());
                    }
                    view.setCropOfsYTextFieldBackground(OK_BACKGROUND);
                }
            }
        }

        @Override
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

    private class CropBarsButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            int cropOffsetY = (int) (model.getSubPic().getHeight() * model.getCinemascopeBarFactor() + 0.5);
            model.setCropOfsY(cropOffsetY); // height of one cinemascope bar in pixels
            view.setPreviewPanelCropOfsY(cropOffsetY);
            setRatio(model.getTargetScreenAspectRatio());
            view.setCropOfsYTextFieldText(String.valueOf(cropOffsetY));
        }
    }

    private class KeepXRadioButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            model.setMoveModeX(CaptionMoveModeX.KEEP_POSITION);
            model.getSubPic().setOfsX(model.getOriginalX());
            setRatio(model.getTargetScreenAspectRatio());
        }
    }

    private class LeftRadioButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            model.setMoveModeX(CaptionMoveModeX.LEFT);
            setRatio(model.getTargetScreenAspectRatio());
        }
    }

    private class RightRadioButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            model.setMoveModeX(CaptionMoveModeX.RIGHT);
            setRatio(model.getTargetScreenAspectRatio());
        }
    }

    private class CenterRadioButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            model.setMoveModeX(CaptionMoveModeX.CENTER);
            setRatio(model.getTargetScreenAspectRatio());
        }
    }

    private class OffsetXTextFieldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (model.isReady()) {
                int offsetX = ToolBox.getInt(view.getOffsetXTextFieldText());

                if (offsetX == -1) {
                    offsetX = model.getOffsetX();  // invalid number -> keep old value
                } else if (offsetX < 0) {
                    offsetX = 0;
                } else if (offsetX > model.getSubPic().getWidth() / 3) {
                    offsetX = model.getSubPic().getWidth() /3;
                }

                if ( offsetX != model.getOffsetX() ) {
                    model.setOffsetX(offsetX);
                    setRatio(model.getTargetScreenAspectRatio());
                }
                view.setOffsetXTextFieldText(String.valueOf(offsetX));
            }
        }
    }

    private class OffsetXTextFieldDocumentListener implements DocumentListener {
        private void check() {
            if (model.isReady()) {
                int offsetX = ToolBox.getInt(view.getOffsetXTextFieldText());

                if ( offsetX < 0 || offsetX > model.getSubPic().getWidth() /3 ) {
                    view.setOffsetXTextFieldBackground(ERROR_BACKGROUND);
                } else {
                    if (offsetX != model.getOffsetX()) {
                        model.setOffsetX(offsetX);
                        setRatio(model.getTargetScreenAspectRatio());
                    }
                    view.setOffsetXTextFieldBackground(OK_BACKGROUND);
                }
            }
        }

        @Override
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
}
