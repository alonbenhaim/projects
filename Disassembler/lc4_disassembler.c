/************************************************************************/
/* File Name : lc4_disassembler.c 					                   					*/
/* Purpose   : This file implements the reverse assembler 			       	*/
/*             for LC4 assembly.  It will be called by main()		       	*/
/*             														                             	*/
/* Author(s) : Alon												                              */
/************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "lc4_memory.h"

int reg_num(char* str,short unsigned int reg,int last)
{
        switch(reg) 
        {

           case 0  :
              strcat(str," R0");
              break;

           case 1  :
              strcat(str," R1");
              break;

           case 2  :
              strcat(str," R2");
              break;

           case 3  :
              strcat(str," R3");
              break;
                
           case 4  :
              strcat(str," R4");
              break;

           case 5  :
              strcat(str," R5");
              break;

           case 6  :
              strcat(str," R6");
              break;

           case 7  :
              strcat(str," R7");
              break;

           default : 
              return -1; 
        }
    
        if (!last) strcat(str,",");
    
        return 0;

}

int reverse_assemble (row_of_memory* memory)
{
    short unsigned int sub_opcode;
    short unsigned int reg;
    row_of_memory* searched_opcode;
    
    searched_opcode=search_opcode(memory,0x0001);
    while (searched_opcode != NULL)
    {
        char assembly[50];
        char imm5[3];
        sub_opcode=(searched_opcode->contents & 0x003f)>>3;
        switch(sub_opcode) 
        {

           case 0  :
              strcpy(assembly,"ADD");
              break; 

           case 1  :
              strcpy(assembly,"MUL");
              break; 

           case 2  :
              strcpy(assembly,"SUB");
              break; 

           case 3  :
              strcpy(assembly,"DIV");
              break; 

           default : 
              strcpy(assembly,"ADD");
              break; 
        }
        reg=(searched_opcode->contents & 0x0fff)>>9; // Rd
        if(reg_num(assembly,reg,0))
            return -1;
        
        reg=(searched_opcode->contents & 0x01ff)>>6; // Rs
        if(reg_num(assembly,reg,0))
            return -1;
        
        if (sub_opcode > 3) //ADD IMM5
        {
            reg=(searched_opcode->contents & 0x001f);
            sprintf(imm5,"%.2X",reg);
            imm5[2]='\0';
            strcat(assembly," x");
            //printf("right here %s",imm5);
            strcat(assembly,imm5);
        }
        else
        {
            reg=(searched_opcode->contents & 0x0007); // Rt
            if(reg_num(assembly,reg,1))
            return -1;
        }
           
        searched_opcode->assembly=malloc( sizeof(char) * ((strlen(assembly))+1) );
        strcpy(searched_opcode->assembly,assembly);
        searched_opcode=search_opcode(memory,0x0001); 
    }
    
	return 0 ;
}

