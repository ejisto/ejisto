package com.ejisto.modules.dao.entities;

public class WebApplicationDescriptorElement {
    enum KIND {
        CLASSPATH,
        BLACKLISTED
    }
    private int id;
    private int webApplicationDescriptorId;
    private String path;
    private KIND kind;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWebApplicationDescriptorId() {
        return webApplicationDescriptorId;
    }

    public void setWebApplicationDescriptorId(int webApplicationDescriptorId) {
        this.webApplicationDescriptorId = webApplicationDescriptorId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getKind() {
        return kind.name();
    }

    public void setKind(String kind) {
        this.kind = KIND.valueOf(kind);
    }

    public void setKind(KIND kind) {
        this.kind = kind;
    }
}
