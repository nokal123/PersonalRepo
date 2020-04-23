#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include  <pthread.h>
#include <string.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/time.h>
#include <sys/stat.h>
#include <syslog.h>
#define VERSION 25
#define BUFSIZE 8096
#define ERROR      42
#define LOG        44
#define FORBIDDEN 403
#define NOTFOUND  404

struct {
    char *ext;
    char *filetype;
} extensions [] = {
        {"gif", "image/gif" },
        {"jpg", "image/jpg" },
        {"jpeg","image/jpeg"},
        {"png", "image/png" },
        {"ico", "image/ico" },
        {"zip", "image/zip" },
        {"gz",  "image/gz"  },
        {"tar", "image/tar" },
        {"htm", "text/html" },
        {"html","text/html" },
        {0,0} };
typedef struct {
	int numBefore;
	int numDispatch;
	int numComplete;
	time_t arriveT;
	time_t dispatchT;
	time_t completeT;
} request;
typedef struct {
    int job_id;
    int job_fd; // the socket file descriptor
    char *extention;
    char *filePath;
    request req;
} job_t;
typedef struct {
    job_t *priorityBuffer; // array of server Jobs on heap
    job_t *regBuffer;
    size_t buf_capacity;
    size_t phead; // position of writer and points to object at top
    size_t ptail;
    size_t rhead; // position of writer and points to object at top
    size_t rtail; // position of reader
    int numJobs;
    pthread_mutex_t work_mutex;
    pthread_cond_t c_cond; // P/C condition variables
    pthread_cond_t p_cond;
} tpool_t;

static const char * HDRS_FORBIDDEN = "HTTP/1.1 403 Forbidden\nContent-Length: 185\nConnection: close\nContent-Type: text/html\n\n<html><head>\n<title>403 Forbidden</title>\n</head><body>\n<h1>Forbidden</h1>\nThe requested URL, file type or operation is not allowed on this simple static file webserver.\n</body></html>\n";
static const char * HDRS_NOTFOUND = "HTTP/1.1 404 Not Found\nContent-Length: 136\nConnection: close\nContent-Type: text/html\n\n<html><head>\n<title>404 Not Found</title>\n</head><body>\n<h1>Not Found</h1>\nThe requested URL was not found on this server.\n</body></html>\n";
static const char * HDRS_OK = "HTTP/1.1 200 OK\nServer: nweb/%d.0\nContent-Length: %ld\nConnection: close\nContent-Type: %s\nRequests-Beforehand: %i\nArrival-Time: %ld\nDispatch-Count: %i\nDispatch-Time: %ld\nComplete-Count: %i\nComplete-Time: %ld\nThread-Id: %i\nThread-Count: %i\nThread-Html: %i\nThread-Image: %i\n\n"; //add everything into headers ok and add extra paramiters on line 154 to pouplate them. 
static int dummy; //keep compiler happy
typedef void * (worker_fn) (void *);
static tpool_t the_pool;
int listenfdglob;
char *schedalg = NULL;
int arrived, dispatched, completed = 0;
struct timeval tv, begining;
//char* argv6;

void logger(int type, char *s1, char *s2, int socket_fd)
{
    int fd ;
    char logbuffer[BUFSIZE*2];

    switch (type) {
        case ERROR: (void)sprintf(logbuffer,"ERROR: %s:%s Errno=%d exiting pid=%d",s1, s2, errno,getpid());
            break;
        case FORBIDDEN:
            dummy = write(socket_fd, HDRS_FORBIDDEN,271);
            (void)sprintf(logbuffer,"FORBIDDEN: %s:%s",s1, s2);
            break;
        case NOTFOUND:
            dummy = write(socket_fd, HDRS_NOTFOUND,224);
            (void)sprintf(logbuffer,"NOT FOUND: %s:%s",s1, s2);
            break;
        case LOG: (void)sprintf(logbuffer," INFO: %s:%s:%d",s1, s2,socket_fd); break;
    }
    /* No checks here, nothing can be done with a failure anyway */
    if((fd = open("nweb.log", O_CREAT| O_WRONLY | O_APPEND,0644)) >= 0) {
        dummy = write(fd,logbuffer,strlen(logbuffer));
        dummy = write(fd,"\n",1);
        (void)close(fd);
    }
}

/* this is a child web server process, so we can exit on errors */
char* fileName(int fd, int hit)
{
 	int j;
    long i, ret;
    static char buffer[BUFSIZE+1]; /* static so zero filled */
    ret =read(fd,buffer,BUFSIZE);   /* read Web request in one go */
    if(ret == 0 || ret == -1) { /* read failure stop now */
        logger(FORBIDDEN,"failed to read browser request","",fd);
        goto endRequest;
    }
    if(ret > 0 && ret < BUFSIZE) {  /* return code is valid chars */
        buffer[ret]=0;      /* terminate the buffer */
    }
    else {
        buffer[0]=0; 
    }
    for(i=0;i<ret;i++) {    /* remove CF and LF characters */
        if(buffer[i] == '\r' || buffer[i] == '\n') {
            buffer[i]='*';
        }
    }
    logger(LOG,"request",buffer,hit);
    if( strncmp(buffer,"GET ",4) && strncmp(buffer,"get ",4) ) {
        logger(FORBIDDEN,"Only simple GET operation supported",buffer,fd);
        goto endRequest;
    }
    for(i=4;i<BUFSIZE;i++) { /* null terminate after the second space to ignore extra stuff */
        if(buffer[i] == ' ') { /* string is "GET URL " +lots of other stuff */
            buffer[i] = 0;
            break;
        }
    }
    for(j=0;j<i-1;j++) {    /* check for illegal parent directory use .. */
        if(buffer[j] == '.' && buffer[j+1] == '.') {
            logger(FORBIDDEN,"Parent directory (..) path names not supported",buffer,fd);
            goto endRequest;
        }
    }
    if( !strncmp(&buffer[0],"GET /\0",6) || !strncmp(&buffer[0],"get /\0",6) ) { /* convert no filename to index file */
        (void)strcpy(buffer,"GET /index.html");
    }
	endRequest:
   	return buffer + 5; 
}
char *fExtention(char* buffer, int fd) 
{
	char * fstr;
	int len, i;
	int buflen = strlen(buffer);
    fstr = (char *)0;
    for(i=0;extensions[i].ext != 0;i++) {
        len = strlen(extensions[i].ext);
        if( !strncmp(&buffer[buflen-len], extensions[i].ext, len)) {
            fstr =extensions[i].filetype;
            break;
        }
    }
    if(fstr == 0){
        logger(FORBIDDEN,"file extension type not supported",buffer,fd);
    }
    return fstr;
    
}

void web(int fd, int hit, char* file, job_t job, int httpReq, int htmlReq, int imageReq)
{
    int file_fd;
    long ret, len;
    char * fstr = job.extention;
    char buffer[BUFSIZE+1]; /* static so zero filled */
    if(( file_fd = open(file,O_RDONLY)) == -1) {  /* open the file for reading */
    	printf("FILE: %s\n", file);
        logger(NOTFOUND, "failed to open file",file,fd);
        goto endRequest;
    }
    gettimeofday(&tv, NULL);
	job.req.completeT = (tv.tv_sec - begining.tv_sec) * 1000000 + tv.tv_usec - begining.tv_usec;
    //printf("Requests-Beforehand: %i\nArrival-Time: %ld\nDispatch-Count: %i\nDispatch-Time: %ld\nComplete-Count: %i\nComplete-Time: %ld\n", job.req.numBefore, job.req.arriveT, job.req.numDispatch, job.req.dispatchT, job.req.numComplete, job.req.completeT); 
    logger(LOG,"SEND",file,hit);
    len = (long)lseek(file_fd, (off_t)0, SEEK_END); /* lseek to the file end to find the length */
          (void)lseek(file_fd, (off_t)0, SEEK_SET); /* lseek back to the file start ready for reading */
          /* print out the response line, stock headers, and a blank line at the end. */
          //printf("Requests-Beforehand: %i\nArrival-Time: %ld\nDispatch-Count: %i\nDispatch-Time: %ld\nComplete-Count: %i\nComplete-Time: %ld\n", job.req.numBefore, job.req.arriveT, job.req.numDispatch, job.req.dispatchT, job.req.numComplete, job.req.completeT);
          //printf("Thread-Id: %i\nThread-Count: %i\nThread-Html: %i\nThread-Image: %i\n",  hit,  httpReq, htmlReq, imageReq);
          (void)sprintf(buffer, HDRS_OK, VERSION, len, fstr, job.req.numBefore, job.req.arriveT, job.req.numDispatch, job.req.dispatchT, job.req.numComplete, job.req.completeT, hit,  httpReq, htmlReq, imageReq);  
           //stage 3 headerok has a blank line at the end. only send that line after all your headers 
    logger(LOG,"Header",buffer,hit);
    dummy = write(fd,buffer,strlen(buffer));
    
    /* send file in 8KB block - last block may be smaller */
    while ( (ret = read(file_fd, buffer, BUFSIZE)) > 0 ) {
        dummy = write(fd,buffer,ret);
    }
    endRequest:
    sleep(1);   /* allow socket to drain before signalling the socket is closed */
    close(fd);
}
void tpool_init(tpool_t *tm, size_t num_threads, size_t buf_size, worker_fn *worker)
{
    pthread_t thread;
    size_t i;
    //tm = calloc(1, sizeof(*tm));
    pthread_mutex_init(&(tm->work_mutex), NULL);
    pthread_cond_init(&(tm->p_cond), NULL);
    pthread_cond_init(&(tm->c_cond), NULL);    // initialize buffer to empty condition
    tm->phead = tm->ptail = tm->rhead = tm->rtail = 0;
    tm->buf_capacity = buf_size;
    tm->regBuffer = calloc(sizeof(job_t), buf_size); //... CALLOC_ACTUAL_BUFFER_SPACE_ON_HEAP
    tm->priorityBuffer = calloc(sizeof(job_t), buf_size); //... CALLOC_ACTUAL_BUFFER_SPACE_ON_HEAP
    for (i=0; i<num_threads; i++) {
        pthread_create(&thread, NULL, worker, (void *) (i + 1)); //worker is a start routine
        pthread_detach(thread); // make non-joinable
    }
   
}
static void *tpool_worker(void *arg)
{
    tpool_t *tm = &the_pool;
    int my_id = (long) arg;
    job_t job;
    int httpReq = 0;
    int htmlReq = 0;
    int imageReq = 0;
    while (1)
    {
        pthread_mutex_lock(&(tm->work_mutex));
        while (tm->numJobs == 0) // && !tm->stop   stop is a bool for stopping the pool
            pthread_cond_wait(&(tm->c_cond), &(tm->work_mutex));
		
		httpReq++;
		if((strcmp(schedalg, "FIFO") == 0) || (strcmp(schedalg, "ANY") == 0)) {
			job = tm->regBuffer[tm->rtail];
			job.req.numDispatch = dispatched;
			dispatched++;
			if(strcmp(job.extention, "text/html") == 0)
				htmlReq++;
			else
				imageReq++; 
			
			gettimeofday(&tv, NULL);
			job.req.dispatchT = (tv.tv_sec - begining.tv_sec) * 1000000 + tv.tv_usec - begining.tv_usec;
			tm->rtail = (tm->rtail +1) % tm->buf_capacity;
        	tm->numJobs--;
		}
		//do priority work unless nothing in that buffer, then do reg work
		else { //if(strcmp(schedalg, "HPIC") || strcmp(schedalg, "HPHC")) {
			if(tm->phead == tm->ptail)
			{
				//printf("REG BUFFER PULL\n");
				job = tm->regBuffer[tm->rtail];;
				job.req.numDispatch = dispatched;
				dispatched++;
				//if(strcmp(job.extention, "text/html") == 0)
					htmlReq++;
				else
					imageReq++; 
				gettimeofday(&tv, NULL);
				job.req.dispatchT = (tv.tv_sec - begining.tv_sec) * 1000000 + tv.tv_usec - begining.tv_usec;
				tm->rtail = (tm->rtail +1) % tm->buf_capacity;
        		tm->numJobs--;
			}
			else
			{
				//printf("PRIORITY BUFFER PULL\n");
				job = tm->priorityBuffer[tm->ptail];
				job.req.numDispatch = dispatched;
				dispatched++;
				if(strcmp(job.extention, "text/html") == 0)
					htmlReq++;
				else
					imageReq++; 
				gettimeofday(&tv, NULL);
				job.req.dispatchT = (tv.tv_sec - begining.tv_sec) * 1000000 + tv.tv_usec - begining.tv_usec;
				tm->ptail = (tm->ptail +1) % tm->buf_capacity;
        		tm->numJobs--;
			}
		}
        pthread_mutex_unlock(&(tm->work_mutex));
        //DO_THE_WORK(job);  // call web() plus ??
        web(job.job_fd, my_id, job.filePath, job, httpReq, htmlReq, imageReq);

        pthread_mutex_lock(&(tm->work_mutex));
        // if SHOULD_WAKE_UP_THE_PRODUCER
        //if we want producer, buffer must not be empty
        
        completed++;
       //THIS MAY BE WHATS CAUSING THE ISSUE!!!
        if ( ((tm->ptail > tm->phead) && (tm->rtail > tm->rhead)) || (((tm->rhead) == (tm->rtail)) == 0 && ((tm->phead) == (tm->ptail)) == 0))
            pthread_cond_signal(&(tm->p_cond));
        pthread_mutex_unlock(&(tm->work_mutex));
    }
    //printf("\n");
    return NULL;
}
int tpool_add_work(tpool_t *tm, job_t jb)
{
	//printf("IN ADD WORK:\n");
    pthread_mutex_lock(&(tm->work_mutex));
    while(tm->numJobs == tm->buf_capacity) {
        pthread_cond_wait(&(tm->p_cond), &(tm->work_mutex));
        }

   if((strcmp(schedalg, "FIFO") == 0) || (strcmp(schedalg, "ANY") == 0)) { //THIS IS NEVER GOING IN!
   			//printf("IN FIFO:\n");
			tm->regBuffer[tm->rhead] = jb;
			tm->rhead = (tm->rhead +1) % tm->buf_capacity;
        	tm->numJobs++;
		}
	else if(strcmp(schedalg, "HPIC") == 0) {
			if( (strcmp(jb.extention, "text/html") == 0)) {
				//printf("Added to NOT priority buffer\n");
				tm->regBuffer[tm->rhead] = jb;
    			tm->rhead = (tm->rhead +1) % tm->buf_capacity; //going to next space that is currently NULL.
    			tm->numJobs++;
			}
			else
			{
				//printf("Added to priority buffer\n");
				tm->priorityBuffer[tm->phead] = jb;
    			tm->phead = (tm->phead +1) % tm->buf_capacity; //going to next space that is currently NULL.
    			tm->numJobs++;
			}
				
		}
			
	else if(strcmp(schedalg, "HPHC") == 0) {
			
			if((strcmp(jb.extention, "text/html") == 0)) {
				//printf("Added to priority buffer\n");
				tm->priorityBuffer[tm->phead] = jb;
    			tm->phead = (tm->phead +1) % tm->buf_capacity; //going to next space that is currently NULL.
    			tm->numJobs++;
			}
			else
			{
				//printf("Added to NOT priority buffer\n");
				tm->regBuffer[tm->rhead] = jb;
    			tm->rhead = (tm->rhead +1) % tm->buf_capacity; //going to next space that is currently NULL.
    			tm->numJobs++;
			}
		
		}

    // Wake the Keystone Cops!! (improve this eventually)
    pthread_cond_broadcast(&(tm->c_cond));
    pthread_mutex_unlock(&(tm->work_mutex));
    return 1; // 0 = false  1 = true
}


static void skeleton_daemon()
{
    pid_t pid;

    /* Fork off the parent process */
    pid = fork();

    /* An error occurred */
    if (pid < 0)
        exit(EXIT_FAILURE);

    /* Success: Let the parent terminate */
    if (pid > 0)
        exit(EXIT_SUCCESS);

    /* On success: The child process becomes session leader */
    if (setsid() < 0)
        exit(EXIT_FAILURE);

    /* Catch, ignore and handle signals */
    //TODO: Implement a working signal handler */
    signal(SIGCHLD, SIG_IGN);
    signal(SIGHUP, SIG_IGN);

    /* Fork off for the second time*/
    pid = fork();

    /* An error occurred */
    if (pid < 0)
        exit(EXIT_FAILURE);

    /* Success: Let the parent terminate */
    if (pid > 0)
        exit(EXIT_SUCCESS);

    /* Set new file permissions */
    umask(0);

    /* Change the working directory to the root directory */
    /* or another appropriated directory */
	if(chdir("/") == -1) {};
	
	
	

    /* Close all open file descriptors */
    int x;
    for (x = sysconf(_SC_OPEN_MAX); x>=0; x--)
    {
        close (x);
    }

    /* Open the log file */
    openlog ("firstdaemon", LOG_PID, LOG_DAEMON);
}


int main(int argc, char **argv)
{
	gettimeofday(&begining, NULL);
    int i, port, listenfd, threads, buffers, hit, socketfd;
    // int folder;
    socklen_t length;
    char *filePath; 
    char *extention;
    static struct sockaddr_in cli_addr; /* static = initialised to zeros */
	static struct sockaddr_in serv_addr; /* static = initialised to zeros */ /* static = initialised to zeros */
	//argv6 = argv[6];
    
    
    
    if( argc < 6  || argc > 7 || !strcmp(argv[1], "-?") ) {
        (void)printf("USAGE: %s <port-number> <top-directory>\t\tversion %d\n\n"
                     "\tnweb is a small and very safe mini web server\n"
                     "\tnweb only servers out file/web pages with extensions named below\n"
                     "\t and only from the named directory or its sub-directories.\n"
                     "\tThere is no fancy features = safe and secure.\n\n"
                     "\tExample: nweb 8181 /home/nwebdir &\n\n"
                     "\tOnly Supports:", argv[0], VERSION);
        for(i=0;extensions[i].ext != 0;i++)
            (void)printf(" %s",extensions[i].ext);

        (void)printf("\n\tNot Supported: URLs including \"..\", Java, Javascript, CGI\n"
                     "\tNot Supported: directories / /etc /bin /lib /tmp /usr /dev /sbin \n"
                     "\tNo warranty given or implied\n\tNigel Griffiths nag@uk.ibm.com\n"  );
        exit(0);
    }
    if( !strncmp(argv[2],"/"   ,2 ) || !strncmp(argv[2],"/etc", 5 ) ||
        !strncmp(argv[2],"/bin",5 ) || !strncmp(argv[2],"/lib", 5 ) ||
        !strncmp(argv[2],"/tmp",5 ) || !strncmp(argv[2],"/usr", 5 ) ||
        !strncmp(argv[2],"/dev",5 ) || !strncmp(argv[2],"/sbin",6) ){
        (void)printf("ERROR: Bad top directory %s, see nweb -?\n",argv[2]);
        exit(3);
    }
    if(chdir(argv[2]) == -1){
        (void)printf("ERROR: Can't Change to directory %s\n",argv[2]);
        exit(4);
    }
    
    

    logger(LOG,"nweb starting",argv[1],getpid());
    /* setup the network socket */
    if((listenfd = socket(AF_INET, SOCK_STREAM,0)) <0){
        logger(ERROR, "system call","socket",0);
    }
    port = atoi(argv[1]);
    if(port < 1025 || port >65000) {
        logger(ERROR,"Invalid port number (try 1025->65000)",argv[1],0);
    }
    threads = atoi(argv[3]);
    buffers = atoi(argv[4]);
    schedalg = argv[5];

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(port);
    if(bind(listenfd, (struct sockaddr *)&serv_addr,sizeof(serv_addr)) <0){
        logger(ERROR,"system call","bind",0);
    }
    if( listen(listenfd,64) <0) {
        logger(ERROR,"system call","listen",0);
    }
    listenfdglob = listenfd;
    
    tpool_init(&the_pool, threads, buffers, tpool_worker);

    for(hit=1; ;hit++) {
		length = sizeof(cli_addr);
		if((socketfd = accept(listenfd, (struct sockaddr *)&cli_addr, &length)) < 0) {
			logger(ERROR,"system call","accept",0);
		}
		gettimeofday(&tv, NULL);
		filePath = fileName(socketfd, hit);
		extention = fExtention(filePath, socketfd);
		job_t newJob;
		newJob.req.numBefore = arrived;
		arrived++;
		newJob.req.arriveT = (tv.tv_sec - begining.tv_sec) * 1000000 + tv.tv_usec - begining.tv_usec;
		newJob.job_id = hit;
		newJob.job_fd = socketfd;
		newJob.extention = extention;
		newJob.filePath = filePath;
		newJob.req.numComplete = completed;
		tpool_add_work(&the_pool, newJob);
	}//tail nweb.log
	

    //adding jobs to the buffer?

	if(argc == 7 && (strcmp(argv[6], "-d" ) == 0)) {
    	skeleton_daemon();
	}

}