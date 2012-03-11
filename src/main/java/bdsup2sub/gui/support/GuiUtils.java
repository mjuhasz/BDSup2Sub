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
package bdsup2sub.gui.support;

import java.awt.*;

public final class GuiUtils {

    private GuiUtils() {
    }

    public static void centerRelativeToParent(Window window, Window parent) {
        Point p = parent.getLocation();
        window.setLocation(p.x + parent.getWidth() / 2 - window.getWidth() / 2, p.y + parent.getHeight() / 2 - window.getHeight() / 2);
    }
}
