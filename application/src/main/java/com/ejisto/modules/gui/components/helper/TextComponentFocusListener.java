/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
 *
 * Ejisto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ejisto is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ejisto.modules.gui.components.helper;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/12/11
 * Time: 7:22 PM
 */
public class TextComponentFocusListener extends FocusAdapter {
    @Override
    public void focusGained(final FocusEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Object o = e.getSource();
                if (o != null && JTextComponent.class.isAssignableFrom(o.getClass())) {
                    ((JTextComponent) e.getSource()).selectAll();
                }
            }
        });
    }
}
