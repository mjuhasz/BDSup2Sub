package bdsup2sub.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

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
 * Graphics pane with zoom function - used to display subtitles - part of BDSup2Sub GUI classes.
 *
 * @author 0xdeadbeef
 */
public class GfxPane extends JPanel implements MouseListener {

    private static final long serialVersionUID = 1L;
    /** zoom scale */
    private int scale;
    /** subtitle image to display */
    private BufferedImage image;
    /** color 1 for background gradient */
    private Color color1 = Color.BLUE;
    /** color 2 for background gradient */
    private Color color2 = Color.BLACK;
    /** reference to this panel */
    private GfxPane thisPanel;

    public GfxPane() {
        super();
        scale = 1;
        thisPanel = this;
        addMouseListener(this);
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

    /**
     * draw color gradient and scaled image
     * @param g graphics
     */
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(new GradientPaint(0,0,color1,this.getWidth(),this.getHeight(), color2));
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        if (image != null) {
            if (scale == 1) {
                g.drawImage(image, 0, 0, this);
            } else {
                g2.drawImage(image, 0, 0, image.getWidth()*scale, image.getHeight()*scale ,this);
            }
        }
    }

    /**
     * Set zoom scale
     * @param s zoom scale
     */
    public void setScale(final int s) {
        if (s != scale && image != null) {
            scale = s;
            Dimension dim = new Dimension(scale*image.getWidth(), scale*image.getHeight());
            this.setPreferredSize(dim);
            this.getParent().setSize(dim);
            ((JScrollPane)thisPanel.getParent().getParent()).revalidate();
        }
    }

    /**
     * set image to display
     * @param img image to display
     */
    public void setImage(final BufferedImage img) {
        image = img;
        Dimension dim;
        if (image != null) {
            dim = new Dimension(scale*image.getWidth(), scale*image.getHeight());
        } else {
            dim = new Dimension(1,1);
        }
        this.setPreferredSize(dim);
        this.getParent().setSize(dim);
        ((JScrollPane)thisPanel.getParent().getParent()).revalidate();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int s = scale;
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (s < 8) {
                s++;
            }
        } else {
            if (s > 1) {
                s--;
            }
        }
        if (s != scale) {
            setScale(s);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}
