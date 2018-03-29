package com.transformuk.hee.tis.genericupload.service.intTests;

import com.transformuk.hee.tis.genericupload.service.Application;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = Application.class)
public class ServiceIntegrationTests {
	public static void main(String[] args) throws Exception {
		Class klass = com.transformuk.hee.tis.tcs.;
		URL location = klass.getResource('/' + klass.getName().replace('.', '/') + ".class");
		System.out.println(location);
		//launchApp(new File("target"));
	}

	private static void launchApp(File fatJar, String... args) throws Exception {
		ClassLoader classLoader = new URLClassLoader(new URL[] {fatJar.toURI().toURL()});
		Class<?> mainClass = classLoader.loadClass(getMainClassName(fatJar));
		Method mainMethod = mainClass.getMethod("main", String[].class);
		mainMethod.invoke(null, new Object[] {args});
	}

	private static String getMainClassName(File fatJar) throws IOException {
		try (JarFile jarFile = new JarFile(fatJar)) {
			return jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
		}
	}
}
