import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Endpoint of the project.
 */
public class Tema2 {

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        String inputPath = args[0];
        int maxThreads = Integer.parseInt(args[1]);

        ExecutorService ordersService = Executors.newFixedThreadPool(maxThreads);
        ExecutorService itemsService = Executors.newFixedThreadPool(maxThreads);

        PrintStream orderPrintStream =
                new PrintStream(new FileOutputStream("orders_out.txt"));
        PrintStream itemsPrintStream =
                new PrintStream(new FileOutputStream("order_products_out.txt"));

        for (int i = 0; i < maxThreads; i++) {
            ordersService.submit(
                    new OrdersThread(
                            i, inputPath, maxThreads, itemsService,
                            orderPrintStream, itemsPrintStream
                    )
            );
        }

        ordersService.shutdown();
        if(ordersService.awaitTermination(2, TimeUnit.DAYS)) {
            itemsService.shutdown();

            orderPrintStream.close();
            itemsPrintStream.close();
        }
    }
}
