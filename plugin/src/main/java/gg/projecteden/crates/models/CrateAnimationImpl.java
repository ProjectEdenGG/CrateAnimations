package gg.projecteden.crates.models;

import gg.projecteden.crates.CrateAnimations;
import gg.projecteden.crates.api.models.CrateAnimation;
import gg.projecteden.crates.util.LocationModifier;
import gg.projecteden.crates.util.VectorUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class CrateAnimationImpl implements CrateAnimation {

	protected final ArmorStand baseEntity;
	private final BiFunction<Location, Consumer<Item>, Item> spawnItemHook;
	protected Item item;

	int taskId = -1;
	CompletableFuture<Void> completableFuture;
	AtomicInteger iteration = new AtomicInteger(0);

	public CrateAnimationImpl(ArmorStand baseEntity, BiFunction<Location, Consumer<Item>, Item> spawnItemHook) {
		this.baseEntity = baseEntity;
		this.spawnItemHook = spawnItemHook;
	}

	@Override
	public CompletableFuture<Void> play() {
		if (this.taskId == -1) {
			CrateAnimations.debug("Calling start");
			this.completableFuture = new CompletableFuture<>();
			this.start();
		}
		CrateAnimations.debug("End of play method");
		return this.completableFuture;
	}

	@Override
	public boolean isActive() {
		return this.taskId != -1;
	}

	private void start() {
		CrateAnimations.debug("Calling on start");
		this.onStart();
		CrateAnimations.debug("Starting runnable");
		this.taskId = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					tick(iteration.getAndIncrement());
				} catch (Throwable ex) {
					ex.printStackTrace();
					stop();
					reset();
				}
				CrateAnimations.debug("tick " + iteration.get());
			}
		}.runTaskTimer(CrateAnimations.getInstance(), 0, 1).getTaskId();
	}

	protected abstract void onStart();

	@Override
	public void stop() {
		CrateAnimations.debug("Stopping");
		if (this.taskId != -1) {
			CrateAnimations.debug("");
			CrateAnimations.getInstance().getServer().getScheduler().cancelTask(this.taskId);
			CrateAnimations.debug("Calling reset");
			this.reset();
			this.completableFuture.complete(null);
			this.completableFuture = null;
			this.taskId = -1;
			CrateAnimations.debug("Calling onStop");
			this.onStop();
		}
	}

	protected abstract void onStop();

	protected abstract void tick(int iteration);

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
			armorStand.setFireTicks(Integer.MAX_VALUE);
			armorStand.setCollidable(false);
			consumer.accept(armorStand);
		});
	}

	protected void spawnItem(Location location) {
		Consumer<Item> itemConsumer = item -> {
			item.setCanMobPickup(false);
			item.setCanPlayerPickup(false);
			item.setUnlimitedLifetime(true);
			item.setVelocity(VectorUtils.ZERO);
		};
		this.item = this.spawnItemHook.apply(location, itemConsumer);
	}

	protected LocationModifier modify(Location location) {
		return new LocationModifier(location);
	}

}
