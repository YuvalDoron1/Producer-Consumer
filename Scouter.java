/**
 * Name: Yuval Doron
 * ID: 207331554
 */
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Scouter implements Runnable{
    // new comment!
    private int id;
    private SynchronizedQueue<File> directoryQueue;
    private File root;
    private SynchronizedQueue<String> milestonesQueue;
    private boolean isMilestones;

    public Scouter(int id, SynchronizedQueue<File> directoryQueue, File root, SynchronizedQueue<String> milestonesQueue, boolean isMilestones){
        this.id = id;
        this.directoryQueue = directoryQueue;
        this.root = root;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    @Override
    public void run() {
        // register right away to the queues, so the consumers will know that the producer might still produce
        if (isMilestones) {
            milestonesQueue.registerProducer();
        }
        directoryQueue.registerProducer();

        // scout the root itself
        directoryQueue.getAndIncrement();
        if (isMilestones) {
            String scoutMilestone = "Scouter on thread id " + id + ": directory named " + root.getName() + " was scouted";
            milestonesQueue.enqueue(scoutMilestone);
        }
        directoryQueue.enqueue(root);

        // this list will allow to scout dirs in a fair FIFO of the sub-dirs depth.
        List<File> nextToScout = new LinkedList<>();

        while (root != null) {
            String[] dirNames = root.list();

            if (dirNames == null)
                break;

            for (String dirName : dirNames) {
                File currentFile = new File(root + "\\" + dirName);

                if (currentFile.isDirectory()) {

                    // check if we reached directories threshold
                    if (directoryQueue.getAndIncrement() >= DiskSearcher.DIRECTORY_QUEUE_CAPACITY){
                        if (isMilestones)
                            milestonesQueue.unregisterProducer();
                        directoryQueue.unregisterProducer();
                        return;
                    }

                    nextToScout.add(currentFile);

                    if (isMilestones) {
                        String scoutMilestone = "Scouter on thread id " + id + ": directory named " + currentFile.getName() + " was scouted";
                        milestonesQueue.enqueue(scoutMilestone);
                    }
                    directoryQueue.enqueue(currentFile);
                }
            }

            // check if there are more sub-dirs to work on
            if (!nextToScout.isEmpty())
                root = nextToScout.remove(0);
            else
                root = null;
        }

        if (isMilestones)
            milestonesQueue.unregisterProducer();
        directoryQueue.unregisterProducer();
    }
}
