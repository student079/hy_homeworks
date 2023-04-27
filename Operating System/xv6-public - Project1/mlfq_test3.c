#include "types.h"
#include "stat.h"
#include "user.h"

#define NUM_LOOP 100000
#define NUM_YIELD 20000
#define NUM_SLEEP 1000

#define NUM_THREAD 4
#define MAX_LEVEL 5

int parent;

int fork_children()
{
  int i, p;
  for (i = 0; i < NUM_THREAD; i++)
    if ((p = fork()) == 0)
    {
      sleep(10);
      return getpid();
    }
  return parent;
}

void exit_children()
{
  if (getpid() != parent)
    exit();
  while (wait() != -1);
}

int main(int argc, char *argv[])
{
  int pid, i;
  int count[MAX_LEVEL] = {0};
//  int child;

  parent = getpid();

  printf(1, "MLFQ test start\n");

  printf(1, "[Test 2] schdulerlockunlock\n");
  pid = fork_children();

  if (pid != parent)
  {
    printf(1, "Process %d\n", pid);
    if (pid == 4){
        printf(1, "Lock()\n");
        schedulerLock(2018079116);

    for (i = 0; i < NUM_LOOP; i++) {
        int x = getLevel();
    if (x < 0 || x > 4){
        printf(1, "Wrong level: %d\n", x);
        exit();
    }
        count[x]++;
    }
    for (i = 0; i < MAX_LEVEL; i++)
        printf(1, "L%d: %d\n", i, count[i]);
    }

    if (pid == 5) {
        printf(1, "Lock()\n");
        schedulerLock(2018079116);
        printf(1,"unlock\n");
        schedulerUnlock(2018079116);

        printf(1, "unlock()\n");
        schedulerUnlock(2018079116);

    for (i = 0; i < NUM_LOOP; i++) {
        int x = getLevel();
    if (x < 0 || x > 4){
        printf(1, "Wrong level: %d\n", x);
        exit();
    }
        count[x]++;
    }
    for (i = 0; i < MAX_LEVEL; i++) {
        printf(1, "L%d: %d\n", i, count[i]);
        printf(1,"5555\n");
        }
    }

    if (pid == 7) {
        printf(1,"passwordcheck");
        schedulerLock(20180);

        for (i = 0; i < NUM_LOOP; i++)
    {
      int x = getLevel();
      if (x < 0 || x > 4)
      {
        printf(1, "Wrong level: %d\n", x);
        exit();
      }
      count[x]++;
    }
    for (i = 0; i < MAX_LEVEL; i++)
      printf(1, "L%d: %d\n", i, count[i]);
    }

    
  }
  exit_children();
  printf(1, "[Test 2] finished\n");
  printf(1, "done\n");
  exit();
}
