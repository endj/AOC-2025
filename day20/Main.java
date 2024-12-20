import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
   public static final int CELL_SIZE = 16;
   public static final int MAX_DISTANCE = 20;


   record Point(int y, int x, int distance) {}
   public static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};


   public static void main(String[] args) throws Exception {
       solve();
   }


   private static void solve() throws Exception {
       final byte[][] grid = loadInput("input");


       int y = 0, x = 0;
       outer:
       for (int i = 0; i < grid.length; i++) {
           for (int j = 0; j < grid[i].length; j++) {
               if (grid[i][j] == 'S') {
                   y = i;
                   x = j;
                   break outer;
               }
           }
       }


       Point[] points = new Point[10_000];
       int pointSize = 0;


       {
           points[pointSize++] = new Point(y, x, 0);
           int distance = 1;
           boolean loop = true;
           while (loop) {
               for (var dir : DIRECTIONS) {
                   int ny = y + dir[0], nx = x + dir[1];
                   if (ny >= 0 && ny < grid.length && nx >= 0 && nx < grid[0].length) {
                       if (grid[ny][nx] == '.') {
                           points[pointSize++] = new Point(ny, nx, distance++);
                           y = ny;
                           x = nx;
                           grid[ny][nx] = (byte) 0;
                       } else if (grid[ny][nx] == 'E') {
                           points[pointSize++] = new Point(ny, nx, distance++);
                           loop = false;
                           break;
                       }
                   }
               }
           }
       }


       final Map<Integer, Map<Integer, List<Point>>> partitionedLookup = partitionPoints(points, pointSize);


       final int[] distanceMapArr = new int[10000];
       final int fullDistance = pointSize;
       int pt1 = 0;


       for (int pi = 0; pi < pointSize; pi++) {
           Point point = points[pi];


           int minX = (point.x() - MAX_DISTANCE) / CELL_SIZE;
           int maxX = (point.x() + MAX_DISTANCE) / CELL_SIZE;
           int minY = (point.y() - MAX_DISTANCE) / CELL_SIZE;
           int maxY = (point.y() + MAX_DISTANCE) / CELL_SIZE;




           for (int yy = minY; yy <= maxY; yy++) {
               for (int xx = minX; xx <= maxX; xx++) {


                   Map<Integer, List<Point>> region = partitionedLookup.get(yy);
                   if(region == null) continue;
                   List<Point> candidates = region.get(xx);
                   if(candidates == null) continue;


                   final int numOfCandidates = candidates.size();
                   for (int i = 0; i < numOfCandidates; i++) {
                       Point p = candidates.get(i);


                       int manhattanDistance = Math.abs(p.x - point.x) + Math.abs(p.y - point.y);
                       if (manhattanDistance <= MAX_DISTANCE) {
                           var pointDistance = fullDistance - point.distance;
                           var fromOtherDistance = fullDistance - p.distance;


                           var withCheat = fromOtherDistance + manhattanDistance;
                           if (withCheat < pointDistance) {
                               int saved = pointDistance - withCheat;
                               if (saved >= 100) {
                                   if (manhattanDistance == 2) {
                                       pt1++;
                                   }
                                   distanceMapArr[saved]++;
                               }
                           }
                       }
                   }
               }
           }
       }
       int pt2 = 0;
       for (int picoSeconds = 100; picoSeconds < distanceMapArr.length; picoSeconds++) {
           pt2 += distanceMapArr[picoSeconds];
       }
       System.out.println("Part one:" + pt1 + ", Part two:" + pt2);
       //if (pt1 != 1311) throw new IllegalArgumentException("Got " + pt1);
       //if (pt2 != 961364) throw new IllegalArgumentException("Got " + pt2);
   }


   private static Map<Integer, Map<Integer, List<Point>>> partitionPoints(Point[] points, int numPoints) {
       Map<Integer, Map<Integer, List<Point>>> partitionedLookup = new HashMap<>();
       for (int i = 0; i < numPoints; i++) {
           Point p = points[i];
           int gridX = p.x() / CELL_SIZE;
           int gridY = p.y() / CELL_SIZE;
           partitionedLookup.computeIfAbsent(gridY, k -> new HashMap<>())
                   .computeIfAbsent(gridX, k -> new ArrayList<>()).add(p);
       }
       return partitionedLookup;
   }




   private static byte[][] loadInput(String file) throws IOException {
       try (FileChannel fileChannel = FileChannel.open(
               Path.of(file), StandardOpenOption.READ)) {
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


}


