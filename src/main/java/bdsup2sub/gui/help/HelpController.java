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

import bdsup2sub.utils.ToolBox;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class HelpController {

    private final HelpModel model;
    private final HelpView view;

    public HelpController(HelpModel model, HelpView view) {
        this.model = model;
        this.view = view;

        view.addEditorPaneHyperlinkListener(new EditorPaneHyperlinkListener());
        view.addEditorPanePopupListener(new EditorPanePopupListener());
        view.addHelpMenuItemActionListener(new HelpAnchorListener());

        view.addOpenPopupMenuItemActionListener(new OpenPopupMenuItemActionListener());
        view.addCopyPopupMenuItemActionListener(new CopyPopupMenuItemActionListener());
    }

    private class EditorPaneHyperlinkListener implements HyperlinkListener {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent event) {
            URL url = event.getURL();
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    if (url.sameFile(model.getHelpURL())) {
                        view.setEditorPage(url);
                    } else {
                        Desktop.getDesktop().browse(url.toURI());
                    }
                } catch (IOException ex) {
                } catch (URISyntaxException ex) {
                }
            } else if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                if (url.sameFile(model.getHelpURL())) {
                    view.setEditorPaneToolTipText(url.getRef());
                } else {
                    view.setEditorPaneToolTipText(url.toExternalForm());
                }
            } else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
                view.setEditorPaneToolTipText(null);
            }
        }
    }

    private class OpenPopupMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            BufferedWriter writer = null;
            try {
                String s = view.getEditorPaneText();
                File tempFile = File.createTempFile("bds2s_help_",".htm");
                writer = new BufferedWriter(new FileWriter(tempFile));
                writer.write(s);
                writer.close();
                Desktop.getDesktop().browse(tempFile.toURI());
                tempFile.deleteOnExit();
            } catch (IOException ex) {
                ToolBox.showException(ex);
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException ex) {
                    ToolBox.showException(ex);
                }
            }
        }
    }

    private class CopyPopupMenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String selectedText = view.getEditorPaneSelectedText();
            if (selectedText != null) {
                StringSelection selection = new StringSelection(selectedText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            }
        }
    }

    private class EditorPanePopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent event) {
            maybeShowPopup(event);
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            maybeShowPopup(event);
        }

        private void maybeShowPopup(MouseEvent event) {
            if (event.isPopupTrigger()) {
                view.enableCopyPopupMenuItem(view.isEditorPaneWithTextSelection());
                view.showPopupMenu(event.getX(), event.getY());
            }
        }
    }

    private class HelpAnchorListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (!event.getActionCommand().equals("open") && !event.getActionCommand().equals("copy")) {
                try {
                    String chapterUrl = event.getActionCommand();
                    view.setEditorPage(new URL(chapterUrl));
                } catch (IOException ex) {
                    throw new RuntimeException("Malformed URL in help file", ex);
                }
            }
        }
    }
}
