        lw      0   1   one     Load 1 into R1
        lw      0   2   sum     Load memory location for sum into R2
        lw      0   3   ten
        lw      0   4   one     
loop    add     2   1   2       Add R1 to sum in memory
        add     4   1   1       Increment R1 by 1
        beq     1   3   done    If R1 equals 10, exit the loop
        beq     0   0   loop    Otherwise, loop back to the start
done    halt                    Halt the program
sum     .fill   0               Initialize sum to 0
one     .fill   1               Constant for incrementing R1
ten     .fill   10              Constant for exit condition
