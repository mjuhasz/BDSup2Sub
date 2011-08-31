package deadbeef.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import deadbeef.SupTools.Core;
import deadbeef.SupTools.CoreException;
import deadbeef.SupTools.SubPicture;
import deadbeef.Tools.ToolBox;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;

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
 * Move dialog - part of BDSup2Sub GUI classes.
 *
 * @author 0xdeadbeef
 */
public class MoveDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JPanel jPanelUp = null;

	private JPanel jPanelLayout = null;

	private JPanel jPanelOffsets = null;

	private JPanel jPanelMove = null;

	private JPanel jPanelButtons = null;

	private JLabel jLabelInfo = null;

	private JButton jButtonPrev = null;

	private JButton jButtonNext = null;

	private EditPane jPanelPreview = null;

	private JLabel jLabelRatio = null;

	private JLabel jLabelOffsetY = null;

	private JButton jButtonCancel = null;

	private JButton jButtonOk = null;

	private JTextField jTextFieldRatio = null;

	private JTextField jTextFieldOffsetY = null;

	private JButton jButton21_9 = null;

	private JLabel jLabelRatio1 = null;

	private JButton jButton240_1 = null;

	private JButton jButton235_1 = null;

	private JRadioButton jRadioButtonKeepY = null;

	private JRadioButton jRadioButtonInside = null;

	private JRadioButton jRadioButtonOutside = null;

	private ButtonGroup radioButtonsY = null;

	private ButtonGroup radioButtonsX = null;

	private JPanel jPanelCrop = null;

	private JLabel jLabelCropOfsY = null;

	private JTextField jTextFieldCropOfsY = null;

	private JButton jButtonCropBars = null;

	private JRadioButton jRadioButtonKeepX = null;

	private JRadioButton jRadioButtonLeft = null;

	private JRadioButton jRadioButtonRight = null;

	private JLabel jLabelOffsetX = null;

	private JTextField jTextFieldOffsetX = null;

	private JRadioButton jRadioButtonCenter = null;

	// user declared variables

	/** width of preview pane */
	private final static int miniWidth = 384;
	/** height of preview pane */
	private final static int miniHeight = 216;

	/** target screen ratio */
	private static double screenRatioTrg = 21.0/9;
	/** aspect ratio of the screen */
	private static final double screenRatio = 16.0/9;

	/** background color for errors */
	private final Color errBgnd = new Color(0xffe1acac);
	/** background color for ok */
	private final Color okBgnd = UIManager.getColor("TextField.background");

	/** image of subpicture to display in preview pane */
	private BufferedImage image = null;

	/** move mode in Y direction */
	private Core.MoveModeY moveModeY = Core.MoveModeY.KEEP;
	/** move mode in X direction */
	private Core.MoveModeX moveModeX = Core.MoveModeX.KEEP;

	/** original X position */
	private int originalX;
	/** original Y position */
	private int originalY;

	/** current subtitle index */
	private int index;
	/** current subpicture */
	private SubPicture subPic;
	/** additional y offset to consider when moving */
	private int offsetY;
	/** additional x offset to consider when moving */
	private int offsetX;
	/** factor to calculate height of one cinemascope bar from screen height */
	private double cineBarFactor = 5.0/42;
	/** Y coordinate crop offset */
	private int cropOfsY = Core.getCropOfsY();
	/** semaphore to disable actions while changing component properties */
	private volatile boolean isReady = false;

	private final static Dimension dimLabel = new Dimension(70,14);
	private final static Dimension dimText  = new Dimension(40,20);


	/**
	 * Constructor
	 * @param owner parent frame
	 * @param modal modal dialog
	 */
	public MoveDialog(Frame owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
		initialize();
		// center dialog
		Point p = owner.getLocation();
		this.setLocation(p.x+owner.getWidth()/2-getWidth()/2, p.y+owner.getHeight()/2-getHeight()/2);
		this.setResizable(false);
		//
		offsetX = Core.getMoveOffsetX();
		offsetY = Core.getMoveOffsetY();
		moveModeX = Core.getMoveModeX();
		moveModeY = Core.getMoveModeY();
		switch (moveModeY) {
			case KEEP:
				jRadioButtonKeepY.setSelected(true);
				break;
			case INSIDE:
				jRadioButtonInside.setSelected(true);
				break;
			case OUTSIDE:
				jRadioButtonOutside.setSelected(true);
				break;
		}
		switch (moveModeX) {
			case KEEP:
				jRadioButtonKeepX.setSelected(true);
				break;
			case LEFT:
				jRadioButtonLeft.setSelected(true);
				break;
			case RIGHT:
				jRadioButtonRight.setSelected(true);
				break;
			case CENTER:
				jRadioButtonCenter.setSelected(true);
				break;
		}
		//
		radioButtonsY = new ButtonGroup();
		radioButtonsY.add( jRadioButtonKeepY );
		radioButtonsY.add( jRadioButtonInside );
		radioButtonsY.add( jRadioButtonOutside );
		radioButtonsX = new ButtonGroup();
		radioButtonsX.add( jRadioButtonKeepX);
		radioButtonsX.add( jRadioButtonLeft);
		radioButtonsX.add( jRadioButtonRight);
		radioButtonsX.add( jRadioButtonCenter);
		//
		jTextFieldRatio.setText(ToolBox.formatDouble(screenRatioTrg));
		jTextFieldOffsetY.setText(""+offsetY);
		jTextFieldOffsetX.setText(""+offsetX);
		//
		jTextFieldCropOfsY.setText(ToolBox.formatDouble(screenRatioTrg));
		jTextFieldCropOfsY.setText(""+cropOfsY);
		//
	}

	/**
	 * This method initializes this dialog
	 */
	private void initialize() {
		this.setSize(392, 546);
		this.setTitle("Move all captions");
		this.setContentPane(getJContentPane());
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
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
			GridBagConstraints gridBagPanelCrop = new GridBagConstraints();
			gridBagPanelCrop.gridx = 0;
			gridBagPanelCrop.weightx = 1.0;
			gridBagPanelCrop.weighty = 1.0;
			gridBagPanelCrop.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelCrop.fill = GridBagConstraints.HORIZONTAL;
			gridBagPanelCrop.gridy = 4;
			GridBagConstraints gridBagPanelButtons = new GridBagConstraints();
			gridBagPanelButtons.gridx = 0;
			gridBagPanelButtons.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelButtons.weightx = 1.0;
			gridBagPanelButtons.weighty = 0.0;
			gridBagPanelButtons.fill = GridBagConstraints.HORIZONTAL;
			gridBagPanelButtons.gridy = 5;
			GridBagConstraints gridBagPanelRadio = new GridBagConstraints();
			gridBagPanelRadio.gridx = 0;
			gridBagPanelRadio.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelRadio.weightx = 1.0;
			gridBagPanelRadio.weighty = 1.0;
			gridBagPanelRadio.fill = GridBagConstraints.HORIZONTAL;
			gridBagPanelRadio.gridy = 2;
			GridBagConstraints gridBagPanelOffsets = new GridBagConstraints();
			gridBagPanelOffsets.gridx = 0;
			gridBagPanelOffsets.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelOffsets.weightx = 1.0;
			gridBagPanelOffsets.weighty = 1.0;
			gridBagPanelOffsets.fill = GridBagConstraints.HORIZONTAL;
			gridBagPanelOffsets.gridy = 3;
			GridBagConstraints gridBagPanelLayout = new GridBagConstraints();
			gridBagPanelLayout.gridx = 0;
			gridBagPanelLayout.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelLayout.weightx = 1.0;
			gridBagPanelLayout.weighty = 1.0;
			gridBagPanelLayout.fill = GridBagConstraints.HORIZONTAL;
			gridBagPanelLayout.gridy = 1;
			GridBagConstraints gridBagPanelUp = new GridBagConstraints();
			gridBagPanelUp.gridx = 0;
			gridBagPanelUp.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelUp.weightx = 1.0D;
			gridBagPanelUp.weighty = 1.0;
			gridBagPanelUp.fill = GridBagConstraints.HORIZONTAL;
			gridBagPanelUp.gridy = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJPanelUp(), gridBagPanelUp);
			jContentPane.add(getJPanelLayout(), gridBagPanelLayout);
			jContentPane.add(getJPanelOffsets(), gridBagPanelOffsets);
			jContentPane.add(getJPanelMove(), gridBagPanelRadio);
			jContentPane.add(getJPanelButtons(), gridBagPanelButtons);
			jContentPane.add(getJPanelCrop(), gridBagPanelCrop);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanelUp
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelUp() {
		if (jPanelUp == null) {
			GridBagConstraints gridBagButtonNext = new GridBagConstraints();
			gridBagButtonNext.gridx = 2;
			gridBagButtonNext.anchor = GridBagConstraints.NORTHWEST;
			gridBagButtonNext.insets = new Insets(2, 4, 2, 9);
			gridBagButtonNext.gridy = 0;
			GridBagConstraints gridBagButtonPrev = new GridBagConstraints();
			gridBagButtonPrev.gridx = 1;
			gridBagButtonPrev.anchor = GridBagConstraints.NORTHWEST;
			gridBagButtonPrev.insets = new Insets(2, 4, 2, 4);
			gridBagButtonPrev.gridy = 0;
			GridBagConstraints gridBagInfo = new GridBagConstraints();
			gridBagInfo.weightx = 1.0;
			gridBagInfo.anchor = GridBagConstraints.WEST;
			gridBagInfo.insets = new Insets(4, 6, 0, 4);
			gridBagInfo.weighty = 1.0;
			jLabelInfo = new JLabel();
			jLabelInfo.setText("Info");
			jPanelUp = new JPanel();
			jPanelUp.setPreferredSize(new Dimension(200, 20));
			jPanelUp.setLayout(new GridBagLayout());
			jPanelUp.add(jLabelInfo, gridBagInfo);
			jPanelUp.add(getJButtonPrev(), gridBagButtonPrev);
			jPanelUp.add(getJButtonNext(), gridBagButtonNext);
		}
		return jPanelUp;
	}

	/**
	 * This method initializes jPanelLayout
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelLayout() {
		if (jPanelLayout == null) {
			GridBagConstraints gridBagPanelPreview = new GridBagConstraints();
			gridBagPanelPreview.gridx = 0;
			gridBagPanelPreview.anchor = GridBagConstraints.NORTHWEST;
			gridBagPanelPreview.weighty = 0.0;
			gridBagPanelPreview.gridy = 0;
			gridBagPanelPreview.insets = new Insets(0, 4, 0, 0);
			jPanelLayout = new JPanel();
			jPanelLayout.setLayout(new GridBagLayout());
			jPanelLayout.add(getJPanelPreview(), gridBagPanelPreview);
		}
		return jPanelLayout;
	}

	/**
	 * This method initializes jPanelOffsets
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelOffsets() {
		if (jPanelOffsets == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 4;
			GridBagConstraints gridBagBtn235_1 = new GridBagConstraints();
			gridBagBtn235_1.gridx = 5;
			gridBagBtn235_1.insets = new Insets(0, 0, 0, 6);
			gridBagBtn235_1.anchor = GridBagConstraints.EAST;
			gridBagBtn235_1.gridy = 1;
			GridBagConstraints gridBagBtn240_1 = new GridBagConstraints();
			gridBagBtn240_1.gridx = 6;
			gridBagBtn240_1.insets = new Insets(0, 0, 0, 0);
			gridBagBtn240_1.anchor = GridBagConstraints.EAST;
			gridBagBtn240_1.gridy = 1;
			GridBagConstraints gridBagLabelRatio1 = new GridBagConstraints();
			gridBagLabelRatio1.gridx = 2;
			gridBagLabelRatio1.weightx = 20.0;
			gridBagLabelRatio1.anchor = GridBagConstraints.WEST;
			gridBagLabelRatio1.insets = new Insets(0, 6, 0, 0);
			gridBagLabelRatio1.gridy = 1;
			jLabelRatio1 = new JLabel();
			jLabelRatio1.setText(" : 1");
			jLabelRatio1.setHorizontalAlignment(SwingConstants.LEFT);
			jLabelRatio1.setHorizontalTextPosition(SwingConstants.LEFT);
			GridBagConstraints gridBagButtonBtn21_9 = new GridBagConstraints();
			gridBagButtonBtn21_9.gridx = 4;
			gridBagButtonBtn21_9.anchor = GridBagConstraints.EAST;
			gridBagButtonBtn21_9.insets = new Insets(0, 0, 0, 6);
			gridBagButtonBtn21_9.weightx = 0.0;
			gridBagButtonBtn21_9.gridy = 1;
			GridBagConstraints gridBagTextRatio = new GridBagConstraints();
			gridBagTextRatio.fill = GridBagConstraints.NONE;
			gridBagTextRatio.gridy = 1;
			gridBagTextRatio.weightx = 2.0;
			gridBagTextRatio.anchor = GridBagConstraints.WEST;
			gridBagTextRatio.insets = new Insets(0, 0, 0, 0);
			gridBagTextRatio.weighty = 0.0;
			gridBagTextRatio.gridx = 1;
			jLabelOffsetY = new JLabel();
			jLabelOffsetY.setText("Offset Y");
			jLabelOffsetY.setPreferredSize(dimLabel);
			jLabelOffsetY.setSize(dimLabel);
			jLabelOffsetY.setMinimumSize(dimLabel);
			jLabelOffsetY.setMaximumSize(dimLabel);
			GridBagConstraints gridBagLabelRatio = new GridBagConstraints();
			gridBagLabelRatio.gridx = 0;
			gridBagLabelRatio.anchor = GridBagConstraints.WEST;
			gridBagLabelRatio.weightx = 0.0;
			gridBagLabelRatio.weighty = 0.0;
			gridBagLabelRatio.insets = new Insets(0, 6, 0, 4);
			gridBagLabelRatio.gridy = 1;
			jLabelRatio = new JLabel();
			jLabelRatio.setText("Aspect ratio");
			jLabelRatio.setPreferredSize(dimLabel);
			jLabelRatio.setSize(dimLabel);
			jLabelRatio.setMinimumSize(dimLabel);
			jLabelRatio.setMaximumSize(dimLabel);
			jPanelOffsets = new JPanel();
			jPanelOffsets.setLayout(new GridBagLayout());
			jPanelOffsets.setBorder(BorderFactory.createTitledBorder(null, "Screen Ratio", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanelOffsets.add(jLabelRatio, gridBagLabelRatio);
			jPanelOffsets.add(getJTextFieldRatio(), gridBagTextRatio);
			jPanelOffsets.add(getJButton21_9(), gridBagButtonBtn21_9);
			jPanelOffsets.add(jLabelRatio1, gridBagLabelRatio1);
			jPanelOffsets.add(getJButton240_1(), gridBagBtn240_1);
			jPanelOffsets.add(getJButton235_1(), gridBagBtn235_1);
		}
		return jPanelOffsets;
	}

	/**
	 * This method initializes jPanelTimes
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelMove() {
		if (jPanelMove == null) {
			GridBagConstraints gridBagRadioCenter = new GridBagConstraints();
			gridBagRadioCenter.gridx = 0;
			gridBagRadioCenter.gridwidth = 2;
			gridBagRadioCenter.anchor = GridBagConstraints.WEST;
			gridBagRadioCenter.insets = new Insets(0, 4, 0, 0);
			gridBagRadioCenter.weightx = 1.0;
			gridBagRadioCenter.gridy = 3;
			GridBagConstraints gridBagTextOfsX = new GridBagConstraints();
			gridBagTextOfsX.gridy = 4;
			gridBagTextOfsX.weightx = 10.0;
			gridBagTextOfsX.insets = new Insets(0, 0, 0, 0);
			gridBagTextOfsX.anchor = GridBagConstraints.WEST;
			gridBagTextOfsX.gridx = 1;
			GridBagConstraints gridBagLabelOfsX = new GridBagConstraints();
			gridBagLabelOfsX.gridx = 0;
			gridBagLabelOfsX.insets = new Insets(0, 6, 0, 4);
			gridBagLabelOfsX.anchor = GridBagConstraints.WEST;
			gridBagLabelOfsX.weightx = 0.0;
			gridBagLabelOfsX.gridy = 4;
			jLabelOffsetX = new JLabel();
			jLabelOffsetX.setText("Offset X");
			jLabelOffsetX.setPreferredSize(dimLabel);
			jLabelOffsetX.setSize(dimLabel);
			jLabelOffsetX.setMinimumSize(dimLabel);
			jLabelOffsetX.setMaximumSize(dimLabel);
			GridBagConstraints gridBagTextOfsY = new GridBagConstraints();
			gridBagTextOfsY.anchor = GridBagConstraints.WEST;
			gridBagTextOfsY.insets = new Insets(0, 0, 0, 0);
			gridBagTextOfsY.gridwidth = 1;
			gridBagTextOfsY.gridx = 3;
			gridBagTextOfsY.gridy = 4;
			gridBagTextOfsY.weightx = 10.0;
			GridBagConstraints gridBagLabelOfsY = new GridBagConstraints();
			gridBagLabelOfsY.anchor = GridBagConstraints.WEST;
			gridBagLabelOfsY.gridx = 2;
			gridBagLabelOfsY.gridy = 4;
			gridBagLabelOfsY.weightx = 0.0;
			gridBagLabelOfsY.insets = new Insets(0, 6, 0, 4);
			GridBagConstraints gridBagRadioRight = new GridBagConstraints();
			gridBagRadioRight.gridx = 0;
			gridBagRadioRight.insets = new Insets(0, 4, 0, 0);
			gridBagRadioRight.anchor = GridBagConstraints.WEST;
			gridBagRadioRight.gridwidth = 2;
			gridBagRadioRight.weightx = 1.0;
			gridBagRadioRight.gridy = 2;
			GridBagConstraints gridBagRadioLeft = new GridBagConstraints();
			gridBagRadioLeft.gridx = 0;
			gridBagRadioLeft.anchor = GridBagConstraints.WEST;
			gridBagRadioLeft.insets = new Insets(0, 4, 0, 0);
			gridBagRadioLeft.gridwidth = 2;
			gridBagRadioLeft.weightx = 1.0;
			gridBagRadioLeft.gridy = 1;
			GridBagConstraints gridBagRadioKeepX = new GridBagConstraints();
			gridBagRadioKeepX.gridx = 0;
			gridBagRadioKeepX.insets = new Insets(0, 4, 0, 0);
			gridBagRadioKeepX.anchor = GridBagConstraints.WEST;
			gridBagRadioKeepX.gridwidth = 2;
			gridBagRadioKeepX.weightx = 1.0;
			gridBagRadioKeepX.gridy = 0;
			GridBagConstraints gridBagRadioKeepY = new GridBagConstraints();
			gridBagRadioKeepY.gridx = 2;
			gridBagRadioKeepY.anchor = GridBagConstraints.WEST;
			gridBagRadioKeepY.insets = new Insets(0, 4, 0, 0);
			gridBagRadioKeepY.gridwidth = 2;
			gridBagRadioKeepY.weightx = 1.0;
			gridBagRadioKeepY.gridy = 0;
			GridBagConstraints gridBagRadioOutside = new GridBagConstraints();
			gridBagRadioOutside.gridx = 2;
			gridBagRadioOutside.weightx = 1.0;
			gridBagRadioOutside.anchor = GridBagConstraints.WEST;
			gridBagRadioOutside.insets = new Insets(0, 4, 0, 0);
			gridBagRadioOutside.gridwidth = 2;
			gridBagRadioOutside.gridy = 2;
			GridBagConstraints gridBagRadioInside = new GridBagConstraints();
			gridBagRadioInside.anchor = GridBagConstraints.WEST;
			gridBagRadioInside.insets = new Insets(0, 4, 0, 0);
			gridBagRadioInside.gridy = 1;
			gridBagRadioInside.gridx = 2;
			gridBagRadioInside.gridwidth = 2;
			gridBagRadioInside.weightx = 1.0;
			jPanelMove = new JPanel();
			jPanelMove.setLayout(new GridBagLayout());
			jPanelMove.setBorder(BorderFactory.createTitledBorder(null, "Move", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanelMove.add(getJRadioButtonInside(), gridBagRadioInside);
			jPanelMove.add(getJRadioButtonOutside(), gridBagRadioOutside);
			jPanelMove.add(getJRadioButtonKeepY(), gridBagRadioKeepY);
			jPanelMove.add(getJRadioButtonKeepX(), gridBagRadioKeepX);
			jPanelMove.add(getJRadioButtonLeft(), gridBagRadioLeft);
			jPanelMove.add(getJRadioButtonRight(), gridBagRadioRight);
			jPanelMove.add(jLabelOffsetY, gridBagLabelOfsY);
			jPanelMove.add(getJTextFieldOffsetY(), gridBagTextOfsY);
			jPanelMove.add(jLabelOffsetX, gridBagLabelOfsX);
			jPanelMove.add(getJTextFieldOffsetX(), gridBagTextOfsX);
			jPanelMove.add(getJRadioButtonCenter(), gridBagRadioCenter);
		}
		return jPanelMove;
	}

	/**
	 * This method initializes jPanelButtons
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			GridBagConstraints gridBagButtonOk = new GridBagConstraints();
			gridBagButtonOk.insets = new Insets(0, 0, 2, 9);
			gridBagButtonOk.anchor = GridBagConstraints.EAST;
			gridBagButtonOk.gridx = 2;
			GridBagConstraints gridBagButtonCancel = new GridBagConstraints();
			gridBagButtonCancel.anchor = GridBagConstraints.WEST;
			gridBagButtonCancel.insets = new Insets(0, 6, 2, 0);
			gridBagButtonCancel.weightx = 1.0;
			jPanelButtons = new JPanel();
			jPanelButtons.setLayout(new GridBagLayout());
			jPanelButtons.add(getJButtonCancel(), gridBagButtonCancel);
			jPanelButtons.add(getJButtonOk(), gridBagButtonOk);
		}
		return jPanelButtons;
	}

	/**
	 * This method initializes jButtonPrev
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonPrev() {
		if (jButtonPrev == null) {
			jButtonPrev = new JButton();
			jButtonPrev.setText("  <  ");
			jButtonPrev.setMnemonic(KeyEvent.VK_LEFT);
			jButtonPrev.setToolTipText("Lose changes and skip to previous frame");
			jButtonPrev.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (index > 0)
						setIndex(index-1);
				}
			});
		}
		return jButtonPrev;
	}

	/**
	 * This method initializes jButtonNext
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonNext() {
		if (jButtonNext == null) {
			jButtonNext = new JButton();
			jButtonNext.setText("  >  ");
			jButtonNext.setMnemonic(KeyEvent.VK_RIGHT);
			jButtonNext.setToolTipText("Lose changes and skip to next frame");
			jButtonNext.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (index < Core.getNumFrames()-1)
						setIndex(index+1);
				}
			});
		}
		return jButtonNext;
	}

	/**
	 * This method initializes jPanelPreview
	 *
	 * @return javax.swing.JPanel
	 */
	private EditPane getJPanelPreview() {
		if (jPanelPreview == null) {
			jPanelPreview = new EditPane();
			jPanelPreview.setLayout(new GridBagLayout());
			Dimension dim = new Dimension(miniWidth, miniHeight);
			jPanelPreview.setPreferredSize(dim);
			jPanelPreview.setSize(dim);
			jPanelPreview.setMinimumSize(dim);
			jPanelPreview.setMaximumSize(dim);
		}
		return jPanelPreview;
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
			jButtonCancel.setMnemonic('c');
			jButtonCancel.setToolTipText("Lose changes and return");
			jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return jButtonCancel;
	}

	/**
	 * This method initializes jButtonOk
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonOk() {
		if (jButtonOk == null) {
			jButtonOk = new JButton();
			jButtonOk.setText("Move all");
			jButtonOk.setMnemonic('m');
			jButtonOk.setPreferredSize(new Dimension(79, 23));
			jButtonOk.setToolTipText("Save changes and return");
			jButtonOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Core.setCropOfsY(cropOfsY);
					Core.setMoveModeX(moveModeX);
					Core.setMoveModeY(moveModeY);
					Core.setMoveOffsetX(offsetX);
					Core.setMoveOffsetY(offsetY);
					Core.setCineBarFactor(cineBarFactor);
					// moving is done in MainFrame
					dispose();
				}
			});
		}
		return jButtonOk;
	}

	/**
	 * This method initializes jTextFieldRatio
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldRatio() {
		if (jTextFieldRatio == null) {
			jTextFieldRatio = new JTextField();
			jTextFieldRatio.setPreferredSize(dimText);
			jTextFieldRatio.setSize(dimText);
			jTextFieldRatio.setMinimumSize(dimText);
			jTextFieldRatio.setMaximumSize(dimText);
			jTextFieldRatio.setToolTipText("Set inner frame ratio");
			jTextFieldRatio.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						double r = ToolBox.getDouble(jTextFieldRatio.getText());
						if ( r == -1.0 )
							r = screenRatioTrg; // invalid number -> keep old value
						else if (r > 4.0)
							r = 4.0;
						else if (r < screenRatio)
							r = screenRatio;
						if (r != screenRatioTrg) {
							screenRatioTrg = r;
							setRatio(screenRatioTrg);
						}
						jTextFieldRatio.setText(ToolBox.formatDouble(screenRatioTrg));
					}
				}
			});
			jTextFieldRatio.getDocument().addDocumentListener(new DocumentListener() {
				private void check(DocumentEvent e) {
					if (isReady) {
						double r = ToolBox.getDouble(jTextFieldRatio.getText());
						if ( r < screenRatio || r > 4.0 )
							jTextFieldRatio.setBackground(errBgnd);
						else {
							if (!ToolBox.formatDouble(r).equalsIgnoreCase(ToolBox.formatDouble(screenRatioTrg))) {
								screenRatioTrg = r;
								setRatio(screenRatioTrg);
							}
							jTextFieldRatio.setBackground(okBgnd);
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
		return jTextFieldRatio;
	}

	/**
	 * This method initializes jTextFieldOffset
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldOffsetY() {
		if (jTextFieldOffsetY == null) {
			jTextFieldOffsetY = new JTextField();
			jTextFieldOffsetY.setPreferredSize(dimText);
			jTextFieldOffsetY.setSize(dimText);
			jTextFieldOffsetY.setMinimumSize(dimText);
			jTextFieldOffsetY.setMaximumSize(dimText);
			jTextFieldOffsetY.setToolTipText("Set offset from lower/upper border in pixels");
			jTextFieldOffsetY.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int y = ToolBox.getInt(jTextFieldOffsetY.getText());

						if (y==-1)
							y = offsetY;  // invalid number -> keep old value
						else if (y < 0)
							y = 0;
						else if (y > subPic.height/3)
							y = subPic.height/3;

						if ( y != offsetY ) {
							offsetY = y;
							setRatio(screenRatioTrg);
						}
						jTextFieldOffsetY.setText(""+offsetY);
					}
				}
			});
			jTextFieldOffsetY.getDocument().addDocumentListener(new DocumentListener() {
				private void check(DocumentEvent e) {
					if (isReady) {
						int y = ToolBox.getInt(jTextFieldOffsetY.getText());

						if ( y < 0 || y > subPic.height/3 )
							jTextFieldOffsetY.setBackground(errBgnd);
						else {
							if (y != offsetY) {
								offsetY = y;
								setRatio(screenRatioTrg);
							}
							jTextFieldOffsetY.setBackground(okBgnd);
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
		return jTextFieldOffsetY;
	}

	/**
	 * error handler
	 * @param s error string to display
	 */
	public void error (final String s) {
		Core.printErr(s);
		JOptionPane.showMessageDialog(this,s,"Error!", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * get current subtitle index
	 * @return current subtitle index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * set current subtitle index, update all components
	 * @param idx subtitle index
	 */
	public void setIndex(final int idx) {
		isReady = false;
		index = idx;
		// update components
		try {
			Core.convertSup(idx, idx+1, Core.getNumFrames());
			subPic = Core.getSubPictureTrg(idx).clone();
			image = Core.getTrgImagePatched(subPic);

			originalX = subPic.getOfsX();
			originalY = subPic.getOfsY();

			jLabelInfo.setText("Frame "+(idx+1)+" of "+Core.getNumFrames());
			move();
			jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
			jPanelPreview.setDim(subPic.width, subPic.height);
			jPanelPreview.setImage(image,subPic.getImageWidth(), subPic.getImageHeight());
			jPanelPreview.setScreenRatio(screenRatioTrg);
			jPanelPreview.setCropOfsY(cropOfsY);
			jPanelPreview.setExcluded(subPic.exclude);
			jPanelPreview.repaint();
			isReady = true;

		} catch (CoreException ex) {
			error(ex.getMessage());
		} catch (Exception ex) {
			ToolBox.showException(ex);
			Core.exit();
			System.exit(4);
		}
	}

	/**
	 * This method initializes jButton21_9
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton21_9() {
		if (jButton21_9 == null) {
			jButton21_9 = new JButton();
			jButton21_9.setText("21:9");
			jButton21_9.setToolTipText("Set inner frame ratio to 21:9");
			jButton21_9.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setRatio(21.0/9);
				}
			});
		}
		return jButton21_9;
	}

	/**
	 * This method initializes jButton240_1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton240_1() {
		if (jButton240_1 == null) {
			jButton240_1 = new JButton();
			jButton240_1.setText("2.40:1");
			jButton240_1.setToolTipText("Set inner frame ratio to 2.40:1");
			jButton240_1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setRatio(2.4);
				}
			});
		}
		return jButton240_1;
	}

	/**
	 * This method initializes jButton235_1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton235_1() {
		if (jButton235_1 == null) {
			jButton235_1 = new JButton();
			jButton235_1.setText("2.35:1");
			jButton235_1.setToolTipText("Set inner frame ratio to 2.35:1");
			jButton235_1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setRatio(2.35);
				}
			});
		}
		return jButton235_1;
	}

	/**
	 * This method initializes jRadioButtonInside
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioButtonInside() {
		if (jRadioButtonInside == null) {
			jRadioButtonInside = new JRadioButton();
			jRadioButtonInside.setText("move inside bounds");
			jRadioButtonInside.setToolTipText("Move the subtitles inside the inner frame");
			jRadioButtonInside.setMnemonic('i');
			jRadioButtonInside.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					moveModeY = Core.MoveModeY.INSIDE;
					setRatio(screenRatioTrg);
				}
			});
		}
		return jRadioButtonInside;
	}

	/**
	 * This method initializes jRadioButtonOutside
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioButtonOutside() {
		if (jRadioButtonOutside == null) {
			jRadioButtonOutside = new JRadioButton();
			jRadioButtonOutside.setText("move outside bounds");
			jRadioButtonOutside.setToolTipText("Move the subtitles outside the inner frame as much as possible");
			jRadioButtonOutside.setMnemonic('o');
			jRadioButtonOutside.setDisplayedMnemonicIndex(5);
			jRadioButtonOutside.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					moveModeY = Core.MoveModeY.OUTSIDE;
					setRatio(screenRatioTrg);
				}
			});
		}
		return jRadioButtonOutside;
	}

	/**
	 * This method initializes jRadioButtonKeepY
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioButtonKeepY() {
		if (jRadioButtonKeepY == null) {
			jRadioButtonKeepY = new JRadioButton();
			jRadioButtonKeepY.setText("keep Y position");
			jRadioButtonKeepY.setToolTipText("Don't alter current Y position");
			jRadioButtonKeepY.setMnemonic('y');
			jRadioButtonKeepY.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					moveModeY = Core.MoveModeY.KEEP;
					subPic.setOfsY(originalY);
					setRatio(screenRatioTrg);
				}
			});
		}
		return jRadioButtonKeepY;
	}

	private void setRatio(double ratio) {
		if (!ToolBox.formatDouble(screenRatioTrg).equalsIgnoreCase(ToolBox.formatDouble(ratio)))
			jTextFieldRatio.setText(ToolBox.formatDouble(ratio));
		screenRatioTrg = ratio;
		cineBarFactor = (1.0 - screenRatio/screenRatioTrg)/2.0;
		move();
		jPanelPreview.setScreenRatio(screenRatioTrg);
		jPanelPreview.setOffsets(subPic.getOfsX(), subPic.getOfsY());
		jPanelPreview.repaint();
	}

	private void move() {
		Core.moveToBounds(subPic, index+1, cineBarFactor, offsetX, offsetY, moveModeX, moveModeY, cropOfsY);
	}

	/**
	 * This method initializes jPanelCrop
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelCrop() {
		if (jPanelCrop == null) {
			GridBagConstraints gridBagBtnCrop = new GridBagConstraints();
			gridBagBtnCrop.anchor = GridBagConstraints.EAST;
			gridBagBtnCrop.insets = new Insets(0, 0, 0, 0);
			gridBagBtnCrop.weightx = 10.0;
			GridBagConstraints gridBagLabelCropY = new GridBagConstraints();
			gridBagLabelCropY.anchor = GridBagConstraints.WEST;
			gridBagLabelCropY.insets = new Insets(0, 6, 0, 4);
			gridBagLabelCropY.weightx = 0.0;
			GridBagConstraints gridBagTextCropY = new GridBagConstraints();
			gridBagTextCropY.fill = GridBagConstraints.NONE;
			gridBagTextCropY.insets = new Insets(0, 0, 0, 0);
			gridBagTextCropY.anchor = GridBagConstraints.WEST;
			gridBagTextCropY.weightx = 2.0;
			jLabelCropOfsY = new JLabel();
			jLabelCropOfsY.setPreferredSize(dimLabel);
			jLabelCropOfsY.setSize(dimLabel);
			jLabelCropOfsY.setMinimumSize(dimLabel);
			jLabelCropOfsY.setMaximumSize(dimLabel);
			jLabelCropOfsY.setText("Crop Offset Y");
			jPanelCrop = new JPanel();
			jPanelCrop.setLayout(new GridBagLayout());
			jPanelCrop.setBorder(BorderFactory.createTitledBorder(null, "Crop", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanelCrop.add(jLabelCropOfsY, gridBagLabelCropY);
			jPanelCrop.add(getJTextFieldCropOfsY(), gridBagTextCropY);
			jPanelCrop.add(getJButtonCropBars(), gridBagBtnCrop);
		}
		return jPanelCrop;
	}

	/**
	 * This method initializes jTextFieldCropOfsY
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldCropOfsY() {
		if (jTextFieldCropOfsY == null) {
			jTextFieldCropOfsY = new JTextField();
			jTextFieldCropOfsY.setPreferredSize(dimText);
			jTextFieldCropOfsY.setSize(dimText);
			jTextFieldCropOfsY.setMinimumSize(dimText);
			jTextFieldCropOfsY.setMaximumSize(dimText);
			jTextFieldCropOfsY.setToolTipText("Set number of lines to be cropped from upper and lower border");
			jTextFieldCropOfsY.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int y = ToolBox.getInt(jTextFieldCropOfsY.getText());

						if (y==-1)
							y = cropOfsY;   // invalid number -> keep old value
						else if (y<0)
							y = 0;
						else if (y > subPic.height/3)
							y = subPic.height/3;

						if (y != cropOfsY) {
							cropOfsY = y;
							jPanelPreview.setCropOfsY(cropOfsY);
							setRatio(screenRatioTrg);
						}
						jTextFieldCropOfsY.setText(""+cropOfsY);
					}
				}
			});
			jTextFieldCropOfsY.getDocument().addDocumentListener(new DocumentListener() {
				private void check(DocumentEvent e) {
					if (isReady) {
						int y = ToolBox.getInt(jTextFieldCropOfsY.getText());

						if ( y < 0 || y > subPic.height/3 )
							jTextFieldCropOfsY.setBackground(errBgnd);
						else {
							if (y != cropOfsY) {
								cropOfsY = y;
								jPanelPreview.setCropOfsY(cropOfsY);
								setRatio(screenRatioTrg);
							}
							jTextFieldCropOfsY.setBackground(okBgnd);
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
		return jTextFieldCropOfsY;
	}

	/**
	 * This method initializes jButtonCropBars
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCropBars() {
		if (jButtonCropBars == null) {
			jButtonCropBars = new JButton();
			jButtonCropBars.setToolTipText("Set crop offsets to cinemascope bars");
			jButtonCropBars.setText("Crop Bars");
			jButtonCropBars.setPreferredSize(new Dimension(79, 23));
			jButtonCropBars.setMnemonic('c');
			jButtonCropBars.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cropOfsY = (int)(subPic.height*cineBarFactor+0.5); // height of one cinemascope bar in pixels
					jPanelPreview.setCropOfsY(cropOfsY);
					setRatio(screenRatioTrg);
					jTextFieldCropOfsY.setText(""+cropOfsY);
				}
			});
		}
		return jButtonCropBars;
	}

	/**
	 * Get screen ratio used for displaying the cinemascopic bars
	 * @return screen ratio used for displaying the cinemascopic bars
	 */
	public double getTrgRatio() {
		return screenRatioTrg;
	}

	/**
	 * This method initializes jRadioButtonKeepX
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioButtonKeepX() {
		if (jRadioButtonKeepX == null) {
			jRadioButtonKeepX = new JRadioButton();
			jRadioButtonKeepX.setText("keep X position");
			jRadioButtonKeepX.setToolTipText("Don't alter current X position");
			jRadioButtonKeepX.setMnemonic('x');
			jRadioButtonKeepX.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					moveModeX = Core.MoveModeX.KEEP;
					subPic.setOfsX(originalX);
					setRatio(screenRatioTrg);
				}
			});
		}
		return jRadioButtonKeepX;
	}

	/**
	 * This method initializes jRadioButtonLeft
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioButtonLeft() {
		if (jRadioButtonLeft == null) {
			jRadioButtonLeft = new JRadioButton();
			jRadioButtonLeft.setText("move left");
			jRadioButtonLeft.setToolTipText("Move to the left");
			jRadioButtonLeft.setMnemonic('l');
			jRadioButtonLeft.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					moveModeX = Core.MoveModeX.LEFT;
					setRatio(screenRatioTrg);
				}
			});
		}
		return jRadioButtonLeft;
	}

	/**
	 * This method initializes jRadioButtonRight
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioButtonRight() {
		if (jRadioButtonRight == null) {
			jRadioButtonRight = new JRadioButton();
			jRadioButtonRight.setText("move right");
			jRadioButtonRight.setToolTipText("Move to the right");
			jRadioButtonRight.setMnemonic('r');
			jRadioButtonRight.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					moveModeX = Core.MoveModeX.RIGHT;
					setRatio(screenRatioTrg);
				}
			});
		}
		return jRadioButtonRight;
	}

	/**
	 * This method initializes jRadioButtonCenter
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioButtonCenter() {
		if (jRadioButtonCenter == null) {
			jRadioButtonCenter = new JRadioButton();
			jRadioButtonCenter.setText("move to center");
			jRadioButtonCenter.setToolTipText("Move to center");
			jRadioButtonCenter.setMnemonic('c');
			jRadioButtonCenter.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					moveModeX = Core.MoveModeX.CENTER;
					setRatio(screenRatioTrg);
				}
			});
		}
		return jRadioButtonCenter;
	}

	/**
	 * This method initializes jTextFieldOffsetX
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldOffsetX() {
		if (jTextFieldOffsetX == null) {
			jTextFieldOffsetX = new JTextField();
			jTextFieldOffsetX.setPreferredSize(dimText);
			jTextFieldOffsetX.setSize(dimText);
			jTextFieldOffsetX.setMinimumSize(dimText);
			jTextFieldOffsetX.setMaximumSize(dimText);
			jTextFieldOffsetX.setToolTipText("Set offset from left/right border in pixels");
			jTextFieldOffsetX.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int x = ToolBox.getInt(jTextFieldOffsetX.getText());

						if (x==-1)
							x = offsetX;  // invalid number -> keep old value
						else if (x < 0)
							x = 0;
						else if (x > subPic.width/3)
							x = subPic.width/3;

						if ( x != offsetX ) {
							offsetX = x;
							setRatio(screenRatioTrg);
						}
						jTextFieldOffsetX.setText(""+offsetX);
					}
				}
			});
			jTextFieldOffsetX.getDocument().addDocumentListener(new DocumentListener() {
				private void check(DocumentEvent e) {
					if (isReady) {
						int x = ToolBox.getInt(jTextFieldOffsetX.getText());

						if ( x < 0 || x > subPic.width/3 )
							jTextFieldOffsetX.setBackground(errBgnd);
						else {
							if (x != offsetX) {
								offsetX = x;
								setRatio(screenRatioTrg);
							}
							jTextFieldOffsetX.setBackground(okBgnd);
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
		return jTextFieldOffsetX;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
