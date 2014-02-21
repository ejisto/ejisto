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

package com.ejisto.modules.dao.entities;

import com.ejisto.modules.dao.entities.helper.WebApplicationDescriptorHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static com.ejisto.modules.dao.entities.WebApplicationDescriptorElement.Kind.CLASSPATH;
import static java.util.stream.Collectors.toList;

@Log4j
public class WebApplicationDescriptor implements Serializable, Entity<String> {
    private static final long serialVersionUID = 7454195671017831484L;
    private List<WebApplicationDescriptorElement> elements = new ArrayList<>();
    private String installationPath;
    private String containerId;
    private String contextPath;
    private String deployablePath;
    private transient final Collection<MockedField> fields;
    private transient final WebApplicationDescriptorHelper helper;
    private transient File warFile;
    private transient List<WebApplicationDescriptorElement> classpathEntries;

    public WebApplicationDescriptor() {
        this.fields = new TreeSet<>((o1, o2) -> o1.getComparisonKey().compareTo(o2.getComparisonKey()));
        this.helper = new WebApplicationDescriptorHelper(this);
    }

    public void addField(MockedField field) {
        this.fields.add(field);
    }

    public void deleteAllFields() {
        this.fields.clear();
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setInstallationPath(String installationPath) {
        this.installationPath = installationPath;
    }

    public String getInstallationPath() {
        return installationPath;
    }

    public Collection<MockedField> getFields() {
        return fields;
    }

    public void setBlacklist(List<String> blacklist) {
        log.debug("blacklisting: " + blacklist);
        helper.setBlacklist(blacklist);
    }

    public boolean isBlacklistedEntry(String filename) {
        return helper.isBlacklistedEntry(filename);
    }

    public List<String> getIncludedJars() {
        return helper.getIncludedJars();
    }

    public List<String> getWhiteListContent() {
        return helper.getWhiteListContent();
    }

    @JsonIgnore
    public File getWarFile() {
        return warFile;
    }

    public void setWarFile(File warFile) {
        this.warFile = warFile;
    }

    public List<WebApplicationDescriptorElement> getElements() {
        return elements;
    }

    public void addElement(WebApplicationDescriptorElement element) {
        this.elements.add(element);
    }

    public void clearElements() {
        this.elements.clear();
    }

    public Collection<MockedField> getModifiedFields() {
        return helper.getModifiedFields();
    }

    public void setClassPathElements(List<WebApplicationDescriptorElement> classpathEntries) {
        this.classpathEntries = classpathEntries;
    }

    @JsonIgnore
    public List<WebApplicationDescriptorElement> getClassPathElements() {
        if (classpathEntries != null) {
            return classpathEntries;
        }
        return elements.stream().filter(x -> x.isOfKind(CLASSPATH)).collect(toList());
    }

    public String getDeployablePath() {
        return deployablePath;
    }

    public void setDeployablePath(String deployablePath) {
        this.deployablePath = deployablePath;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public static WebApplicationDescriptor copyOf(WebApplicationDescriptor src) {
        WebApplicationDescriptor target = new WebApplicationDescriptor();
        target.elements.addAll(src.elements);
        target.installationPath = src.installationPath;
        target.containerId = src.containerId;
        target.contextPath = src.contextPath;
        target.fields.addAll(src.fields);
        target.deployablePath = src.deployablePath;
        target.warFile = src.warFile;
        target.setClassPathElements(src.getClassPathElements());
        return target;
    }

    @Override
    public String getKey() {
        return contextPath;
    }

}
