package me.luucka.pillars.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class PillarCommand {

	@Getter
	private final String label;

	@Getter
	@Setter
	private String description;

	@Getter
	private final List<String> aliases;

	@Getter
	@Setter
	private String permission;

	@Getter
	@Setter
	private boolean playerOnly = false;

	public PillarCommand(final String label) {
		this(label, new ArrayList<>());
	}

	public PillarCommand(final String label, final List<String> aliases) {
		this.label = label;
		this.aliases = aliases;
	}

	private final Map<String, PillarSubCommand> subs = new LinkedHashMap<>();

	protected final void registerSub(PillarSubCommand sub) {
		subs.put(sub.getSublabel().toLowerCase(Locale.ROOT), sub);
	}

	/**
	 * Override se vuoi registrare i subcommands nel costruttore o in init()
	 */
	protected void init() {
	}

	public final void register(io.papermc.paper.command.brigadier.Commands registrar) {
		init();

		final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(label);

		root.requires(src -> {
			final CommandSender sender = src.getSender();
			if (playerOnly && !(sender instanceof Player)) return false;
			final String perm = permission;
			return perm == null || perm.isBlank() || sender.hasPermission(perm);
		});

		root.executes(ctx -> {
			final CommandSender sender = ctx.getSource().getSender();
			sender.sendMessage("§7Usage:");
			for (PillarSubCommand sub : subs.values()) {
				sender.sendMessage(" §7- §f" + sub.getSublabel() + " §8– " + sub.getDescription());
			}
			return 1;
		});

		for (final PillarSubCommand sub : subs.values()) {
			final ArgumentBuilder<CommandSourceStack, ?> node = sub.build();

			node.requires(src -> {
				final CommandSender sender = src.getSender();
				if (sub.isPlayerOnly() && !(sender instanceof Player)) return false;
				final String perm = sub.getPermission();
				return perm == null || perm.isBlank() || sender.hasPermission(perm);
			});

			root.then(node);
		}

		registrar.register(root.build(), description, aliases);
	}
}
