package gg.projecteden.crates.models;

import gg.projecteden.crates.CrateAnimations;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public abstract class CrateAnimation {

	Entity baseEntity;
	BiFunction<Location, Consumer<Item>, Item> spawnItemHook;
	protected Item item;

	int taskId = -1;
	CompletableFuture<Void> completableFuture;
	AtomicInteger iteration = new AtomicInteger(0);

	public CrateAnimation(Entity baseEntity, BiFunction<Location, Consumer<Item>, Item> spawnItemHook) {
		this.baseEntity = baseEntity;
		this.spawnItemHook = spawnItemHook;
	}

	public CompletableFuture<Void> play() {
		if (this.taskId == -1) {
			this.completableFuture = new CompletableFuture<>();
			this.start();
		}
		return this.completableFuture;
	}

	public boolean isActive() {
		return this.taskId != -1;
	}

	private void start() {
		this.onStart();
		this.taskId = new BukkitRunnable() {
			@Override
			public void run() {
				CrateAnimation.this.tick(CrateAnimation.this.iteration.getAndIncrement());
			}
		}.runTaskTimer(CrateAnimations.getInstance(), 0, 1).getTaskId();
	}

	protected abstract void onStart();

	protected void stop() {
		if (this.taskId != -1) {
			CrateAnimations.getInstance().getServer().getScheduler().cancelTask(this.taskId);
			this.reset();
			this.completableFuture.complete(null);
			this.completableFuture = null;
			this.taskId = -1;
			this.onStop();
		}
	}

	protected abstract void onStop();

	protected abstract void tick(int iteration);

	public abstract void reset();

	protected ArmorStand summonZero(Location location, Consumer<ArmorStand> consumer) {
		return location.getWorld().spawn(location, ArmorStand.class, armorStand -> {
			armorStand.setVisible(false);
			armorStand.setInvulnerable(true);
			armorStand.addDisabledSlots(EquipmentSlot.values());
			armorStand.setCanTick(false);
			armorStand.setGravity(false);
			armorStand.setBasePlate(false);
			armorStand.setHeadPose(EulerAngle.ZERO);
			armorStand.setBodyPose(EulerAngle.ZERO);
			armorStand.setLeftArmPose(EulerAngle.ZERO);
			armorStand.setRightArmPose(EulerAngle.ZERO);
			armorStand.setLeftLegPose(EulerAngle.ZERO);
			armorStand.setRightLegPose(EulerAngle.ZERO);
			armorStand.setMarker(true);
			armorStand.setFireTicks(Integer.MAX_VALUE);
			consumer.accept(armorStand);
		});
	}

	protected void spawnItem(Location location) {
		this.item = this.spawnItemHook.apply(location, item -> {
			item.setCanMobPickup(false);
			item.setCanPlayerPickup(false);
			item.setUnlimitedLifetime(true);
			item.setVelocity(new Vector(0, 0, 0));
		});
	}
}
