package gg.projecteden.crates.animations;

import com.destroystokyo.paper.ParticleBuilder;
import gg.projecteden.crates.CrateAnimations;
import gg.projecteden.crates.models.CrateAnimationImpl;
import gg.projecteden.crates.util.ItemUtils;
import gg.projecteden.crates.util.RandomUtils;
import gg.projecteden.crates.util.SoundUtils;
import gg.projecteden.crates.util.VectorUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MinigamesCrateAnimation extends CrateAnimationImpl {

	private static final ItemStack FULL = ItemUtils.setModelData(new ItemStack(Material.PAPER), "ui/crates/minigames/all");
	private static final ItemStack BASE = ItemUtils.setModelData(new ItemStack(Material.PAPER), "ui/crates/minigames/base");
	private static final ItemStack LID = ItemUtils.setModelData(new ItemStack(Material.PAPER), "ui/crates/minigames/lid");
	private static final double TILT1 = Math.toRadians(3);
	static List<Material> DYES = new ArrayList<>() {{
		this.add(Material.RED_DYE);
		this.add(Material.ORANGE_DYE);
		this.add(Material.YELLOW_DYE);
		this.add(Material.LIME_DYE);
		this.add(Material.BLUE_DYE);
		this.add(Material.LIGHT_BLUE_DYE);
		this.add(Material.PURPLE_DYE);
		this.add(Material.PINK_DYE);
	}};

	private ArmorStand lid;

	public MinigamesCrateAnimation(ArmorStand baseEntity, BiFunction<Location, Consumer<Item>, Item> spawnItemHook) {
		super(baseEntity, spawnItemHook);
	}

	@Override
	public void reset() {
		this.baseEntity.setHeadPose(EulerAngle.ZERO);
		this.baseEntity.getEquipment().setHelmet(FULL);
		if (this.lid != null)
			this.lid.remove();
		if (this.item != null)
			this.item.remove();
	}

	@Override
	protected void onStart() {

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
					x -= 20;
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
	protected void tick(int iteration) {
		int wait = 0;
		if (iteration < (wait += 10)) // initial wait delay
			return;

		if (iteration < (wait += 15)) {
			switch (iteration % 4) {
				case 0 -> {
					this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setX(TILT1));
					this.baseEntity.getWorld().playSound(this.baseEntity.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, .5f, .5f);
				}
				case 1 -> this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setX(-TILT1));
				case 2 -> this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setZ(TILT1));
				case 3 -> {
					this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setZ(-TILT1));
					this.baseEntity.getWorld().playSound(this.baseEntity.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, .5f, .5f);
				}
			}
			for (int i = 0; i < 4; i++) {
				Location particleLoc = this.baseEntity.getLocation().clone().add(0, 1.8, 0);
				particleLoc.add(RandomUtils.randomDouble(-1f, 1f), RandomUtils.randomDouble(-1f, 1f), RandomUtils.randomDouble(-1f, 1f));
				new ParticleBuilder(Particle.DUST)
						.location(particleLoc)
						.allPlayers()
						.data(new DustOptions(Color.fromRGB(RandomUtils.randomInt(0, 255), RandomUtils.randomInt(0, 255), RandomUtils.randomInt(0, 255)), 1))
						.spawn();
			}
			return;
		}

		if (iteration == wait) { // Reset and setup for hinge
			this.baseEntity.setHeadPose(EulerAngle.ZERO);
			this.baseEntity.getEquipment().setHelmet(BASE);
			Location loc = modify(this.baseEntity.getLocation()).up(0.41).build();
			this.lid = summonZero(loc, as -> {
				as.getEquipment().setHelmet(LID);
			});
		}

		if (iteration < (wait += 10))
			return;

		if (iteration == (wait += 5))
			SoundUtils.playSound(this.baseEntity.getEyeLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);

		if (iteration < (wait + 10)) { // Hinge lid
			double x = Math.toDegrees(this.lid.getHeadPose().getX());
			if (x == 360)
				x = 0;
			x += 100 / 15d; // Get from 360 -> 260 over 15 ticks
			this.lid.setHeadPose(this.lid.getHeadPose().setX(Math.toRadians(x)));
		}

		if (iteration + 5 < (wait += 10)) { // Spit item crack
			Location location = this.baseEntity.getLocation().clone().add(0, 2, 0);
			location.setYaw(location.getYaw() + 180);
			location.setPitch(-25);
			Vector direction = modify(location).backwards(1).up(.25).build().getDirection().multiply(0.4);
			for (int i = 0; i < 50; i++) {
				double x = direction.getX() + RandomUtils.randomDouble(-.2, .2);
				double y = direction.getY() + RandomUtils.randomDouble(-.2, .2);
				double z = direction.getZ() + RandomUtils.randomDouble(-.2, .2);
				new ParticleBuilder(Particle.ITEM)
						.location(location)
						.allPlayers()
						.data(new ItemStack(RandomUtils.randomElement(DYES)))
						.offset(x, y, z)
						.count(0)
						.extra(1)
						.spawn();
			}
			location.getWorld().playSound(location, Sound.ENTITY_CHICKEN_EGG, 1f, 1f);
		}

		if (iteration == wait - 10)  // spawn item
			this.spawnItem(modify(baseEntity.getLocation()).up(2).backwards(.5).build());

		if (iteration == wait)
			SoundUtils.playSound(this.baseEntity.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.6f);

		if (iteration == (wait + 70)) // Wait 3.5 seconds before stopping
			this.stop();
	}
}
