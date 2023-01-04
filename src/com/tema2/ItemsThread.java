package com.tema2;

import java.io.*;

public class ItemsThread implements Runnable {

    private final String commandId;
    private Integer commandSize;
    private final String ioPath;

    public ItemsThread(String commandId, Integer commandSize, String ioPath) {
        this.commandId = commandId;
        this.commandSize = commandSize;
        this.ioPath = ioPath;
    }

    @Override
    public void run() {
        File inputFile = new File(ioPath + "\\order_products.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            String line = br.readLine();
            while (line != null) {
                if (commandSize == 0) {
                    break;
                }

                String[] args = line.split(",");
                if (args[0].equals(commandId)) {

                    // vezi daca trb sincronizare
                    Writer fileWriter = new FileWriter(ioPath + "\\order_products_out.txt", true);
                    fileWriter.write(line + ",shipped\n");
                    fileWriter.close();

                    commandSize--;
                }

                line = br.readLine();
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
