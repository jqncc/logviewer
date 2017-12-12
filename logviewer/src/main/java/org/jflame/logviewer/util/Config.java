package org.jflame.logviewer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jflame.logviewer.model.ProjLogInfo;
import org.jflame.logviewer.model.Server;
import org.jflame.toolkit.crypto.SymmetricEncryptor;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.JsonHelper;
import org.jflame.toolkit.util.StringHelper;

import com.alibaba.fastjson.JSON;

@SuppressWarnings("unchecked")
public final class Config {

    public static List<Server> activeWebServers;

    public static String LOG_BASE_FOLDER;

    public static List<ProjLogInfo> PROJ_INFOS;
    private static final String PROJ_CONFIG_FILE = "proj.json";

    public static List<Server> SERVER_INFOS;
    private static final String SERVER_CONFIG_FILE = "server.json";

    public static String SESSION_CURRENT_USER = "current_user_key";
    public static String user = "loger";
    public static String pwd = "c60c2f6cd30e85f6a1333271eb257313f4f5143a";// viewer17
    private static byte[] passwd = "1234560123456789".getBytes();

    static {
        try (InputStream jsonStream = FileHelper.readFileFromClassPath(PROJ_CONFIG_FILE)) {
            PROJ_INFOS = (List<ProjLogInfo>) JSON.parseObject(jsonStream,
                    JsonHelper.buildListType(ProjLogInfo.class).getType());
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件失败:" + PROJ_CONFIG_FILE);
        }
        try (InputStream jsonStream = FileHelper.readFileFromClassPath(SERVER_CONFIG_FILE)) {
            SERVER_INFOS = (List<Server>) JSON.parseObject(jsonStream,
                    JsonHelper.buildListType(Server.class).getType());
            if (CollectionHelper.isNotEmpty(SERVER_INFOS)) {
                for (Server server : SERVER_INFOS) {
                    try {
                        if (StringHelper.isNotEmpty(server.getPwd())) {
                            System.out.println(server.getPwd());
                            server.setPwd(Config.dencrypt(server.getPwd()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("读取配置文件失败:" + SERVER_CONFIG_FILE);
        }
    }

    public static ProjLogInfo getProjById(String projId) {
        for (ProjLogInfo proj : PROJ_INFOS) {
            if (proj.getProjId().equals(projId)) {
                return proj;
            }
        }
        return null;
    }

    public static Server getServerByIp(String ip) {
        for (Server server : SERVER_INFOS) {
            if (server.getIp().equals(ip)) {
                return server;
            }
        }
        return null;
    }

    public static String encrypt(String text) {
        return SymmetricEncryptor.aesEncrypt(text, passwd);
    }

    public static String dencrypt(String text) {
        return SymmetricEncryptor.aesDencrypt(text, passwd);
    }

    public static void main(String[] args) {
        System.out.println(dencrypt("prAp6m70BCX3RChjCPUDXA"));
    }

}
