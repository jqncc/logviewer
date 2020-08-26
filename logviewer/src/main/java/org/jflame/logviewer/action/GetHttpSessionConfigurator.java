package org.jflame.logviewer.action;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.jflame.commons.exception.BusinessException;
import org.jflame.commons.model.CallResult.ResultEnum;
import org.jflame.logviewer.SysParam;

public class GetHttpSessionConfigurator extends Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if (httpSession == null || httpSession.getAttribute(SysParam.SESSION_CURRENT_USER) == null) {
            throw new BusinessException(ResultEnum.NO_AUTH);
        }
        config.getUserProperties().put(HttpSession.class.getName(), httpSession);
    }
}
