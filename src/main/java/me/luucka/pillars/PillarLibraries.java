package me.luucka.pillars;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
final class PillarLibraries {

	public static void load(final PillarPlugin plugin) {

		plugin.loadLibrary("org.spongepowered", "configurate-yaml", "4.2.0");

	}

}
