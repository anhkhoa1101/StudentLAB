package Utilities;

import java.util.List;
import java.util.Map;

public class PrintUtils {

    // Print Map
    public static <K, V> void printMap(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            System.out.println("No data.");
            return;
        }

        for (Map.Entry<K, V> entry : map.entrySet()) {
            System.out.println(
                    entry.getKey() + " : " + entry.getValue()
            );
        }
    }


}