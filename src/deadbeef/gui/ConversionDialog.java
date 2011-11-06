package deadbeef.gui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import deadbeef.core.Core;
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
 * Dialog for conversion options - part of BDSup2Sub GUI classes.
 *
 * @author 0xdeadbeef
 */
public class ConversionDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JPanel jPanelResolution = null;

	private JComboBox jComboBoxResolution = null;

	private JCheckBox jCheckBoxFrameRate = null;

	private JCheckBox jCheckBoxResolution = null;	

	private JPanel jPanelFPS = null;

	private JComboBox jComboBoxFPSSrc = null;

	private JComboBox jComboBoxFPSTrg = null;
	
	
	private JPanel jPanelMove = null;
	
	private JCheckBox jCheckBoxMove = null;


	private JPanel jPanelTimes = null;

	private JTextField jTextFieldDelay = null;

	private JCheckBox jCheckBoxFixMinTime = null;

	private JTextField jTextFieldMinTime = null;

	private JTextField fpsTrgEditor = null;

	private JTextField fpsSrcEditor = null;


	private JPanel jPanelDefaults = null;

	private JButton jButtonStore = null;

	private JButton jButtonRestore = null;

	private JButton jButtonReset = null;


	private JPanel jPanelButtons = null;

	private JButton jButtonOk = null;

	private JButton jButtonCancel = null;


	private JPanel jPanelScale = null;

	private JCheckBox jCheckBoxScale = null;

	private JTextField jTextFieldScaleX = null;

	private JTextField jTextFieldScaleY = null;

	private JPanel jPanelForced = null;

	private JComboBox jComboBoxForced = null;


	/** background color for errors */
	private final Color errBgnd = new Color(0xffe1acac);
	/** background color for warnings */
	private final Color warnBgnd = new Color(0xffffffc0);
	/** background color for ok */
	private final Color okBgnd = UIManager.getColor("TextField.background");

	/** selected output resolution */
	private Core.Resolution resolution;
	/** selected delay in 90kHz resolution */
	private int     delayPTS;
	/** selected minimum frame time in 90kHz resolution */
	private int     minTimePTS;
	/** flag that tells whether to convert the frame rate or not */
	private boolean changeFPS;
	/** flag that tells whether to convert the resolution or not */
	private boolean changeResolution;
	/** flag that tells whether to fix frames shorter than a minimum time */
	private boolean fixShortFrames;
	/** source frame rate */
	private double  fpsSrc;
	/** target frame rate */
	private double  fpsTrg;
	/** cancel state */
	private boolean cancel;
	/** semaphore to disable actions while changing component properties */
	private volatile boolean isReady = false;
	/** flag that tells whether to use free scaling or not */
	private boolean changeScale;
	/** X scaling factor */
	private double scaleX;
	/** Y scaling factor */
	private double scaleY;
	/** source fps is certain */
	private boolean fpsSrcCertain;
	/** clear/set all forced flags */
	private Core.SetState forcedState;
	/** apply move settings */
	private boolean moveCaptions;

	static Dimension lDim = new Dimension(70,20);

	/**
	 * Constructor
	 * @param owner parent frame
	 * @param modal show as modal dialog
	 */
	public ConversionDialog(Frame owner, boolean modal) {
		super(owner, modal);

		// initialize internal variables
		fpsTrgEditor = new JTextField();
		fpsSrcEditor = new JTextField();

		// TODO Auto-generated constructor stub
		initialize();

		// center to parent frame
		Point p = owner.getLocation();
		this.setLocation(p.x+owner.getWidth()/2-getWidth()/2, p.y+owner.getHeight()/2-getHeight()/2);

		this.setResizable(false);

		changeResolution = Core.getConvertResolution();
		// fix output resolution in case that it should not be changed
		// change target resolution to source resolution if no conversion is needed
		if (!changeResolution && Core.getNumFrames()>0)
			resolution = Core.getResolution(Core.getSubPictureSrc(0).width, Core.getSubPictureSrc(0).height);
		else
			resolution = Core.getOutputResolution();
		
		moveCaptions = Core.getMoveCaptions();
		jCheckBoxMove.setEnabled(false);
		jCheckBoxMove.setSelected(moveCaptions);

		delayPTS = Core.getDelayPTS();
		minTimePTS = (int)Core.syncTimePTS(Core.getMinTimePTS(), Core.getFPSTrg());
		changeFPS = Core.getConvertFPS();
		changeScale = Core.getApplyFreeScale();
		fixShortFrames = Core.getFixShortFrames();
		fpsSrc = Core.getFPSSrc();
		fpsTrg = Core.getFPSTrg();
		scaleX = Core.getFreeScaleX();
		scaleY = Core.getFreeScaleY();
		fpsSrcCertain = Core.getFpsSrcCertain();
		cancel = false;

		// fill comboboxes and text fields
		for (Core.Resolution r : Core.Resolution.values()) {
			jComboBoxResolution.addItem(Core.getResolutionName(r));
		}

		jComboBoxFPSSrc.addItem("23.975");
		jComboBoxFPSSrc.addItem("23.976");
		jComboBoxFPSSrc.addItem("24");
		jComboBoxFPSSrc.addItem("25");
		jComboBoxFPSSrc.addItem("29.97");
		jComboBoxFPSSrc.addItem("50");
		jComboBoxFPSSrc.addItem("59.94");

		jComboBoxFPSTrg.addItem("23.975");
		jComboBoxFPSTrg.addItem("23.976");
		jComboBoxFPSTrg.addItem("24");
		jComboBoxFPSTrg.addItem("25");
		jComboBoxFPSTrg.addItem("29.97");
		jComboBoxFPSTrg.addItem("50");
		jComboBoxFPSTrg.addItem("59.94");

		jComboBoxFPSSrc.setEditor(new MyComboBoxEditor(fpsSrcEditor));
		jComboBoxFPSTrg.setEditor(new MyComboBoxEditor(fpsTrgEditor));

		// note: order has to be ordinal order of enum!
		forcedState = Core.getForceAll();
		jComboBoxForced.addItem("keep      ");
		jComboBoxForced.addItem("set all   ");
		jComboBoxForced.addItem("clear all ");

		fillDialog();

		isReady = true;
	}

	/**
	 * Enter values into dialog elements
	 */
	private void fillDialog() {
		jComboBoxResolution.setSelectedIndex(resolution.ordinal());
		jComboBoxResolution.setEnabled(changeResolution);
		jCheckBoxResolution.setSelected(changeResolution);

		jTextFieldDelay.setText(ToolBox.formatDouble(delayPTS/90.0));

		jCheckBoxFrameRate.setSelected(changeFPS);
		jComboBoxFPSSrc.setSelectedItem(ToolBox.formatDouble(fpsSrc));
		jComboBoxFPSSrc.setEnabled(changeFPS);
		jComboBoxFPSTrg.setSelectedItem(ToolBox.formatDouble(fpsTrg));
		jComboBoxFPSTrg.setEnabled(true);

		jTextFieldMinTime.setText(ToolBox.formatDouble(minTimePTS/90.0));
		jCheckBoxFixMinTime.setEnabled(true);
		jCheckBoxFixMinTime.setSelected(fixShortFrames);


		jCheckBoxFixMinTime.setSelected(fixShortFrames);
		jTextFieldMinTime.setEnabled(fixShortFrames);

		jCheckBoxScale.setSelected(changeScale);
		jTextFieldScaleX.setText(ToolBox.formatDouble(scaleX));
		jTextFieldScaleX.setEnabled(changeScale);
		jTextFieldScaleY.setText(ToolBox.formatDouble(scaleY));
		jTextFieldScaleY.setEnabled(changeScale);

		jComboBoxForced.setSelectedIndex(forcedState.ordinal());
	}

	/**
	 * This method initializes this dialog
	 */
	private void initialize() {
		this.setSize(500, 350);
		this.setTitle("Conversion Options");
		this.setPreferredSize(new Dimension(500, 350));
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
	 * This method initializes jPanelResolution
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelResolution() {
		if (jPanelResolution == null) {
			GridBagConstraints gridBagCheckBoxResolution = new GridBagConstraints();
			gridBagCheckBoxResolution.gridx = 0;
			gridBagCheckBoxResolution.gridy = 0;
			gridBagCheckBoxResolution.anchor = GridBagConstraints.WEST;
			gridBagCheckBoxResolution.gridwidth = 2;

			GridBagConstraints gridBagLabelResolution = new GridBagConstraints();
			gridBagLabelResolution.gridx = 0;
			gridBagLabelResolution.gridy = 1;
			gridBagLabelResolution.anchor = GridBagConstraints.WEST;
			gridBagLabelResolution.insets = new Insets(2, 6, 2, 0);

			GridBagConstraints gridBagComboResolution = new GridBagConstraints();
			gridBagComboResolution.gridx = 1;
			gridBagComboResolution.gridy = 1;
			gridBagComboResolution.weightx = 1.0;
			gridBagComboResolution.anchor = GridBagConstraints.WEST;
			gridBagComboResolution.insets = new Insets(2, 4, 2, 4);

			jPanelResolution = new JPanel();
			jPanelResolution.setLayout(new GridBagLayout());
			jPanelResolution.setBorder(BorderFactory.createTitledBorder(null, "Resolution", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanelResolution.setMinimumSize(new Dimension(200, 70));
			jPanelResolution.setPreferredSize(new Dimension(200, 70));

			JLabel label = new JLabel("Resolution");
			label.setMinimumSize(lDim);
			jPanelResolution.add(getJCheckBoxResolution(), gridBagCheckBoxResolution);
			jPanelResolution.add(label, gridBagLabelResolution);
			jPanelResolution.add(getJComboBoxResolution(), gridBagComboResolution);

		}
		return jPanelResolution;
	}

	/**
	 * This method initializes jPanelMove
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelMove() {
		if (jPanelMove == null) {
			GridBagConstraints gridBagCheckBoxMove = new GridBagConstraints();
			gridBagCheckBoxMove.gridx = 0;
			gridBagCheckBoxMove.gridy = 0;
			gridBagCheckBoxMove.anchor = GridBagConstraints.WEST;
			gridBagCheckBoxMove.weightx = 1.0;
			//gridBagCheckBoxMove.insets = new Insets(2, 6, 2, 0);

			jPanelMove = new JPanel();
			jPanelMove.setLayout(new GridBagLayout());
			jPanelMove.setBorder(BorderFactory.createTitledBorder(null, "Move", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanelMove.setMinimumSize(new Dimension(200, 50));
			jPanelMove.setPreferredSize(new Dimension(200, 50));

			jPanelMove.add(getJCheckBoxMove(), gridBagCheckBoxMove);

		}
		return jPanelMove;
	}

	
	/**
	 * This method initializes jPanelFPS
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelFPS() {
		if (jPanelFPS == null) {
			GridBagConstraints gridBagCheckBoxFrameRate = new GridBagConstraints();
			gridBagCheckBoxFrameRate.gridx = 0;
			gridBagCheckBoxFrameRate.gridy = 0;
			gridBagCheckBoxFrameRate.anchor = GridBagConstraints.WEST;
			gridBagCheckBoxFrameRate.gridwidth = 2;

			GridBagConstraints gridBagLabelFPSSrc = new GridBagConstraints();
			gridBagLabelFPSSrc.gridx = 0;
			gridBagLabelFPSSrc.gridy = 1;
			gridBagLabelFPSSrc.anchor = GridBagConstraints.WEST;
			gridBagLabelFPSSrc.insets = new Insets(2, 6, 2, 0);

			GridBagConstraints gridBagComboFPSSrc = new GridBagConstraints();
			gridBagComboFPSSrc.gridx = 1;
			gridBagComboFPSSrc.gridy = 1;
			//gridBagComboFPSSrc.fill = GridBagConstraints.NONE;
			gridBagComboFPSSrc.weightx = 1.0;
			gridBagComboFPSSrc.anchor = GridBagConstraints.WEST;
			gridBagComboFPSSrc.insets = new Insets(2, 4, 2, 4);

			GridBagConstraints gridBagLabelFPSTrg = new GridBagConstraints();
			gridBagLabelFPSTrg.gridx = 0;
			gridBagLabelFPSTrg.gridy = 2;
			gridBagLabelFPSTrg.anchor = GridBagConstraints.WEST;
			gridBagLabelFPSTrg.insets = new Insets(2, 6, 2, 0);

			GridBagConstraints gridBagComboFPSTrg = new GridBagConstraints();
			//gridBagComboFPSTrg.fill = GridBagConstraints.VERTICAL;
			gridBagComboFPSTrg.gridx = 1;
			gridBagComboFPSTrg.gridy = 2;
			gridBagComboFPSTrg.weightx = 1.0;
			gridBagComboFPSTrg.anchor = GridBagConstraints.WEST;
			gridBagComboFPSTrg.insets = new Insets(2, 4, 2, 4);

			jPanelFPS = new JPanel();
			jPanelFPS.setLayout(new GridBagLayout());
			jPanelFPS.setBorder(BorderFactory.createTitledBorder(null, "Framerate", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanelFPS.setMinimumSize(new Dimension(200, 100));
			jPanelFPS.setPreferredSize(new Dimension(200, 100));

			jPanelFPS.add(getJCheckBoxFrameRate(), gridBagCheckBoxFrameRate);
			JLabel label = new JLabel("FPS Source");
			label.setMinimumSize(lDim);
			jPanelFPS.add(label, gridBagLabelFPSSrc);
			label = new JLabel("FPS Target");
			label.setMinimumSize(lDim);
			jPanelFPS.add(label, gridBagLabelFPSTrg);
			jPanelFPS.add(getJComboBoxFPSSrc(), gridBagComboFPSSrc);
			jPanelFPS.add(getJComboBoxFPSTrg(), gridBagComboFPSTrg);
		}
		return jPanelFPS;
	}

	/**
	 * This method initializes jPanelTimes
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelTimes() {
		if (jPanelTimes == null) {
			GridBagConstraints gridBagLabelDelay = new GridBagConstraints();
			gridBagLabelDelay.gridx = 0;
			gridBagLabelDelay.gridy = 0;
			gridBagLabelDelay.anchor = GridBagConstraints.WEST;
			gridBagLabelDelay.insets = new Insets(2, 6, 2, 0);

			GridBagConstraints gridBagTextDelay = new GridBagConstraints();
			gridBagTextDelay.gridx = 1;
			gridBagTextDelay.gridy = 0;
			gridBagTextDelay.weightx = 1.0D;
			gridBagTextDelay.anchor = GridBagConstraints.WEST;
			gridBagTextDelay.insets = new Insets(2, 4, 2, 4);
			gridBagTextDelay.ipadx = 100;

			GridBagConstraints gridBagCheckBoxFixMinTime = new GridBagConstraints();
			gridBagCheckBoxFixMinTime.gridx = 0;
			gridBagCheckBoxFixMinTime.gridy = 1;
			gridBagCheckBoxFixMinTime.anchor = GridBagConstraints.WEST;
			gridBagCheckBoxFixMinTime.gridwidth = 2;
			gridBagCheckBoxFixMinTime.insets = new Insets(0, 0, 0, 0);

			GridBagConstraints gridBagLabelMinTime = new GridBagConstraints();
			gridBagLabelMinTime.gridx = 0;
			gridBagLabelMinTime.gridy = 2;
			gridBagLabelMinTime.anchor = GridBagConstraints.WEST;
			gridBagLabelMinTime.insets = new Insets(2, 6, 2, 2);

			GridBagConstraints gridBagTextMinTime = new GridBagConstraints();
			gridBagTextMinTime.gridx = 1;
			gridBagTextMinTime.gridy = 2;
			gridBagTextMinTime.anchor = GridBagConstraints.WEST;
			gridBagTextMinTime.weightx = 1.0;
			gridBagTextMinTime.ipadx = 100;
			gridBagTextMinTime.insets = new Insets(2, 4, 2, 4);

			jPanelTimes = new JPanel();
			jPanelTimes.setLayout(new GridBagLayout());
			jPanelTimes.setBorder(BorderFactory.createTitledBorder(null, "Times", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanelTimes.setMinimumSize(new Dimension(200, 100));
			jPanelTimes.setPreferredSize(new Dimension(200, 100));

			JLabel label = new JLabel("Delay (ms)");
			label.setMinimumSize(lDim);
			jPanelTimes.add(label, gridBagLabelDelay);
			jPanelTimes.add(getJTextFieldDelay(), gridBagTextDelay);
			jPanelTimes.add(getJCheckBoxFixMineTime(), gridBagCheckBoxFixMinTime);
			jPanelTimes.add(getJTextFieldMinTime(), gridBagTextMinTime);
			label = new JLabel("Min Time (ms)");
			label.setMinimumSize(lDim);
			jPanelTimes.add(label, gridBagLabelMinTime);
		}
		return jPanelTimes;
	}

	/**
	 * This method initializes jPanelScale
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelScale() {
		if (jPanelScale == null) {
			GridBagConstraints gridBagCheckBoxScale = new GridBagConstraints();
			gridBagCheckBoxScale.gridx = 0;
			gridBagCheckBoxScale.gridy = 0;
			gridBagCheckBoxScale.anchor = GridBagConstraints.WEST;
			gridBagCheckBoxScale.gridwidth = 2;

			GridBagConstraints gridBagLabelScaleX = new GridBagConstraints();
			gridBagLabelScaleX.gridx = 0;
			gridBagLabelScaleX.gridy = 1;
			gridBagLabelScaleX.anchor = GridBagConstraints.WEST;
			gridBagLabelScaleX.insets = new Insets(2, 6, 2, 2);

			GridBagConstraints gridBagTextScaleX = new GridBagConstraints();
			gridBagTextScaleX.gridx = 1;
			gridBagTextScaleX.gridy = 1;
			gridBagTextScaleX.anchor = GridBagConstraints.WEST;
			gridBagTextScaleX.insets = new Insets(2, 4, 2, 4);
			gridBagTextScaleX.weightx = 1.0;
			gridBagTextScaleX.ipadx = 100;

			GridBagConstraints gridBagLabelScaleY = new GridBagConstraints();
			gridBagLabelScaleY.gridx = 0;
			gridBagLabelScaleY.gridy = 2;
			gridBagLabelScaleY.anchor = GridBagConstraints.WEST;
			gridBagLabelScaleY.insets = new Insets(2, 6, 2, 2);

			GridBagConstraints gridBagTextScaleY = new GridBagConstraints();
			gridBagTextScaleY.gridx = 1;
			gridBagTextScaleY.gridy = 2;
			gridBagTextScaleY.anchor = GridBagConstraints.WEST;
			gridBagTextScaleY.insets = new Insets(2, 4, 2, 4);
			gridBagTextScaleY.weightx = 1.0;
			gridBagTextScaleY.ipadx = 100;

			jPanelScale = new JPanel();
			jPanelScale.setLayout(new GridBagLayout());
			jPanelScale.setBorder(BorderFactory.createTitledBorder(null, "Scale", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanelScale.setMinimumSize(new Dimension(200, 100));
			jPanelScale.setPreferredSize(new Dimension(200, 100));

			jPanelScale.add(getJCheckBoxScale(), gridBagCheckBoxScale);
			JLabel label = new JLabel("Scale X");
			label.setMinimumSize(lDim);
			jPanelScale.add(label, gridBagLabelScaleX);
			jPanelScale.add(getJTextFieldScaleX(), gridBagTextScaleX);
			label = new JLabel("Scale Y");
			label.setMinimumSize(lDim);
			jPanelScale.add(label, gridBagLabelScaleY);
			jPanelScale.add(getJTextFieldScaleY(), gridBagTextScaleY);
		}
		return jPanelScale;
	}

	/**
	 * This method initializes jPanelButtons
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelDefaults() {
		if (jPanelDefaults == null) {
			GridBagConstraints gridBagButtonStore = new GridBagConstraints();
			gridBagButtonStore.gridx = 0;
			gridBagButtonStore.gridy = 0;
			gridBagButtonStore.anchor = GridBagConstraints.WEST;
			gridBagButtonStore.weightx = 1.0;
			gridBagButtonStore.insets = new Insets(2, 0, 2, 0);

			GridBagConstraints gridBagButtonRestore = new GridBagConstraints();
			gridBagButtonRestore.gridx = 1;
			gridBagButtonRestore.gridy = 0;
			gridBagButtonRestore.anchor = GridBagConstraints.CENTER;
			gridBagButtonRestore.weightx = 1.0;
			gridBagButtonRestore.insets = new Insets(2, 0, 2, 0);

			GridBagConstraints gridBagButtonReset = new GridBagConstraints();
			gridBagButtonReset.gridx = 2;
			gridBagButtonReset.gridy = 0;
			gridBagButtonReset.anchor = GridBagConstraints.EAST;
			gridBagButtonReset.weightx = 1.0;
			gridBagButtonReset.insets = new Insets(2, 4, 2, 0);

			jPanelDefaults = new JPanel();
			jPanelDefaults.setLayout(new GridBagLayout());
			jPanelDefaults.setBorder(BorderFactory.createTitledBorder(null, "Defaults", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanelDefaults.setMinimumSize(new Dimension(200, 60));
			jPanelDefaults.setPreferredSize(new Dimension(200, 60));

			jPanelDefaults.add(getJButtonStore(), gridBagButtonStore);
			jPanelDefaults.add(getJButtonRestore(), gridBagButtonRestore);
			jPanelDefaults.add(getJButtonReset(), gridBagButtonReset);
		}
		return jPanelDefaults;
	}

	/**
	 * This method initializes jPanelForced
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelForced() {
		if (jPanelForced == null) {

			GridBagConstraints gridBagLabelForced = new GridBagConstraints();
			gridBagLabelForced.gridx = 0;
			gridBagLabelForced.gridy = 0;
			gridBagLabelForced.anchor = GridBagConstraints.WEST;
			gridBagLabelForced.insets = new Insets(2, 6, 2, 0);

			GridBagConstraints gridBagComboForced = new GridBagConstraints();
			gridBagComboForced.gridx = 1;
			gridBagComboForced.gridy = 0;
			gridBagComboForced.weightx = 1.0;
			gridBagComboForced.anchor = GridBagConstraints.WEST;
			gridBagComboForced.insets = new Insets(2, 4, 2, 4);

			jPanelForced = new JPanel();
			jPanelForced.setLayout(new GridBagLayout());
			jPanelForced.setBorder(BorderFactory.createTitledBorder(null, "Global forced flags", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanelForced.setMinimumSize(new Dimension(200, 70));
			jPanelForced.setPreferredSize(new Dimension(200, 70));

			JLabel label = new JLabel("Force all");
			label.setMinimumSize(lDim);
			jPanelForced.add(label, gridBagLabelForced);
			jPanelForced.add(getJComboBoxForced(), gridBagComboForced);

		}
		return jPanelForced;
	}

	/**
	 * This method initializes jPanelButtons
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			GridBagConstraints gridBagButtonCancel = new GridBagConstraints();
			gridBagButtonCancel.gridx = 0;
			gridBagButtonCancel.gridy = 0;
			gridBagButtonCancel.anchor = GridBagConstraints.WEST;
			gridBagButtonCancel.weightx = 1.0;
			gridBagButtonCancel.insets = new Insets(2, 8, 4, 0);

			GridBagConstraints gridBagButtonOk = new GridBagConstraints();
			gridBagButtonOk.gridx = 3;
			gridBagButtonOk.gridy = 0;
			gridBagButtonOk.anchor = GridBagConstraints.EAST;
			gridBagButtonOk.weightx = 1.0;
			gridBagButtonOk.insets = new Insets(2, 4, 4, 8);

			jPanelButtons = new JPanel();
			jPanelButtons.setLayout(new GridBagLayout());
			jPanelButtons.setMinimumSize(new Dimension(200, 30));
			jPanelButtons.setPreferredSize(new Dimension(200, 30));

			jPanelButtons.add(getJButtonOk(), gridBagButtonOk);
			jPanelButtons.add(getJButtonCancel(), gridBagButtonCancel);
		}
		return jPanelButtons;
	}


	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagPanelResolution = new GridBagConstraints();
			gridBagPanelResolution.gridx = 0;
			gridBagPanelResolution.gridy = 0;
			gridBagPanelResolution.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelResolution.weightx = 1.0;
			gridBagPanelResolution.weighty = 1.0;
			gridBagPanelResolution.fill = GridBagConstraints.BOTH;

			GridBagConstraints gridBagPanelMove = new GridBagConstraints();
			gridBagPanelMove.gridx = 0;
			gridBagPanelMove.gridy = 1;
			gridBagPanelMove.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelMove.weightx = 1.0;
			gridBagPanelMove.weighty = 1.0;
			gridBagPanelMove.fill = GridBagConstraints.BOTH;
			
			GridBagConstraints gridBagPanelScale = new GridBagConstraints();
			gridBagPanelScale.gridx = 1;
			gridBagPanelScale.gridy = 0;
			gridBagPanelScale.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelScale.weightx = 1.0;
			gridBagPanelScale.weighty = 1.0;
			gridBagPanelScale.gridheight = 2;
			gridBagPanelScale.fill = GridBagConstraints.BOTH;

			GridBagConstraints gridBagPanelFPS = new GridBagConstraints();
			gridBagPanelFPS.gridx = 0;
			gridBagPanelFPS.gridy = 2;
			gridBagPanelFPS.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelFPS.weightx = 1.0;
			gridBagPanelFPS.weighty = 1.0;
			gridBagPanelFPS.fill = GridBagConstraints.BOTH;

			GridBagConstraints gridBagPanelTimes = new GridBagConstraints();
			gridBagPanelTimes.gridx = 1;
			gridBagPanelTimes.gridy = 2;
			gridBagPanelTimes.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelTimes.weightx = 1.0;
			gridBagPanelTimes.weighty = 1.0;
			gridBagPanelTimes.fill = GridBagConstraints.BOTH;

			GridBagConstraints gridBagPanelForced = new GridBagConstraints();
			gridBagPanelForced.gridx = 0;
			gridBagPanelForced.gridy = 3;
			gridBagPanelForced.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelForced.weightx = 1.0;
			gridBagPanelForced.weighty = 1.0;
			gridBagPanelForced.fill = GridBagConstraints.BOTH;

			GridBagConstraints gridBagPanelDefaults = new GridBagConstraints();
			gridBagPanelDefaults.gridx = 1;
			gridBagPanelDefaults.gridy = 3;
			gridBagPanelDefaults.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelDefaults.weightx = 1.0;
			gridBagPanelDefaults.weighty = 1.0;
			gridBagPanelDefaults.fill = GridBagConstraints.BOTH;

			GridBagConstraints gridBagPanelButtons = new GridBagConstraints();
			gridBagPanelButtons.gridx = 0;
			gridBagPanelButtons.gridy = 4;
			gridBagPanelButtons.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelButtons.weightx = 1.0;
			gridBagPanelButtons.weighty = 1.0;
			gridBagPanelButtons.gridwidth = 2;
			gridBagPanelButtons.fill = GridBagConstraints.HORIZONTAL;

			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());

			jContentPane.add(getJPanelResolution(), gridBagPanelResolution);
			jContentPane.add(getJPanelMove(), gridBagPanelMove);
			jContentPane.add(getJPanelFPS(), gridBagPanelFPS);
			jContentPane.add(getJPanelTimes(), gridBagPanelTimes);
			jContentPane.add(getJPanelScale(), gridBagPanelScale);
			jContentPane.add(getJPanelForced(), gridBagPanelForced);
			jContentPane.add(getJPanelDefaults(), gridBagPanelDefaults);
			jContentPane.add(getJPanelButtons(), gridBagPanelButtons);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jComboBoxResolution
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxResolution() {
		if (jComboBoxResolution == null) {
			jComboBoxResolution = new JComboBox();
			jComboBoxResolution.setPreferredSize(new Dimension(200, 20));
			jComboBoxResolution.setMinimumSize(new Dimension(150, 20));
			jComboBoxResolution.setEditable(false);
			jComboBoxResolution.setToolTipText("Select the target resolution");
			jComboBoxResolution.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (isReady) {
						int idx = jComboBoxResolution.getSelectedIndex();
						for (Core.Resolution r : Core.Resolution.values()) {
							if (idx == r.ordinal()) {
								resolution = r;
								if (!Core.getKeepFps())
									fpsTrg = Core.getDefaultFPS(r);
								jComboBoxFPSTrg.setSelectedItem(ToolBox.formatDouble(fpsTrg));
								break;
							}
						}
					}
				}
			});
		}
		return jComboBoxResolution;
	}

	/**
	 * This method initializes jCheckBoxFrameRate
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxFrameRate() {
		if (jCheckBoxFrameRate == null) {
			jCheckBoxFrameRate = new JCheckBox();
			jCheckBoxFrameRate.setToolTipText("Convert frame rate from FPS Source to FPS target");
			jCheckBoxFrameRate.setText("Change frame rate");
			jCheckBoxFrameRate.setMnemonic('f');
			jCheckBoxFrameRate.setFocusable(false);
			jCheckBoxFrameRate.setIconTextGap(10);
			jCheckBoxFrameRate.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						changeFPS = jCheckBoxFrameRate.isSelected();
						jComboBoxFPSSrc.setEnabled(changeFPS);
					}
				}
			});
		}
		return jCheckBoxFrameRate;
	}

	/**
	 * This method initializes jCheckBoxResolution
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxResolution() {
		if (jCheckBoxResolution == null) {
			jCheckBoxResolution = new JCheckBox();
			jCheckBoxResolution.setToolTipText("Convert resolution");
			jCheckBoxResolution.setText("Convert resolution");
			jCheckBoxResolution.setMnemonic('r');
			jCheckBoxResolution.setDisplayedMnemonicIndex(8);
			jCheckBoxResolution.setFocusable(false);
			jCheckBoxResolution.setIconTextGap(10);
			jCheckBoxResolution.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						changeResolution = jCheckBoxResolution.isSelected();
						jComboBoxResolution.setEnabled(changeResolution);
					}
				}
			});
		}
		return jCheckBoxResolution;
	}

	/**
	 * This method initializes jCheckBoxMove
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxMove() {
		if (jCheckBoxMove == null) {
			jCheckBoxMove = new JCheckBox();
			jCheckBoxMove.setToolTipText("Apply settings for moving captions");
			jCheckBoxMove.setText("Apply 'move all' settings");
			jCheckBoxMove.setMnemonic('k');
			jCheckBoxMove.setDisplayedMnemonicIndex(8);
			jCheckBoxMove.setFocusable(false);
			jCheckBoxMove.setIconTextGap(10);
			jCheckBoxMove.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) 
						moveCaptions = jCheckBoxMove.isSelected();					
				}
			});
		}
		return jCheckBoxMove;
	}

	/**
	 * This method initializes jComboBoxFPSSrc
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxFPSSrc() {
		if (jComboBoxFPSSrc == null) {
			jComboBoxFPSSrc = new JComboBox();
			jComboBoxFPSSrc.setPreferredSize(new Dimension(200, 20));
			jComboBoxFPSSrc.setMinimumSize(new Dimension(150, 20));
			jComboBoxFPSSrc.setEditable(true);
			jComboBoxFPSSrc.setEnabled(false);
			jComboBoxFPSSrc.setToolTipText("Set the source frame rate (only needed for frame rate conversion)");
			jComboBoxFPSSrc.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						String s = (String)jComboBoxFPSSrc.getSelectedItem();
						double d = Core.getFPS(s);
						if (d > 0)
							fpsSrc = d;
						jComboBoxFPSSrc.setSelectedItem(ToolBox.formatDouble(fpsSrc));
						jComboBoxFPSSrc.getEditor().getEditorComponent().setBackground(okBgnd);
						fpsSrcCertain = false;
					}
				}
			});
			fpsSrcEditor.getDocument().addDocumentListener(new DocumentListener() {
				private void check(DocumentEvent e) {
					if (isReady) {
						String s = fpsSrcEditor.getText();
						double d = Core.getFPS(s);
						Color c;
						if (d>0) {
							c = okBgnd;
							fpsSrc = d;
						} else
							c = errBgnd;
						fpsSrcEditor.setBackground(c);
						fpsSrcCertain = false;
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
		return jComboBoxFPSSrc;
	}

	/**
	 * This method initializes jComboBoxFPSTrg
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxFPSTrg() {
		if (jComboBoxFPSTrg == null) {
			jComboBoxFPSTrg = new JComboBox();
			jComboBoxFPSTrg.setPreferredSize(new Dimension(200, 20));
			jComboBoxFPSTrg.setMinimumSize(new Dimension(150, 20));
			jComboBoxFPSTrg.setEditable(true);
			jComboBoxFPSTrg.setEnabled(false);
			jComboBoxFPSTrg.setToolTipText("Set the target frame rate");
			jComboBoxFPSTrg.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						String s = (String)jComboBoxFPSTrg.getSelectedItem();
						double d = Core.getFPS(s);
						if (d > 0)
							fpsTrg = d;
						jComboBoxFPSTrg.setSelectedItem(ToolBox.formatDouble(fpsTrg));
						jComboBoxFPSTrg.getEditor().getEditorComponent().setBackground(okBgnd);
						//
						delayPTS = (int)Core.syncTimePTS(delayPTS,fpsTrg);
						jTextFieldDelay.setText(ToolBox.formatDouble(delayPTS/90.0));
						//
						minTimePTS = (int)Core.syncTimePTS(minTimePTS,fpsTrg);
						d = minTimePTS;
						jTextFieldMinTime.setText(ToolBox.formatDouble(minTimePTS/90.0));
					}
				}
			});
			fpsTrgEditor.getDocument().addDocumentListener(new DocumentListener() {
				private void check(DocumentEvent e) {
					if (isReady) {
						String s = fpsTrgEditor.getText();
						double d = Core.getFPS(s);
						Color c;
						if (d>0) {
							if ((int)Core.syncTimePTS(delayPTS,fpsTrg) != delayPTS || minTimePTS != (int)Core.syncTimePTS(minTimePTS,fpsTrg))
								c = warnBgnd;
							else
								c = okBgnd;
							fpsTrg = d;
						} else
							c = errBgnd;
						fpsTrgEditor.setBackground(c);
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
		return jComboBoxFPSTrg;
	}

	/**
	 * This method initializes jTextFieldDelay
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldDelay() {
		if (jTextFieldDelay == null) {
			jTextFieldDelay = new JTextField();
			jTextFieldDelay.setPreferredSize(new Dimension(200, 20));
			jTextFieldDelay.setToolTipText("Set global delay (in milliseconds) added to all timestamps");
			jTextFieldDelay.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						String s = jTextFieldDelay.getText();
						try {
							// don't use getDouble as the value can be negative
							delayPTS = (int)Core.syncTimePTS((long)(Double.parseDouble(s)*90),fpsTrg);
						} catch (NumberFormatException ex) {}
						jTextFieldDelay.setBackground(okBgnd);
						jTextFieldDelay.setText(ToolBox.formatDouble(delayPTS/90.0));
					}
				}
			});
			jTextFieldDelay.getDocument().addDocumentListener(new DocumentListener() {
				private void check(DocumentEvent e) {
					if (isReady) {
						String s = jTextFieldDelay.getText();
						try {
							// don't use getDouble as the value can be negative
							delayPTS = (int)Core.syncTimePTS((long)(Double.parseDouble(s)*90),fpsTrg);
							if (!s.equalsIgnoreCase(ToolBox.formatDouble(delayPTS/90.0)))
								jTextFieldDelay.setBackground(warnBgnd);
							else
								jTextFieldDelay.setBackground(okBgnd);
						} catch (NumberFormatException ex) {
							jTextFieldDelay.setBackground(errBgnd);
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
		return jTextFieldDelay;
	}



	/**
	 * This method initializes jButtonCancel
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton();
			jButtonCancel.setText("Cancel");
			jButtonCancel.setToolTipText("Lose all changes and use the default values");
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
	 * This method initializes jButtonStore
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonStore() {
		if (jButtonStore == null) {
			jButtonStore = new JButton();
			jButtonStore.setText("Store");
			jButtonStore.setToolTipText("Store current settings as default");
			jButtonStore.setMnemonic('o');
			jButtonStore.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						// read values of editable boxes
						String s;
						double d;
						// fps source
						Core.storeConvertFPS(changeFPS);
						if (changeFPS) {
							s = (String)jComboBoxFPSSrc.getSelectedItem();
							d = Core.getFPS(s);
							if (d > 0) {
								fpsSrc = d;
								Core.storeFPSSrc(fpsSrc);
							}
						}
						// fps target
						s = (String)jComboBoxFPSTrg.getSelectedItem();
						d = Core.getFPS(s);
						if (d > 0) {
							fpsTrg = d;
							Core.storeFPSTrg(fpsTrg);
						}
						// delay
						s = jTextFieldDelay.getText();
						try {
							delayPTS = (int)Core.syncTimePTS((long)(Double.parseDouble(s)*90),fpsTrg);
							Core.storeDelayPTS(delayPTS);
						} catch (NumberFormatException ex) {}
						// min time
						Core.storeFixShortFrames(fixShortFrames);
						s = jTextFieldMinTime.getText();
						try {
							minTimePTS = (int)Core.syncTimePTS((long)(Double.parseDouble(s)*90),fpsTrg);
							Core.storeMinTimePTS(minTimePTS);
						} catch (NumberFormatException ex) {}
						// exit
						Core.storeConvertResolution(changeResolution);
						if (changeResolution)
							Core.storeOutputResolution(resolution);
						// scaleX
						s = jTextFieldScaleX.getText();
						d = ToolBox.getDouble(s);
						if (d >0) {
							if (d > Core.maxScale)
								d = Core.maxScale;
							else if (d < Core.minScale)
								d = Core.minScale;
							scaleX = d;
						};
						// scaleY
						s = jTextFieldScaleY.getText();
						d = ToolBox.getDouble(s);
						if (d >0) {
							if (d > Core.maxScale)
								d = Core.maxScale;
							else if (d < Core.minScale)
								d = Core.minScale;
							scaleY = d;
						};
						// set scale X/Y
						Core.storeApplyFreeScale(changeScale);
						if (changeScale)
							Core.storeFreeScale(scaleX, scaleY);
						// forceAll is not stored
						//
						Core.storeProps();
					}
				}
			});
		}
		return jButtonStore;
	}

	/**
	 * This method initializes jButtonRestore
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonRestore() {
		if (jButtonRestore == null) {
			jButtonRestore = new JButton();
			jButtonRestore.setText("Restore");
			jButtonRestore.setToolTipText("Restore last default settings");
			jButtonRestore.setMnemonic('e');
			jButtonRestore.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					changeResolution = Core.restoreConvertResolution();
					if (changeResolution)
						resolution = Core.restoreResolution();
					changeFPS = Core.restoreConvertFPS();
					if (changeFPS && !fpsSrcCertain)
						fpsSrc = Core.restoreFpsSrc();
					fpsTrg = Core.restoreFpsTrg();
					delayPTS = Core.restoreDelayPTS();
					fixShortFrames = Core.restoreFixShortFrames();
					minTimePTS = Core.restoreMinTimePTS();
					changeScale = Core.restoreApplyFreeScale();
					if (changeScale) {
						scaleX = Core.restoreFreeScaleX();
						scaleY = Core.restoreFreeScaleY();
					}
					forcedState = Core.getForceAll();
					fillDialog();
				}
			});
		}
		return jButtonRestore;
	}


	/**
	 * This method initializes jButtonReset
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonReset() {
		if (jButtonReset == null) {
			jButtonReset = new JButton();
			jButtonReset.setText("Reset");
			jButtonReset.setToolTipText("Reset defaults");
			jButtonReset.setMnemonic('t');
			jButtonReset.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					changeResolution = Core.getConvertResolutionDefault();
					if (changeResolution)
						resolution = Core.getResolutionDefault();
					changeFPS = Core.getConvertFPSdefault();
					if (changeFPS) {
						if (!fpsSrcCertain)
							fpsSrc = Core.getFpsSrcDefault();
						fpsTrg = Core.getFpsTrgDefault();
					} else
						fpsTrg = fpsSrc;
					delayPTS = Core.getDelayPTSdefault();
					fixShortFrames = Core.getFixShortFramesDefault();
					minTimePTS = Core.getMinTimePTSdefault();
					changeScale = Core.getApplyFreeScaleDefault();
					if (changeScale) {
						scaleX = Core.getFreeScaleXdefault();
						scaleY = Core.getFreeScaleYdefault();
					}
					forcedState = Core.SetState.KEEP;
					fillDialog();
				}
			});
		}
		return jButtonReset;
	}


	/**
	 * Get cancel state
	 * @return true if canceled
	 */
	public boolean wasCanceled() {
		return cancel;
	}

	/**
	 * This method initializes jCheckBoxScale
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxScale() {
		if (jCheckBoxScale == null) {
			jCheckBoxScale = new JCheckBox();
			jCheckBoxScale.setToolTipText("Allow free scaling of subtitles in X and Y direction");
			jCheckBoxScale.setText("Apply free scaling");
			jCheckBoxScale.setMnemonic('a');
			jCheckBoxScale.setFocusable(false);
			jCheckBoxScale.setIconTextGap(10);
			jCheckBoxScale.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (isReady) {
						changeScale = jCheckBoxScale.isSelected();
						jTextFieldScaleX.setEnabled(changeScale);
						jTextFieldScaleY.setEnabled(changeScale);
					}
				}
			});
		}
		return jCheckBoxScale;
	}


	/**
	 * This method initializes jCheckBoxFixMinTime
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxFixMineTime() {
		if (jCheckBoxFixMinTime == null) {
			jCheckBoxFixMinTime = new JCheckBox();
			jCheckBoxFixMinTime.setToolTipText("Force a minimum display duration of 'Min Time'");
			jCheckBoxFixMinTime.setText("Fix too short frames");
			jCheckBoxFixMinTime.setMnemonic('s');
			jCheckBoxFixMinTime.setFocusable(false);
			jCheckBoxFixMinTime.setIconTextGap(10);
			jCheckBoxFixMinTime.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (isReady) {
						fixShortFrames = jCheckBoxFixMinTime.isSelected();
						jTextFieldMinTime.setEnabled(fixShortFrames);
					}
				}
			});
		}
		return jCheckBoxFixMinTime;
	}

	/**
	 * This method initializes jComboBoxMinTime
	 * @return javax.swing.JComboBox
	 */
	private JTextField getJTextFieldMinTime() {
		if (jTextFieldMinTime == null) {
			jTextFieldMinTime = new JTextField();
			jTextFieldMinTime.setEditable(true);
			jTextFieldMinTime.setPreferredSize(new Dimension(200, 20));
			jTextFieldMinTime.setEnabled(false);
			jTextFieldMinTime.setToolTipText("Set minimum display time for a subtitle");
			jTextFieldMinTime.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						String s = jTextFieldMinTime.getText();
						try {
							minTimePTS = (int)Core.syncTimePTS((long)(Double.parseDouble(s)*90),fpsTrg);
						} catch (NumberFormatException ex) {}
						jTextFieldMinTime.setBackground(okBgnd);
						jTextFieldMinTime.setText(ToolBox.formatDouble(minTimePTS/90.0));
					}
				}
			});
			jTextFieldMinTime.getDocument().addDocumentListener(new DocumentListener() {
				private void check(DocumentEvent e) {
					if (isReady) {
						String s = jTextFieldMinTime.getText();
						try {
							minTimePTS = (int)Core.syncTimePTS((long)(Double.parseDouble(s)*90),fpsTrg);
							if (!s.equalsIgnoreCase(ToolBox.formatDouble(minTimePTS/90.0)))
								jTextFieldMinTime.setBackground(warnBgnd);
							else
								jTextFieldMinTime.setBackground(okBgnd);
						} catch (NumberFormatException ex) {
							jTextFieldMinTime.setBackground(errBgnd);
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
		return jTextFieldMinTime;
	}

	/**
	 * This method initializes jButtonOk
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonOk() {
		if (jButtonOk == null) {
			jButtonOk = new JButton();
			jButtonOk.setText("  Ok  ");
			jButtonOk.setMnemonic('o');
			jButtonOk.setToolTipText("Use current values and continue");
			jButtonOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						// read values of editable boxes
						String s;
						double d;
						// fps source
						Core.setConvertFPS(changeFPS);
						if (changeFPS) {
							s = (String)jComboBoxFPSSrc.getSelectedItem();
							d = Core.getFPS(s);
							if (d > 0) {
								fpsSrc = d;
								Core.setFPSSrc(fpsSrc);
							}
						}
						// fps target
						s = (String)jComboBoxFPSTrg.getSelectedItem();
						d = Core.getFPS(s);
						if (d > 0) {
							fpsTrg = d;
							Core.setFPSTrg(fpsTrg);
						}
						// delay
						s = jTextFieldDelay.getText();
						try {
							delayPTS = (int)Core.syncTimePTS((long)(Double.parseDouble(s)*90),fpsTrg);
							Core.setDelayPTS(delayPTS);
						} catch (NumberFormatException ex) {}
						// min time
						Core.setFixShortFrames(fixShortFrames);
						s = jTextFieldMinTime.getText();
						try {
							minTimePTS = (int)Core.syncTimePTS((long)(Double.parseDouble(s)*90),fpsTrg);
							Core.setMinTimePTS(minTimePTS);
						} catch (NumberFormatException ex) {}
						// exit
						Core.setConvertResolution(changeResolution);
						if (changeResolution)
							Core.setOutputResolution(resolution);
						// scaleX
						s = jTextFieldScaleX.getText();
						d = ToolBox.getDouble(s);
						if (d >0) {
							if (d > Core.maxScale)
								d = Core.maxScale;
							else if (d < Core.minScale)
								d = Core.minScale;
							scaleX = d;
						};
						// scaleY
						s = jTextFieldScaleY.getText();
						d = ToolBox.getDouble(s);
						if (d >0) {
							if (d > Core.maxScale)
								d = Core.maxScale;
							else if (d < Core.minScale)
								d = Core.minScale;
							scaleY = d;
						};
						// set scale X/Y
						Core.setApplyFreeScale(changeScale);
						if (changeScale)
							Core.setFreeScale(scaleX, scaleY);
						cancel = false;
						// forced state
						Core.setForceAll(forcedState);
						// keep move settings
						if (jCheckBoxMove.isEnabled()) {
							Core.setMoveCaptions(moveCaptions);
						}
						//
						dispose();
					}
				}
			});
		}
		return jButtonOk;
	}

	/**
	 * This method initializes jTextFieldScaleX
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldScaleX() {
		if (jTextFieldScaleX == null) {
			jTextFieldScaleX = new JTextField();
			jTextFieldScaleX.setPreferredSize(new Dimension(200, 20));
			jTextFieldScaleX.setToolTipText("Set free scaling factor in X direction");
			jTextFieldScaleX.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						String s = jTextFieldScaleX.getText();
						double d = ToolBox.getDouble(s);
						if (d >0) {
							if (d > Core.maxScale)
								d = Core.maxScale;
							else if (d < Core.minScale)
								d = Core.minScale;
							scaleX = d;
						};
						jTextFieldScaleX.setText(ToolBox.formatDouble(scaleX));
						jTextFieldScaleX.setBackground(okBgnd);
					}
				}
			});
			jTextFieldScaleX.getDocument().addDocumentListener(new DocumentListener() {
				private void check(DocumentEvent e) {
					if (isReady) {
						String s = jTextFieldScaleX.getText();
						double d = ToolBox.getDouble(s);
						if (d >= Core.minScale && d <= Core.maxScale) {
							scaleX = d;
							jTextFieldScaleX.setBackground(okBgnd);
						} else
							jTextFieldScaleX.setBackground(errBgnd);
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
		return jTextFieldScaleX;
	}

	/**
	 * This method initializes jTextFieldScaleY
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldScaleY() {
		if (jTextFieldScaleY == null) {
			jTextFieldScaleY = new JTextField();
			jTextFieldScaleY.setPreferredSize(new Dimension(200, 20));
			jTextFieldScaleY.setToolTipText("Set free scaling factor in Y direction");
			jTextFieldScaleY.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						String s = jTextFieldScaleY.getText();
						double d = ToolBox.getDouble(s);
						if (d >0) {
							if (d > Core.maxScale)
								d = Core.maxScale;
							else if (d < Core.minScale)
								d = Core.minScale;
							scaleY = d;
						};
						jTextFieldScaleY.setText(ToolBox.formatDouble(scaleY));
					}
				}
			});
			jTextFieldScaleY.getDocument().addDocumentListener(new DocumentListener() {
				private void check(DocumentEvent e) {
					if (isReady) {
						String s = jTextFieldScaleY.getText();
						double d = ToolBox.getDouble(s);
						if (d >= Core.minScale && d <= Core.maxScale) {
							scaleY = d;
							jTextFieldScaleY.setBackground(okBgnd);
						} else
							jTextFieldScaleY.setBackground(errBgnd);
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
		return jTextFieldScaleY;
	}

	/**
	 * This method initializes jComboBoxForced
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxForced() {
		if (jComboBoxForced == null) {
			jComboBoxForced = new JComboBox();
			jComboBoxForced.setPreferredSize(new Dimension(200, 20));
			jComboBoxForced.setMinimumSize(new Dimension(150, 20));
			jComboBoxForced.setEditable(false);
			jComboBoxForced.setToolTipText("Select the target resolution");
			jComboBoxForced.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (isReady) {
						int idx = jComboBoxForced.getSelectedIndex();
						for (Core.SetState s : Core.SetState.values()) {
							if (idx == s.ordinal()) {
								forcedState = s;
								break;
							}
						}
					}
				}
			});
		}
		return jComboBoxForced;
	}

	/**
	 * Enable the "Keep move settings" checkbox (default: disabled)
	 * @param e true: enable, false: disable
	 */
	public void enableOptionMove(boolean e) {
		jCheckBoxMove.setEnabled(e);
	}

}
