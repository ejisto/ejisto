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

package com.ejisto.event.listener;

import com.ejisto.event.def.MockedFieldOperation;
import com.ejisto.modules.controller.MockedFieldOperationController;
import org.springframework.context.ApplicationListener;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/3/11
 * Time: 8:28 AM
 */
public class MockedFieldOperationListener implements ApplicationListener<MockedFieldOperation> {
    @Override
    public void onApplicationEvent(final MockedFieldOperation event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Window window = SwingUtilities.windowForComponent((Component) event.getSource());
                new MockedFieldOperationController(window, event.getMockedField(),
                                                   event.getOperationType()).showDialog();
            }
        });
    }
}
