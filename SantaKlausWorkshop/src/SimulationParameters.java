import java.util.Random;

public class SimulationParameters {
    // we have:
    // number of factories and size
    // elf spawn time
    // reindeer wait time
    // gift interval bounds
    // time between positions checks
    // number of elves and gifts
    // getters for elves and gifts
    // random generation function
    // necessary printing function for the console
    static Random rand = new Random();
    static final int number_of_factories = 5;
    static final int factories_map_size_min = 100;
    static final int factories_map_size_max = 500;
    static final int time_elf_min = 500;
    static final int time_elf_max = 1000;
    static final int reindeer_wait_min = 10;
    static final int reindeer_wait_max = 50;
    static final int no_gifts_needed_min = 10;
    static final int no_gifts_needed_max = 30;
    static final int no_gifts_announced = 8; // how many gifts we sent out
    static final int time_check_pos_min = 1000;
    static final int time_check_pos_max = 5000;
    static int no_elves = 0;
    static int no_gifts = 0;

    synchronized static int GetGiftsSerial() {
        return no_gifts++;
    }
    synchronized static int GetElfSerial() {
        return no_elves++;
    }
    static int RandomNumberInInterval(int min,int max) {
        return rand.nextInt(max-min) + min;
    }
    static synchronized void syncOut(Object message) {
        System.out.println(message);
    }
}





