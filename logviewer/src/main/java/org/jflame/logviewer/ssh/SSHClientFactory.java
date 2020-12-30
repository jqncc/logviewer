package org.jflame.logviewer.ssh;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jflame.commons.exception.RemoteAccessException;
import org.jflame.commons.util.IOHelper;
import org.jflame.logviewer.model.Server;

public class SSHClientFactory {

    // 一个服务器一个用户只保持一个shell会话
    private static Map<String,Map<String,SSHClient>> userSSHClientMap = new ConcurrentHashMap<>();
    private static Map<String,Map<String,SFTPClient>> userFtpClientMap = new ConcurrentHashMap<>();

    public static SSHClient getSSHClient(String sessionId, Server s) throws RemoteAccessException {
        Map<String,SSHClient> sshMap = userSSHClientMap.get(sessionId);
        SSHClient client;
        String key = s.getIPAndPort();
        if (sshMap == null) {
            client = new SSHClient(s);
            sshMap = new HashMap<>();
            sshMap.put(key, client);
            userSSHClientMap.put(sessionId, sshMap);
            return client;
        } else {
            client = sshMap.get(key);
            if (client == null) {
                client = new SSHClient(s);
                sshMap.put(key, client);
            }
        }
        return client;
    }

    public static SFTPClient getFtpClient(String sessionId, Server s) throws RemoteAccessException {
        Map<String,SFTPClient> sshMap = userFtpClientMap.get(sessionId);
        SFTPClient client;
        String key = s.getIPAndPort();
        if (sshMap == null) {
            client = new SFTPClient(s);
            sshMap = new HashMap<>();
            sshMap.put(key, client);
            userFtpClientMap.put(sessionId, sshMap);
            return client;
        } else {
            client = sshMap.get(key);
            if (client == null) {
                client = new SFTPClient(s);
                sshMap.put(key, client);
            }
        }
        return client;
    }

    public static void removeUserSSHClient(String sessionId) {
        Map<String,SSHClient> sshMap = userSSHClientMap.get(sessionId);
        if (sshMap != null) {
            sshMap.forEach((k, v) -> {
                IOHelper.closeQuietly(v);
            });
            userSSHClientMap.remove(sessionId);
        }
        Map<String,SFTPClient> sftpMap = userFtpClientMap.get(sessionId);
        if (sftpMap != null) {
            sftpMap.forEach((k, v) -> {
                IOHelper.closeQuietly(v);
            });
            userFtpClientMap.remove(sessionId);
        }
    }

}
