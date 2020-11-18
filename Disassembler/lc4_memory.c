/************************************************************************/
/* File Name : lc4_memory.c		 										*/
/* Purpose   : This file implements the linked_list helper functions	*/
/* 			   to manage the LC4 memory									*/
/*             															*/
/* Author(s) : Alon         											*/
/************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "lc4_memory.h"


/*
 * adds a new node to linked list pointed to by head
 */
int add_to_list (row_of_memory** head,
		 short unsigned int address,
		 short unsigned int contents)
{
    
	/* allocate memory for a single node */

	/* populate fields in newly allocated node w/ address&contents */

	/* if head==NULL, node created is the new head of the list! */

	/* otherwise, traverse linked list until reach the right spot to insert this node */

	/* insert node into the list - perform necessary "surgery" on this list */

	/* return 0 for success, -1 if malloc fails */
    
    row_of_memory* new_row = malloc ( sizeof(row_of_memory));
    if (new_row == NULL)  return -1;
    new_row->address=address;
    new_row->label=NULL;
    new_row->contents=contents;
    new_row->assembly=NULL;
    new_row->next=NULL;
    if ((*head) == NULL) 
    {
        *head=new_row;
        return 0;
    }
    
    if ( address < (*head)->address )
    {
        new_row->next=(*head);
        (*head)=new_row;
        return 0;
    }
    
    row_of_memory* p=*head;
        
    while ((*head)->next != NULL ) 
    {   
        if ( address > (*head)->address  && address < ((*head)->next)->address )
        {
            new_row->next=(*head)->next;
            break;
        }         
        (*head) = (*head)->next;
    }
              
    (*head)->next = new_row;
    
    *head=p;

    return 0 ;

}


/*
 * search linked list by address field, returns node if found
 */
row_of_memory* search_address (row_of_memory* head,
			       short unsigned int address )
{
	/* traverse linked list, searching each node for "address"  */

	/* return pointer to node in the list if item is found */

	/* return NULL if list is empty or if "address" isn't found */

    while (head != NULL)
    {
      if (head->address == address)
      {
          return head ;
      }
      head=head->next;
    }
    
	return NULL ;
}

/*
 * search linked list by opcode field, returns node if found
 */
row_of_memory* search_opcode  (row_of_memory* head,
				      short unsigned int opcode  )
{
	/* traverse linked list until node is found with matching opcode
	   AND "assembly" field of node is empty */

	/* return pointer to node in the list if item is found */

	/* return NULL if list is empty or if no matching nodes */

	while (head != NULL)
    {
      if (((head->contents>>12) == opcode) && (head->assembly == NULL))
      {
          return head ;
      }
      head=head->next;
    }
    
	return NULL ;
}


void print_list (row_of_memory* head )
{
	/* make sure head isn't NULL */

	/* print out a header */

	/* traverse linked list, print contents of each node */
    
    if (head==NULL)
        return ;
    
    printf("\n<label>       <address>       <contents>       <assembly>");
    while (head != NULL)
    {
        if (head->label != NULL)
        {
            printf("\n%s",head->label);
            for(int i=0; i<(14-(strlen(head->label))); i++)
                printf(" ");
        }
        else printf("\n              ");
        printf("%.4X            ",head->address);
        printf("%.4X             ",head->contents);
        if (head->assembly != NULL)
            printf("%s",head->assembly);
        head=head->next;
    }

    printf("\n");  

}

/*
 * delete entire linked list
 */
int delete_list    (row_of_memory** head )
{
    
	/* delete entire list node by node */
	/* if no errors, set head = NULL upon deletion */

	/* return 0 if no error, -1 for any errors that may arise */
    
    while ((*head) != NULL)
    {
        row_of_memory* temp = (*head)->next;
        if ( ((*head)->label)!=NULL )
            free((*head)->label);
        if ( ((*head)->assembly)!=NULL )
            free((*head)->assembly);
        free((*head));
        (*head) = temp;
    }
    
    *head=NULL;
    
	return 0 ;
}
