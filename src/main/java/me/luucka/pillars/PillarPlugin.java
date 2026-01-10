package me.luucka.pillars;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.luucka.pillars.language.Lang;
import me.luucka.pillars.library.BukkitLibraryManager;
import me.luucka.pillars.library.Library;
import me.luucka.pillars.library.LibraryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public abstract class PillarPlugin extends JavaPlugin {

	private static PillarPlugin instance;

	public static PillarPlugin getInstance() {
		if (instance == null) {
			try {
				instance = JavaPlugin.getPlugin(PillarPlugin.class);

			} catch (final IllegalStateException ex) {
				if (Bukkit.getPluginManager().getPlugin("PlugMan") != null || Bukkit.getPluginManager().getPlugin("PlugManX") != null)
					Bukkit.getLogger().severe("Failed to get instance of the plugin, if you reloaded using PlugMan you need to do a clean restart instead.");

				throw ex;
			}

			Objects.requireNonNull(instance, "Cannot get a new instance! Have you reloaded?");
		}

		return instance;
	}

	public static boolean hasInstance() {
		return instance != null;
	}

	protected void onPillarLoad() {
	}

	protected void onPillarStart() {
	}

	protected void onPillarDisable() {
	}

	protected void onPluginReload() {
	}

	/**
	 * The library manager used to load third party libraries.
	 */
	private LibraryManager libraryManager;

	public final LibraryManager getLibraryManager() {
		if (this.libraryManager == null)
			this.libraryManager = new BukkitLibraryManager(this);

		return this.libraryManager;
	}

	public void loadLibrary(final Library library) {
		this.getLibraryManager().loadLibrary(library);
	}

	public void loadLibrary(final String groupId, final String artifactId, final String version) {
		this.loadLibrary(Library
				.builder()
				.groupId(groupId)
				.artifactId(artifactId)
				.resolveTransitiveDependencies(true)
				.version(version)
				.build());
	}

	@Override
	public final void onLoad() {
		try {
			getInstance();

			PillarLibraries.load(this);

			this.onPillarLoad();
		} catch (final Throwable t) {
			this.setEnabled(false);
			throw t;
		}
	}

	@Override
	public final void onEnable() {
		reload();
		this.getLifecycleManager().registerEventHandler(
				LifecycleEvents.COMMANDS,
				event -> {
					Commands registrar = event.registrar();

					PillarCommandRegisterScanner.scanAndLoad(registrar);
				}
		);

		this.onPillarStart();
	}

	@Override
	public final void onDisable() {
		this.onPillarDisable();
	}

	public final void reload() {
		Lang.load();
		this.onPluginReload();
	}

	@Override
	public final File getFile() {
		return super.getFile();
	}

	public final ClassLoader getPluginClassLoader() {
		return super.getClassLoader();
	}
}
