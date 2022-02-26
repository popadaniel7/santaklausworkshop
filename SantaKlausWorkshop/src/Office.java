import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.Semaphore;


public class Office extends Thread{
    Vector<Factory> factories = new Vector<>();
    Vector<Reindeer> reindeer = new Vector<>();
    Vector<Gift> gifts = new Vector<>();
    ServerSocket socket = null;
    int factories_running;
    public static volatile Semaphore elfRetireSemaphore = new Semaphore(0);
    private ElvesRetire elfRetire = new ElvesRetire();
    
    Office() {
        //creates factories
        factories_running = SimulationParameters.number_of_factories;
        for(int i=0; i<SimulationParameters.number_of_factories; i++) {
            factories.add(new Factory(this,i));
        }
        // creates reindeer
        for(int i=0; i<10; i++) {
            reindeer.add(new Reindeer(this,factories));
        }
        // open TCP/IP
        openTransportLine();
        elfRetire.start();
        SimulationParameters.syncOut("OFFICE RUNNING");
    }
    public void run() {
        // run() function which adds elves at random times
        // while there are factories up and running
        // we also add elves with the specified delay
        // in the assignment description
        while(factoriesRunning()) {
            addElves();
            try {
                Thread.sleep(SimulationParameters.RandomNumberInInterval(SimulationParameters.time_elf_min,SimulationParameters.time_elf_max));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        SimulationParameters.syncOut("ALL FACTORIES ARE DONE");
        elfRetire.stopRetirement();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void openTransportLine() {
        // creates a new thread to handle the link between
        // the gifts created by elves and reindeer
        // short description: transports gifts from factories to reindeer
        try {
            socket = new ServerSocket(7777);
            new Thread(this::acceptGifts).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // as long as factories are running
    // we make a connection between reindeer and gifts
    // if the connection is successful, we eventually close the socket
    private void acceptGifts() {
        while(factoriesRunning()) {
            Socket rreindeer;
            try {
                rreindeer = socket.accept();
                InputStream inputStream = rreindeer.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                try {
                    SimulationParameters.syncOut("GIFT RECEIVED");
                    Gift gif = (Gift) objectInputStream.readObject();
                    gifts.add(gif);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                rreindeer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
       }
    }
    private synchronized void addElves() {
        // adding elves
        for(int i=0; i<factories.size(); i++) {
            if(factories.elementAt(i).needsElf())
                factories.elementAt(i).addElf(new Elves(SimulationParameters.GetElfSerial()));
        }
    }
    public void announce(Factory F) {
        // tells reindeer there are gifts ready for delivery
        for(int i=0;i<reindeer.size();i++) {
            reindeer.elementAt(i).announce(F);
        }
    }
    // we call this anytime a factory is done
    public synchronized void announceStop() {
        factories_running--;
    }
    // the program runs as long as we have active factories
    public boolean factoriesRunning() {
        return factories_running != 0;
    }
}
