import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class Day9 {
    private static final byte NINE_ASCII = (byte) '9';
    private static final int[] DIGIT_LUT = new int[NINE_ASCII + 1];

    static {
        for (int i = (byte) '0'; i < NINE_ASCII + 1; i++) {
            DIGIT_LUT[i] = Character.digit(i, 10);
        }
    }


    public static void main(String[] args) throws Exception {
        solve();
    }


    record Free(int index, int size) {
    }


    record FileLocation(int id, int startIndex, int endIndex) {
    }


    record Next(int id, int size, int index) {
    }


    private static void solve() throws Exception {
        byte[] diskMap = Files.readAllBytes(Path.of("input"));
        List<String> expandedList = new ArrayList<>(diskMap.length);
        TreeMap<Integer, Free> frees = new TreeMap<>();
        Map<Integer, FileLocation> locations = new TreeMap<>(Comparator.reverseOrder());

        int id = 0;
        int expandedIdx = 0;
        for (int i = 0; i < diskMap.length - 1; i++) {
            int blocks = DIGIT_LUT[diskMap[i]];
            if (i % 2 == 0) {
                String sId = String.valueOf(id);
                for (int j = 0; j < blocks; j++) {
                    expandedList.add(sId);
                }
                locations.put(expandedIdx, new FileLocation(id, expandedIdx, expandedIdx + blocks));
                expandedIdx += blocks;
                id++;
            } else {
                if (blocks > 0) {
                    frees.put(expandedIdx, new Free(expandedIdx, blocks));
                }
                for (int j = 0; j < blocks; j++) {
                    expandedList.add(".");
                }
                expandedIdx += blocks;
            }
        }
        String[] expanded = new String[expandedList.size()];
        expandedList.toArray(expanded);

        Collection<FileLocation> values = locations.values();
        List<Next> blocks = new ArrayList<>(values.size());
        for (FileLocation value : values) {
            blocks.add(new Next(
                    value.id, value.endIndex - value.startIndex, value.startIndex - 1
            ));
        }

        for (Next block : blocks) {
            Iterator<Map.Entry<Integer, Free>> iterator = frees.headMap(block.index).entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Free> entry = iterator.next();
                Free free = entry.getValue();
                if (free.size >= block.size && block.index >= free.index) {
                    iterator.remove();
                    FileLocation current = locations.remove(block.index + 1);
                    locations.put(free.index, new FileLocation(current.id, free.index, free.index + block.size));

                    if (block.size < free.size) {
                        frees.put(free.index + block.size, new Free(free.index + block.size, free.size - block.size));
                    }
                    break;
                }
            }
        }

        int takeFrom = expanded.length - 1;
        for (int i = 0; i < expanded.length; i++) {
            if (expanded[i].equals(".")) {
                while (takeFrom > i && expanded[takeFrom].equals(".")) {
                    takeFrom--;
                }
                expanded[i] = expanded[takeFrom];
                expanded[takeFrom] = ".";
            }
        }
        System.out.println("Part one: " + checkSum(expanded) + "\nPart two: " + checkSumPt2(locations));
    }


    private static long checkSum(String[] expanded) {
        long sum = 0;
        for (int i = 0; i < expanded.length; i++) {
            String x = expanded[i];
            if (x.equals(".")) continue;
            sum += (long) i * Long.parseUnsignedLong(x);
        }
        return sum;
    }

    private static long checkSumPt2(Map<Integer, FileLocation> locations) {
        long sum = 0;
        for (FileLocation fileLocation : locations.values()) {
            for (int i = fileLocation.startIndex; i < fileLocation.endIndex; i++) {
                sum += fileLocation.id * (long) i;
            }
        }
        return sum;
    }

}
