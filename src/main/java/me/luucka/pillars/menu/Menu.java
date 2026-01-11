package me.luucka.pillars.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public abstract class Menu implements InventoryHolder {

	private Component title;

	private Integer size;

	private Player viewer;
}
