package gg.projecteden.crates.util;

import org.bukkit.Location;
import org.bukkit.Sound;

public class SoundUtils {

	public static void playSound(Location loc, Sound sound, float volume, float pitch) {
		loc.getWorld().playSound(loc, sound, volume, pitch);
	}

	public static void playSound(Location loc, String sound, float volume, float pitch) {
		loc.getWorld().playSound(loc, sound, volume, pitch);
	}

	public static float getPitch(int step) {
		return (float) Math.pow(2, ((-12 + step) / 12f));
	}

}
