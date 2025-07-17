package deus.momentum.utils;

public class TicksTimer {

	private int delayTicks = 20; // default 1 second
	private int remainingTicks;
	private Runnable callback = () -> {};
	private boolean shouldUpdate = false;

	public boolean autostart = false;

	// === Constructors ===

	public TicksTimer(Runnable callback, int delayTicks) {
		this.callback = callback;
		this.delayTicks = delayTicks;
		this.remainingTicks = delayTicks;
		if (autostart) start();
	}

	public TicksTimer(Runnable callback) {
		this(callback, 20);
	}

	public TicksTimer(int delayTicks) {
		this(() -> {}, delayTicks);
	}

	// === Fluent config ===

	public TicksTimer onTimeout(Runnable callback) {
		this.callback = callback;
		return this;
	}

	// === Timer control ===

	public void start() {
		shouldUpdate = true;
		remainingTicks = delayTicks;
	}

	public void restart() {
		remainingTicks = delayTicks;
		shouldUpdate = true;
	}

	public void reset() {
		shouldUpdate = false;
		remainingTicks = delayTicks;
	}

	public void stop() {
		shouldUpdate = false;
	}

	public boolean isRunning() {
		return shouldUpdate && remainingTicks > 0;
	}

	// === Main update logic ===

	public void update() {
		if (shouldUpdate && remainingTicks > 0) {
			remainingTicks--;
			if (remainingTicks == 0) {
				callback.run();
				reset();
				if (autostart) {
					restart();
				}
			}
		}
	}

	// === Getters / Setters ===

	public int getDelayTicks() {
		return delayTicks;
	}

	public void setDelayTicks(int delayTicks) {
		this.delayTicks = delayTicks;
	}

	public int getRemainingTicks() {
		return remainingTicks;
	}
}
