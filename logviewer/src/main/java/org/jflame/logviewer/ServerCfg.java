package org.jflame.logviewer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.SerializationUtils;
import org.jflame.commons.crypto.SymmetricEncryptor;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.XmlBeanHelper;
import org.jflame.commons.util.file.FileHelper;
import org.jflame.logviewer.model.Server;
import org.jflame.logviewer.model.Servers;

public final class ServerCfg {

    private static volatile Servers serverCfgs;
    private static byte[] passwdBytes = { 9,2,3,8,5,3,7,8,9,0,1,2,6,4,1,6 };
    private static final String cfgFile = "cfg.xml";

    public static List<Server> getServers() {
        load();
        return serverCfgs.getServer();
    }

    public static Optional<Server> getServer(String ipport) {
        String[] ipstrArr = ipport.trim().split("_");
        String ip = ipstrArr[0];
        int port = Integer.parseInt(ipstrArr[1]);
        return getServer(ip, port);
    }

    public static Optional<Server> getServer(String ip, int port) {
        return getServers().stream().filter(s -> s.getIp().equals(ip) && s.getPort() == port).findFirst();
    }

    @SuppressWarnings("unchecked")
    public static void load() {
        if (serverCfgs == null) {
            Path cfgPath = FileHelper.toAbsolutePath(cfgFile);
            serverCfgs = XmlBeanHelper.xmlFileToBean(cfgPath, Servers.class);
            List<Server> servers = serverCfgs.getServer();
            if (CollectionHelper.isNotEmpty(servers)) {
                for (Server s : servers) {
                    if (StringHelper.isNotEmpty(s.getPwd())) {
                        s.setPwd(SymmetricEncryptor.aesDencrypt(s.getPwd(), passwdBytes));
                    }
                }
            }
        }
    }

    public static void save() {
        Servers serversCopy = SerializationUtils.clone(serverCfgs);
        List<Server> servers = serversCopy.getServer();
        if (CollectionHelper.isNotEmpty(servers)) {
            for (Server s : servers) {
                if (StringHelper.isNotEmpty(s.getPwd())) {
                    s.setPwd(SymmetricEncryptor.aesEncrypt(s.getPwd(), passwdBytes));
                }
            }
            Path cfgPath = FileHelper.toAbsolutePath(cfgFile);
            XmlBeanHelper.toXmlFile(serversCopy, cfgPath);
        }
    }

}
