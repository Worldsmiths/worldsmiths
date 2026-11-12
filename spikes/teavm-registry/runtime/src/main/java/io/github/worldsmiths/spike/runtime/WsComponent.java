package io.github.worldsmiths.spike.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a component class for the build-time registry. The class must be public, concrete, have a public no-arg
 * constructor, and expose its serialized state as non-private, non-final fields of type boolean, int, long, float,
 * double or String. Transient and static fields are skipped.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface WsComponent {
    /** Type tag written to JSON. Defaults to the simple class name. */
    String value() default "";
}
