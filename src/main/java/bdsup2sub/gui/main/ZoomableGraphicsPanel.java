/*
 * Copyright 2014 Volker Oth (0xdeadbeef) / Miklos Juhasz (mjuhasz)
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
package bdsup2sub.gui.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

class ZoomableGraphicsPanel extends JPanel {

    private int zoomScale = 1;
    private BufferedImage image;
    private final Color color1 = Color.BLUE;
    private final Color color2 = Color.BLACK;

    public ZoomableGraphicsPanel() {
        addMouseListener(new MouseListener());
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        draw(g);
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
        draw(g);
    }

    private void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(new GradientPaint(0, 0, color1, getWidth(),getHeight(), color2));
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (image != null) {
            if (zoomScale == 1) {
                g.drawImage(image, 0, 0, this);
            } else {
                g2.drawImage(image, 0, 0, image.getWidth() * zoomScale, image.getHeight() * zoomScale, this);
            }
        }
    }

    private void setZoomScale(int s) {
        if (s != zoomScale && image != null) {
            zoomScale = s;
            Dimension dim = new Dimension(zoomScale * image.getWidth(), zoomScale * image.getHeight());
            setPreferredSize(dim);
            getParent().setSize(dim);
            getParent().getParent().revalidate();
        }
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        Dimension dim;
        if (image != null) {
            dim = new Dimension(zoomScale * image.getWidth(), zoomScale * image.getHeight());
        } else {
            dim = new Dimension(1, 1);
        }
        setPreferredSize(dim);
        getParent().setSize(dim);
        getParent().getParent().revalidate();
    }

    private class MouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            int s = zoomScale;
            if (event.getButton() == MouseEvent.BUTTON1) {
                if (s < 8) {
                    s++;
                }
            } else {
                if (s > 1) {
                    s--;
                }
            }
            if (s != zoomScale) {
                setZoomScale(s);
            }
        }
    }
}
