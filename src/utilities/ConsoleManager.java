package utilities;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class ConsoleManager {

    public static FXMLLoader enableConsole(Class clazz, boolean contained){
        FXMLLoader loader = null;
        Parent root;
        ConsoleController controller = null;
        try {
            loader = new FXMLLoader(LoaderUtil.pathMaker(ConsoleController.class, "fxml"));
            System.out.println(loader.getLocation());
            root = loader.load();
            controller = loader.getController();
            if(!contained){
                Stage stage = new Stage();
                stage.setTitle("Console");
                stage.setScene(new Scene(root, 600, 400));
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        final ConsoleController consoleAccessor = controller;
        File logDirectory = new File((LoaderUtil.pathMaker(clazz, "").getPath()+"logs").replace("%20", " "));
        File log = null;
        try {
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
            log = new File( logDirectory.getPath() + File.separator +"log " + hour + '꞉' + minute + '-' + seconds + ".txt");
            log.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            System.setOut(new PrintStream(new FileOutputStream(log, true) {

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
                    consoleAccessor.updateConsole(new String(b));
                    try {
                        super.write(b);
                        flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }));
            System.setErr(System.out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return loader;
    }
}
