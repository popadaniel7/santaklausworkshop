import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Factory extends Thread{
    ReentrantLock move_lock = new ReentrantLock(true);
    Office office;
    Vector<Elves> elves = new Vector<>();
    Vector<Gift> gifts = new Vector<>();
    Semaphore semaphore = new Semaphore(10,true);
    int map_size;
    int[][] map;
    int code;
    int no_toys_created = 0;
    int no_toys_needed;
    private final ReentrantLock factoryLock = new ReentrantLock();
    private final ReentrantLock elvesListLock = new ReentrantLock();

    public Factory(Office office, int code) {
        // creates the factory's map
        // sets a minimum of toys where it should start
        // new thread for handling positions of elves
        this.office = office;
        map_size = SimulationParameters.RandomNumberInInterval(SimulationParameters.factories_map_size_min, SimulationParameters.factories_map_size_max);
        map = new int[map_size][map_size];
        this.code = code;
        no_toys_needed = SimulationParameters.RandomNumberInInterval(SimulationParameters.no_gifts_needed_min, SimulationParameters.no_gifts_needed_max);
        new Thread(this::RequestPositions).start();
        this.start();
    }
    public void run() {
        // tells office there are enough gifts for delivery
        while(factoryRunning()) {
            if(gifts.size() > SimulationParameters.no_gifts_announced) {
                office.announce(this);
            }
        }

        // enough gifts <=> stop elves
        stopElves();


        while(!gifts.isEmpty()) {
            office.announce(this);
        }

        // tells factory done
        SimulationParameters.syncOut("FACTORY: "+code+" CREATED ALL THE TOYS");
        office.announceStop();
    }
    // adds elf
    // lock until elf placed
    // elf placed random
    public void addElf(Elves elf) {
        int x;
        int y;
        move_lock.lock();
        do {
            x = SimulationParameters.RandomNumberInInterval(0, map_size);
            y = SimulationParameters.RandomNumberInInterval(0, map_size);
        } while (map[x][y] != 0);
        map[x][y] = 1;
        move_lock.unlock();
        elf.Assign(this,x,y);
        elves.add(elf);
        elf.start();
        SimulationParameters.syncOut("ELF STARTED");
    }
    public void RequestMove() {
        move_lock.lock();
    }
    // checks if direction is valid
    // if direction valid
    // elf moves
    // no sync for this function
    // elf locks the lock before calling
    // if elf wants to move
    // checks all directions before it says it cannot move
    // function synced <=> elves could move without being able to move
    public boolean RequestMoveDirection(int x,int y,int i) {
        int val = -1;
        switch (i) {
            case 0 -> {
                if (x + 1 >= map_size)
                    break;
                val = map[x + 1][y];
            }
            case 1 -> {
                if (x - 1 < 0)
                    break;
                val = map[x - 1][y];
            }
            case 2 -> {
                if (y + 1 >= map_size)
                    break;
                val = map[x][y + 1];
            }
            case 3 -> {
                if (y - 1 < 0)
                    break;
                val = map[x][y - 1];
            }
        }
        if(val == 0) {
            switch (i) {
                case 0 -> map[x + 1][y]++;
                case 1 -> map[x - 1][y]++;
                case 2 -> map[x][y + 1]++;
                case 3 -> map[x][y - 1]++;
            }
            map[x][y]--;
            return true;
        }
        return false;
    }
    public void MoveFinished() {
        move_lock.unlock();
    }
    // elf moves toy;
    // toy moved -> prints in console at which factory was created
    public void GiveToy(Gift toy) {
        move_lock.lock();
        gifts.add(toy);
        no_toys_created++;
        move_lock.unlock();
        SimulationParameters.syncOut("GIFT CREATED AT FACTORY "+code);
    }
    // checking pos at random times
    public void RequestPositions() { 
        while(factoryRunning()) {
            try{
                move_lock.lock();
                for(int i = 0; i< elves.size(); i++) {
                    elves.elementAt(i).GetPosition();
                }
            }
            finally {
                move_lock.unlock();
            }
            try {	
                sleep(SimulationParameters.RandomNumberInInterval(SimulationParameters.time_check_pos_min,SimulationParameters.time_check_pos_max));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    // condition for factory running
    private boolean factoryRunning() {
        return no_toys_needed > no_toys_created; 
    }
    // if there are a lot of elves
    // return the answer
    public boolean needsElf() {
        return elves.size() < map_size / 2 && factoryRunning();
    }
    // factory number
    public int getCode() {
        return code;		
    }
    // used by reindeer
    public Vector<Gift> getGiftsList() {
        return gifts;
    }
    //stops elves
    private void stopElves() { 
        for(int i=0; i<elves.size(); i++){
            elves.elementAt(i).stopProduction();
        }
    }
    // gets access
    public boolean RequestTransportAccess() {
        return semaphore.tryAcquire();
    }
    // tells transport finished
    public void TransportFinished() {
        semaphore.release();
    }
    public void retireElf(Elves elf) {
        try {
            // change elves list and map
            elvesListLock.lock();
            factoryLock.lock();
            elves.remove(elf);
            int X = elf.getX();
            int Y = elf.getY();
            map[X][Y] = 0;

            System.out.println("ELF " + SimulationParameters.GetElfSerial() + " RETIRED FROM FACTORY " + code);
        }finally {
            elvesListLock.unlock();
            factoryLock.unlock();
        }
    }
}
