package org.jflame.logviewer.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;
import org.jflame.commons.util.StringHelper;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 服务器信息
 * 
 * @author yucan.zhang, Cloneable
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Server implements Serializable, Cloneable {

    private static final long serialVersionUID = 7306324708429221895L;
    private String name;
    private String ip;

    @JSONField(serialize = false)
    private String user;

    @XmlElement
    @JSONField(serialize = false)
    private String pwd;
    /**
     * 日志目录,多个以逗号分隔
     */
    private int port = 22;
    @XmlElement(name = "log_dir")
    private String logDir;
    /**
     * 要排除的日志目录,多个以逗号分隔
     */
    @XmlElement(name = "exclude_log")
    private String excludeLog;

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

    public String getLogDir() {
        return logDir;
    }

    public String[] dirs() {
        if (StringHelper.isNotEmpty(logDir)) {
            return StringUtils.deleteWhitespace(logDir).split(",");
        }
        return null;
    }

    /**
     * 判断给定文件是否可读,只有在配置目录里的文件才能访问
     * 
     * @param file
     * @return
     */
    public boolean isCanRead(String file) {
        if (StringHelper.isEmpty(file)) {
            return false;
        }
        String[] dirs = dirs();
        for (String d : dirs) {
            if (file.startsWith(d)) {
                return true;
            }
        }
        return false;
    }

    public String[] excludeLogs() {
        if (StringHelper.isNotEmpty(excludeLog)) {
            return StringUtils.deleteWhitespace(excludeLog).split(",");
        }
        return null;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getExcludeLog() {
        return excludeLog;
    }

    public void setExcludeLog(String excludeLog) {
        this.excludeLog = excludeLog;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Server [");
        if (name != null) {
            builder.append("name=");
            builder.append(name);
            builder.append(", ");
        }
        if (ip != null) {
            builder.append("ip=");
            builder.append(ip);
            builder.append(", ");
        }
        if (user != null) {
            builder.append("user=");
            builder.append(user);
            builder.append(", ");
        }
        builder.append("port=");
        builder.append(port);
        builder.append(", ");
        if (logDir != null) {
            builder.append("logDir=");
            builder.append(logDir);
        }
        if (excludeLog != null) {
            builder.append("excludeLog=");
            builder.append(excludeLog);
        }
        builder.append("]");
        return builder.toString();
    }

}
