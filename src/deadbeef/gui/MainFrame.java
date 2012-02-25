package deadbeef.gui;


import deadbeef.bitmap.Palette;
import deadbeef.core.*;
import deadbeef.utils.ToolBox;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static deadbeef.core.Constants.*;

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
 * Main GUI class for BDSup2Sub.
 *
 * @author 0xdeadbeef
 */
public class MainFrame extends JFrame implements ClipboardOwner {

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane;
    private JScrollPane jScrollPaneSrc;
    private JScrollPane jScrollPaneTrg;
    private JPanel jPanelUp;
    private JPanel jPanelDown;
    private GfxPane jPanelSrc;
    private GfxPane jPanelTrg;
    private JComboBox jComboBoxSubNum;
    private JComboBox jComboBoxAlphaThr;
    private JComboBox jComboBoxHiMedThr;
    private JComboBox jComboBoxMedLowThr;
    private JComboBox jComboBoxOutFormat;
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
    private JMenuItem jMenuItemExit;
    private JMenu jMenuHelp;
    private JMenuItem jMenuItemHelp;
    private JMenu jMenuPrefs;
    private JMenuItem jMenuItemEditColors;
    private JMenuItem jMenuItemEditCurColors;
    private JMenuItem jMenuItemEditFramePalAlpha;
    private JMenuItem jMenuItemConversionSettings;
    private JCheckBoxMenuItem jMenuItemSwapCrCb;
    private JCheckBoxMenuItem jMenuItemVerbatim;
    private JCheckBoxMenuItem jMenuItemFixAlpha;
    private JMenu jMenuEdit;
    private JMenuItem jMenuItemEditFrame;
    private JMenuItem jMenuItemBatchMove;
    private JMenuItem jMenuItemResetCrop;
    private JPopupMenu jPopupMenu;
    private JMenuItem jPopupMenuItemCopy;
    private JMenuItem jPopupMenuItemClear;
    private JScrollPane jScrollPaneConsole;
    private JTextArea console;
    private EditPane jLayoutPane;
    private JComboBox jComboBoxPalette;
    private JComboBox jComboBoxFilter;
    private JTextField jTextSubNum;
    private JTextField jTextAlphaThr;
    private JTextField jTextHiMedThr;
    private JTextField jTextMedLowThr;


    /** semaphore for synchronization of threads */
    private final Object threadSemaphore = new Object();
    /** reference to this frame (to allow access to "this" from inner classes */
    private JFrame mainFrame;
    /** current caption index */
    private int subIndex;
    /** path to load SUP from */
    private String loadPath;
    /** file name of last file loaded/stored */
    private String saveFilename;
    /** path to save SUP to */
    private String savePath;
    /** path to load color profiles from */
    private String colorPath;
    /** transfer handler for Drag'n'Drop support */
    private TransferHandler thandler;

    /** font size for output console */
    private int fontSize = 12;
    //private static final int maxDocSize = 1000000; // to work around bad TextPane performance
    /** background color for errors */
    private Color errBgnd = new Color(0xffe1acac);
    /** background color for ok */
    private Color okBgnd = UIManager.getColor("TextField.background");


    public MainFrame(String fname) {
        this();
        loadPath = fname;
        load(fname);
    }

    public MainFrame() {
        super(APP_NAME_AND_VERSION);

        jTextSubNum = new JTextField();
        jTextAlphaThr = new JTextField();
        jTextHiMedThr = new JTextField();
        jTextMedLowThr = new JTextField();

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

        ClassLoader loader = MainFrame.class.getClassLoader();
        Image img = Toolkit.getDefaultToolkit().getImage(loader.getResource("icon_32.png"));
        setIconImage(img);

        Core.setMainFrame(this);
        Core.init(this);

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

        loadPath = Core.props.get("loadPath", "");
        //savePath = Core.props.get("savePath", "");
        colorPath = Core.props.get("colorPath", "");
        mainFrame = this;

        subIndex = 0;

        updateRecentFilesMenu();

        // fill comboboxes
        jComboBoxSubNum.setEditor(new MyComboBoxEditor(jTextSubNum));
        jComboBoxAlphaThr.setEditor(new MyComboBoxEditor(jTextAlphaThr));
        jComboBoxHiMedThr.setEditor(new MyComboBoxEditor(jTextHiMedThr));
        jComboBoxMedLowThr.setEditor(new MyComboBoxEditor(jTextMedLowThr));

        for (int i=0; i<256; i++) {
            String s = Integer.toString(i);
            jComboBoxAlphaThr.addItem(s);
            jComboBoxHiMedThr.addItem(s);
            jComboBoxMedLowThr.addItem(s);
        }
        jComboBoxAlphaThr.setSelectedIndex(Core.getAlphaThr());
        jComboBoxHiMedThr.setSelectedIndex(Core.getLumThr()[0]);
        jComboBoxMedLowThr.setSelectedIndex(Core.getLumThr()[1]);

        for (OutputMode m : OutputMode.values()) {
            jComboBoxOutFormat.addItem(m.toString());
        }
        jComboBoxOutFormat.setSelectedIndex(Core.getOutputMode().ordinal());

        for (PaletteMode m : PaletteMode.values()) {
            jComboBoxPalette.addItem(m.toString());
        }
        jComboBoxPalette.setSelectedIndex(Core.getPaletteMode().ordinal());

        for (ScalingFilter s : ScalingFilter.values()) {
            jComboBoxFilter.addItem(s.toString());
        }
        jComboBoxFilter.setSelectedIndex(Core.getScalingFilter().ordinal());

        jMenuItemVerbatim.setSelected(Core.getVerbatim());
        jMenuItemFixAlpha.setSelected(Core.getFixZeroAlpha());

        // console
        Font f = new Font("Monospaced", Font.PLAIN, fontSize );
        console.setFont(f);

        // popup menu
        getJPopupMenu();
        MouseListener popupListener = new PopupListener();
        console.addMouseListener(popupListener);
        setVisible(true);

        // drag'n'drop
        thandler = new TransferHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support))
                    return false;

                Transferable t = support.getTransferable();

                try {
                    List<File> flist = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
                    load(flist.get(0).getAbsolutePath());
                } catch (UnsupportedFlavorException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }

                return true;
            }
        };
        setTransferHandler(thandler);

        print(APP_NAME_AND_VERSION + " - a converter from Blu-Ray/HD-DVD SUP to DVD SUB/IDX and more\n");
        print(AUTHOR_AND_DATE + "\n");
        print("Official thread at Doom9: http://forum.doom9.org/showthread.php?t=145277\n\n");
        flush();
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

    /**
     * Update all components belonging to the target window
     * @param index caption index
     */
    private void refreshTrgFrame(final int index) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jLayoutPane.setDim(Core.getTrgWidth(index), Core.getTrgHeight(index));
                jLayoutPane.setOffsets(Core.getTrgOfsX(index), Core.getTrgOfsY(index));
                jLayoutPane.setCropOfsY(Core.getCropOfsY());
                jLayoutPane.setImage(Core.getTrgImage(),Core.getTrgImgWidth(index), Core.getTrgImgHeight(index));
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
    private void refreshSrcFrame(final int index) {
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
    private void exit(int code) {
        if (code == 0) {
            // store width and height
            Dimension d = getSize();
            if (this.getExtendedState() != MainFrame.MAXIMIZED_BOTH) {
                Core.props.set("frameWidth", d.width);
                Core.props.set("frameHeight", d.height);
                // store frame pos
                Point p = getLocation();
                Core.props.set("framePosX", p.x);
                Core.props.set("framePosY", p.y);
            }
            // store load/save path
            Core.props.set("loadPath", loadPath);
            //Core.props.set("savePath", savePath);
            Core.props.set("colorPath", colorPath);
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
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), (ClipboardOwner)mainFrame);
        // set
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), (ClipboardOwner)mainFrame);
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        setSize(800, 600);
        setMinimumSize(new Dimension(700,300));
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
            jPanelUp.add(getJComboBoxAlphaThr(), gridBagComboBoxAlphaThr);
            jPanelUp.add(jLabelMedLowThr, gridBagLabelMedLowThr);
            jPanelUp.add(getJComboBoxMedLowThr(), gridBagComboBoxMedLowThr);
            jPanelUp.add(jLabelHiMedThr, gridBagLabelHiMedThr);
            jPanelUp.add(getJComboBoxHiMedThr(), gridBagJComboBoxHiMedThr);
            jPanelUp.add(jLabelOutFormat, gridBagLabelOutFormat);
            jPanelUp.add(getJComboBoxOutFormat(), gridBagComboBoxOutFormat);
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
            jComboBoxSubNum.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (Core.isReady()) {
                        int num = Core.getNumFrames();
                        int idx;
                        try {
                            idx = Integer.parseInt(jComboBoxSubNum.getSelectedItem().toString())-1;
                        } catch (NumberFormatException ex) {
                            idx = subIndex; // invalid number -> keep old value
                        }

                        if (idx < 0) {
                            idx = 0;
                        }
                        if (idx >= num) {
                            idx = num-1;
                        }
                        subIndex = idx;
                        jComboBoxSubNum.setSelectedIndex(subIndex);

                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshSrcFrame(subIndex);
                                        refreshTrgFrame(subIndex);
                                    } catch (CoreException ex) {
                                        error(ex.getMessage());
                                    } catch (Exception ex) {
                                        ToolBox.showException(ex);
                                        exit(4);
                                    }

                                } } }).start();
                    }
                }
            });
            jTextSubNum.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (Core.isReady()) {
                        int idx = ToolBox.getInt(jTextSubNum.getText())-1;
                        if (idx < 0 || idx >= Core.getNumFrames()) {
                            jTextSubNum.setBackground(errBgnd);
                        } else {
                            subIndex = idx;
                            (new Thread() {
                                @Override
                                public void run() {
                                    synchronized (threadSemaphore) {
                                        try {
                                            Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                            refreshSrcFrame(subIndex);
                                            refreshTrgFrame(subIndex);
                                        } catch (CoreException ex) {
                                            error(ex.getMessage());
                                        } catch (Exception ex) {
                                            ToolBox.showException(ex);
                                            exit(4);
                                        }

                                    } } }).start();
                            jTextSubNum.setBackground(okBgnd);
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
            });
        }
        return jComboBoxSubNum;
    }

    private JComboBox getJComboBoxAlphaThr() {
        if (jComboBoxAlphaThr == null) {
            jComboBoxAlphaThr = new JComboBox();
            jComboBoxAlphaThr.setEnabled(false);
            jComboBoxAlphaThr.setEditable(true);
            jComboBoxAlphaThr.setToolTipText("Set alpha threshold");
            jComboBoxAlphaThr.setPreferredSize(new Dimension(100, 20));
            jComboBoxAlphaThr.setMinimumSize(new Dimension(80, 20));
            jComboBoxAlphaThr.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (Core.isReady()) {
                        int idx;
                        try {
                            idx = Integer.parseInt(jComboBoxAlphaThr.getSelectedItem().toString());
                        } catch (NumberFormatException ex) {
                            idx = Core.getAlphaThr(); // invalid number -> keep old value
                        }

                        if (idx < 0) {
                            idx = 0;
                        }
                        if (idx > 255) {
                            idx = 255;
                        }

                        Core.setAlphaThr(idx);
                        jComboBoxAlphaThr.setSelectedIndex(Core.getAlphaThr());

                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshTrgFrame(subIndex);
                                    } catch (CoreException ex) {
                                        error(ex.getMessage());
                                    } catch (Exception ex) {
                                        ToolBox.showException(ex);
                                        exit(4);
                                    }
                                } } }).start();
                    }
                }
            });
            jTextAlphaThr.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (Core.isReady()) {
                        int idx = ToolBox.getInt(jTextAlphaThr.getText());
                        if (idx < 0 || idx > 255) {
                            jTextAlphaThr.setBackground(errBgnd);
                        } else {
                            Core.setAlphaThr(idx);
                            (new Thread() {
                                @Override
                                public void run() {
                                    synchronized (threadSemaphore) {
                                        try {
                                            Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                            refreshTrgFrame(subIndex);
                                        } catch (CoreException ex) {
                                            error(ex.getMessage());
                                        } catch (Exception ex) {
                                            ToolBox.showException(ex);
                                            exit(4);
                                        }
                                    } } }).start();
                            jTextAlphaThr.setBackground(okBgnd);
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
            });

        }
        return jComboBoxAlphaThr;
    }

    private JComboBox getJComboBoxHiMedThr() {
        if (jComboBoxHiMedThr == null) {
            jComboBoxHiMedThr = new JComboBox();
            jComboBoxHiMedThr.setEditable(true);
            jComboBoxHiMedThr.setEnabled(false);
            jComboBoxHiMedThr.setPreferredSize(new Dimension(100, 20));
            jComboBoxHiMedThr.setMinimumSize(new Dimension(80, 20));
            jComboBoxHiMedThr.setToolTipText("Set medium/high luminance threshold");
            jComboBoxHiMedThr.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (Core.isReady()) {
                        int lumThr[] = Core.getLumThr();
                        int idx;
                        try {
                            idx = Integer.parseInt(jComboBoxHiMedThr.getSelectedItem().toString());
                        } catch (NumberFormatException ex) {
                            idx = lumThr[0]; // invalid number -> keep old value
                        }

                        if (idx <= lumThr[1]) { // must be greater than med/low threshold
                            idx = lumThr[1] + 1;
                        }

                        if (idx < 0) {
                            idx = 0;
                        }
                        if (idx > 255) {
                            idx = 255;
                        }

                        lumThr[0] = idx;
                        Core.setLumThr(lumThr);
                        jComboBoxHiMedThr.setSelectedIndex(Core.getLumThr()[0]);

                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshTrgFrame(subIndex);
                                    } catch (CoreException ex) {
                                        error(ex.getMessage());
                                    } catch (Exception ex) {
                                        ToolBox.showException(ex);
                                        exit(4);
                                    }

                                } } }).start();
                    }
                }
            });
            jTextHiMedThr.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (Core.isReady()) {
                        int lumThr[] = Core.getLumThr();
                        int idx = ToolBox.getInt(jTextHiMedThr.getText());
                        if (idx < 0 || idx > 255 | idx <= lumThr[1]) {
                            jTextHiMedThr.setBackground(errBgnd);
                        } else {
                            lumThr[0] = idx;
                            Core.setLumThr(lumThr);
                            (new Thread() {
                                @Override
                                public void run() {
                                    synchronized (threadSemaphore) {
                                        try {
                                            Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                            refreshTrgFrame(subIndex);
                                        } catch (CoreException ex) {
                                            error(ex.getMessage());
                                        } catch (Exception ex) {
                                            ToolBox.showException(ex);
                                            exit(4);
                                        }

                                    } } }).start();
                            jTextHiMedThr.setBackground(okBgnd);
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
            });

        }
        return jComboBoxHiMedThr;
    }

    private JComboBox getJComboBoxMedLowThr() {
        if (jComboBoxMedLowThr == null) {
            jComboBoxMedLowThr = new JComboBox();
            jComboBoxMedLowThr.setEditable(true);
            jComboBoxMedLowThr.setEnabled(false);
            jComboBoxMedLowThr.setToolTipText("Set low/medium luminance threshold");
            jComboBoxMedLowThr.setPreferredSize(new Dimension(100, 20));
            jComboBoxMedLowThr.setMinimumSize(new Dimension(80, 20));
            jComboBoxMedLowThr.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (Core.isReady()) {
                        int lumThr[] = Core.getLumThr();
                        int idx;
                        try {
                            idx = Integer.parseInt(jComboBoxMedLowThr.getSelectedItem().toString());
                        } catch (NumberFormatException ex) {
                            idx = lumThr[1]; // invalid number -> keep old value
                        }

                        if (idx >= lumThr[0]) { // must be smaller than med/high threshold
                            idx = lumThr[0] - 1;
                        }

                        if (idx < 0) {
                            idx = 0;
                        }
                        if (idx > 255) {
                            idx = 255;
                        }

                        lumThr[1] = idx;
                        Core.setLumThr(lumThr);

                        final int index = idx;
                        jComboBoxMedLowThr.setSelectedIndex(index);

                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshTrgFrame(subIndex);
                                    } catch (CoreException ex) {
                                        error(ex.getMessage());
                                    } catch (Exception ex) {
                                        ToolBox.showException(ex);
                                        exit(4);
                                    }

                                } } }).start();
                    }
                }
            });
            jTextMedLowThr.getDocument().addDocumentListener(new DocumentListener() {
                private void check() {
                    if (Core.isReady()) {
                        int lumThr[] = Core.getLumThr();
                        int idx = ToolBox.getInt(jTextMedLowThr.getText());
                        if (idx < 0 || idx > 255 | idx >= lumThr[0])
                            jTextMedLowThr.setBackground(errBgnd);
                        else {
                            lumThr[1] = idx;
                            Core.setLumThr(lumThr);
                            (new Thread() {
                                @Override
                                public void run() {
                                    synchronized (threadSemaphore) {
                                        try {
                                            Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                            refreshTrgFrame(subIndex);
                                        } catch (CoreException ex) {
                                            error(ex.getMessage());
                                        } catch (Exception ex) {
                                            ToolBox.showException(ex);
                                            exit(4);
                                        }

                                    } } }).start();
                            jTextMedLowThr.setBackground(okBgnd);
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
            });

        }
        return jComboBoxMedLowThr;
    }

    private JComboBox getJComboBoxOutFormat() {
        if (jComboBoxOutFormat == null) {
            jComboBoxOutFormat = new JComboBox();
            jComboBoxOutFormat.setEnabled(false);
            jComboBoxOutFormat.setToolTipText("Select export format");
            jComboBoxOutFormat.setPreferredSize(new Dimension(120, 20));
            jComboBoxOutFormat.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (Core.isReady()) {
                        int idx = jComboBoxOutFormat.getSelectedIndex();
                        for (OutputMode m : OutputMode.values()) {
                            if (idx == m.ordinal()) {
                                Core.setOutputMode(m);
                                break;
                            }
                        }

                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshTrgFrame(subIndex);
                                        if (Core.getOutputMode() == OutputMode.VOBSUB || Core.getOutputMode() == OutputMode.SUPIFO)
                                            enableVobsubStuff(true);
                                        else
                                            enableVobsubStuff(false);
                                    } catch (CoreException ex) {
                                        error(ex.getMessage());
                                    } catch (Exception ex) {
                                        ToolBox.showException(ex);
                                        exit(4);
                                    }

                                } } }).start();
                    }
                }
            });
        }
        return jComboBoxOutFormat;
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
            jMenuFile.add(getJMenuItemExit());
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
            jMenuPrefs.add(getJMenuItemFixAlpha());
            jMenuPrefs.add(getJMenuItemVerbatim());
        }
        return jMenuPrefs;
    }

    private JMenu getJMenuEdit() {
        if (jMenuEdit == null) {
            jMenuEdit = new JMenu();
            jMenuEdit.setText("Edit");
            jMenuEdit.setMnemonic('e');
            jMenuEdit.add(getJMenuItemEditFrame());
            jMenuEdit.add(getJMenuItemEditColors());
            jMenuEdit.add(getJMenuItemEditCurColors());
            jMenuEdit.add(getJMenuItemEditFramePalAlpha());
            jMenuEdit.add(getJMenuItemBatchMove());
            jMenuEdit.add(getJMenuItemResetCrop());
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
            jMenuItemHelp.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Help help = new Help();
                    help.setLocation(getX()+30,getY()+30);
                    help.setSize(800,600);
                    help.setVisible(true);
                }
            });
        }
        return jMenuItemHelp;
    }

    private JMenuItem getJMenuItemEditFrame() {
        if (jMenuItemEditFrame == null) {
            jMenuItemEditFrame = new JMenuItem();
            jMenuItemEditFrame.setText("Edit Frame");
            jMenuItemEditFrame.setMnemonic('e');
            jMenuItemEditFrame.setEnabled(false);
            jMenuItemEditFrame.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (Core.isReady()) {
                        EditDialog ed = new EditDialog(mainFrame);
                        ed.setIndex(subIndex);
                        ed.setVisible(true);
                        subIndex = ed.getIndex();
                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshSrcFrame(subIndex);
                                        refreshTrgFrame(subIndex);
                                        jComboBoxSubNum.setSelectedIndex(subIndex);
                                    } catch (CoreException ex) {
                                        error(ex.getMessage());
                                    } catch (Exception ex) {
                                        ToolBox.showException(ex);
                                        exit(4);
                                    }
                                } } }).start();
                    }
                }
            });
        }
        return jMenuItemEditFrame;
    }

    private JMenuItem getJMenuItemBatchMove() {
        if (jMenuItemBatchMove == null) {
            jMenuItemBatchMove = new JMenuItem();
            jMenuItemBatchMove.setText("Move all captions");
            jMenuItemBatchMove.setMnemonic('m');
            jMenuItemBatchMove.setEnabled(false);
            jMenuItemBatchMove.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (Core.isReady()) {
                        MoveDialog ed = new MoveDialog(mainFrame);
                        ed.setCurrentSubtitleIndex(subIndex);
                        ed.setVisible(true);
                        if (Core.getMoveCaptions()) {
                            try {
                                Core.moveAllThreaded(mainFrame);
                            } catch (CoreException ex) {
                                error(ex.getMessage());
                            } catch (Exception ex) {
                                ToolBox.showException(ex);
                                exit(4);
                            }
                        }
                        subIndex = ed.getCurrentSubtitleIndex();
                        jLayoutPane.setAspectRatio(ed.getTrgRatio());
                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshSrcFrame(subIndex);
                                        refreshTrgFrame(subIndex);
                                        jComboBoxSubNum.setSelectedIndex(subIndex);
                                    } catch (CoreException ex) {
                                        error(ex.getMessage());
                                    } catch (Exception ex) {
                                        ToolBox.showException(ex);
                                        exit(4);
                                    }
                                } } }).start();
                    }
                }
            });
        }
        return jMenuItemBatchMove;
    }

    private JMenuItem getJMenuItemResetCrop() {
        if (jMenuItemResetCrop == null) {
            jMenuItemResetCrop = new JMenuItem();
            jMenuItemResetCrop.setMnemonic('r');
            jMenuItemResetCrop.setText("Reset crop offset");  // Generated
            jMenuItemResetCrop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Core.setCropOfsY(0);
                    jLayoutPane.setCropOfsY(Core.getCropOfsY());
                    jLayoutPane.repaint();
                }
            });
        }
        return jMenuItemResetCrop;
    }

    private JMenuItem getJMenuItemSwapCrCb() {
        if (jMenuItemSwapCrCb == null) {
            jMenuItemSwapCrCb = new JCheckBoxMenuItem();
            jMenuItemSwapCrCb.setText("Swap Cr/Cb");
            jMenuItemSwapCrCb.setMnemonic('s');
            jMenuItemSwapCrCb.setSelected(false);
            jMenuItemSwapCrCb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean selected = jMenuItemSwapCrCb.isSelected();
                    Core.setSwapCrCb(selected);
                    // create and show image
                    (new Thread() {
                        @Override
                        public void run() {
                            synchronized (threadSemaphore) {
                                try {
                                    if (Core.isReady()) {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshSrcFrame(subIndex);
                                        refreshTrgFrame(subIndex);
                                    }
                                } catch (CoreException ex) {
                                    error(ex.getMessage());
                                } catch (Exception ex) {
                                    ToolBox.showException(ex);
                                    exit(4);
                                }

                            } } }).start();
                }
            });
        }
        return jMenuItemSwapCrCb;
    }

    private JMenuItem getJMenuItemVerbatim() {
        if (jMenuItemVerbatim == null) {
            jMenuItemVerbatim = new JCheckBoxMenuItem();
            jMenuItemVerbatim.setText("Verbatim Output");
            jMenuItemVerbatim.setMnemonic('v');
            jMenuItemVerbatim.setSelected(false);
            jMenuItemVerbatim.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean selected = jMenuItemVerbatim.isSelected();
                    Core.setVerbatim(selected);
                }
            });
        }
        return jMenuItemVerbatim;
    }

    private JMenuItem getJMenuItemFixAlpha() {
        if (jMenuItemFixAlpha == null) {
            jMenuItemFixAlpha = new JCheckBoxMenuItem();
            jMenuItemFixAlpha.setText("Fix invisible frames");  // Generated
            jMenuItemFixAlpha.setMnemonic('f');
            jMenuItemFixAlpha.setSelected(false);
            jMenuItemFixAlpha.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean selected = jMenuItemFixAlpha.isSelected();
                    Core.setFixZeroAlpha(selected);
                }
            });
        }
        return jMenuItemFixAlpha;
    }

    private JMenuItem getJMenuItemConversionSettings() {
        if (jMenuItemConversionSettings == null) {
            jMenuItemConversionSettings = new JMenuItem();
            jMenuItemConversionSettings.setText("Conversion Settings");
            jMenuItemConversionSettings.setMnemonic('c');
            jMenuItemConversionSettings.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final Resolution rOld = Core.getOutputResolution();
                    final double fpsTrgOld = Core.getFPSTrg();
                    final boolean changeFpsOld = Core.getConvertFPS();
                    final int delayOld = Core.getDelayPTS();
                    final double fsXOld;
                    final double fsYOld;
                    if (Core.getApplyFreeScale()) {
                        fsXOld = Core.getFreeScaleX();
                        fsYOld = Core.getFreeScaleY();
                    } else {
                        fsXOld = 1.0;
                        fsYOld = 1.0;
                    }
                    // show dialog
                    ConversionDialog trans = new ConversionDialog(mainFrame);
                    trans.enableOptionMove(false);
                    trans.setVisible(true);

                    if (!trans.wasCanceled()) {
                        // create and show image
                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        if (Core.isReady()) {
                                            Core.reScanSubtitles(rOld, fpsTrgOld, delayOld, changeFpsOld,fsXOld,fsYOld);
                                            Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                            refreshTrgFrame(subIndex);
                                        }
                                    } catch (CoreException ex) {
                                        error(ex.getMessage());
                                    } catch (Exception ex) {
                                        ToolBox.showException(ex);
                                        exit(4);
                                    }
                                } } }).start();
                    }
                }
            });
        }
        return jMenuItemConversionSettings;
    }

    private JMenuItem getJMenuItemEditColors() {
        if (jMenuItemEditColors == null) {
            jMenuItemEditColors = new JMenuItem();
            //jMenuItemEditColors.setEnabled(false);
            jMenuItemEditColors.setText("Edit default DVD Palette");
            jMenuItemEditColors.setMnemonic('d');
            jMenuItemEditColors.setDisplayedMnemonicIndex(5);
            jMenuItemEditColors.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorDialog cDiag = new ColorDialog(mainFrame);
                    final String cName[] = {
                            "white","light gray","dark gray",
                            "Color 1 light","Color 1 dark",
                            "Color 2 light","Color 2 dark",
                            "Color 3 light","Color 3 dark",
                            "Color 4 light","Color 4 dark",
                            "Color 5 light","Color 5 dark",
                            "Color 6 light","Color 6 dark"
                    };
                    Color cColor[] = new Color[15];
                    Color cColorDefault[] = new Color[15];
                    for (int i=0; i < cColor.length; i++) {
                        cColor[i] = Core.getCurrentDVDPalette().getColor(i+1);
                        cColorDefault[i] = DEFAULT_DVD_PALETTE.getColor(i+1);
                    }
                    cDiag.setParameters(cName, cColor, cColorDefault);
                    cDiag.setPath(colorPath);
                    cDiag.setVisible(true);
                    if (!cDiag.wasCanceled()) {
                        cColor = cDiag.getColors();
                        colorPath = cDiag.getPath();
                        for (int i=0; i<cColor.length; i++) {
                            Core.getCurrentDVDPalette().setColor(i+1, cColor[i]);
                        }

                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        if (Core.isReady()) {
                                            Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                            refreshTrgFrame(subIndex);
                                        }
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
            });
        }
        return jMenuItemEditColors;
    }

    private JMenuItem getJMenuItemEditCurColors() {
        if (jMenuItemEditCurColors == null) {
            jMenuItemEditCurColors = new JMenuItem();
            jMenuItemEditCurColors.setEnabled(false);
            jMenuItemEditCurColors.setText("Edit imported DVD Palette");
            jMenuItemEditCurColors.setMnemonic('i');
            jMenuItemEditCurColors.setDisplayedMnemonicIndex(5);
            jMenuItemEditCurColors.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorDialog cDiag = new ColorDialog(mainFrame);
                    final String cName[] = {
                            "Color 0", "Color 1", "Color 2", "Color 3",
                            "Color 4", "Color 5", "Color 6", "Color 7",
                            "Color 8", "Color 9", "Color 10", "Color 11",
                            "Color 12", "Color 13", "Color 14", "Color 15",
                    };
                    Color cColor[] = new Color[16];
                    Color cColorDefault[] = new Color[16];
                    for (int i=0; i < cColor.length; i++) {
                        cColor[i] = Core.getCurSrcDVDPalette().getColor(i);
                        cColorDefault[i] = Core.getDefSrcDVDPalette().getColor(i);
                    }
                    cDiag.setParameters(cName, cColor, cColorDefault);
                    cDiag.setPath(colorPath);
                    cDiag.setVisible(true);
                    if (!cDiag.wasCanceled()) {
                        cColor = cDiag.getColors();
                        colorPath = cDiag.getPath();
                        Palette p = new Palette(cColor.length, true);
                        for (int i=0; i<cColor.length; i++) {
                            p.setColor(i, cColor[i]);
                        }
                        Core.setCurSrcDVDPalette(p);

                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        if (Core.isReady()) {
                                            Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                            refreshSrcFrame(subIndex);
                                            refreshTrgFrame(subIndex);
                                        }
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
            });
        }
        return jMenuItemEditCurColors;
    }

    private JMenuItem getJMenuItemEditFramePalAlpha() {
        if (jMenuItemEditFramePalAlpha == null) {
            jMenuItemEditFramePalAlpha = new JMenuItem();
            jMenuItemEditFramePalAlpha.setEnabled(false);
            jMenuItemEditFramePalAlpha.setText("Edit DVD Frame Palette");
            jMenuItemEditFramePalAlpha.setMnemonic('f');
            jMenuItemEditFramePalAlpha.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    FramePalDialog cDiag = new FramePalDialog(mainFrame);
                    cDiag.setCurrentSubtitleIndex(subIndex);
                    cDiag.setVisible(true);

                    (new Thread() {
                        @Override
                        public void run() {
                            synchronized (threadSemaphore) {
                                try {
                                    if (Core.isReady()) {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshSrcFrame(subIndex);
                                        refreshTrgFrame(subIndex);
                                    }
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
            });
        }
        return jMenuItemEditFramePalAlpha;
    }

    private void enableCoreComponents(boolean state) {
        jMenuItemLoad.setEnabled(state);
        jMenuRecentFiles.setEnabled(state && Core.getRecentFiles().size() > 0);
        jMenuItemSave.setEnabled(state && Core.getNumFrames() > 0);
        jMenuItemClose.setEnabled(state);
        jMenuItemEditFrame.setEnabled(state);
        jMenuItemBatchMove.setEnabled(state);
        jComboBoxSubNum.setEnabled(state);
        jComboBoxOutFormat.setEnabled(state);
        jComboBoxFilter.setEnabled(state);
    }

    /**
     * Enable/disable components dependent only available for VobSubs
     */
    private void enableVobSubMenuCombo() {
        boolean b = (Core.getOutputMode() == OutputMode.VOBSUB   || Core.getOutputMode() == OutputMode.SUPIFO)
                && ( (Core.getInputMode()  != InputMode.VOBSUB   && Core.getInputMode() != InputMode.SUPIFO)
                        || Core.getPaletteMode() != PaletteMode.KEEP_EXISTING);

        jComboBoxAlphaThr.setEnabled(b);
        jComboBoxHiMedThr.setEnabled(b);
        jComboBoxMedLowThr.setEnabled(b);

        b = (Core.getInputMode()  == InputMode.VOBSUB  || Core.getInputMode() == InputMode.SUPIFO);
        jMenuItemEditCurColors.setEnabled(b);
        jMenuItemEditFramePalAlpha.setEnabled(b);
    }

    /**
     * Enable/disable components dependent only available for VobSubs
     * @param b true: enable
     */
    private void enableVobsubStuff(boolean b) {
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
    private void warningDialog() {
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
                    s+"\nCheck the log for details",
                    "Warning!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void load(String fname) {
        if (fname != null) {
            if (!new File(fname).exists()) {
                JOptionPane.showMessageDialog(mainFrame, "File '"+fname+"' does not exist",
                        "File not found!", JOptionPane.WARNING_MESSAGE);
            } else {
                synchronized (threadSemaphore) {
                    boolean xml = ToolBox.getExtension(fname).equalsIgnoreCase("xml");
                    boolean idx = ToolBox.getExtension(fname).equalsIgnoreCase("idx");
                    boolean ifo = ToolBox.getExtension(fname).equalsIgnoreCase("ifo");
                    byte id[] = ToolBox.getFileID(fname, 4);
                    StreamID sid = (id == null) ? StreamID.UNKNOWN : Core.getStreamID(id);
                    if (idx || xml || ifo || sid != StreamID.UNKNOWN) {
                        mainFrame.setTitle(APP_NAME_AND_VERSION + " - " + fname);
                        subIndex = 0;
                        loadPath = fname;
                        saveFilename = ToolBox.stripExtension(ToolBox.getFileName(loadPath));
                        savePath = ToolBox.getPathName(loadPath);
                        enableCoreComponents(false);
                        enableVobsubStuff(false);
                        try {
                            Core.readStreamThreaded(loadPath, mainFrame, sid);
                            warningDialog();
                            int num = Core.getNumFrames();
                            Core.setReady(false);
                            jComboBoxSubNum.removeAllItems();
                            for (int i=1; i<=num; i++) {
                                jComboBoxSubNum.addItem(Integer.toString(i));
                            }
                            jComboBoxSubNum.setSelectedIndex(subIndex);
                            jComboBoxAlphaThr.setSelectedIndex(Core.getAlphaThr());
                            jComboBoxHiMedThr.setSelectedIndex(Core.getLumThr()[0]);
                            jComboBoxMedLowThr.setSelectedIndex(Core.getLumThr()[1]);
                            //
                            if (Core.getCropOfsY() > 0) {
                                if (JOptionPane.showConfirmDialog(mainFrame, "Reset Crop Offset?",
                                        "", JOptionPane.YES_NO_OPTION) == 0) {
                                    Core.setCropOfsY(0);
                                }
                            }

                            ConversionDialog trans = new ConversionDialog(mainFrame);
                            trans.enableOptionMove(Core.getMoveCaptions());
                            trans.setVisible(true);
                            if (!trans.wasCanceled()) {
                                Core.scanSubtitles();
                                if (Core.getMoveCaptions()) {
                                    Core.moveAllThreaded(mainFrame);
                                }
                                Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                Core.setReady(true);
                                jMenuItemExit.setEnabled(true);
                                refreshSrcFrame(subIndex);
                                refreshTrgFrame(subIndex);
                                enableCoreComponents(true);
                                if (Core.getOutputMode() == OutputMode.VOBSUB || Core.getInputMode() == InputMode.SUPIFO) {
                                    enableVobsubStuff(true);
                                }
                                // tell the core that a stream was loaded via the GUI
                                Core.loadedHook();
                                Core.addRecent(loadPath);
                                updateRecentFilesMenu();
                            } else {
                                closeSub();
                                printWarn("Loading cancelled by user.");
                                Core.close();
                            }
                        } catch (CoreException ex) {
                            jMenuItemLoad.setEnabled(true);
                            updateRecentFilesMenu();
                            jComboBoxOutFormat.setEnabled(true);
                            error(ex.getMessage());
                        } catch (Exception ex) {
                            ToolBox.showException(ex);
                            exit(4);
                        } finally {
                            flush();
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "This is not a supported SUP stream",
                                "Wrong format!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }

    private JMenuItem getJMenuItemLoad() {
        if (jMenuItemLoad == null) {
            jMenuItemLoad = new JMenuItem();
            jMenuItemLoad.setText("Load");
            jMenuItemLoad.setMnemonic('l');
            jMenuItemLoad.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String[] ext = new String[5];
                    ext[0] = "idx";
                    ext[1] = "ifo";
                    ext[2] = "sub";
                    ext[3] = "sup";
                    ext[4] = "xml";
                    console.setText("");
                    String p = ToolBox.getPathName(loadPath);
                    String fn = ToolBox.getFileName(loadPath);
                    final String fname = ToolBox.getFileName(p, fn, ext, true, mainFrame);
                    (new Thread() {
                        @Override
                        public void run() {
                            load(fname);
                        } }).start();
                }
            });
        }
        return jMenuItemLoad;
    }

    private void updateRecentFilesMenu() {
        jMenuRecentFiles.setEnabled(false);
        ArrayList<String> recentFiles = Core.getRecentFiles();
        int size = recentFiles.size();
        if (size>0) {
            jMenuRecentFiles.removeAll();
            for (int i=0; i<size; i++) {
                JMenuItem j = new JMenuItem();
                String s = recentFiles.get(i);
                j.setText(i+": "+s);
                j.setActionCommand(s);
                j.setMnemonic((""+i).charAt(0));
                j.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        console.setText("");
                        final String fname = e.getActionCommand();
                        (new Thread() {
                            @Override
                            public void run() {
                                load(fname);
                            } }).start();
                    }
                });
                jMenuRecentFiles.add(j);
            }
            jMenuRecentFiles.setEnabled(true);
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
            jMenuItemSave.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean showException = true;
                    String path;
                    try {
                        ExportDialog exp = new ExportDialog(mainFrame);
                        path = savePath + File.separatorChar + saveFilename+"_exp.";
                        if (Core.getOutputMode() == OutputMode.VOBSUB) {
                            path += "idx";
                        } else if (Core.getOutputMode() == OutputMode.SUPIFO) {
                            path += "ifo";
                        } else if (Core.getOutputMode() == OutputMode.BDSUP) {
                            path += "sup";
                        } else {
                            path += "xml";
                        }

                        exp.setFileName(path);
                        exp.setVisible(true);

                        String fn = exp.getFileName();
                        if (!exp.wasCanceled() && fn != null) {
                            savePath = ToolBox.getPathName(fn);
                            saveFilename = ToolBox.stripExtension(ToolBox.getFileName(fn));
                            saveFilename = saveFilename.replaceAll("_exp$","");
                            //
                            File fi,fs;
                            if (Core.getOutputMode() == OutputMode.VOBSUB) {
                                fi = new File(ToolBox.stripExtension(fn)+".idx");
                                fs = new File(ToolBox.stripExtension(fn)+".sub");
                            } else if (Core.getOutputMode() == OutputMode.SUPIFO) {
                                fi = new File(ToolBox.stripExtension(fn)+".ifo");
                                fs = new File(ToolBox.stripExtension(fn)+".sup");
                            } else {
                                fs = new File(ToolBox.stripExtension(fn)+".sup");
                                fi = fs; // we don't need the idx file
                            }
                            if (fi.exists() || fs.exists()) {
                                showException = false;
                                if ((fi.exists() && !fi.canWrite()) || (fs.exists() && !fs.canWrite())) {
                                    throw new CoreException("Target is write protected.");
                                }
                                if (JOptionPane.showConfirmDialog(mainFrame, "Target exists! Overwrite?",
                                        "", JOptionPane.YES_NO_OPTION) == 1) {
                                    throw new CoreException("Target exists. Aborted by user.");
                                }
                                showException = true;
                            }
                            // start conversion
                            Core.createSubThreaded(fn, mainFrame);
                            warningDialog();
                        }
                    } catch (CoreException ex) {
                        if (showException) {
                            error(ex.getMessage());
                        }
                    } catch (Exception ex) {
                        ToolBox.showException(ex);
                        exit(4);
                    } finally {
                        flush();
                    }
                }
            });

        }
        return jMenuItemSave;
    }

    private void closeSub() {
        jComboBoxSubNum.removeAllItems();
        enableCoreComponents(false);
        jMenuItemLoad.setEnabled(true);
        updateRecentFilesMenu();
        jComboBoxPalette.setEnabled(false);
        jComboBoxAlphaThr.setEnabled(false);
        jComboBoxHiMedThr.setEnabled(false);
        jComboBoxMedLowThr.setEnabled(false);
        jMenuItemEditCurColors.setEnabled(false);
        jMenuItemEditFramePalAlpha.setEnabled(false);

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
            jMenuItemClose.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Core.close();
                    closeSub();
                }
            });
        }
        return jMenuItemClose;
    }

    private JMenuItem getJMenuItemExit() {
        if (jMenuItemExit == null) {
            jMenuItemExit = new JMenuItem();
            jMenuItemExit.setText("Exit");
            jMenuItemExit.setMnemonic('e');
            jMenuItemExit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exit(0);
                }
            });
        }
        return jMenuItemExit;
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
                            ed.setIndex(subIndex);
                            ed.setVisible(true);
                            subIndex = ed.getIndex();
                            (new Thread() {
                                @Override
                                public void run() {
                                    synchronized (threadSemaphore) {
                                        try {
                                            Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
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
            jComboBoxPalette.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (Core.isReady()) {
                        int idx = jComboBoxPalette.getSelectedIndex();
                        for (PaletteMode m : PaletteMode.values()) {
                            if (idx == m.ordinal()) {
                                Core.setPaletteMode(m);
                                break;
                            }
                        }

                        enableVobSubMenuCombo();

                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshTrgFrame(subIndex);
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
            });
        }
        return jComboBoxPalette;
    }

    private JComboBox getJComboBoxFilter() {
        if (jComboBoxFilter == null) {
            jComboBoxFilter = new JComboBox();
            jComboBoxFilter.setEnabled(false);
            jComboBoxFilter.setToolTipText("Select filter for scaling");
            jComboBoxFilter.setPreferredSize(new Dimension(120, 20));
            jComboBoxFilter.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (Core.isReady()) {
                        int idx = jComboBoxFilter.getSelectedIndex();
                        for (ScalingFilter s : ScalingFilter.values()) {
                            if (idx == s.ordinal()) {
                                Core.setScalingFilter(s);
                                break;
                            }
                        }

                        (new Thread() {
                            @Override
                            public void run() {
                                synchronized (threadSemaphore) {
                                    try {
                                        Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
                                        refreshTrgFrame(subIndex);
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
            });

        }
        return jComboBoxFilter;
    }
}
