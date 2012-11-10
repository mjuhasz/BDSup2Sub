/*
 * Copyright 2012 Miklos Juhasz (mjuhasz), JetBrains s.r.o.
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
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class GuiUtils {

    public static final Color GTK_AMBIANCE_TEXT_COLOR = new Color(223, 219, 210);
    public static final Color GTK_AMBIANCE_BACKGROUND_COLOR = new Color(67, 66, 63);

    private GuiUtils() {
    }

    public static boolean isUnderGTKLookAndFeel() {
        return UIManager.getLookAndFeel().getName().contains("GTK");
    }

    public static String getGtkThemeName() {
        final LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf != null && "GTKLookAndFeel".equals(laf.getClass().getSimpleName())) {
            try {
                final Method method = laf.getClass().getDeclaredMethod("getGtkThemeName");
                method.setAccessible(true);
                final Object theme = method.invoke(laf);
                if (theme != null) {
                    return theme.toString();
                }
            }
            catch (Exception ignored) {
            }
        }
        return null;
    }

    public static void applyGtkThemeWorkarounds() {
        fixGtkPopupStyle();
        fixGtkPopupWeight();
    }

    private static void fixGtkPopupStyle() {
        if (!isUnderGTKLookAndFeel()) {
            return;
        }
        final SynthStyleFactory original = SynthLookAndFeel.getStyleFactory();
        SynthLookAndFeel.setStyleFactory(new SynthStyleFactory() {
            @Override
            public SynthStyle getStyle(final JComponent c, final Region id) {
                final SynthStyle style = original.getStyle(c, id);
                if (id == Region.POPUP_MENU) {
                    fixPopupMenuStyle(style);
                } else if (id == Region.POPUP_MENU_SEPARATOR) {
                    fixPopupMenuSeparatorStyle(style);
                }
                return style;
            }

            private void fixPopupMenuStyle(SynthStyle style) {
                try {
                    Field f = getAccessibleFieldFromStyle(style, "xThickness");
                    final Object x = f.get(style);
                    if (x instanceof Integer && (Integer)x == 0) {
                        f.set(style, 1);
                        f = getAccessibleFieldFromStyle(style, "yThickness");
                        f.set(style, 3);
                    }
                } catch (Exception ignore) {
                    // ignore
                }
            }

            private void fixPopupMenuSeparatorStyle(SynthStyle style) {
                try {
                    Field f = getAccessibleFieldFromStyle(style, "yThickness");
                    final Object y = f.get(style);
                    if (y instanceof Integer && (Integer)y == 0) {
                        f.set(style, 2);
                    }
                } catch (Exception ignore) {
                    // ignore
                }
            }

            private Field getAccessibleFieldFromStyle(SynthStyle style, String fieldName) throws NoSuchFieldException {
                Field f = style.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                return f;
            }
        });

        new JPopupMenu();  // invokes updateUI() -> updateStyle()
        new JPopupMenu.Separator(); // invokes updateUI() -> updateStyle()

        SynthLookAndFeel.setStyleFactory(original);
    }

    private static void fixGtkPopupWeight() {
        if (!isUnderGTKLookAndFeel()) {
            return;
        }

        PopupFactory factory = PopupFactory.getSharedInstance();
        if (!(factory instanceof MyPopupFactory)) {
            factory = new MyPopupFactory(factory);
            PopupFactory.setSharedInstance(factory);
        }
    }

    private static class MyPopupFactory extends PopupFactory {
        private static final int WEIGHT_HEAVY = 2; // package-private in PopupFactory

        private final PopupFactory myDelegate;

        public MyPopupFactory(final PopupFactory delegate) {
            myDelegate = delegate;
        }

        public Popup getPopup(final Component owner, final Component contents, final int x, final int y) throws IllegalArgumentException {
            final int popupType = GuiUtils.isUnderGTKLookAndFeel() ? WEIGHT_HEAVY : PopupUtil.getPopupType(this);
            if (popupType >= 0) {
                PopupUtil.setPopupType(myDelegate, popupType);
            }
            return myDelegate.getPopup(owner, contents, x, y);
        }
    }

    public static void centerRelativeToOwner(Window window) {
        Window owner = window.getOwner();
        Point p = owner.getLocation();
        window.setLocation(p.x + owner.getWidth() / 2 - window.getWidth() / 2, p.y + owner.getHeight() / 2 - window.getHeight() / 2);
    }
}
