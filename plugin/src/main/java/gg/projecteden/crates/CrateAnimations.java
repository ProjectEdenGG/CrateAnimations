package gg.projecteden.crates;

import gg.projecteden.crates.animations.*;
import gg.projecteden.crates.api.models.CrateAnimation;
import gg.projecteden.crates.api.models.CrateAnimationsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class CrateAnimations extends JavaPlugin implements CrateAnimationsAPI {

	private static CrateAnimations instance;
	private static boolean debug = false;

	public static CrateAnimations getInstance() {
		return CrateAnimations.instance;
	}

	public CrateAnimations() {
		CrateAnimations.instance = this;
	}

	@Override
	public void onEnable() {
		Bukkit.getServicesManager().register(CrateAnimationsAPI.class, this, CrateAnimations.getInstance(), ServicePriority.Highest);
	}

	@Override
	public void onDisable() {
		Bukkit.getServicesManager().unregisterAll(CrateAnimations.getInstance());
	}

	public static void debug(String message) {
		if (debug)
			getInstance().getLogger().info(message);
	}

	@Override
	public CrateAnimation getAnimation(String type, ArmorStand entity, BiFunction<Location, Consumer<Item>, Item> itemSpawnHook) {
		return switch (type.toUpperCase()) {
			case "VOTE" -> new VoteCrateAnimation(entity, itemSpawnHook);
			case "MYSTERY" -> new MysteryCrateAnimation(entity, itemSpawnHook);
			case "WITHER" -> new WitherCrateAnimation(entity, itemSpawnHook);
			case "WEEKLY_WAKKA" -> new WeeklyWakkaCrateAnimation(entity, itemSpawnHook);
			case "GEM_CRAFTER" -> new GemCrafterAnimation(entity, itemSpawnHook);
			default -> null;
		};
	}

}
