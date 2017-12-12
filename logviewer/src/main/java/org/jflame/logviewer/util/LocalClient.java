package org.jflame.logviewer.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.websocket.Session;

import org.jflame.toolkit.exception.RemoteAccessException;
import org.jflame.toolkit.util.IOHelper;
import org.jflame.toolkit.util.StringHelper;

public class LocalClient extends CmdClient {

    @Override
    public void close() throws IOException {
        if (processHoler != null) {
            processHoler.destroy();
        }
    }

    Process processHoler;

    @Override
    protected InputStream doExec(String cmd, Session session) throws RemoteAccessException {
        Process process;
        InputStream inputStream = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            inputStream = process.getErrorStream();
            String errmsg = IOHelper.readText(inputStream, Charset.defaultCharset().name());
            if (StringHelper.isEmpty(errmsg)) {
                processHoler = process;
                return process.getInputStream();
            } else {
                throw new RemoteAccessException("命令执行错误" + errmsg);
            }
        } catch (IOException e) {
            throw new RemoteAccessException(e);
        } finally {
            IOHelper.closeQuietly(inputStream);
        }
    }

    /*  @Override
    public String exec(String cmd) throws RemoteAccessException {
        stopLastCmdThread();
        Process process;
        InputStream inputStream = null;
        String msg;
        try {
            process = Runtime.getRuntime().exec(cmd);
            inputStream = process.getErrorStream();
            msg = IOHelper.readText(inputStream, Charset.defaultCharset().name());
            if (StringHelper.isEmpty(msg)) {
                inputStream.close();
                inputStream = process.getInputStream();
                msg = IOHelper.readText(inputStream, Charset.defaultCharset().name());
            }
        } catch (IOException e) {
            throw new RemoteAccessException(e);
        } finally {
            IOHelper.closeQuietly(inputStream);
        }
        return msg;
    }
    
    @Override
    public void execAsync(final String cmd, final CmdReadHandler handler) throws RemoteAccessException {
        stopLastCmdThread();
        new Thread(new Runnable() {
    
            public void run() {
                Process process;
                InputStream inputStream = null;
                String msg;
                BufferedReader reader = null;
                isStop.set(false);
                try {
                    process = Runtime.getRuntime().exec(cmd);
                    inputStream = process.getErrorStream();
                    msg = IOHelper.readText(inputStream, Charset.defaultCharset().name());
                    if (StringHelper.isNotEmpty(msg)) {
                        throw new RuntimeException("命令执行有错:" + msg);
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    if (handler != null) {
                        String line;
                        while (true && !isStop.get()) {
                            while (!isStop.get() && (line = reader.readLine()) != null) {
                                handler.handle(line);
                            }
                        }
                    }
    
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    isStop.set(false);
                    IOHelper.closeQuietly(inputStream);
                }
            }
        }).start();
    }
    
    void stopLastCmdThread() {
        isStop.set(true);
        try {
            Thread.sleep(200);// 暂停,让上个线程退出
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/
}
