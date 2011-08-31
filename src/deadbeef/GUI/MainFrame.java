package deadbeef.GUI;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import deadbeef.SupTools.Core;
import deadbeef.SupTools.CoreException;
import deadbeef.SupTools.Palette;
import deadbeef.SupTools.Core.PaletteMode;
import deadbeef.SupTools.Core.StreamID;
import deadbeef.Tools.ToolBox;

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

	private JPanel jContentPane = null;

	private JScrollPane jScrollPaneSrc = null;

	private JScrollPane jScrollPaneTrg = null;

	private JPanel jPanelUp = null;

	private JPanel jPanelDown = null;

	private JLabel jLabelAlphaThr = null;

	private JLabel jLabelHiMedThr = null;

	private JLabel jLabelMedLowThr = null;

	private JLabel jLabelOutFormat = null;

	private GfxPane jPanelSrc = null;

	private GfxPane jPanelTrg = null;

	private JComboBox jComboBoxSubNum = null;

	private JLabel jLabelSubNum = null;

	private JComboBox jComboBoxAlphaThr = null;

	private JComboBox jComboBoxHiMedThr = null;

	private JComboBox jComboBoxMedLowThr = null;

	private JComboBox jComboBoxOutFormat = null;

	private JLabel jLabelInfoSrc = null;

	private JLabel jLabelInfoTrg = null;

	private JPanel jPanelUp2 = null;

	private JPanel jPanelMid = null;

	private JMenuBar jMenuBar = null;

	private JMenu jMenuFile = null;

	private JMenuItem jMenuItemLoad = null;
	
	private JMenu jMenuRecent = null;

	private JMenuItem jMenuItemSave = null;

	private JMenuItem jMenuItemClose = null;

	private JMenuItem jMenuItemExit = null;

	private JMenu jMenuHelp = null;

	private JMenuItem jMenuItemHelp = null;

	private JMenu jMenuPrefs = null;

	private JMenuItem jMenuItemEditColors = null;

	private JMenuItem jMenuItemEditCurColors = null;

	private JMenuItem jMenuItemEditFramePalAlpha = null;

	private JMenuItem jMenuItemConversionSettings = null;

	private JCheckBoxMenuItem jMenuItemSwapCrCb = null;

	private JCheckBoxMenuItem jMenuItemVerbatim = null;

	private JCheckBoxMenuItem jMenuItemFixAlpha = null;

	private JMenu jMenuEdit = null;

	private JMenuItem jMenuItemEditFrame = null;

	private JMenuItem jMenuItemBatchMove = null;

	private JMenuItem jMenuItemResetCrop = null;

	private JPopupMenu jPopupMenu = null;

	private JMenuItem jPopupMenuItemCopy = null;

	private JMenuItem jPopupMenuItemClear = null;

	private JScrollPane jScrollPaneConsole = null;

	private JTextArea console = null;

	private EditPane jLayoutPane = null;

	private JComboBox jComboBoxPalette = null;

	private JLabel jLabelPalette = null;

	private JLabel jLabelFilter = null;

	private JComboBox jComboBoxFilter = null;

	private JTextField jTextSubNum = null;

	private JTextField jTextAlphaThr = null;

	private JTextField jTextHiMedThr = null;

	private JTextField jTextMedLowThr = null;

	// own stuff

	/** semaphore for synchronization of threads */
	private final Object threadSemaphore = new Object();
	/** reference to this frame (to allow access to "this" from inner classes */
	private final JFrame mainFrame;
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
	private final TransferHandler thandler;

	/** font size for output console */
	private final int fontSize = 12;
	//private final static int maxDocSize = 1000000; // to work around bad TextPane performance
	/** background color for errors */
	private final Color errBgnd = new Color(0xffe1acac);
	/** background color for ok */
	private final Color okBgnd = UIManager.getColor("TextField.background");

	/**
	 * Constructor
	 * @param fname file name
	 * @throws HeadlessException
	 */
	public MainFrame(String fname) throws HeadlessException {
		this();
		loadPath = fname;
		load(fname);
	}

	/**
	 * Constructor
	 * @throws HeadlessException
	 */
	public MainFrame() throws HeadlessException {
		super();

		jTextSubNum = new JTextField();
		jTextAlphaThr  = new JTextField();
		jTextHiMedThr  = new JTextField();
		jTextMedLowThr = new JTextField();

		// TODO Auto-generated constructor stub
		initialize();

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				exit(0);
			}

			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
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
		p.x -= this.getWidth()/2;
		p.y -= this.getHeight()/2;
		p.x = Core.props.get("framePosX", p.x);
		p.y = Core.props.get("framePosY", p.y);
		this.setLocation(p);

		loadPath = Core.props.get("loadPath", "");
		//savePath = Core.props.get("savePath", "");
		colorPath = Core.props.get("colorPath", "");
		mainFrame = this;

		subIndex = 0;
		
		updateRecentMenu();

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

		for (Core.OutputMode m : Core.OutputMode.values()) {
			jComboBoxOutFormat.addItem(Core.getOutputFormatName(m));
		}
		jComboBoxOutFormat.setSelectedIndex(Core.getOutputMode().ordinal());

		for (Core.PaletteMode m : Core.PaletteMode.values()) {
			jComboBoxPalette.addItem(Core.getPaletteModeName(m));
		}
		jComboBoxPalette.setSelectedIndex(Core.getPaletteMode().ordinal());

		for (Core.ScalingFilters s : Core.ScalingFilters.values()) {
			jComboBoxFilter.addItem(Core.getScalingFilterName(s));
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
		this.setVisible(true);

		// drag'n'drop
		thandler = new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean canImport(TransferHandler.TransferSupport support) {
				if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					return false;
				}
				return true;
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean importData(TransferHandler.TransferSupport support) {
				if (!canImport(support))
					return false;

				Transferable t = support.getTransferable();

				try {
					java.util.List<File> flist =
						(java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
					load(flist.get(0).getAbsolutePath());
				} catch (UnsupportedFlavorException e) {
					return false;
				} catch (IOException e) {
					return false;
				}

				return true;
			}
		};
		this.setTransferHandler(thandler);

		print(Core.getProgVerName()+" - a converter from Blu-Ray/HD-DVD SUP to DVD SUB/IDX and more\n");
		print(Core.getAuthorDate()+"\n");
		print("Official thread at Doom9: http://forum.doom9.org/showthread.php?t=145277\n\n");
		flush();
	}

	/**
	 * Print string to console window
	 * @param s String to print
	 */
	public void print(final String s) {
		Document doc = console.getDocument();
		int length = doc.getLength();
		try {
			doc.insertString(length, s, null);
		} catch (BadLocationException ex) {}
	}

	/**
	 * Force console to display last line
	 */
	public void flush() {
		SwingUtilities.invokeLater(new Runnable() { public void run() { console.setCaretPosition(console.getDocument().getLength()); } });
	}

	/**
	 * Print text to output pane
	 * @param s text to print
	 */
	public void printOut(final String s) {
		//try {
			SwingUtilities.invokeLater(new Runnable() { public void run() { print(s); } });
		//} catch (InterruptedException e) {
		//} catch (InvocationTargetException e) {
		//}
	}

	/**
	 * Print text to error pane
	 * @param s text to print
	 */
	public void printErr(final String s) {
		//try {
			SwingUtilities.invokeLater(new Runnable() { public void run() { print(s); }});
		//} catch (InterruptedException e) {
		//} catch (InvocationTargetException e) {
		//}
	}

	/**
	 * Print warning
	 * @param s text to print
	 */
	public void printWarn(final String s) {
		//try {
			SwingUtilities.invokeLater(new Runnable() { public void run() { print(s); }});
		//} catch (InterruptedException e) {
		//} catch (InvocationTargetException e) {
		//}
	}

	/**
	 * Print error and show error dialog
	 * @param s error message to display
	 */
	public void error (String s) {
		Core.printErr(s);
		JOptionPane.showMessageDialog(mainFrame,s,"Error!", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Update all components belonging to the target window
	 * @param index caption index
	 */
	private void refreshTrgFrame(final int index) {
		SwingUtilities.invokeLater( new Runnable() { public void run() {
			jLayoutPane.setDim(Core.getTrgWidth(index), Core.getTrgHeight(index));
			jLayoutPane.setOffsets(Core.getTrgOfsX(index), Core.getTrgOfsY(index));
			jLayoutPane.setCropOfsY(Core.getCropOfsY());
			jLayoutPane.setImage(Core.getTrgImage(),Core.getTrgImgWidth(index), Core.getTrgImgHeight(index));
			jLayoutPane.setExcluded(Core.getTrgExcluded(index));
			jPanelTrg.setImage(Core.getTrgImage());
			printInfoTrg(index);
			jLayoutPane.repaint();
		} } );
	}

	/**
	 * Update all components belonging to the source window
	 * @param index caption index
	 */
	private void refreshSrcFrame(final int index) {
		SwingUtilities.invokeLater( new Runnable() { public void run() {
			BufferedImage img = Core.getSrcImage();
			jPanelSrc.setImage(img);
			printInfoSrc(index);
		} } );
	}

	/**
	 * Common exit routine, stores properties and releases Core file handles
	 * @param code exit code
	 */
	private void exit(int code) {
		if (code == 0) {
			// store width and height
			Dimension d = this.getSize();
			if (this.getExtendedState() != MainFrame.MAXIMIZED_BOTH) {
				Core.props.set("frameWidth", d.width);
				Core.props.set("frameHeight", d.height);
				// store frame pos
				Point p = this.getLocation();
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
		this.setSize(800, 600);
		this.setMinimumSize(new Dimension(700,300));
		this.setJMenuBar(getjMenuBar());
		this.setContentPane(getJContentPane());
		this.setTitle(Core.getProgVerName());
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

	/**
	 * This method initializes jScrollPaneSup
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPaneSup() {
		if (jScrollPaneSrc == null) {
			jScrollPaneSrc = new JScrollPane();
			jScrollPaneSrc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPaneSrc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			jScrollPaneSrc.setViewportView(getJPanelSup());
		}
		return jScrollPaneSrc;
	}

	/**
	 * This method initializes jScrollPaneSub
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPaneSub() {
		if (jScrollPaneTrg == null) {
			jScrollPaneTrg = new JScrollPane();
			jScrollPaneTrg.setViewportView(getJPanelSub());
		}
		return jScrollPaneTrg;
	}

	/**
	 * This method initializes jPanelUp
	 *
	 * @return javax.swing.JPanel
	 */
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
			jLabelFilter = new JLabel();
			jLabelFilter.setPreferredSize(new Dimension(120, 20));
			jLabelFilter.setText("Filter");
			GridBagConstraints gridBagLabelPalette = new GridBagConstraints();
			gridBagLabelPalette.insets = new Insets(0, 4, 0, 4);
			gridBagLabelPalette.anchor = GridBagConstraints.WEST;
			gridBagLabelPalette.gridx = 6;
			gridBagLabelPalette.gridy = 0;
			jLabelPalette = new JLabel();
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

			jLabelSubNum = new JLabel();
			jLabelSubNum.setText("Subtitle");
			jLabelSubNum.setPreferredSize(new Dimension(100, 20));
			jLabelOutFormat = new JLabel();
			jLabelOutFormat.setText("Output Format");
			jLabelOutFormat.setPreferredSize(new Dimension(120, 20));
			jLabelMedLowThr = new JLabel();
			jLabelMedLowThr.setText("Med/Low Threshold");
			jLabelMedLowThr.setPreferredSize(new Dimension(100, 20));
			jLabelHiMedThr = new JLabel();
			jLabelHiMedThr.setText("Hi/Med Threshold");
			jLabelHiMedThr.setPreferredSize(new Dimension(100, 20));
			jLabelAlphaThr = new JLabel();
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

	/**
	 * This method initializes jPanelDown
	 *
	 * @return javax.swing.JPanel
	 */
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

	/**
	 * This method initializes jPanelSup
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelSup() {
		if (jPanelSrc == null) {
			jPanelSrc = new GfxPane();
		}
		return jPanelSrc;
	}

	/**
	 * This method initializes jPanelSub
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelSub() {
		if (jPanelTrg == null) {
			jPanelTrg = new GfxPane();
		}
		return jPanelTrg;
	}

	/**
	 * This method initializes jComboBoxSubNum
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxSubNum() {
		if (jComboBoxSubNum == null) {
			jComboBoxSubNum = new JComboBox();
			jComboBoxSubNum.setEnabled(false);
			jComboBoxSubNum.setPreferredSize(new Dimension(100, 20));
			jComboBoxSubNum.setMinimumSize(new Dimension(80, 20));
			jComboBoxSubNum.setToolTipText("Set subtitle number");
			jComboBoxSubNum.setEditable(true);
			jComboBoxSubNum.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (Core.isReady()) {
						int num = Core.getNumFrames();
						int idx;
						try {
							idx = Integer.parseInt(jComboBoxSubNum.getSelectedItem().toString())-1;
						} catch (NumberFormatException ex) {
							idx = subIndex; // invalid number -> keep old value
						}

						if (idx < 0)
							idx = 0;
						if (idx >= num)
							idx = num-1;
						subIndex = idx;
						jComboBoxSubNum.setSelectedIndex(subIndex);

						(new Thread() { @Override
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
				private void check(DocumentEvent e) {
					if (Core.isReady()) {
						int idx = ToolBox.getInt(jTextSubNum.getText())-1;
						if (idx < 0 || idx >= Core.getNumFrames())
							jTextSubNum.setBackground(errBgnd);
						else {
							subIndex = idx;
							(new Thread() { @Override
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

				public void insertUpdate(DocumentEvent e) {
					check(e);
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					check(e);
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					check(e);
				}
			});
		}
		return jComboBoxSubNum;
	}

	/**
	 * This method initializes jComboBoxAlphaThr
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxAlphaThr() {
		if (jComboBoxAlphaThr == null) {
			jComboBoxAlphaThr = new JComboBox();
			jComboBoxAlphaThr.setEnabled(false);
			jComboBoxAlphaThr.setEditable(true);
			jComboBoxAlphaThr.setToolTipText("Set alpha threshold");
			jComboBoxAlphaThr.setPreferredSize(new Dimension(100, 20));
			jComboBoxAlphaThr.setMinimumSize(new Dimension(80, 20));
			jComboBoxAlphaThr.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (Core.isReady()) {
						int idx;
						try {
							idx = Integer.parseInt(jComboBoxAlphaThr.getSelectedItem().toString());
						} catch (NumberFormatException ex) {
							idx = Core.getAlphaThr(); // invalid number -> keep old value
						}

						if (idx < 0)
							idx = 0;
						if (idx > 255)
							idx = 255;

						Core.setAlphaThr(idx);
						jComboBoxAlphaThr.setSelectedIndex(Core.getAlphaThr());

						(new Thread() { @Override
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
				private void check(DocumentEvent e) {
					if (Core.isReady()) {
						int idx = ToolBox.getInt(jTextAlphaThr.getText());
						if (idx < 0 || idx > 255)
							jTextAlphaThr.setBackground(errBgnd);
						else {
							Core.setAlphaThr(idx);
							(new Thread() { @Override
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

				public void insertUpdate(DocumentEvent e) {
					check(e);
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					check(e);
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					check(e);
				}
			});

		}
		return jComboBoxAlphaThr;
	}

	/**
	 * This method initializes jComboBoxHiMedThr
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxHiMedThr() {
		if (jComboBoxHiMedThr == null) {
			jComboBoxHiMedThr = new JComboBox();
			jComboBoxHiMedThr.setEditable(true);
			jComboBoxHiMedThr.setEnabled(false);
			jComboBoxHiMedThr.setPreferredSize(new Dimension(100, 20));
			jComboBoxHiMedThr.setMinimumSize(new Dimension(80, 20));
			jComboBoxHiMedThr.setToolTipText("Set medium/high luminance threshold");
			jComboBoxHiMedThr.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (Core.isReady()) {
						int lumThr[] = Core.getLumThr();
						int idx;
						try {
							idx = Integer.parseInt(jComboBoxHiMedThr.getSelectedItem().toString());
						} catch (NumberFormatException ex) {
							idx = lumThr[0]; // invalid number -> keep old value
						}

						if (idx <= lumThr[1]) // must be greater than med/low threshold
							idx = lumThr[1] + 1;

						if (idx < 0)
							idx = 0;
						if (idx > 255)
							idx = 255;

						lumThr[0] = idx;
						Core.setLumThr(lumThr);
						jComboBoxHiMedThr.setSelectedIndex(Core.getLumThr()[0]);

						(new Thread() { @Override
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
				private void check(DocumentEvent e) {
					if (Core.isReady()) {
						int lumThr[] = Core.getLumThr();
						int idx = ToolBox.getInt(jTextHiMedThr.getText());
						if (idx < 0 || idx > 255 | idx <= lumThr[1])
							jTextHiMedThr.setBackground(errBgnd);
						else {
							lumThr[0] = idx;
							Core.setLumThr(lumThr);
							(new Thread() { @Override
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

				public void insertUpdate(DocumentEvent e) {
					check(e);
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					check(e);
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					check(e);
				}
			});

		}
		return jComboBoxHiMedThr;
	}

	/**
	 * This method initializes jComboBoxMedLowThr
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxMedLowThr() {
		if (jComboBoxMedLowThr == null) {
			jComboBoxMedLowThr = new JComboBox();
			jComboBoxMedLowThr.setEditable(true);
			jComboBoxMedLowThr.setEnabled(false);
			jComboBoxMedLowThr.setToolTipText("Set low/medium luminance threshold");
			jComboBoxMedLowThr.setPreferredSize(new Dimension(100, 20));
			jComboBoxMedLowThr.setMinimumSize(new Dimension(80, 20));
			jComboBoxMedLowThr.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (Core.isReady()) {
						int lumThr[] = Core.getLumThr();
						int idx;
						try {
							idx = Integer.parseInt(jComboBoxMedLowThr.getSelectedItem().toString());
						} catch (NumberFormatException ex) {
							idx = lumThr[1]; // invalid number -> keep old value
						}

						if (idx >= lumThr[0]) // must be smaller than med/high threshold
							idx = lumThr[0] - 1;

						if (idx < 0)
							idx = 0;
						if (idx > 255)
							idx = 255;

						lumThr[1] = idx;
						Core.setLumThr(lumThr);

						final int index = idx;
						jComboBoxMedLowThr.setSelectedIndex(index);

						(new Thread() { @Override
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
				private void check(DocumentEvent e) {
					if (Core.isReady()) {
						int lumThr[] = Core.getLumThr();
						int idx = ToolBox.getInt(jTextMedLowThr.getText());
						if (idx < 0 || idx > 255 | idx >= lumThr[0])
							jTextMedLowThr.setBackground(errBgnd);
						else {
							lumThr[1] = idx;
							Core.setLumThr(lumThr);
							(new Thread() { @Override
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

				public void insertUpdate(DocumentEvent e) {
					check(e);
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					check(e);
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					check(e);
				}
			});

		}
		return jComboBoxMedLowThr;
	}

	/**
	 * This method initializes jComboBoxOutMode
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxOutFormat() {
		if (jComboBoxOutFormat == null) {
			jComboBoxOutFormat = new JComboBox();
			jComboBoxOutFormat.setEnabled(false);
			jComboBoxOutFormat.setToolTipText("Select export format");
			jComboBoxOutFormat.setPreferredSize(new Dimension(120, 20));
			jComboBoxOutFormat.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (Core.isReady()) {
						int idx = jComboBoxOutFormat.getSelectedIndex();
						for (Core.OutputMode m : Core.OutputMode.values()) {
							if (idx == m.ordinal()) {
								Core.setOutputMode(m);
								break;
							}
						}

						(new Thread() { @Override
							public void run() {
							synchronized (threadSemaphore) {
							try {
								Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
								refreshTrgFrame(subIndex);
								if (Core.getOutputMode() == Core.OutputMode.VOBSUB || Core.getOutputMode() == Core.OutputMode.SUPIFO)
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

	/**
	 * This method initializes jPanelUp2
	 *
	 * @return javax.swing.JPanel
	 */
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

	/**
	 * This method initializes jPanelMid
	 *
	 * @return javax.swing.JPanel
	 */
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

	/**
	 * This method initializes jJMenuBar
	 *
	 * @return javax.swing.JMenuBar
	 */
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

	/**
	 * This method initializes jMenuFile
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuFile() {
		if (jMenuFile == null) {
			jMenuFile = new JMenu();
			jMenuFile.setName("");
			jMenuFile.setMnemonic('f');
			jMenuFile.setText("File");
			jMenuFile.add(getJMenuItemLoad());
			jMenuFile.add(getJMenuItemRecent());
			jMenuFile.add(getJMenuItemSave());
			jMenuFile.add(getJMenuItemClose());
			jMenuFile.add(getJMenuItemExit());
		}
		return jMenuFile;
	}

	/**
	 * This method initializes jMenuPrefs
	 *
	 * @return javax.swing.JMenu
	 */
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

	/**
	 * This method initializes jMenuEdit
	 *
	 * @return javax.swing.JMenu
	 */
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

	/**
	 * This method initializes jMenuHelp
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getJMenuHelp() {
		if (jMenuHelp == null) {
			jMenuHelp = new JMenu();
			jMenuHelp.setText("Help");
			jMenuHelp.setMnemonic('h');
			jMenuHelp.add(getJMenuItemHelp());
		}
		return jMenuHelp;
	}
	/**
	 * This method initializes jMenuItemHelp
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemHelp() {
		if (jMenuItemHelp == null) {
			jMenuItemHelp = new JMenuItem();
			jMenuItemHelp.setText("Help");
			jMenuItemHelp.setMnemonic('h');
			jMenuItemHelp.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Help help = new Help();
					help.setLocation(getX()+30,getY()+30);
					help.setSize(800,600);
					help.setVisible(true);
				}
			});
		}
		return jMenuItemHelp;
	}

	/**
	 * This method initializes jMenuItemEditFrame
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemEditFrame() {
		if (jMenuItemEditFrame == null) {
			jMenuItemEditFrame = new JMenuItem();
			jMenuItemEditFrame.setText("Edit Frame");
			jMenuItemEditFrame.setMnemonic('e');
			jMenuItemEditFrame.setEnabled(false);
			jMenuItemEditFrame.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (Core.isReady()) {
						EditDialog ed = new EditDialog(mainFrame, true);
						ed.setIndex(subIndex);
						ed.setVisible(true);
						subIndex = ed.getIndex();
						(new Thread() { @Override
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

	/**
	 * This method initializes jMenuItemBatchMove
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemBatchMove() {
		if (jMenuItemBatchMove == null) {
			jMenuItemBatchMove = new JMenuItem();
			jMenuItemBatchMove.setText("Move all captions");
			jMenuItemBatchMove.setMnemonic('m');
			jMenuItemBatchMove.setEnabled(false);
			jMenuItemBatchMove.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (Core.isReady()) {
						MoveDialog ed = new MoveDialog(mainFrame, true);
						ed.setIndex(subIndex);
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
						subIndex = ed.getIndex();
						jLayoutPane.setScreenRatio(ed.getTrgRatio());
						(new Thread() { @Override
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

	/**
	 * This method initializes jMenuItemBatchMove
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemResetCrop() {
		if (jMenuItemResetCrop == null) {
			jMenuItemResetCrop = new JMenuItem();
			jMenuItemResetCrop.setMnemonic('r');
			jMenuItemResetCrop.setText("Reset crop offset");  // Generated
			jMenuItemResetCrop.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Core.setCropOfsY(0);
					jLayoutPane.setCropOfsY(Core.getCropOfsY());
					jLayoutPane.repaint();
				}
			});
		}
		return jMenuItemResetCrop;
	}

	/**
	 * This method initializes jMenuItemSwapCrCb
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemSwapCrCb() {
		if (jMenuItemSwapCrCb == null) {
			jMenuItemSwapCrCb = new JCheckBoxMenuItem();
			jMenuItemSwapCrCb.setText("Swap Cr/Cb");
			jMenuItemSwapCrCb.setMnemonic('s');
			jMenuItemSwapCrCb.setSelected(false);
			jMenuItemSwapCrCb.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					boolean selected = jMenuItemSwapCrCb.isSelected();
					Core.setSwapCrCb(selected);
					// create and show image
					(new Thread() { @Override
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

	/**
	 * This method initializes jMenuItemVerbatim
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemVerbatim() {
		if (jMenuItemVerbatim == null) {
			jMenuItemVerbatim = new JCheckBoxMenuItem();
			jMenuItemVerbatim.setText("Verbatim Output");
			jMenuItemVerbatim.setMnemonic('v');
			jMenuItemVerbatim.setSelected(false);
			jMenuItemVerbatim.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					boolean selected = jMenuItemVerbatim.isSelected();
					Core.setVerbatim(selected);
				}
			});
		}
		return jMenuItemVerbatim;
	}

	/**
	 * This method initializes jMenuItemVerbatim
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemFixAlpha() {
		if (jMenuItemFixAlpha == null) {
			jMenuItemFixAlpha = new JCheckBoxMenuItem();
			jMenuItemFixAlpha.setText("Fix invisible frames");  // Generated
			jMenuItemFixAlpha.setMnemonic('f');
			jMenuItemFixAlpha.setSelected(false);
			jMenuItemFixAlpha.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					boolean selected = jMenuItemFixAlpha.isSelected();
					Core.setFixZeroAlpha(selected);
				}
			});
		}
		return jMenuItemFixAlpha;
	}

	/**
	 * This method initializes jMenuItemConversionSettings
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemConversionSettings() {
		if (jMenuItemConversionSettings == null) {
			jMenuItemConversionSettings = new JMenuItem();
			jMenuItemConversionSettings.setText("Conversion Settings");
			jMenuItemConversionSettings.setMnemonic('c');
			jMenuItemConversionSettings.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					final Core.Resolution rOld = Core.getOutputResolution();
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
					ConversionDialog trans = new ConversionDialog(mainFrame, true);
					trans.enableOptionMove(false);
					trans.setVisible(true);

					if (!trans.wasCanceled()) {
						// create and show image
						(new Thread() { @Override
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

	/**
	 * This method initializes jMenuItemEditColors
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemEditColors() {
		if (jMenuItemEditColors == null) {
			jMenuItemEditColors = new JMenuItem();
			//jMenuItemEditColors.setEnabled(false);
			jMenuItemEditColors.setText("Edit default DVD Palette");
			jMenuItemEditColors.setMnemonic('d');
			jMenuItemEditColors.setDisplayedMnemonicIndex(5);
			jMenuItemEditColors.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					ColorDialog cDiag = new ColorDialog(mainFrame, true);
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
					for (int i=0; i<cColor.length; i++) {
						cColor[i] = Core.getCurrentDVDPalette().getColor(i+1);
						cColorDefault[i] = Core.getDefaultDVDPalette().getColor(i+1);
					}
					cDiag.setParameters(cName, cColor, cColorDefault);
					cDiag.setPath(colorPath);
					cDiag.setVisible(true);
					if (!cDiag.wasCanceled()) {
						cColor = cDiag.getColors();
						colorPath = cDiag.getPath();
						for (int i=0; i<cColor.length; i++)
							Core.getCurrentDVDPalette().setColor(i+1, cColor[i]);
						(new Thread() { @Override
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

						} } }).start();
					}
				}
			});
		}
		return jMenuItemEditColors;
	}

	/**
	 * This method initializes jMenuItemEditCurColors
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemEditCurColors() {
		if (jMenuItemEditCurColors == null) {
			jMenuItemEditCurColors = new JMenuItem();
			jMenuItemEditCurColors.setEnabled(false);
			jMenuItemEditCurColors.setText("Edit imported DVD Palette");
			jMenuItemEditCurColors.setMnemonic('i');
			jMenuItemEditCurColors.setDisplayedMnemonicIndex(5);
			jMenuItemEditCurColors.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					ColorDialog cDiag = new ColorDialog(mainFrame, true);
					final String cName[] = {
							"Color 0", "Color 1", "Color 2", "Color 3",
							"Color 4", "Color 5", "Color 6", "Color 7",
							"Color 8", "Color 9", "Color 10", "Color 11",
							"Color 12", "Color 13", "Color 14", "Color 15",
					};
					Color cColor[] = new Color[16];
					Color cColorDefault[] = new Color[16];
					for (int i=0; i<cColor.length; i++) {
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
						for (int i=0; i<cColor.length; i++)
							p.setColor(i, cColor[i]);
						Core.setCurSrcDVDPalette(p);
						(new Thread() { @Override
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
				}
			});
		}
		return jMenuItemEditCurColors;
	}

	/**
	 * This method initializes jMenuItemEditFramePalAlpha
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemEditFramePalAlpha() {
		if (jMenuItemEditFramePalAlpha == null) {
			jMenuItemEditFramePalAlpha = new JMenuItem();
			jMenuItemEditFramePalAlpha.setEnabled(false);
			jMenuItemEditFramePalAlpha.setText("Edit DVD Frame Palette");
			jMenuItemEditFramePalAlpha.setMnemonic('f');
			jMenuItemEditFramePalAlpha.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					FramePalDialog cDiag = new FramePalDialog(mainFrame, true);
					cDiag.setIndex(subIndex);
					cDiag.setVisible(true);
					(new Thread() { @Override
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
		return jMenuItemEditFramePalAlpha;
	}

	/**
	 * Enable/disable components dependent on Core state
	 * @param b true: enable
	 */
	private void enableCoreComponents(boolean b) {
		jMenuItemLoad.setEnabled(b);
		jMenuRecent.setEnabled(b&&Core.getRecentFiles().size()>0);
		jMenuItemSave.setEnabled(b&&Core.getNumFrames()>0);
		jMenuItemClose.setEnabled(b);
		jMenuItemEditFrame.setEnabled(b);
		jMenuItemBatchMove.setEnabled(b);
		jComboBoxSubNum.setEnabled(b);
		jComboBoxOutFormat.setEnabled(b);
		jComboBoxFilter.setEnabled(b);
	}

	/**
	 * Enable/disable components dependent only available for VobSubs
	 */
	private void enableVobSubMenuCombo() {
		boolean b;
		if ( (Core.getOutputMode() == Core.OutputMode.VOBSUB   || Core.getOutputMode() == Core.OutputMode.SUPIFO)
				&& ( (Core.getInputMode()  != Core.InputMode.VOBSUB    && Core.getInputMode() != Core.InputMode.SUPIFO)
						|| Core.getPaletteMode() != Core.PaletteMode.KEEP_EXISTING) )
			b = true;
		else
			b = false;

		jComboBoxAlphaThr.setEnabled(b);
		jComboBoxHiMedThr.setEnabled(b);
		jComboBoxMedLowThr.setEnabled(b);

		if ( (Core.getInputMode()  == Core.InputMode.VOBSUB  || Core.getInputMode() == Core.InputMode.SUPIFO) )
			b = true;
		else
			b = false;
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
		for (Core.PaletteMode m : Core.PaletteMode.values())
			if (!b || m != PaletteMode.CREATE_DITHERED)
				jComboBoxPalette.addItem(Core.getPaletteModeName(m));
		if (!b || Core.getPaletteMode() != PaletteMode.CREATE_DITHERED)
			jComboBoxPalette.setSelectedIndex(Core.getPaletteMode().ordinal());
		else
			jComboBoxPalette.setSelectedIndex(PaletteMode.CREATE_NEW.ordinal());

		if (!b || Core.getInputMode() == Core.InputMode.VOBSUB || Core.getInputMode() == Core.InputMode.SUPIFO) {
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
				if (w==1)
					s += w+" warning";
				else
					s += w+" warnings";
			}
			if (w>0 && e>0)
				s += " and ";
			if (e > 0) {
				if (e==1)
					s = e+" error";
				else
					s = e+" errors";
			}

			if (w+e < 3)
				s = "There was "+s;
			else
				s = "There were "+s;

			JOptionPane.showMessageDialog(mainFrame,
					s+"\nCheck the log for details",
					"Warning!", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void load(String fname) {
		if (fname != null) {
			if (!new File(fname).exists())
				JOptionPane.showMessageDialog(mainFrame, "File '"+fname+"' does not exist",
						"File not found!", JOptionPane.WARNING_MESSAGE);
			else {
				synchronized (threadSemaphore) {
					boolean xml = ToolBox.getExtension(fname).equalsIgnoreCase("xml");
					boolean idx = ToolBox.getExtension(fname).equalsIgnoreCase("idx");
					boolean ifo = ToolBox.getExtension(fname).equalsIgnoreCase("ifo");
					byte id[] = ToolBox.getFileID(fname, 4);
					StreamID sid = (id == null) ? Core.StreamID.UNKNOWN : Core.getStreamID(id);
					if (idx || xml || ifo || sid != Core.StreamID.UNKNOWN) {
						mainFrame.setTitle(Core.getProgVerName()+" - "+fname);
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
							for (int i=1; i<=num; i++)
								jComboBoxSubNum.addItem(Integer.toString(i));
							jComboBoxSubNum.setSelectedIndex(subIndex);
							jComboBoxAlphaThr.setSelectedIndex(Core.getAlphaThr());
							jComboBoxHiMedThr.setSelectedIndex(Core.getLumThr()[0]);
							jComboBoxMedLowThr.setSelectedIndex(Core.getLumThr()[1]);
							//
							if (Core.getCropOfsY() > 0)
								if (JOptionPane.showConfirmDialog(mainFrame, "Reset Crop Offset?",
										"", JOptionPane.YES_NO_OPTION) == 0)
									Core.setCropOfsY(0);

							ConversionDialog trans = new ConversionDialog(mainFrame, true);
							trans.enableOptionMove(Core.getMoveCaptions());
							trans.setVisible(true);
							if (!trans.wasCanceled()) {
								Core.scanSubtitles();
								if (Core.getMoveCaptions())
									Core.moveAllThreaded(mainFrame);
								Core.convertSup(subIndex, subIndex+1, Core.getNumFrames());
								Core.setReady(true);
								jMenuItemExit.setEnabled(true);
								refreshSrcFrame(subIndex);
								refreshTrgFrame(subIndex);
								enableCoreComponents(true);
								if (Core.getOutputMode() == Core.OutputMode.VOBSUB || Core.getInputMode() == Core.InputMode.SUPIFO)
									enableVobsubStuff(true);
								// tell the core that a stream was loaded via the GUI
								Core.loadedHook();
								Core.addRecent(loadPath);
								updateRecentMenu();
							} else {
								closeSub();
								printWarn("Loading cancelled by user.");
								Core.close();
							}
						} catch (CoreException ex) {
							jMenuItemLoad.setEnabled(true);
							updateRecentMenu();
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

	/**
	 * This method initializes jMenuItemLoad
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemLoad() {
		if (jMenuItemLoad == null) {
			jMenuItemLoad = new JMenuItem();
			jMenuItemLoad.setText("Load");
			jMenuItemLoad.setMnemonic('l');
			jMenuItemLoad.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
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
					(new Thread() { @Override
					public void run() {
						load(fname);
					} }).start();
				}
			});
		}
		return jMenuItemLoad;
	}

	/**
	 * Update "recent files" menu
	 */
	private void updateRecentMenu() {
		jMenuRecent.setEnabled(false);
		ArrayList<String> recentFiles = Core.getRecentFiles();
		int size = recentFiles.size(); 
		if (size>0) {
			jMenuRecent.removeAll();
			for (int i=0; i<size; i++) {
				JMenuItem j = new JMenuItem();
				String s = recentFiles.get(i);
				j.setText(i+": "+s);
				j.setActionCommand(s);				
				j.setMnemonic((""+i).charAt(0));
				j.addActionListener(new ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						console.setText("");
						final String fname = e.getActionCommand();
						(new Thread() { @Override
						public void run() {
							load(fname);
						} }).start();
					}
				});				
				jMenuRecent.add(j);
			}
			jMenuRecent.setEnabled(true);			
		}
	}
	
	/**
	 * This method initializes jMenuRecent
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenu getJMenuItemRecent() {
		if (jMenuRecent == null) {
			jMenuRecent = new JMenu();
			jMenuRecent.setText("Recent Files");
			jMenuRecent.setMnemonic('r');		
		}
		return jMenuRecent;
	}
	
	/**
	 * This method initializes jMenuItemSave
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemSave() {
		if (jMenuItemSave == null) {
			jMenuItemSave = new JMenuItem();
			jMenuItemSave.setText("Save/Export");
			jMenuItemSave.setMnemonic('s');
			jMenuItemSave.setEnabled(false);
			jMenuItemSave.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					boolean showException = true;
					String path;
					try {
						ExportDialog exp = new ExportDialog(mainFrame, true);
						path = savePath + File.separatorChar + saveFilename+"_exp.";
						if (Core.getOutputMode() == Core.OutputMode.VOBSUB)
							path += "idx";
						else if (Core.getOutputMode() == Core.OutputMode.SUPIFO)
							path += "ifo";
						else if (Core.getOutputMode() == Core.OutputMode.BDSUP)
							path += "sup";
						else
							path += "xml";

						exp.setFileName(path);
						exp.setVisible(true);

						String fn = exp.getFileName();
						if (!exp.wasCanceled() && fn != null) {
							savePath = ToolBox.getPathName(fn);
							saveFilename = ToolBox.stripExtension(ToolBox.getFileName(fn));
							saveFilename = saveFilename.replaceAll("_exp$","");
							//
							File fi,fs;
							if (Core.getOutputMode() == Core.OutputMode.VOBSUB) {
								fi = new File(ToolBox.stripExtension(fn)+".idx");
								fs = new File(ToolBox.stripExtension(fn)+".sub");
							} else if (Core.getOutputMode() == Core.OutputMode.SUPIFO) {
								fi = new File(ToolBox.stripExtension(fn)+".ifo");
								fs = new File(ToolBox.stripExtension(fn)+".sup");
							} else {
								fs = new File(ToolBox.stripExtension(fn)+".sup");
								fi = fs; // we don't need the idx file
							}
							if (fi.exists() || fs.exists()) {
								showException = false;
								if ((fi.exists() && !fi.canWrite()) || (fs.exists() && !fs.canWrite()))
									throw new CoreException("Target is write protected.");
								if (JOptionPane.showConfirmDialog(mainFrame, "Target exists! Overwrite?",
										"", JOptionPane.YES_NO_OPTION) == 1)
									throw new CoreException("Target exists. Aborted by user.");
								showException = true;
							}
							// start conversion
							Core.createSubThreaded(fn, mainFrame);
							warningDialog();
						}
					} catch (CoreException ex) {
						if (showException)
							error(ex.getMessage());
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
		updateRecentMenu();
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
	
	/**
	 * This method initializes jMenuItemClose
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemClose() {
		if (jMenuItemClose == null) {
			jMenuItemClose = new JMenuItem();
			jMenuItemClose.setText("Close");
			jMenuItemClose.setEnabled(false);
			jMenuItemClose.setMnemonic('c');
			jMenuItemClose.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Core.close();
					closeSub();
				}
			});
		}
		return jMenuItemClose;
	}

	/**
	 * This method initializes jMenuItemExit
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItemExit() {
		if (jMenuItemExit == null) {
			jMenuItemExit = new JMenuItem();
			jMenuItemExit.setText("Exit");
			jMenuItemExit.setMnemonic('e');
			jMenuItemExit.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					exit(0);
				}
			});
		}
		return jMenuItemExit;
	}

	/**
	 * This method initializes jScrollPaneConsole
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPaneConsole() {
		if (jScrollPaneConsole == null) {
			jScrollPaneConsole = new JScrollPane();
			jScrollPaneConsole.setViewportView(getConsole());
		}
		return jScrollPaneConsole;
	}

	/**
	 * This method initializes jPopupMenu
	 *
	 * @return javax.swing.JPopupMenu
	 */
	private JPopupMenu getJPopupMenu() {
		if (jPopupMenu == null) {
			jPopupMenu = new JPopupMenu();
			jPopupMenu.add(getJPopupMenuItemCopy());
			jPopupMenu.add(getJPopupMenuItemClear());
			//jPopupMenu.setVisible(false);
		}
		return jPopupMenu;
	}
	
	/**
	 * This method initializes jPopupMenuItemCopy
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJPopupMenuItemCopy() {
		if (jPopupMenuItemCopy == null) {
			jPopupMenuItemCopy = new JMenuItem();
			jPopupMenuItemCopy.setText("Copy");
			jPopupMenuItemCopy.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String s = console.getSelectedText();
					try {
						if (s!=null)
							setClipboard(s);
					}catch (OutOfMemoryError ex) {
						JOptionPane.showMessageDialog(mainFrame,"Out of heap! Use -Xmx256m to increase heap! ","Error!", JOptionPane.WARNING_MESSAGE);
					}
				}
			});
		}
		return jPopupMenuItemCopy;
	}

	/**
	 * This method initializes jPopupMenuItemClear
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJPopupMenuItemClear() {
		if (jPopupMenuItemClear == null) {
			jPopupMenuItemClear = new JMenuItem();
			jPopupMenuItemClear.setText("Clear");  // Generated
			jPopupMenuItemClear.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					console.setText("");
				}
			});
		}
		return jPopupMenuItemClear;
	}

	/**
	 * This method initializes jTextPane
	 *
	 * @return javax.swing.JTextPane
	 */
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

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
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
							EditDialog ed = new EditDialog(mainFrame, true);
							ed.setIndex(subIndex);
							ed.setVisible(true);
							subIndex = ed.getIndex();
							(new Thread() { @Override
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
				}

				@Override
				public void mouseEntered(MouseEvent e) {}

				@Override
				public void mouseExited(MouseEvent e) {}

				@Override
				public void mousePressed(MouseEvent e) {}

				@Override
				public void mouseReleased(MouseEvent e) {}

			});
		}
		return jLayoutPane;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// NOP
	}

	/**
	 * This method initializes jComboBoxPalette
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxPalette() {
		if (jComboBoxPalette == null) {
			jComboBoxPalette = new JComboBox();
			jComboBoxPalette.setEnabled(false);
			jComboBoxPalette.setToolTipText("Select palette mode");
			jComboBoxPalette.setPreferredSize(new Dimension(120, 20));
			jComboBoxPalette.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (Core.isReady()) {
						int idx = jComboBoxPalette.getSelectedIndex();
						for (Core.PaletteMode m : Core.PaletteMode.values()) {
							if (idx == m.ordinal()) {
								Core.setPaletteMode(m);
								break;
							}
						}

						enableVobSubMenuCombo();

						(new Thread() { @Override
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
		}
		return jComboBoxPalette;
	}

	/**
	 * This method initializes jComboBoxFilter
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxFilter() {
		if (jComboBoxFilter == null) {
			jComboBoxFilter = new JComboBox();
			jComboBoxFilter.setEnabled(false);
			jComboBoxFilter.setToolTipText("Select filter for scaling");
			jComboBoxFilter.setPreferredSize(new Dimension(120, 20));
			jComboBoxFilter.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (Core.isReady()) {
						int idx = jComboBoxFilter.getSelectedIndex();
						for (Core.ScalingFilters s : Core.ScalingFilters.values()) {
							if (idx == s.ordinal()) {
								Core.setScalingFilter(s);
								break;
							}
						}

						(new Thread() { @Override
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

		}
		return jComboBoxFilter;
	}

}
