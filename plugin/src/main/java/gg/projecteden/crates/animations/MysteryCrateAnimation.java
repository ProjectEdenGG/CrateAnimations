package gg.projecteden.crates.animations;

import com.destroystokyo.paper.ParticleBuilder;
import gg.projecteden.crates.CrateAnimations;
import gg.projecteden.crates.models.CrateAnimationImpl;
import gg.projecteden.crates.util.ItemUtils;
import gg.projecteden.crates.util.LocationModifier.Direction;
import gg.projecteden.crates.util.SoundUtils;
import gg.projecteden.crates.util.SplinePath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MysteryCrateAnimation extends CrateAnimationImpl {

	private static final ItemStack FULL = ItemUtils.setModelData(new ItemStack(Material.PAPER), "ui/crates/mystery/all");
	private static final ItemStack BASE = ItemUtils.setModelData(new ItemStack(Material.PAPER), "ui/crates/mystery/base");
	private static final ItemStack LID = ItemUtils.setModelData(new ItemStack(Material.PAPER), "ui/crates/mystery/lid");
	private static final ItemStack RUNES = ItemUtils.setModelData(new ItemStack(Material.PAPER), "ui/crates/mystery/runes");
	private static final ItemStack KEY = ItemUtils.setModelData(new ItemStack(Material.PAPER), "misc/crate_keys/mystery");

	private ArmorStand key;
	private ArmorStand lid;
	private ArmorStand runes;
	private final Location itemLoc;

	float keyYaw;
	private SplinePath keyPath;

	public MysteryCrateAnimation(ArmorStand baseEntity, BiFunction<Location, Consumer<Item>, Item> spawnItemHook) {
		super(baseEntity, spawnItemHook);
		this.itemLoc = modify(this.baseEntity.getEyeLocation()).up(0.25).build();
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
				else if (iter < 10) {
					double x = Math.toDegrees(runes.getHeadPose().getX());
					x += 29;
					runes.setHeadPose(runes.getHeadPose().setX(Math.toRadians(x)));
					iter++;
				}
				else {
					SoundUtils.playSound(baseEntity.getEyeLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 1f, 1f);
					this.cancel();
					reset();
				}
			}
		}.runTaskTimer(CrateAnimations.getInstance(), 0, 1);
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

		if (iteration == this.keyPath.getPath().size() + (wait)) { // Reset and setup for hinge
			this.baseEntity.setHeadPose(EulerAngle.ZERO);
			this.baseEntity.getEquipment().setHelmet(BASE);
			Location loc = modify(this.baseEntity.getLocation()).backwards(.625).up(0.46).build();
			this.lid = summonZero(loc, as -> {
				as.getEquipment().setHelmet(LID);
			});
			this.runes = summonZero(loc, as -> {
				as.getEquipment().setHelmet(RUNES);
			});
		}

		if (iteration < this.keyPath.getPath().size() + wait + 50) {
			int i = iteration - (this.keyPath.getPath().size() + wait);
			new ParticleBuilder(Particle.ENCHANT)
					.location(this.baseEntity.getEyeLocation())
					.offset(0, .5, 0)
					.count(i)
					.spawn();
		}

		wait += 50;
		if (iteration < this.keyPath.getPath().size() + (wait += 20)) // small wait
			return;

		if (iteration == this.keyPath.getPath().size() + wait)
			SoundUtils.playSound(this.baseEntity.getEyeLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1f);

		if (iteration < this.keyPath.getPath().size() + (wait += 5)) { // Hinge runes
			double x = Math.toDegrees(this.runes.getHeadPose().getX());
			if (x == 0)
				x = 360;
			x -= 145 / 5d; // Get from 360 -> 260 over 15 ticks
			this.runes.setHeadPose(this.runes.getHeadPose().setX(Math.toRadians(x)));
		}

		if (iteration < this.keyPath.getPath().size() + (wait += 15)) // Small Wait
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

		if (iteration == this.keyPath.getPath().size() + (wait += 70)) // Wait 2 seconds before stopping
			this.stop();
	}

	@Override
	public void reset() {
		this.baseEntity.setHeadPose(EulerAngle.ZERO);
		this.baseEntity.getEquipment().setHelmet(FULL);
		new BukkitRunnable() {
			@Override
			public void run() {
				if (key != null)
					key.remove();
				if (lid != null)
					lid.remove();
				if (runes != null)
					runes.remove();
				if (item != null)
					item.remove();
			}
		}.runTaskLater(CrateAnimations.getInstance(), 1);
	}

	private void generateKeyPath() {
		Location loc = modify(this.baseEntity.getLocation()).forwards(1.75).up(1.37).facing(Direction.BACKWARDS).build();
		this.keyYaw = loc.getYaw();
		this.keyPath = new SplinePath(.1, .1,
				loc.toVector(),
				modify(this.baseEntity.getLocation()).forwards(1.25).up(1.37).facing(Direction.BACKWARDS).build().toVector());
	}

}
