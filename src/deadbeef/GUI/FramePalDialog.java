package deadbeef.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import deadbeef.SupTools.Core;
import deadbeef.SupTools.Palette;
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
 * Edit dialog for frame palette and alpha - part of BDSup2Sub GUI classes.
 *
 * @author 0xdeadbeef
 */
public class FramePalDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JButton jButtonOk = null;

	private JButton jButtonCancel = null;

	private JLabel jLabelColor1 = null;

	private JLabel jLabelColor2 = null;

	private JLabel jLabelColor3 = null;

	private JLabel jLabelColor4 = null;

	private JLabel jLabelAlpha1 = null;

	private JLabel jLabelAlpha2 = null;

	private JLabel jLabelAlpha3 = null;

	private JLabel jLabelAlpha4 = null;

	private JComboBox jComboBoxColor1 = null;

	private JComboBox jComboBoxColor2 = null;

	private JComboBox jComboBoxColor3 = null;

	private JComboBox jComboBoxColor4 = null;

	private JComboBox jComboBoxAlpha1 = null;

	private JComboBox jComboBoxAlpha2 = null;

	private JComboBox jComboBoxAlpha3 = null;

	private JComboBox jComboBoxAlpha4 = null;

	private JButton jButtonSetAll = null;

	private JButton jButtonResetAll = null;

	private JButton jButtonReset = null;


	/** array containing the 16 numeric values 00 - 15 as strings */
	private final static String[] cName = {	"00", "01", "02", "03", "04", "05", "06" ,"07",
											"08", "09", "10", "11", "12", "13", "14", "15"};

	/** current subtitle index */
	private int index;
	/** image icons to preview color */
	private final ImageIcon cIcon[];
	/** semaphore to disable actions while changing component properties */
	private volatile boolean isReady = false;
	/* frame alpha */
	private int alpha[];
	/* frame palette */
	private int pal[];
	/** cancel state */
	private boolean canceled = true;

	/**
	 * Constructor for modal dialog in parent frame
	 * @param frame parent frame
	 * @param modal create modal dialog?
	 */
	public FramePalDialog(JFrame frame, boolean modal) {
		super(frame, modal);
		initialize();

		//
		// center to parent frame
		Point p = frame.getLocation();
		this.setLocation(p.x+frame.getWidth()/2-getWidth()/2, p.y+frame.getHeight()/2-getHeight()/2);
		this.setResizable(false);
		//
		Palette pal = Core.getCurSrcDVDPalette();
		cIcon = new ImageIcon[16];
		for (int i=0; i < pal.getSize(); i++) {
			cIcon[i] = new ImageIcon(new BufferedImage(12,12,BufferedImage.TYPE_INT_RGB ));
			Color c = new Color (pal.getARGB(i) | 0xff000000); // make fully opaque
			paintIcon(cIcon[i],c);
			jComboBoxColor1.addItem(cName[i]);
			jComboBoxColor2.addItem(cName[i]);
			jComboBoxColor3.addItem(cName[i]);
			jComboBoxColor4.addItem(cName[i]);
			jComboBoxAlpha1.addItem(cName[i]);
			jComboBoxAlpha2.addItem(cName[i]);
			jComboBoxAlpha3.addItem(cName[i]);
			jComboBoxAlpha4.addItem(cName[i]);
		}
		MyListCellRenderer myListCellRenderer = new MyListCellRenderer();
		jComboBoxColor1.setRenderer(myListCellRenderer);
		jComboBoxColor2.setRenderer(myListCellRenderer);
		jComboBoxColor3.setRenderer(myListCellRenderer);
		jComboBoxColor4.setRenderer(myListCellRenderer);
		jComboBoxColor1.repaint();
		jComboBoxColor2.repaint();
		jComboBoxColor3.repaint();
		jComboBoxColor4.repaint();
	}

	/**
	 * Modified ListCellRenderer to display text and color icons
	 * @author 0xdeadbeef
	 */
	private class MyListCellRenderer extends DefaultListCellRenderer {
		final static long serialVersionUID = 0x000000001;
		@Override
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,  boolean cellHasFocus) {
			Component retValue = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus);
			int idx = ToolBox.getInt(value.toString());
			if (idx >= 0) {
				setText(cName[idx]);
				setIcon(cIcon[idx]);
			}
			return retValue;
		}
	}

	/**
	 * paint icon in a given color
	 * @param b JButton to paint
	 * @param c Color to paint
	 */
	private void paintIcon(final ImageIcon i, final Color c) {
		Graphics g = i.getImage().getGraphics();
		g.setColor(c);
		g.setPaintMode();
		g.fillRect(0,0,i.getIconWidth(),i.getIconHeight());
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setSize(294, 209);
		this.setTitle("Edit Frame Palette");
		this.setContentPane(getJContentPane());
	}

	/**
	 * set current subtitle index, update all components
	 * @param idx subtitle index
	 */
	public void setIndex(final int idx) {
		index = idx;
		isReady = false;

		// we need a deep copy here to allow editing
		alpha = new int[4];
		pal = new int[4];
		int a[] = Core.getFrameAlpha(index);
		int p[] = Core.getFramePal(index);
		for (int i=0; i<4; i++) {
			if (a != null)
				alpha[i] = a[i];
			if (p != null)
				pal[i]   = p[i];
		};

		jComboBoxAlpha1.setSelectedIndex(alpha[0]);
		jComboBoxAlpha2.setSelectedIndex(alpha[1]);
		jComboBoxAlpha3.setSelectedIndex(alpha[2]);
		jComboBoxAlpha4.setSelectedIndex(alpha[3]);

		jComboBoxColor1.setSelectedIndex(pal[0]);
		jComboBoxColor2.setSelectedIndex(pal[1]);
		jComboBoxColor3.setSelectedIndex(pal[2]);
		jComboBoxColor4.setSelectedIndex(pal[3]);

		isReady = true;
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabelAlpha4 = new JLabel();
			jLabelAlpha4.setBounds(new Rectangle(155, 85, 36, 16));
			jLabelAlpha4.setText("Alpha 4");
			jLabelAlpha3 = new JLabel();
			jLabelAlpha3.setBounds(new Rectangle(155, 60, 36, 16));
			jLabelAlpha3.setText("Alpha 3");
			jLabelAlpha2 = new JLabel();
			jLabelAlpha2.setBounds(new Rectangle(155, 35, 36, 16));
			jLabelAlpha2.setText("Alpha 2");
			jLabelAlpha1 = new JLabel();
			jLabelAlpha1.setBounds(new Rectangle(155, 10, 41, 16));
			jLabelAlpha1.setText("Alpha 1");
			jLabelColor4 = new JLabel();
			jLabelColor4.setBounds(new Rectangle(15, 85, 45, 16));
			jLabelColor4.setText("Color 4");
			jLabelColor3 = new JLabel();
			jLabelColor3.setBounds(new Rectangle(15, 60, 46, 16));
			jLabelColor3.setText("Color 3");
			jLabelColor2 = new JLabel();
			jLabelColor2.setBounds(new Rectangle(15, 35, 46, 16));
			jLabelColor2.setText("Color 2");
			jLabelColor1 = new JLabel();
			jLabelColor1.setBounds(new Rectangle(15, 10, 46, 16));
			jLabelColor1.setText("Color 1");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJButtonOk(), null);
			jContentPane.add(getJButtonCancel(), null);
			jContentPane.add(getJComboBoxColor1(), null);
			jContentPane.add(jLabelColor1, null);
			jContentPane.add(jLabelColor2, null);
			jContentPane.add(jLabelColor3, null);
			jContentPane.add(jLabelColor4, null);
			jContentPane.add(jLabelAlpha1, null);
			jContentPane.add(jLabelAlpha2, null);
			jContentPane.add(jLabelAlpha3, null);
			jContentPane.add(jLabelAlpha4, null);
			jContentPane.add(getJComboBoxColor2(), null);
			jContentPane.add(getJComboBoxColor3(), null);
			jContentPane.add(getJComboBoxColor4(), null);
			jContentPane.add(getJComboBoxAlpha1(), null);
			jContentPane.add(getJComboBoxAlpha2(), null);
			jContentPane.add(getJComboBoxAlpha3(), null);
			jContentPane.add(getJComboBoxAlpha4(), null);
			jContentPane.add(getJButtonSetAll(), null);
			jContentPane.add(getJButtonResetAll(), null);
			jContentPane.add(getJButtonReset(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes JComboBoxColor1
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxColor1() {
		if (jComboBoxColor1 == null) {
			jComboBoxColor1 = new JComboBox();
			jComboBoxColor1.setBounds(new Rectangle(70, 10, 61, 16));
			jComboBoxColor1.setEditable(false);
			jComboBoxColor1.setToolTipText("Set palette index of frame color 1");
			jComboBoxColor1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int idx = ToolBox.getInt(jComboBoxColor1.getSelectedItem().toString());
						if (idx >= 0 && idx < 16)
							pal[0] = idx;
					}
				}
			});
		}
		return jComboBoxColor1;
	}

	/**
	 * This method initializes JComboBoxColor2
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxColor2() {
		if (jComboBoxColor2 == null) {
			jComboBoxColor2 = new JComboBox();
			jComboBoxColor2.setBounds(new Rectangle(70, 35, 61, 16));
			jComboBoxColor2.setEditable(false);
			jComboBoxColor2.setToolTipText("Set palette index of frame color 2");
			jComboBoxColor2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int idx = ToolBox.getInt(jComboBoxColor2.getSelectedItem().toString());
						if (idx >= 0 && idx < 16)
							pal[1] = idx;
					}
				}
			});
		}
		return jComboBoxColor2;
	}

	/**
	 * This method initializes JComboBoxColor3
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxColor3() {
		if (jComboBoxColor3 == null) {
			jComboBoxColor3 = new JComboBox();
			jComboBoxColor3.setBounds(new Rectangle(70, 60, 61, 16));
			jComboBoxColor3.setEditable(false);
			jComboBoxColor3.setToolTipText("Set palette index of frame color 3");
			jComboBoxColor3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int idx = ToolBox.getInt(jComboBoxColor3.getSelectedItem().toString());
						if (idx >= 0 && idx < 16)
							pal[2] = idx;
					}
				}
			});
		}
		return jComboBoxColor3;
	}

	/**
	 * This method initializes JComboBoxColor4
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxColor4() {
		if (jComboBoxColor4 == null) {
			jComboBoxColor4 = new JComboBox();
			jComboBoxColor4.setBounds(new Rectangle(70, 85, 61, 16));
			jComboBoxColor4.setEditable(false);
			jComboBoxColor4.setToolTipText("Set palette index of frame color 4");
			jComboBoxColor4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int idx = ToolBox.getInt(jComboBoxColor4.getSelectedItem().toString());
						if (idx >= 0 && idx < 16)
							pal[3] = idx;
					}
				}
			});
		}
		return jComboBoxColor4;
	}

	/**
	 * This method initializes JComboBoxAlpha1
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxAlpha1() {
		if (jComboBoxAlpha1 == null) {
			jComboBoxAlpha1 = new JComboBox();
			jComboBoxAlpha1.setBounds(new Rectangle(215, 10, 56, 16));
			jComboBoxAlpha1.setEditable(false);
			jComboBoxAlpha1.setToolTipText("Set alpha value of frame color 1");
			jComboBoxAlpha1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int idx = ToolBox.getInt(jComboBoxAlpha1.getSelectedItem().toString());
						if (idx >= 0 && idx < 16)
							alpha[0] = idx;
					}
				}
			});
		}
		return jComboBoxAlpha1;
	}

	/**
	 * This method initializes JComboBoxAlpha2
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxAlpha2() {
		if (jComboBoxAlpha2 == null) {
			jComboBoxAlpha2 = new JComboBox();
			jComboBoxAlpha2.setBounds(new Rectangle(215, 35, 56, 16));
			jComboBoxAlpha2.setEditable(false);
			jComboBoxAlpha2.setToolTipText("Set alpha value of frame color 2");
			jComboBoxAlpha2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int idx = ToolBox.getInt(jComboBoxAlpha2.getSelectedItem().toString());
						if (idx >= 0 && idx < 16)
							alpha[1] = idx;
					}
				}
			});
		}
		return jComboBoxAlpha2;
	}

	/**
	 * This method initializes JComboBoxAlpha3
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxAlpha3() {
		if (jComboBoxAlpha3 == null) {
			jComboBoxAlpha3 = new JComboBox();
			jComboBoxAlpha3.setBounds(new Rectangle(215, 60, 56, 16));
			jComboBoxAlpha3.setEditable(false);
			jComboBoxAlpha3.setToolTipText("Set alpha value of frame color 3");
			jComboBoxAlpha3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int idx = ToolBox.getInt(jComboBoxAlpha3.getSelectedItem().toString());
						if (idx >= 0 && idx < 16)
							alpha[2] = idx;
					}
				}
			});
		}
		return jComboBoxAlpha3;
	}

	/**
	 * This method initializes JComboBoxAlpha4
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxAlpha4() {
		if (jComboBoxAlpha4 == null) {
			jComboBoxAlpha4 = new JComboBox();
			jComboBoxAlpha4.setBounds(new Rectangle(215, 85, 56, 16));
			jComboBoxAlpha4.setEditable(false);
			jComboBoxAlpha4.setToolTipText("Set alpha value of frame color 4");
			jComboBoxAlpha4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (isReady) {
						int idx = ToolBox.getInt(jComboBoxAlpha4.getSelectedItem().toString());
						if (idx >= 0 && idx < 16)
							alpha[3] = idx;
					}
				}
			});
		}
		return jComboBoxAlpha4;
	}

	/**
	 * This method initializes jButtonOk
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonOk() {
		if (jButtonOk == null) {
			jButtonOk = new JButton();
			jButtonOk.setName("jButtonOk");
			jButtonOk.setBounds(new Rectangle(200, 145, 75, 21));
			jButtonOk.setText("Ok");
			jButtonOk.setToolTipText("Use current settings and return");
			jButtonOk.setMnemonic('o');
			jButtonOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int a[] = Core.getFrameAlpha(index);
					int p[] = Core.getFramePal(index);
					for (int i= 0; i<4; i++) {
						if (a != null)
							a[i] = alpha[i];
						if (p != null)
							p[i] = pal[i];
					}
					dispose();
				}
			});
		}
		return jButtonOk;
	}

	/**
	 * This method initializes jButtonCancel
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton();
			jButtonCancel.setName("jButtonCancel");
			jButtonCancel.setBounds(new Rectangle(10, 145, 75, 21));
			jButtonCancel.setText("Cancel");
			jButtonCancel.setToolTipText("Lose changes and return");
			jButtonCancel.setMnemonic('c');
			jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return jButtonCancel;
	}

	/**
	 * This method initializes jButtonSetAll
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonSetAll() {
		if (jButtonSetAll == null) {
			jButtonSetAll = new JButton();
			jButtonSetAll.setBounds(new Rectangle(200, 115, 75, 21));
			jButtonSetAll.setText("Set All");
			jButtonSetAll.setToolTipText("Apply these settings for whole stream and return");
			jButtonSetAll.setMnemonic('s');
			jButtonSetAll.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					for (int j=0; j<Core.getNumFrames(); j++) {
						int a[] = Core.getFrameAlpha(j);
						int p[] = Core.getFramePal(j);
						for (int i= 0; i<4; i++) {
							if (a != null)
								a[i] = alpha[i];
							if (p != null)
								p[i] = pal[i];
						}
					}
					dispose();
				}
			});
		}
		return jButtonSetAll;
	}

	/**
	 * return cancel state of this dialog
	 * @return true if canceled
	 */
	public boolean wasCanceled() {
		return canceled;
	}

	/**
	 * This method initializes jButtonResetAll
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonResetAll() {
		if (jButtonResetAll == null) {
			jButtonResetAll = new JButton();
			jButtonResetAll.setBounds(new Rectangle(105, 115, 75, 23));
			jButtonResetAll.setText("Reset All");
			jButtonResetAll.setToolTipText("Revert to original frame palettes for whole stream and return");
			jButtonResetAll.setMnemonic('a');
			jButtonResetAll.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					for (int j=0; j<Core.getNumFrames(); j++) {
						int ao[] = Core.getOriginalFrameAlpha(j);
						int po[] = Core.getOriginalFramePal(j);
						int a[] = Core.getFrameAlpha(j);
						int p[] = Core.getFramePal(j);
						for (int i= 0; i<4; i++) {
							if (a != null && ao != null)
								a[i] = ao[i];
							if (p != null && po != null)
								p[i] = po[i];
						}
					}
					dispose();
				}
			});
		}
		return jButtonResetAll;
	}

	/**
	 * This method initializes jButtonReset
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonReset() {
		if (jButtonReset == null) {
			jButtonReset = new JButton();
			jButtonReset.setBounds(new Rectangle(105, 145, 75, 23));
			jButtonReset.setText("Reset");
			jButtonReset.setToolTipText("Revert to original frame palette");
			jButtonReset.setMnemonic('r');
			jButtonReset.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int ao[] = Core.getOriginalFrameAlpha(index);
					int po[] = Core.getOriginalFramePal(index);
					int a[] = Core.getFrameAlpha(index);
					int p[] = Core.getFramePal(index);
					for (int i= 0; i<4; i++) {
						if (a != null && ao != null)
							a[i] = ao[i];
						if (p != null && po != null)
							p[i] = po[i];
					}
					setIndex(index);
				}
			});
		}
		return jButtonReset;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
