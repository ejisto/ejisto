/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/18/11
 * Time: 8:35 AM
 */
public class MockedFieldEditingEventHelper {

    private List<FieldEditingListener> listeners;

    public MockedFieldEditingEventHelper() {
        this.listeners = new ArrayList<FieldEditingListener>();
    }

    public void addFieldEditingListener(FieldEditingListener fieldEditingListener) {
        listeners.add(fieldEditingListener);
    }

    public void removeFieldEditingListener(FieldEditingListener fieldEditingListener) {
        listeners.remove(fieldEditingListener);
    }

    public void fireEditingStarted(MockedFieldEditingEvent event) {
        for (FieldEditingListener listener : listeners) {
            listener.editingStarted(event);
        }
    }

    public void fireEditingStopped(MockedFieldEditingEvent event) {
        for (FieldEditingListener listener : listeners) {
            listener.editingStopped(event);
        }
    }

    public void fireEditingCanceled(MockedFieldEditingEvent event) {
        for (FieldEditingListener listener : listeners) {
            listener.editingCanceled(event);
        }
    }
}
