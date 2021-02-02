I.
Alexander Schlesinger
aschles1

Noah Kalandar
nkalanda

Part 1: Alex & Noah worked collaboratively throughout
Part 2: Noah
Part 3: Noah
Part 4: Alex
Part 5: Alex


II.
Multi-Threaded Server:

The server code has multiple methods to help it in its process of client requests. It have a mian
which creates a pool of threads to handle all incoming requests. Client requests as they are accepted
by the server are added to one of two buffers depending on the scheduling specified on the command-line. 
Each buffer is circular and allows for as many requests are the client would like without limit. Before 
being added, each job during its processing in the main method has it's extension and file path retrieved
and stored in a struct inside of the job struct, called request. This is done by a helper method called add_work. 

One buffer is a priority buffer and one is a low priority buffer. For HPIC and HPHC, the priority buffer is used in handling requests that are priority and the other for low priority. For FIFO, just the low priority buffer is used. Add_work than checks the extension of the job to determine if the job is an html or an image file and places it in the correct buffer. All requests from the client are stamped with a time relative to the start of the server on arrival, dispatch to a worker thread, and completion.  Once the job has been placed in the relevant buffer,another helper method, tpool_worker, checks the scheduling policy and pulls out a job from the correct buffer. The job is then sent to a helper method web which processes the actual print out to the command line of all job headers and information including Content-Length, Connection ,Content-Type , Requests-Beforehand, Arrival-Time, Dispatch-Count ,Dispatch-Time ,Complete-Count ,Complete-Time, Thread-Id, Thread-Count ,Thread-Html, and Thread-Image. At the end the server is daemonized.

Multi-Threaded Client:

The client.c code utilizes a main method to take arguments off of the command line with the information to establish a connection to the server, to create a pool of threads to process GET requests (the number of threads to be created), and the file(s) to which will be requested. The main method also assures the command line has the correct number of arguments.
The main method also calls tpool_init to initialize a pool of threads, as well as calls the initialization methods for the Semaphores and barrier.

The initialization of the threads then sends the threads to do its main “work” in tpool_worker. The main purpose of tpool_worker is to, in an infinite  “while” loop, call the GET method by multiple threads, which in turn creates infinite requests for the file to the server. Before the GET request can take place, the client must get the host information and establish a connection, done by the methods with those respective names. 

The scheduling order in which these threads create requests is organized by the barrier and by the Semaphores, depending on whether the scheduling is set to FIFO or CONCUR. If the scheduling is set to CONCUR, the barrier causes the threads to wait till n threads hit the barrier before they each send their requests, thereby causing n threads to send their GET requests essentially simultaneously, thereby causing the actual order of which request to send to the server to be random. When FIFO is the scheduling of choice, the Semaphores for each thread interact with each other, allowing a thread, when it sends its request, to trigger the next thread’s semaphore to do its “work,” resulting in the requests coming out in thread order of processing (1st, then 2nd, then 3rd, etc), otherwise known as First In First Out. 

The end of that “while” loop contains logic to toggle between 2 files so that GET requests can be interchangably sent for each, if arguments for 2 files are present on the command line.


III. 

Ambiguities in Server

Ambiguities in the server, such as the ANY scheduling policy, in that case was solved by undestanding that ANY could be any such scheduling policy, so it was hard coded to always be FIFO. Another ambiguity was retrieving the complete time of a job, which required getting the time before the job was actually completed in order that it could be printed as a header in the web method. 


Ambiguities in Client

Handling 2 files, when put on the command line, was quite ambiguous in that I was not sure if handling the files using threads interchangeably meant between threads or respective to threads (for FIFO, for example, one could claim that the first thread should handle the first file, the second thread handle the second file, etc, or is it that the first time each of the thread go through the loop they should each handle the first file, then the next time handle the second file, etc). I chose the latter, as I felt it worked best with the logic of the code and I was able to write a proper implementation, by toggling a variable with the command line argument for each file at the end of the “while” loop.


IV.

Server

One thing currently not working in the server is the "age" header for each process. 

Additionally, the Daemon code seems correct and compiles, but for a reason I could not exactly track down, the server stops early (says ‘Done’) before I can run a client.

V.

Testing the Server:

 For testing of the server, strategic printfs where places at the beginning and end of each method, signaling the entry and exit of the method. Inside the method, each local and global variable was printed out in order to track variations and bugs in the processing of requests. For scheduling policies, a server with one thread and a buffer with spaces for 2 times the amount of threads in the client where created. 

Requests from the client and printfs in the server allowed us to debug for scheduling policy issues. Tests varied in the number of threads of the server and client, as well as space on the buffer in the server, to assure consistent, correct results. After experiencing an issue of accidentally testing on an old server for days, we became careful to often switch ports for testing. For HPIC (and doing the reverse for HPHC), we tested by making the number of threads for the server 1, with a buffer of many (10, or 20, for example), and a client with many threads, such that it is half the amount of threads that can fit in the buffer, for example. We would utilize the client's ability to take 2 files, and we would place the file that is lower priority (index.html for HPIC) as the first file argument. The reason for this is because afte rprocessing perhaps one of the html files at most, the buffer should contain a batch of html files and a bunch of jpg files. In a FIFO bufferm the html files would get processed first, because they would have entered the buffer first, but because we have a high-priority buffer and a low-priority buffer for the HPIC (and HPHC files), the higher-priority file should then get processed before the rest of the lower priority files.

A problem we encountered was that as far as we were able to see, the even when the thread number for the server was 1, for some reason it was processing the files wuickly enough that the buffer didn;t contain both types of files at the same time, and therefore there weren't instances that we were able to identify in which a priority had to be chosen. Te logic of the code seems to be correct, so we hope it works correctly.


Testing the Client:

I tested the client throughout by using printf many times, such as the beginning and end of each method, before and after the GET request, etc.

I also created a variable with an id for each thread, and utilized timval to compare when the GET request was sent for each thread. 

Additionally, I utilized the ‘grep’ command to select words in my printl to bunch together and highlight when running the client so that I can easily see the order of the threads and the times in which they made their requests. For FIFO scheduling, I assured that the threads were printing out in order (thread 1 for the first GET request, thread 2 for the second, throughout all n threads, and then returning back to thread 1, etc). For CONCUR, I assured that all of the threads were sending their requests in a very short span of time and in random order. I tested with each type of file individually (jpg and html) as well as both of them together.

We worked tirelessly on this project and put much focus on it for the last several weeks (above all of our other work). We learned so much and are very appreciative of your (Dr. Kelly's) willingness and continuous efforts to take the time to meet with us and help us improve. We know that it is not perfect, but we did truly put in a massive effort and spent much time doing our best. Thank you.  
