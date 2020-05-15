package com.transformuk.hee.tis.genericupload.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {

  /**
   * Returns the name of the column.
   *
   * @return the column name.
   */
  String name();

  /**
   * Returns whether the column is a required field, default is false.
   *
   * @return whether the column is required.
   */
  boolean required() default false;
}
