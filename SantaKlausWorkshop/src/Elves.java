import java.util.Random;

public class Elves extends Thread {
    int x;
    int y;
    private Factory factory=null;
    boolean toys_needed;
    private final int serial;
    int[] directions;
    Elves(int serial){
        this.serial = serial;
        toys_needed = true;
        directions = new int[4];
        directions[0] = 0;
        directions[1] = 1;
        directions[2] = 2;
        directions[3] = 3;
    }
    public void Assign(Factory factory,int x, int y) {
        this.factory = factory; // link between elves and factories
        this.x = x;
        this.y = y;
    }
    // elf moves
    // if it moves creates gift
    // the gift is given to factory
    // if gift created rest
    // if not created wait
    // we also retire elves here for task 1
    public void run(){
        boolean moved;
        while(toys_needed) {
            moved = tryToMove();
            if(moved){
                Gift toy = new Gift(factory.getCode(),this.serial,SimulationParameters.GetGiftsSerial());
                factory.GiveToy(toy);
                SimulationParameters.syncOut("ELF "+ serial+ " CREATES TOY AT POSITION {"+x+","+y+"}");
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                sleep(SimulationParameters.RandomNumberInInterval(1000,5000));
                if(Office.elfRetireSemaphore.tryAcquire()) {
                    factory.retireElf(this);
                    break;}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
          }
        }



    // elf moves in random places
    // tries to move in each direction if possible
    // prints which elf did a toy
    private boolean tryToMove() {
        shuffleDirections();
        boolean moved = false;
        try{
            factory.RequestMove();
            for(int i = 0 ; i < 4 ; i++) {
                if(factory.RequestMoveDirection(x,y,directions[i])) {
                    Move(directions[i]);
                    moved = true;

                    break;
                }
            }
        }
        finally {
            factory.MoveFinished();
        }
        return moved;
    }

    // update coordinates used
    private void Move(int i) {
        switch (i) {
            case 0 -> x++;
            case 1 -> x--;
            case 2 -> y++;
            case 3 -> y--;
        }

    }

    public int GetPosition() {
        return x*1000+y;

    }

    // stops elf
    public void stopProduction() {
        toys_needed = false;
        SimulationParameters.syncOut("ELF "+serial+" STOPPED AT POSITION {"+x+","+y+"}");
    }

    // achieving random directions
    private void shuffleDirections() {
        Random rand = new Random();
        for (int i = 0; i < directions.length; i++) {
            int randomIndexToSwap = rand.nextInt(directions.length);
            int temp = directions[randomIndexToSwap];
            directions[randomIndexToSwap] = directions[i];
            directions[i] = temp;
        }
    }

    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }

}
