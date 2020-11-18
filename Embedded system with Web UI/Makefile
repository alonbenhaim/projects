ARGS = -Wall -g

all: clean server
	
server:
	cc -pthread -o server $(ARGS) server.c 
	
clean: 
	rm -rf server
