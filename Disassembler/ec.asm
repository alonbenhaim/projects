;; This is the data section
	.DATA
	.ADDR x4020		; Start the data at address 0x4020
	
global_array
	.FILL #6
	.FILL #5
	.FILL #8
	.FILL #10
	.FILL #-5

	;; Start of the code section
	.CODE
	.ADDR 0x0000		; Start the code at address 0x0000
INIT
	LEA R0 global_array ; R0 contains the address of the data
	CONST R1 5		    ; R1 is our loop counter init to 5
	JMP TEST
BODY
	JSR SUB_FACTORIAL   ; Call the subroutine to do factorial
	ADD R0 R0 #1		; increment the address
	ADD R1 R1 #-1		; decrement the loop counter
TEST
	CMPI R1, #0		; check if the loop counter is zero yet
	BRp BODY        ; if R1 > 0, jump to BODY, otherwise jump to END
	JMP END

.FALIGN              ; ALIGNS THE SUBROUTINE
	SUB_FACTORIAL
		LDR R2 R0 #0		; Load the data value into R2
		CHECK
			CMPI R2 #0      ;
			BRn SET         ; IF R2 < 0, GO TO SET
			CMPI R2 #8
			BRp SET    	    ; IF R2 > 8, GO TO SET
			JMP END_SET
		SET
			CONST R3 #-1    ; FACTORIAL = -1
			JMP END_LOOP    ; Jump to store R3 to data memory
			END_SET  
		ADD R3 R2 #0     	; R3 stores the factorial product, init to number in R2
		LOOP
			CMPI R2 #1      ; WHILE LOOP
			BRnz END_LOOP   ; A > 1? If not, jump out of loop
			ADD R2 R2 #-1   ; R2 = R2 - 1
			MUL R3 R3 R2    ; R3 = R3 * R2
		BRnzp LOOP  	
		END_LOOP			; END LOOP
		STR R3 R0 #0        ; Store the factorial in R3 back to data memory

	RET                 ; END SUBROUTINE
END	 
