package gg.projecteden.crates.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

	public static ItemStack setModelData(ItemStack item, String modelData) {
		ItemMeta meta = item.getItemMeta();
		meta.setItemModel(NamespacedKey.minecraft(modelData));
		item.setItemMeta(meta);
		return item;
	}

}
