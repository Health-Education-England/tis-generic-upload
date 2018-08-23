package com.transformuk.hee.tis.genericupload.service.util;

public class BooleanUtil {

  public static Boolean parseBooleanObject(String value){
    if("YES".equalsIgnoreCase(value)){
      return true;
    }
    return false;
  }
}
