import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class Main {
    private static final byte OBSTRUCTION = '#';
    private static final byte GUARD = '^';
    static byte[][] LUT_RIGHT = null;
    static byte[][] LUT_LEFT = null;
    static byte[][] LUT_UP = null;
    static byte[][] LUT_DOWN = null;
    static byte[][][] LUTS;


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


    private static final int[][] DIRECTIONS = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
    private static int DIR_INDEX = 0;


    public static void main(String[] args) throws Exception {
        solve(args);
    }


    private static void solve(String[] args) throws IOException {
        String file = "input";
        if (args.length == 1) file = args[0];
        byte[][] grid = loadInput(file);
        int gx = -1, gy = -1;

        LUT_RIGHT = new byte[grid.length][grid[0].length];
        LUT_LEFT = new byte[grid.length][grid[0].length];
        LUT_UP = new byte[grid.length][grid[0].length];
        LUT_DOWN = new byte[grid.length][grid[0].length];
        LUTS = new byte[][][]{LUT_UP, LUT_RIGHT, LUT_DOWN, LUT_LEFT};

        Set<Integer> visited = new HashSet<>();

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[0].length; x++) {
                if (grid[y][x] == GUARD) {
                    gy = y;
                    gx = x;
                }
                if (grid[y][x] == OBSTRUCTION) {
                    if (x != 0) {
                        for (int i = x - 1, j = 0; i >= 0; i--, j++) {
                            if (grid[y][i] == OBSTRUCTION) break;
                            LUT_RIGHT[y][i] = (byte) j;
                        }
                    }


                    for (int i = x + 1, j = 0; i < grid[0].length; i++, j++) {
                        if (grid[y][i] == OBSTRUCTION) break;
                        LUT_LEFT[y][i] = (byte) j;
                    }


                    for (int i = y - 1, j = 0; i >= 0; i--, j++) {
                        if (grid[i][x] == OBSTRUCTION) break;
                        LUT_DOWN[i][x] = (byte) j;
                    }


                    for (int i = y + 1, j = 0; i < grid.length; i++, j++) {
                        if (grid[i][x] == OBSTRUCTION) break;
                        LUT_UP[i][x] = (byte) (j);
                    }
                }
            }
        }


        final int startingY = gy;
        final int startingX = gx;


        int cord = (gy << 16) | (gx & 0xFFFF);
        visited.add(cord);
        while (true) {
            int skip = LUTS[DIR_INDEX][gy][gx];
            int skipY = ((~DIR_INDEX) & 1) * skip;
            int skipX = (DIR_INDEX & 1) * skip;


            int dy = DIRECTIONS[DIR_INDEX][0];
            int dx = DIRECTIONS[DIR_INDEX][1];


            int ny = gy + (skipY == 0 ? dy : (dy * skipY));
            int nx = gx + (skipX == 0 ? dx : (dx * skipX));


            if (ny < 0 || ny >= grid.length || nx < 0 || nx >= grid[0].length) { // OOB
                break;
            }


            if (grid[ny][nx] == OBSTRUCTION) { // In front of obstruction
                DIR_INDEX = (DIR_INDEX + 1) & 3;
            } else {
                if (gx == nx) {
                    for (int i = Math.min(gy, ny); i < Math.max(gy, ny); i++) {
                        visited.add((i << 16) | (gx & 0xFFFF));
                    }
                } else {
                    for (int i = Math.min(gx, nx); i < Math.max(gx, nx); i++) {
                        visited.add((gy << 16) | (i & 0xFFFF));
                    }
                }
                gx = nx;
                gy = ny;
                visited.add((gy << 16) | (gx & 0xFFFF));
            }
        }


        int loops = 0;


        for (int obstruction : visited) {
            int y = (obstruction >> 16) & 0xFFFF;
            int x = obstruction & 0xFFFF;
            if (loops(startingY, startingX, grid, y, x)) {
                loops++;
            }
        }


        System.out.println("Part one: " + visited.size() + "\nPart Two: " + loops);
    }


    private static boolean loops(final int y, final int x, byte[][] grid, int obstructionY, int obstructionX) {
        int gy = y, gx = x;
        DIR_INDEX = 0;


        IntHashSet pointDirections = new IntHashSet();


        int dy = DIRECTIONS[DIR_INDEX][0];
        int dx = DIRECTIONS[DIR_INDEX][1];
        while (true) {
            int skip = LUTS[DIR_INDEX][gy][gx];
            int skipY = ((~DIR_INDEX) & 1) * skip;
            int skipX = (DIR_INDEX & 1) * skip;


            int ny, nx;
            if (gx == obstructionX || gy == obstructionY) {
                ny = gy + dy;
                nx = gx + dx;
            } else {
                ny = gy + dy * (skipY + (1 & (~(skipY | -skipY) >> 31)));
                //ny = gy + (skipY == 0 ? dy : (dy * skipY));
                nx = gx + dx * (skipX + (1 & (~(skipX | -skipX) >> 31)));
                //nx = gx + (skipX == 0 ? dx : (dx * skipX));
            }


            if (ny < 0 || ny >= grid.length || nx < 0 || nx >= grid[0].length) { // OOB
                return false;
            }


            if (grid[ny][nx] == OBSTRUCTION || (ny == obstructionY && nx == obstructionX)) {
                int packedInt = ((ny & 0xFF) << 24) | ((nx & 0xFF) << 16) | ((dy & 0xFF) << 8) | (dx & 0xFF);
                if (pointDirections.contains(packedInt)) {// Loop?
                    return true;
                }
                DIR_INDEX = (DIR_INDEX + 1) & 3;  // 3 is 0b11
                dy = DIRECTIONS[DIR_INDEX][0];
                dx = DIRECTIONS[DIR_INDEX][1];
                pointDirections.add(packedInt);
            } else {
                gy = ny;
                gx = nx;
            }
        }
    }


    public static class IntHashSet {
        private int[] table;
        private int capacity;
        private int size;
        private static final int DEFAULT_CAPACITY = 2 << 10;
        private static final float LOAD_FACTOR = 0.75f;
        private static final int EMPTY = Integer.MIN_VALUE;


        public IntHashSet() {
            this.capacity = DEFAULT_CAPACITY;
            this.table = new int[capacity];
            this.size = 0;
            Arrays.fill(table, EMPTY);
        }


        public boolean add(int key) {
            if (size >= capacity * LOAD_FACTOR) {
                resize();
            }
            int index = indexFor(key);
            while (table[index] != EMPTY) {
                if (table[index] == key) {
                    return false;
                }
                index = (index + 1) & (capacity - 1);
            }


            table[index] = key;
            size++;
            return true;
        }


        public boolean contains(int key) {
            int index = indexFor(key);

            while (table[index] != EMPTY) {
                if (table[index] == key) {
                    return true;
                }
                index = (index + 1) & (capacity - 1);
            }


            return false;
        }


        private void resize() {
            int newCapacity = capacity << 1;
            int[] oldTable = table;

            table = new int[newCapacity];
            capacity = newCapacity;
            size = 0;
            Arrays.fill(table, EMPTY);

            for (int key : oldTable) {
                if (key != EMPTY) {
                    add(key);
                }
            }
        }


        private int indexFor(int key) {
            int hash = key ^ (key >>> 16);
            return hash & (capacity - 1);
        }
    }
}



