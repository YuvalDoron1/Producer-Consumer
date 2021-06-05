/**
 * Name: Yuval Doron
 * ID: 207331554
 */
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.*;


public class Copier implements Runnable{

    private int id;
    private File destination;
    private SynchronizedQueue<File> resultQueue;
    private SynchronizedQueue<String> milestonesQueue;
    private boolean isMilestones;

    public Copier(int id, File destination, SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> milestonesQueue, boolean isMilestones){
        this.id = id;
        this.destination = destination;
        this.resultQueue = resultsQueue;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    @Override
    public void run() {
        // register right away to the queues, so the consumers will know that the producer might still produce
        if (isMilestones) {
            milestonesQueue.registerProducer();
        }

        File currentFile;

        // dequeue will return null when queue is empty and there are no expected new results
        while ((currentFile = resultQueue.dequeue()) != null){
            if (isMilestones){
                String milestone = "Copier from thread id " + id + ": file named " + currentFile.getName() + " was copied";
                milestonesQueue.enqueue(milestone);
            }

            Path src = Paths.get(currentFile.getAbsolutePath());
            Path dst = Paths.get(destination.getAbsolutePath() + "\\" + currentFile.getName());
            try {
                Files.copy(src,dst,REPLACE_EXISTING);
            } catch (IOException e) {
                continue;
            }
        }
        milestonesQueue.unregisterProducer();
    }
}