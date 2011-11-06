package deadbeef.gui;


import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


import deadbeef.core.Core;
import deadbeef.core.Core.OutputMode;
import deadbeef.tools.ToolBox;

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
 * Export dialog - part of BDSup2Sub GUI classes.
 *
 * @author 0xdeadbeef
 */
public class ExportDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JLabel jLabelFilename = null;

	private JTextField jTextFieldFileName = null;

	private JButton jButtonFileName = null;

	private JLabel jLabelLanguage = null;

	private JComboBox jComboBoxLanguage = null;

	private JButton jButtonCancel = null;

	private JButton jButtonSave = null;

	private JCheckBox jCheckBoxForced = null;

	private JCheckBox jCheckBoxWritePGCPal = null;


	/** reference to main window */
	private JFrame  mainFrame;
	/** file name used for export */
	private String  fileName;
	/** language index for VobSub export */
	private int     languageIdx;
	/** export only forced subtitles? */
	private boolean exportForced;
	/** export target palette in PGCEdit text format */
	private boolean writePGCPal;
	/** cancel state */
	private boolean cancel;
	/** semaphore to disable actions while changing component properties */
	private volatile boolean isReady = false;
	/** file extension */
	private String extension;

	/**
	 * Constructor
	 * @param owner parent frame
	 * @param modal show modal dialog
	 */
	public ExportDialog(Frame owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
		initialize();

		Point p = owner.getLocation();
		this.setLocation(p.x+owner.getWidth()/2-getWidth()/2, p.y+owner.getHeight()/2-getHeight()/2);
		this.setResizable(false);

		// init internal variables
		mainFrame = (JFrame)owner;
		fileName = "";
		languageIdx = Core.getLanguageIdx();
		exportForced = (Core.getNumForcedFrames() > 0) && Core.getExportForced();
		writePGCPal = Core.getWritePGCEditPal();
		cancel = false;

		// init components
		for (int i=0; i < Core.getLanguages().length; i++) {
			int n;
			if (Core.getOutputMode() == OutputMode.XML)
				n = 2;
			else
				n = 1;
			jComboBoxLanguage.addItem(Core.getLanguages()[i][0]+" ("+Core.getLanguages()[i][n]+")");
		}
		jComboBoxLanguage.setSelectedIndex(languageIdx);
		if (Core.getOutputMode() == Core.OutputMode.BDSUP)
			jComboBoxLanguage.setEnabled(false);

		if (Core.getOutputMode() == OutputMode.VOBSUB || Core.getOutputMode() == OutputMode.SUPIFO) {
			jCheckBoxWritePGCPal.setEnabled(true);
			jCheckBoxWritePGCPal.setSelected(writePGCPal);
		} else
			jCheckBoxWritePGCPal.setEnabled(false);

		if (Core.getNumForcedFrames() > 0)
			setForced(true, exportForced);
		else
			setForced(false, false);

		String sTitle;
		
		if (Core.getOutputMode() == Core.OutputMode.VOBSUB) {
			extension = "idx";
			sTitle = "SUB/IDX (VobSub)";
		} else if (Core.getOutputMode() == Core.OutputMode.SUPIFO) {
			extension = "ifo";
			sTitle = "SUP/IFO (SUP DVD)";
		} else if (Core.getOutputMode() == Core.OutputMode.BDSUP) {
			extension = "sup";
			sTitle = "BD SUP";
		} else {
			extension = "xml";
			sTitle = "XML (SONY BDN)";
		}
		
		setTitle("Export "+sTitle);

		isReady = true;
	}

	/**
	 * This method initializes this dialog
	 */
	private void initialize() {
		this.setPreferredSize(new Dimension(350, 160));
		this.setBounds(new Rectangle(0, 0, 350, 160));
		this.setMaximumSize(new Dimension(350, 160));
		this.setMinimumSize(new Dimension(350, 160));
		this.setContentPane(getJContentPane());
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				cancel = true;
				dispose();
			}
		});
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridwidth = 3;
			gridBagConstraints.gridy = 4;
			GridBagConstraints gridBagButtonSave = new GridBagConstraints();
			gridBagButtonSave.gridx = 2;
			gridBagButtonSave.insets = new Insets(2, 4, 2, 0);
			gridBagButtonSave.gridy = 7;
			GridBagConstraints gridBagButtonCancel = new GridBagConstraints();
			gridBagButtonCancel.gridx = 0;
			gridBagButtonCancel.insets = new Insets(2, 4, 2, 0);
			gridBagButtonCancel.gridy = 7;
			GridBagConstraints gridBagCheckBoxForced = new GridBagConstraints();
			gridBagCheckBoxForced.gridx = 0;
			gridBagCheckBoxForced.insets = new Insets(0, 0, 0, 0);
			gridBagCheckBoxForced.gridwidth = 2;
			gridBagCheckBoxForced.anchor = GridBagConstraints.NORTHWEST;
			gridBagCheckBoxForced.gridy = 3;
			GridBagConstraints gridBagComboLanguage = new GridBagConstraints();
			gridBagComboLanguage.fill = GridBagConstraints.NONE;
			gridBagComboLanguage.gridy = 1;
			gridBagComboLanguage.weightx = 1.0;
			gridBagComboLanguage.anchor = GridBagConstraints.WEST;
			gridBagComboLanguage.insets = new Insets(2, 4, 2, 4);
			gridBagComboLanguage.gridx = 1;
			GridBagConstraints gridBagLabelLanguage = new GridBagConstraints();
			gridBagLabelLanguage.gridx = 0;
			gridBagLabelLanguage.anchor = GridBagConstraints.WEST;
			gridBagLabelLanguage.insets = new Insets(2, 6, 2, 0);
			gridBagLabelLanguage.gridy = 1;
			jLabelLanguage = new JLabel();
			jLabelLanguage.setText("Language");
			GridBagConstraints gridBagButtonFileName = new GridBagConstraints();
			gridBagButtonFileName.gridx = 2;
			gridBagButtonFileName.anchor = GridBagConstraints.EAST;
			gridBagButtonFileName.insets = new Insets(2, 0, 2, 4);
			gridBagButtonFileName.gridy = 0;
			GridBagConstraints gridBagTextFileName = new GridBagConstraints();
			gridBagTextFileName.fill = GridBagConstraints.NONE;
			gridBagTextFileName.gridy = 0;
			gridBagTextFileName.weightx = 1.0D;
			gridBagTextFileName.anchor = GridBagConstraints.WEST;
			gridBagTextFileName.insets = new Insets(2, 4, 2, 4);
			gridBagTextFileName.ipadx = 200;
			gridBagTextFileName.gridx = 1;
			GridBagConstraints gridBagLabelFilename = new GridBagConstraints();
			gridBagLabelFilename.gridx = 0;
			gridBagLabelFilename.anchor = GridBagConstraints.WEST;
			gridBagLabelFilename.insets = new Insets(2, 6, 2, 0);
			gridBagLabelFilename.gridy = 0;
			jLabelFilename = new JLabel();
			jLabelFilename.setText("Filename");
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(jLabelFilename, gridBagLabelFilename);
			jContentPane.add(getJTextFieldFileName(), gridBagTextFileName);
			jContentPane.add(getJButtonFileName(), gridBagButtonFileName);
			jContentPane.add(jLabelLanguage, gridBagLabelLanguage);
			jContentPane.add(getJComboBoxLanguage(), gridBagComboLanguage);
			jContentPane.add(getJButtonCancel(), gridBagButtonCancel);
			jContentPane.add(getJButtonSave(), gridBagButtonSave);
			jContentPane.add(getJCheckBoxForced(), gridBagCheckBoxForced);
			jContentPane.add(getJCheckBoxWritePGCPal(), gridBagConstraints);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTextFieldFileName
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldFileName() {
		if (jTextFieldFileName == null) {
			jTextFieldFileName = new JTextField();
			jTextFieldFileName.setPreferredSize(new Dimension(200, 20));
			jTextFieldFileName.setHorizontalAlignment(JTextField.LEADING);
			jTextFieldFileName.setToolTipText("Set file name for export");
			jTextFieldFileName.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						String s = jTextFieldFileName.getText();
						if (s != null)
							fileName = ToolBox.stripExtension(s)+"."+extension;
					}
				}
			});
		}
		return jTextFieldFileName;
	}

	/**
	 * This method initializes jButtonFileName
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonFileName() {
		if (jButtonFileName == null) {
			jButtonFileName = new JButton();
			jButtonFileName.setText("Browse");
			jButtonFileName.setMnemonic('b');
			jButtonFileName.setToolTipText("Open file dialog to select file name for export");
			jButtonFileName.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						String[] ext = new String[1];
						ext[0] = extension;
						String p = ToolBox.getPathName(fileName);
						String fn = ToolBox.getFileName(fileName);
						String fname = ToolBox.getFileName(p, fn, ext, false, mainFrame);
						if (fname != null) {
							fileName = ToolBox.stripExtension(fname)+"."+extension;
							jTextFieldFileName.setText(fileName);
						}
					}
				}
			});
		}
		return jButtonFileName;
	}

	/**
	 * This method initializes jComboBoxLanguage
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxLanguage() {
		if (jComboBoxLanguage == null) {
			jComboBoxLanguage = new JComboBox();
			jComboBoxLanguage.setPreferredSize(new Dimension(200, 20));
			jComboBoxLanguage.setEditable(false);
			jComboBoxLanguage.setToolTipText("Set language identifier");
			jComboBoxLanguage.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (isReady)
						languageIdx = jComboBoxLanguage.getSelectedIndex();
				}
			});
		}
		return jComboBoxLanguage;
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
			jButtonCancel.setToolTipText("Cancel export and return");
			jButtonCancel.setMnemonic('c');
			jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cancel = true;
					dispose();
				}
			});
		}
		return jButtonCancel;
	}

	/**
	 * This method initializes jButtonSave
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonSave() {
		if (jButtonSave == null) {
			jButtonSave = new JButton();
			jButtonSave.setText("Save");
			jButtonSave.setToolTipText("Start creation of export stream");
			jButtonSave.setMnemonic('s');
			jButtonSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						// read values of editable boxes
						String s;
						// file name
						s = jTextFieldFileName.getText();
						if (s != null)
							fileName = ToolBox.stripExtension(s)+"."+extension;
						// exit
						Core.setExportForced(exportForced);
						Core.setLanguageIdx(languageIdx);
						if (Core.getOutputMode() == OutputMode.VOBSUB || Core.getOutputMode() == OutputMode.SUPIFO)
							Core.setWritePGCEditPal(writePGCPal);
						cancel = false;
						dispose();
					}
				}
			});
		}
		return jButtonSave;
	}

	/**
	 * get file name used for export
	 * @return file name used for export
	 */
	public String getFileName() {
		if (fileName.length() == 0)
			return null;
		return fileName;
	}

	/**
	 * set file name used for export
	 * @param fn file name used for export
	 */
	public void setFileName(final String fn) {
		fileName = fn;
		jTextFieldFileName.setText(fileName);
	}

	/**
	 * enable and set state of the "export forced" checkbox
	 * @param en enable checkbox
	 * @param set checkbox select state
	 */
	private void setForced(final boolean en, final boolean set) {
		exportForced = set;
		jCheckBoxForced.setEnabled(en);
		jCheckBoxForced.setSelected(set);
	}

	/**
	 * get cancel state
	 * @return true if canceled
	 */
	public boolean wasCanceled() {
		return cancel;
	}

	/**
	 * This method initializes jCheckBoxForced
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxForced() {
		if (jCheckBoxForced == null) {
			jCheckBoxForced = new JCheckBox();
			jCheckBoxForced.setToolTipText("Export only subpictures marked as 'forced'");
			jCheckBoxForced.setText("                 Export only forced");
			jCheckBoxForced.setMnemonic('f');
			jCheckBoxForced.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (isReady) {
						exportForced = jCheckBoxForced.isSelected();
					}
				}
			});
		}
		return jCheckBoxForced;
	}

	/**
	 * This method initializes jCheckBoxWritePGCPal
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxWritePGCPal() {
		if (jCheckBoxWritePGCPal == null) {
			jCheckBoxWritePGCPal = new JCheckBox();
			jCheckBoxWritePGCPal.setToolTipText("Export palette in PGCEdit text format (RGB, 0..255)");
			jCheckBoxWritePGCPal.setText("                 Export palette in PGCEdit text format");
			jCheckBoxWritePGCPal.setMnemonic('p');
			jCheckBoxWritePGCPal.setDisplayedMnemonicIndex(24);
			jCheckBoxWritePGCPal.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (isReady) {
						writePGCPal = jCheckBoxWritePGCPal.isSelected();
					}
				}
			});
		}
		return jCheckBoxWritePGCPal;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
