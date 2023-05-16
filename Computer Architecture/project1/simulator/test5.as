        lw      0   0   zero
        lw      0   1   x       Load value of x into r1
        lw      0   2   y       Load value of y into r2
        lw      0   3   zero
        lw      0   4   r4
        lw      0   5   stAddr
loop    add     1   2   3       r1 + r2 into r3
        sw      0   3   z       Store value of r3 in memory location z
        beq     1   2   done    If r1 equals r2, exit loop
        add     4   1   1       Add value of r4 to r1
        jalr    5   0           Jump to beginning of loop
done    halt                    Stop execution
x       .fill   10              Define value of x as 10
y       .fill   16              Define value of y as 16
z       .fill   0               Define memory location z for storing results
r4      .fill   2               Define value of r4 as 2
zero    .fill   0
stAddr  .fill   loop
