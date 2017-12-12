package org.jflame.logviewer.model;

import java.io.Serializable;

public class TomcatInfo implements Serializable {

    private static final long serialVersionUID = 4855339718323131069L;
    private String dir;// tomcat根目录
    private String name;// 名称
    private int port;// 端口
    private String desc;// 描述

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "TomcatInfo [dir=" + dir + ", name=" + name + ", port=" + port + "]";
    }

}
