package org.headsupdev.agile.api.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation that indicates a field should be published through our REST API.
 * <p/>
 * Created: 10/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Publish {
}
