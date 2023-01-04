package com.tema2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        String inputPath = args[0];
        int nrThreads = Integer.parseInt(args[1]);

        ExecutorService ordersService = Executors.newFixedThreadPool(nrThreads);
        ExecutorService itemsService = Executors.newFixedThreadPool(nrThreads);

        for (int i = 0; i < nrThreads; i++) {
            ordersService.submit(new OrdersThread(i, inputPath, nrThreads, itemsService));
        }

        ordersService.shutdown();
        itemsService.shutdown();
    }
}
