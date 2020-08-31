package org.jflame.logviewer.action;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang3.StringUtils;
import org.jflame.commons.exception.RemoteAccessException;
import org.jflame.commons.util.StringHelper;
import org.jflame.logviewer.ServerCfg;
import org.jflame.logviewer.model.Server;
import org.jflame.logviewer.ssh.AsyncCmdCallBack;
import org.jflame.logviewer.ssh.SSHClient;
import org.jflame.logviewer.ssh.SSHClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/shell.do", configurator = GetHttpSessionConfigurator.class)
public class WebShell {

    private static final Logger logger = LoggerFactory.getLogger(WebShell.class);

    private static CopyOnWriteArrayList<WebShell> webShellList = new CopyOnWriteArrayList<>();
    private Session curSession;
    private String curHttpSessionId;
    private Server curServer;
    private SSHClient curSSHClient;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        String serverIp = session.getRequestParameterMap().get("ip").get(0);
        if (StringHelper.isEmpty(serverIp)) {
            session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "参数错误,关闭连接"));
            return;
        }

        Optional<Server> serverCfg = ServerCfg.getServer(serverIp.trim());
        if (serverCfg.isPresent()) {
            curServer = serverCfg.get();
        } else {
            session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "服务器不存在" + serverIp));
            logger.error("websocket error,server not found {}", serverIp);
            return;
        }
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        // 一个用户只保留一个连接
        for (WebShell w : webShellList) {
            if (w.getCurHttpSessionId().equals(httpSession.getId()) && w.getCurServer().getIp().equals(serverIp)) {
                w.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "重复的ws连接,关闭前一个"));
            }
        }

        curSession = session;
        // 关联HttpSession
        webShellList.add(this);
        curHttpSessionId = httpSession.getId();
        logger.debug("建立websocket连接,httpSessId:{},wsSessId:{}", httpSession.getId(), session.getId());
    }

    private void initSSHClient() {
        if (curSSHClient == null) {
            try {
                curSSHClient = SSHClientFactory.getSSHClient(curHttpSessionId, curServer);
                logger.info("开启远程连接,ip:{},wsid:{},httpSid:{}", curServer.getIp(), curSession.getId(), curHttpSessionId);
            } catch (Exception e) {
                sendMessage("远程连接失败:" + e.getMessage());
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
        if (StringHelper.isNotEmpty(message)) {
            if (message.indexOf("&") > 0 || message.indexOf("||") > 0) {
                logger.warn("拒绝执行命令:{}", message);
                sendMessage("命令不能含& ||特殊字符");
                return;
            }
            initSSHClient();
            String cmd = StringUtils.substringBefore(message, " ");
            if ("tail".equals(cmd) || "tailf".equals(cmd)) {
                try {
                    curSSHClient.execAsync(message, new AsyncCmdCallBack() {

                        @Override
                        public void hanndle(String returnText) {
                            sendMessage(returnText);
                        }
                    });
                } catch (RemoteAccessException e) {
                    sendMessage(cmd + "命令执行错误:" + e.getMessage());
                }
            } else if ("cat".equals(cmd) || "head".equals(cmd)) {
                StringBuffer result = null;
                try {
                    result = curSSHClient.exec(message);
                } catch (RemoteAccessException e) {
                    sendMessage(cmd + "命令执行错误:" + e.getMessage());
                }
                if (result != null && result.length() > 0) {
                    sendMessage(result.toString());
                }
            } else {
                sendMessage("不支持的命令");
            }
        }

    }

    @OnClose
    public void onClose(CloseReason cr) {
        webShellList.remove(this);
        try {
            if (curSSHClient != null) {
                curSSHClient.close();
            }
        } catch (IOException e) {
            logger.error("关闭远程连接异常", e);
        }
        logger.debug("close ws,wsid:{},reason:{}", curSession.getId(), cr != null ? cr.getReasonPhrase() : "");
    }

    public static void close(String sessionId) {
        for (WebShell c : webShellList) {
            if (c.getCurHttpSessionId().equals(sessionId)) {
                c.close();
            }
        }
    }

    public void close() {
        close((CloseReason) null);
    }

    public void close(CloseReason reason) {
        try {
            if (reason != null) {
                curSession.close(reason);
            } else {
                curSession.close();
            }
            webShellList.removeIf(w -> w.getCurHttpSessionId().equals(curHttpSessionId));
        } catch (Exception e) {
            logger.error("", e);
            curSession = null;
        }
    }

    void sendMessage(String message) {
        try {
            // System.out.println(message);
            if (curSession != null && curSession.isOpen()) {
                curSession.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            logger.error("发送消息失败,内容:" + message, e);
        }
    }

    @OnError
    public void onError(Throwable thr) {
        logger.error("websocket连接错误", thr.getMessage());
    }

    protected String getCurHttpSessionId() {
        return curHttpSessionId;
    }

    protected Server getCurServer() {
        return curServer;
    }

}
