package org.jflame.logviewer.model;

import org.jflame.commons.model.TreeNode;
import org.jflame.commons.util.DateHelper;

public class FileAttri extends TreeNode implements Comparable<FileAttri> {

    private static final long serialVersionUID = 3862330131151025200L;
    private String path;
    private long lastUpdateDate;

    public void setSize(String size) {
        addAttribute("size", size);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUpdateDateText() {
        return DateHelper.formatLong(new java.util.Date(lastUpdateDate * 1000));
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public int compareTo(FileAttri o) {
        if (this.getLastUpdateDate() > o.getLastUpdateDate())
            return 1;
        else if (this.getLastUpdateDate() < o.getLastUpdateDate())
            return -1;
        else {
            return 0;
        }
    }

}
