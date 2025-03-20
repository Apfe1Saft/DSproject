package org.example.server;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import org.example.server.manager.LoadBalancer;

public class LoadBalancerServer {
    private static final int LOAD_BALANCER_PORT = 9090;

    public static void main(String[] args) {
        try {
            WebServer webServer = new WebServer(LOAD_BALANCER_PORT);
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            phm.addHandler("LoadBalancer", LoadBalancer.class);
            xmlRpcServer.setHandlerMapping(phm);

            XmlRpcServerConfigImpl serverConfig = new XmlRpcServerConfigImpl();
            serverConfig.setEnabledForExtensions(true);
            xmlRpcServer.setConfig(serverConfig);

            webServer.start();
            System.out.println("Load Balancer running on port: " + LOAD_BALANCER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

