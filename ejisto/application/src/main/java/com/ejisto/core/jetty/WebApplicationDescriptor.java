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
package com.ejisto.core.jetty;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import com.ejisto.modules.dao.entities.MockedField;

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
