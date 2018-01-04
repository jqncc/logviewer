package org.jflame.logviewer.action;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.jflame.logviewer.model.Container;
import org.jflame.logviewer.model.Server;
import org.jflame.logviewer.util.CmdClient;
import org.jflame.logviewer.util.Config;
import org.jflame.logviewer.util.LocalClient;
import org.jflame.logviewer.util.SSHClient;
import org.jflame.toolkit.net.IPAddressHelper;
import org.jflame.toolkit.util.IOHelper;
import org.jflame.toolkit.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/realTimeLogViewer")
public class RealTimeLogEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeLogEndpoint.class);

    private Session curSession;// endpoint为多实例,所以不会有多线程安全问题
    private CmdClient cmd;
    private Server server = null;
    private Container appContainer = null;
    private String catalinaLogPath;

    @OnOpen
    public void onOpen(Session session) {
        String ip = session.getRequestParameterMap().get("serverId").get(0);
        String portStr = session.getRequestParameterMap().get("port").get(0);
        if (StringHelper.isEmpty(ip) || StringHelper.isEmpty(portStr)) {
            closeSession(session, CloseCodes.CANNOT_ACCEPT, "参数错误");
            return;
        }
        server = Config.getServerByIp(ip);
        if (server == null) {
            sendMessage(String.format("服务器%s不存在", ip));
            closeSession(session, CloseCodes.CANNOT_ACCEPT, "服务器不存在");
            logger.error("websocket error,server not found {}", ip);
            return;
        }
        int port = Integer.parseInt(portStr);
        appContainer = null;
        for (Container ti : server.getTomcats()) {
            if (ti.getPort() == port) {
                appContainer = ti;
            }
        }
        if (appContainer == null) {
            sendMessage(String.format("服务器%s中端口为%s的应用不存在", ip, portStr));
            closeSession(session, CloseCodes.CANNOT_ACCEPT, "应用不存在");
            logger.error("websocket error,container not found {}:{}", ip, portStr);
            return;
        }
        if (StringHelper.isEmpty(appContainer.getConsole())) {
            catalinaLogPath = Paths.get(appContainer.getDir(), "logs/catalina.out").toString().replace('\\', '/');// windows平台运行路径为\所以替换
        } else {
            catalinaLogPath = appContainer.getConsole();
        }
        try {
            if (IPAddressHelper.getLocalIP().equals(server.getIp())) {
                logger.info("open websocket local client,server:{},container:", server, appContainer);
                cmd = new LocalClient();
            } else {
                logger.info("open websocket ssh client,server:{},container:", server, appContainer);
                cmd = new SSHClient(server);
            }
        } catch (Exception e) {
            logger.error("建立命令终端失败,ip:" + server.getIp(), e);
            sendMessage("与" + ip + "建立连接失败");
        }
        curSession = session;
    }

    /* public static void main(String[] args) {
        Path p = Paths.get("/usr/local/tomcat-7", "logs/catalina.out");
        System.out.println(p.toString());
    }*/

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
        System.out.println(message);
        if (StringHelper.isNotEmpty(message)) {
            Map<String,String> params = StringHelper.buildMapFromUrlParam(message);
            final String cmdParam = params.get("cmd");
            String cmdText;
            if ("tail".equals(cmdParam)) {
                try {
                    if (params.containsKey("line")) {
                        cmdText = "tail -n " + params.get("line") + " " + catalinaLogPath;
                        // System.out.println(cmdText);
                        logger.info(cmdText);
                        cmd.exec(cmdText, curSession);
                    } else {
                        cmdText = "tail -f " + catalinaLogPath;
                        logger.info(cmdText);
                        cmd.execAsync(cmdText, curSession);
                    }
                } catch (Exception e) {
                    logger.error("", e);
                    sendMessage("执行错误:" + e.getMessage());
                }
            } else {
                sendMessage("不支持的命令" + cmdParam);
            }
        }
    }

    @OnClose
    public void onClose(CloseReason cr) {
        // System.out.println(cr.getReasonPhrase());
        if (cmd != null) {
            IOHelper.closeQuietly(cmd);
        }
    }

    void closeSession(Session session, CloseReason.CloseCodes code, String reason) {
        try {
            session.close(new CloseReason(code, reason));
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    void sendMessage(String message) {
        try {
            curSession.getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.error("发送消息失败,内容:" + message, e);
        }
    }

    @OnError
    public void onError(Throwable thr) {
        logger.error("websocket连接错误", thr);
    }

}
