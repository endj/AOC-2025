import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


class MainThreaded {
   static int[][] DIRS = new int[][]{
           {0, 1},    // Right
           {1, 0},    // Down
           {0, -1},   // Left
           {-1, 0},   // Up
           {1, 1},    // Down-Right
           {1, -1},   // Down-Left
           {-1, -1},  // Up-Left
           {-1, 1}    // Up-Right
   };
   static byte[] XMAS = new byte[]{'X', 'M', 'A', 'S'};


   private static int pt1(byte[][] arr, int y, int x, int dy, int dx, int pos) {
       if (outOfBounds(arr, y, x) || arr[y][x] != XMAS[pos]) return 0;
       if (pos == 3) return 1;
       return pt1(arr, y + dy, x + dx, dy, dx, pos + 1);
   }


   private static boolean outOfBounds(byte[][] arr, int y, int x) {
       return y >= arr.length || y < 0 || x >= arr[0].length || x < 0;
   }


   private static byte safeGet(byte[][] arr, int y, int x) {
       if (outOfBounds(arr, y, x)) return '0';
       return arr[y][x];
   }


   private static byte[][] loadInput() throws IOException {
       try (FileChannel fileChannel = FileChannel.open(Path.of("input"), StandardOpenOption.READ)) {
           long fileSize = fileChannel.size();
           MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
           int lineSize = 0;
           while (map.get() != '\n') {
               lineSize++;
           }
           map.position(0);
           int lines = (int) ((double) fileSize / (lineSize + 1));
           byte[][] arr = new byte[lines][lineSize];
           for (int i = 0; i < lines; i++) {
               map.get(arr[i]);
               map.get();
           }
           return arr;
       }
   }


   public static void main(String[] args) throws Exception {
       byte[][] arr = loadInput();
       int coreCount = Runtime.getRuntime().availableProcessors();
       Thread[] threads = new Thread[coreCount];
       int[] resultsPt1 = new int[coreCount];
       int[] resultsPt2 = new int[coreCount];


       int chunkSize = (int) Math.ceil((double) arr.length / coreCount);


       for (int i = 0; i < coreCount; i++) {
           int start = i * chunkSize;
           int end = Math.min(start + chunkSize, arr.length);


           int threadIndex = i;
           threads[i] = new Thread(() -> {
               int pt1 = 0, pt2 = 0;
               for (int y = start; y < end; y++) {
                   for (int x = 0; x < arr[0].length; x++) {
                       if (arr[y][x] == 'X') {
                           for (int[] dir : DIRS) {
                               int dy = dir[0];
                               int dx = dir[1];
                               pt1 += pt1(arr, y, x, dy, dx, 0);
                           }
                       }
                       if (arr[y][x] == 'A') {
                           byte topLeft = safeGet(arr, y - 1, x - 1);
                           if (topLeft != 'S' && topLeft != 'M') continue;
                           byte botRight = safeGet(arr, y + 1, x + 1);
                           if (botRight != 'S' && botRight != 'M'
                                   || topLeft == 'M' && botRight != 'S'
                                   || topLeft == 'S' && botRight != 'M') continue;


                           byte botLeft = safeGet(arr, y + 1, x - 1);
                           if (botLeft != 'S' && botLeft != 'M') continue;
                           byte topRight = safeGet(arr, y - 1, x + 1);
                           if (topRight != 'S' && topRight != 'M'
                                   || botLeft == 'M' && topRight != 'S'
                                   || botLeft == 'S' && topRight != 'M') continue;
                           pt2++;
                       }
                   }
               }
               resultsPt1[threadIndex] = pt1;
               resultsPt2[threadIndex] = pt2;
           });
           threads[i].start();
       }


       for (Thread thread : threads) {
           thread.join();
       }


       int pt1 = 0, pt2 = 0;
       for (int i = 0; i < coreCount; i++) {
           pt1 += resultsPt1[i];
           pt2 += resultsPt2[i];
       }


       System.out.println("Part one: " + pt1 + "\nPart Two: " + pt2);
   }


}
