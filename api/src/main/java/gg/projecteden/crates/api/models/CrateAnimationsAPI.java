package gg.projecteden.crates.api.models;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface CrateAnimationsAPI {

	@Nullable CrateAnimation getAnimation(String type, ArmorStand entity, BiFunction<Location, Consumer<Item>, Item> itemSpawnHook);

}

