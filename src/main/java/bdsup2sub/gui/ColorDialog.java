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

import bdsup2sub.core.CoreException;
import bdsup2sub.tools.Props;
import bdsup2sub.utils.FilenameUtils;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static bdsup2sub.core.Constants.APP_NAME_AND_VERSION;
import static bdsup2sub.gui.support.GuiUtils.centerRelativeToOwner;

public class ColorDialog extends JDialog {
    private static final long serialVersionUID = 0x000000001;

    private JPanel jContentPane;
    private JScrollPane jScrollPane;
    private JList jList;
    private JButton btnOk;
    private JButton btnCancel;
    private JButton btnDefault;
    private JButton btnColor;
    private JButton jButtonSave;
    private JButton jButtonLoad;

    /** image icons to preview color */
    private ImageIcon cIcon[];
    /** selected colors */
    private Color  cColor[];
    /** default colors */
    private Color  cColorDefault[];
    /** color names */
    private String cName[];
    /** path to load and store color profiles */
    private String colorPath;
    /** cancel state */
    private boolean canceled = true;
    /** global reference to this class */
    private Component thisFrame;

    /**
     * Paint JButton's Icon in a given color
     * @param i ImageIcon to paint
     * @param c Color to paint
     */
    private void paintIcon(ImageIcon i, Color c) {
        Graphics g = i.getImage().getGraphics();
        g.setColor(c);
        g.setPaintMode();
        g.fillRect(0,0,i.getIconWidth(),i.getIconHeight());
    }

    /**
     * Change a color (by pressing the according button or double clicking the list item
     * @param idx index of color
     */
    private void changeColor(int idx) {
        Color c = JColorChooser.showDialog( null, "Chose Input Color "+cName[idx], cColor[idx] );
        if (c != null) {
            cColor[idx] = c;
        }
        paintIcon(cIcon[idx],  cColor[idx]);
        jList.repaint();
    }

    /**
     * Set dialog parameters
     * @param name array of color names
     * @param cCurrent current colors
     * @param cDefault default colors
     */
    public void setParameters(String name[], Color cCurrent[], Color cDefault[]) {
        cName = name;
        cColorDefault = cDefault;
        cColor = new Color[cName.length];
        cIcon = new ImageIcon[cName.length];
        // initialize color list box
        jList = new JList(cName);
        jList.setSelectedIndex(0);
        jList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
        jList.setCellRenderer(new myListCellRenderer());
        for (int i=0; i < cName.length; i++) {
            cIcon[i] = new ImageIcon(new BufferedImage(12,12,BufferedImage.TYPE_INT_RGB ));
            cColor[i] = new Color(cCurrent[i].getRGB());
            paintIcon(cIcon[i],cColor[i]);
        }
        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    changeColor(((JList) e.getSource()).locationToIndex(e.getPoint()));
                }
            }
        });

        jScrollPane.setViewportView(jList);
    }

    /**
     * get edited colors
     * @return array of colors
     */
    public Color[] getColors() {
        return cColor;
    }

    /**
     * Constructor for modal dialog in parent frame
     * @param frame parent frame
     *
     */
    public ColorDialog(JFrame frame) {
        super(frame, true);
        thisFrame = this;
        initialize();

        centerRelativeToOwner(this);
        this.setResizable(false);
    }

    /**
     * This method initializes this dialog
     */
    private void initialize() {
        this.setContentPane(getJContentPane());
        this.setSize(372, 231);
        this.setTitle("Choose Colors");
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cColor = cColorDefault; // same as cancel
            }
        });
    }

    /**
     * This method initializes jContentPane
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if(jContentPane == null) {
            JLabel lblColor = new JLabel();
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            lblColor.setText("Choose Color");
            lblColor.setBounds(15, 9, 73, 16);
            jContentPane.add(lblColor, null);
            jContentPane.add(getBtnOk(), null);
            jContentPane.add(getBtnCancel(), null);
            jContentPane.add(getBtnDefault(), null);
            jContentPane.add(getJScrollPane(), null);
            jContentPane.add(getBtnColor(), null);
            jContentPane.add(getJButtonSave(), null);
            jContentPane.add(getJButtonLoad(), null);
        }
        return jContentPane;
    }

    /**
     * This method initializes jScrollPane
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setBounds(15, 30, 150, 152);
        }
        return jScrollPane;
    }

    /**
     * This method initializes btnOk
     * @return javax.swing.JButton
     */
    private JButton getBtnOk() {
        if (btnOk == null) {
            btnOk = new JButton();
            btnOk.setBounds(280, 162, 66, 20);
            btnOk.setText("OK");
            btnOk.setToolTipText("Apply changes and return");
            btnOk.setMnemonic('o');
            btnOk.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    canceled = false;
                    dispose();
                }
            });
        }
        return btnOk;
    }

    /**
     * This method initializes btnCancel
     * @return javax.swing.JButton
     */
    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setBounds(175, 161, 70, 20);
            btnCancel.setText("Cancel");
            btnCancel.setToolTipText("Lose changes and return");
            btnCancel.setMnemonic('c');
            btnCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cColor = cColorDefault;
                    dispose();
                }
            });
        }
        return btnCancel;
    }

    /**
     * This method initializes btnDefault
     * @return javax.swing.JButton
     */
    private JButton getBtnDefault() {
        if (btnDefault == null) {
            btnDefault = new JButton();
            btnDefault.setBounds(175, 60, 170, 20);
            btnDefault.setText("Restore default Colors");
            btnDefault.setToolTipText("Revert to default colors");
            btnDefault.setMnemonic('r');
            btnDefault.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for (int i=0; i < cName.length; i++) {
                        cColor[i] = new Color(cColorDefault[i].getRGB());
                        paintIcon(cIcon[i],  cColor[i]);
                    }
                    jList.repaint();
                }
            });
        }
        return btnDefault;
    }

    /**
     * Modified ListCellRenderer to display text and color icons
     */
    private class myListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 0x000000001;
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,  boolean cellHasFocus) {
            Component retValue = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus);
            setText(cName[index]);
            setIcon(cIcon[index]);
            return retValue;
        }
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnColor() {
        if (btnColor == null) {
            btnColor = new JButton();
            btnColor.setBounds(175, 30, 170, 20);
            btnColor.setText("Change Color");
            btnColor.setToolTipText("Edit the selected color");
            btnColor.setMnemonic('h');
            btnColor.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int idx = jList.getSelectedIndex();
                    changeColor(idx);
                }
            });
        }
        return btnColor;
    }

    /**
     * return path to color profiles
     * @return path to color profiles
     */
    public String getPath() {
        return colorPath;
    }

    /**
     * set path to color profiles
     * @param p path to color profiles
     */
    public void setPath(String p) {
        colorPath = p;
    }

    /**
     * return cancel state of this dialog
     * @return true if canceled
     */
    public boolean wasCanceled() {
        return canceled;
    }

    /**
     * This method initializes jButtonSave
     * @return javax.swing.JButton
     */
    private JButton getJButtonSave() {
        if (jButtonSave == null) {
            jButtonSave = new JButton();
            jButtonSave.setBounds(new Rectangle(175, 92, 170, 20));
            jButtonSave.setText("Save Palette");
            jButtonSave.setToolTipText("Save the current palette settings in an INI file");
            jButtonSave.setMnemonic('s');
            jButtonSave.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] ext = new String[1];
                    ext[0] = "ini";
                    String p = FilenameUtils.getParent(colorPath);
                    String fn = FilenameUtils.getName(colorPath);
                    String fname = ToolBox.getFilename(p, fn, ext, false, thisFrame);
                    if (fname != null) {
                        fname = FilenameUtils.removeExtension(fname) + ".ini";
                        File f = new File(fname);
                        try {
                            if (f.exists()) {
                                if ((f.exists() && !f.canWrite()) || (f.exists() && !f.canWrite())) {
                                    throw new CoreException("Target is write protected.");
                                }
                                if (JOptionPane.showConfirmDialog(thisFrame, "Target exists! Overwrite?",
                                        "", JOptionPane.YES_NO_OPTION) == 1) {
                                    throw new CoreException();
                                }
                            }
                            colorPath = fname;
                            Props colProps = new Props();
                            colProps.setHeader("COL - created by " + APP_NAME_AND_VERSION);
                            for (int i=0; i<cColor.length; i++) {
                                String s = ""+cColor[i].getRed()+","+cColor[i].getGreen()+","+cColor[i].getBlue();
                                colProps.set("Color_"+i, s);
                            }
                            colProps.save(colorPath);
                        } catch (CoreException ex) {
                            if (ex.getMessage() != null) {
                                JOptionPane.showMessageDialog(thisFrame,ex.getMessage(),
                                        "Error!", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                }
            });
        }
        return jButtonSave;
    }

    /**
     * This method initializes jButtonLoad
     * @return javax.swing.JButton
     */
    private JButton getJButtonLoad() {
        if (jButtonLoad == null) {
            jButtonLoad = new JButton();
            jButtonLoad.setBounds(new Rectangle(178, 126, 168, 18));
            jButtonLoad.setText("Load Palette");
            jButtonLoad.setToolTipText("Load palette settings from an INI file");
            jButtonLoad.setMnemonic('l');
            jButtonLoad.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] ext = new String[1];
                    ext[0] = "ini";
                    String p = FilenameUtils.getParent(colorPath);
                    String fn = FilenameUtils.getName(colorPath);
                    String fname = ToolBox.getFilename(p, fn, ext, true, thisFrame);
                    if (fname != null) {
                        File f = new File(fname);
                        try {
                            if (f.exists()) {
                                byte id[] = ToolBox.getFileID(fname, 4);
                                if (id == null || id[0] != 0x23 || id[1] != 0x43 || id[2]!= 0x4F || id[3] != 0x4C) { //#COL
                                    JOptionPane.showMessageDialog(thisFrame, "This is not a valid palette file",
                                            "Wrong format!", JOptionPane.WARNING_MESSAGE);
                                    throw new CoreException();
                                }
                            } else {
                                throw new CoreException("File not found.");
                            }
                            Props colProps = new Props();
                            colProps.load(fname);
                            colorPath = fname;
                            for (int i=0; i < cColor.length; i++) {
                                String s = colProps.get("Color_"+i, "0,0,0");
                                String sp[] = s.split(",");
                                if (sp.length >= 3) {
                                    int r = Integer.valueOf(sp[0].trim())&0xff;
                                    int g = Integer.valueOf(sp[1].trim())&0xff;
                                    int b = Integer.valueOf(sp[2].trim())&0xff;
                                    cColor[i] = new Color(r,g,b);
                                    paintIcon(cIcon[i],  cColor[i]);
                                }
                            }
                            jList.repaint();
                        } catch (CoreException ex) {
                            if (ex.getMessage() != null) {
                                JOptionPane.showMessageDialog(thisFrame,ex.getMessage(),
                                        "Error!", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                }
            });
        }
        return jButtonLoad;
    }
}
