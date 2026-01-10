package me.luucka.pillars;

import io.papermc.paper.command.brigadier.Commands;
import lombok.NoArgsConstructor;
import me.luucka.pillars.command.PillarCommand;
import me.luucka.pillars.command.annotation.PillarCommandRegister;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
final class PillarCommandRegisterScanner {

	public static void scanAndLoad(final Commands registrar) {
		final List<Class<?>> classes = findValidClasses();

		for (final Class<?> clazz : classes) {
			final PillarCommandRegister autoRegister = clazz.getAnnotation(PillarCommandRegister.class);

			if (autoRegister == null) continue;
			if (!PillarCommand.class.isAssignableFrom(clazz)) continue;
			if (Modifier.isAbstract(clazz.getModifiers())) continue;
			if (!Modifier.isFinal(clazz.getModifiers())) {
				throw new RuntimeException("Please make " + clazz + " final for it to be registered automatically via @PillarCommandRegister");
			}

			try {
				final PillarCommand command = (PillarCommand) clazz.getDeclaredConstructor().newInstance();
				command.register(registrar);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to register command " + clazz + " due to an exception", ex);
			}
		}
	}

	private static List<Class<?>> findValidClasses() {
		final List<Class<?>> classes = new ArrayList<>();

		// Ignore anonymous inner classes
		final Pattern anonymousClassPattern = Pattern.compile("\\w+\\$[0-9]$");

		try (final JarFile file = new JarFile(PillarPlugin.getInstance().getFile())) {
			for (final Enumeration<JarEntry> entry = file.entries(); entry.hasMoreElements(); ) {
				final JarEntry jar = entry.nextElement();
				final String name = jar.getName().replace("/", ".");

				// Ignore files such as settings.yml
				if (!name.endsWith(".class"))
					continue;

				final String className = name.substring(0, name.length() - 6);
				Class<?> clazz = null;

				// Look up the Java class, silently ignore if failing
				try {
					clazz = PillarPlugin.getInstance().getPluginClassLoader().loadClass(className);

				} catch (final ClassFormatError | VerifyError | NoClassDefFoundError | ClassNotFoundException |
							   IncompatibleClassChangeError error) {
					continue;
				}

				// Ignore abstract or anonymous classes
				if (!Modifier.isAbstract(clazz.getModifiers()) && !anonymousClassPattern.matcher(className).find())
					classes.add(clazz);
			}

		} catch (final Throwable t) {
			try {
				throw new Exception(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return classes;
	}
}
