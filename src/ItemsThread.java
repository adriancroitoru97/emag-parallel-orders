import java.io.*;

public class ItemsThread implements Runnable {

    private final String commandId;
    private Integer commandSize;
    private final String ioPath;
    private final PrintStream itemsPrintStream;

    public ItemsThread(String commandId, Integer commandSize, String ioPath, PrintStream itemsPrintStream) {
        this.commandId = commandId;
        this.commandSize = commandSize;
        this.ioPath = ioPath;
        this.itemsPrintStream = itemsPrintStream;
    }

    @Override
    public void run() {
        File inputFile = new File(ioPath + "/order_products.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            String line = br.readLine();
            while (line != null) {
                if (commandSize == 0) {
                    break;
                }

                String[] args = line.split(",");
                if (args[0].equals(commandId)) {
                    /* Write the shipped item in the output file */
                    itemsPrintStream.println(line + ",shipped");

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
