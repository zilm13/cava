package net.consensys.cava.ssz;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Object SSZ scheme.</p>
 *
 * <p>Enumerates all object fields and their
 * properties in appropriate order.</p>
 */
public class SSZScheme {
  List<SSZField> fields = new ArrayList<>();

  static class SSZField {
    Class type;
    SSZSerializer.SSZType sszType;
    String name;
    String getter;
    /**
     * <ul>
     * <li>Container not needed (primitive type) : null</li>
     * <li>Container needed : false</li>
     * <li>Container needed but should be omitted: true</li>
     * </ul>
     */
    Boolean skipContainer = null;
  }
}
