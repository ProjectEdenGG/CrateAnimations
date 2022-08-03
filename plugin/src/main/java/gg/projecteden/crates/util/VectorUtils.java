package gg.projecteden.crates.util;

import org.bukkit.util.Vector;

import java.util.Random;

public class VectorUtils {

	private static final Random RANDOM = new Random();
	public static final Vector ZERO = new Vector(0, 0, 0);

	public static Vector setLength(Vector vector, double length) {
		if (vector.length() == 0)
			throw new IllegalArgumentException("Cannot normalize a vector with a length of zero");

		return vector.normalize().multiply(length);
	}

	public static Vector randomCone(final Vector vector, double cone) {
		Vector v1 = vector.clone();
		final double length = v1.length();
		v1.add(VectorUtils.random(cone));
		return setLength(v1, length);
	}

	public static Vector random() {
		return random(1);
	}

	public static Vector random(double length) {
		return setLength(new Vector((RANDOM.nextDouble() - .5), (RANDOM.nextDouble() - .5), (RANDOM.nextDouble() - .5)), length);
	}

}
