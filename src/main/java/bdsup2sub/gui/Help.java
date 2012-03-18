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

import bdsup2sub.core.Constants;
import bdsup2sub.tools.Props;
import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class Help extends JFrame {

    private JPanel jContentPane;
    private JScrollPane jScrollPane;
    private JPopupMenu jPopupMenu;
    private JMenuItem jPopupMenuItemCopy;
    private JMenuItem jPopupMenuItemOpen;
    private JEditorPane thisEditor;

    private URL helpURL;
    private Props chapters;


    public Help() {
        super(Constants.APP_NAME_AND_VERSION + " Help");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(600,600);
        setContentPane(getJContentPane());

        init();
    }

    /**
     * init function. loads html page.
     */
    private void init() {
        ClassLoader loader = Help.class.getClassLoader();
        helpURL = loader.getResource("help.htm");
        chapters = new Props();
        chapters.load(loader.getResource("help.ini"));

        try {
            thisEditor = new JEditorPane(helpURL);
            thisEditor.setEditable( false );
            // needed to open browser via clicking on a link
            thisEditor.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    URL url = e.getURL();
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            if (url.sameFile(helpURL)) {
                                thisEditor.setPage(url);
                            } else {
                                Desktop.getDesktop().browse(url.toURI());
                            }
                        } catch (IOException ex) {
                        } catch (URISyntaxException ex) {
                        }
                    } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                        if (url.sameFile(helpURL)) {
                            thisEditor.setToolTipText(url.getRef());
                        } else {
                            thisEditor.setToolTipText(url.toExternalForm());
                        }
                    } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                        thisEditor.setToolTipText(null);
                    }
                }
            });
            jScrollPane.setViewportView(thisEditor);
            // popup menu
            getJPopupMenu();

            jPopupMenu.addSeparator();
            String s;
            int i = 0;
            while ((s = chapters.get("chapter_"+i, "")).length() > 0) {
                String str[] = s.split(",");
                if (str.length == 2) {
                    JMenuItem j = new JMenuItem();
                    j.setText(str[1]);
                    jPopupMenu.add(j);
                    helpAnchorListener h = new helpAnchorListener();
                    h.setEditor(thisEditor);
                    h.setChapterUrl(new URL(helpURL.toExternalForm() + "#" + str[0]));
                    j.addActionListener(h);
                }
                i++;
            }

            MouseListener popupListener = new PopupListener();
            thisEditor.addMouseListener(popupListener);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void setClipboard(String str) {
        StringSelection ss = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }

    private JPopupMenu getJPopupMenu() {
        if (jPopupMenu == null) {
            jPopupMenu = new JPopupMenu();
            jPopupMenu.add(getJPopupMenuItemOpen());
            jPopupMenu.add(getJPopupMenuItemCopy());
        }
        return jPopupMenu;
    }

    private JMenuItem getJPopupMenuItemOpen() {
        if (jPopupMenuItemOpen == null) {
            jPopupMenuItemOpen = new JMenuItem();
            jPopupMenuItemOpen.setText("Open in browser");  // Generated
            jPopupMenuItemOpen.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    BufferedWriter fw = null;
                    try {
                        String s = thisEditor.getText();
                        File temp = File.createTempFile("bds2s_help_",".htm");
                        fw = new BufferedWriter(new FileWriter(temp));
                        fw.write(s);
                        fw.close();
                        Desktop.getDesktop().browse(temp.toURI());
                        temp.deleteOnExit();
                    } catch (IOException ex) {
                        ToolBox.showException(ex);
                    } finally {
                        try {
                            if (fw != null) {
                                fw.close();
                            }
                        } catch (IOException ex) {
                            ToolBox.showException(ex);
                        }
                    }
                }

            });
        }
        return jPopupMenuItemOpen;
    }

    private JMenuItem getJPopupMenuItemCopy() {
        if (jPopupMenuItemCopy == null) {
            jPopupMenuItemCopy = new JMenuItem();
            jPopupMenuItemCopy.setText("Copy");  // Generated
            jPopupMenuItemCopy.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String s = thisEditor.getSelectedText();
                    if (s != null) {
                        setClipboard(s);
                    }
                }
            });
        }
        return jPopupMenuItemCopy;
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
        }
        return jScrollPane;
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
                jPopupMenuItemCopy.setEnabled(thisEditor.getSelectionStart()!=thisEditor.getSelectionEnd());
                jPopupMenu.show(thisEditor,e.getX(), e.getY());
            }
        }
    }
}


/**
 * Listener to implement the popup chapter selection
 */
class helpAnchorListener implements ActionListener {	
    private URL chapterUrl;
    private JEditorPane editor;

    public void setChapterUrl(URL url) {
        chapterUrl = url;
    }

    public void setEditor(JEditorPane editor) {
        this.editor = editor;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            editor.setPage(chapterUrl);
        } catch (IOException ex) {
        }
    }
}
