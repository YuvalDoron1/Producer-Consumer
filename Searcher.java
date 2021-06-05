/**
 * Name: Yuval Doron
 * ID: 207331554
 */
import java.io.File;

/**
 * This class reads a directory from the directory queue and lists all files in this directory. Then,
 * it checks for each file name if it has the correct extension. Files that have the correct
 * extension are enqueued to the results queue (to be copied).
 *
 */
public class Searcher implements Runnable{
    private int id;
    private String extension;
    private SynchronizedQueue<File> directoryQueue;
    private SynchronizedQueue<File> resultsQueue;
    private SynchronizedQueue<String> milestonesQueue;
    private boolean isMilestones;

    public Searcher(int id, String extension, SynchronizedQueue<File> directoryQueue, SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> milestonesQueue, boolean isMilestones){
        this.id = id;
        this.extension = extension;
        this.directoryQueue = directoryQueue;
        this.resultsQueue = resultsQueue;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    @Override
    public void run() {
        // register right away to the queues, so the consumers will know that the producer might still produce
        if (isMilestones) {
            milestonesQueue.registerProducer();
        }
        resultsQueue.registerProducer();

        File dir;

        // work until no more dirs to work on
        while ((dir = directoryQueue.dequeue()) != null) {
            File[] allFiles = dir.listFiles();
            for (File file : allFiles) {
                String fileName = file.getName();
                String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
                if (file.isFile() && extension.equals(this.extension)) {

                    // if we got to the threshold of files to search - finish the search
                    if (resultsQueue.getAndIncrement() >= DiskSearcher.RESULTS_QUEUE_CAPACITY) {
                        if (isMilestones)
                            milestonesQueue.unregisterProducer();
                        resultsQueue.unregisterProducer();
                        return;
                    }

                    if (isMilestones) {
                        String scoutMilestone = "Searcher on thread id " + id + ": file named " + fileName + " was found";
                        milestonesQueue.enqueue(scoutMilestone);
                    }
                    resultsQueue.enqueue(file);
                }
            }
        }
        if (isMilestones)
            milestonesQueue.unregisterProducer();
        resultsQueue.unregisterProducer();
    }
}