package gg.projecteden.crates.animations;

import gg.projecteden.crates.models.CrateAnimationImpl;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class WitherCrateAnimation extends CrateAnimationImpl {

	public WitherCrateAnimation(ArmorStand baseEntity, BiFunction<Location, Consumer<Item>, Item> spawnItemHook) {
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
