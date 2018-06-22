package com.transformuk.hee.tis.genericupload.service.util;

import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class ReflectionUtil {
	private static final Logger logger = getLogger(ReflectionUtil.class);

	ReflectionUtil() {
		super();
	}

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
			if(objectOptional.isPresent() && (objectOptional.get().getClass() != String.class || (objectOptional.get().getClass() == String.class && !objectOptional.get().equals("")))) {
				field.set(to, value);
			}

			field.setAccessible(originalAccessibleState);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			logger.error(e.getMessage());
		}
	}
}
