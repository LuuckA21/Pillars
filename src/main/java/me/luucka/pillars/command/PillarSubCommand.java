package me.luucka.pillars.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import lombok.Setter;

public abstract class PillarSubCommand {

	protected final PillarCommand parent;

	@Getter
	protected final String sublabel;

	@Getter
	@Setter
	protected String description;

	@Getter
	@Setter
	protected String usage;

	@Getter
	@Setter
	protected String permission;

	@Getter
	@Setter
	protected boolean playerOnly = false;

	public PillarSubCommand(final PillarCommand parent, final String sublabel) {
		this.parent = parent;
		this.sublabel = sublabel;
	}

	protected abstract ArgumentBuilder<CommandSourceStack, ?> build();
}
