package gg.projecteden.crates.animations;

import gg.projecteden.crates.util.ItemUtils;
import gg.projecteden.crates.util.LocationModifier.Direction;
import gg.projecteden.crates.util.SplinePath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;
import gg.projecteden.crates.models.CrateAnimation;
import org.bukkit.util.EulerAngle;

import java.util.function.BiFunction;

public class VoteCrateAnimation extends CrateAnimation {

	private static final ItemStack FULL = ItemUtils.setModelData(new ItemStack(Material.PAPER), 13010);
	private static final ItemStack BASE = ItemUtils.setModelData(new ItemStack(Material.PAPER), 13011);
	private static final ItemStack LID = ItemUtils.setModelData(new ItemStack(Material.PAPER), 13012);
	private static final ItemStack KEY = ItemUtils.setModelData(new ItemStack(Material.PAPER), 10000);

	private static final double TILT = Math.toRadians(5);


	private ArmorStand key;
	private ArmorStand lid;

	private SplinePath keyPath;

	public VoteCrateAnimation(ArmorStand baseEntity, BiFunction<Location, Consumer<Item>, Item> spawnItemHook) {
		super(baseEntity, spawnItemHook);
	}

	@Override
	protected void onStart() {
		this.generateKeyPath();
		this.key = summonZero(this.keyPath.getPath().get(0).toLocation(this.baseEntity.getWorld()), as -> {
			as.setSmall(true);
			double i = Math.toRadians(90);
			as.setHeadPose(new EulerAngle(i, 0, i));
			as.getEquipment().setHelmet(KEY);
		});
	}

	@Override
	protected void onStop() {
		this.reset();
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
		if (iteration < this.keyPath.getPath().size()) { // Move key on path
			this.key.teleport(this.keyPath.getPath().get(iteration).toLocation(this.key.getWorld()));
		}
		else if (iteration < this.keyPath.getPath().size() + 10) { // Turn the key
			EulerAngle current = this.key.getHeadPose();
			current.setZ(Math.toRadians(Math.toDegrees(current.getZ()) + 10));
			this.key.setHeadPose(current);
		}

		if (iteration == this.keyPath.getPath().size() + 10) // Remove key
			this.key.remove();

		if (iteration < this.keyPath.getPath().size() + 30) // Small Wait
			return;

		if (iteration < this.keyPath.getPath().size() + 80) { // Shaking
			switch (iteration % 4) {
				case 0 -> this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setX(TILT));
				case 1 -> this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setX(-TILT));
				case 2 -> this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setZ(TILT));
				case 3 -> this.baseEntity.setHeadPose(this.baseEntity.getHeadPose().setZ(-TILT));
			}
		}

		if (iteration == this.keyPath.getPath().size() + 80) { // Reset and setup for hinge
			this.baseEntity.setHeadPose(EulerAngle.ZERO);
			this.baseEntity.getEquipment().setHelmet(BASE);
			Location loc = modify(this.baseEntity.getLocation()).backwards(.4).up(0.58).build();
			this.lid = summonZero(loc, as -> {
				as.getEquipment().setHelmet(LID);
			});
		}

		if (iteration < this.keyPath.getPath().size() + 90) // Small wait
			return;

		this.spawnItem(modify(this.baseEntity.getLocation()).up(0.75).build());

		if (iteration < this.keyPath.getPath().size() + 105) { // Hinge lid
			double x = Math.toDegrees(this.lid.getHeadPose().getX());
			if (x == 0)
				x = 360;
			x -= 100 / 15d; // Get from 360 -> 260 over 15 ticks
			this.lid.setHeadPose(this.lid.getHeadPose().setX(Math.toRadians(x)));
		}

		if (iteration == this.keyPath.getPath().size() + 145) // Wait 2 seconds before stopping
			this.stop();
	}

	private void generateKeyPath() {
		this.keyPath = new SplinePath(.1, .1,
				modify(this.baseEntity.getLocation()).forwards(1.5).up(1.17).facing(Direction.BACKWARDS).build().toVector(),
				modify(this.baseEntity.getLocation()).forwards(1).up(1.17).facing(Direction.BACKWARDS).build().toVector());
	}

}
