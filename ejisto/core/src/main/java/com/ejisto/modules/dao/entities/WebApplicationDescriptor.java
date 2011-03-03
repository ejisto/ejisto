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

package com.ejisto.modules.dao.entities;


import com.ejisto.modules.dao.entities.helper.WebApplicationDescriptorHelper;
import com.ejisto.util.JndiDataSourcesRepository;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.Serializable;
import java.util.*;


public class WebApplicationDescriptor implements Serializable {
    private static final long serialVersionUID = 2024622778793996648L;
    private static final Logger logger = Logger.getLogger(WebApplicationDescriptor.class);
    private List<WebApplicationDescriptorElement> elements = new ArrayList<WebApplicationDescriptorElement>();
    private int id = -1;
    private String installationPath;
    private String contextPath;
    private Collection<MockedField> fields;
    private String deployablePath;
    private transient WebApplicationDescriptorHelper helper;
    private transient File warFile;
    private transient List<WebApplicationDescriptorElement> classpathEntries;

    public WebApplicationDescriptor() {
        this.fields = new TreeSet<MockedField>(new Comparator<MockedField>() {
            @Override
            public int compare(MockedField o1, MockedField o2) {
                return o1.getComparisonKey().compareTo(o2.getComparisonKey());
            }
        });
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
        logger.debug("blacklisting: " + blacklist);
        helper.setBlacklist(blacklist);
    }

    public boolean isBlacklistedEntry(String filename) {
        return helper.isBlacklistedEntry(filename);
    }

    public List<String> getIncludedJars() {
        return helper.getIncludedJars();
    }

    public File getWarFile() {
        return warFile;
    }

    public void setWarFile(File warFile) {
        this.warFile = warFile;
    }

    public List<WebApplicationDescriptorElement> getElements() {
        return elements;
    }

    public void setElements(List<WebApplicationDescriptorElement> elements) {
        this.elements = elements;
    }

    public void addElement(WebApplicationDescriptorElement element) {
        element.setId(id);
        this.elements.add(element);
    }

    public void clearElements() {
        this.elements.clear();
    }

    public Collection<MockedField> getModifiedFields() {
        return helper.getModifiedFields();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setClassPathElements(List<WebApplicationDescriptorElement> classpathEntries) {
        this.classpathEntries = classpathEntries;
    }

    public List<WebApplicationDescriptorElement> getClassPathElements() {
        return classpathEntries;
    }

    public List<JndiDataSource> getDataSources() {
        return JndiDataSourcesRepository.loadDataSources();
    }

    public boolean containsDataSources() {
        return !CollectionUtils.isEmpty(JndiDataSourcesRepository.loadDataSources());
    }

    public String getDeployablePath() {
        return deployablePath;
    }

    public void setDeployablePath(String deployablePath) {
        this.deployablePath = deployablePath;
    }
}
