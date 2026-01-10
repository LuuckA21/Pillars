package me.luucka.pillars.language;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.luucka.pillars.PillarCore;
import me.luucka.pillars.util.FileUtil;
import me.luucka.pillars.util.PillarLog;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.nio.file.StandardOpenOption;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lang {

	/**
	 * The instance of this class
	 */
	private static final Lang instance = new Lang();

	private JsonObject dictionary;

	private Map<String, String> plainCache;
	private Map<String, String> legacyCache;
	private Map<String, String> templateCache;

	public String getPlain(final String path) {
		return plainCache.get(path);
	}

	public String getLegacy(final String path) {
		return legacyCache.get(path);
	}

	public String getTemplate(final String path) {
		return templateCache.get(path);
	}

	public static String plain(final String path) {
		return instance.getPlain(path);
	}

	public static String legacy(final String path) {
		return instance.getLegacy(path);
	}

	public static Component component(final String path) {
		return PillarCore.component(instance.getTemplate(path));
	}

	public static Component component(final String path, Object... placeholders) {
		return PillarCore.component(instance.getTemplate(path), placeholders);
	}

	/**
	 * Loads localization data from predefined files and populates the dictionary with it.
	 * The process involves reading a default language file from the internal path, followed by
	 * attempts to override it with an external file if available.
	 * <p>
	 * Steps performed by this method:
	 * 1. Reads localization messages for the default language ("en_US") from an internal JAR file.
	 * 2. Populates the dictionary with the default messages using {@code putToDictionary}.
	 * 3. Looks for an external file with localization messages for "en_US".
	 * 4. If the external file exists, attempts to parse its contents and update the dictionary, ensuring valid JSON syntax.
	 * 5. Logs a warning message if the external file contains invalid JSON syntax.
	 * 6. Synchronizes the dictionary and updates external files via {@code updateFileIfExists}.
	 * <p>
	 * Any missing or unused keys during this process will be updated in the respective locale file as
	 * part of the synchronization.
	 * <p>
	 * Note:
	 * - The method relies on utility methods such as {@code FileUtil.readLinesFromInternalPath},
	 * {@code FileUtil.readLinesFromFile}, {@code putToDictionary}, and {@code updateFileIfExists}.
	 * - If invalid JSON syntax is detected in the external file, a warning is logged with details of the error.
	 */
	public static void load() {
		List<String> content;
		final JsonObject dictionary = new JsonObject();

		// Set early to make dumpLocale work to update old files
		instance.dictionary = dictionary;
		final String englishLangTag = "en_US";

		// Read messages from Jar file (default values)
		content = FileUtil.readLinesFromInternalPath("lang/" + englishLangTag + ".json");
		putToDictionary(dictionary, content);


		content = FileUtil.readLinesFromFile("lang/" + englishLangTag + ".json");
		if (content != null) {
			try {
				putToDictionary(dictionary, content);
			} catch (final JsonSyntaxException ex) {
				PillarLog.warn("Invalid syntax in localization file " + englishLangTag + ". Use services like https://jsonformatter.org/ to correct it. Error: " + ex.getMessage());
			}
		}

		updateFileIfExists();

		final Map<String, String> plainCache = new HashMap<>();
		final Map<String, String> legacyCache = new HashMap<>();
		final Map<String, String> templateCache = new HashMap<>();

		for (final Map.Entry<String, JsonElement> entry : dictionary.entrySet()) {
			final String key = entry.getKey();
			final JsonElement value = dictionary.get(key);

			if (value.isJsonPrimitive()) {
				String string = value.getAsString();

				if (string.isEmpty())
					string = "none";

				if (key.startsWith("prefix-") && "none".equals(string)) {
					// ignore
				} else {
					final Component component = Component.text(string);

					plainCache.put(key, string);
					legacyCache.put(key, PillarCore.LEGACY_SERIALIZER.serialize(component));
					templateCache.put(key, string);
				}
			} else if (value.isJsonArray()) {
				final JsonArray array = value.getAsJsonArray();

				final List<String> plainList = new ArrayList<>();
				final List<String> templateList = new ArrayList<>();
				final List<String> legacyList = new ArrayList<>();

				for (final JsonElement element : array)
					if (element.isJsonPrimitive()) {
						String string = element.getAsString();

						final Component component = Component.text(string);

						plainList.add(string);
						templateList.add(string);
						legacyList.add(PillarCore.LEGACY_SERIALIZER.serialize(component));

					} else {
						if (!element.isJsonNull()) {
							throw new RuntimeException("Missing element in array for lang key " + key + "! Make sure to remove ',' at the end of the list");
						}

						PillarLog.warn("Invalid element in array for lang key " + key + ": " + element + ", only Strings and primitives are supported");
					}

				plainCache.put(key, String.join("\n", plainList));
//				templateCache.put(key, Component.join(JoinConfiguration.newlines(), templateList));
				templateCache.put(key, String.join("\n", templateList));
				legacyCache.put(key, String.join("\n", legacyList));

			} else {
				if (!value.isJsonNull()) {
					throw new RuntimeException("Missing element for lang key " + key + ", check for trailing commas");
				}

				PillarLog.warn("Invalid element for lang key " + key + ": " + value + ", only Strings, primitives and arrays are supported");
			}
		}

		instance.plainCache = plainCache;
		instance.legacyCache = legacyCache;
		instance.templateCache = templateCache;
	}

	public static File updateFileIfExists() {
		return dumpToFile0(true);
	}

	/**
	 * Dumps the dictionary to a local JSON file, optionally creating the file if it does not exist.
	 * Updates the local JSON file by synchronizing it with the dictionary:
	 * - Removes unused keys that no longer exist in the dictionary.
	 * - Adds new keys that are present in the dictionary but missing in the local file.
	 * - Ensures keys are sorted in the resulting JSON file for better readability.
	 *
	 * @param createFileIfNotExists a boolean value determining whether the method should create the file if it does not exist
	 * @return the {@link File} object representing the local JSON file
	 * @throws RuntimeException if the JSON in the file is invalid or if there is an error creating/writing the file
	 */
	private static File dumpToFile0(final boolean createFileIfNotExists) {
		final String path = "lang/" + "en_US" + ".json";
		final File localFile = FileUtil.getFile(path);

		if (!localFile.exists()) {
			if (createFileIfNotExists)
				FileUtil.createIfNotExists(path);
			else
				return localFile;
		}

		JsonObject localJson;

		try {
			localJson = PillarCore.GSON.fromJson(String.join("\n", FileUtil.readLinesFromFile(localFile)), JsonObject.class);

		} catch (final JsonSyntaxException ex) {
			throw new RuntimeException("Invalid JSON in " + localFile + " file. Use services like https://jsonformatter.org/ to correct it. Error: " + ex.getMessage());
		}

		if (localJson == null)
			localJson = new JsonObject();

		// First, remove local keys that no longer exist in our dictionary
		for (final Map.Entry<String, JsonElement> entry : localJson.entrySet()) {
			final String key = entry.getKey();

			if (!instance.dictionary.has(key)) {
				PillarLog.info("Removing unused key '" + key + "' from locale file " + localFile);

				localJson.remove(key);
			}
		}

		// Then, add new keys to the local file
		for (final Map.Entry<String, JsonElement> entry : instance.dictionary.entrySet()) {
			final String key = entry.getKey();

			if (!localJson.has(key)) {
				PillarLog.info("Adding new key '" + key + "' from locale file " + localFile);

				localJson.add(key, instance.dictionary.get(key));
			}
		}

		// Trick to sort keys.
		final String unsortedDump = PillarCore.GSON_PRETTY.toJson(localJson);
		final Map<String, Object> map = PillarCore.GSON.fromJson(unsortedDump, TreeMap.class);

		FileUtil.write(localFile, Collections.singletonList(PillarCore.GSON_PRETTY.toJson(map)), StandardOpenOption.TRUNCATE_EXISTING);

		return localFile;
	}

	/**
	 * Populates the given dictionary with key-value pairs extracted from the provided content.
	 * The content is expected to be a list of JSON strings, which will be parsed and added to the dictionary.
	 * If the content is null or empty, the method does nothing.
	 *
	 * @param dictionary the {@link JsonObject} to which the parsed key-value pairs will be added
	 * @param content    a {@link List} of {@link String} representing lines of JSON content
	 * @throws JsonSyntaxException if the content contains invalid JSON syntax
	 */
	private static void putToDictionary(final JsonObject dictionary, final List<String> content) {
		if (content != null && !content.isEmpty()) {
			final JsonObject json = PillarCore.GSON.fromJson(String.join("\n", content), JsonObject.class);

			for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
				final String key = entry.getKey();

				dictionary.add(key, json.get(key));
			}
		}
	}
}

