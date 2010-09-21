/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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

package com.ejisto.core.jetty;

import com.ejisto.modules.dao.entities.MockedField;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

public class WebApplicationDescriptor implements Serializable {
    private static final long serialVersionUID = 2024622778793996648L;

    private String installationPath;
	private String contextPath;
	private Collection<MockedField> fields;
	private List<MockedField> modifiedFields;
	private URL[] classpathEntries;
	private List<String> blacklist;
	private HashSet<String> includedJars = new HashSet<String>();
	private transient File warFile;
	
	public WebApplicationDescriptor() {
        this.fields = new TreeSet<MockedField>(new Comparator<MockedField>() {
            @Override
            public int compare(MockedField o1, MockedField o2) {
                return o1.getComparisonKey().compareTo(o2.getComparisonKey());
            }
        });
    }
	
	public void addField(MockedField field) {
	    this.fields.add(field);
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
	
	public void setClasspathEntries(URL[] classpathEntries) {
        this.classpathEntries = classpathEntries;
    }
	
	public URL[] getClasspathEntries() {
        return classpathEntries;
    }
	
	public Collection<MockedField> getFields() {
        return fields;
    }
	
	public void setBlacklist(List<String> blacklist) {
        this.blacklist = blacklist;
    }
	
	public boolean isBlacklistedEntry(String filename) {
	    return blacklist.contains(filename);
	}
	
	public void addJarFileName(String filename) {
	    this.includedJars.add(filename);
	}
	
	public List<String> getIncludedJars() {
        return new ArrayList<String>(includedJars);
    }
	
	public File getWarFile() {
        return warFile;
    }
	
	public void setWarFile(File warFile) {
        this.warFile = warFile;
    }
	
	public void setModifiedFields(List<MockedField> modifiedFields) {
        this.modifiedFields = modifiedFields;
    }
	
	public List<MockedField> getModifiedFields() {
        return modifiedFields;
    }
}
