package org.example.server.manager;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class LoadBalancer {
    private static final Logger logger = Logger.getLogger(LoadBalancer.class.getName());

    private static final String[] SERVER_URLS = {
            "http://localhost:3030",
            "http://localhost:3031",
            "http://localhost:3032"
    };

    private static final List<Boolean> serverStatus = new ArrayList<>();
    private static int currentServerIndex = 0;

    static {
        for (int i = 0; i < SERVER_URLS.length; i++) {
            serverStatus.add(true);
        }
    }

    public String routeRequest(String methodName, Object[] params) throws Exception {
        String serverUrl = getNextAvailableServer();
        if (serverUrl == null) {
            logger.info("NO AVAILABLE SERVERS FOR REQUEST: " + methodName);
            return "No available servers!";
        }

        logger.info("ROUTING REQUEST: " + methodName + " TO: " + serverUrl + ".");

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(serverUrl));

        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        try {
            String response = (String) client.execute(methodName, params);
            logger.info("REQUEST: " + methodName + " COMPLETED AT: " + serverUrl + " WITH RESPONSE: " + response );
            return response;
        } catch (Exception e) {
            logger.info("ERROR PROCESSING REQUEST: " + methodName + " AT: " + serverUrl + ".");
            return "Error processing request.";
        }
    }

    private static synchronized String getNextAvailableServer() {
        int attempts = 0;
        while (attempts < SERVER_URLS.length) {
            String serverUrl = SERVER_URLS[currentServerIndex];
            currentServerIndex = (currentServerIndex + 1) % SERVER_URLS.length;

            if (serverStatus.get(currentServerIndex) && isServerHealthy(serverUrl)) {
                return serverUrl;
            }
            attempts++;
        }
        return null;
    }

    private static boolean isServerHealthy(String serverUrl) {
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(serverUrl));

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            Object[] params = new Object[]{"ping"};
            String response = (String) client.execute("SERVER.echo", params);

            boolean isHealthy = "PONG: ping".equals(response);
            if (isHealthy) {
                logger.info("SERVER CHECK: " + serverUrl + " IS HEALTHY.");
            } else {
                logger.info("SERVER CHECK: " + serverUrl + " IS DOWN.");
            }
            return isHealthy;
        } catch (Exception e) {
            logger.info("SERVER UNREACHABLE: " + serverUrl + ".");
            return false;
        }
    }
}
