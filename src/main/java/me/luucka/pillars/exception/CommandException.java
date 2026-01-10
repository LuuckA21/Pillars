package me.luucka.pillars.exception;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class CommandException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final Component[] messages;

	public CommandException() {
		this((Component[]) null);
	}

	public CommandException(final String... messages) {
		super("");
		if (messages == null) {
			this.messages = null;
		} else {
			this.messages = new Component[messages.length];
			for (int i = 0; i < messages.length; i++) {
				this.messages[i] = Component.text(messages[i]);
			}
		}
	}

	public CommandException(final Component... messages) {
		super("");
		this.messages = messages;
	}

	public void sendErrorMessage(final Player player) {
		for (final Component message : this.messages) {
			player.sendMessage(message);
		}
	}

}
