package org.jflame.logviewer.ssh;

import java.io.Closeable;
import java.io.IOException;

import org.jflame.commons.exception.BusinessException;
import org.jflame.commons.exception.RemoteAccessException;
import org.jflame.commons.util.StringHelper;
import org.jflame.logviewer.model.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public abstract class BaseJchClient implements Closeable {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final static int DEFAULT_CONN_TIMEOUT = 60000;
    protected Session session = null;
    protected Server serverInfo;

    public BaseJchClient(Server serverInfo) throws RemoteAccessException {
        this.serverInfo = serverInfo;
        if (StringHelper.isEmpty(serverInfo.getPwd())) {
            throw new RemoteAccessException("请输入密码");
        }
    }

    public Session newSession(Server serverInfo) throws BusinessException, RemoteAccessException {
        try {
            JSch jsch = new JSch();
            Session newSession = jsch.getSession(serverInfo.getUser(), serverInfo.getIp(), serverInfo.getPort());
            newSession.setPassword(serverInfo.getPwd());
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            newSession.setConfig(config);
            newSession.setServerAliveInterval(10000);
            return newSession;
        } catch (JSchException e) {
            if ("Auth fail".equals(e.getMessage()) || "Auth cancel".equals(e.getMessage())) {
                throw new RemoteAccessException("远程连接用户验证失败", 4001);
            }
            throw new RemoteAccessException(e);
        }
    }

    public void conn() throws RemoteAccessException {
        try {
            if (session == null) {
                session = newSession(serverInfo);
            }
            if (!session.isConnected()) {
                session.connect(DEFAULT_CONN_TIMEOUT);
            }
        } catch (JSchException e) {
            logger.error("ftp连接异常,ip:{},ex:{}", serverInfo.getIp(), e);
            throw new RemoteAccessException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        session = null;
        logger.debug("远程连接关闭,ip={}", serverInfo.getIp());
    }
}
