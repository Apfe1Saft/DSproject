package org.example.client;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Client {

    private XmlRpcClient client;


    public Client(String serverUrl) throws  IOException {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(serverUrl));
        this.client = new XmlRpcClient();
        this.client.setConfig(config);
    }

    public String echo(String message) throws XmlRpcException {
        return (String) client.execute("SERVER.echo", List.of(message));
    }

    public String addNote(String topic, String text, String timestamp) throws XmlRpcException {
        return (String) client.execute("SERVER.addNote", List.of(topic, text, timestamp));
    }

    public String getNotes(String topic) throws XmlRpcException {
        return (String) client.execute("SERVER.getNote", List.of(topic));
    }

    public String deleteNote(String topic, String timestamp) throws XmlRpcException {
        return (String) client.execute("SERVER.deleteNote", List.of(topic, timestamp));
    }

    public String getWikipediaSuggestions(String topic) throws XmlRpcException {
        return (String) client.execute("SERVER.getWikipediaSuggestions", List.of(topic));
    }

    public String saveSelectedLink(String topic, String selectedLink) throws XmlRpcException {
        return (String) client.execute("SERVER.saveSelectedLink", List.of(topic, selectedLink));
    }
}
