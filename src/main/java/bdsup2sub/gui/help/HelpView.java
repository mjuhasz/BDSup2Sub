/*
 * Copyright 2012 Miklos Juhasz (mjuhasz)
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
package bdsup2sub.gui.help;

import bdsup2sub.core.Constants;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class HelpView extends JFrame {

    private JPanel jContentPane;
    private JScrollPane jScrollPane;
    private JPopupMenu jPopupMenu;
    private JMenuItem jPopupMenuItemCopy;
    private JMenuItem jPopupMenuItemOpen;
    private JEditorPane jEditorPane;

    private final HelpModel model;


    public HelpView(HelpModel model, Frame mainFrame) {
        super(Constants.APP_NAME_AND_VERSION + " Help");
        this.model = model;

        initialize(mainFrame.getLocation());
    }

    private void initialize(Point location) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setContentPane(getJContentPane());
        setLocation(location.x + 30, location.y + 30);
        initHelpMenu();
    }

    private JPanel getJContentPane() {
        if(jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BoxLayout(jContentPane, BoxLayout.X_AXIS));
            jContentPane.add(getJScrollPane(), null);
        }
        return jContentPane;
    }

    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getjEditorPane());
        }
        return jScrollPane;
    }

    private JEditorPane getjEditorPane() {
        if (jEditorPane == null) {
            try {
                jEditorPane = new JEditorPane(model.getHelpURL());
            } catch (IOException ex) {
                throw new RuntimeException("Unable to access the Help page", ex);
            }
            jEditorPane.setEditable(false);
            getJPopupMenu();
        }
        return jEditorPane;
    }

    void addEditorPaneHyperlinkListener(HyperlinkListener hyperlinkListener) {
        jEditorPane.addHyperlinkListener(hyperlinkListener);
    }

    void addEditorPanePopupListener(MouseListener mouseListener) {
        jEditorPane.addMouseListener(mouseListener);
    }

    void setEditorPage(URL url) {
        try {
            jEditorPane.setPage(url);
        } catch (IOException ex) {
            throw new RuntimeException("Malformed URL for help page", ex);
        }
    }

    String getEditorPaneText() {
        return jEditorPane.getText();
    }

    String getEditorPaneSelectedText() {
        return jEditorPane.getSelectedText();
    }

    boolean isEditorPaneWithTextSelection() {
        return jEditorPane.getSelectionStart() != jEditorPane.getSelectionEnd();
    }

    void setEditorPaneToolTipText(String text) {
        jEditorPane.setToolTipText(text);
    }

    private JPopupMenu getJPopupMenu() {
        if (jPopupMenu == null) {
            jPopupMenu = new JPopupMenu();
            jPopupMenu.add(getJPopupMenuItemOpen());
            jPopupMenu.add(getJPopupMenuItemCopy());
        }
        return jPopupMenu;
    }

    void showPopupMenu(int x, int y) {
        jPopupMenu.show(jEditorPane, x, y);
    }

    private JMenuItem getJPopupMenuItemOpen() {
        if (jPopupMenuItemOpen == null) {
            jPopupMenuItemOpen = new JMenuItem();
            jPopupMenuItemOpen.setText("Open in browser");
            jPopupMenuItemOpen.setActionCommand("open");
        }
        return jPopupMenuItemOpen;
    }

    void addOpenPopupMenuItemActionListener(ActionListener actionListener) {
        jPopupMenuItemOpen.addActionListener(actionListener);
    }

    private JMenuItem getJPopupMenuItemCopy() {
        if (jPopupMenuItemCopy == null) {
            jPopupMenuItemCopy = new JMenuItem();
            jPopupMenuItemCopy.setText("Copy");
            jPopupMenuItemCopy.setActionCommand("copy");
        }
        return jPopupMenuItemCopy;
    }

    void addCopyPopupMenuItemActionListener(ActionListener actionListener) {
        jPopupMenuItemCopy.addActionListener(actionListener);
    }

    void enableCopyPopupMenuItem(boolean enable) {
        jPopupMenuItemCopy.setEnabled(enable);
    }

    private void initHelpMenu() {
        jPopupMenu.addSeparator();

        String s;
        int i = 0;
        while ((s = model.getChapters().get("chapter_" + i, "")).length() > 0) {
            String menuItemTexts[] = s.split(",");
            if (menuItemTexts.length == 2) {
                JMenuItem menuItem = new JMenuItem();
                menuItem.setText(menuItemTexts[1]);
                String chapterUrl = model.getHelpURL().toExternalForm() + "#" + menuItemTexts[0];
                menuItem.setActionCommand(chapterUrl);
                jPopupMenu.add(menuItem);
            }
            i++;
        }
    }
    
    void addHelpMenuItemActionListener(ActionListener actionListener) {
        MenuElement[] subElements = jPopupMenu.getSubElements();
        for(int i=0; i < subElements.length; i++) {
            ((JMenuItem) subElements[i]).addActionListener(actionListener);
        }
    }
}
