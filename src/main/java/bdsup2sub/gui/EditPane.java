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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * Preview pane for edit dialog.
 * Shows color gradient, cinemascope bars and scaled down subtitle image.
 */
public class EditPane extends JPanel implements MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;

    /** image of subtitle to display */
    private BufferedImage image;
    /** color 1 for color gradient */
    private Color color1 = Color.BLUE;
    /** color 2 for color gradient */
    private Color color2 = Color.BLACK;
    /** width of original screen */
    private int width;
    /** height of original screen */
    private int height;
    /** x offset of subtitle in original screen */
    private int ofsX;
    /** y offset of subtitle in original screen */
    private int ofsY;
    /** subtitle width in original screen */
    private int imgWidth;
    /** subtitle height in original screen */
    private int imgHeight;
    /** aspect ratio of the inner frame (e.g. 21/9=2.333 for cinemascope */
    private static double aspectRatioIn = 21.0/9;
    /** factor to calculate height of one cinemascope bar from screen height */
    private static double cineBarFactor = 5.0/42;
    /** aspect ratio of the screen */
    private static final double ASPECT_RATIO = 16.0/9;
    /** Y coordinate crop offset */
    private int cropOfsY = 0;
    /** minimum distance to left and right in pixels */
    private static final int INSET = 2;
    /** is this EditPane a LayoutPane ? */
    private boolean layoutPane;
    /** upper left x coordinate of selection rectangle */
    private int selectStartX;
    /** lower right x coordinate of selection rectangle */
    private int selectEndX;
    /** upper left y coordinate of selection rectangle */
    private int selectStartY;
    /** lower right y coordinate of selection rectangle */
    private int selectEndY;
    /** allow selection in this pane */
    private boolean allowSelection;
    /** selection valid */
    private boolean validSelection;
    /** left button currently pressed */
    private boolean leftButtonPressed;
    /** caption excluded from export */
    private boolean excluded;
    /** selectionListener */
    private SelectListener selectListener;
    /** x scale of caption */
    private double xScaleCaption;
    /** y scale of caption */
    private double yScaleCaption;
    /** y offset after cropping */
    private int yCrop;

    public EditPane(boolean isLayoutPane) {
        super();

        layoutPane = isLayoutPane;
        width = 1920;
        height = 1080;
        imgWidth = 0;
        imgHeight = 0;
        image = null;

        selectStartX = -1;
        addMouseListener(this);
        addMouseMotionListener(this);

        allowSelection = false;
    }

    public EditPane() {
        this(false);
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
     * create color gradient, cinemascope bars and draw scaled down subtitle image
     * @param g
     */
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        /* NOTE!
         * for fillRect(x,y,w,h), the lower right pixel is at (x+w-1,y+h-1) (as you would assume)
         * for drawRect(x,y,w,h), the lower right pixel is at (x+w,y+h) !!!
         */

        int w = this.getWidth();
        int h = this.getHeight();

        int wl, hl, xl, yl;

        if (layoutPane) {
            g2.setColor(UIManager.getColor("Panel.background"));
            g2.fillRect(0, 0, w, h);
            // paint outer frame (16:9)
            wl = w-2*INSET;
            hl = (wl*9+8)/16;
            if (hl > h) {
                hl = h-INSET;
                wl = (hl*32+8)/18;
            }
            yl = (h-hl+1)/2;
            xl = (w-wl+1)/2;
        } else {
            wl = w;
            hl = h;
            xl = 0;
            yl = 0;
        }

        int cineH = (int)(hl * cineBarFactor + 0.5); // height of one cinemascope bar in pixels
        // paint color gradient
        g2.setPaint(new GradientPaint(xl, yl, color1, wl, hl, color2));
        g2.fillRect(xl, yl+cineH, wl, hl-cineH);
        // paint cinemascope bars
        g2.setPaint(Color.BLACK);
        g2.fillRect(xl,yl,wl, cineH);
        g2.fillRect(xl,yl+hl-cineH, wl, cineH);

        yCrop = ofsY;
        if (yCrop < cropOfsY) {
            yCrop = cropOfsY;
        } else {
            int yMax = height - imgHeight - cropOfsY;
            if (yCrop > yMax) {
                yCrop = yMax;
            }
        }

        double sx;
        double sy;


        // draw scaled down subtitle image
        sx = (double)wl / width;
        sy = (double)hl / height;
        xScaleCaption = sx;
        yScaleCaption = sy;
        if (imgWidth > 0 && image != null) {
            // inner frame
            int wi = (int)(imgWidth*sx+0.5);
            int hi = (int)(imgHeight*sy+0.5);
            int xi = xl + (int)(ofsX*sx+0.5);
            int yi = yl + (int)(yCrop*sy+0.5);

            g2.setColor(Color.GREEN);
            g2.drawRect(xi, yi, wi-1, hi-1);

            g2.drawImage(image, xi, yi, wi, hi ,this);

            if (validSelection && !leftButtonPressed) {
                if (selectStartX >= imgWidth+ofsX || selectEndX <= ofsX || selectStartY >= imgHeight+yCrop || selectEndY < yCrop) {
                    validSelection = false;
                } else {
                    if (selectStartX < ofsX) {
                        selectStartX = ofsX;
                    }
                    if (selectEndX >= imgWidth+ofsX) {
                        selectEndX = imgWidth+ofsX-1;
                    }
                    if (selectStartY < yCrop) {
                        selectStartY = yCrop;
                    }
                    if (selectEndY >= imgHeight+yCrop) {
                        selectEndY = imgHeight+yCrop-1;
                    }
                }
            }
        }

        // draw lines representing crop offset
        if (cropOfsY > 0) {
            g2.setPaint(Color.RED);
            int y = yl + (int)(cropOfsY*sy+0.5);
            g2.drawLine(xl, y, wl-1, y);
            y = yl + hl - (int)(cropOfsY*sy+0.5) - 1;
            g2.drawLine(xl, y, wl-1, y);
        }

        // draw selection window
        if (validSelection) {
            g2.setPaint(Color.YELLOW);
            g2.drawRect((int)(selectStartX*sx+0.5), (int)(selectStartY*sy+0.5),
                    (int)((selectEndX-selectStartX)*sx+0.5), (int)((selectEndY-selectStartY)*sy+0.5));
        }

        if (excluded) {
            g2.setPaint(Color.RED);
            g2.drawLine(xl, yl, xl+wl-1, yl+hl-1);
            g2.drawLine(xl+wl, yl, xl-1, yl+hl-1);
        }

    }

    /**
     * set subtitle image
     * @param img subtitle image
     * @param w width of subtitle in original screen
     * @param h height of subtitle in original screen
     */
    public void setImage(BufferedImage img, int w, int h) {
        image = img;
        imgWidth = w;
        imgHeight = h;
    }

    /**
     * set width and height of original screen
     * @param w width of original screen
     * @param h height of original screen
     */
    public void setDim(int w, int h) {
        width = w;
        height = h;
    }

    /**
     * set subtitle offset in original screen
     * @param x x offset in original screen
     * @param y y offset in original screen
     */
    public void setOffsets(int x, int y) {
        ofsX = x;
        if (y < cropOfsY) {
            ofsY = cropOfsY;
        } else {
            ofsY = y;
        }
    }

    /**
     * set aspect ratio of inner frame (display area)
     * @param sr screen ration (e.g. 21.0/9)
     */
    public void setAspectRatio(double sr) {
        aspectRatioIn = sr;
        cineBarFactor = (1.0 - ASPECT_RATIO/aspectRatioIn)/2.0;
    }

    /**
     * Set Y coordinate cropping offset
     * @param ofs
     */
    public void setCropOfsY(int ofs) {
        cropOfsY = ofs;
    }

    /**
     * Enable/disable selection via mouse dragging
     * @param e true: allow selection via mouse dragging
     */
    public void setAllowSelection(boolean e) {
        allowSelection = e;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (allowSelection && e.getButton() == MouseEvent.BUTTON1) {
            selectStartX = (int)(e.getX()/xScaleCaption+0.5);
            selectStartY = (int)(e.getY()/yScaleCaption+0.5);
            leftButtonPressed = true;
            validSelection = false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (allowSelection && e.getButton() == MouseEvent.BUTTON1) {
            selectEndX = (int)(e.getX()/xScaleCaption+0.5);
            selectEndY = (int)(e.getY()/yScaleCaption+0.5);
            leftButtonPressed = false;
            if (selectStartX >= 0 && selectEndX>selectStartX && selectEndY>selectStartY) {
                validSelection = true;
            }
            repaint();
            selectListener.selectionPerformed(validSelection);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (leftButtonPressed) {
            selectEndX = (int)(e.getX()/xScaleCaption+0.5);
            selectEndY = (int)(e.getY()/yScaleCaption+0.5);
            if (selectStartX >= 0 && selectEndX>selectStartX && selectEndY>selectStartY) {
                validSelection = true;
            }
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * Set "excluded from export" state for the current caption
     * @param excluded
     */
    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    /**
     * Returns current selection
     * @return null if no valid selection. Else int array [x0,y0,x1,y1]  where x1 > x0 and y1 > y0
     */
    public int[] getSelection() {
        if (!allowSelection || !validSelection) {
            return null;
        }
        int ret[] = new int[4];
        ret[0] = selectStartX-ofsX;
        ret[1] = selectStartY-yCrop;
        ret[2] = selectEndX-ofsX;
        ret[3] = selectEndY-yCrop;
        return ret;
    }

    public void removeSelection() {
        if (allowSelection && validSelection) {
            validSelection = false;
        }
    }

    public void addSelectListener(SelectListener s) {
        selectListener = s;
    }
}
