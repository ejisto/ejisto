package com.ejisto.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.ejisto.modules.dao.entities.MockedField;

public class GuiUtils {
	
	public static void centerOnScreen(Window window) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        window.setBounds((screen.width / 2 - window.getWidth() / 2), (screen.height / 2 - window.getHeight() / 2), window.getWidth(), window.getHeight());
    }
	
	public static String getMessage(String key, Object... values) {
		return SpringBridge.getMessage(key, "en", values);//TODO localize
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
	
}
