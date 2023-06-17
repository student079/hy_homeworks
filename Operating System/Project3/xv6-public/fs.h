// On-disk file system format.
// Both the kernel and user programs use this header file.


#define ROOTINO 1  // root i-number
#define BSIZE 512  // block size

// Disk layout:
// [ boot block | super block | log | inode blocks |
//                                          free bit map | data blocks]
//
// mkfs computes the super block and builds an initial file system. The
// super block describes the disk layout:
struct superblock {
  uint size;         // Size of file system image (blocks)
  uint nblocks;      // Number of data blocks
  uint ninodes;      // Number of inodes.
  uint nlog;         // Number of log blocks
  uint logstart;     // Block number of first log block
  uint inodestart;   // Block number of first inode block
  uint bmapstart;    // Block number of first free map block
};

//  Triple indirect implement
#define NDIRECT 10  // To simplify implementation reduece NDIRECT
#define NINDIRECT (BSIZE / sizeof(uint))  // 128
#define DOUBLE_INDIRECT (NINDIRECT * NINDIRECT)   // 128 * 128
#define TRIPLE_INDIRECT (DOUBLE_INDIRECT * NINDIRECT)   // 128 * 128 * 128
#define MAXFILE (NDIRECT + NINDIRECT + DOUBLE_INDIRECT + TRIPLE_INDIRECT)
      // 10 + 128 + 128 * 128 + 128 * 128 * 128
      // 2113674 * 512 = 1GB


// On-disk inode structure
struct dinode {
  short type;           // File type
  short major;          // Major device number (T_DEV only)
  short minor;          // Minor device number (T_DEV only)
  short nlink;          // Number of links to inode in file system
  uint size;            // Size of file (bytes)
  char target[64];      // Symbolic target

  uint addrs[NDIRECT+3];   // Data block addresses and triple indirect
};

// Inodes per block.
#define IPB           (BSIZE / sizeof(struct dinode))

// Block containing inode i
#define IBLOCK(i, sb)     ((i) / IPB + sb.inodestart)

// Bitmap bits per block
#define BPB           (BSIZE*8)

// Block of free map containing bit for block b
#define BBLOCK(b, sb) (b/BPB + sb.bmapstart)

// Directory is a file containing a sequence of dirent structures.
#define DIRSIZ 14

struct dirent {
  ushort inum;
  char name[DIRSIZ];
};

