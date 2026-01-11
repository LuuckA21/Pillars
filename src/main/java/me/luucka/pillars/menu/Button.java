package me.luucka.pillars.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public abstract class Button {

	private int slot = -1;

	public abstract ItemStack item();

	public abstract void onClickedInMenu(Player player, Menu menu, ClickType click);

	public static Button make(final ItemStack item, final int slot, final Consumer<Player> onClick) {
		return new Button() {

			@Override
			public ItemStack item() {
				return item;
			}

			@Override
			public void onClickedInMenu(Player player, Menu menu, ClickType click) {
				onClick.accept(player);
			}
		};
	}
}
