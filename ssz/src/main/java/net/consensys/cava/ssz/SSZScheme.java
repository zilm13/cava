package net.consensys.cava.ssz;

import java.util.ArrayList;
import java.util.List;

/**
 * Object SSZ scheme.
 *
 * Enumerates all object fields and their
 * properties in appropriate order.
 */
public class SSZScheme {
    List<SSZField> fields = new ArrayList<>();

    static class SSZField {
        Class type;
        SSZSerializer.SSZType sszType;
        String name;
        String getter;
        // container not needed : null
        // container needed : false
        // container needed but should be omitted: true
        Boolean skipContainer = null;
    }
}
