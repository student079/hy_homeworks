#include "types.h"
#include "stat.h"
#include "user.h"

// Adding symbolic link
int
main(int argc, char *argv[])
{
  if(argc != 4){
    printf(2, "Usage: ln [-o] old new\n");
    exit();
  }
  
  // Hard link
  if (strcmp(argv[1], "-h") == 0) {
    if(link(argv[2], argv[3]) < 0)
      printf(2, "Hard link %s %s: failed\n", argv[2], argv[3]);
  }
  // Symbolic link
  else if(strcmp(argv[1], "-s") == 0) {
    if(symlink(argv[2], argv[3]) < 0)
      printf(2, "Symbolic link %s %s: failed\n", argv[2], argv[3]);
  }

  exit();
}
