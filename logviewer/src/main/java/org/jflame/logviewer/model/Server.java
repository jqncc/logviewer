package org.jflame.logviewer.model;

import java.io.Serializable;
import java.util.Set;

/**
 * 服务器信息
 * 
 * @author yucan.zhang
 */
public class Server implements Serializable {

    private static final long serialVersionUID = -7081434072194904353L;
    private String name;
    private String ip;
    private String desc;
    private String user;
    private String pwd;
    private Set<Container> tomcats;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Set<Container> getTomcats() {
        return tomcats;
    }

    public void setTomcats(Set<Container> tomcats) {
        this.tomcats = tomcats;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "Server [name=" + name + ", ip=" + ip + ", desc=" + desc + ", user=" + user + "]";
    }

}
