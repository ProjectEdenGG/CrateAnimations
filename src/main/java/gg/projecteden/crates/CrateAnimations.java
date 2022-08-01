package gg.projecteden.crates;

import org.bukkit.plugin.java.JavaPlugin;

public final class CrateAnimations extends JavaPlugin {

	private static CrateAnimations instance;

	public static CrateAnimations getInstance() {
		return CrateAnimations.instance;
	}

	public CrateAnimations() {
		CrateAnimations.instance = this;
	}

	@Override
	public void onEnable() { }

}
