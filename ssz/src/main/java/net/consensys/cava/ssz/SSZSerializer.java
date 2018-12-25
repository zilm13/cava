package net.consensys.cava.ssz;

import javafx.util.Pair;
import net.consensys.cava.bytes.Bytes;
import net.consensys.cava.ssz.annotation.SSZSerializable;
import net.consensys.cava.ssz.annotation.SSZTransient;
import javax.annotation.Nullable;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

public class SSZSerializer {

  static int DEFAULT_SHORT_SIZE = 16;
  static int DEFAULT_INT_SIZE = 32;
  static int DEFAULT_LONG_SIZE = 64;
  static int DEFAULT_BIGINT_SIZE = 512;
  static int LENGTH_PREFIX_BYTE_SIZE = DEFAULT_INT_SIZE / Byte.SIZE;

  private static final String TYPE_REGEX = "^(\\D+)((\\d+)?)$";
  private static final Set<SSZType.Type> NUMERIC_TYPES = new HashSet<SSZType.Type>(){{
    add(SSZType.Type.INT);
    add(SSZType.Type.LONG);
    add(SSZType.Type.BIGINT);
  }};
  private static Map<Class, SSZType> classToSSZType = new HashMap<>();

  static {
    classToSSZType.put(int.class, SSZType.of(SSZType.Type.INT, DEFAULT_INT_SIZE));
    classToSSZType.put(Integer.class, SSZType.of(SSZType.Type.INT, DEFAULT_INT_SIZE));
    classToSSZType.put(short.class, SSZType.of(SSZType.Type.INT, DEFAULT_SHORT_SIZE));
    classToSSZType.put(Short.class, SSZType.of(SSZType.Type.INT, DEFAULT_SHORT_SIZE));
    classToSSZType.put(long.class, SSZType.of(SSZType.Type.LONG, DEFAULT_LONG_SIZE));
    classToSSZType.put(Long.class, SSZType.of(SSZType.Type.LONG, DEFAULT_LONG_SIZE));
    classToSSZType.put(BigInteger.class, SSZType.of(SSZType.Type.BIGINT, DEFAULT_BIGINT_SIZE));
    classToSSZType.put(List.class, SSZType.of(SSZType.Type.LIST));
    classToSSZType.put(byte[].class, SSZType.of(SSZType.Type.BYTES));
    classToSSZType.put(boolean.class, SSZType.of(SSZType.Type.BOOLEAN));
    classToSSZType.put(Boolean.class, SSZType.of(SSZType.Type.BOOLEAN));
    classToSSZType.put(String.class, SSZType.of(SSZType.Type.STRING));
  }

  static class SSZType {
    Integer size = null;
    Type type = null;

    public static SSZType of(Type type) {
      SSZType sszType = new SSZType();
      sszType.type = type;

      return sszType;
    }

    public static SSZType of(Type type, Integer size) {
      SSZType sszType = SSZType.of(type);
      sszType.size = size;

      return sszType;
    }

    enum Type {
      INT("int"),
      LONG("long"),
      BIGINT("bigint"),
      BYTES("bytes"),
      HASH("hash"),
      BOOLEAN("boolean"),
      ADDRESS("address"),
      STRING("string"),
      CONTAINER("container"),
      LIST("list");

      private String type;
      private static final Map<String, Type> ENUM_MAP;
      static {
        ENUM_MAP = Stream.of(Type.values()).collect(Collectors.toMap(e -> e.type, identity()));
      }

      Type(String type) {
        this.type = type;
      }

      static Type fromValue(String type) {
        return ENUM_MAP.get(type);
      }

      @Override
      public String toString() {
        return type;
      }
    }
  }

  static SSZType extractType(Class clazz, @Nullable String extra) {
    if (extra == null) {
      SSZType res = classToSSZType.get(clazz);
      if (res != null) {
        return res;
      } else {
        return SSZType.of(SSZType.Type.CONTAINER);  // Default for unknown classes
      }
    }

    SSZType res = new SSZType();
    Pattern pattern = Pattern.compile(TYPE_REGEX);
    Matcher matcher = pattern.matcher(extra);
    if (matcher.find()) {
      String type = matcher.group(1);
      String endNumber = matcher.group(3);
      res.type = SSZType.Type.fromValue(type);
      if (endNumber != null) {
        int bitLength = Integer.valueOf(endNumber);
        if (res.type != null && NUMERIC_TYPES.contains(res.type) && bitLength % Byte.SIZE != 0) {
          String error = String.format("Size of field in bits should match whole bytes, found %s",
              extra);
          throw new RuntimeException(error);
        }

        res.size = bitLength;
      }
    } else {
      String error = String.format("Type annotation \"%s\" for class %s is not correct",
          extra, clazz.getName());
      throw new RuntimeException(error);
    }

    return res;
  }

  public static byte[] encode(Object input) {
    checkSSZSerializableAnnotation(input.getClass());

    // Fill up map with all available method getters
    Map<String, Method> getters = new HashMap<>();
    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(input.getClass()).getPropertyDescriptors()) {
        getters.put(pd.getReadMethod().getName(), pd.getReadMethod());
      }
    } catch (IntrospectionException e) {
      String error = String.format("Couldn't enumerate all getters in class %s",
          input.getClass().getName());
      throw new RuntimeException(error, e);
    }

    // Encode object fields one by one
    SSZScheme scheme = buildScheme(input.getClass());
    ByteArrayOutputStream res = new ByteArrayOutputStream();
    for (SSZScheme.SSZField field : scheme.fields) {
      Object value;
      Method getter = getters.get(field.getter);
      try {
        if (getter != null) {   // We have getter
          value = getter.invoke(input);
        } else {                // Trying to access field directly
          value = input.getClass().getField(field.name).get(input);
        }
      } catch (Exception e) {
        String error = String.format("Failed to get value from field %s, your should "
            + "either have public field or public getter for it", field.name);
        throw new RuntimeException(error);
      }

      SSZEncoder.encodeField(value, field, res);
    }

    return res.toByteArray();
  }

  private static void checkSSZSerializableAnnotation(Class clazz) {
    if (!clazz.isAnnotationPresent(SSZSerializable.class)) {
      throw new RuntimeException("SSZ serializable class should be annotated "
          + "with SSZSerializable!");
    }
  }

  private static SSZScheme buildScheme(Class clazz) {
    SSZScheme scheme = new SSZScheme();
    SSZSerializable mainAnnotation = (SSZSerializable) clazz.getAnnotation(SSZSerializable.class);

    // Encode parameter means we don't need to serialize class
    // using any built-in logic, we should only call "encode"
    // method to get all object data and that's all!
    if(!mainAnnotation.encode().isEmpty()) {
      SSZScheme.SSZField encode = new SSZScheme.SSZField();
      encode.type = byte[].class;
      encode.sszType = SSZType.of(SSZType.Type.BYTES);
      encode.name = "encode";
      encode.getter = mainAnnotation.encode();
      scheme.fields.add(encode);
      return scheme;
    }

    // No encode parameter, build scheme field by field
    Map<String, Method> fieldGetters = new HashMap<>();
    try {
      for (PropertyDescriptor pd: Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
        fieldGetters.put(pd.getName(), pd.getReadMethod());
      }
    } catch (IntrospectionException e) {
      String error = String.format("Couldn't enumerate all getters in class %s", clazz.getName());
      throw new RuntimeException(error, e);
    }

    for (Field field : clazz.getDeclaredFields()) {

      // Skip SSZTransient
      boolean transientField = false;
      for (Annotation annotation : field.getAnnotations()) {
        if (annotation.annotationType().equals(SSZTransient.class)) {
          transientField = true;
          break;
        }
      }
      if (transientField) {
        continue;
      }

      // Check for SSZ annotation and read its parameters
      Class type = field.getType();
      net.consensys.cava.ssz.annotation.SSZ annotation = null;
      if (field.isAnnotationPresent(net.consensys.cava.ssz.annotation.SSZ.class)) {
        annotation = field.getAnnotation(net.consensys.cava.ssz.annotation.SSZ.class);
      }
      String typeAnnotation = null;
      if (annotation != null && !annotation.type().isEmpty()) {
        typeAnnotation = annotation.type();
      }

      // Construct SSZField
      SSZScheme.SSZField newField = new SSZScheme.SSZField();
      if (annotation != null && annotation.skipContainer()) {
        newField.skipContainer = true;
      }
      newField.type = type;
      String name = field.getName();
      newField.name = name;
      SSZType sszType = extractType(type, typeAnnotation);
      newField.sszType = sszType;
      if (sszType.type.equals(SSZType.Type.CONTAINER) && newField.skipContainer == null) {
        newField.skipContainer = false;
      }

      newField.getter = fieldGetters.containsKey(name) ? fieldGetters.get(name).getName() : null;
      scheme.fields.add(newField);
    }

    return scheme;
  }

  public static Object decode(byte[] data, Class clazz) {
    checkSSZSerializableAnnotation(clazz);

    SSZScheme scheme = buildScheme(clazz);
    List<SSZScheme.SSZField> fields = scheme.fields;
    int size = fields.size();
    Class[] params = new Class[size];
    Object[] values = new Object[size];
    BytesSSZReader reader = new BytesSSZReader(Bytes.of(data));

    // For each field resolve its type and decode its value
    for (int i = 0; i < size; i++) {
      SSZScheme.SSZField field = fields.get(i);
      params[i] = field.type;
      values[i] = SSZDecoder.decodeField(field, reader);
    }

    // Construct clazz instance
    Object result;
    Pair<Boolean, Object> constructorAttempt = createInstanceWithConstructor(clazz, params, values);
    if (!constructorAttempt.getKey()) {
      Pair<Boolean, Object> setterAttempt = createInstanceWithSetters(clazz, fields, values);
      if (!setterAttempt.getKey()) {
        String fieldTypes = Arrays.stream(values)
            .map(v -> v.getClass().toString())
            .collect(Collectors.joining(","));
        String error = String.format("Unable to find appropriate class %s "
            + "construction method with params [%s]."
            + "You should either have constructor with all non-transient fields "
            + "or setters/public fields.", clazz.getName(), fieldTypes);
        throw new RuntimeException(error);
      } else {
        result = setterAttempt.getValue();
      }
    } else {
      result = constructorAttempt.getValue();
    }

    return result;
  }

  private static Pair<Boolean, Object> createInstanceWithConstructor(Class clazz, Class[] params,
                                                                     Object[] values) {
    // Find constructor for params
    Constructor constructor;
    try {
      constructor = clazz.getConstructor(params);
    } catch (NoSuchMethodException e) {
      return new Pair<>(false, null);
    }

    // Invoke constructor using values as params
    Object result;
    try {
      result = constructor.newInstance(values);
    } catch (Exception e) {
      return new Pair<>(false, null);
    }

    return new Pair<>(true, result);
  }

  private static Pair<Boolean, Object> createInstanceWithSetters(
      Class clazz, List<SSZScheme.SSZField> fields, Object[] values) {
    // Find constructor with no params
    Constructor constructor;
    try {
      constructor = clazz.getConstructor();
    } catch (NoSuchMethodException e) {
      return new Pair<>(false, null);
    }

    // Create empty instance
    Object result;
    try {
      result = constructor.newInstance();
    } catch (Exception e) {
      return new Pair<>(false, null);
    }

    Map<String, Method> fieldSetters = new HashMap<>();
    try {
      for (PropertyDescriptor pd: Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
        fieldSetters.put(pd.getName(), pd.getWriteMethod());
      }
    } catch (IntrospectionException e) {
      String error = String.format("Couldn't enumerate all setters in class %s", clazz.getName());
      throw new RuntimeException(error, e);
    }

    // Fill up field by field
    for (int i = 0; i < fields.size(); ++i) {
      SSZScheme.SSZField currentField = fields.get(i);
      try {   // Try to set by field assignment
        clazz.getField(currentField.name).set(result, values[i]);
      } catch (Exception e) {
        try {    // Try to set using setter
          fieldSetters.get(currentField.name).invoke(result, values[i]);
        } catch (Exception ex) {    // Cannot set the field
          return new Pair<>(false, null);
        }
      }
    }

    return new Pair<>(true, result);
  }
}
