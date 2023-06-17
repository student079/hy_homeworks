#include "types.h"
#include "stat.h"
#include "user.h"
#include "fcntl.h"

#define NUM_BYTES 16 * 1024 * 1024 // 16 MB

char buf[NUM_BYTES], buf2[NUM_BYTES];
char filename[16] = "test_file0";

void failed(const char *msg)
{
  printf(1, msg);
  printf(1, "Test failed!!\n");
  exit();
}

void test1()
{
  int fd;

  printf(1, "Test 1: Write %d bytes\n", NUM_BYTES);
  fd = open(filename, O_CREATE | O_WRONLY);
  if (fd < 0)
    failed("File open error\n");
  if (write(fd, buf, NUM_BYTES) < 0)
    failed("File write error\n");
  if (close(fd) < 0)
    failed("File close error\n");
  printf(1, "Test 1 passed\n\n");
} 

void test2()
{
  int i, fd;
  for (i = 0; i < NUM_BYTES; i++)
    buf2[i] = 0;
  
  printf(1, "Test 2: Read %d bytes\n", NUM_BYTES);
  fd = open(filename, O_RDONLY);
  if (fd < 0)
    failed("File open error\n");
  if (read(fd, buf2, NUM_BYTES) < 0)
    failed("File read error\n");
  for (i = 0; i < NUM_BYTES; i++) {
    if (buf2[i] != (i % 26) + 'a') {
      printf(1, "%dth character, expected %c, found %c\n", i, (i % 26) + 'a', buf2[i]);
      failed("");
    }
  }
  if (close(fd) < 0)
    failed("File close error\n");
  if (unlink(filename) < 0)
    failed("File unlink error\n");
  if ((fd = open(filename, O_RDONLY)) >= 0)
      failed("File not erased\n");
  printf(1, "Test 2 passed\n\n");
}

int main(int argc, char *argv[])
{
  int i;
  for (i = 0; i < NUM_BYTES; i++)
    buf[i] = (i % 26) + 'a'; // alpahbet

  test1();
  test2();
  
  exit();
}