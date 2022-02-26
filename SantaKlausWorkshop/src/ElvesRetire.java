// Extra Task 1

public class ElvesRetire extends Thread{
    boolean retired = false;
    public void run() {
        while(!retired) {
            // give permit to elf so he retires
            Office.elfRetireSemaphore.release();
            // elf sleeps for 50
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void stopRetirement() {
        retired = true;
       //SimulationParameters.syncOut("ELVES ARE RETIRED");
    }
}
