package net.consensys.cava.ssz.annotation;

import java.lang.annotation.*;

/**
 * Object field annotation
 *
 * Clarifies SSZ encoding/decoding handling
 */
@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface SSZ {

    /**
     * Specifies type and size (for fixed sizes)
     * Examples: "bytes", "hash32"
     */
    String type() default "";

    /**
     * If true, non-standard field is not handled like container,
     * instead it's type is passed through and reconstructed
     *
     * So if you need to have, for example, some field to be stored
     * as "hash32" in SSZ but you don't want to use byte[] for it
     * in Java representation, this parameter marks that it's such kind of field
     */
    boolean skipContainer() default false;
}
