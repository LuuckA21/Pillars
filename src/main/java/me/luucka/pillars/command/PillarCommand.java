package me.luucka.pillars.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class PillarCommand {

	public abstract String label();

	public abstract String description();

	public List<String> aliases() {
		return List.of();
	}

	public String permission() {
		return null;
	}

	public boolean playerOnly() {
		return false;
	}

	private final Map<String, PillarSubCommand> subs = new LinkedHashMap<>();

	protected final void registerSub(PillarSubCommand sub) {
		subs.put(sub.name().toLowerCase(Locale.ROOT), sub);
	}

	/**
	 * Override se vuoi registrare i subcommands nel costruttore o in init()
	 */
	protected void init() {
	}

	public final void register(io.papermc.paper.command.brigadier.Commands registrar) {
		init();

		final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(label());

		root.requires(src -> {
			final CommandSender sender = src.getSender();
			if (playerOnly() && !(sender instanceof Player)) return false;
			final String perm = permission();
			return perm == null || perm.isBlank() || sender.hasPermission(perm);
		});

		root.executes(ctx -> {
			final CommandSender sender = ctx.getSource().getSender();
			sender.sendMessage("§eUso:");
			for (PillarSubCommand sub : subs.values()) {
				sender.sendMessage(" §7- §f" + sub.usage() + " §8– " + sub.description());
			}
			return 1;
		});

		for (PillarSubCommand sub : subs.values()) {
			final ArgumentBuilder<CommandSourceStack, ?> node = sub.build();

			node.requires(src -> {
				final CommandSender sender = src.getSender();
				if (sub.playerOnly() && !(sender instanceof Player)) return false;
				final String perm = sub.permission();
				return perm == null || perm.isBlank() || sender.hasPermission(perm);
			});

			root.then(node);
		}

		registrar.register(root.build(), description(), aliases());
	}
}
