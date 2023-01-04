package com.tema2;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Order's Manipulator Thread
 */
public class OrdersThread implements Runnable {

    private final Integer id;
    private final Integer nrThreads;
    private final String ioPath;
    private final ExecutorService itemsService;

    public OrdersThread(Integer id, String ioPath, Integer nrThreads, ExecutorService itemsService) {
        this.ioPath = ioPath;
        this.nrThreads = nrThreads;
        this.id = id;
        this.itemsService = itemsService;
    }

    /**
     * Deletes extra characters from the beginning of the content string, so
     * the returned string contains a valid list of commands.
     * @param content the initial string
     * @return the modified valid string
     */
    private String deleteExtraBeginning(String content) {
        if (content.charAt(0) != 'o' || content.charAt(1) != '_') {
            int i = 0;
            while (content.charAt(i) != '\n') {
                i++;
            }

            return content.substring(i + 1);
        }

        return content;
    }

    /**
     * Gets the according bytes chunk for a specific thread, making sure that
     * orders are not split.
     * @param chunkSize current thread's calculated chunkSize in bytes
     * @param channel
     * @return a String containing a list of orders for current thread
     * @throws IOException
     */
    private String chunkFile(int chunkSize, FileChannel channel) throws IOException {
        int size = chunkSize;

        ByteBuffer buffer = ByteBuffer.allocate(size);
        int bytesRead = channel.read(buffer, (long) id * chunkSize);
        String content = new String(buffer.array(), StandardCharsets.UTF_8);

        while (content.charAt(content.length() - 1) != '\n') {
            size++;
            buffer = ByteBuffer.allocate(size);
            bytesRead = channel.read(buffer, (long) id * chunkSize);
            content = new String(buffer.array(), StandardCharsets.UTF_8);
        }

        return deleteExtraBeginning(content);
    }

    @Override
    public void run() {
        Map<String, Future<?>> ordersStatus = new HashMap<>();
        Map<String, Boolean> ordersWritten = new HashMap<>();

        File file = new File(ioPath + "\\orders.txt");
        Long fileSize = file.length();
        int chunkSize = Math.toIntExact(fileSize / nrThreads);

        try {
            FileInputStream fis = new FileInputStream(file);
            FileChannel channel = fis.getChannel();

            String content = chunkFile(chunkSize, channel);
            String[] orders = content.split("\n");

            for (String order : orders) {
                String[] args = order.split(",");
                Future<?> f = itemsService.submit(new ItemsThread(args[0], Integer.parseInt(args[1]), ioPath));
                ordersStatus.put(order, f);
                ordersWritten.put(order, false);
            }

            channel.close();

            int doneOrders = 0;
            while (doneOrders != ordersStatus.size()) {

                for (Map.Entry<String, Future<?>> order : ordersStatus.entrySet()) {
                    if (!ordersWritten.get(order.getKey()) && order.getValue().isDone()) {
                        // vezi daca trb sync
                        Writer fileWriter = new FileWriter(ioPath + "\\orders_out.txt", true);
                        fileWriter.write(id+order.getKey() + ",shipped\n");
                        fileWriter.close();

                        ordersWritten.replace(order.getKey(), true);

                        doneOrders++;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
