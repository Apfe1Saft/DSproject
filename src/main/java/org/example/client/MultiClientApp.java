package org.example.client;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
public class MultiClientApp {
    private static final String LOAD_BALANCER_URL = "http://localhost:9090";

    public static void main(String[] args) {
        int numThreads = 7;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        for (int i = 0; i < numThreads; i++) {
            int finalI = i;
            executor.execute(() -> {
                try {
                    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                    config.setServerURL(new URL(LOAD_BALANCER_URL));

                    XmlRpcClient client = new XmlRpcClient();
                    client.setConfig(config);

                    String threadName = Thread.currentThread().getName();
                    String requestTime, responseTime;

                    requestTime = sdf.format(new Date());
                    System.out.println(threadName + " - Sending Echo Request at: " + requestTime);

                    Object[] echoParams = new Object[]{"Test from thread " + finalI};
                    String echoResponse = (String) client.execute("LoadBalancer.routeRequest", new Object[]{"SERVER.echo", echoParams});

                    responseTime = sdf.format(new Date());
                    System.out.println(threadName + " - Echo Response: " + echoResponse + " (Received at: " + responseTime + ")");

                    String topic = "TestTopic_" + finalI;
                    String text = "This is a test note";
                    String timestamp = sdf.format(new Date());

                    requestTime = sdf.format(new Date());
                    System.out.println(threadName + " - Sending Add Note Request at: " + requestTime);

                    Object[] addNoteParams = new Object[]{topic, text, timestamp};
                    String addNoteResponse = (String) client.execute("LoadBalancer.routeRequest", new Object[]{"SERVER.addNote", addNoteParams});

                    responseTime = sdf.format(new Date());
                    System.out.println(threadName + " - Add Note Response: " + addNoteResponse + " (Received at: " + responseTime + ")");

                    requestTime = sdf.format(new Date());
                    System.out.println(threadName + " - Sending Get Notes Request at: " + requestTime);

                    Object[] getNotesParams = new Object[]{topic};
                    String getNoteResponse = (String) client.execute("LoadBalancer.routeRequest", new Object[]{"SERVER.getNote", getNotesParams});

                    responseTime = sdf.format(new Date());
                    System.out.println(threadName + " - Get Notes Response: " + getNoteResponse + " (Received at: " + responseTime + ")");

                } catch (Exception e) {
                    System.err.println(Thread.currentThread().getName() + " - ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                System.err.println("Timeout: Some threads didn't finish in time.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Executor shutdown interrupted.");
            executor.shutdownNow();
        }
    }
}
