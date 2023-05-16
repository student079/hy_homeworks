/* Assembler code fragment for LC-2K */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define MAXLINELENGTH 1000
#define MAXLABELNAMELENGTH 7

int readAndParse(FILE *, char *, char *, char *, char *, char *);
int isNumber(char *);
int regTest(char* , char* );

int main(int argc, char *argv[]) 
{
	char *inFileString, *outFileString;
	FILE *inFilePtr, *outFilePtr;
	char label[MAXLINELENGTH], opcode[MAXLINELENGTH], arg0[MAXLINELENGTH], 
			 arg1[MAXLINELENGTH], arg2[MAXLINELENGTH];
	int PC = 0;
	char assignedLabel[MAXLINELENGTH][MAXLABELNAMELENGTH];

	if (argc != 3) {
		printf("error: usage: %s <assembly-code-file> <machine-code-file>\n",
				argv[0]);
		exit(1);
	}

	inFileString = argv[1];
	outFileString = argv[2];

	inFilePtr = fopen(inFileString, "r");
	if (inFilePtr == NULL) {
		printf("error in opening %s\n", inFileString);
		exit(1);
	}
	outFilePtr = fopen(outFileString, "w");
	if (outFilePtr == NULL) {
		printf("error in opening %s\n", outFileString);
		exit(1);
	}

	/* TODO: Phase-1 label calculation */
	while (readAndParse(inFilePtr, label, opcode, arg0, arg1, arg2)) {
		if (strcmp(label,"")) {
			// duplicate label check
			for (int i = 0; i< MAXLINELENGTH; i++)
				if (!strcmp(label,assignedLabel[i])) {
					printf("duplicate label error: %s\n", label);
					exit(1);
				}
			
			// Label name validation test
			if (atoi(label)) {
				printf("label name error: %s\n", label);
				exit(1);
			}
			strcpy(assignedLabel[PC], label);
		}
		PC++;
	}

	/* this is how to rewind the file ptr so that you start reading from the
		 beginning of the file */
	rewind(inFilePtr);

	/* TODO: Phase-2 generate machine codes to outfile */
	PC = 0;
	while (readAndParse(inFilePtr, label, opcode, arg0, arg1, arg2)) {
		int op;
		int s;
		int t;
		int offset;
		int ins;

		// add
		if (!strcmp(opcode,"add")) {
			op = 0;
			if (!regTest(arg0,arg1)) exit(1);
			s = atoi(arg0);
			t = atoi(arg1);
			offset = atoi(arg2);
		}
		// nor
		else if (!strcmp(opcode,"nor")) {
			op = 1;
			if (!regTest(arg0,arg1)) exit(1);
			s = atoi(arg0);
			t = atoi(arg1);
			offset = atoi(arg2);
		}
		// lw
		else if (!strcmp(opcode,"lw")) {
			op = 2;
			if (!regTest(arg0,arg1)) exit(1);
			s = atoi(arg0);
			t = atoi(arg1);
			if (isNumber(arg2))
				offset = atoi(arg2);
			else {
				int i = 0;
				for (i = 0; i < MAXLINELENGTH ; i++){
					if (!strcmp(arg2,assignedLabel[i])){
						offset = i;
						break;
					}
				}

				// using undefined label
				if (i == MAXLINELENGTH) {
					printf("undefined label error: %s\n",arg2);
					exit(1);
				}
			}
		}
		// sw
		else if (!strcmp(opcode,"sw")) {
			op = 3;
			if (!regTest(arg0,arg1)) exit(1);
			s = atoi(arg0);
			t = atoi(arg1);
			if (isNumber(arg2))
				offset = atoi(arg2);
			else {
				int i = 0;
				for (i = 0; i < MAXLINELENGTH ; i++){
					if (!strcmp(arg2,assignedLabel[i])){
						offset = i;
						break;
					}
				}

				// using undefined label
				if (i == MAXLINELENGTH) {
					printf("undefined label error: %s\n",arg2);
					exit(1);
				}
			}
		}
		// beq
		else if (!strcmp(opcode,"beq")) {
			op = 4;
			if (!regTest(arg0,arg1)) exit(1);
			s = atoi(arg0);
			t = atoi(arg1);
			if (isNumber(arg2))
				offset = atoi(arg2);
			else {
				int i = 0;
				for (i = 0; i < MAXLINELENGTH ; i++){
					if (!strcmp(arg2,assignedLabel[i])){
						offset = i - PC - 1;
						break;
					}
				}

				// using undefined label
				if (i == MAXLINELENGTH) {
					printf("undefined label error: %s\n",arg2);
					exit(1);
				}
			}
		}
		// jalr
		else if (!strcmp(opcode,"jalr")) {
			op = 5;
			if (!regTest(arg0,arg1)) exit(1);
			s = atoi(arg0);
			t = atoi(arg1);
			offset = 0;
		}
		// halt
		else if (!strcmp(opcode,"halt")) {
			op = 6;
			s = t = offset = 0;
		}
		// noop
		else if (!strcmp(opcode,"noop")) {
			op = 7;
			s = t = offset = 0;
		}
		// .fill
		else if (!strcmp(opcode,".fill")) {
			if (isNumber(arg0))
				ins = atoi(arg0);
			else {
				int i = 0;
				for (i = 0; i < MAXLINELENGTH ; i++){
					if (!strcmp(arg0,assignedLabel[i])){
						ins = i;
						break;
					}
				}

				// using undefined label
				if (i == MAXLINELENGTH) {
					printf("undefined label error: %s\n",arg0);
					exit(1);
				}
			}

			fprintf(outFilePtr, "%d\n", ins);
			PC++;
			continue;
		}
		// Unrecognized opcodes
		else {
			printf("Unrecognized opcode error: %s\n",opcode);
			exit(1);
		}

		// only least 16bit
		offset = offset & 0xFFFF;

		ins = (op << 22) | (s << 19) | (t << 16) | offset;

        fprintf(outFilePtr, "%d\n", ins);

		PC++;

	}

	if (inFilePtr) {
		fclose(inFilePtr);
	}
	if (outFilePtr) {
		fclose(outFilePtr);
	}

	// without error
	exit(0);
}

/*
 * Read and parse a line of the assembly-language file.  Fields are returned
 * in label, opcode, arg0, arg1, arg2 (these strings must have memory already
 * allocated to them).
 *
 * Return values:
 *     0 if reached end of file
 *     1 if all went well
 *
 * exit(1) if line is too long.
 */
int readAndParse(FILE *inFilePtr, char *label, char *opcode, char *arg0,
		char *arg1, char *arg2)
{
	char line[MAXLINELENGTH];
	char *ptr = line;

	/* delete prior values */
	label[0] = opcode[0] = arg0[0] = arg1[0] = arg2[0] = '\0';

	/* read the line from the assembly-language file */
	if (fgets(line, MAXLINELENGTH, inFilePtr) == NULL) {
		/* reached end of file */
		return(0);
	}

	/* check for line too long (by looking for a \n) */
	if (strchr(line, '\n') == NULL) {
		/* line too long */
		printf("error: line too long\n");
		exit(1);
	}

	/* is there a label? */
	ptr = line;
	if (sscanf(ptr, "%[^\t\n\r ]", label)) {
		/* successfully read label; advance pointer over the label */
		ptr += strlen(label);
	}

	/*
	 * Parse the rest of the line.  Would be nice to have real regular
	 * expressions, but scanf will suffice.
	 */
	sscanf(ptr, "%*[\t\n\r ]%[^\t\n\r ]%*[\t\n\r ]%[^\t\n\r ]%*[\t\n\r ]%"
			"[^\t\n\r ]%*[\t\n\r ]%[^\t\n\r ]", opcode, arg0, arg1, arg2);
	return(1);
}

int isNumber(char *string)
{
	/* return 1 if string is a number */
	int i;
	return( (sscanf(string, "%d", &i)) == 1);
}

int regTest(char* arg0, char* arg1)
{
	/* return 1 if arg0, arg1 are valid*/
	if ((atof(arg0) - atoi(arg0) > 0) || (atof(arg1) - atoi(arg1) > 0)) {
		printf("not integer reg error: %s %s\n",arg0, arg1);
		return 0;
	}
	if ((atoi(arg0) < 0 || atoi(arg0) > 7) || (atoi(arg1) < 0 || atoi(arg1) > 7)) {
		printf("reg range error: %s %s\n",arg0,arg1);
		return 0;
	}

	return 1;
}