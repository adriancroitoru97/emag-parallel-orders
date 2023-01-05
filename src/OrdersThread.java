import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Order's Worker Thread.
 */
public class OrdersThread implements Runnable {

    private final Integer id;
    private final Integer nrThreads;
    private final String inputPath;
    private final ExecutorService itemsService;

    private final PrintStream orderPrintStream;
    private final PrintStream itemsPrintStream;

    public OrdersThread(Integer id, String inputPath,
                        Integer nrThreads,
                        ExecutorService itemsService,
                        PrintStream orderPrintStream,
                        PrintStream itemsPrintStream) {
        this.inputPath = inputPath;
        this.nrThreads = nrThreads;
        this.id = id;
        this.itemsService = itemsService;
        this.orderPrintStream = orderPrintStream;
        this.itemsPrintStream = itemsPrintStream;
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
     * the last order is not split in half.
     * @param chunkSize current thread's calculated chunkSize in bytes
     * @param channel the input channel
     * @return a String containing a list of orders for current thread
     * @throws IOException thrown by the input channel
     */
    private String chunkFile(int chunkSize, FileChannel channel) throws IOException {
        int size = chunkSize;

        ByteBuffer buffer = ByteBuffer.allocate(size);
        channel.read(buffer, (long) id * chunkSize);
        String content = new String(buffer.array(), StandardCharsets.UTF_8);

        while (content.charAt(content.length() - 1) != '\n') {
            size++;
            buffer = ByteBuffer.allocate(size);
            channel.read(buffer, (long) id * chunkSize);
            content = new String(buffer.array(), StandardCharsets.UTF_8);
        }

        return deleteExtraBeginning(content);
    }

    @Override
    public void run() {
        Map<String, Future<?>> ordersStatus = new HashMap<>();
        Map<String, Boolean> ordersWritten = new HashMap<>();

        File inputFile = new File(inputPath + "/orders.txt");
        Long fileSize = inputFile.length();
        int chunkSize = Math.toIntExact(fileSize / nrThreads);

        try {
            FileInputStream fis = new FileInputStream(inputFile);
            FileChannel channel = fis.getChannel();

            String content = chunkFile(chunkSize, channel);
            String[] orders = content.split("\n");

            for (String order : orders) {
                String[] args = order.split(",");

                /* Allow only non-empty commands */
                if (Integer.parseInt(args[1]) > 0) {
                    Future<?> f = itemsService.submit(new ItemsThread(
                            args[0], Integer.parseInt(args[1]),
                            inputPath, itemsPrintStream
                    ));
                    ordersStatus.put(order, f);
                    ordersWritten.put(order, false);
                }
            }

            channel.close();

            int doneOrders = 0;
            while (doneOrders != ordersStatus.size()) {
                for (Map.Entry<String, Future<?>> order : ordersStatus.entrySet()) {
                    if (!ordersWritten.get(order.getKey()) && order.getValue().isDone()) {
                        /* Write the shipped order in the output file */
                        orderPrintStream.println(order.getKey() + ",shipped");
                        /* Mark the current order as shipped */
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
