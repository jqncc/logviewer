package org.jflame.logviewer.model;

import java.io.Serializable;

import org.jflame.commons.model.TreeNode;
import org.jflame.commons.util.DateHelper;

public class FileAttri extends TreeNode implements Serializable {

    private static final long serialVersionUID = 3862330131151025200L;
    private String path;// Comparable<FileAttri>,

    public void setSize(String size) {
        addAttribute("size", size);
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        addAttribute("lastUpdateDate", DateHelper.formatLong(new java.util.Date(lastUpdateDate * 1000)));
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /*  @Override
    public int compareTo(FileAttri o) {
        if (this.getLastUpdateDate() > o.getLastUpdateDate())
            return -1;
        else if (this.getLastUpdateDate() < o.getLastUpdateDate())
            return 1;
        else {
            return 0;
        }
    }*/

}
