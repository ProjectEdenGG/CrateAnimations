package gg.projecteden.crates;

import org.bukkit.plugin.java.JavaPlugin;

public final class CrateAnimations extends JavaPlugin {

	private static CrateAnimations instance;
	private static boolean debug = true;

	public static CrateAnimations getInstance() {
		return CrateAnimations.instance;
	}

	public CrateAnimations() {
		CrateAnimations.instance = this;
	}

	public static void debug(String message) {
		if (debug)
			getInstance().getLogger().info(message);
	}

}
