package org.jflame.logviewer;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.jflame.logviewer.action.WebShell;
import org.jflame.logviewer.ssh.SSHClientFactory;

@WebListener
public class MySessionListener implements HttpSessionListener {

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        try {
            SSHClientFactory.removeUserSSHClient(session.getId());
            System.out.println("sessionDestroyed clear sshclient:" + session.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            WebShell.close(session.getId());
            System.out.println("sessionDestroyed clear websocket:" + session.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
