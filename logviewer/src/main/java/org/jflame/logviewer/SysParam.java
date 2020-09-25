package org.jflame.logviewer;

import org.jflame.commons.config.ConfigKey;
import org.jflame.commons.config.PropertiesConfigHolder;

public final class SysParam {

    static {
        PropertiesConfigHolder.loadProperties("classpath:system.properties");
    }
    public static String SESSION_CURRENT_USER = "current_user_key";

    public static String TMP_DIR;

    /**
     * 程序监听端口
     * 
     * @return 端口
     */
    public static int getServerPort() {
        int port = 8889;
        String portStr = System.getenv("port");
        if (portStr != null) {
            port = Integer.parseInt(System.getenv("port"));
        } else {
            port = PropertiesConfigHolder.getInt(new ConfigKey<>("server.port", port));
        }
        return port;
    }

    /**
     * 请求应用路径
     * 
     * @return
     */
    public static String getServerContextPath() {
        return PropertiesConfigHolder.getStringOrDefault("server.context", "");
    }

    /**
     * 应用域名
     * 
     * @return
     */
    public static String getServerHost() {
        return PropertiesConfigHolder.getString("server.host");
    }

    public static String getUser() {
        return PropertiesConfigHolder.getString("sys.user");
    }

    public static String getUserpwd() {
        return PropertiesConfigHolder.getString("sys.pwd");
    }
}
