package utilities;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class ConsoleManager {

    public static FXMLLoader enableConsole(Class clazz, boolean contained){
        try {
            FXMLLoader loader = new FXMLLoader(LoaderUtil.pathMaker(ConsoleController.class, "fxml"));
            Parent root = loader.load();
            ConsoleController controller = loader.getController();
            if(!contained){
                Stage stage = new Stage();
                stage.setTitle("Console");
                stage.setScene(new Scene(root, 600, 400));
                stage.show();
            }
            final ConsoleController consoleAccessor = controller;
            File logDirectory = new File((LoaderUtil.pathMaker(clazz, "").getPath()+"logs").replace("%20", " "));
            LocalDateTime time = LocalDateTime.now();
            int[] date = new int[]{time.getYear(), time.getMonthValue(), time.getDayOfMonth()};
            int hour = time.getHour();
            int minute = time.getMinute();
            int seconds = time.getSecond();
            for(int i = 0; i<4;){
                if (!logDirectory.exists()) {
                    Files.createDirectory(logDirectory.toPath());
                }
                if(i != 3) {
                    logDirectory = new File(logDirectory.getPath() + File.separator + date[i]);
                }
                i++;
            }
            File log = new File( logDirectory.getPath() + File.separator +"log " + hour + '꞉' + minute + '-' + seconds + ".txt");
            log.createNewFile();
            File crashLog = new File(log.getPath().replace(".txt", " Errors.txt"));
            crashLog.createNewFile();
            class logStream extends FileOutputStream {

                private Paint color;
                private logStream(File file, boolean b, Paint textColor) throws FileNotFoundException {
                    super(file, b);
                    color = textColor;
                }

                @Override
                public void write(byte[] b, int off, int len){
                    byte[] line = new byte[len];
                    System.arraycopy(b, off, line, 0, len);
                    write(line);
                }

                @Override
                public void write(int b){
                    write(new byte[]{((byte)b)});
                }

                @Override
                public void write(byte[] b){
                    consoleAccessor.updateConsole(new String(b), color);
                    try {
                        super.write(b);
                        flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.setOut(new PrintStream(new logStream(log, true, Color.BLACK)));
            System.setErr(new PrintStream(new logStream(crashLog, true, Color.RED)));
            return loader;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
