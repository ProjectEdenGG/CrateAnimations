package gg.projecteden.crates.api;

import gg.projecteden.crates.api.models.CrateAnimation;
import gg.projecteden.crates.api.models.CrateAnimationType;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface CrateAnimationsAPI {

	@Nullable CrateAnimation getAnimation(CrateAnimationType type, ArmorStand entity, BiFunction<Location, Consumer<Item>, Item> itemSpawnHook);

}

