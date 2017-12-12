package org.jflame.logviewer.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.websocket.Session;

import org.jflame.toolkit.exception.RemoteAccessException;
import org.jflame.toolkit.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CmdClient implements Closeable {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private AtomicBoolean isStop = new AtomicBoolean(false);

    /**
     * 执行命令,无持续输出的命令
     * 
     * @param cmd 命令
     * @return
     * @throws RemoteAccessException
     */
    public void exec(final String cmd, final Session session) throws RemoteAccessException {
        stopLastCmdThread();
        StringBuilder buf = new StringBuilder();
        String line = null;
        BufferedReader reader = null;
        try (InputStream stream = doExec(cmd, session)) {
            if (stream != null) {
                reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                while ((line = reader.readLine()) != null) {
                    buf.append(line).append("<br>");
                }
                session.getBasicRemote().sendText(buf.toString());
            }
        } catch (IOException e) {
            logger.error("", e);
        }

    }

    protected abstract InputStream doExec(final String cmd, final Session session) throws RemoteAccessException;

    /**
     * 异步执行命令
     * 
     * @param cmd
     * @param handler
     * @throws RemoteAccessException
     */
    public void execAsync(final String cmd, final Session session) throws RemoteAccessException {
        stopLastCmdThread();
        final InputStream stream = doExec(cmd, session);

        new Thread(new Runnable() {

            public void run() {
                logger.debug(isStop.toString());
                String line;
                isStop.set(false);
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    while (true && !isStop.get()) {
                        while (!isStop.get() && (line = reader.readLine()) != null) {
                            // logger.debug(line);
                            session.getBasicRemote().sendText(line + "<br>");
                        }
                    }
                    logger.debug("thread end");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    isStop.set(false);// 信号复位
                    IOHelper.closeQuietly(reader);
                }
            }

        }).start();
    }

    void stopLastCmdThread() {
        isStop.set(true);
        try {
            Thread.sleep(100);// 暂停,让上个线程退出
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
