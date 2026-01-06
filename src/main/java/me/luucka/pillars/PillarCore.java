package me.luucka.pillars;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class PillarCore {

	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

	public static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	public static MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

	public static LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().hexColors().build();

	public static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();

	public static Component component(final String string) {
		return MINI_MESSAGE.deserialize(string);
	}

	public static Component component(final String string, Object... placeholders) {
		return MINI_MESSAGE.deserialize(string, toResolver0(placeholders));
	}

	private static TagResolver toResolver0(Object... placeholders) {
		if (placeholders == null || placeholders.length == 0) {
			return TagResolver.empty();
		}

		if ((placeholders.length % 2) != 0) {
			throw new IllegalArgumentException(
					"Placeholders must be key/value pairs (even length)"
			);
		}

		final TagResolver.Builder builder = TagResolver.builder();

		for (int i = 0; i < placeholders.length; i += 2) {
			Object keyObj = placeholders[i];
			Object valObj = placeholders[i + 1];

			if (!(keyObj instanceof String key) || key.isBlank()) {
				continue; // Consider logging a warning here
			}

			TagResolver resolver = switch (valObj) {
				case null -> Placeholder.unparsed(key, "");
				case Component c -> Placeholder.component(key, c);
				case String s -> Placeholder.unparsed(key, s);
				default -> Placeholder.unparsed(key, String.valueOf(valObj));
			};
			builder.resolver(resolver);
		}

		return builder.build();
	}
}
