package org.jflame.logviewer.model;

import java.io.Serializable;

import org.jflame.toolkit.util.DateHelper;

public class FileAttri implements Comparable<FileAttri>, Serializable {

    private static final long serialVersionUID = 3862330131151025200L;
    private String name;
    private String size;
    private long lastUpdateDate;
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getLastUpdateDateText() {
        return DateHelper.formatLong(new java.util.Date(lastUpdateDate));
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int compareTo(FileAttri o) {
        if (this.lastUpdateDate > o.lastUpdateDate)
            return -1;
        else if (this.lastUpdateDate < o.lastUpdateDate)
            return 1;
        else {
            return 0;
        }
    }

}
