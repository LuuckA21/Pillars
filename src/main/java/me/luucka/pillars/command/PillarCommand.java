package me.luucka.pillars.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import lombok.Setter;
import me.luucka.pillars.PillarCore;
import me.luucka.pillars.util.PillarLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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

	@Getter
	@Setter
	private boolean handleHelp = true;

	protected boolean requires(final CommandSourceStack source) {
		return true;
	}

	public PillarCommand(final String label) {
		this(label, new ArrayList<>());
	}

	public PillarCommand(final String label, final List<String> aliases) {
		this.label = label;
		this.aliases = aliases;
	}

	private final Map<String, PillarSubCommand> subs = new LinkedHashMap<>();

	protected final void registerSubcommand(final PillarSubCommand sub) {
		subs.put(sub.getSublabel().toLowerCase(Locale.ROOT), sub);
	}

	protected abstract void registerSubcommands();

	public final void register(io.papermc.paper.command.brigadier.Commands registrar) {
		registerSubcommands();

		final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(label);

		root.requires(src -> {
			final CommandSender sender = src.getSender();
			if (playerOnly && !(sender instanceof Player)) {
				return false;
			}
			final String perm = permission;
			if (perm != null && !sender.hasPermission(perm)) {
				return false;
			}
			return requires(src);
		});

		root.executes(ctx -> {
			final CommandSender sender = ctx.getSource().getSender();
			sender.sendPlainMessage("Main command info!");
			return 1;
		});

		addHelpSubcommand0(root);

		for (final PillarSubCommand sub : subs.values()) {
			if ("help".equalsIgnoreCase(sub.getSublabel()) || "?".equalsIgnoreCase(sub.getSublabel())) {
				PillarLog.error("Subcommand with label 'help' or '?' is reserved for help command!");
				continue;
			}

			final ArgumentBuilder<CommandSourceStack, ?> node = sub.build();
			node.requires(src -> {
				final CommandSender sender = src.getSender();
				if (sub.isPlayerOnly() && !(sender instanceof Player)) {
					return false;
				}
				final String perm = sub.getPermission();
				if (perm != null && !sender.hasPermission(perm)) {
					return false;
				}
				return sub.requires(src);
			});

			root.then(node);
		}

		registrar.register(root.build(), description, aliases);
	}

	private void addHelpSubcommand0(final LiteralArgumentBuilder<CommandSourceStack> root) {
		if (!handleHelp) return;

		root.then(Commands.literal("help")
				.executes(ctx -> {
					final CommandSender sender = ctx.getSource().getSender();
					buildHelper0(sender);
					return 1;
				})
		);

		root.then(Commands.literal("?")
				.executes(ctx -> {
					final CommandSender sender = ctx.getSource().getSender();
					buildHelper0(sender);
					return 1;
				})
		);
	}

	private void buildHelper0(final CommandSender sender) {
		sender.sendMessage(PillarCore.component("<gray>Usage:"));
		for (final PillarSubCommand sub : subs.values()) {
			final Component message = Component.text("- ", NamedTextColor.GRAY)
					.append(
							Component.text(sub.getUsage(), NamedTextColor.WHITE)
									.clickEvent(ClickEvent.suggestCommand(sub.getUsage()))
									.hoverEvent(HoverEvent.showText(
											Component.text("Click to suggest ", NamedTextColor.GRAY)
													.append(Component.text(sub.getUsage(), NamedTextColor.GOLD))
									))
					)
					.append(Component.text(" – ", NamedTextColor.GRAY))
					.append(Component.text(sub.getDescription(), NamedTextColor.YELLOW));

			sender.sendMessage(message);
		}
	}
}
