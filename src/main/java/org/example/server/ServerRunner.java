package org.example.server;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.IOException;

public class ServerRunner {
    //RUN
    private static final Logger logger = Logger.getLogger(ServerRunner.class.getName());
    private static final int MAX_THREADS = 10;

        public static void main(String[] args) {
            int[] ports = {3030, 3031, 3032};

            for (int port : ports) {
                new Thread(() -> startServer(port)).start();
            }

        }

    private static void startServer(int port) {
        try {
            WebServer ws = new WebServer(port);
            XmlRpcServer xmlRpcServer = ws.getXmlRpcServer();

            PropertyHandlerMapping mapping = new PropertyHandlerMapping();
            mapping.addHandler("SERVER", Server.class);
            xmlRpcServer.setHandlerMapping(mapping);

            XmlRpcServerConfigImpl serverConfig = new XmlRpcServerConfigImpl();
            serverConfig.setEnabledForExtensions(true);
            serverConfig.setContentLengthOptional(false);
            xmlRpcServer.setConfig(serverConfig);

            xmlRpcServer.setMaxThreads(MAX_THREADS);

            ws.start();
            logger.info("Server is running at http://localhost:" + port);
        } catch (IOException | XmlRpcException e) {
            e.printStackTrace();
        }
    }
}
