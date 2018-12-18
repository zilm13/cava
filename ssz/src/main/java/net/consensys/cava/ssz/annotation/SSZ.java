package net.consensys.cava.ssz.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Object field annotation</p>
 *
 * <p>Clarifies SSZ encoding/decoding handling</p>
 */
@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface SSZ {
  /**
   * <p>Specifies type and size (for fixed sizes)</p>
   * <p>Examples: "bytes", "hash32"</p>
   */
  String type() default "";

  /**
   * <p>If true, non-standard field is not handled like container,
   * instead it's type is passed through and reconstructed</p>
   *
   * <p>So if you need to have, for example, some field to be stored
   * as "hash32" in SSZ but you don't want to use byte[] for it
   * in Java representation, this parameter marks that it's such kind of field</p>
   */
  boolean skipContainer() default false;
}
