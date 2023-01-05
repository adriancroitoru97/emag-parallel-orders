import java.io.*;

/**
 * Items Worker Thread.
 */
public class ItemsThread implements Runnable {

    private final String commandId;
    private Integer commandSize;
    private final String inputPath;
    private final PrintStream itemsPrintStream;

    public ItemsThread(String commandId, Integer commandSize,
                       String inputPath, PrintStream itemsPrintStream) {
        this.commandId = commandId;
        this.commandSize = commandSize;
        this.inputPath = inputPath;
        this.itemsPrintStream = itemsPrintStream;
    }

    @Override
    public void run() {
        File inputFile = new File(inputPath + "/order_products.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            String line = br.readLine();
            while (line != null && commandSize > 0) {
                String[] args = line.split(",");
                if (args[0].equals(commandId)) {
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
