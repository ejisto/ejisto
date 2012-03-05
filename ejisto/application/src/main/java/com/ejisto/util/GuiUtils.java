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

package com.ejisto.util;

import ch.lambdaj.Lambda;
import com.ejisto.constants.StringConstants;
import com.ejisto.core.container.WebApplication;
import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.event.listener.ApplicationEventDispatcher;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.executor.ErrorDescriptor;
import com.ejisto.modules.gui.EjistoAction;
import com.ejisto.modules.gui.components.ContainerTab;
import com.ejisto.modules.repository.SettingsRepository;
import com.ejisto.modules.repository.WebApplicationRepository;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.*;
import static com.ejisto.constants.StringConstants.LAST_FILESELECTION_PATH;
import static org.hamcrest.Matchers.equalTo;

@Log4j
public class GuiUtils {

    private static ActionMap actionMap = new ActionMap();
    private static Font defaultFont;

    public static void centerOnScreen(Window window) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        window.setBounds((screen.width / 2 - window.getWidth() / 2), (screen.height / 2 - window.getHeight() / 2),
                         window.getWidth(),
                         window.getHeight());
    }

    public static String getMessage(String key, Object... values) {
        return SpringBridge.getMessage(key, "en", values);//TODO localize
    }

    public static <T extends BaseApplicationEvent> ImageIcon getIcon(T applicationEvent) {
        return getIcon(applicationEvent.getIconKey());
    }

    public static ImageIcon getIcon(String key) {
        if (!StringUtils.hasText(key)) return null;
        return new ImageIcon(GuiUtils.class.getResource(getMessage(key)));
    }

    public static boolean showWarning(Component owner, String text, Object... values) {
        return JOptionPane.showConfirmDialog(owner, getMessage(text, values), getMessage("confirmation.title"),
                                             JOptionPane.YES_NO_OPTION,
                                             JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public static void showErrorMessage(final Component owner, final String text) {
        try {
            synchronousRunInEDT(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(owner, text, "error", JOptionPane.ERROR_MESSAGE);
                }
            });
        } catch (InvocationTargetException e) {
            log.error("exception during error message notification", e);
        }

    }

    public static List<List<String>> asStringList(Collection<MockedField> fields, EditorColumnFillStrategy fillStrategy) {
        List<List<String>> fieldsAsString = new ArrayList<List<String>>();
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

    public static synchronized Collection<Action> getActionsFor(String prefix) {
        return select(actionMap,
                      having(on(Action.class).getValue(Action.NAME).toString().startsWith(prefix), equalTo(true)));
    }

    public static void setDefaultFont(Font defaultFont) {
        GuiUtils.defaultFont = defaultFont;
    }

    public static Font getDefaultFont() {
        return defaultFont;
    }

    public static Map<String, List<WebApplication<?>>> getAllRegisteredContexts() {
        return SpringBridge.getInstance().getBean("webApplicationRepository",
                                                  WebApplicationRepository.class).getInstalledWebApplications();
    }

    public static String buildCommand(StringConstants commandPrefix, String containerId, String contextPath) {
        return new StringBuilder(containerId).append(commandPrefix.getValue()).append(contextPath).toString();
    }

    public static void synchronousRunInEDT(Runnable action) throws InvocationTargetException {
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

    @SuppressWarnings("unchecked")
    public static <T extends ApplicationEvent> void registerEventListener(Class<T> eventClass, ApplicationListener<T> listener) {
        ApplicationEventDispatcher applicationEventDispatcher = SpringBridge.getInstance().getBean(
                "applicationEventDispatcher", ApplicationEventDispatcher.class);
        applicationEventDispatcher.registerApplicationEventListener(eventClass,
                                                                    (ApplicationListener<ApplicationEvent>) listener);
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
        for (Object key : original.keys()) {
            map.put(key, original.get(key));
        }
        return map;
    }

    public static List<ContainerTab> getRegisteredContainers() {
        List<com.ejisto.modules.dao.entities.Container> containers = SpringBridge.loadExistingContainers();
        List<ContainerTab> containerTabs = new ArrayList<ContainerTab>();
        for (Container container : containers) {
            containerTabs.add(buildContainerTab(container));
        }
        return containerTabs;
    }

    public static List<com.ejisto.modules.dao.entities.Container> getActiveContainers() {
        return SpringBridge.loadExistingContainers();
    }

    public static com.ejisto.modules.dao.entities.Container loadContainer(String id) {
        try {
            return SpringBridge.loadExistingContainer(id);
        } catch (NotInstalledException e) {
            throw new RuntimeException(e);
        }
    }

    private static ContainerTab buildContainerTab(Container container) {
        return new ContainerTab(container.getDescription(), container.getCargoId());
    }

    public static abstract class EditorColumnFillStrategy {
        public abstract void fillRow(List<List<String>> rows, MockedField row);
    }

    public static Throwable getRootThrowable(Throwable in) {
        if (in.getCause() != null) return getRootThrowable(in.getCause());
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

    public static String encodeTreePath(Object treePath) {
        return Lambda.join(treePath, ">");
    }

    public static String encodeTreePath(String[] path, int from, int to) {
        Assert.isTrue(from < to);
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

    public static File selectFile(Component parent, String directoryPath, boolean saveLastSelectionPath, final Collection<String> extensions) {
        JFileChooser fileChooser = new JFileChooser(directoryPath);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || FilenameUtils.isExtension(f.getName(), extensions);
            }

            @Override
            public String getDescription() {
                return "*." + join(extensions, ", *.");
            }
        });
        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            if (saveLastSelectionPath)
                SettingsRepository.getInstance().putSettingValue(LAST_FILESELECTION_PATH,
                                                                 fileChooser.getCurrentDirectory().getAbsolutePath());
            return fileChooser.getSelectedFile();
        } else return null;
    }
}
