package me.luucka.pillars.util;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.luucka.pillars.PillarPlugin;

import java.io.*;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class FileUtil {

	public static File getFile(final String path) {
		return new File(PillarPlugin.getInstance().getDataFolder(), path);
	}

	public static List<String> readLinesFromInternalPath(@NonNull final String path) {
		return readLinesFromInternalPath(PillarPlugin.getInstance().getFile(), path);
	}

	public static List<String> readLinesFromInternalPath(@NonNull final File pluginFile, @NonNull final String path) {
		try (JarFile jarFile = new JarFile(pluginFile)) {

			for (final Enumeration<JarEntry> it = jarFile.entries(); it.hasMoreElements(); ) {
				final JarEntry entry = it.nextElement();

				if (entry.toString().equals(path)) {
					final InputStream is = jarFile.getInputStream(entry);
					final BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
					final List<String> lines = reader.lines().collect(Collectors.toList());

					reader.close();
					return lines;
				}
			}

		} catch (final Throwable ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static List<String> readLinesFromFile(final String fileName) {
		return readLinesFromFile(getFile(fileName));
	}

	public static List<String> readLinesFromFile(@NonNull final File file) {
		if (!file.exists())
			return null;

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			final List<String> lines = new ArrayList<>();
			String line;

			while ((line = br.readLine()) != null)
				lines.add(line);

			return lines;

		} catch (final IOException ee) {
//			throw new Exception(ee, "Could not read lines from " + file.getName());
			throw new RuntimeException(ee);
		}
	}

	public static File createIfNotExists(final String path) {
		final File datafolder = PillarPlugin.getInstance().getDataFolder();
		final int lastIndex = path.lastIndexOf('/');
		final File directory = new File(datafolder, path.substring(0, Math.max(lastIndex, 0)));

		directory.mkdirs();

		final File destination = new File(datafolder, path);

		if (!destination.exists())
			try {
				destination.createNewFile();

			} catch (final Throwable t) {
				if (t.getMessage().equals("Read-only file system") || t.getMessage().equals("Permission denied"))
					throw new RuntimeException("Failed to create file " + destination + " because the file system is read-only. Please check your permissions.");

				throw new RuntimeException("Could not create new file '" + destination + "' due to ", t);
			}

		return destination;
	}

	public static void write(final File to, final Collection<String> lines, final StandardOpenOption... options) {
		if (!to.exists()) {
			throw new RuntimeException("Cannot write to non-existing file: " + to);
		}

		try {
			final Path path = Paths.get(to.toURI());

			Files.write(path, lines, StandardCharsets.UTF_8, options);

		} catch (final ClosedByInterruptException ex) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(to, true))) {
				for (final String line : lines)
					writer.append(System.lineSeparator() + line);

			} catch (final IOException exception) {
				exception.printStackTrace();
			}

		} catch (final Exception ex) {

			// do not throw our exception since it would cause an infinite loop if there is a problem due to error writing
			if (ex instanceof IOException && "There is not enough space on the disk".equals(ex.getMessage()))
				ex.printStackTrace();
			else
				throw new RuntimeException("Failed to write to " + to, ex);
		}
	}

}
