#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <unistd.h>
#include <signal.h>
#include <sys/utsname.h>
#include <time.h>

#define beaconCmdPort 9993

typedef enum { false, true } bool;

bool isBigEndian = false;

int currentSeconds(void);
void GetLocalOS(int agent_socket);
void GetLocalTime(int agent_socket);
void checkEndian();

void *BeaconThread(void *vargp){

	srand(time(NULL));

	typedef struct Beacon {
		int ID;
		int startTime;//int startTime;//char startTime[3]; // Make 64 bit integer
		//int startTime[3];
		char IP[4];
		int CmdPort;

	} Beacon;


	Beacon b;

	char hostbuffer[256];
	    char *IPbuffer;
	    struct hostent *host_entry;
	    int hostname;

	    // To retrieve hostname
	    hostname = gethostname(hostbuffer, sizeof(hostbuffer));
	    host_entry = gethostbyname(hostbuffer);
	    IPbuffer = inet_ntoa(*((struct in_addr*)
	                               host_entry->h_addr_list[0]));

	    printf("IP: %s",IPbuffer);
	    printf("ip1: %d",IPbuffer[0]);


	b.ID = rand() % 101;
	b.startTime = currentSeconds();
	b.IP[0] = 127;
	b.IP[1] = 0;
	b.IP[2] = 0;
	b.IP[3] = 1;
	b.CmdPort = 9993;
	int socketfd, len;

	struct sockaddr_in servaddr,cliaddr;
	len = sizeof(servaddr);
	socketfd=socket(AF_INET,SOCK_DGRAM,0);

	bzero(&servaddr,len);

	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr=inet_addr("127.0.0.1");
	servaddr.sin_port=htons(beaconCmdPort);

	int size = sizeof(Beacon);
	char buf[size];

	int fromlen = sizeof(struct sockaddr_in);

	memset(buf, 0x00, size);
	memcpy(buf, &b, size);

	while(1){

		int n = sendto(socketfd,buf,sizeof(buf),0, (struct sockaddr *)&servaddr,fromlen);
		while (n==-1){
			printf("Failed to connect.\n");
			n = sendto(socketfd,buf,sizeof(buf),0, (struct sockaddr *)&servaddr,fromlen);
		}
		printf("sent agent with ID: %d\n", b.ID);
		usleep(60000000);
	}
	return 0;
}

int currentSeconds(void){

	time_t now = time(NULL);
	time(&now);
	struct tm *time = localtime(&now);
	return (time->tm_hour * 3600) + (time->tm_min * 60) + (time->tm_sec);
}

void GetLocalOS(int agent_socket){

	struct utsname OS_Name;
	uname(&OS_Name);
	//int size = sizeof(OS_Name.sysname);
	write(agent_socket,OS_Name.sysname,sizeof(OS_Name.sysname));
}

void GetLocalTime(int agent_socket){

	time_t rawtime = time(NULL);
	char array[24];
	time(&rawtime);
	struct tm  *timeinfo = localtime(&rawtime);
	strftime(array, sizeof(array), "%Y/%m/%d %H:%M:%S", timeinfo);
	write(agent_socket,array,sizeof(array));
}

int main(){
	//Creates new thread for beacon UDP.
	pthread_t beaconThread;
	pthread_create(&beaconThread,NULL, BeaconThread,NULL);

	//TCP implementation
	int socketfd, connfd, len;
	struct sockaddr_in servaddr, cli;

	socketfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	//Created Socket
	if (socketfd == -1) {
	    printf("socket creation failed...\n");
	    exit(0);
	}
	bzero(&servaddr, sizeof(servaddr));

    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_port = htons(9992);
    servaddr.sin_addr.s_addr= INADDR_ANY;

    bind(socketfd, (struct sockaddr *)&servaddr, sizeof(servaddr));

	listen(socketfd,5);
	int count = 0;
	char server_message;
	while(1){
		struct sockaddr client_addr;
		unsigned int client_len;
		int agent_socket = accept(socketfd, &client_addr, &client_len);
		recv(agent_socket,(char *)&server_message,1,0);
		if(server_message == 49){
			GetLocalOS(agent_socket);
		}
		else{
			GetLocalTime(agent_socket);
		}
	}

	pthread_join(beaconThread,NULL);
	return 0;
}
