package gg.projecteden.crates.api.models;

import java.util.concurrent.CompletableFuture;

public interface CrateAnimation {

	CompletableFuture<Void> play();

	void stop();

	boolean isActive();

	void reset();

}
