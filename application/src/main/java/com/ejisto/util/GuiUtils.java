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
import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.executor.ErrorDescriptor;
import com.ejisto.modules.gui.EjistoAction;
import com.ejisto.modules.gui.components.ContainerTab;
import com.ejisto.modules.gui.components.tree.node.FieldNode;
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.modules.repository.SettingsRepository;
import com.ejisto.modules.repository.WebApplicationRepository;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.ejisto.constants.StringConstants.LAST_FILESELECTION_PATH;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Log4j
public abstract class GuiUtils {

    public static final AtomicReference<ApplicationEventDispatcher> EVENT_DISPATCHER = new AtomicReference<>();
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("messages");
    private static ActionMap actionMap = new ActionMap();
    private static Font defaultFont;

    private GuiUtils() {

    }

    public static void centerOnScreen(Window window) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        window.setBounds((screen.width / 2 - window.getWidth() / 2), (screen.height / 2 - window.getHeight() / 2),
                         window.getWidth(),
                         window.getHeight());
    }

    public static String getMessage(String key, Object... values) {
        if (!MESSAGES.containsKey(key)) {
            return key;
        }
        String value = MESSAGES.getString(key);
        return MessageFormat.format(value, values);
    }

    public static <T extends BaseApplicationEvent> ImageIcon getIcon(T applicationEvent) {
        return getIcon(applicationEvent.getIconKey());
    }

    public static ImageIcon getIcon(String key) {
        if (!StringUtils.isNotBlank(key)) {
            return null;
        }
        return new ImageIcon(GuiUtils.class.getResource(getMessage(key)));
    }

    public static boolean showWarning(Component owner, String text, Object... values) {
        return JOptionPane.showConfirmDialog(owner, getMessage(text, values), getMessage("confirmation.title"),
                                             JOptionPane.YES_NO_OPTION,
                                             JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public static void showErrorMessage(final Component owner, final String text) {
        try {
            synchronousRunInEDT(() -> JOptionPane.showMessageDialog(owner, text, "error", JOptionPane.ERROR_MESSAGE));
        } catch (InvocationTargetException e) {
            log.error("exception during error message notification", e);
        }

    }

    public static List<List<String>> asStringList(Collection<MockedField> fields, EditorColumnFillStrategy fillStrategy) {
        List<List<String>> fieldsAsString = new ArrayList<>();
        for (MockedField mockedField : fields) {
            fillStrategy.fillRow(fieldsAsString, mockedField);
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

    public static void setDefaultFont(Font defaultFont) {
        GuiUtils.defaultFont = defaultFont;
    }

    public static Font getDefaultFont() {
        return defaultFont;
    }

    public static String buildCommand(StringConstants commandPrefix, String containerId, String contextPath) {
        return containerId + commandPrefix.getValue() + contextPath;
    }

    private static void synchronousRunInEDT(Runnable action) throws InvocationTargetException {
        try {
            SwingUtilities.invokeAndWait(action);
        } catch (InterruptedException e) {
            log.error("action interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public static void runOnEDT(Runnable action) {
        SwingUtilities.invokeLater(action);
    }

    public static void setActionMap(ActionMap actionMap, JComponent component) {
        fillActionMap(component.getActionMap(), actionMap);
    }

    public static void fillActionMap(ActionMap original, ActionMap target) {
        ActionMap parent = target;
        if (original.getParent() != null) {
            parent = cloneActionMap(target);
            parent.setParent(original.getParent());
        }
        original.setParent(parent);
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

    public static List<ContainerTab> getRegisteredContainers(ContainersRepository containersRepository,
                                                             WebApplicationRepository webApplicationRepository) {
        List<com.ejisto.modules.dao.entities.Container> containers = containersRepository.loadContainers();
        List<ContainerTab> containerTabs = new ArrayList<>();
        for (Container container : containers) {
            containerTabs.add(buildContainerTab(container, webApplicationRepository));
        }
        return containerTabs;
    }

    public static List<com.ejisto.modules.dao.entities.Container> getActiveContainers(ContainersRepository containersRepository) {
        return containersRepository.loadContainers();
    }

    public static com.ejisto.modules.dao.entities.Container loadContainer(ContainersRepository containersRepository, String id) {
        try {
            return containersRepository.loadContainer(id);
        } catch (NotInstalledException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ContainerTab buildContainerTab(Container container,
                                                  WebApplicationRepository webApplicationRepository) {
        return new ContainerTab(container.getDescription(), container.getId(), webApplicationRepository);
    }

    public abstract static class EditorColumnFillStrategy {
        public abstract void fillRow(List<List<String>> rows, MockedField row);
    }

    public static Throwable getRootThrowable(Throwable in) {
        if (in.getCause() != null) {
            return getRootThrowable(in.getCause());
        }
        return in;
    }

    public static void disableFocusPainting(JButton button) {
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setRolloverEnabled(true);
        button.setFocusPainted(false);
    }

    public static Icon getErrorIcon(ErrorDescriptor.Category category) {
        return getIcon(category.getIconKey());
    }

    public static String encodeTreePath(String[] treePath) {
        return Arrays.stream(treePath).collect(joining(">"));
    }

    public static String encodeTreePath(String[] path, int from, int to) {
        if (from >= to) {
            throw new IllegalArgumentException("from: " + from + " to:" + to);
        }
        String[] target = new String[to - from];
        System.arraycopy(path, 0, target, from, to - from);
        return encodeTreePath(target);
    }

    public static void makeTransparent(JButton button) {
        button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        button.setBackground(new Color(255, 255, 255, 0));
        button.setHideActionText(false);
        disableFocusPainting(button);
    }

    public static File selectFile(Component parent,
                                  String title,
                                  String directoryPath,
                                  boolean saveLastSelectionPath,
                                  SettingsRepository settingsRepository,
                                  String... extensions) {
        JFileChooser fileChooser = new JFileChooser(directoryPath);
        fileChooser.setFileFilter(new FileNameExtensionFilter(format("*.%s", stream(extensions).collect(joining(", *.")))));
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

    public static TreePath getNodePath(FieldNode node) {
        Deque<FieldNode> path = new ArrayDeque<>();
        path.add(node);
        while ((node = (FieldNode) node.getParent()) != null) {
            path.addFirst(node);
        }
        return new TreePath(path.toArray());
    }

    public static void publishError(Object source, Throwable e) {
        publishEvent(new ApplicationError(source, ApplicationError.Priority.FATAL, e));
    }

    public static void publishEvent(BaseApplicationEvent event) {
        ApplicationEventDispatcher.publish(event);
    }

    public static <T extends BaseApplicationEvent> void registerApplicationEventListener(ApplicationListener<T> listener) {
        EVENT_DISPATCHER.get().registerApplicationEventListener(listener);
    }

}
