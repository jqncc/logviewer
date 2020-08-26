package org.jflame.logviewer.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.jflame.commons.exception.RemoteAccessException;
import org.jflame.commons.model.Chars;
import org.jflame.commons.util.IOHelper;
import org.jflame.logviewer.model.Server;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

public class SSHClient extends BaseJchClient {

    public SSHClient(Server serverInfo) throws RemoteAccessException {
        super(serverInfo);
    }

    private ChannelExec getChannel(String cmd) throws RemoteAccessException {
        conn();
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(cmd);
            channel.setInputStream(null);
            channel.setErrStream(System.err);
            return channel;
        } catch (JSchException e) {
            throw new RemoteAccessException(e);
        }
    }

    /**
     * 执行单条命令
     * 
     * @param cmd
     * @return StringBuffer命令结果
     * @throws RemoteAccessException
     */
    public StringBuffer exec(String cmd) throws RemoteAccessException {
        String line = null;
        BufferedReader reader = null;
        ChannelExec channelExec = null;
        StringBuffer resultBuf = new StringBuffer();
        InputStream inStream = null;
        try {
            channelExec = getChannel(cmd);
            stopLastCmdThread();// 中断前一个异步命令的执行
            inStream = channelExec.getInputStream();
            channelExec.connect();
            if (inStream != null) {
                reader = IOHelper.toBufferedReader(inStream, StandardCharsets.UTF_8.name());
                while ((line = reader.readLine()) != null) {
                    resultBuf.append(line).append(Chars.LF);
                }
            }
        } catch (IOException | JSchException e) {
            logger.error("执行命令异常,ip:{},cmd:{},ex:{}", serverInfo.getIp(), cmd, e);
            throw new RemoteAccessException(e);
        } finally {
            IOHelper.closeQuietly(inStream);
            if (channelExec != null) {
                channelExec.disconnect();
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{}执行命令:{},结果:{}", serverInfo.getIp(), cmd, resultBuf);
        }
        return resultBuf;
    }

    ExecAsyncCmdThread cmdThread = null;

    /**
     * 异步执行命令
     * 
     * @param cmd
     * @param handler
     * @throws RemoteAccessException
     */
    public void execAsync(final String cmd, final AsyncCmdCallBack callBack) throws RemoteAccessException {
        ChannelExec channelExec = getChannel(cmd);
        stopLastCmdThread();
        cmdThread = new ExecAsyncCmdThread(channelExec, callBack);
        cmdThread.start();
    }

    class ExecAsyncCmdThread extends Thread {

        private AsyncCmdCallBack callBack;
        private ChannelExec channelExec = null;
        private boolean isRunning = false;

        public ExecAsyncCmdThread(final ChannelExec channelExec, AsyncCmdCallBack callBack) {
            this.channelExec = channelExec;
            this.callBack = callBack;
        }

        @Override
        public void run() {
            InputStream inStream = null;
            String line;
            BufferedReader reader = null;
            channelExec.setPty(true);
            try {
                inStream = channelExec.getInputStream();
                channelExec.connect();
                isRunning = true;
                reader = IOHelper.toBufferedReader(inStream, StandardCharsets.UTF_8.name());
                while (isRunning) {
                    while ((line = reader.readLine()) != null) {
                        callBack.hanndle(line);
                    }
                    if (channelExec.isClosed()) {
                        int res = channelExec.getExitStatus();
                        isRunning = false;
                        System.out.println(
                                String.format("Exit-status: %d,thread:%s", res, Thread.currentThread().getId()));
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("", e);
            } finally {
                IOHelper.closeQuietly(reader);
                if (channelExec != null && channelExec.isConnected()) {
                    channelExec.disconnect();
                }
                isRunning = false;
            }
        }

        public void close() {
            channelExec.disconnect();
            isRunning = false;
        }

    }

    void stopLastCmdThread() {
        if (cmdThread != null) {
            cmdThread.close();
        }
    }

    @Override
    public void close() throws IOException {
        stopLastCmdThread();
        cmdThread = null;
        super.close();
    }

}
