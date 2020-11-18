/************************************************************************/
/* File Name : lc4.c 													*/
/* Purpose   : This file contains the main() for this project			*/
/*             main() will call the loader and disassembler functions	*/
/*             															*/
/* Author(s) : Alon     												*/
/************************************************************************/

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "lc4_memory.h"
#include "lc4_loader.h"
#include "lc4_disassembler.h"

/* program to mimic pennsim loader and disassemble object files */

int main (int argc, char** argv) {
    
    char* file_name = NULL;	// name of OBJ file
    int length; // length of OBJ file name
    FILE* my_obj_file;
    
	/**
	 * main() holds the linked list &
	 * only calls functions in other files
	 */

	/* step 1: create head pointer to linked list: memory 	*/

	row_of_memory* memory = NULL ;
    
	/* step 2: determine filename, then open it		*/
	/*   TODO: extract filename from argv, pass it to open_file() */
 
    if (argc != 2) 
    {
        printf("error1: usage: ./lc4 <object_file.obj>\n");
        return 1;
    }
    file_name=argv[1];
    length=strlen(file_name);
    if (length < 5) 
    {
        printf("error1: usage: ./lc4 <object_file.obj>\n");
        return 1;
    }
    if (file_name[length-1]!='j' || file_name[length-2]!='b' || file_name[length-3]!='o' || file_name[length-4]!='.') 
    {
        printf("error1: usage: ./lc4 <object_file.obj>\n");
        return 1;
    }
    
    my_obj_file=open_file(file_name);
    if (my_obj_file==NULL) 
    {
        printf("error1: usage: ./lc4 <object_file.obj>\n");
        return 1;
    }

	/* step 3: call function: parse_file() in lc4_loader.c 	*/
	/*   TODO: call function & check for errors		*/
    
    if(parse_file(my_obj_file,&memory) != 0)
    {
        printf("error2: parse_file() failed\n");
        delete_list(&memory);
        return 2;
    }
    
	/* step 4: call function: reverse_assemble() in lc4_disassembler.c */
	/*   TODO: call function & check for errors		*/
    
    if(reverse_assemble(memory) != 0)
    {
        printf("error3: reverse_assemble() failed\n");
        delete_list(&memory);
        return 3;
    }

	/* step 5: call function: print_list() in lc4_memory.c 	*/
	/*   TODO: call function 				*/
    
    print_list(memory);

	/* step 6: call function: delete_list() in lc4_memory.c */
	/*   TODO: call function & check for errors		*/
    
    if(delete_list(&memory) != 0)
    {
        printf("error4: delete_list() failed\n");
        return 4;
    }

	/* only return 0 if everything works properly */
    

    /* unit test
     * 
    // Test add_to_list()
    add_to_list(&memory,4,5);
    
    printf("%d\n",memory->address);
    
    printf("%d\n",memory->contents);
    
    add_to_list(&memory,6,7);
    
    printf("%d\n",memory->address);
    
    printf("%d\n",memory->contents);
    
    // Test search_address()
    
    row_of_memory* searched_address = NULL ;
    searched_address=search_address(memory,6);
    printf("%d\n",searched_address->contents);
    
    // Test print_list()
     
    print_list(memory);
    
    //Test delete_list()
    
    delete_list(&memory);
    */

	return 0 ;
}
