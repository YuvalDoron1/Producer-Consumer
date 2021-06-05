/**
 * Name: Yuval Doron
 * ID: 207331554
 */
import java.io.File;

public class DiskSearcher {

    // we want to have a threshold for the overall dirs and files we processed during the program.
    // these thresholds are also important for determine the mileStones queue capacity (potential milestones that will be there, see piazza).
    static final int RESULTS_QUEUE_CAPACITY = 50;
    static final int DIRECTORY_QUEUE_CAPACITY = 10;

    public static void main(String[] args){
        long startTime = System.currentTimeMillis();

        boolean isMilestones = Boolean.parseBoolean(args[0]);
        String extension = args[1];
        String rootStr = args[2];
        String destinationStr = args[3];
        int searcherLength = Integer.parseInt(args[4]);
        int copiersLength = Integer.parseInt(args[5]);
        File root = new File(rootStr);
        File destination = new File(destinationStr);
        int id = 1;

        // initialize directoryQueue with capacity of the number of files and sub-dirs in it.
        // note - directory capacity may be set to any positive number
        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACITY);

        // initialize resultsQueue with capacity of FILES_CAPACITY.
        // note - resultQueue capacity may be set to any positive number
        SynchronizedQueue<File> resultQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);

        // initialize milestonesQueue with capacity:
        // FILES_CAPACITY * 2 -> we can get as much as two milestones for each file we will copy (from searcher and from copier)
        // + dirsCapacity -> we can have one as much as one milestone for every sub-dir.
        // warning: lower capacity might cause that not all milestones will be enqueued.
        SynchronizedQueue<String> milestonesQueue = new SynchronizedQueue<>((2 * RESULTS_QUEUE_CAPACITY) + DIRECTORY_QUEUE_CAPACITY);

        // initialize and start the scouter
        Thread t1 = new Thread(new Scouter(id++, directoryQueue, root, milestonesQueue, isMilestones));
        t1.start();

        // initialize and start the searcher
        Thread[] searchers = new Thread[searcherLength];
        for (int i = 0; i < searchers.length; i++) {
            searchers[i] = new Thread(new Searcher(id++, extension, directoryQueue, resultQueue, milestonesQueue, isMilestones));
            searchers[i].start();
        }

        // initialize and start the copiers
        Thread[] copiers = new Thread[copiersLength];
        for (int i = 0; i < copiers.length; i++) {
            copiers[i] = new Thread(new Copier(id++, destination, resultQueue, milestonesQueue, isMilestones));
            copiers[i].start();
        }

        // wait for scouter to finish
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // wait for searchers to finish
        for (Thread searcher : searchers) {
            try {
                searcher.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // wait for copiers to finish
        for (Thread copier : copiers) {
            try {
                copier.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // print the mileStones queue if isMilestones is true
        if (isMilestones)
            milestonesQueue.printQueue();

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.println("Execution time in milliseconds: " + timeElapsed);
    }
}