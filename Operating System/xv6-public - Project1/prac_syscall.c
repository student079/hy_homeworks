#include "types.h"
#include "defs.h"

//Simple system call
int myfunction(char *str)
{
    cprintf("%s\n",str);
    return 0xABCD;
}

int sys_myfunction(void){
    char* str;

    if (argstr(0, &str) < 0)
    return -1;
    return myfunction(str);
}