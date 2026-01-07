package me.luucka.pillars.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public abstract class PillarSubCommand {

	protected final PillarCommand mainCommand;

	public PillarSubCommand(PillarCommand mainCommand) {
		this.mainCommand = mainCommand;
	}

	protected abstract String name();

	protected abstract String description();

	protected abstract String usage();

	String permission() {
		return null;
	}

	boolean playerOnly() {
		return false;
	}

	/**
	 * Ritorna il nodo Brigadier del subcommand (literal("reload").then(...).executes(...))
	 */
	protected abstract ArgumentBuilder<CommandSourceStack, ?> build();
}
