/************************************************************************/
/* File Name : lc4_loader.c		 									                       	*/
/* Purpose   : This file implements the loader (ld) from PennSim	     	*/
/*             It will be called by main()						              		*/
/*             													                            		*/
/* Author(s) : Alon											                              	*/
/************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "lc4_memory.h"

/* declarations of functions that must defined in lc4_loader.c */

FILE* open_file(char* file_name)
{
    FILE *file = fopen (file_name, "rb"); 
    if (file == NULL)
        return NULL ;
   
    return file ;
}

int parse_file (FILE* my_obj_file, row_of_memory** memory)
{
    unsigned short int header;
    unsigned short int address;
    unsigned short int n;
    unsigned short int contents;
    char* label;
    row_of_memory* searched_address;
    
    while (fread(&header, sizeof(unsigned short int),1,my_obj_file)) 
    {

        header=(header << 8) | (header >> 8);
        fread(&address, sizeof(unsigned short int),1,my_obj_file);
        address=(address << 8) | (address >> 8);
        fread(&n, sizeof(unsigned short int),1,my_obj_file);
        n=(n << 8) | (n >> 8);
        
        if (header == 0xcade || header == 0xdada)
            while (n>0)
               {
                    fread(&contents, sizeof(unsigned short int),1,my_obj_file);
                    contents=(contents << 8) | (contents >> 8);
                    add_to_list(memory,address++,contents);
                    n--;
               }
         else
             if (header == 0xc3b7)
             {
                   label=malloc((sizeof(char)*n)+1);
                   if (label == NULL) return 2;
                   int i=0;
                   while (n>0)
                   {
                       fread(&label[i], sizeof(char),1,my_obj_file);
                       i++;
                       n--;
                   }
                   label[i]='\0';
                   searched_address=search_address((*memory),address);
                   if (searched_address==NULL)
                   {
                      add_to_list(memory,address,0);
                      searched_address=search_address((*memory),address);
                      searched_address->label=label; 
                   }
                   searched_address->label=label; 
             }
             else return 2 ; // error check header is not cade, dada or c3b7.  
 
    }
    
    if (fclose(my_obj_file)!=0) return 2 ;

    return 0 ;
}
