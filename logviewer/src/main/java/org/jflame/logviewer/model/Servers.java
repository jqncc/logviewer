package org.jflame.logviewer.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jflame.commons.codec.TranscodeHelper;

@XmlRootElement(name = "servers")
@XmlAccessorType(XmlAccessType.FIELD)
public class Servers implements Serializable {

    private static final long serialVersionUID = 1317905713381199596L;

    @XmlElement
    private List<Server> server;

    public List<Server> getServer() {
        return server;
    }

    public void setServer(List<Server> server) {
        this.server = server;
    }

    public static void main(String[] args) {
        /*Servers cfg = new Servers();
        
        Server s1 = new Server();
        s1.setIp("10.18.200.18");
        s1.setLogDir("/webserver/logs");
        
        Server s2 = new Server();
        s2.setIp("10.18.200.58");
        s2.setLogDir("/webserver/logs");
        
        cfg.setServer(CollectionHelper.newList(s1, s2));
        String xml = XmlBeanHelper.toXml(cfg);
        System.err.println(xml);*/

        // String a = "61,62,63,31,32,4e2d";
        String a = "61";
        System.out.println(TranscodeHelper.dencodeHex(a));
    }
}
