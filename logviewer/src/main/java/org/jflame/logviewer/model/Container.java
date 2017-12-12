package org.jflame.logviewer.model;

import java.io.Serializable;

public class Container implements Serializable {

    private static final long serialVersionUID = 4855339718323131069L;
    private String dir;// 根目录
    private String name;// 名称
    private String desc;// 描述
    private String console;
    private int port;// 端口

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

    public String getConsole() {
        return console;
    }

    public void setConsole(String console) {
        this.console = console;
    }

    @Override
    public String toString() {
        return "Container [dir=" + dir + ", name=" + name + ", desc=" + desc + ", console=" + console + ", port=" + port
                + "]";
    }

}
