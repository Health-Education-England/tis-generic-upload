package com.transformuk.hee.tis.genericupload.service.util;

import java.lang.reflect.Field;
import java.util.Optional;

public class ReflectionUtil {
	public static void copyIfNotNullOrEmpty(Object from, Object to, String... fieldNames) {
		for(String fieldName : fieldNames) {
			copyIfNotNullOrEmpty(from, to, fieldName);
		}
	}

	public static void copyIfNotNullOrEmpty(Object from, Object to, String fieldName) {
		try {
			Field field = from.getClass().getDeclaredField(fieldName);
			boolean originalAccessibleState = field.isAccessible();
			field.setAccessible(true);

			Object value = field.get(from);
			Optional<Object> objectOptional = Optional.ofNullable(value);
			if(objectOptional.isPresent()) {
				if(objectOptional.get().getClass() != String.class || (objectOptional.get().getClass() == String.class && !objectOptional.get().equals(""))) {
					field.set(to, value);
				}
			}

			field.setAccessible(originalAccessibleState);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
