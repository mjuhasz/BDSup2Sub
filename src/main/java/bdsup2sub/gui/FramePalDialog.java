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

import bdsup2sub.bitmap.Palette;
import bdsup2sub.core.Core;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import static bdsup2sub.gui.support.GuiUtils.centerRelativeToOwner;

public class FramePalDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /** array containing the 16 numeric values 00 - 15 as strings */
    private static final String[] COLOR_NAME = { "00", "01", "02", "03", "04", "05", "06" ,"07", "08", "09", "10", "11", "12", "13", "14", "15"};

    private JPanel jContentPane;

    private JComboBox jComboBoxColor1;
    private JComboBox jComboBoxColor2;
    private JComboBox jComboBoxColor3;
    private JComboBox jComboBoxColor4;

    private JComboBox jComboBoxAlpha1;
    private JComboBox jComboBoxAlpha2;
    private JComboBox jComboBoxAlpha3;
    private JComboBox jComboBoxAlpha4;

    private JButton jButtonOk;
    private JButton jButtonCancel;
    private JButton jButtonSetAll;
    private JButton jButtonResetAll;
    private JButton jButtonReset;

    private int currentSubtitleIndex;
    private ImageIcon colorPreviewIcon[];
    private volatile boolean isReady; // semaphore to disable actions while changing component properties
    private int alpha[];
    private int palette[];


    public FramePalDialog(JFrame frame) {
        super(frame, "Edit Frame Palette", true);

        setSize(294, 209);
        setContentPane(getJContentPane());
        centerRelativeToOwner(this);
        setResizable(false);

        Palette palette = Core.getCurSrcDVDPalette();
        colorPreviewIcon = new ImageIcon[16];
        for (int i=0; i < palette.getSize(); i++) {
            colorPreviewIcon[i] = new ImageIcon(new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB));
            Color c = new Color (palette.getARGB(i) | 0xff000000); // make fully opaque
            paintIcon(colorPreviewIcon[i], c);
            jComboBoxColor1.addItem(COLOR_NAME[i]);
            jComboBoxColor2.addItem(COLOR_NAME[i]);
            jComboBoxColor3.addItem(COLOR_NAME[i]);
            jComboBoxColor4.addItem(COLOR_NAME[i]);
            jComboBoxAlpha1.addItem(COLOR_NAME[i]);
            jComboBoxAlpha2.addItem(COLOR_NAME[i]);
            jComboBoxAlpha3.addItem(COLOR_NAME[i]);
            jComboBoxAlpha4.addItem(COLOR_NAME[i]);
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

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            JLabel jLabelAlpha1 = new JLabel();
            jLabelAlpha1.setBounds(new Rectangle(155, 10, 41, 16));
            jLabelAlpha1.setText("Alpha 1");
            JLabel jLabelAlpha2 = new JLabel();
            jLabelAlpha2.setBounds(new Rectangle(155, 35, 36, 16));
            jLabelAlpha2.setText("Alpha 2");
            JLabel jLabelAlpha3 = new JLabel();
            jLabelAlpha3.setBounds(new Rectangle(155, 60, 36, 16));
            jLabelAlpha3.setText("Alpha 3");
            JLabel jLabelAlpha4 = new JLabel();
            jLabelAlpha4.setBounds(new Rectangle(155, 85, 36, 16));
            jLabelAlpha4.setText("Alpha 4");
            JLabel jLabelColor1 = new JLabel();
            jLabelColor1.setBounds(new Rectangle(15, 10, 46, 16));
            jLabelColor1.setText("Color 1");
            JLabel jLabelColor2 = new JLabel();
            jLabelColor2.setBounds(new Rectangle(15, 35, 46, 16));
            jLabelColor2.setText("Color 2");
            JLabel jLabelColor3 = new JLabel();
            jLabelColor3.setBounds(new Rectangle(15, 60, 46, 16));
            jLabelColor3.setText("Color 3");
            JLabel jLabelColor4 = new JLabel();
            jLabelColor4.setBounds(new Rectangle(15, 85, 45, 16));
            jLabelColor4.setText("Color 4");

            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(getJButtonOk(), null);
            jContentPane.add(getJButtonCancel(), null);
            jContentPane.add(jLabelColor1, null);
            jContentPane.add(jLabelColor2, null);
            jContentPane.add(jLabelColor3, null);
            jContentPane.add(jLabelColor4, null);
            jContentPane.add(jLabelAlpha1, null);
            jContentPane.add(jLabelAlpha2, null);
            jContentPane.add(jLabelAlpha3, null);
            jContentPane.add(jLabelAlpha4, null);
            jContentPane.add(getJComboBoxColor1(), null);
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
     * Modified ListCellRenderer to display text and color icons
     */
    private class MyListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,  boolean cellHasFocus) {
            Component retValue = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            int idx = ToolBox.getInt(value.toString());
            if (idx >= 0) {
                setText(COLOR_NAME[idx]);
                setIcon(colorPreviewIcon[idx]);
            }
            return retValue;
        }
    }

    private void paintIcon(ImageIcon icon, Color color) {
        Graphics graphics = icon.getImage().getGraphics();
        graphics.setColor(color);
        graphics.setPaintMode();
        graphics.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
    }

    private JComboBox getJComboBoxColor1() {
        if (jComboBoxColor1 == null) {
            jComboBoxColor1 = new JComboBox();
            jComboBoxColor1.setBounds(new Rectangle(70, 10, 61, 16));
            jComboBoxColor1.setEditable(false);
            jComboBoxColor1.setToolTipText("Set palette index of frame color 1");
            jComboBoxColor1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        int idx = ToolBox.getInt(jComboBoxColor1.getSelectedItem().toString());
                        if (idx >= 0 && idx < 16) {
                            palette[0] = idx;
                        }
                    }
                }
            });
        }
        return jComboBoxColor1;
    }

    private JComboBox getJComboBoxColor2() {
        if (jComboBoxColor2 == null) {
            jComboBoxColor2 = new JComboBox();
            jComboBoxColor2.setBounds(new Rectangle(70, 35, 61, 16));
            jComboBoxColor2.setEditable(false);
            jComboBoxColor2.setToolTipText("Set palette index of frame color 2");
            jComboBoxColor2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        int idx = ToolBox.getInt(jComboBoxColor2.getSelectedItem().toString());
                        if (idx >= 0 && idx < 16) {
                            palette[1] = idx;
                        }
                    }
                }
            });
        }
        return jComboBoxColor2;
    }

    private JComboBox getJComboBoxColor3() {
        if (jComboBoxColor3 == null) {
            jComboBoxColor3 = new JComboBox();
            jComboBoxColor3.setBounds(new Rectangle(70, 60, 61, 16));
            jComboBoxColor3.setEditable(false);
            jComboBoxColor3.setToolTipText("Set palette index of frame color 3");
            jComboBoxColor3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        int idx = ToolBox.getInt(jComboBoxColor3.getSelectedItem().toString());
                        if (idx >= 0 && idx < 16) {
                            palette[2] = idx;
                        }
                    }
                }
            });
        }
        return jComboBoxColor3;
    }

    private JComboBox getJComboBoxColor4() {
        if (jComboBoxColor4 == null) {
            jComboBoxColor4 = new JComboBox();
            jComboBoxColor4.setBounds(new Rectangle(70, 85, 61, 16));
            jComboBoxColor4.setEditable(false);
            jComboBoxColor4.setToolTipText("Set palette index of frame color 4");
            jComboBoxColor4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        int idx = ToolBox.getInt(jComboBoxColor4.getSelectedItem().toString());
                        if (idx >= 0 && idx < 16) {
                            palette[3] = idx;
                        }
                    }
                }
            });
        }
        return jComboBoxColor4;
    }

    private JComboBox getJComboBoxAlpha1() {
        if (jComboBoxAlpha1 == null) {
            jComboBoxAlpha1 = new JComboBox();
            jComboBoxAlpha1.setBounds(new Rectangle(215, 10, 56, 16));
            jComboBoxAlpha1.setEditable(false);
            jComboBoxAlpha1.setToolTipText("Set alpha value of frame color 1");
            jComboBoxAlpha1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        int idx = ToolBox.getInt(jComboBoxAlpha1.getSelectedItem().toString());
                        if (idx >= 0 && idx < 16) {
                            alpha[0] = idx;
                        }
                    }
                }
            });
        }
        return jComboBoxAlpha1;
    }

    private JComboBox getJComboBoxAlpha2() {
        if (jComboBoxAlpha2 == null) {
            jComboBoxAlpha2 = new JComboBox();
            jComboBoxAlpha2.setBounds(new Rectangle(215, 35, 56, 16));
            jComboBoxAlpha2.setEditable(false);
            jComboBoxAlpha2.setToolTipText("Set alpha value of frame color 2");
            jComboBoxAlpha2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        int idx = ToolBox.getInt(jComboBoxAlpha2.getSelectedItem().toString());
                        if (idx >= 0 && idx < 16) {
                            alpha[1] = idx;
                        }
                    }
                }
            });
        }
        return jComboBoxAlpha2;
    }

    private JComboBox getJComboBoxAlpha3() {
        if (jComboBoxAlpha3 == null) {
            jComboBoxAlpha3 = new JComboBox();
            jComboBoxAlpha3.setBounds(new Rectangle(215, 60, 56, 16));
            jComboBoxAlpha3.setEditable(false);
            jComboBoxAlpha3.setToolTipText("Set alpha value of frame color 3");
            jComboBoxAlpha3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        int idx = ToolBox.getInt(jComboBoxAlpha3.getSelectedItem().toString());
                        if (idx >= 0 && idx < 16) {
                            alpha[2] = idx;
                        }
                    }
                }
            });
        }
        return jComboBoxAlpha3;
    }

    private JComboBox getJComboBoxAlpha4() {
        if (jComboBoxAlpha4 == null) {
            jComboBoxAlpha4 = new JComboBox();
            jComboBoxAlpha4.setBounds(new Rectangle(215, 85, 56, 16));
            jComboBoxAlpha4.setEditable(false);
            jComboBoxAlpha4.setToolTipText("Set alpha value of frame color 4");
            jComboBoxAlpha4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isReady) {
                        int idx = ToolBox.getInt(jComboBoxAlpha4.getSelectedItem().toString());
                        if (idx >= 0 && idx < 16) {
                            alpha[3] = idx;
                        }
                    }
                }
            });
        }
        return jComboBoxAlpha4;
    }

    private JButton getJButtonOk() {
        if (jButtonOk == null) {
            jButtonOk = new JButton();
            jButtonOk.setName("jButtonOk");
            jButtonOk.setBounds(new Rectangle(200, 145, 75, 21));
            jButtonOk.setText("Ok");
            jButtonOk.setToolTipText("Use current settings and return");
            jButtonOk.setMnemonic('o');
            jButtonOk.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int a[] = Core.getFrameAlpha(currentSubtitleIndex);
                    int p[] = Core.getFramePal(currentSubtitleIndex);
                    for (int i= 0; i<4; i++) {
                        if (a != null) {
                            a[i] = alpha[i];
                        }
                        if (p != null) {
                            p[i] = palette[i];
                        }
                    }
                    dispose();
                }
            });
        }
        return jButtonOk;
    }

    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setName("jButtonCancel");
            jButtonCancel.setBounds(new Rectangle(10, 145, 75, 21));
            jButtonCancel.setText("Cancel");
            jButtonCancel.setToolTipText("Lose changes and return");
            jButtonCancel.setMnemonic('c');
            jButtonCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        }
        return jButtonCancel;
    }

    private JButton getJButtonSetAll() {
        if (jButtonSetAll == null) {
            jButtonSetAll = new JButton();
            jButtonSetAll.setBounds(new Rectangle(200, 115, 75, 21));
            jButtonSetAll.setText("Set All");
            jButtonSetAll.setToolTipText("Apply these settings for whole stream and return");
            jButtonSetAll.setMnemonic('s');
            jButtonSetAll.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int j=0; j < Core.getNumFrames(); j++) {
                        int a[] = Core.getFrameAlpha(j);
                        int p[] = Core.getFramePal(j);
                        for (int i= 0; i<4; i++) {
                            if (a != null) {
                                a[i] = alpha[i];
                            }
                            if (p != null) {
                                p[i] = palette[i];
                            }
                        }
                    }
                    dispose();
                }
            });
        }
        return jButtonSetAll;
    }

    private JButton getJButtonResetAll() {
        if (jButtonResetAll == null) {
            jButtonResetAll = new JButton();
            jButtonResetAll.setBounds(new Rectangle(105, 115, 75, 23));
            jButtonResetAll.setText("Reset All");
            jButtonResetAll.setToolTipText("Revert to original frame palettes for whole stream and return");
            jButtonResetAll.setMnemonic('a');
            jButtonResetAll.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int j=0; j < Core.getNumFrames(); j++) {
                        int ao[] = Core.getOriginalFrameAlpha(j);
                        int po[] = Core.getOriginalFramePal(j);
                        int a[] = Core.getFrameAlpha(j);
                        int p[] = Core.getFramePal(j);
                        for (int i= 0; i<4; i++) {
                            if (a != null && ao != null) {
                                a[i] = ao[i];
                            }
                            if (p != null && po != null) {
                                p[i] = po[i];
                            }
                        }
                    }
                    dispose();
                }
            });
        }
        return jButtonResetAll;
    }

    private JButton getJButtonReset() {
        if (jButtonReset == null) {
            jButtonReset = new JButton();
            jButtonReset.setBounds(new Rectangle(105, 145, 75, 23));
            jButtonReset.setText("Reset");
            jButtonReset.setToolTipText("Revert to original frame palette");
            jButtonReset.setMnemonic('r');
            jButtonReset.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int ao[] = Core.getOriginalFrameAlpha(currentSubtitleIndex);
                    int po[] = Core.getOriginalFramePal(currentSubtitleIndex);
                    int a[] = Core.getFrameAlpha(currentSubtitleIndex);
                    int p[] = Core.getFramePal(currentSubtitleIndex);
                    for (int i= 0; i<4; i++) {
                        if (a != null && ao != null) {
                            a[i] = ao[i];
                        }
                        if (p != null && po != null) {
                            p[i] = po[i];
                        }
                    }
                    setCurrentSubtitleIndex(currentSubtitleIndex);
                }
            });
        }
        return jButtonReset;
    }

    public void setCurrentSubtitleIndex(int idx) {
        currentSubtitleIndex = idx;
        isReady = false;

        // we need a deep copy here to allow editing
        alpha = new int[4];
        palette = new int[4];
        int a[] = Core.getFrameAlpha(currentSubtitleIndex);
        int p[] = Core.getFramePal(currentSubtitleIndex);
        for (int i=0; i<4; i++) {
            if (a != null) {
                alpha[i] = a[i];
            }
            if (p != null) {
                palette[i]   = p[i];
            }
        }

        jComboBoxAlpha1.setSelectedIndex(alpha[0]);
        jComboBoxAlpha2.setSelectedIndex(alpha[1]);
        jComboBoxAlpha3.setSelectedIndex(alpha[2]);
        jComboBoxAlpha4.setSelectedIndex(alpha[3]);

        jComboBoxColor1.setSelectedIndex(palette[0]);
        jComboBoxColor2.setSelectedIndex(palette[1]);
        jComboBoxColor3.setSelectedIndex(palette[2]);
        jComboBoxColor4.setSelectedIndex(palette[3]);

        isReady = true;
    }
}
