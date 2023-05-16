	lw	0	0	zero
	lw	0	1	init	load init value into r1
	lw	0	2	mcand	load multiplicand into r2
	lw	0	3	mplier	load multiplier into r3
	lw	0	4	idx		load index into r4
	lw	0	5	imax	load max index into r5
	lw	0	6	zero	load zero into r6 to check the bit set or clear
loop	nor	3	3	3 	~r3
	nor	4	4	4		~r4
	nor	3	4	6		check mplier idxth bit is set or clear
	nor	3	3	3		~r3
	nor	4	4	4		~r4
	beq	0	6	inc		if mplier idxth bit is 0, skip addition part
	add	2	1	1		if 1, add multiplicand to r1
inc	add	4	4	4		increment index
	add	2	2	2		shift left multiplicand like *2
	beq	4	5	fin		if idx is imax, finish
	beq	0	0	loop	else go to loop
fin	halt				halt
mcand	.fill	32766
mplier	.fill 	12328
idx	.fill	1
init	.fill	0
zero	.fill	0
one	.fill	1
imax	.fill	32768
