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
package bdsup2sub.gui;

import bdsup2sub.core.*;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static bdsup2sub.core.Constants.APP_NAME_AND_VERSION;
import static bdsup2sub.core.Constants.AUTHOR_AND_DATE;

public class MainFrameView extends JFrame implements ClipboardOwner {

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane;
    private JPanel jPanelTop;
    private JPanel jPanelInfoSource;
    private JLabel jLabelInfoSource;
    private JScrollPane jScrollPaneSource;
    private GfxPane jPanelSource;
    private JPanel jPanelInfoTarget;
    private JLabel jLabelInfoTarget;
    private JScrollPane jScrollPaneTarget;
    private GfxPane jPanelTarget;
    private JPanel jPanelBottom;
    private EditPane jLayoutPane;
    private JScrollPane jScrollPaneConsole;
    private JTextArea console;

    private JMenuBar jMenuBar;
    private JMenu jMenuFile;
    private JMenuItem jMenuItemLoad;
    private JMenu jMenuRecentFiles;
    private JMenuItem jMenuItemSave;
    private JMenuItem jMenuItemClose;
    private JMenuItem jMenuItemQuit;
    private JMenu jMenuEdit;
    private JMenuItem jMenuItemEditFrame;
    private JMenuItem jMenuItemEditDefaultDvdPalette;
    private JMenuItem jMenuItemEditImportedDvdPalette;
    private JMenuItem jMenuItemEditDvdFramePalette;
    private JMenuItem jMenuItemMoveAll;
    private JMenuItem jMenuItemResetCropOffset;
    private JMenu jMenuSettings;
    private JMenuItem jMenuItemConversionSettings;
    private JCheckBoxMenuItem jMenuItemSwapCrCb;
    private JCheckBoxMenuItem jMenuItemFixInvisibleFrames;
    private JCheckBoxMenuItem jMenuItemVerbatimOutput;
    private JMenu jMenuHelp;
    private JMenuItem jMenuItemHelp;

    private JComboBox jComboBoxSubNum;
    private JComboBox jComboBoxAlphaThreshold;
    private JComboBox jComboBoxMedLowThreshold;
    private JComboBox jComboBoxHiMedThreshold;
    private JComboBox jComboBoxOutputFormat;
    private JComboBox jComboBoxPalette;
    private JComboBox jComboBoxFilter;
    private JTextField jTextSubNum = new JTextField();
    private JTextField jTextAlphaThreshold = new JTextField();
    private JTextField jTextMedLowThreshold = new JTextField();
    private JTextField jTextHiMedThreshold = new JTextField();

    private JPopupMenu jPopupMenu;
    private JMenuItem jPopupMenuItemCopy;
    private JMenuItem jPopupMenuItemClear;


    /** semaphore for synchronization of threads */
    final Object threadSemaphore = new Object();

    private ActionListener recentFilesMenuActionListener;
    
    private MainFrameModel model;
    
    public MainFrameView(MainFrameModel model) {
        super(APP_NAME_AND_VERSION);
        this.model = model;

        init();

        Core.setMainFrame(this);

        // read properties, set window size and position
        int w = Core.props.get("frameWidth", 800);
        int h = Core.props.get("frameHeight", 600);
        this.setSize(w,h);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        
        Point p = ge.getCenterPoint();
        p.x = Core.props.get("framePosX", -1);
        p.y = Core.props.get("framePosY", -1);
        if ((p.x != -1) && (p.y != -1)) {
            setLocation(p);
        } else {
            setLocationRelativeTo(null);
        }

        updateRecentFilesMenu();

        // fill comboboxes
        jComboBoxSubNum.setEditor(new MyComboBoxEditor(jTextSubNum));
        jComboBoxAlphaThreshold.setEditor(new MyComboBoxEditor(jTextAlphaThreshold));
        jComboBoxHiMedThreshold.setEditor(new MyComboBoxEditor(jTextHiMedThreshold));
        jComboBoxMedLowThreshold.setEditor(new MyComboBoxEditor(jTextMedLowThreshold));

        for (int i=0; i < 256; i++) {
            jComboBoxAlphaThreshold.addItem(i);
            jComboBoxHiMedThreshold.addItem(i);
            jComboBoxMedLowThreshold.addItem(i);
        }
        jComboBoxAlphaThreshold.setSelectedIndex(Core.getAlphaThr());
        jComboBoxHiMedThreshold.setSelectedIndex(Core.getLumThr()[0]);
        jComboBoxMedLowThreshold.setSelectedIndex(Core.getLumThr()[1]);

        for (OutputMode m : OutputMode.values()) {
            jComboBoxOutputFormat.addItem(m.toString());
        }
        jComboBoxOutputFormat.setSelectedIndex(Core.getOutputMode().ordinal());

        for (PaletteMode m : PaletteMode.values()) {
            jComboBoxPalette.addItem(m.toString());
        }
        jComboBoxPalette.setSelectedIndex(Core.getPaletteMode().ordinal());

        for (ScalingFilter s : ScalingFilter.values()) {
            jComboBoxFilter.addItem(s.toString());
        }
        jComboBoxFilter.setSelectedIndex(Core.getScalingFilter().ordinal());

        jMenuItemVerbatimOutput.setSelected(Core.getVerbatim());
        jMenuItemFixInvisibleFrames.setSelected(Core.getFixZeroAlpha());

        printToConsole(APP_NAME_AND_VERSION + " - a converter from Blu-Ray/HD-DVD SUP to DVD SUB/IDX and more\n");
        printToConsole(AUTHOR_AND_DATE + "\n");
        printToConsole("Official thread at Doom9: http://forum.doom9.org/showthread.php?t=145277\n\n");
        flushConsole();
    }

    private void init() {
        setSize(800, 600);
        setMinimumSize(new Dimension(700, 300));
        setJMenuBar(getjMenuBar());
        setContentPane(getJContentPane());
        getJPopupMenu();
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon_32.png")));
    }

    private JMenuBar getjMenuBar() {
        if (jMenuBar == null) {
            jMenuBar = new JMenuBar();
            jMenuBar.add(getJMenuFile());
            jMenuBar.add(getJMenuEdit());
            jMenuBar.add(getJMenuPrefs());
            jMenuBar.add(getJMenuHelp());
        }
        return jMenuBar;
    }

    private JMenu getJMenuFile() {
        if (jMenuFile == null) {
            jMenuFile = new JMenu();
            jMenuFile.setName("");
            jMenuFile.setMnemonic('f');
            jMenuFile.setText("File");
            jMenuFile.add(getJMenuItemLoad());
            jMenuFile.add(getJMenuItemRecentFiles());
            jMenuFile.add(getJMenuItemSave());
            jMenuFile.add(getJMenuItemClose());
            jMenuFile.add(getJMenuItemQuit());
        }
        return jMenuFile;
    }

    private JMenuItem getJMenuItemLoad() {
        if (jMenuItemLoad == null) {
            jMenuItemLoad = new JMenuItem();
            jMenuItemLoad.setText("Load");
            jMenuItemLoad.setMnemonic('l');
        }
        return jMenuItemLoad;
    }

    void addLoadMenuItemActionListener(ActionListener actionListener) {
        jMenuItemLoad.addActionListener(actionListener);
    }

    void setLoadMenuItemEnabled(boolean enable) {
        jMenuItemLoad.setEnabled(enable);
    }

    private JMenu getJMenuItemRecentFiles() {
        if (jMenuRecentFiles == null) {
            jMenuRecentFiles = new JMenu();
            jMenuRecentFiles.setText("Recent Files");
            jMenuRecentFiles.setMnemonic('r');
        }
        return jMenuRecentFiles;
    }


    void addRecentFilesMenuItemActionListener(ActionListener actionListener) {
        recentFilesMenuActionListener = actionListener;
        for(int i=0; i < jMenuRecentFiles.getItemCount(); i++) {
            jMenuRecentFiles.getItem(i).addActionListener(recentFilesMenuActionListener);
        }
    }

    void updateRecentFilesMenu() {
        jMenuRecentFiles.setEnabled(false);
        ArrayList<String> recentFiles = Core.getRecentFiles();
        int size = recentFiles.size();
        if (size>0) {
            jMenuRecentFiles.removeAll();
            for (int i=0; i < size; i++) {
                JMenuItem j = new JMenuItem();
                String s = recentFiles.get(i);
                j.setText(i + ": " + s);
                j.setActionCommand(s);
                j.setMnemonic(Character.forDigit(i, 10));
                j.addActionListener(recentFilesMenuActionListener);
                jMenuRecentFiles.add(j);
            }
            jMenuRecentFiles.setEnabled(true);
        }
    }

    private JMenuItem getJMenuItemSave() {
        if (jMenuItemSave == null) {
            jMenuItemSave = new JMenuItem();
            jMenuItemSave.setText("Save/Export");
            jMenuItemSave.setMnemonic('s');
            jMenuItemSave.setEnabled(false);
        }
        return jMenuItemSave;
    }

    void addSaveMenuItemActionListener(ActionListener actionListener) {
        jMenuItemSave.addActionListener(actionListener);
    }

    private JMenuItem getJMenuItemClose() {
        if (jMenuItemClose == null) {
            jMenuItemClose = new JMenuItem();
            jMenuItemClose.setText("Close");
            jMenuItemClose.setEnabled(false);
            jMenuItemClose.setMnemonic('c');
        }
        return jMenuItemClose;
    }

    void addCloseMenuItemActionListener(ActionListener actionListener) {
        jMenuItemClose.addActionListener(actionListener);
    }

    private JMenuItem getJMenuItemQuit() {
        if (jMenuItemQuit == null) {
            jMenuItemQuit = new JMenuItem();
            jMenuItemQuit.setText("Quit");
            jMenuItemQuit.setMnemonic('q');
        }
        return jMenuItemQuit;
    }

    void addQuitMenuItemActionListener(ActionListener actionListener) {
        jMenuItemQuit.addActionListener(actionListener);
    }

    void setQuitMenuItemEnabled(boolean enable) {
        jMenuItemQuit.setEnabled(enable);
    }

    private JMenu getJMenuEdit() {
        if (jMenuEdit == null) {
            jMenuEdit = new JMenu();
            jMenuEdit.setText("Edit");
            jMenuEdit.setMnemonic('e');
            jMenuEdit.add(getJMenuItemEditFrame());
            jMenuEdit.add(getJMenuItemEditDefaultDvdPalette());
            jMenuEdit.add(getJMenuItemEditImportedDvdPalette());
            jMenuEdit.add(getJMenuItemEditDvdFramePalette());
            jMenuEdit.add(getJMenuItemMoveAll());
            jMenuEdit.add(getJMenuItemResetCropOffset());
        }
        return jMenuEdit;
    }

    private JMenuItem getJMenuItemEditFrame() {
        if (jMenuItemEditFrame == null) {
            jMenuItemEditFrame = new JMenuItem();
            jMenuItemEditFrame.setText("Edit Frame");
            jMenuItemEditFrame.setMnemonic('e');
            jMenuItemEditFrame.setEnabled(false);
        }
        return jMenuItemEditFrame;
    }

    void addEditFrameMenuItemActionListener(ActionListener actionListener) {
        jMenuItemEditFrame.addActionListener(actionListener);
    }

    private JMenuItem getJMenuItemEditDefaultDvdPalette() {
        if (jMenuItemEditDefaultDvdPalette == null) {
            jMenuItemEditDefaultDvdPalette = new JMenuItem();
            jMenuItemEditDefaultDvdPalette.setText("Edit default DVD Palette");
            jMenuItemEditDefaultDvdPalette.setMnemonic('d');
            jMenuItemEditDefaultDvdPalette.setDisplayedMnemonicIndex(5);
        }
        return jMenuItemEditDefaultDvdPalette;
    }

    void addEditDefaultDvdPaletteMenuItemActionListener(ActionListener actionListener) {
        jMenuItemEditDefaultDvdPalette.addActionListener(actionListener);
    }

    private JMenuItem getJMenuItemEditImportedDvdPalette() {
        if (jMenuItemEditImportedDvdPalette == null) {
            jMenuItemEditImportedDvdPalette = new JMenuItem();
            jMenuItemEditImportedDvdPalette.setEnabled(false);
            jMenuItemEditImportedDvdPalette.setText("Edit imported DVD Palette");
            jMenuItemEditImportedDvdPalette.setMnemonic('i');
            jMenuItemEditImportedDvdPalette.setDisplayedMnemonicIndex(5);
        }
        return jMenuItemEditImportedDvdPalette;
    }

    void addEditImportedDvdPaletteMenuItemActionListener(ActionListener actionListener) {
        jMenuItemEditImportedDvdPalette.addActionListener(actionListener);
    }

    private JMenuItem getJMenuItemEditDvdFramePalette() {
        if (jMenuItemEditDvdFramePalette == null) {
            jMenuItemEditDvdFramePalette = new JMenuItem();
            jMenuItemEditDvdFramePalette.setEnabled(false);
            jMenuItemEditDvdFramePalette.setText("Edit DVD Frame Palette");
            jMenuItemEditDvdFramePalette.setMnemonic('f');
        }
        return jMenuItemEditDvdFramePalette;
    }

    void addEditDvdFramePaletteMenuItemActionListener(ActionListener actionListener) {
        jMenuItemEditDvdFramePalette.addActionListener(actionListener);
    }

    private JMenuItem getJMenuItemMoveAll() {
        if (jMenuItemMoveAll == null) {
            jMenuItemMoveAll = new JMenuItem();
            jMenuItemMoveAll.setText("Move all captions");
            jMenuItemMoveAll.setMnemonic('m');
            jMenuItemMoveAll.setEnabled(false);
        }
        return jMenuItemMoveAll;
    }

    void addMoveAllMenuItemActionListener(ActionListener actionListener) {
        jMenuItemMoveAll.addActionListener(actionListener);
    }

    private JMenuItem getJMenuItemResetCropOffset() {
        if (jMenuItemResetCropOffset == null) {
            jMenuItemResetCropOffset = new JMenuItem();
            jMenuItemResetCropOffset.setMnemonic('r');
            jMenuItemResetCropOffset.setText("Reset crop offset");
        }
        return jMenuItemResetCropOffset;
    }

    void addResetCropOffsetMenuItemActionListener(ActionListener actionListener) {
        jMenuItemResetCropOffset.addActionListener(actionListener);
    }

    private JMenu getJMenuPrefs() {
        if (jMenuSettings == null) {
            jMenuSettings = new JMenu();
            jMenuSettings.setText("Settings");
            jMenuSettings.setMnemonic('s');
            jMenuSettings.add(getJMenuItemConversionSettings());
            jMenuSettings.add(getJMenuItemSwapCrCb());
            jMenuSettings.add(getJMenuItemFixInvisibleFrames());
            jMenuSettings.add(getJMenuItemVerbatimOutput());
        }
        return jMenuSettings;
    }

    private JMenuItem getJMenuItemConversionSettings() {
        if (jMenuItemConversionSettings == null) {
            jMenuItemConversionSettings = new JMenuItem();
            jMenuItemConversionSettings.setText("Conversion Settings");
            jMenuItemConversionSettings.setMnemonic('c');
        }
        return jMenuItemConversionSettings;
    }

    void addConversionSettingsMenuItemActionListener(ActionListener actionListener) {
        jMenuItemConversionSettings.addActionListener(actionListener);
    }

    private JMenuItem getJMenuItemSwapCrCb() {
        if (jMenuItemSwapCrCb == null) {
            jMenuItemSwapCrCb = new JCheckBoxMenuItem();
            jMenuItemSwapCrCb.setText("Swap Cr/Cb");
            jMenuItemSwapCrCb.setMnemonic('s');
            jMenuItemSwapCrCb.setSelected(false);
        }
        return jMenuItemSwapCrCb;
    }

    void addSwapCrCbMenuItemActionListener(ActionListener actionListener) {
        jMenuItemSwapCrCb.addActionListener(actionListener);
    }

    boolean isSwapCrCbSelected() {
        return jMenuItemSwapCrCb.isSelected();
    }

    private JMenuItem getJMenuItemFixInvisibleFrames() {
        if (jMenuItemFixInvisibleFrames == null) {
            jMenuItemFixInvisibleFrames = new JCheckBoxMenuItem();
            jMenuItemFixInvisibleFrames.setText("Fix invisible frames");
            jMenuItemFixInvisibleFrames.setMnemonic('f');
            jMenuItemFixInvisibleFrames.setSelected(false);
        }
        return jMenuItemFixInvisibleFrames;
    }

    void addFixInvisibleFramesMenuItemActionListener(ActionListener actionListener) {
        jMenuItemFixInvisibleFrames.addActionListener(actionListener);
    }

    boolean isFixInvisibleFramesSelected() {
        return jMenuItemFixInvisibleFrames.isSelected();
    }

    private JMenuItem getJMenuItemVerbatimOutput() {
        if (jMenuItemVerbatimOutput == null) {
            jMenuItemVerbatimOutput = new JCheckBoxMenuItem();
            jMenuItemVerbatimOutput.setText("Verbatim Output");
            jMenuItemVerbatimOutput.setMnemonic('v');
            jMenuItemVerbatimOutput.setSelected(false);
        }
        return jMenuItemVerbatimOutput;
    }

    void addVerbatimOutputMenuItemActionListener(ActionListener actionListener) {
        jMenuItemVerbatimOutput.addActionListener(actionListener);
    }

    boolean isVerbatimOutputSelected() {
        return jMenuItemVerbatimOutput.isSelected();
    }

    private JMenu getJMenuHelp() {
        if (jMenuHelp == null) {
            jMenuHelp = new JMenu();
            jMenuHelp.setText("Help");
            jMenuHelp.setMnemonic('h');
            jMenuHelp.add(getJMenuItemHelp());
        }
        return jMenuHelp;
    }

    private JMenuItem getJMenuItemHelp() {
        if (jMenuItemHelp == null) {
            jMenuItemHelp = new JMenuItem();
            jMenuItemHelp.setText("Help");
            jMenuItemHelp.setMnemonic('h');
        }
        return jMenuItemHelp;
    }

    void addHelpMenuItemActionListener(ActionListener actionListener) {
        jMenuItemHelp.addActionListener(actionListener);
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            GridBagConstraints gridBagPanelTop = new GridBagConstraints();
            gridBagPanelTop.gridx = 0;
            gridBagPanelTop.gridy = 0;
            gridBagPanelTop.anchor = GridBagConstraints.WEST;
            gridBagPanelTop.fill = GridBagConstraints.HORIZONTAL;
            gridBagPanelTop.insets = new Insets(0, 4, 0, 4);
            gridBagPanelTop.weightx = 0.0;
            gridBagPanelTop.ipadx = 0;
            gridBagPanelTop.weighty = 0.0;

            GridBagConstraints gridBagPanelInfoSource = new GridBagConstraints();
            gridBagPanelInfoSource.gridx = 0;
            gridBagPanelInfoSource.gridy = 1;
            gridBagPanelInfoSource.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelInfoSource.fill = GridBagConstraints.HORIZONTAL;
            gridBagPanelInfoSource.insets = new Insets(4, 0, 0, 0);
            gridBagPanelInfoSource.weightx = 0.0;
            gridBagPanelInfoSource.weighty = 0.0;

            GridBagConstraints gridBagScrollPaneSource = new GridBagConstraints();
            gridBagScrollPaneSource.gridx = 0;
            gridBagScrollPaneSource.gridy = 2;
            gridBagScrollPaneSource.fill = GridBagConstraints.BOTH;
            gridBagScrollPaneSource.anchor = GridBagConstraints.NORTHWEST;
            gridBagScrollPaneSource.weightx = 1.0;
            gridBagScrollPaneSource.weighty = 1.0;

            GridBagConstraints gridBagPanelInfoTarget = new GridBagConstraints();
            gridBagPanelInfoTarget.gridx = 0;
            gridBagPanelInfoTarget.gridy = 3;
            gridBagPanelInfoTarget.fill = GridBagConstraints.HORIZONTAL;
            gridBagPanelInfoTarget.anchor = GridBagConstraints.WEST;
            gridBagPanelInfoTarget.gridwidth = 1;
            gridBagPanelInfoTarget.weighty = 0.0;
            gridBagPanelInfoTarget.weightx = 0.0;

            GridBagConstraints gridBagScrollPaneTarget = new GridBagConstraints();
            gridBagScrollPaneTarget.gridx = 0;
            gridBagScrollPaneTarget.gridy = 4;
            gridBagScrollPaneTarget.fill = GridBagConstraints.BOTH;
            gridBagScrollPaneTarget.anchor = GridBagConstraints.NORTHWEST;
            gridBagScrollPaneTarget.weightx = 1.0;
            gridBagScrollPaneTarget.weighty = 1.0;

            GridBagConstraints gridBagPanelBottom = new GridBagConstraints();
            gridBagPanelBottom.gridx = 0;
            gridBagPanelBottom.gridy = 5;
            gridBagPanelBottom.anchor = GridBagConstraints.SOUTHWEST;
            gridBagPanelBottom.fill = GridBagConstraints.BOTH;

            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.setPreferredSize(new Dimension(800, 600));
            jContentPane.add(getJPanelTop(), gridBagPanelTop);
            jContentPane.add(getJPanelInfoSource(), gridBagPanelInfoSource);
            jContentPane.add(getJScrollPaneSource(), gridBagScrollPaneSource);
            jContentPane.add(getJPanelInfoTarget(), gridBagPanelInfoTarget);
            jContentPane.add(getJScrollPaneTarget(), gridBagScrollPaneTarget);
            jContentPane.add(getJPanelBottom(), gridBagPanelBottom);
        }
        return jContentPane;
    }

    private JPanel getJPanelTop() {
        if (jPanelTop == null) {
            GridBagConstraints gridBagComboFilter = new GridBagConstraints();
            gridBagComboFilter.fill = GridBagConstraints.NONE;
            gridBagComboFilter.gridx = 7;
            gridBagComboFilter.gridy = 1;
            gridBagComboFilter.insets = new Insets(0, 4, 0, 4);
            gridBagComboFilter.anchor = GridBagConstraints.WEST;
            gridBagComboFilter.ipadx = 10;
            gridBagComboFilter.weightx = 5.0;
            GridBagConstraints gridBagLabelFilter = new GridBagConstraints();
            gridBagLabelFilter.gridx = 7;
            gridBagLabelFilter.anchor = GridBagConstraints.WEST;
            gridBagLabelFilter.insets = new Insets(0, 4, 0, 4);
            gridBagLabelFilter.gridy = 0;
            GridBagConstraints gridBagLabelPalette = new GridBagConstraints();
            gridBagLabelPalette.insets = new Insets(0, 4, 0, 4);
            gridBagLabelPalette.anchor = GridBagConstraints.WEST;
            gridBagLabelPalette.gridx = 6;
            gridBagLabelPalette.gridy = 0;
            GridBagConstraints gridBagComboPalette = new GridBagConstraints();
            gridBagComboPalette.fill = GridBagConstraints.NONE;
            gridBagComboPalette.gridx = 6;
            gridBagComboPalette.gridy = 1;
            gridBagComboPalette.anchor = GridBagConstraints.WEST;
            gridBagComboPalette.insets = new Insets(0, 4, 0, 4);
            gridBagComboPalette.ipadx = 10;
            gridBagComboPalette.weightx = 0.0;
            GridBagConstraints gridBagLabelSubNum = new GridBagConstraints();
            gridBagLabelSubNum.gridx = 0;
            gridBagLabelSubNum.gridy = 0;
            gridBagLabelSubNum.anchor = GridBagConstraints.WEST;
            gridBagLabelSubNum.insets = new Insets(0, 8, 0, 4);

            GridBagConstraints gridBagComboBoxSubNum = new GridBagConstraints();
            gridBagComboBoxSubNum.gridx = 0;
            gridBagComboBoxSubNum.gridy = 1;
            gridBagComboBoxSubNum.anchor = GridBagConstraints.WEST;
            gridBagComboBoxSubNum.ipadx = 10;
            gridBagComboBoxSubNum.insets = new Insets(0, 4, 0, 4);

            GridBagConstraints gridBagLabelAlphaThr = new GridBagConstraints();
            gridBagLabelAlphaThr.gridx = 1;
            gridBagLabelAlphaThr.gridy = 0;
            gridBagLabelAlphaThr.anchor = GridBagConstraints.WEST;
            gridBagLabelAlphaThr.insets = new Insets(0, 4, 0, 4);

            GridBagConstraints gridBagComboBoxAlphaThr = new GridBagConstraints();
            gridBagComboBoxAlphaThr.gridx = 1;
            gridBagComboBoxAlphaThr.gridy = 1;
            gridBagComboBoxAlphaThr.anchor = GridBagConstraints.WEST;
            gridBagComboBoxAlphaThr.ipadx = 10;
            gridBagComboBoxAlphaThr.insets = new Insets(0, 4, 0, 4);

            GridBagConstraints gridBagLabelMedLowThr = new GridBagConstraints();
            gridBagLabelMedLowThr.gridx = 2;
            gridBagLabelMedLowThr.gridy = 0;
            gridBagLabelMedLowThr.anchor = GridBagConstraints.WEST;
            gridBagLabelMedLowThr.insets = new Insets(0, 4, 0, 4);

            GridBagConstraints gridBagComboBoxMedLowThr = new GridBagConstraints();
            gridBagComboBoxMedLowThr.gridx = 2;
            gridBagComboBoxMedLowThr.gridy = 1;
            gridBagComboBoxMedLowThr.anchor = GridBagConstraints.WEST;
            gridBagComboBoxMedLowThr.ipadx = 10;
            gridBagComboBoxMedLowThr.insets = new Insets(0, 4, 0, 4);

            GridBagConstraints gridBagLabelHiMedThr = new GridBagConstraints();
            gridBagLabelHiMedThr.gridx = 3;
            gridBagLabelHiMedThr.gridy = 0;
            gridBagLabelHiMedThr.anchor = GridBagConstraints.WEST;
            gridBagLabelHiMedThr.insets = new Insets(0, 4, 0, 4);

            GridBagConstraints gridBagJComboBoxHiMedThr = new GridBagConstraints();
            gridBagJComboBoxHiMedThr.gridx = 3;
            gridBagJComboBoxHiMedThr.gridy = 1;
            gridBagJComboBoxHiMedThr.anchor = GridBagConstraints.WEST;
            gridBagJComboBoxHiMedThr.ipadx = 10;
            gridBagJComboBoxHiMedThr.insets = new Insets(0, 4, 0, 4);


            GridBagConstraints gridBagLabelOutFormat = new GridBagConstraints();
            gridBagLabelOutFormat.gridx = 5;
            gridBagLabelOutFormat.gridy = 0;
            gridBagLabelOutFormat.anchor = GridBagConstraints.WEST;
            gridBagLabelOutFormat.insets = new Insets(0, 4, 0, 4);

            GridBagConstraints gridBagComboBoxOutFormat = new GridBagConstraints();
            gridBagComboBoxOutFormat.gridx = 5;
            gridBagComboBoxOutFormat.gridy = 1;
            gridBagComboBoxOutFormat.anchor = GridBagConstraints.WEST;
            gridBagComboBoxOutFormat.weightx = 0.0;
            gridBagComboBoxOutFormat.fill = GridBagConstraints.NONE;
            gridBagComboBoxOutFormat.ipadx = 10;
            gridBagComboBoxOutFormat.insets = new Insets(0, 4, 0, 4);

            JLabel jLabelSubNum = new JLabel();
            jLabelSubNum.setText("Subtitle");
            jLabelSubNum.setPreferredSize(new Dimension(100, 20));
            JLabel jLabelAlphaThr = new JLabel();
            jLabelAlphaThr.setText("Alpha Threshold");
            jLabelAlphaThr.setPreferredSize(new Dimension(100, 20));
            JLabel jLabelMedLowThr = new JLabel();
            jLabelMedLowThr.setText("Med/Low Threshold");
            jLabelMedLowThr.setPreferredSize(new Dimension(100, 20));
            JLabel jLabelHiMedThr = new JLabel();
            jLabelHiMedThr.setText("Hi/Med Threshold");
            jLabelHiMedThr.setPreferredSize(new Dimension(100, 20));
            JLabel jLabelOutFormat = new JLabel();
            jLabelOutFormat.setText("Output Format");
            jLabelOutFormat.setPreferredSize(new Dimension(120, 20));
            JLabel jLabelPalette = new JLabel();
            jLabelPalette.setPreferredSize(new Dimension(120, 20));
            jLabelPalette.setText("Palette");
            JLabel jLabelFilter = new JLabel();
            jLabelFilter.setPreferredSize(new Dimension(120, 20));
            jLabelFilter.setText("Filter");

            jPanelTop = new JPanel();
            jPanelTop.setLayout(new GridBagLayout());
            jPanelTop.setPreferredSize(new Dimension(600, 40));
            jPanelTop.setMinimumSize(new Dimension(600, 40));
            jPanelTop.setMaximumSize(new Dimension(600, 40));
            jPanelTop.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            jPanelTop.add(jLabelSubNum, gridBagLabelSubNum);
            jPanelTop.add(getJComboBoxSubNum(), gridBagComboBoxSubNum);
            jPanelTop.add(jLabelAlphaThr, gridBagLabelAlphaThr);
            jPanelTop.add(getJComboBoxAlphaThreshold(), gridBagComboBoxAlphaThr);
            jPanelTop.add(jLabelMedLowThr, gridBagLabelMedLowThr);
            jPanelTop.add(getJComboBoxMedLowThreshold(), gridBagComboBoxMedLowThr);
            jPanelTop.add(jLabelHiMedThr, gridBagLabelHiMedThr);
            jPanelTop.add(getJComboBoxHiMedThreshold(), gridBagJComboBoxHiMedThr);
            jPanelTop.add(jLabelOutFormat, gridBagLabelOutFormat);
            jPanelTop.add(getJComboBoxOutputFormat(), gridBagComboBoxOutFormat);
            jPanelTop.add(getJComboBoxPalette(), gridBagComboPalette);
            jPanelTop.add(jLabelPalette, gridBagLabelPalette);
            jPanelTop.add(jLabelFilter, gridBagLabelFilter);
            jPanelTop.add(getJComboBoxFilter(), gridBagComboFilter);
        }
        return jPanelTop;
    }

    private JPanel getJPanelInfoSource() {
        if (jPanelInfoSource == null) {
            GridBagConstraints gridBagLabelInfoSup = new GridBagConstraints();
            gridBagLabelInfoSup.anchor = GridBagConstraints.WEST;
            gridBagLabelInfoSup.insets = new Insets(4, 8, 2, 8);
            gridBagLabelInfoSup.gridwidth = 1;
            gridBagLabelInfoSup.gridx = 0;
            gridBagLabelInfoSup.gridy = 0;
            gridBagLabelInfoSup.weightx = 1.0;
            gridBagLabelInfoSup.fill = GridBagConstraints.HORIZONTAL;

            jLabelInfoSource = new JLabel();
            jLabelInfoSource.setHorizontalAlignment(SwingConstants.LEFT);
            jLabelInfoSource.setHorizontalTextPosition(SwingConstants.LEFT);

            jPanelInfoSource = new JPanel();
            jPanelInfoSource.setLayout(new GridBagLayout());
            jPanelInfoSource.setPreferredSize(new Dimension(600, 20));
            jPanelInfoSource.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.lightGray));
            jPanelInfoSource.add(jLabelInfoSource, gridBagLabelInfoSup);
        }
        return jPanelInfoSource;
    }

    private JScrollPane getJScrollPaneSource() {
        if (jScrollPaneSource == null) {
            jScrollPaneSource = new JScrollPane();
            jScrollPaneSource.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jScrollPaneSource.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            jScrollPaneSource.setViewportView(getJPanelSource());
        }
        return jScrollPaneSource;
    }

    private JPanel getJPanelSource() {
        if (jPanelSource == null) {
            jPanelSource = new GfxPane();
        }
        return jPanelSource;
    }

    private JPanel getJPanelInfoTarget() {
        if (jPanelInfoTarget == null) {
            GridBagConstraints gridBagLabelSubInfo = new GridBagConstraints();
            gridBagLabelSubInfo.gridx = 0;
            gridBagLabelSubInfo.weightx = 1.0;
            gridBagLabelSubInfo.insets = new Insets(4, 8, 2, 8);
            gridBagLabelSubInfo.fill = GridBagConstraints.HORIZONTAL;
            gridBagLabelSubInfo.anchor = GridBagConstraints.WEST;
            gridBagLabelSubInfo.gridy = 0;

            jLabelInfoTarget = new JLabel();
            jLabelInfoTarget.setHorizontalTextPosition(SwingConstants.LEFT);
            jLabelInfoTarget.setHorizontalAlignment(SwingConstants.LEFT);
            jPanelInfoTarget = new JPanel();
            jPanelInfoTarget.setLayout(new GridBagLayout());
            jPanelInfoTarget.setPreferredSize(new Dimension(300, 20));
            jPanelInfoTarget.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            jPanelInfoTarget.add(jLabelInfoTarget, gridBagLabelSubInfo);
        }
        return jPanelInfoTarget;
    }

    private JScrollPane getJScrollPaneTarget() {
        if (jScrollPaneTarget == null) {
            jScrollPaneTarget = new JScrollPane();
            jScrollPaneTarget.setViewportView(getJPanelTarget());
        }
        return jScrollPaneTarget;
    }

    private JPanel getJPanelTarget() {
        if (jPanelTarget == null) {
            jPanelTarget = new GfxPane();
        }
        return jPanelTarget;
    }

    private JPanel getJPanelBottom() {
        if (jPanelBottom == null) {
            GridBagConstraints gridBagLayout = new GridBagConstraints();
            gridBagLayout.anchor = GridBagConstraints.NORTHEAST;
            gridBagLayout.gridx = 1;
            gridBagLayout.gridy = 0;
            gridBagLayout.weightx = 1.0;
            gridBagLayout.weighty = 1.0;
            gridBagLayout.fill = GridBagConstraints.BOTH;
            GridBagConstraints gridBagConsole = new GridBagConstraints();
            gridBagConsole.fill = GridBagConstraints.BOTH;
            gridBagConsole.gridy = 0;
            gridBagConsole.weightx = 2.0;
            gridBagConsole.weighty = 2.0;
            gridBagConsole.gridx = 0;
            jPanelBottom = new JPanel();
            jPanelBottom.setLayout(new GridBagLayout());
            jPanelBottom.setPreferredSize(new Dimension(300, 150));
            jPanelBottom.setMinimumSize(new Dimension(300, 150));
            jPanelBottom.add(getJPanelLayout(), gridBagLayout);
            jPanelBottom.add(getJScrollPaneConsole(), gridBagConsole);
        }
        return jPanelBottom;
    }

    private JPanel getJPanelLayout() {
        if (jLayoutPane == null) {
            jLayoutPane = new EditPane(true);
            jLayoutPane.setLayout(new GridBagLayout());
            jLayoutPane.setPreferredSize(new Dimension(180, 100));
            jLayoutPane.setMaximumSize(new Dimension(180,100));
            jLayoutPane.addMouseListener( new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (Core.isReady()) {
                            EditDialog ed = new EditDialog(MainFrameView.this);
                            ed.setIndex(model.getSubIndex());
                            ed.setVisible(true);
                            model.setSubIndex(ed.getIndex());
                            (new Thread() {
                                @Override
                                public void run() {
                                    synchronized (threadSemaphore) {
                                        try {
                                            int subIndex = model.getSubIndex();
                                            Core.convertSup(subIndex, subIndex + 1, Core.getNumFrames());
                                            refreshSrcFrame(subIndex);
                                            refreshTrgFrame(subIndex);
                                            jComboBoxSubNum.setSelectedIndex(subIndex);
                                        } catch (CoreException ex) {
                                            error(ex.getMessage());
                                        } catch (Exception ex) {
                                            ToolBox.showException(ex);
                                            exit(4);
                                        }

                                    }
                                }
                            }).start();
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }
            });
        }
        return jLayoutPane;
    }

    void setLayoutPaneAspectRatio(double trgRatio) {
        jLayoutPane.setAspectRatio(trgRatio);
    }

    void setLayoutPaneCropOffsetY(int cropOfsY) {
        jLayoutPane.setCropOfsY(cropOfsY);
    }

    void repaintLayoutPane() {
        jLayoutPane.repaint();
    }

    private JScrollPane getJScrollPaneConsole() {
        if (jScrollPaneConsole == null) {
            jScrollPaneConsole = new JScrollPane();
            jScrollPaneConsole.setViewportView(getConsole());
        }
        return jScrollPaneConsole;
    }

    private JTextArea getConsole() {
        if (console == null) {
            console = new JTextArea();
            console.setEditable(false);
            console.setFont(new Font("Monospaced", Font.PLAIN, 12));
        }
        return console;
    }

    void addConsoleMouseListener(MouseListener mouseListener) {
        console.addMouseListener(mouseListener);
    }

    private void printToConsole(String message) {
        Document doc = console.getDocument();
        int length = doc.getLength();
        try {
            doc.insertString(length, message, null);
        } catch (BadLocationException ex) {
            //
        }
    }

    public void printOut(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                printToConsole(message);
            }
        });
    }

    public void printErr(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                printToConsole(message);
            }
        });
    }

    public void printWarn(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                printToConsole(message);
            }
        });
    }

    public void flushConsole() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                console.setCaretPosition(console.getDocument().getLength());
            }
        });
    }

    String getConsoleSelectedText() {
        return console.getSelectedText();
    }

    void setConsoleText(String text) {
        console.setText(text);
    }

    private JComboBox getJComboBoxSubNum() {
        if (jComboBoxSubNum == null) {
            jComboBoxSubNum = new JComboBox();
            jComboBoxSubNum.setEnabled(false);
            jComboBoxSubNum.setPreferredSize(new Dimension(100, 20));
            jComboBoxSubNum.setMinimumSize(new Dimension(80, 20));
            jComboBoxSubNum.setToolTipText("Set subtitle number");
            jComboBoxSubNum.setEditable(true);
        }
        return jComboBoxSubNum;
    }

    void addSubNumComboBoxActionListener(ActionListener actionListener) {
        jComboBoxSubNum.addActionListener(actionListener);
    }

    void addSubNumComboBoxDocumentListener(DocumentListener documentListener) {
        jTextSubNum.getDocument().addDocumentListener(documentListener);
    }
    
    void setSubNumComboBoxBackground(Color color) {
        jTextSubNum.setBackground(color);
    }
    
    String getSubNumComboBoxText() {
        return jTextSubNum.getText();
    }

    void initSubNumComboBox(int subCount) {
        jComboBoxSubNum.removeAllItems();
        for (int i=1; i <= subCount; i++) {
            jComboBoxSubNum.addItem(i);
        }
        jComboBoxSubNum.setSelectedIndex(0);
    }

    Object getSubNumComboBoxSelectedItem() {
        return jComboBoxSubNum.getSelectedItem();
    }

    void setSubNumComboBoxSelectedIndex(int index) {
        jComboBoxSubNum.setSelectedIndex(index);
    }

    private JComboBox getJComboBoxAlphaThreshold() {
        if (jComboBoxAlphaThreshold == null) {
            jComboBoxAlphaThreshold = new JComboBox();
            jComboBoxAlphaThreshold.setEnabled(false);
            jComboBoxAlphaThreshold.setEditable(true);
            jComboBoxAlphaThreshold.setToolTipText("Set alpha threshold");
            jComboBoxAlphaThreshold.setPreferredSize(new Dimension(100, 20));
            jComboBoxAlphaThreshold.setMinimumSize(new Dimension(80, 20));
        }
        return jComboBoxAlphaThreshold;
    }

    void addAlphaThresholdComboBoxActionListener(ActionListener actionListener) {
        jComboBoxAlphaThreshold.addActionListener(actionListener);
    }

    void addAlphaThresholdComboBoxDocumentListener(DocumentListener documentListener) {
        jTextAlphaThreshold.getDocument().addDocumentListener(documentListener);
    }

    void initAlphaThresholdComboBoxSelectedIndices() {
        jComboBoxAlphaThreshold.setSelectedIndex(Core.getAlphaThr());
        jComboBoxHiMedThreshold.setSelectedIndex(Core.getLumThr()[0]);
        jComboBoxMedLowThreshold.setSelectedIndex(Core.getLumThr()[1]);
    }

    Object getAlphaThresholdComboBoxSelectedItem() {
        return jComboBoxAlphaThreshold.getSelectedItem();
    }

    String getAlphaThresholdComboBoxText() {
        return jTextAlphaThreshold.getText();
    }

    void setAlphaThresholdComboBoxSelectedIndex(int index) {
        jComboBoxAlphaThreshold.setSelectedIndex(index);
    }

    void setAlphaThresholdComboBoxBackground(Color color) {
        jTextAlphaThreshold.setBackground(color);
    }

    private JComboBox getJComboBoxHiMedThreshold() {
        if (jComboBoxHiMedThreshold == null) {
            jComboBoxHiMedThreshold = new JComboBox();
            jComboBoxHiMedThreshold.setEditable(true);
            jComboBoxHiMedThreshold.setEnabled(false);
            jComboBoxHiMedThreshold.setPreferredSize(new Dimension(100, 20));
            jComboBoxHiMedThreshold.setMinimumSize(new Dimension(80, 20));
            jComboBoxHiMedThreshold.setToolTipText("Set medium/high luminance threshold");
        }
        return jComboBoxHiMedThreshold;
    }

    void addHiMedThresholdComboBoxActionListener(ActionListener actionListener) {
        jComboBoxHiMedThreshold.addActionListener(actionListener);
    }

    void addHiMedThresholdComboBoxDocumentListener(DocumentListener documentListener) {
        jTextHiMedThreshold.getDocument().addDocumentListener(documentListener);
    }

    void setHiMedThresholdComboBoxBackground(Color color) {
        jTextHiMedThreshold.setBackground(color);
    }

    String getHiMedThresholdComboBoxText() {
        return jTextHiMedThreshold.getText();
    }

    Object getHiMedThresholdComboBoxSelectedItem() {
        return jComboBoxHiMedThreshold.getSelectedItem();
    }

    void setHiMedThresholdComboBoxSelectedIndex(int index) {
        jComboBoxHiMedThreshold.setSelectedIndex(index);
    }

    private JComboBox getJComboBoxMedLowThreshold() {
        if (jComboBoxMedLowThreshold == null) {
            jComboBoxMedLowThreshold = new JComboBox();
            jComboBoxMedLowThreshold.setEditable(true);
            jComboBoxMedLowThreshold.setEnabled(false);
            jComboBoxMedLowThreshold.setToolTipText("Set low/medium luminance threshold");
            jComboBoxMedLowThreshold.setPreferredSize(new Dimension(100, 20));
            jComboBoxMedLowThreshold.setMinimumSize(new Dimension(80, 20));
        }
        return jComboBoxMedLowThreshold;
    }

    void addMedLowThresholdComboBoxActionListener(ActionListener actionListener) {
        jComboBoxMedLowThreshold.addActionListener(actionListener);
    }

    void addMedLowThresholdComboBoxDocumentListener(DocumentListener documentListener) {
        jTextMedLowThreshold.getDocument().addDocumentListener(documentListener);
    }

    void setMedLowThresholdComboBoxBackground(Color color) {
        jTextMedLowThreshold.setBackground(color);
    }

    String getMedLowThresholdComboBoxText() {
        return jTextMedLowThreshold.getText();
    }

    Object getMedLowThresholdComboBoxSelectedItem() {
        return jComboBoxMedLowThreshold.getSelectedItem();
    }

    void setMedLowThresholdComboBoxSelectedIndex(int index) {
        jComboBoxMedLowThreshold.setSelectedIndex(index);
    }

    private JComboBox getJComboBoxOutputFormat() {
        if (jComboBoxOutputFormat == null) {
            jComboBoxOutputFormat = new JComboBox();
            jComboBoxOutputFormat.setEnabled(false);
            jComboBoxOutputFormat.setToolTipText("Select export format");
            jComboBoxOutputFormat.setPreferredSize(new Dimension(120, 20));
        }
        return jComboBoxOutputFormat;
    }

    void addOutputFormatComboBoxActionListener(ActionListener actionListener) {
        jComboBoxOutputFormat.addActionListener(actionListener);
    }
    
    int getOutputFormatComboBoxSelectedIndex() {
        return jComboBoxOutputFormat.getSelectedIndex();
    }

    void setComboBoxOutFormatEnabled(boolean enable) {
        jComboBoxOutputFormat.setEnabled(enable);
    }

    private JComboBox getJComboBoxPalette() {
        if (jComboBoxPalette == null) {
            jComboBoxPalette = new JComboBox();
            jComboBoxPalette.setEnabled(false);
            jComboBoxPalette.setToolTipText("Select palette mode");
            jComboBoxPalette.setPreferredSize(new Dimension(120, 20));
        }
        return jComboBoxPalette;
    }

    void addPaletteComboBoxActionListener(ActionListener actionListener) {
        jComboBoxPalette.addActionListener(actionListener);
    }

    int getPaletteComboBoxSelectedIndex() {
        return jComboBoxPalette.getSelectedIndex();
    }

    private JComboBox getJComboBoxFilter() {
        if (jComboBoxFilter == null) {
            jComboBoxFilter = new JComboBox();
            jComboBoxFilter.setEnabled(false);
            jComboBoxFilter.setToolTipText("Select filter for scaling");
            jComboBoxFilter.setPreferredSize(new Dimension(120, 20));
        }
        return jComboBoxFilter;
    }

    void addFilterComboBoxActionListener(ActionListener actionListener) {
        jComboBoxFilter.addActionListener(actionListener);
    }

    int getFilterComboBoxSelectedIndex() {
        return jComboBoxFilter.getSelectedIndex();
    }

    void enableCoreComponents(boolean state) {
        jMenuItemLoad.setEnabled(state);
        jMenuRecentFiles.setEnabled(state && Core.getRecentFiles().size() > 0);
        jMenuItemSave.setEnabled(state && Core.getNumFrames() > 0);
        jMenuItemClose.setEnabled(state);
        jMenuItemEditFrame.setEnabled(state);
        jMenuItemMoveAll.setEnabled(state);
        jComboBoxSubNum.setEnabled(state);
        jComboBoxOutputFormat.setEnabled(state);
        jComboBoxFilter.setEnabled(state);
    }

    /**
     * Enable/disable components dependent only available for VobSubs
     */
    void enableVobSubMenuCombo() {
        boolean b = (Core.getOutputMode() == OutputMode.VOBSUB   || Core.getOutputMode() == OutputMode.SUPIFO)
                && ( (Core.getInputMode()  != InputMode.VOBSUB   && Core.getInputMode() != InputMode.SUPIFO)
                        || Core.getPaletteMode() != PaletteMode.KEEP_EXISTING);

        jComboBoxAlphaThreshold.setEnabled(b);
        jComboBoxHiMedThreshold.setEnabled(b);
        jComboBoxMedLowThreshold.setEnabled(b);

        b = (Core.getInputMode()  == InputMode.VOBSUB  || Core.getInputMode() == InputMode.SUPIFO);
        jMenuItemEditImportedDvdPalette.setEnabled(b);
        jMenuItemEditDvdFramePalette.setEnabled(b);
    }

    /**
     * Enable/disable components dependent only available for VobSubs
     * @param b true: enable
     */
    void enableVobsubBits(boolean b) {
        boolean ready = Core.isReady();
        Core.setReady(false);
        jComboBoxPalette.removeAllItems();
        for (PaletteMode m : PaletteMode.values()) {
            if (!b || m != PaletteMode.CREATE_DITHERED) {
                jComboBoxPalette.addItem(m.toString());
            }
        }
        if (!b || Core.getPaletteMode() != PaletteMode.CREATE_DITHERED) {
            jComboBoxPalette.setSelectedIndex(Core.getPaletteMode().ordinal());
        } else {
            jComboBoxPalette.setSelectedIndex(PaletteMode.CREATE_NEW.ordinal());
        }

        if (!b || Core.getInputMode() == InputMode.VOBSUB || Core.getInputMode() == InputMode.SUPIFO) {
            jComboBoxPalette.setEnabled(true);
        } else {
            jComboBoxPalette.setEnabled(false);
        }

        enableVobSubMenuCombo();
        Core.setReady(ready);
    }

    void closeSub() {
        jComboBoxSubNum.removeAllItems();
        enableCoreComponents(false);
        jMenuItemLoad.setEnabled(true);
        updateRecentFilesMenu();
        jComboBoxPalette.setEnabled(false);
        jComboBoxAlphaThreshold.setEnabled(false);
        jComboBoxHiMedThreshold.setEnabled(false);
        jComboBoxMedLowThreshold.setEnabled(false);
        jMenuItemEditImportedDvdPalette.setEnabled(false);
        jMenuItemEditDvdFramePalette.setEnabled(false);

        jLayoutPane.setImage(null,1,1);
        jLayoutPane.repaint();
        jPanelTarget.setImage(null);
        jPanelSource.setImage(null);

        jLabelInfoTarget.setText("");
        jLabelInfoSource.setText("");
    }

    private JPopupMenu getJPopupMenu() {
        if (jPopupMenu == null) {
            jPopupMenu = new JPopupMenu();
            jPopupMenu.add(getJPopupMenuItemCopy());
            jPopupMenu.add(getJPopupMenuItemClear());
        }
        return jPopupMenu;
    }

    void showPopupMenu(int x, int y) {
        jPopupMenu.show(console, x, y);
    }

    private JMenuItem getJPopupMenuItemCopy() {
        if (jPopupMenuItemCopy == null) {
            jPopupMenuItemCopy = new JMenuItem();
            jPopupMenuItemCopy.setText("Copy");
        }
        return jPopupMenuItemCopy;
    }

    void addCopyPopupMenuItemActionListener(ActionListener actionListener) {
        jPopupMenuItemCopy.addActionListener(actionListener);
    }

    void setCopyPopupMenuItemEnabled(boolean enable) {
        jPopupMenuItemCopy.setEnabled(enable);
    }

    private JMenuItem getJPopupMenuItemClear() {
        if (jPopupMenuItemClear == null) {
            jPopupMenuItemClear = new JMenuItem();
            jPopupMenuItemClear.setText("Clear");
        }
        return jPopupMenuItemClear;
    }

    void addClearPopupMenuItemActionListener(ActionListener actionListener) {
        jPopupMenuItemClear.addActionListener(actionListener);
    }

    void addTransferHandler(TransferHandler transferHandler) {
        setTransferHandler(transferHandler);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    /**
     * Update all components belonging to the source window
     * @param index caption index
     */
    void refreshSrcFrame(final int index) {
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                BufferedImage img = Core.getSrcImage();
                jPanelSource.setImage(img);
                jLabelInfoSource.setText(Core.getSrcInfoStr(index));
            }
        });
    }
    /**
     * Update all components belonging to the target window
     * @param index caption index
     */
    void refreshTrgFrame(final int index) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jLayoutPane.setDim(Core.getTrgWidth(index), Core.getTrgHeight(index));
                jLayoutPane.setOffsets(Core.getTrgOfsX(index), Core.getTrgOfsY(index));
                jLayoutPane.setCropOfsY(Core.getCropOfsY());
                jLayoutPane.setImage(Core.getTrgImage(), Core.getTrgImgWidth(index), Core.getTrgImgHeight(index));
                jLayoutPane.setExcluded(Core.getTrgExcluded(index));
                jPanelTarget.setImage(Core.getTrgImage());
                jLabelInfoTarget.setText(Core.getTrgInfoStr(index));
                jLayoutPane.repaint();
            }
        });
    }

    /**
     * Common exit routine, stores properties and releases Core file handles
     * @param code exit code
     */
    void exit(int code) {
        if (code == 0) {
            // store width and height
            Dimension d = getSize();
            if (this.getExtendedState() != MainFrameView.MAXIMIZED_BOTH) {
                Core.props.set("frameWidth", d.width);
                Core.props.set("frameHeight", d.height);
                // store frame pos
                Point p = getLocation();
                Core.props.set("framePosX", p.x);
                Core.props.set("framePosY", p.y);
            }
        }
        Core.exit();
        System.exit(code);
    }

    /**
     * Output a dialog with number of warnings and errors
     */
    void warningDialog() {
        int w = Core.getWarnings();
        Core.resetWarnings();
        int e = Core.getErrors();
        Core.resetErrors();
        if (w+e > 0) {
            String s = "";
            if (w > 0) {
                if (w==1) {
                    s += w+" warning";
                } else {
                    s += w+" warnings";
                }
            }
            if (w>0 && e>0) {
                s += " and ";
            }
            if (e > 0) {
                if (e==1) {
                    s = e+" error";
                } else {
                    s = e+" errors";
                }
            }

            if (w+e < 3) {
                s = "There was "+s;
            } else {
                s = "There were "+s;
            }

            JOptionPane.showMessageDialog(this, s + "\nCheck the log for details", "Warning!", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void error (String message) {
        Core.printErr(message);
        JOptionPane.showMessageDialog(this, message, "Error!", JOptionPane.WARNING_MESSAGE);
    }
}
