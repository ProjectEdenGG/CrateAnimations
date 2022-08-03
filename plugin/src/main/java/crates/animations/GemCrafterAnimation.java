package crates.animations;

import com.destroystokyo.paper.ParticleBuilder;
import crates.models.CrateAnimationImpl;
import crates.util.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Hangable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GemCrafterAnimation extends CrateAnimationImpl {

	Location center;
	Location bookshelf;
	Direction direction;
	SplinePath path;
	List<SplinePath> particlePaths = new ArrayList<>();
	ArmorStand book;

	public GemCrafterAnimation(ArmorStand center, BiFunction<Location, Consumer<Item>, Item> spawnItemHook) {
		super(null, spawnItemHook);
		this.center = center.getLocation().toCenterLocation();
	}

	@Override
	public void reset() {
		this.center.getBlock().setType(Material.BARRIER);
		this.book.remove();
		if (this.item != null)
			this.item.remove();
		Location lantern = this.center.clone().add(0, 3, 0);
		lantern.getBlock().setType(Material.SOUL_LANTERN);
		Hangable data = (Hangable) lantern.getBlock().getBlockData();
		data.setHanging(true);
		lantern.getBlock().setBlockData(data);
	}


	@Override
	protected void onStart() {
		this.findBookshelf();
		this.generateBookPath();
		this.generateParticlePaths();
		this.summonStand();
	}

	private void findBookshelf() {
		Location shelf = null;
		int attempts = 0;
		while (shelf == null || attempts == 100) {
			double x = RandomUtils.randomDouble(5) - 2.5;
			double y = RandomUtils.randomDouble(1, 3);
			double z = RandomUtils.randomDouble(5) - 2.5;
			Location loc = this.center.clone().add(x, y, z);
			if (loc.getBlock().getType() != Material.BOOKSHELF)
				continue;
			Direction direction = null;
			for (Direction dir : Direction.values()) {
				if (direction == null || loc.getBlock().getRelative(dir.getBlockFace()).getType() == Material.AIR) {
					double dist = loc.distance(this.center);
					if (direction != null)
						dist = loc.getBlock().getRelative(direction.getBlockFace()).getLocation().toCenterLocation().distance(this.center);
					if (dist > loc.getBlock().getRelative(dir.getBlockFace()).getLocation().toCenterLocation().distance(this.center))
						direction = dir;
				}
			}
			if (direction != null)
				shelf = loc;
			this.direction = direction;
			attempts++;
		}
		this.bookshelf = shelf;
	}

	private void generateBookPath() {
		this.path = new SplinePath(
				.05,
				.025,
				this.bookshelf.toCenterLocation().subtract(0, 1.75, 0).toVector(),
				this.bookshelf.getBlock().getRelative(this.direction.getBlockFace()).getLocation().toCenterLocation().subtract(0, 1.5, 0).toVector(),
				this.center.toCenterLocation().subtract(0, 1.2, 0).toVector()
		);
	}

	private void generateParticlePaths() {
		this.particlePaths = new ArrayList<>();
		for (int i = 0; i < RandomUtils.randomInt(3, 5); i++) {
			Vector direction = new Vector(0, 1, 0);
			VectorUtils.setLength(direction, 0.5D);
			List<Vector> points = new ArrayList<>();

			Vector v = this.center.toCenterLocation().toVector();

			points.add(v.clone());

			for (int j = 0; j < 6; j++) {
				v.add(VectorUtils.randomCone(direction, 0.3D));
				points.add(v.clone());
			}

			SplinePath path = new SplinePath(0.04, 0.075, points.toArray(new Vector[0]));
			path.getPath().addAll(new SplinePath(0.04 / 2, 0.075 / 2, path.getPath().get(path.getPath().size() - 1), this.center.toCenterLocation().add(0, 3, 0).toVector()).getPath());
			this.particlePaths.add(path);
		}
	}


	private void summonStand() {
		Location loc = this.path.getPath().get(0).toLocation(this.center.getWorld());
		this.book = this.summonZero(loc, as -> {
			as.getEquipment().setHelmet(ItemUtils.setModelData(new ItemStack(Material.PAPER), 13024)); // Enchantment Table Book Model
			as.setHeadPose(new EulerAngle(0, Math.toRadians(this.direction.getYaw()), 0));
		});
	}

	@Override
	protected void onStop() {
		this.reset();
	}

	int rotationStart = 40;
	@Override
	protected void tick(int iteration) {
		if (iteration <= 20 && iteration % 5 == 0)
			SoundUtils.playSound(this.center, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
		if (iteration < this.path.getPath().size()) {
			this.book.teleport(this.path.getPath().get(iteration).toLocation(this.center.getWorld()));
			if (iteration % 10 == 0) {
				Location loc = this.path.getPath().get(iteration).toLocation(this.center.getWorld()).clone().add(0, 1.5, 0);
				new ParticleBuilder(Particle.ENCHANTMENT_TABLE)
						.count(0)
						.location(loc)
						.allPlayers()
						.spawn();
			}
			if (iteration > rotationStart) {
				int totalRotation = this.path.getPath().size() - rotationStart;
				double yawPerIteration = (this.direction.getYaw() - this.center.getYaw()) / totalRotation;
				double pitchPerRotation = -80F / totalRotation;
				double yaw = Math.toDegrees(this.book.getHeadPose().getY()) + yawPerIteration;
				double pitch = Math.toDegrees(this.book.getHeadPose().getX()) + pitchPerRotation;

				yaw = Math.toRadians(yaw);
				pitch = Math.toRadians(pitch);
				this.book.setHeadPose(new EulerAngle(pitch, yaw, 0));
			}
			if (iteration > 25 && iteration % 15 == 0)
				SoundUtils.playSound(this.center, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
		}
		if (iteration == this.path.getPath().size()) {
			this.center.getBlock().setType(Material.ENCHANTING_TABLE);
			this.book.remove();
			SoundUtils.playSound(this.center, Sound.ITEM_BOOK_PAGE_TURN, 1f, 0.75f);
		}
		if (iteration == this.path.getPath().size() + 10)
			SoundUtils.playSound(this.center, "custom.crates.gemcrafter.infuse", 1f, 1f);
		int particlesDone = this.particlePaths.stream().max(Comparator.comparingInt(sp -> sp.getPath().size())).get().getPath().size() + this.path.getPath().size() + 40;
		if (iteration >= this.path.getPath().size() + 40 && iteration < particlesDone) {
			int particleIteration = iteration - this.path.getPath().size() - 40;
			this.particlePaths.forEach(path -> {
				if (particleIteration >= path.getPath().size())
					return;
				Location loc = path.getPath().get(particleIteration).toLocation(this.center.getWorld());
				new ParticleBuilder(Particle.REVERSE_PORTAL)
						.location(loc)
						.count(0)
						.allPlayers()
						.spawn();
			});
		}
		if (iteration == particlesDone) {
			Location lantern = this.center.toCenterLocation().add(0, 3, 0);
			lantern.getBlock().setType(Material.AIR);
			for (int i = 0; i < 25; i++)
				new ParticleBuilder(Particle.BLOCK_CRACK)
						.count(1)
						.location(lantern)
						.allPlayers()
						.data(Material.WARPED_HYPHAE.createBlockData())
						.spawn();
			ItemStack gem = ItemUtils.setModelData( new ItemStack(Material.PAPER), 9000);
			gem.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
			this.spawnItem(lantern);
			this.center.getBlock().setType(Material.BARRIER);
			SoundUtils.playSound(this.center, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2f, 1f);
		}
		if (iteration == particlesDone + 50) {
			this.stop();
		}
	}

	public enum Direction {
		NORTH(BlockFace.SOUTH, 180),
		WEST(BlockFace.EAST, 90),
		SOUTH(BlockFace.NORTH, 0),
		EAST(BlockFace.WEST, -90);

		private BlockFace blockFace;
		private int yaw;

		Direction(BlockFace blockFace, int yaw) {
			this.blockFace = blockFace;
			this.yaw = yaw;
		}

		public BlockFace getBlockFace() {
			return this.blockFace;
		}

		public int getYaw() {
			return this.yaw;
		}
	}

}
