import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Tema2 {

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        String inputPath = args[0];
        int nrThreads = Integer.parseInt(args[1]);

        ExecutorService ordersService = Executors.newFixedThreadPool(nrThreads);
        ExecutorService itemsService = Executors.newFixedThreadPool(nrThreads);

        PrintStream orderPrintStream = new PrintStream(new FileOutputStream("orders_out.txt"));
        PrintStream itemsPrintStream = new PrintStream(new FileOutputStream("order_products_out.txt"));

        for (int i = 0; i < nrThreads; i++) {
            ordersService.submit(new OrdersThread(i, inputPath, nrThreads, itemsService, orderPrintStream, itemsPrintStream));
        }

        /* Await all tasks to be done and close the services and output files */
        ordersService.shutdown();
        if(ordersService.awaitTermination(2, TimeUnit.DAYS)) {
            itemsService.shutdown();

            orderPrintStream.close();
            itemsPrintStream.close();
        }
    }

}
