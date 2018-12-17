package net.consensys.cava.ssz.annotation;

import java.lang.annotation.*;

/**
 * Skips field in SSZ representation
 */
@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface SSZTransient {
}
