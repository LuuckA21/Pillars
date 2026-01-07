package me.luucka.pillars.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.luucka.pillars.PillarPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PillarLog {

	private static final ComponentLogger LOGGER = ComponentLogger.logger(PillarPlugin.getInstance().getName());

	public static void info(final String... messages) {
		for (final String message : messages) {
			if (isValidMessage(message)) {
				LOGGER.info(Component.text(message));
			}
		}
	}

	public static void warn(final String... messages) {
		for (final String message : messages) {
			if (isValidMessage(message)) {
				LOGGER.warn(Component.text(message).color(NamedTextColor.YELLOW));
			}
		}
	}

	public static void error(final String... messages) {
		for (final String message : messages) {
			if (isValidMessage(message)) {
				LOGGER.error(Component.text(message).color(NamedTextColor.RED));
			}
		}
	}

	public static void debug(final String... messages) {
		for (final String message : messages) {
			if (isValidMessage(message)) {
				LOGGER.debug(Component.text(message).color(NamedTextColor.GRAY));
			}
		}
	}

	private static boolean isValidMessage(final String message) {
		return message != null && !"none".equals(message) && !message.isBlank();
	}
}