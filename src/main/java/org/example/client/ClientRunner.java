package org.example.client;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class ClientRunner implements Runnable {
    private static final String LOAD_BALANCER_URL = "http://localhost:9090";
    private XmlRpcClient client;
    private Scanner scanner;

    public ClientRunner() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(LOAD_BALANCER_URL));

            client = new XmlRpcClient();
            client.setConfig(config);

            System.out.println("Connected to Load Balancer at " + LOAD_BALANCER_URL);

            while (true) {
                System.out.println("\nChoose an option:\n1. Echo\n2. Add Note\n3. Get Notes\n4. Delete Note\n5. Add Wikipedia Info\n6. Exit");
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 1) {
                    Object[] params = new Object[]{"Hello XMLRPC!"};
                    String res = (String) client.execute("LoadBalancer.routeRequest", new Object[]{"SERVER.echo", params});
                    System.out.println(res);

                } else if (choice == 2) {
                    System.out.print("Enter topic: ");
                    String topic = scanner.nextLine();
                    System.out.print("Enter note: ");
                    String text = scanner.nextLine();
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

                    Object[] params = new Object[]{topic, text, timestamp};
                    String response = (String) client.execute("LoadBalancer.routeRequest", new Object[]{"SERVER.addNote", params});
                    System.out.println(response);

                } else if (choice == 3) {
                    System.out.print("Enter topic: ");
                    String topic = scanner.nextLine();

                    Object[] params = new Object[]{topic};
                    String response = (String) client.execute("LoadBalancer.routeRequest", new Object[]{"SERVER.getNote", params});
                    System.out.println(response);

                } else if (choice == 4) {
                    System.out.print("Enter topic: ");
                    String topic = scanner.nextLine();
                    System.out.print("Enter timestamp of note to delete: ");
                    String timestamp = scanner.nextLine();

                    Object[] params = new Object[]{topic, timestamp};
                    String response = (String) client.execute("LoadBalancer.routeRequest", new Object[]{"SERVER.deleteNote", params});
                    System.out.println(response);

                } else if (choice == 5) {
                    System.out.print("Enter topic to search Wikipedia for: ");
                    String topic = scanner.nextLine();

                    Object[] params = new Object[]{topic};
                    String response = (String) client.execute("LoadBalancer.routeRequest", new Object[]{"SERVER.getWikipediaSuggestions", params});
                    System.out.println(response);

                    System.out.print("Select a topic by number: ");
                    int selectedChoice = scanner.nextInt();
                    scanner.nextLine();

                    String[] lines = response.split("\n");
                    if (selectedChoice <= 0 || selectedChoice >= lines.length) {
                        System.out.println("Invalid selection. Please select a valid number.");
                        continue;
                    }

                    String selectedLink = lines[selectedChoice].split(" - ")[1].trim();
                    System.out.println("Selected link: " + selectedLink);

                    Object[] saveParams = new Object[]{topic, selectedLink};
                    String noteResponse = (String) client.execute("LoadBalancer.routeRequest", new Object[]{"SERVER.saveSelectedLink", saveParams});
                    System.out.println(noteResponse);

                } else {
                    System.out.println("Exiting...");
                    break;
                }
            }
        } catch (XmlRpcException | IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
