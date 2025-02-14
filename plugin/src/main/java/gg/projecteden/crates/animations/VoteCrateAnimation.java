package gg.projecteden.crates.animations;

import com.destroystokyo.paper.ParticleBuilder;
import gg.projecteden.crates.CrateAnimations;
import gg.projecteden.crates.models.CrateAnimationImpl;
import gg.projecteden.crates.util.ItemUtils;
import gg.projecteden.crates.util.LocationModifier.Direction;
import gg.projecteden.crates.util.RandomUtils;
import gg.projecteden.crates.util.SoundUtils;
import gg.projecteden.crates.util.SplinePath;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class VoteCrateAnimation extends CrateAnimationImpl {

	private static final ItemStack FULL = ItemUtils.setModelData(new ItemStack(Material.PAPER), "ui/crates/vote/all");
	private static final ItemStack BASE = ItemUtils.setModelData(new ItemStack(Material.PAPER), "ui/crates/vote/base");
	private static final ItemStack LID = ItemUtils.setModelData(new ItemStack(Material.PAPER), "ui/crates/vote/lid");
	private static final ItemStack KEY = ItemUtils.setModelData(new ItemStack(Material.PAPER), "misc/crate_keys/vote");

	private static final double TILT = Math.toRadians(3);


	private ArmorStand key;
	private ArmorStand lid;
	private final Location itemLoc;

	float keyYaw;
	private SplinePath keyPath;

	public VoteCrateAnimation(ArmorStand baseEntity, BiFunction<Location, Consumer<Item>, Item> spawnItemHook) {
		super(baseEntity, spawnItemHook);
		this.itemLoc = modify(this.baseEntity.getEyeLocation()).up(0.5).build();
	}

	@Override
	protected void onStart() {
		this.generateKeyPath();
		Location loc = this.keyPath.getPath().get(0).toLocation(this.baseEntity.getWorld(), this.keyYaw, 0);
		this.key = summonZero(loc, as -> {
			as.setSmall(true);
			double i = Math.toRadians(90);
			as.setHeadPose(new EulerAngle(i, 0, i));
			as.getEquipment().setHelmet(KEY);
		});
	}

	@Override
	public void stop() {
		if (this.taskId != -1) {
			CrateAnimations.getInstance().getServer().getScheduler().cancelTask(this.taskId);
			this.completableFuture.complete(null);
			this.completableFuture = null;
			this.taskId = -1;
			this.onStop();
			CrateAnimations.ANIMATIONS.remove(this);
		}
	}

	@Override
	protected void onStop() {
		if (this.item != null)
			this.item.remove();
		SoundUtils.playSound(this.baseEntity.getEyeLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
		new BukkitRunnable() {
			int iter = 0;
			@Override
			public void run() {
				if (iter < 5) { // Close lid on end
					double x = Math.toDegrees(lid.getHeadPose().getX());
					x += 20;
					lid.setHeadPose(lid.getHeadPose().setX(Math.toRadians(x)));
					iter++;
				}
				else {
					this.cancel();
					reset();
				}
			}
		}.runTaskTimer(CrateAnimations.getInstance(), 0, 1);
	}

	@Override
	public void reset() {
		this.baseEntity.setHeadPose(EulerAngle.ZERO);
		this.baseEntity.getEquipment().setHelmet(FULL);
		if (this.key != null)
			this.key.remove();
		if (this.lid != null)
			this.lid.remove();
		if (this.item != null)
			this.item.remove();
	}

	@Override
	protected void tick(int iteration) {
		int wait = 0;
		if (iteration < this.keyPath.getPath().size()) { // Move key on path
			this.key.teleport(this.keyPath.getPath().get(iteration).toLocation(this.key.getWorld(), this.keyYaw, 0));
		}

		if (iteration < this.keyPath.getPath().size() + (wait += 10))
			return;

		if (iteration == this.keyPath.getPath().size() + wait) {
			SoundUtils.playSound(this.baseEntity.getEyeLocation(), Sound.BLOCK_CHEST_LOCKED, 1f, 1f);
		}

		if (iteration < this.keyPath.getPath().size() + (wait += 10)) { // Turn the key
			double z = Math.toDegrees(this.key.getHeadPose().getZ());
			if (z == 0)
				z = 360;
			z -= 10;
			this.key.setHeadPose(this.key.getHeadPose().setZ(Math.toRadians(z)));
		}

		if (iteration == this.keyPath.getPath().size() + (wait += 10)) // Remove key
			this.key.remove();

		if (iteration < this.keyPath.getPath().size() + (wait += 20)) // Small Wait
			return;

		if (iteration < this.keyPath.getPath().size() + (wait += 30)) { // Shaking
			if (iteration % 2 == 0) {
				for (int i = 0; i < 3; i++) {
					Location particleLoc = this.baseEntity.getEyeLocation().clone();
					particleLoc.add(RandomUtils.randomDouble(-.75f, .75f), RandomUtils.randomDouble(-.75f, .75f), RandomUtils.randomDouble(-.75f, .75f));
					new ParticleBuilder(Particle.DUST)
							.color(Color.fromRGB(77, 55, 32))
							.location(particleLoc)
							.allPlayers()
							.spawn();
				}
			}
			SoundUtils.playSound(this.baseEntity.getEyeLocation(), Sound.BLOCK_SAND_HIT, .4f, .7f);
			switch (iteration % 4) {
				case 0 -> this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setX(TILT));
				case 1 -> this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setX(-TILT));
				case 2 -> this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setZ(TILT));
				case 3 -> this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setZ(-TILT));
			}
		}

		if (iteration == this.keyPath.getPath().size() + (wait)) { // Reset and setup for hinge
			this.baseEntity.setHeadPose(EulerAngle.ZERO);
			this.baseEntity.getEquipment().setHelmet(BASE);
			Location loc = modify(this.baseEntity.getLocation()).backwards(.4).up(0.58).build();
			this.lid = summonZero(loc, as -> {
				as.getEquipment().setHelmet(LID);
			});
		}

		if (iteration < this.keyPath.getPath().size() + (wait += 15)) // Small wait
			return;

		if (iteration == this.keyPath.getPath().size() + (wait += 5)) {
			this.spawnItem(this.itemLoc.clone());
			SoundUtils.playSound(this.baseEntity.getEyeLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
		}

		if (iteration < this.keyPath.getPath().size() + (wait += 10)) { // Hinge lid
			double x = Math.toDegrees(this.lid.getHeadPose().getX());
			if (x == 0)
				x = 360;
			x -= 100 / 15d; // Get from 360 -> 260 over 15 ticks
			this.lid.setHeadPose(this.lid.getHeadPose().setX(Math.toRadians(x)));
		}
		if (iteration == this.keyPath.getPath().size() + wait)
			SoundUtils.playSound(this.baseEntity.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.6f);

		if (iteration == this.keyPath.getPath().size() + (wait += 70)) // Wait 2 seconds before stopping
			this.stop();
	}

	private void generateKeyPath() {
		Location loc = modify(this.baseEntity.getLocation()).forwards(1.5).up(1.17).facing(Direction.BACKWARDS).build();
		this.keyYaw = loc.getYaw();
		this.keyPath = new SplinePath(.1, .1,
				loc.toVector(),
				modify(this.baseEntity.getLocation()).forwards(1).up(1.17).facing(Direction.BACKWARDS).build().toVector());
	}

}
