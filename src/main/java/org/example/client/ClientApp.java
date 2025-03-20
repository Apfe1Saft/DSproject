package org.example.client;

public class ClientApp {

    public static void main(String[] args) {

        new Thread(new ClientRunner()).start();
    }
}
