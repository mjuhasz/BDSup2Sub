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
package bdsup2sub.gui.palette;

import bdsup2sub.core.Core;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;

class FramePaletteDialogController {

    private final FramePaletteDialogModel model;
    private final FramePaletteDialogView view;

    public FramePaletteDialogController(FramePaletteDialogModel model, FramePaletteDialogView view) {
        this.model = model;
        this.view = view;

        addColorComboBoxActionListeners(view);
        addAlphaComboBoxActionListeners(view);

        addButtonActionListeners(view);
    }

    private void addColorComboBoxActionListeners(FramePaletteDialogView view) {
        view.addColor1ComboBoxActionListener(new Color1ComboBoxActionListener());
        view.addColor2ComboBoxActionListener(new Color2ComboBoxActionListener());
        view.addColor3ComboBoxActionListener(new Color3ComboBoxActionListener());
        view.addColor4ComboBoxActionListener(new Color4ComboBoxActionListener());
    }

    private void addAlphaComboBoxActionListeners(FramePaletteDialogView view) {
        view.addAlpha1ComboBoxActionListener(new Alpha1ComboBoxActionListener());
        view.addAlpha2ComboBoxActionListener(new Alpha2ComboBoxActionListener());
        view.addAlpha3ComboBoxActionListener(new Alpha3ComboBoxActionListener());
        view.addAlpha4ComboBoxActionListener(new Alpha4ComboBoxActionListener());
    }

    private void addButtonActionListeners(FramePaletteDialogView view) {
        view.addCancelButtonActionListener(new CancelButtonActionListener());
        view.addOkButtonActionListener(new OkButtonActionListener());
        view.addSetAllButtonActionListener(new SetAllButtonActionListener());
        view.addResetAllButtonActionListener(new ResetAllButtonActionListener());
        view.addResetButtonActionListener(new ResetButtonActionListener());
    }

    private class Color1ComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateArrayElementWithSelectedItem((JComboBox) event.getSource(), model.getPalette(), 0);
        }
    }

    private class Color2ComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateArrayElementWithSelectedItem((JComboBox) event.getSource(), model.getPalette(), 1);
        }
    }

    private class Color3ComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateArrayElementWithSelectedItem((JComboBox) event.getSource(), model.getPalette(), 2);
        }
    }

    private class Color4ComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateArrayElementWithSelectedItem((JComboBox) event.getSource(), model.getPalette(), 3);
        }
    }

    private class Alpha1ComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateArrayElementWithSelectedItem((JComboBox) event.getSource(), model.getAlpha(), 0);
        }
    }

    private class Alpha2ComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateArrayElementWithSelectedItem((JComboBox) event.getSource(), model.getAlpha(), 1);
        }
    }

    private class Alpha3ComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateArrayElementWithSelectedItem((JComboBox) event.getSource(), model.getAlpha(), 2);
        }
    }

    private class Alpha4ComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            updateArrayElementWithSelectedItem((JComboBox) event.getSource(), model.getAlpha(), 3);
        }
    }

    private void updateArrayElementWithSelectedItem(JComboBox comboBox, int[] array, int index) {
        if (model.isReady()) {
            int idx = ToolBox.getInt(comboBox.getSelectedItem().toString());
            if (idx >= 0 && idx < 16) {
                array[index] = idx;
            }
        }
    }

    private class CancelButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.dispose();
        }
    }
    private class OkButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            setPaletteAndAlphaValues(model.getCurrentSubtitleIndex());
            view.dispose();
        }
    }

    private class SetAllButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int j=0; j < Core.getNumFrames(); j++) {
                setPaletteAndAlphaValues(j);
            }
            view.dispose();
        }
    }

    private void setPaletteAndAlphaValues(int subtitleIndex) {
        int alpha[] = Core.getFrameAlpha(subtitleIndex);
        int palette[] = Core.getFramePal(subtitleIndex);

        if (alpha != null) {
            System.arraycopy(model.getAlpha(), 0, alpha, 0, alpha.length);
        }
        if (palette != null) {
            System.arraycopy(model.getPalette(), 0, palette, 0, palette.length);
        }
    }

    private class ResetAllButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int j=0; j < Core.getNumFrames(); j++) {
                resetPaletteAndAlphaValues(j);
            }
            view.dispose();
        }
    }

    private class ResetButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            resetPaletteAndAlphaValues(model.getCurrentSubtitleIndex());
        }
    }

    private void resetPaletteAndAlphaValues(int subtitleIndex) {
        int originalAlpha[] = Core.getOriginalFrameAlpha(subtitleIndex);
        int originalPalette[] = Core.getOriginalFramePal(subtitleIndex);
        int alpha[] = Core.getFrameAlpha(subtitleIndex);
        int palette[] = Core.getFramePal(subtitleIndex);

        if ((alpha != null) && (originalAlpha != null)) {
            System.arraycopy(originalAlpha, 0, alpha, 0, alpha.length);
        }
        if ((palette != null) && (originalPalette != null)) {
            System.arraycopy(originalPalette, 0, palette, 0, palette.length);
        }

        setCurrentSubtitleIndex(subtitleIndex);
    }

    void setCurrentSubtitleIndex(int idx) {
        model.setCurrentSubtitleIndex(idx);

        model.setReady(false);

        copyAlphaAndPaletteValuesFromCurrentSubtitle();
        view.updateComboBoxSelections();

        model.setReady(true);
    }

    private void copyAlphaAndPaletteValuesFromCurrentSubtitle() {
        model.setAlpha(new int[4]);
        model.setPalette(new int[4]);
        int alpha[] = Core.getFrameAlpha(model.getCurrentSubtitleIndex());
        int palette[] = Core.getFramePal(model.getCurrentSubtitleIndex());
        if (alpha != null) {
            System.arraycopy(alpha, 0, model.getAlpha(), 0, alpha.length);
        }
        if (palette != null) {
            System.arraycopy(palette, 0, model.getPalette(), 0, palette.length);
        }
    }
}
