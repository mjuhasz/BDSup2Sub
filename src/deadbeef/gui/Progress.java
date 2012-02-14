package deadbeef.gui;


import deadbeef.core.Core;
import deadbeef.core.CoreThreadState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;


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
 * Progress bar dialog - part of BDSup2Sub GUI classes.
 *
 * @author 0xdeadbeef
 */
public class Progress extends JDialog {

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane;

    private JButton jButtonCancel;

    private JProgressBar jProgressBar;

    private JLabel jLabelProgress;

    /** timer used to dispose when thread was canceled */
    private Timer timer;

    /**
     * Constructor
     * @param owner parent window
     * @param modal show as modal dialog
     */
    public Progress(Frame owner, boolean modal) {
        super(owner, modal);
        initialize();

        Point p = owner.getLocation();
        this.setLocation(p.x+owner.getWidth()/2-getWidth()/2, p.y+owner.getHeight()/2-getHeight()/2);
        this.setResizable(false);
        jProgressBar.setMaximum(100);
        jProgressBar.setMinimum(0);
        jProgressBar.setValue(0);
    }

    /* (non-Javadoc)
     * @see java.awt.Dialog#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean b) {
        timer = new Timer();
        timer.schedule( new progressTimer(), 200, 200 );
        super.setVisible(b);
    }

    /**
     * Set text to display in dialog
     * @param s text to display
     */
    public void setText(String s) {
        jLabelProgress.setText(s);
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setMinimumSize(new Dimension(224, 139));
        this.setResizable(false);
        this.setBounds(new Rectangle(0, 0, 224, 139));
        this.setMaximumSize(new Dimension(224, 139));
        this.setContentPane(getJContentPane());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Core.cancel();
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
            GridBagConstraints gridBagBtnCancel = new GridBagConstraints();
            gridBagBtnCancel.insets = new Insets(8, 8, 8, 8);
            gridBagBtnCancel.gridx = 0;
            gridBagBtnCancel.gridy = 2;
            GridBagConstraints gridBagProgressBar = new GridBagConstraints();
            gridBagProgressBar.gridy = 1;
            gridBagProgressBar.fill = GridBagConstraints.VERTICAL;
            gridBagProgressBar.insets = new Insets(8, 8, 8, 8);
            gridBagProgressBar.gridx = 0;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints.insets = new Insets(8, 8, 8, 8);
            gridBagConstraints.gridy = 0;
            jLabelProgress = new JLabel();
            jLabelProgress.setText("Exporting SUB/IDX");
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(jLabelProgress, gridBagConstraints);
            jContentPane.add(getJProgressBar(), gridBagProgressBar);
            jContentPane.add(getJButtonCancel(), gridBagBtnCancel);
        }
        return jContentPane;
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
            jButtonCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Core.cancel();
                }
            });
        }
        return jButtonCancel;
    }

    /**
     * This method initializes jProgressBar
     *
     * @return javax.swing.JProgressBar
     */
    private JProgressBar getJProgressBar() {
        if (jProgressBar == null) {
            jProgressBar = new JProgressBar();
            jProgressBar.setPreferredSize(new Dimension(200, 20));
            jProgressBar.setMinimumSize(new Dimension(200, 20));
            jProgressBar.setStringPainted(true);
        }
        return jProgressBar;
    }

    /**
     * Set minimum and maximum value for progress bar
     * @param min minimum value for progress bar
     * @param max maximum value for progress bar
     */
    public void setMinMax(int min, int max) {
        jProgressBar.setMinimum(min);
        jProgressBar.setMaximum(max);
    }

    /**
     * Set value for progress bar
     * @param val value for progress bar
     */
    public void setProgress(final int val) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() { public void run() { jProgressBar.setValue( val ); jProgressBar.repaint();} } );
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Timer used to automatically dispose this dialog once the Core thread is no longer active
     * @author 0xdeadbeef
     */
    class progressTimer extends TimerTask {
        @Override
        public void run() {
            if (Core.getStatus() != CoreThreadState.ACTIVE) {
                timer.cancel();
                dispose();
            }
        }
    }
}
