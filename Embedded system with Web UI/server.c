/* 
This code primarily comes from 
http://www.prasannatech.net/2008/07/socket-programming-tutorial.html
and
http://www.binarii.com/files/papers/c_sockets.txt
 */

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h> 
#include <pthread.h> 

#define HTML_SIZE 5000 //this is the max size of the HTML file string sent to the server, make this larger if needed

pthread_mutex_t lock,lock2;

//Global variables 
double usagePercent = 0; // latest cpu percentage
double maxCPUPercent = 0.0; 
double avgCPUPercent; 
int terminate = 0; // 0 is false, meaning continute running.


void* terminate_server(void* p) {
    char user_input[256];

    while(1) {
        scanf("%s", user_input);

        if( strcmp(user_input,"q")==0 ){  
            //write to global
            pthread_mutex_lock(&lock2);
            terminate = 1;  // Will be used to terminate the server    
            pthread_mutex_unlock(&lock2); 
            //
            break;
        }

    }
    return NULL;    
}


void* calc_usage(void* p) {
    double dataForHour[3600];
	char first_word[10]; 
	int not_used;
	int idleTime;
	int loopCount = 0; 
	int prevCPU = 0;
	int newCPU = 0;
    int end = 3599;
    int i;
    int sum;
    
    double up=0; // usagePercent local variable
    double max=0; // maxCPUPercent local variable
    double avg=0; // avgCPUPercent local variable
    int t=0; // terminate local variable
    		
	while(1) {
        
        //read from global
        pthread_mutex_lock(&lock2);
        t=terminate;
        pthread_mutex_unlock(&lock2);
        //
        if (t==1) {
            printf("Shutting down\n");
            break;
        }
        
        FILE* fileProc = fopen("/proc/stat","r"); 
        if (fileProc == NULL) {
            printf("could not open file.");
            return (int*)-1;
        }    
		fscanf(fileProc, "%s %d %d %d %d %d %d %d %d %d %d", first_word, &not_used, &not_used, &not_used, &idleTime, &not_used, &not_used, &not_used, &not_used, &not_used, &not_used);
		if(loopCount==0) {
			prevCPU = idleTime;
			
		}
		else {
			newCPU = idleTime;
 			up = 100 - ((newCPU-prevCPU)/4); 
			prevCPU = newCPU; 
			if(up < 0) {
				up = 0.00;
			}
			else if (up > 100) {
				up = 100.00;
			
			}	
		}
        loopCount++;
        if (loopCount>1)
            printf("Latest CPU usage percentage %f \n", up);
        //Update CPU Max Percent
        if(up > max){
            max = up;
        }
        //Update CPU Avg Percent
        dataForHour[(loopCount-1)%3600] = up;
        if(loopCount>=3600) { 
            end = 3600;
            
        }
        else {
            end = loopCount;
        }
        sum = 0;
        for(i=0; i<end;i++){
            sum += dataForHour[i];
        }
        avg = (double) sum/end; 
        
       // printf("The average CPU percent for this round is %f \n", avg);
        
        
        //write to global
        pthread_mutex_lock(&lock);     
        usagePercent=up;
        maxCPUPercent=max;
        avgCPUPercent=avg;        
        pthread_mutex_unlock(&lock);   
        //
        
        fclose(fileProc); 
        
        sleep(1); // wait one second

	}
    return NULL;
}

void* start_server(void* p)
{
      
      int PORT_NUMBER=*(int*)p;
      int t=0; // terminate local variable
      double up; // usagePercent local variable
      double max; // maxCPUPercent local variable
      double avg; // avgCPUPercent local variable
    
    
        // convert index.html to one line string
        FILE* html_file = fopen("index.html","r"); 
        if (html_file == NULL) {
            printf("could not open html file.");
            return (int*)-1;
        }    
    
        char line[300];
        char html_one_line[HTML_SIZE]="HTTP/1.1 200 OK\nContent-Type: text/html\n\n";
        while (fgets (line, 300, html_file) != NULL) { 
          line[strlen(line)-1]='\0';
          strcat(html_one_line,line);
        }
        //
    
        fclose(html_file);
    
      // structs to represent the server and client
      struct sockaddr_in server_addr,client_addr;    
      
      int sock; // socket descriptor

      // 1. socket: creates a socket descriptor that you later use to make other system calls
      if ((sock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
	perror("Socket");
	exit(1);
      }
      int temp = 1;
      if (setsockopt(sock,SOL_SOCKET,SO_REUSEADDR,&temp,sizeof(int)) == -1) {
	perror("Setsockopt");
	exit(1);
      }

      // configure the server
      server_addr.sin_port = htons(PORT_NUMBER); // specify port number
      server_addr.sin_family = AF_INET;         
      server_addr.sin_addr.s_addr = INADDR_ANY; 
      bzero(&(server_addr.sin_zero),8); 
      
      // 2. bind: use the socket and associate it with the port number
      if (bind(sock, (struct sockaddr *)&server_addr, sizeof(struct sockaddr)) == -1) {
	perror("Unable to bind");
	exit(1);
      }

      // 3. listen: indicates that we want to listen to the port to which we bound; second arg is number of allowed connections
      if (listen(sock, 1) == -1) {
	perror("Listen");
	exit(1);
      }
          
      // once you get here, the server is set up and about to start listening
      //printf("\nServer configured to listen on port %d\n", PORT_NUMBER);
      fflush(stdout);
     
    int count = 0; // count the number of pages requested (for debugging purposes)
    while(1) { // keep looping and accept additional incoming connections
        
      //read from global  
      pthread_mutex_lock(&lock2);
      t = terminate;  //Will be used to close the server    
      pthread_mutex_unlock(&lock2);    
        
      if (t==1) break;
        
      // 4. accept: wait here until we get a connection on that port
      int sin_size = sizeof(struct sockaddr_in);
      int fd = accept(sock, (struct sockaddr *)&client_addr,(socklen_t *)&sin_size);
      if (fd != -1) {
         //printf("Server got a connection from (%s, %d)\n", inet_ntoa(client_addr.sin_addr),ntohs(client_addr.sin_port));

        // buffer to read data into
        char request[1024];

        // 5. recv: read incoming message (request) into buffer
        int bytes_received = recv(fd,request,1024,0);
        // null-terminate the string
        request[bytes_received] = '\0';
        // print it to standard out
        //printf("REQUEST:\n%s\n", request);
          
        count++; // increment counter for debugging purposes
          
        //read from global  
        pthread_mutex_lock(&lock);
        up=usagePercent;
        max=maxCPUPercent;
        avg=avgCPUPercent;
        pthread_mutex_unlock(&lock);
        //
          
        // this is the message that we'll send back
        char* response = (char*)malloc(HTML_SIZE * sizeof(char));
          
        //check if the request is a refresh or a button press (json request) 
        const char data[10] = "/data";
        char *ret;

        ret = strstr(request, data);
          
        if (ret != NULL) { // json request
            sprintf(response, "HTTP/1.1 200 OK\nContent-Type: application/json\n\n{\"up\" : \"%f\",\"max\" : \"%f\",\"avg\" : %f}", up, max, avg); 
        }
          else{ // refresh request
              sprintf(response, html_one_line, count, up, max, avg);  
          }
 
        //printf("RESPONSE:\n%s\n", response);

        // 6. send: send the outgoing message (response) over the socket
        // note that the second argument is a char*, and the third is the number of chars	
        send(fd, response, strlen(response), 0);

          free(response);

        // 7. close: close the connection
        close(fd);
        //printf("Server closed connection\n");
       }
    }

    // 8. close: close the socket
    
    close(sock);
    //printf("Server shutting down\n");
  
    return 0;
} 
 


int main(int argc, char *argv[])
{
 
    /*
  // check the number of arguments
  if (argc != 2) {
      printf("\nUsage: %s [port_number]\n", argv[0]);
      exit(-1);
  }

  int port_number = atoi(argv[1]);
  if (port_number <= 1024) {
    printf("\nPlease specify a port number greater than 1024\n");
    exit(-1);
  }*/
    
    pthread_t t1,t2,t3; 
    
    pthread_mutex_init(&lock,NULL);
    pthread_mutex_init(&lock2,NULL);
    
    int port_number = 3000; // hard-coded for use on Codio
    
    
    pthread_create(&t1, NULL, start_server, &port_number);
    
    pthread_create(&t2, NULL, calc_usage, NULL);
    
    pthread_create(&t3, NULL, terminate_server,NULL);
    
    pthread_join(t1, NULL);
    
    pthread_join(t2, NULL);
    
    pthread_join(t3, NULL); 

    return 0;

}

