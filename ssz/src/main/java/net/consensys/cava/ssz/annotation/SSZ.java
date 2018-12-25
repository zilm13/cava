package net.consensys.cava.ssz.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Model field annotation</p>
 *
 * <p>Clarifies SSZ encoding/decoding details</p>
 */
@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface SSZ {
  /**
   * <p> Specifies type and size (for fixed sizes).
   * If custom type is specified, it overrides default type
   * mapped from Java class.</p>
   * <p>Type should be one of: "int", "long", "bigint", "bytes",
   * "hash", "boolean", "address", "string", "container".</p>
   * <p>Size could be omitted if it's not fixed. Otherwise for
   * non-byte types size should be multiplier of 8
   * as size is in bits. For byte types ("bytes", "hash",
   * "address", "string") size is provided in bytes.</p>
   *
   * <p>Types and default mapping:
   * <ul>
   * <li>"int" - unsigned integer, with 32 bit maximum.
   * int.class is mapped to "int32", short.class is mapped to "int16"</li>
   * <li>"long" - unsigned long integer, with 64 bit maximum.
   * long.class is mapped to "long64"</li>
   * <li>"bigint" - unsigned big integer.
   * BigInteger.class is mapped to "bigint"</li>
   * <li>"bytes" - bytes data.
   * byte[].class is mapped to "bytes"</li>
   * <li>"hash" - same as bytes, but purposed to use to store hash.
   * no types are mapped to "hash" by default</li>
   * <li>"address" - bytes with size of 20, standard address size.
   * no types are mapped to "address" by default</li>
   * <li>"boolean" - bool type.
   * boolean.class is mapped to "boolean"</li>
   * <li>"string" - string, text type.
   * String.class is mapped to "string"</li>
   * <li>"container" - type designed to store another model inside.
   * Any class which has no default mapping will be handled
   * as Container and should be SSZ-serializable.</li>
   * </ul>
   * </p>  TODO: Null values handling!!!11
   *
   * <p>Examples: "bytes", "hash32"</p>
   */
  String type() default "";

  /**
   * <p>If true, non-standard field is not handled like container,
   * instead its type is passed through and reconstructed</p>
   *
   * <p>So if you need to have, for example, some field to be stored
   * as "hash32" in SSZ but you don't want to use byte[] for it
   * in Java representation, you need some class to handle it,
   * this parameter when set to <b>true</b> marks that it's such kind
   * of field</p>
   */
  boolean skipContainer() default false;
}
