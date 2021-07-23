# Producer-Consumer
Implementation of Producer(s)-Consumer(s) multi-thread program in Java.
The program gets as input root directory name, target directory, and file extension name, and copy all files with the specific extension from the root and it's subdirectory to the target directory. 
The program allows the user to determine the number of workers.

Scouter - This class is responsible for listing all directories that exist under the given root directory. It
enqueues all directories into the directory queue.
There is always only one scouter thread in the system.

Searcher - This class reads a directory from the directory queue and lists all files in this directory. Then,
it checks for each file name if it has the correct extension. Files that have the correct
extension are enqueued to the results queue (to be copied).

Copier - This class reads a file from the results queue (the queue of files that contains the output of
the searchers), and copies it into the specified destination directory.

![image](https://user-images.githubusercontent.com/83776265/126750815-bf6c60ab-e64b-4cca-a69f-97ca435da61b.png)
