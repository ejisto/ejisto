package com.ejisto.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.ejisto.modules.dao.entities.MockedField;

public class WebApplicationDescriptor {
	private String installationPath;
	private String contextPath;
	private Collection<MockedField> fields;
	private List<MockedField> modifiedFields;
	private URL[] classpathEntries;
	private List<String> blacklist;
	private List<String> includedJars = new ArrayList<String>();
	private File warFile;
	
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
        return includedJars;
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
