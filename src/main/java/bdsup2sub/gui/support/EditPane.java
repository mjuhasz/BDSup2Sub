/*
 * Copyright 2013 Volker Oth (0xdeadbeef) / Miklos Juhasz (mjuhasz)
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
package bdsup2sub.gui.support;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Preview pane for edit dialog.
 * Shows color gradient, cinemascope bars and scaled down subtitle image.
 */
public class EditPane extends JPanel {

    private static final double SCREEN_ASPECT_RATIO = 16.0/9;

    private BufferedImage image;
    private int screenWidth = 1920;
    private int screenHeight = 1080;
    private int offsetX;
    private int offsetY;
    private int subtitleImageWidth;
    private int subtitleImageHeight;
    private double cinemascopeBarFactor = 5.0/42;
    private int cropOffsetY;
    private static final int INSET = 2;
    private int selectionStartX = -1;
    private int selectionEndX;
    private int selectionStartY;
    private int selectionEndY;
    private boolean selectionAllowed;
    private boolean selectionValid;
    private boolean leftButtonPressed;
    private boolean excluded;
    private int yCrop;
    private double xScaleCaption;
    private double yScaleCaption;
    private final boolean layoutPane;

    private SelectListener selectListener;

    public EditPane(boolean isLayoutPane) {
        this.layoutPane = isLayoutPane;

        addMouseListener(new EditPaneMouseListener());
        addMouseMotionListener(new EditPaneMouseMotionListener());
    }

    public EditPane() {
        this(false);
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        draw(graphics);
    }

    @Override
    public void update(Graphics graphics) {
        super.update(graphics);
        draw(graphics);
    }

    /**
     * Create color gradient, cinemascope bars and draw scaled down subtitle image.
     */
    void draw(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D)graphics;

        int width = this.getWidth();
        int height = this.getHeight();

        int xl, yl, wl, hl;

        if (layoutPane) {
            graphics2D.setColor(UIManager.getColor("Panel.background"));
            graphics2D.fillRect(0, 0, width, height);
            // paint outer frame (16:9)
            wl = width - 2 * INSET;
            hl = (wl * 9 + 8) / 16;
            if (hl > height) {
                hl = height - INSET;
                wl = (hl * 32 + 8) / 18;
            }
            yl = (height - hl + 1) / 2;
            xl = (width - wl + 1) / 2;
        } else {
            wl = width;
            hl = height;
            xl = 0;
            yl = 0;
        }

        int cinemascopeBarHeight = (int)(hl * cinemascopeBarFactor + 0.5);
        // paint color gradient
        graphics2D.setPaint(new GradientPaint(xl, yl, Color.BLUE, wl, hl, Color.BLACK));
        graphics2D.fillRect(xl, yl + cinemascopeBarHeight, wl, hl - cinemascopeBarHeight);
        // paint cinemascope bars
        graphics2D.setPaint(Color.BLACK);
        graphics2D.fillRect(xl, yl, wl, cinemascopeBarHeight);
        graphics2D.fillRect(xl, yl + hl - cinemascopeBarHeight, wl, cinemascopeBarHeight);

        yCrop = offsetY;
        if (yCrop < cropOffsetY) {
            yCrop = cropOffsetY;
        } else {
            int yMax = screenHeight - subtitleImageHeight - cropOffsetY;
            if (yCrop > yMax) {
                yCrop = yMax;
            }
        }

        double sx;
        double sy;
        // draw scaled down subtitle image
        sx = (double)wl / screenWidth;
        sy = (double)hl / screenHeight;
        xScaleCaption = sx;
        yScaleCaption = sy;
        if (subtitleImageWidth > 0 && image != null) {
            // inner frame
            int wi = (int)(subtitleImageWidth * sx + 0.5);
            int hi = (int)(subtitleImageHeight * sy + 0.5);
            int xi = xl + (int)(offsetX * sx + 0.5);
            int yi = yl + (int)(yCrop * sy + 0.5);

            graphics2D.setColor(Color.GREEN);
            graphics2D.drawRect(xi, yi, wi - 1, hi - 1);

            graphics2D.drawImage(image, xi, yi, wi, hi, this);

            if (selectionValid && !leftButtonPressed) {
                if (selectionStartX >= subtitleImageWidth + offsetX || selectionEndX <= offsetX || selectionStartY >= subtitleImageHeight + yCrop || selectionEndY < yCrop) {
                    selectionValid = false;
                } else {
                    if (selectionStartX < offsetX) {
                        selectionStartX = offsetX;
                    }
                    if (selectionEndX >= subtitleImageWidth + offsetX) {
                        selectionEndX = subtitleImageWidth + offsetX - 1;
                    }
                    if (selectionStartY < yCrop) {
                        selectionStartY = yCrop;
                    }
                    if (selectionEndY >= subtitleImageHeight + yCrop) {
                        selectionEndY = subtitleImageHeight + yCrop - 1;
                    }
                }
            }
        }

        // draw lines representing crop offset
        if (cropOffsetY > 0) {
            graphics2D.setPaint(Color.RED);
            int y = yl + (int)(cropOffsetY * sy + 0.5);
            graphics2D.drawLine(xl, y, wl - 1, y);
            y = yl + hl - (int)(cropOffsetY * sy + 0.5) - 1;
            graphics2D.drawLine(xl, y, wl - 1, y);
        }

        // draw selection window
        if (selectionValid) {
            graphics2D.setPaint(Color.YELLOW);
            graphics2D.drawRect((int) (selectionStartX * sx + 0.5), (int) (selectionStartY * sy + 0.5),
                    (int) ((selectionEndX - selectionStartX) * sx + 0.5), (int) ((selectionEndY - selectionStartY) * sy + 0.5));
        }

        if (excluded) {
            graphics2D.setPaint(Color.RED);
            graphics2D.drawLine(xl, yl, xl + wl - 1, yl + hl - 1);
            graphics2D.drawLine(xl + wl, yl, xl - 1, yl + hl - 1);
        }

    }

    public void setImage(BufferedImage image, int width, int height) {
        this.image = image;
        this.subtitleImageWidth = width;
        this.subtitleImageHeight = height;
    }

    public void setScreenDimension(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public void setSubtitleOffsets(int x, int y) {
        this.offsetX = x;
        this.offsetY = y < cropOffsetY ? cropOffsetY : y;
    }

    public void setAspectRatio(double aspectRatio) {
        this.cinemascopeBarFactor = (1.0 - SCREEN_ASPECT_RATIO / aspectRatio) / 2.0;
    }

    public void setCropOffsetY(int offset) {
        this.cropOffsetY = offset;
    }

    public void setSelectionAllowed(boolean selectionAllowed) {
        this.selectionAllowed = selectionAllowed;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    /**
     * Returns current selection
     * @return null if no valid selection. Else int array [x0,y0,x1,y1]  where x1 > x0 and y1 > y0
     */
    public int[] getSelection() {
        if (!selectionAllowed || !selectionValid) {
            return null;
        }
        int ret[] = new int[4];
        ret[0] = selectionStartX - offsetX;
        ret[1] = selectionStartY -yCrop;
        ret[2] = selectionEndX - offsetX;
        ret[3] = selectionEndY -yCrop;
        return ret;
    }

    public void removeSelection() {
        if (selectionAllowed && selectionValid) {
            this.selectionValid = false;
        }
    }

    public void addSelectListener(SelectListener selectListener) {
        this.selectListener = selectListener;
    }

    private class EditPaneMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (selectionAllowed && e.getButton() == MouseEvent.BUTTON1) {
                selectionStartX = (int)(e.getX() / xScaleCaption + 0.5);
                selectionStartY = (int)(e.getY() / yScaleCaption + 0.5);
                leftButtonPressed = true;
                selectionValid = false;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (selectionAllowed && e.getButton() == MouseEvent.BUTTON1) {
                selectionEndX = (int)(e.getX() / xScaleCaption + 0.5);
                selectionEndY = (int)(e.getY() / yScaleCaption + 0.5);
                leftButtonPressed = false;
                if (selectionStartX >= 0 && selectionEndX > selectionStartX && selectionEndY > selectionStartY) {
                    selectionValid = true;
                }
                repaint();
                selectListener.selectionPerformed(selectionValid);
            }
        }
    }

    private class EditPaneMouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (leftButtonPressed) {
                selectionEndX = (int)(e.getX() / xScaleCaption + 0.5);
                selectionEndY = (int)(e.getY() / yScaleCaption + 0.5);
                if (selectionStartX >= 0 && selectionEndX > selectionStartX && selectionEndY > selectionStartY) {
                    selectionValid = true;
                }
                repaint();
            }
        }
    }

    public interface SelectListener {
        void selectionPerformed(boolean valid);
    }
}
