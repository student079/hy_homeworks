#include "types.h"
#include "user.h"
#include "fcntl.h"

void create_file(const char* path, const char* content) {
  int fd = open(path, O_WRONLY | O_CREATE);
  if (fd < 0) {
    printf(1, "Error creating file: %s\n", path);
    return;
  }
  write(fd, content, strlen(content));
  close(fd);
}

void read_file(const char* path) {
  int fd = open(path, O_RDONLY);
  if (fd < 0) {
    printf(1, "Error opening file: %s\n", path);
    return;
  }

  char buffer[100];
  int bytesRead = read(fd, buffer, sizeof(buffer)-1);
  if (bytesRead == 0) {
    printf(1, "Error reading file: %s\n", path);
  } else {
    buffer[bytesRead] = '\0';
    printf(1, "Content of %s: %s\n", path, buffer);
  }

  close(fd);
}

int main(void) {
  // Create a symbolic link
  char* target = "/file.txt";
  char* symlinkfile = "/symlinkfile.txt";
  // Create the target file
  char* content = "Hello, world!";
  create_file(target, content);

  if (symlink(target, symlinkfile) < 0) {
    printf(1, "Error creating symbolic link\n");
    exit();
  }


  // Read from the symbolic link
  read_file(symlinkfile);

  // Remove the original file
  if (unlink(target) < 0) {
    printf(1, "Error removing file: %s\n", target);
  }

  // Attempt to read from the symbolic link again
  // read_file(symlinkfile);

  exit();
}