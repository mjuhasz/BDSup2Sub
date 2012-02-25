package deadbeef.gui;

import java.awt.*;

final class GuiUtils {

    private GuiUtils() {
    }

    static void centerRelativeToParent(Window window, Window parent) {
        Point p = parent.getLocation();
        window.setLocation(p.x + parent.getWidth() / 2 - window.getWidth() / 2, p.y + parent.getHeight() / 2 - window.getHeight() / 2);
    }
}
