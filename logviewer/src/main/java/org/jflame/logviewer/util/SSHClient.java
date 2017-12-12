package org.jflame.logviewer.util;

import java.io.IOException;
import java.io.InputStream;

import org.jflame.logviewer.model.Server;
import org.jflame.toolkit.exception.RemoteAccessException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHClient extends CmdClient {

    private static final int DEFAULT_PORT = 22;
    private final int SESSION_TIMEOUT = 60000;
    private JSch jsch = null;
    private Session session = null;

    public SSHClient(Server serverInfo) {
        jsch = new JSch();
        try {
            session = jsch.getSession(serverInfo.getUser(), serverInfo.getIp(), DEFAULT_PORT);
            session.setPassword(serverInfo.getPwd());
            // session.setUserInfo(new MyUserInfo());
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setServerAliveInterval(10000);
            session.connect(SESSION_TIMEOUT);
        } catch (JSchException e) {
            throw new RuntimeException("", e);
        }
    }

    // private ChannelExec channelHolder = null;
    /* private AtomicBoolean isStop = new AtomicBoolean(false);
    
    public String exec(String cmd) throws RemoteAccessException {
        stopLastCmdThread();
        ChannelExec channelExec = null;
        StringBuilder buf = new StringBuilder();
        String line = null;
        BufferedReader reader = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(cmd);
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);
            InputStream in = channelExec.getInputStream();
            channelExec.connect();
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            while ((line = reader.readLine()) != null) {
                buf.append(line).append("<br>");
            }
            // isStop.set(false);
        } catch (Exception e) {
            throw new RemoteAccessException("执行命令异常:" + cmd, e);
        } finally {
            IOHelper.closeQuietly(reader);
            channelExec.disconnect();
        }
        return buf.toString();
    }
    
    private static final Logger logger = LoggerFactory.getLogger(SSHClient.class);
    
    public void execAsync(final String cmd, final CmdReadHandler handler) throws RemoteAccessException {
        stopLastCmdThread();
        new Thread(new Runnable() {
    
            public void run() {
                ChannelExec channelExec = null;
                BufferedReader reader = null;
                isStop.set(false);
                try {
                    channelExec = (ChannelExec) session.openChannel("exec");
                    channelExec.setCommand(cmd);
                    channelExec.setInputStream(null);
                    channelExec.setErrStream(System.err);
                    InputStream in = channelExec.getInputStream();
                    channelExec.connect();
                    reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    if (handler != null) {
                        String line;
                        logger.debug(isStop.toString());
                        while (true && !isStop.get()) {
                            logger.debug("=====");
                            while (!isStop.get() && (line = reader.readLine()) != null) {
                                logger.debug(line);
                                handler.handle(line);
                            }
                            if (channelExec.isClosed()) {
                                int res = channelExec.getExitStatus();
                                System.out.println(String.format("Exit-status: %d,thread:%s", res,
                                        Thread.currentThread().getId()));
                                break;
                            }
                        }
                        logger.debug("thread end");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    isStop.set(false);// 信号复位
                    IOHelper.closeQuietly(reader);
                    channelExec.disconnect();
                }
            }
        }).start();
    }
    
    **/
    public void close() throws IOException {
        if (channelHolder != null) {
            channelHolder.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    private ChannelExec channelHolder = null;

    @Override
    protected InputStream doExec(String cmd, javax.websocket.Session socketSession) throws RemoteAccessException {
        if (channelHolder != null && channelHolder.isConnected()) {
            channelHolder.disconnect();
        }
        ChannelExec channelExec = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(cmd);
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);
            InputStream in = channelExec.getInputStream();
            channelExec.connect();
            channelHolder = channelExec;
            return in;
        } catch (JSchException | IOException e) {
            throw new RemoteAccessException(e);
        }

    }

    /* private static class MyUserInfo implements UserInfo {
    
        @Override
        public String getPassphrase() {
            return null;
        }
    
        @Override
        public String getPassword() {
            return null;
        }
    
        @Override
        public boolean promptPassword(String s) {
            return false;
        }
    
        @Override
        public boolean promptPassphrase(String s) {
            return true;
        }
    
        @Override
        public boolean promptYesNo(String s) {
            return true;
        }
    
        @Override
        public void showMessage(String s) {
        }
    }*/
}
