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

package com.ejisto.util;

import com.ejisto.constants.StringConstants;
import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.SettingsRepository;
import lombok.extern.log4j.Log4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static com.ejisto.constants.StringConstants.LAST_FILESELECTION_PATH;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Log4j
public abstract class GuiUtils {

    public static final AtomicReference<ApplicationEventDispatcher> EVENT_DISPATCHER = new AtomicReference<>();
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("messages");
    private static ActionMap actionMap = new ActionMap();

    private GuiUtils() {

    }

    public static void centerOnScreen(Window window) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        window.setBounds((screen.width / 2 - window.getWidth() / 2), (screen.height / 2 - window.getHeight() / 2),
                         window.getWidth(),
                         window.getHeight());
    }

    public static boolean showWarning(Component owner, String text, Object... values) {
        return JOptionPane.showConfirmDialog(owner, getMessage(text, values), getMessage("confirmation.title"),
                                             JOptionPane.YES_NO_OPTION,
                                             JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public static String getMessage(String key, Object... values) {
        if (!MESSAGES.containsKey(key)) {
            return key;
        }
        String value = MESSAGES.getString(key);
        return MessageFormat.format(value, values);
    }

    public static void showErrorMessage(final Component owner, final String text) {
        try {
            synchronousRunInEDT(() -> JOptionPane.showMessageDialog(owner, text, "error", JOptionPane.ERROR_MESSAGE));
        } catch (InvocationTargetException e) {
            GuiUtils.log.error("exception during error message notification", e);
        }

    }

    private static void synchronousRunInEDT(Runnable action) throws InvocationTargetException {
        try {
            SwingUtilities.invokeAndWait(action);
        } catch (InterruptedException e) {
            GuiUtils.log.error("action interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public static synchronized void putAction(Action action) {
        actionMap.put(action.getValue(Action.NAME), action);
    }

    public static synchronized void putAction(Object key, Action action) {
        actionMap.put(key, action);
    }

    public static String buildCommand(StringConstants commandPrefix, String containerId, String contextPath) {
        return containerId + commandPrefix.getValue() + contextPath;
    }

    public static void runOnEDT(Runnable action) {
        SwingUtilities.invokeLater(action);
    }

    private static ActionMap cloneActionMap(ActionMap original) {
        ActionMap map = new ActionMap();
        Object[] keys = original.keys();
        if (keys != null) {
            for (Object key : keys) {
                map.put(key, original.get(key));
            }
        }
        return map;
    }

    public static Throwable getRootThrowable(Throwable in) {
        if (in.getCause() != null) {
            return getRootThrowable(in.getCause());
        }
        return in;
    }

    public static String encodeTreePath(String[] treePath) {
        return Arrays.stream(treePath).collect(joining(">"));
    }

    public static File selectFile(Component parent,
                                  String title,
                                  String directoryPath,
                                  boolean saveLastSelectionPath,
                                  SettingsRepository settingsRepository,
                                  String... extensions) {
        JFileChooser fileChooser = new JFileChooser(directoryPath);
        String description = String.format("*.%s", stream(extensions).collect(joining(", *.")));
        fileChooser.setFileFilter(new FileNameExtensionFilter(description, extensions));
        fileChooser.setDialogTitle(title);
        return openFileSelectionDialog(parent, saveLastSelectionPath, fileChooser, LAST_FILESELECTION_PATH,
                                       settingsRepository);
    }

    public static File openFileSelectionDialog(Component parent,
                                               boolean saveLastSelectionPath,
                                               JFileChooser fileChooser,
                                               StringConstants settingKey,
                                               SettingsRepository settingsRepository) {
        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            if (saveLastSelectionPath) {
                String path = selected.isDirectory() ? selected.getAbsolutePath() : selected.getParent();
                settingsRepository.putSettingValue(settingKey, path);
            }
            return selected;
        } else {
            return null;
        }
    }

    public static void publishEvent(BaseApplicationEvent event) {
        ApplicationEventDispatcher.publish(event);
    }

    @FunctionalInterface
    public interface EditorColumnFormattingStrategy {
        public abstract List<String> format(MockedField row);
    }

}
