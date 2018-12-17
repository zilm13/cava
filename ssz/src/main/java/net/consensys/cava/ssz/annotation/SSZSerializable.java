package net.consensys.cava.ssz.annotation;

import java.lang.annotation.*;

/**
 * Identifies class that could be SSZ serializable
 */
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface SSZSerializable {
    /**
     * If set, uses following method to get encoded class value
     * @return method for encoding
     */
    String encode() default "";
}
