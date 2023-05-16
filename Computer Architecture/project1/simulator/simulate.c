/* LC-2K Instruction-level simulator */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define NUMMEMORY 65536 /* maximum number of words in memory */
#define NUMREGS 8 /* number of machine registers */
#define MAXLINELENGTH 1000 
typedef struct stateStruct {
    int pc;
    int mem[NUMMEMORY];
    int reg[NUMREGS];
    int numMemory;
} stateType;

void printState(stateType *);
int convertNum(int num);

int main(int argc, char *argv[])
{
    char line[MAXLINELENGTH];
    stateType state;
    FILE *filePtr;
    int total = 0;
    int ins;
    int op;
    int arg0;
    int arg1;
    int arg2;
    int offset;

    if (argc != 2) {
        printf("error: usage: %s <machine-code file>\n", argv[0]);
        exit(1);
    }

    filePtr = fopen(argv[1], "r");
    if (filePtr == NULL) {
        printf("error: can't open file %s", argv[1]);
        perror("fopen");
        exit(1);
    }

    /* read in the entire machine-code file into memory */
    for (state.numMemory = 0; fgets(line, MAXLINELENGTH, filePtr) != NULL;
            state.numMemory++) {

        if (sscanf(line, "%d", state.mem+state.numMemory) != 1) {
            printf("error in reading address %d\n", state.numMemory);
            exit(1);
        }
        printf("memory[%d]=%d\n", state.numMemory, state.mem[state.numMemory]);
    }

	// Initialize
    state.pc = 0;
    for (int i = 0; i < NUMREGS; i++)
        state.reg[i] = 0;

    while(1) {
        // decode
        ins = state.mem[state.pc];
        op = ins >> 22;
        arg0 = (ins >> 19) & 0x7;
        arg1 = (ins >> 16) & 0x7;
        arg2 = ins & 0x7;
        offset = ins & 0xFFFF;

        printState(&state);

        // add
        if(op == 0){
            state.reg[arg2] = state.reg[arg0] + state.reg[arg1];
        }
        // nor
        else if(op == 1){
            state.reg[arg2] = ~(state.reg[arg0] | state.reg[arg1]);
        }
        // lw
        else if(op == 2){
            state.reg[arg1] = state.mem[convertNum(state.reg[arg0] + offset)];
        }
        // sw
        else if(op == 3){
            state.mem[convertNum(state.reg[arg0] + offset)] = state.reg[arg1];
        }
        // beq
        else if(op == 4){
            if(state.reg[arg0] == state.reg[arg1])
                state.pc = state.pc + convertNum(offset);
        }
        // jalr
        else if(op == 5){
            state.reg[arg1] = state.pc + 1;
            state.pc = state.reg[arg0] - 1;
        }
        // halt
        else if(op == 6){
            state.pc++;
            total++;
            break;
        }
        // noop
        else if(op == 7){}

        state.pc++;
        total++;
    }
    

    printf("machine halted\n");
    printf("total of %d instructions executed\n", total);
    printf("final state of machine:\n");
    printState(&state);
    return(0);
}

void printState(stateType *statePtr)
{
    int i;
    printf("\n@@@\nstate:\n");
    printf("\tpc %d\n", statePtr->pc);
    printf("\tmemory:\n");
    for (i = 0; i < statePtr->numMemory; i++) {
        printf("\t\tmem[ %d ] %d\n", i, statePtr->mem[i]);
    }
    printf("\tregisters:\n");
    for (i = 0; i < NUMREGS; i++) {
        printf("\t\treg[ %d ] %d\n", i, statePtr->reg[i]);
    }
    printf("end state\n");
}

int convertNum(int num)
{
	/* convert a 16-bit number into a 32-bit Linux integer */
	if (num & (1 << 15)) {
		num -= (1 << 16);
	}
	return (num);
}
