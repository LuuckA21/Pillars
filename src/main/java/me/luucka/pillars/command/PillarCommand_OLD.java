package me.luucka.pillars.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public abstract class PillarCommand_OLD {

	public abstract String label();

	public abstract String description();

	public String permission() {
		return null;
	}

	private final List<String> aliases = new ArrayList<>();

	private final List<ArgumentBuilder<CommandSourceStack, ?>> children = new ArrayList<>();

	/**
	 * fluent helpers
	 */
	protected final void alias(String... values) {
		aliases.addAll(Arrays.asList(values));
	}

	protected final void child(ArgumentBuilder<CommandSourceStack, ?> builder) {
		children.add(builder);
	}

	protected LiteralArgumentBuilder<CommandSourceStack> createRoot() {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(label());

		String perm = permission();
		if (perm != null && !perm.isBlank()) {
			Predicate<CommandSourceStack> check = src -> src.getSender().hasPermission(perm);
			root.requires(check);
		}

		return root;
	}

	/**
	 * Aggiungi subcomandi / argomenti qui
	 */
	protected void setup(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this::execute);
	}

	protected abstract int execute(final CommandContext<CommandSourceStack> ctx);

	/**
	 * Registra il comando usando il registrar di Paper
	 */
	public final void register(io.papermc.paper.command.brigadier.Commands registrar) {
		LiteralArgumentBuilder<CommandSourceStack> root = createRoot();
		setup(root);
		children.forEach(root::then);

		// registra con alias/description
		registrar.register(
				root.build(),
				description(),
				aliases
		);
	}
}
