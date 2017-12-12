package org.jflame.logviewer.util;

import java.io.IOException;
import java.io.InputStream;

import javax.websocket.Session;

public interface CmdReadHandler {

    String handle(InputStream inStream, Session session) throws IOException;

}
