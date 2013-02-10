/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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

package com.ejisto.modules.gui.components;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/30/11
 * Time: 7:50 PM
 */
class BufferedTextArea extends JTextArea implements DocumentListener {

    private final int bufferSize;

    public BufferedTextArea(int bufferSize) {
        super();
        this.bufferSize = bufferSize;
        getDocument().addDocumentListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        try {
            checkBufferSize();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    private synchronized void checkBufferSize() throws BadLocationException {
        Element root = getDocument().getDefaultRootElement();
        while (root.getElementCount() > bufferSize) {
            getDocument().remove(0, root.getElement(0).getEndOffset());
        }
    }
}
