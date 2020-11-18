CONST R0, #-2		
ADD R1, R0, #0	
LOOP  
CMPI R0, #1		  
BRnz END 			  
ADD R0, R0, #-1	
MUL R1, R1, R0	
BRnzp LOOP 		  
END