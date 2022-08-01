package gg.projecteden.crates.animations;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.util.Consumer;
import gg.projecteden.crates.models.CrateAnimation;

import java.util.function.BiFunction;

public class MysteryCrateAnimation extends CrateAnimation {

	public MysteryCrateAnimation(Entity baseEntity, BiFunction<Location, Consumer<Item>, Item> spawnItemHook) {
		super(baseEntity, spawnItemHook);
	}

	@Override
	protected void onStart() {

	}

	@Override
	protected void onStop() {

	}

	@Override
	protected void tick(int iteration) {
		this.stop();
	}

	@Override
	public void reset() {

	}
}
