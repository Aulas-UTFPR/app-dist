package socket.webserver;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class LogUtil {
    private static final String FILE_LOG = "WebServerLogs.txt";
    private static List<String> logs = new LinkedList<String>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
    /**
     * Write log to local storage list
     * @param log, the content of the log
     */
    public static void write(String log) {
        write(log, true);
    }
 
    /**
     * Write log to local storage list
     * @param log, the content of the log
     * @param print, print to screen
     */
    public static void write(String log, boolean print) {
        String message = sdf.format(new Date()) + " " + log;
        // Store new log
        logs.add(message);
 
        if(print) {
            // Print the log
            System.out.println(message);
        }
    }
 
    /**
     * Save logs to the specified file
     * @param append, true is append, false is override
     */
    public static void save(boolean append) {
        try {
            if (logs!=null && logs.size()>0) {
 
                // Open the log
                FileWriter fileWriterLog = new FileWriter(FILE_LOG, append);
 
                // User BufferedWriter to add new line
                BufferedWriter bufferedWriterLog = new BufferedWriter(fileWriterLog);
 
                // Go through all the logs and write them to the file
                for (String str : logs) {
                    // Write the current log
                    bufferedWriterLog.write(str);
                    // One log one line
                    bufferedWriterLog.newLine();
                }
 
                // Always close files.
                bufferedWriterLog.close();
            }
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + FILE_LOG + "'");
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }
 
 
    /**
     * Clear log
     */
    public static void clear() {
        logs.clear();
    }
 
}