import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {


    public static void main(String[] args) throws Exception {
        solve(args);
    }

    private static void solve(String[] args) throws IOException {
        String file = "input";
        if (args.length == 1) file = args[0];
        byte[][] grid = loadInput(file);

        Map<Byte, List<Integer>> locations = HashMap.newHashMap(64);
        byte[][] pt1 = new byte[grid.length][grid[0].length];
        byte[][] pt2 = new byte[grid.length][grid[0].length];

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[0].length; x++) {
                if (grid[y][x] != '.') {
                    locations.computeIfAbsent(grid[y][x], (__) -> new ArrayList<>()).add((y << 16) | x);
                }
            }
        }

        for (var entry : locations.entrySet()) {
            for (int a : entry.getValue()) {
                int ay = (a >> 16), ax = a & 0xFFFF;

                for (int b : entry.getValue()) {
                    int by = (b >> 16), bx = b & 0xFFFF;
                    if (ax == bx && ay == by) continue;

                    int dy = ay - by;
                    int dx = ax - bx;

                    int locAy = ay - (dy * 2);
                    int locAx = ax - (dx * 2);
                    if (locAy >= 0 && locAy < grid.length && locAx >= 0 && locAx < grid[0].length) {
                        pt1[locAy][locAx] = 1;
                    }

                    int locBy = by + (dy * 2);
                    int locBx = bx + (dx * 2);
                    if (locBy >= 0 && locBy < grid.length && locBx >= 0 && locBx < grid[0].length) {
                        pt1[locBy][locBx] = 1;
                    }

                    markLines(pt2, ay, ax, dy, dx, grid);
                    markLines(pt2, by, bx, dy, dx, grid);
                }
            }
        }


        int sum1 = 0;
        for (byte[] bytes : pt1) {
            for (byte aByte : bytes) {
                sum1 += aByte;
            }
        }
        int sum2 = 0;
        for (byte[] bytes : pt2) {
            for (byte aByte : bytes) {
                sum2 += aByte;
            }
        }

        System.out.println("Part one:" + sum1 + "\nPart two: " + sum2);
    }

    private static void markLines(byte[][] grid, int startY, int startX, int dy, int dx, byte[][] bounds) {
        int y = startY + dy, x = startX + dx;
        while (y >= 0 && y < bounds.length && x >= 0 && x < bounds[0].length) {
            grid[y][x] = 1;
            y += dy;
            x += dx;
        }
    }

    private static byte[][] loadInput(String file) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(Path.of(file), StandardOpenOption.READ)) {
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





