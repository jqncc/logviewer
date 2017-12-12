package org.jflame.logviewer.model;

import java.io.Serializable;
import java.util.Arrays;

public class ProjLogInfo implements Serializable {

    private static final long serialVersionUID = -3729723896376107348L;
    private String projName;
    private String projId;
    private String[] logPaths;
    private String nameFilter;

    public String getProjName() {
        return projName;
    }

    public void setProjName(String projName) {
        this.projName = projName;
    }

    public String[] getLogPaths() {
        return logPaths;
    }

    public void setLogPaths(String[] logPaths) {
        this.logPaths = logPaths;
    }

    public String getProjId() {
        return projId;
    }

    public void setProjId(String projId) {
        this.projId = projId;
    }

    public String getNameFilter() {
        return nameFilter;
    }

    public void setNameFilter(String nameFilter) {
        this.nameFilter = nameFilter;
    }

    @Override
    public String toString() {
        return "ProjLogInfo [projName=" + projName + ", projId=" + projId + ", logPaths=" + Arrays.toString(logPaths)
                + ", nameFilter=" + nameFilter + "]";
    }

}
