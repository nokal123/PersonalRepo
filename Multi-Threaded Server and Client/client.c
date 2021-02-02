
/* Generic */
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <semaphore.h> 
#include <time.h>
#include <sys/time.h>


/* Network */
#include <netdb.h>


#define BUF_SIZE 250


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
    int job_id;
    int job_fd; // the socket file descriptor
   	
} job_t;
typedef struct {
    job_t *jobBuffer; // array of server Jobs on heap
    size_t buf_capacity;
    size_t head; // position of writer and points to object at top
    size_t tail; // position of reader
    int numJobs;
 //pthread_mutex_t work_mutex;
   
} tpool_t;

typedef void * (worker_fn) (void *);
static tpool_t the_pool;
int listenfdglob;

int port;
int threads;
char* argv1;
char* argv2;
char* argv5;
char* argv6 = NULL;

//void* semMem;

static pthread_barrier_t barrier;
char* schedAlg;

sem_t* mutex;



struct timeval stop, start;







// Get host information (used to establishConnection)
struct addrinfo *getHostInfo(char* host, char* port) {
//printf("entered getHostInfo\n");
  int r;
  struct addrinfo hints, *getaddrinfo_res;
  // Setup hints
  memset(&hints, 0, sizeof(hints));
  hints.ai_family = AF_INET;
  hints.ai_socktype = SOCK_STREAM;

  if ((r = getaddrinfo(host, port, &hints, &getaddrinfo_res))) {

    fprintf(stderr, "[getHostInfo:21:getaddrinfo] %s\n", gai_strerror(r));
    return NULL;
  }


  return getaddrinfo_res;
}

// Establish connection with host
int establishConnection(struct addrinfo *info) {

  if (info == NULL) return -1;

  int clientfd;
  for (;info != NULL; info = info->ai_next) {
    if ((clientfd = socket(info->ai_family,
                           info->ai_socktype,
                           info->ai_protocol)) < 0) {
      perror("[establishConnection:35:socket]");
      continue;
    }

    if (connect(clientfd, info->ai_addr, info->ai_addrlen) < 0) {
      close(clientfd);
      perror("[establishConnection:42:connect]");
      continue;
    }

    freeaddrinfo(info);
    return clientfd;
  }

  freeaddrinfo(info);
  return -1;
}

// Send GET request
void GET(int clientfd, char *path) {
  char req[1000] = {0};
  sprintf(req, "GET %s HTTP/1.0\r\n\r\n", path);
  send(clientfd, req, strlen(req), 0);
  
  
}






void tpool_init(tpool_t *tm, size_t num_threads, worker_fn *worker)
{

    pthread_t threads[num_threads];
    size_t i;
   
 

    for (i=0; i<num_threads; i++) {
		printf("This is thread number %ld being created\n", i); //thread num
        pthread_create(&threads[i], NULL, worker, (void *) (i + 1)); //worker is a start routine
      
    }
    gettimeofday(&start, NULL);

}




static void *tpool_worker(void *arg)
{




    int my_id = (long) arg;
   
       
     int clientfd; 
     char buf[BUF_SIZE];
     char* getArg = argv5;
    
 
    
     
    
    while (1)
    {
    	if(strcmp(schedAlg, "FIFO") == 0) {
    		if(my_id != 1) {
    			sem_wait(&mutex[my_id - 2]);
    		}
    	}
    		
    
    	// Establish connection with <hostname>:<port> 
  		clientfd = establishConnection(getHostInfo(argv1, argv2));
  
  		if (clientfd == -1) {
    		fprintf(stderr,
            "[main:73] Failed to connect to: %s:%s%s \n",
            argv1, argv2, getArg);
    		return (char*) 3;
  		}
    
  	
   
       
  		GET(clientfd, getArg);
  		
  		
  		

  		
  		gettimeofday(&stop, NULL);
  		//printf("took %lu us for GET request on id %d\n", (stop.tv_sec - start.tv_sec) * 1000000 + stop.tv_usec - start.tv_usec, my_id); 
  		
  		if(strcmp(schedAlg, "FIFO") == 0) {
  				sem_post(&mutex[my_id - 1]); 
  		}
  		
  		
  		while (recv(clientfd, buf, BUF_SIZE, 0) > 0) {
    		fputs(buf, stdout);
    		memset(buf, 0, BUF_SIZE);
    	}
    	
    	
		
    	close(clientfd);
    	
    
    
   
    	pthread_barrier_wait(&barrier);
    	
    	
    	
    	if(argv6 != NULL) {
    		if(getArg != argv6) {
    			getArg = argv6;
    		}
    		else {
    			getArg = argv5;
    		}
    	}
   
    

  }
        
      
    

    return NULL;
}







int main(int argc, char **argv) {

   

  
   	int i;
	argv1 = argv[1];
	argv2 = argv[2];
	schedAlg = argv[4];
	//printf("schedAlg: %p\n", schedAlg);
	argv5 = argv[5];
	
	if(argc == 7) {
		argv6 = argv[6];
	}
	
    
	port = atoi(argv[2]);
    
    printf("port: %i\n", port);
    //new arg for thread number
    threads = atoi(argv[3]);
    printf("threads: %i\n", threads);
    
   
    /* Initialize the barrier. The final argument specifies the
       number of threads that must call pthread_barrier_wait()
       before any thread will unblock from that call. */

	pthread_barrier_init(&barrier, NULL, threads);

  	if(strcmp(schedAlg, "FIFO") == 0) {
  		
  		mutex = calloc(threads, sizeof(sem_t)); 
  		for(i = 0; i < threads - 1; i++)
    		sem_init(&mutex[i], 0, 0);
    				
  	}
 
    
    
    
  if (argc != 6 && argc != 7) { 
    fprintf(stderr, "USAGE: %s <hostname> <port> <request path>\n", argv[0]);
    return 1;
  }


 
    
    
    tpool_init(&the_pool, threads, tpool_worker);
    
    while (1);
	//prevent return from happening, wait for first worker to quit, which will never happen, control c should quit 
  
  
  return 0;
}



