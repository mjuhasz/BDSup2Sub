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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static bdsup2sub.core.Constants.*;

public class MainFrameView extends JFrame implements ClipboardOwner {

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane;
    private JScrollPane jScrollPaneSrc;
    private JScrollPane jScrollPaneTrg;
    private JPanel jPanelUp;
    private JPanel jPanelDown;
    private GfxPane jPanelSrc;
    private GfxPane jPanelTrg;
    private JComboBox jComboBoxSubNum;
    private JComboBox jComboBoxAlphaThreshold;
    private JComboBox jComboBoxHiMedThreshold;
    private JComboBox jComboBoxMedLowThreshold;
    private JComboBox jComboBoxOutputFormat;
    private JLabel jLabelInfoSrc;
    private JLabel jLabelInfoTrg;
    private JPanel jPanelUp2;
    private JPanel jPanelMid;
    private JMenuBar jMenuBar;
    private JMenu jMenuFile;
    private JMenuItem jMenuItemLoad;
    private JMenu jMenuRecentFiles;
    private JMenuItem jMenuItemSave;
    private JMenuItem jMenuItemClose;
    private JMenuItem jMenuItemQuit;
    private JMenu jMenuHelp;
    private JMenuItem jMenuItemHelp;
    private JMenu jMenuPrefs;
    private JMenuItem jMenuItemEditDefaultDvdPalette;
    private JMenuItem jMenuItemEditImportedDvdPalette;
    private JMenuItem jMenuItemEditDvdFramePalette;
    private JMenuItem jMenuItemConversionSettings;
    private JCheckBoxMenuItem jMenuItemSwapCrCb;
    private JCheckBoxMenuItem jMenuItemVerbatimOutput;
    private JCheckBoxMenuItem jMenuItemFixInvisibleFrames;
    private JMenu jMenuEdit;
    private JMenuItem jMenuItemEditFrame;
    private JMenuItem jMenuItemMoveAll;
    private JMenuItem jMenuItemResetCropOffset;
    private JPopupMenu jPopupMenu;
    private JMenuItem jPopupMenuItemCopy;
    private JMenuItem jPopupMenuItemClear;
    private JScrollPane jScrollPaneConsole;
    private JTextArea console;
    private EditPane jLayoutPane;
    private JComboBox jComboBoxPalette;
    private JComboBox jComboBoxFilter;
    private JTextField jTextSubNum;
    private JTextField jTextAlphaThreshold;
    private JTextField jTextHiMedThreshold;
    private JTextField jTextMedLowThreshold;


    /** semaphore for synchronization of threads */
    final Object threadSemaphore = new Object();
    /** reference to this frame (to allow access to "this" from inner classes */
    private JFrame mainFrame;
    /** font size for output console */
    private int fontSize = 12;
    //private static final int maxDocSize = 1000000; // to work around bad TextPane performance

    private ActionListener recentFilesMenuActionListener;
    
    private MainFrameModel model;
    
    public MainFrameView(MainFrameModel model) {
        super(APP_NAME_AND_VERSION);
        this.model = model;

        jTextSubNum = new JTextField();
        jTextAlphaThreshold = new JTextField();
        jTextHiMedThreshold = new JTextField();
        jTextMedLowThreshold = new JTextField();

        initialize();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit(0);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                exit(0);
            }
        });

        ClassLoader loader = MainFrameView.class.getClassLoader();
        Image img = Toolkit.getDefaultToolkit().getImage(loader.getResource("icon_32.png"));
        setIconImage(img);

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

        mainFrame = this;

        updateRecentFilesMenu();

        // fill comboboxes
        jComboBoxSubNum.setEditor(new MyComboBoxEditor(jTextSubNum));
        jComboBoxAlphaThreshold.setEditor(new MyComboBoxEditor(jTextAlphaThreshold));
        jComboBoxHiMedThreshold.setEditor(new MyComboBoxEditor(jTextHiMedThreshold));
        jComboBoxMedLowThreshold.setEditor(new MyComboBoxEditor(jTextMedLowThreshold));

        for (int i=0; i<256; i++) {
            String s = Integer.toString(i);
            jComboBoxAlphaThreshold.addItem(s);
            jComboBoxHiMedThreshold.addItem(s);
            jComboBoxMedLowThreshold.addItem(s);
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

        // console
        Font f = new Font("Monospaced", Font.PLAIN, fontSize );
        console.setFont(f);

        // popup menu
        getJPopupMenu();
        MouseListener popupListener = new PopupListener();
        console.addMouseListener(popupListener);
        setVisible(true);

        print(APP_NAME_AND_VERSION + " - a converter from Blu-Ray/HD-DVD SUP to DVD SUB/IDX and more\n");
        print(AUTHOR_AND_DATE + "\n");
        print("Official thread at Doom9: http://forum.doom9.org/showthread.php?t=145277\n\n");
        flush();
    }

    void addTransferHandler(TransferHandler transferHandler) {
        setTransferHandler(transferHandler);
    }

    /**
     * Print string to console window
     * @param message String to print
     */
    public void print(String message) {
        Document doc = console.getDocument();
        int length = doc.getLength();
        try {
            doc.insertString(length, message, null);
        } catch (BadLocationException ex) {
            //
        }
    }

    /**
     * Force console to display last line
     */
    public void flush() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                console.setCaretPosition(console.getDocument().getLength());
            }
        });
    }

    /**
     * Print text to output pane
     * @param message text to print
     */
    public void printOut(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                print(message);
            }
        });
    }

    /**
     * Print text to error pane
     * @param message text to print
     */
    public void printErr(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                print(message);
            }
        });
    }

    /**
     * Print warning
     * @param message text to print
     */
    public void printWarn(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                print(message);
            }
        });
    }

    /**
     * Print error and show error dialog
     * @param message error message to display
     */
    public void error (String message) {
        Core.printErr(message);
        JOptionPane.showMessageDialog(mainFrame, message, "Error!", JOptionPane.WARNING_MESSAGE);
    }

    void setConsoleText(String text) {
        console.setText(text);
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
                jPanelTrg.setImage(Core.getTrgImage());
                printInfoTrg(index);
                jLayoutPane.repaint();
            }
        });
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
                jPanelSrc.setImage(img);
                printInfoSrc(index);
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
     * Write a string to the system clipboard.
     * @param str String to copy to the system clipboard
     */
    private void setClipboard(String str) {
        // clear
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), (ClipboardOwner) mainFrame);
        // set
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), (ClipboardOwner) mainFrame);
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        setSize(800, 600);
        setMinimumSize(new Dimension(700, 300));
        setJMenuBar(getjMenuBar());
        setContentPane(getJContentPane());
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            GridBagConstraints gridBagPanelUp = new GridBagConstraints();
            gridBagPanelUp.gridx = 0;
            gridBagPanelUp.gridy = 0;
            gridBagPanelUp.anchor = GridBagConstraints.WEST;
            gridBagPanelUp.fill = GridBagConstraints.HORIZONTAL;
            gridBagPanelUp.insets = new Insets(0, 4, 0, 4);
            gridBagPanelUp.weightx = 0.0;
            gridBagPanelUp.ipadx = 0;
            gridBagPanelUp.weighty = 0.0;

            GridBagConstraints gridBagPanelUp2 = new GridBagConstraints();
            gridBagPanelUp2.gridx = 0;
            gridBagPanelUp2.gridy = 1;
            gridBagPanelUp2.anchor = GridBagConstraints.NORTHWEST;
            gridBagPanelUp2.fill = GridBagConstraints.HORIZONTAL;
            gridBagPanelUp2.insets = new Insets(4, 0, 0, 0);
            gridBagPanelUp2.weightx = 0.0;
            gridBagPanelUp2.weighty = 0.0;

            GridBagConstraints gridBagScrollPaneSup = new GridBagConstraints();
            gridBagScrollPaneSup.gridx = 0;
            gridBagScrollPaneSup.gridy = 2;
            gridBagScrollPaneSup.fill = GridBagConstraints.BOTH;
            gridBagScrollPaneSup.anchor = GridBagConstraints.NORTHWEST;
            gridBagScrollPaneSup.weightx = 1.0;
            gridBagScrollPaneSup.weighty = 1.0;

            GridBagConstraints gridBagPanelMid = new GridBagConstraints();
            gridBagPanelMid.gridx = 0;
            gridBagPanelMid.gridy = 3;
            gridBagPanelMid.fill = GridBagConstraints.HORIZONTAL;
            gridBagPanelMid.anchor = GridBagConstraints.WEST;
            gridBagPanelMid.gridwidth = 1;
            gridBagPanelMid.weighty = 0.0;
            gridBagPanelMid.weightx = 0.0;

            GridBagConstraints gridBagScrollPaneSub = new GridBagConstraints();
            gridBagScrollPaneSub.gridx = 0;
            gridBagScrollPaneSub.gridy = 4;
            gridBagScrollPaneSub.fill = GridBagConstraints.BOTH;
            gridBagScrollPaneSub.anchor = GridBagConstraints.NORTHWEST;
            gridBagScrollPaneSub.weightx = 1.0;
            gridBagScrollPaneSub.weighty = 1.0;

            GridBagConstraints gridBagPanelDown = new GridBagConstraints();
            gridBagPanelDown.gridx = 0;
            gridBagPanelDown.gridy = 5;
            gridBagPanelDown.anchor = GridBagConstraints.SOUTHWEST;
            gridBagPanelDown.fill = GridBagConstraints.BOTH;

            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.setPreferredSize(new Dimension(800, 600));
            jContentPane.add(getJPanelUp(), gridBagPanelUp);
            jContentPane.add(getJPanelUp2(), gridBagPanelUp2);
            jContentPane.add(getJScrollPaneSup(), gridBagScrollPaneSup);
            jContentPane.add(getJPanelMid(), gridBagPanelMid);
            jContentPane.add(getJScrollPaneSub(), gridBagScrollPaneSub);
            jContentPane.add(getJPanelDown(), gridBagPanelDown);
        }
        return jContentPane;
    }

    private JScrollPane getJScrollPaneSup() {
        if (jScrollPaneSrc == null) {
            jScrollPaneSrc = new JScrollPane();
            jScrollPaneSrc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jScrollPaneSrc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            jScrollPaneSrc.setViewportView(getJPanelSup());
        }
        return jScrollPaneSrc;
    }

    private JScrollPane getJScrollPaneSub() {
        if (jScrollPaneTrg == null) {
            jScrollPaneTrg = new JScrollPane();
            jScrollPaneTrg.setViewportView(getJPanelSub());
        }
        return jScrollPaneTrg;
    }

    private JPanel getJPanelUp() {
        if (jPanelUp == null) {
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
            JLabel jLabelFilter = new JLabel();
            jLabelFilter.setPreferredSize(new Dimension(120, 20));
            jLabelFilter.setText("Filter");
            GridBagConstraints gridBagLabelPalette = new GridBagConstraints();
            gridBagLabelPalette.insets = new Insets(0, 4, 0, 4);
            gridBagLabelPalette.anchor = GridBagConstraints.WEST;
            gridBagLabelPalette.gridx = 6;
            gridBagLabelPalette.gridy = 0;
            JLabel jLabelPalette = new JLabel();
            jLabelPalette.setPreferredSize(new Dimension(120, 20));
            jLabelPalette.setText("Palette");
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
            JLabel jLabelOutFormat = new JLabel();
            jLabelOutFormat.setText("Output Format");
            jLabelOutFormat.setPreferredSize(new Dimension(120, 20));
            JLabel jLabelMedLowThr = new JLabel();
            jLabelMedLowThr.setText("Med/Low Threshold");
            jLabelMedLowThr.setPreferredSize(new Dimension(100, 20));
            JLabel jLabelHiMedThr = new JLabel();
            jLabelHiMedThr.setText("Hi/Med Threshold");
            jLabelHiMedThr.setPreferredSize(new Dimension(100, 20));
            JLabel jLabelAlphaThr = new JLabel();
            jLabelAlphaThr.setText("Alpha Threshold");
            jLabelAlphaThr.setPreferredSize(new Dimension(100, 20));

            jPanelUp = new JPanel();
            jPanelUp.setLayout(new GridBagLayout());
            jPanelUp.setPreferredSize(new Dimension(600, 40));
            jPanelUp.setMinimumSize(new Dimension(600, 40));
            jPanelUp.setMaximumSize(new Dimension(600, 40));
            jPanelUp.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            jPanelUp.add(jLabelSubNum, gridBagLabelSubNum);
            jPanelUp.add(getJComboBoxSubNum(), gridBagComboBoxSubNum);
            jPanelUp.add(jLabelAlphaThr, gridBagLabelAlphaThr);
            jPanelUp.add(getJComboBoxAlphaThreshold(), gridBagComboBoxAlphaThr);
            jPanelUp.add(jLabelMedLowThr, gridBagLabelMedLowThr);
            jPanelUp.add(getJComboBoxMedLowThreshold(), gridBagComboBoxMedLowThr);
            jPanelUp.add(jLabelHiMedThr, gridBagLabelHiMedThr);
            jPanelUp.add(getJComboBoxHiMedThreshold(), gridBagJComboBoxHiMedThr);
            jPanelUp.add(jLabelOutFormat, gridBagLabelOutFormat);
            jPanelUp.add(getJComboBoxOutputFormat(), gridBagComboBoxOutFormat);
            jPanelUp.add(getJComboBoxPalette(), gridBagComboPalette);
            jPanelUp.add(jLabelPalette, gridBagLabelPalette);
            jPanelUp.add(jLabelFilter, gridBagLabelFilter);
            jPanelUp.add(getJComboBoxFilter(), gridBagComboFilter);
        }
        return jPanelUp;
    }

    private JPanel getJPanelDown() {
        if (jPanelDown == null) {
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
            jPanelDown = new JPanel();
            jPanelDown.setLayout(new GridBagLayout());
            jPanelDown.setPreferredSize(new Dimension(300, 150));
            jPanelDown.setMinimumSize(new Dimension(300, 150));
            jPanelDown.add(getJPanelLayout(), gridBagLayout);
            jPanelDown.add(getJScrollPaneConsole(), gridBagConsole);
        }
        return jPanelDown;
    }

    private JPanel getJPanelSup() {
        if (jPanelSrc == null) {
            jPanelSrc = new GfxPane();
        }
        return jPanelSrc;
    }

    private JPanel getJPanelSub() {
        if (jPanelTrg == null) {
            jPanelTrg = new GfxPane();
        }
        return jPanelTrg;
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

    /**
     * Update info string for source window
     * @param index caption index
     */
    private void printInfoSrc(int index) {
        jLabelInfoSrc.setText(Core.getSrcInfoStr(index));
    }

    /**
     * Update info string for target window
     * @param index caption index
     */
    private void printInfoTrg(int index) {
        jLabelInfoTrg.setText(Core.getTrgInfoStr(index));
    }

    private JPanel getJPanelUp2() {
        if (jPanelUp2 == null) {
            GridBagConstraints gridBagLabelInfoSup = new GridBagConstraints();
            gridBagLabelInfoSup.anchor = GridBagConstraints.WEST;
            gridBagLabelInfoSup.insets = new Insets(4, 8, 2, 8);
            gridBagLabelInfoSup.gridwidth = 1;
            gridBagLabelInfoSup.gridx = 0;
            gridBagLabelInfoSup.gridy = 0;
            gridBagLabelInfoSup.weightx = 1.0;
            gridBagLabelInfoSup.fill = GridBagConstraints.HORIZONTAL;

            jLabelInfoSrc = new JLabel();
            jLabelInfoSrc.setHorizontalAlignment(SwingConstants.LEFT);
            jLabelInfoSrc.setHorizontalTextPosition(SwingConstants.LEFT);

            jPanelUp2 = new JPanel();
            jPanelUp2.setLayout(new GridBagLayout());
            jPanelUp2.setPreferredSize(new Dimension(600, 20));
            jPanelUp2.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.lightGray));
            jPanelUp2.add(jLabelInfoSrc, gridBagLabelInfoSup);
        }
        return jPanelUp2;
    }

    private JPanel getJPanelMid() {
        if (jPanelMid == null) {
            GridBagConstraints gridBagLabelSubInfo = new GridBagConstraints();
            gridBagLabelSubInfo.gridx = 0;
            gridBagLabelSubInfo.weightx = 1.0;
            gridBagLabelSubInfo.insets = new Insets(4, 8, 2, 8);
            gridBagLabelSubInfo.fill = GridBagConstraints.HORIZONTAL;
            gridBagLabelSubInfo.anchor = GridBagConstraints.WEST;
            gridBagLabelSubInfo.gridy = 0;
            jLabelInfoTrg = new JLabel();
            jLabelInfoTrg.setHorizontalTextPosition(SwingConstants.LEFT);
            jLabelInfoTrg.setHorizontalAlignment(SwingConstants.LEFT);
            jPanelMid = new JPanel();
            jPanelMid.setLayout(new GridBagLayout());
            jPanelMid.setPreferredSize(new Dimension(300, 20));
            jPanelMid.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            jPanelMid.add(jLabelInfoTrg, gridBagLabelSubInfo);
        }
        return jPanelMid;
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

    private JMenu getJMenuPrefs() {
        if (jMenuPrefs == null) {
            jMenuPrefs = new JMenu();
            jMenuPrefs.setText("Settings");
            jMenuPrefs.setMnemonic('s');
            jMenuPrefs.add(getJMenuItemConversionSettings());
            jMenuPrefs.add(getJMenuItemSwapCrCb());
            jMenuPrefs.add(getJMenuItemFixInvisibleFrames());
            jMenuPrefs.add(getJMenuItemVerbatimOutput());
        }
        return jMenuPrefs;
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
    void enableVobsubStuff(boolean b) {
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

            JOptionPane.showMessageDialog(mainFrame,
                    s + "\nCheck the log for details",
                    "Warning!", JOptionPane.WARNING_MESSAGE);
        }
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

    void setComboBoxOutFormatEnabled(boolean enable) {
        jComboBoxOutputFormat.setEnabled(enable);
    }

    void setMenuItemExitEnabled(boolean enable) {
        jMenuItemQuit.setEnabled(enable);
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

    void addRecentFilesMenuItemActionListener(ActionListener actionListener) {
        recentFilesMenuActionListener = actionListener;
        for(int i=0; i < jMenuRecentFiles.getItemCount(); i++) {
            jMenuRecentFiles.getItem(i).addActionListener(recentFilesMenuActionListener);
        }
    }

    private JMenu getJMenuItemRecentFiles() {
        if (jMenuRecentFiles == null) {
            jMenuRecentFiles = new JMenu();
            jMenuRecentFiles.setText("Recent Files");
            jMenuRecentFiles.setMnemonic('r');
        }
        return jMenuRecentFiles;
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
        jPanelTrg.setImage(null);
        jPanelSrc.setImage(null);

        jLabelInfoTrg.setText("");
        jLabelInfoSrc.setText("");
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

    private JScrollPane getJScrollPaneConsole() {
        if (jScrollPaneConsole == null) {
            jScrollPaneConsole = new JScrollPane();
            jScrollPaneConsole.setViewportView(getConsole());
        }
        return jScrollPaneConsole;
    }

    private JPopupMenu getJPopupMenu() {
        if (jPopupMenu == null) {
            jPopupMenu = new JPopupMenu();
            jPopupMenu.add(getJPopupMenuItemCopy());
            jPopupMenu.add(getJPopupMenuItemClear());
            //jPopupMenu.setVisible(false);
        }
        return jPopupMenu;
    }

    private JMenuItem getJPopupMenuItemCopy() {
        if (jPopupMenuItemCopy == null) {
            jPopupMenuItemCopy = new JMenuItem();
            jPopupMenuItemCopy.setText("Copy");
            jPopupMenuItemCopy.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String s = console.getSelectedText();
                    try {
                        if ( s!= null) {
                            setClipboard(s);
                        }
                    } catch (OutOfMemoryError ex) {
                        JOptionPane.showMessageDialog(mainFrame,"Out of heap! Use -Xmx256m to increase heap! ","Error!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        }
        return jPopupMenuItemCopy;
    }

    private JMenuItem getJPopupMenuItemClear() {
        if (jPopupMenuItemClear == null) {
            jPopupMenuItemClear = new JMenuItem();
            jPopupMenuItemClear.setText("Clear");  // Generated
            jPopupMenuItemClear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    console.setText("");
                }
            });
        }
        return jPopupMenuItemClear;
    }

    private JTextArea getConsole() {
        if (console == null) {
            console = new JTextArea();
            console.setEditable(false);
        }
        return console;
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

    class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                boolean canCopy = (console.getSelectedText() != null);
                jPopupMenuItemCopy.setEnabled(canCopy);
                jPopupMenu.show(console,e.getX(), e.getY());
            }
        }
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
                            EditDialog ed = new EditDialog(mainFrame);
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

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
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
}
