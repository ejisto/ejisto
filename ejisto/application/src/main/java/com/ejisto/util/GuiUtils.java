/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.util;

import java.awt.Component;
import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.util.StringUtils;

import com.ejisto.core.jetty.WebAppContextRepository;
import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.EjistoAction;

public class GuiUtils {
    
    private static ActionMap actionMap = new ActionMap();
	
	public static void centerOnScreen(Window window) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        window.setBounds((screen.width / 2 - window.getWidth() / 2), (screen.height / 2 - window.getHeight() / 2), window.getWidth(), window.getHeight());
    }
	
	public static String getMessage(String key, Object... values) {
		return SpringBridge.getMessage(key, "en", values);//TODO localize
	}
	
	public static <T extends BaseApplicationEvent> ImageIcon getIcon(T applicationEvent) {
        String iconKey = applicationEvent.getIconKey();
        if(!StringUtils.hasText(iconKey)) return null;
        return new ImageIcon(GuiUtils.class.getResource(getMessage(iconKey)));
    }
	
	public static boolean showWarning(Component owner, String text, Object... values) {
		return JOptionPane.showConfirmDialog(owner, getMessage(text, values), getMessage("confirmation.title"), JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
    }
	
	public static List<List<String>> stringify(List<MockedField> fields) {
		return stringify(fields, false);
	}
	
	public static List<List<String>> stringify(List<MockedField> fields, boolean partial) {
        List<List<String>> fieldsAsString = new ArrayList<List<String>>();
        ArrayList<String> property;
        for (MockedField mockedField : fields) {
            property = new ArrayList<String>();
            if(!partial) {
            	property.add(String.valueOf(mockedField.getId()));
            	property.add(mockedField.getContextPath());
            }
            property.add(mockedField.getClassName());
            property.add(mockedField.getFieldName());
            property.add(mockedField.getFieldType());
            property.add(mockedField.getFieldValue());
            fieldsAsString.add(property);
        }
        return fieldsAsString;
    }
	
	public static synchronized void putAction(Action action) {
	    actionMap.put(action.getValue(Action.NAME), action);
	}
	
	public static synchronized void putAction(Object key, Action action) {
        actionMap.put(key, action);
    }
	
	public static synchronized <T extends BaseApplicationEvent> void putAction(EjistoAction<T> action) {
        actionMap.put(action.getKey(), action);
    }
	
	public static synchronized Action getAction(String name) {
	    return actionMap.get(name);
	}
	
	public static synchronized ActionMap getActionMap() {
	    return actionMap;
	}
	
	public static synchronized Collection<Action> getActionsFor(String prefix) {
	    return select(actionMap, having(on(Action.class).getValue(Action.NAME).toString().startsWith(prefix), equalTo(true)));
	}
	
	public static Collection<WebAppContext> getAllRegisteredContexts() {
	    return SpringBridge.getInstance().getBean("webAppContextRepository", WebAppContextRepository.class).getAllContexts();
	}
	
}
