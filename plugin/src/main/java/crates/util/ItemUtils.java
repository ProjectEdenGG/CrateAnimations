package crates.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

	public static ItemStack setModelData(ItemStack item, int modelData) {
		ItemMeta meta = item.getItemMeta();
		meta.setCustomModelData(modelData);
		item.setItemMeta(meta);
		return item;
	}

}
