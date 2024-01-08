package gg.projecteden.crates.animations;

import com.destroystokyo.paper.ParticleBuilder;
import gg.projecteden.crates.models.CrateAnimationImpl;
import gg.projecteden.crates.util.ItemUtils;
import gg.projecteden.crates.util.RandomUtils;
import gg.projecteden.crates.util.SoundUtils;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class WeeklyWakkaCrateAnimation extends CrateAnimationImpl {


	private static final ItemStack FULL = ItemUtils.setModelData(new ItemStack(Material.PAPER), 13020);
	private static final ItemStack BASE = ItemUtils.setModelData(new ItemStack(Material.PAPER), 13021);
	private static final ItemStack LID = ItemUtils.setModelData(new ItemStack(Material.PAPER), 13022);

	private ArmorStand lid;

	public WeeklyWakkaCrateAnimation(ArmorStand baseEntity, BiFunction<Location, Consumer<Item>, Item> spawnItemHook) {
		super(baseEntity, spawnItemHook);
	}

	@Override
	protected void onStart() {
		this.baseEntity.getEquipment().setHelmet(BASE);
		this.lid = summonZero(modify(this.baseEntity.getLocation()).backwards(0.41).up(0.425).build(), as -> {
			as.getEquipment().setHelmet(LID);
		});
	}

	@Override
	protected void onStop() {
		this.baseEntity.getEquipment().setHelmet(FULL);
		this.lid.remove();
		if (this.item != null)
			this.item.remove();
	}

	@Override
	protected void tick(int iteration) {
		int wait = 10;

		if (iteration < wait) return;

		if (iteration < (wait += 5)) {
			jawUp();
			return;
		}
		if (iteration < (wait += 5)) {
			if (iteration == wait - 3) bite();
			jawDown();
			return;
		}

		if (iteration < (wait += 5)) {
			jawUp();
			return;
		}
		if (iteration < (wait += 5)) {
			if (iteration == wait - 3) bite();
			jawDown();
			return;
		}

		if (iteration < (wait += 5)) {
			jawUp();
			return;
		}
		if (iteration < (wait += 5)) {
			if (iteration == wait - 3) bite();
			jawDown();
			return;
		}

		if (iteration < (wait += 10)) return;

		if (iteration == wait) burp();
		if (iteration < (wait += 8)) {
			if (iteration == wait - 4) {
				spawnItem(this.baseEntity.getEyeLocation());
				this.item.setGravity(true);

				Location location = this.baseEntity.getLocation().clone().add(0, 2, 0);
				Vector direction = modify(location).backwards(1).build().getDirection().multiply(0.1);
				direction.setY(.2);
				this.item.setVelocity(direction);
			}

			double x = Math.toDegrees(this.lid.getHeadPose().getX());
			if (x == 0)
				x = 360;
			x -= 120 / 8d;
			this.lid.setHeadPose(this.lid.getHeadPose().setX(Math.toRadians(x)));
			return;
		}

		if (iteration < (wait += 20)) return;

		if (iteration < (wait += 8)) {
			double x = Math.toDegrees(this.lid.getHeadPose().getX());
			if (x == 0)
				x = 360;
			x += 120 / 8d;
			this.lid.setHeadPose(this.lid.getHeadPose().setX(Math.toRadians(x)));
			return;
		}

		if (iteration == wait + 40) this.stop();
	}

	private void jawUp() {
		double x = Math.toDegrees(this.lid.getHeadPose().getX());
		if (x == 0)
			x = 360;
		x -= 100 / 5d;
		this.lid.setHeadPose(this.lid.getHeadPose().setX(Math.toRadians(x)));
	}

	private void jawDown() {
		double x = Math.toDegrees(this.lid.getHeadPose().getX());
		if (x == 0)
			x = 360;
		x += 100 / 5d;
		this.lid.setHeadPose(this.lid.getHeadPose().setX(Math.toRadians(x)));
	}

	private void bite() {
		SoundUtils.playSound(this.baseEntity.getEyeLocation(), Sound.ENTITY_GENERIC_EAT, 1F, 1F);

		Location location = this.baseEntity.getLocation().clone().add(0, 2, 0);
		Vector direction = modify(location).backwards(1).up(.75).build().getDirection().multiply(0.2);
		for (int i = 0; i < 25; i++) {
			double x = direction.getX() + RandomUtils.randomDouble(-.2, .2);
			double y = direction.getY() + RandomUtils.randomDouble(0, 0.5);
			double z = direction.getZ() + RandomUtils.randomDouble(-.2, .2);

			new ParticleBuilder(Particle.ITEM_CRACK)
					.data(new ItemStack(Material.REDSTONE))
					.extra(1)
					.count(0)
					.offset(x, y, z)
					.location(this.baseEntity.getEyeLocation().clone().add(RandomUtils.randomDouble(-.5, .5), RandomUtils.randomDouble(-.1, .1), RandomUtils.randomDouble(-.5, .5)))
					.allPlayers()
					.spawn();
		}
	}

	private void burp() {
		SoundUtils.playSound(this.baseEntity.getEyeLocation(), "custom.crates.weeklywakka.burp", 1f, 1f);
	}

	@Override
	public void reset() {
		this.onStop();
	}
}
