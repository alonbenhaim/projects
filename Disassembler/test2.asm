  .DATA                
  .ADDR x7000          

global_string          
  .FILL x4C            
  .FILL x65            
  .FILL x6E           
  .FILL x67           
  .FILL x74           
  .FILL x68            
  .FILL x20           
  .FILL x3D           
  .FILL x20           
  .FILL #0             

.CODE
.ADDR x0000

  CONST R0, x20 
  HICONST R0, x20        
  AND R1, R1, #0         
  TRAP x02              

  TEMPS .UCONST x7100    
  LC R7, TEMPS           
  STR R1, R7, #0         
  
  LEA R0, global_string  
  TRAP x03               
label1
  LC R7, TEMPS           
  LDR R1, R7, #0         
  CONST R2, x30          
  ADD R0, R1, R2         

  TRAP x01              

  CONST R0, x0A          
  TRAP x01               
label2
  CONST R0, x20 
  HICONST R0, x20        

  TRAP x03               

END
