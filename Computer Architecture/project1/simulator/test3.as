        lw      0   1   value   Load initial value into R1
        lw      0   2   zero
        add     1   1   2       Increment R1
        halt                    Halt the program
value   .fill   42              Value to load into R1
zero    .fill   0
