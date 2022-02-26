import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

public class Reindeer extends Thread {

    Office office;
    private final Vector<Factory> factories;
    private volatile Factory source_factory = null;
    Socket socket = null;

    public Reindeer(Office office, Vector<Factory> factories) {
        this.office = office;
        this.factories  = factories; // link to factory information
        this.start();
    }
    // checks factory info
    // takes gifts if possible
    public void run() {
       while(office.factoriesRunning()) {
            readData();
           if(getSourceFactory() != null) {
                transportGifts();
           }
           setSource(null);
     }
    }
    // gets permit, lock, gift list
    // if no gift return
    // gets first gift available
    // release lock, permit
    // gets output stream from socket
    // creates object from output stream we use as transport line
    private void transportGifts() {
       // SimulationParameters.syncOut("xx");
        if(source_factory.RequestTransportAccess()) {
            source_factory.RequestMove();
            Vector<Gift> G = source_factory.getGiftsList();
            if(G.isEmpty()) {
                source_factory.MoveFinished();
                source_factory.TransportFinished();
                return;
            }
            Gift gif = G.remove(0);
            source_factory.MoveFinished();
            SimulationParameters.syncOut("GIFTS SENT TO OFFICE");
            try {
                socket = new Socket("localhost", 7777);
                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(gif);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            source_factory.TransportFinished();
        }
    }
    // checks info from factory and waits between checks
    private void readData() {
        for(int i=0; i<factories.size();i++) {
            try {
                sleep(SimulationParameters.RandomNumberInInterval(SimulationParameters.reindeer_wait_min,SimulationParameters.reindeer_wait_max));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    // sets source for reindeer to transport from
    // returns status
    public boolean announce(Factory F) {
        if(source_factory == null) {
            setSource(F);
            return true;
        }
        return false;
    }
    //synced source factory setter
    private synchronized void setSource(Factory F) {
        source_factory = F;
    }
    private Factory getSourceFactory() {
        return source_factory;
    }
}
