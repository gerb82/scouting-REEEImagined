package utilities.GBSockets;

import java.io.IOException;
import java.nio.channels.Selector;

public class SelectorManager {

    Selector selector;

    public SelectorManager(){
        try{
            selector = Selector.open();
        } catch (IOException e) {
            new IOException("Selector could not be opened", e).printStackTrace();
        }
    }

    private class SelectorThread extends Thread{

        @Override
        public void run() {

        }
    }
}
