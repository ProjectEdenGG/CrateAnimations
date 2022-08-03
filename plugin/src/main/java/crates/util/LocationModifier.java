package crates.util;

import org.bukkit.Location;

/**
 * Utility class for handling directions based on specified locations yaw/pitch
 */
public class LocationModifier {

	Location location;

	public LocationModifier(Location location) {
		this.location = location.clone();
	}

	public LocationModifier right(double distance) {
		Direction.RIGHT.apply(this.location, distance);
		return this;
	}

	public LocationModifier left(double distance) {
		Direction.LEFT.apply(this.location, distance);
		return this;
	}

	public LocationModifier backwards(double distance) {
		Direction.BACKWARDS.apply(this.location, distance);
		return this;
	}

	public LocationModifier forwards(double distance) {
		Direction.FORWARDS.apply(this.location, distance);
		return this;
	}

	public LocationModifier up(double distance) {
		Direction.UP.apply(this.location, distance);
		return this;
	}

	public LocationModifier down(double distance) {
		Direction.DOWN.apply(this.location, distance);
		return this;
	}

	public LocationModifier facing(Direction direction) {
		return this.facing(direction, 90);
	}

	public LocationModifier facing(Direction direction, float amount) {
		direction.face(this.location, amount);
		return this;
	}

	public Location build() {
		return this.location;
	}

	public enum Direction {
		UP {
			@Override
			protected void apply(Location location, double distance) {
				location.add(0, distance, 0);
			}

			@Override
			protected void face(Location location, float amount) {
				float pitch = Location.normalizePitch(location.getPitch() + amount);
				location.setPitch(pitch);
			}
		},
		DOWN {
			@Override
			protected void apply(Location location, double distance) {
				Direction.UP.apply(location, -distance);
			}

			@Override
			protected void face(Location location, float amount) {
				Direction.UP.apply(location, -amount);
			}
		},
		LEFT {
			@Override
			protected void apply(Location location, double distance) {
				float yaw = Location.normalizeYaw(location.getYaw() - 90);
				Location loc = location.clone();
				loc.setYaw(yaw);
				location.add(loc.getDirection().multiply(distance));
			}

			@Override
			protected void face(Location location, float amount) {
				float yaw = Location.normalizeYaw(location.getYaw() - 90);
				location.setYaw(yaw);
			}
		},
		RIGHT {
			@Override
			protected void apply(Location location, double distance) {
				Direction.LEFT.apply(location, -distance);
			}

			@Override
			protected void face(Location location, float amount) {
				Direction.LEFT.face(location, -amount);
			}
		},
		FORWARDS {
			@Override
			protected void apply(Location location, double distance) {
				location.add(location.getDirection().multiply(distance));
			}

			@Override
			protected void face(Location location, float amount) {
			}
		},
		BACKWARDS {
			@Override
			protected void apply(Location location, double distance) {
				Direction.FORWARDS.apply(location, -distance);
			}

			@Override
			protected void face(Location location, float amount) {
				location.setYaw(Location.normalizeYaw(location.getYaw() - 180));
			}
		},
		;

		protected abstract void apply(Location location, double distance);

		protected abstract void face(Location location, float amount);

	}

}
