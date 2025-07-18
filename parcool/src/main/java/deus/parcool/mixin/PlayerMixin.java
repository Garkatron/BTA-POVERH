package deus.parcool.mixin;

import deus.momentum.interfaces.mixin.IPlayerMovementExtra;
import deus.momentum.interfaces.mixin.IPlayerStamina;
import deus.momentum.utils.TicksTimer;
import deus.parcool.interfaces.IPlayerParcool;
import net.minecraft.client.Minecraft;
import net.minecraft.core.achievement.stat.Stat;
import net.minecraft.core.achievement.stat.StatList;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin extends Mob implements IPlayerParcool, IPlayerMovementExtra, IPlayerStamina {

	// === Fields ===
	@Shadow public abstract void addStat(Stat statbase, int i);

	@Shadow
	protected abstract void jump();

	@Unique private final Minecraft mc = Minecraft.getMinecraft();

	@Unique private boolean wasSpriting = false;
	@Unique private final TicksTimer spritingTimer = new TicksTimer(() -> wasSpriting = false, 12);

	@Unique private final int ticksDelayWallJump = 10;
	@Unique private int ticksRemainingWallJump = 0;
	@Unique private Vector3f lastWallJumpPos = null;
	@Unique private boolean canWallJump = true;

	@Unique private int maxJumps = 1;
	@Unique private int remainingJumps = maxJumps;

	// === Constructor ===
	public PlayerMixin(@Nullable World world) {
		super(world);
	}


	// === Tick Update ===

	@Inject(method = "onLivingUpdate()V", at = @At("HEAD"), remap = false)
	private void onTick(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		updateWallJump(player);
		updateWallClimb(player);
		updateWallSliding(player);
	}

	// === Wall Climb ===

	@Unique
	private void updateWallClimb(Player player) {
		if (!momentum$isExhausted() && parcool$isCollidingWithWall() && mc.gameSettings.keyInteract.isPressed()) {
			player.yd = Math.max(player.yd, 0);
			player.xd = 0;
			player.zd = 0;
			momentum$spendStamina(3.5f);
		}
	}

	// === Wall Slide ===

	@Unique
	private void updateWallSliding(Player player) {
		if (!momentum$isExhausted() && parcool$isWallSliding()) {
			player.fallDistance = 0;
			player.yd = Math.max(player.yd, -0.3);
			player.xd *= 1.06;
			player.zd *= 1.06;
			momentum$spendStamina(0.6f);
		} else if (!momentum$isExhausted() && isSneaking() && parcool$isCollidingWithWall()) {
			player.fallDistance = 0;
			player.yd = Math.max(player.yd, -0.05);
			player.xd = 0;
			player.zd = 0;
			momentum$spendStamina(0.8f);
		}
	}

	// === Wall Jump ===

	@Unique
	private void updateWallJump(Player player) {
		if (momentum$isExhausted()) return;

		spritingTimer.update();

		Vector3f wallPos = new Vector3f();
		if (ticksRemainingWallJump > 0) ticksRemainingWallJump--;

		boolean wall = parcool$checkWallCollision(player, player.world, player.bbWidth + 0.75f, wallPos);

		if (!wall) {
			lastWallJumpPos = null;
			canWallJump = true;
		}

		if (momentum$isSprintingOnAir()) {
			wasSpriting = true;
			spritingTimer.restart();
		}

		if (mc.thePlayer.input.jump && wall && !player.noPhysics && ticksRemainingWallJump <= 0 && wasSpriting) {
			if (canWallJump && !isSameWall(wallPos, lastWallJumpPos)) {
				performWallJump(player, wallPos);
				ticksRemainingWallJump = ticksDelayWallJump;
				canWallJump = false;
				lastWallJumpPos = new Vector3f(wallPos);
				momentum$spendStamina(8);
			}
		}
	}

	@Unique
	private boolean isSameWall(Vector3f a, Vector3f b) {
		if (a == null || b == null) return false;
		float dx = a.x - b.x, dy = a.y - b.y, dz = a.z - b.z;
		return dx * dx + dy * dy + dz * dz < 0.01f * 0.01f;
	}

	@Unique
	private void performWallJump(Player player, Vector3f wallPos) {
		float yawRad = player.yRot * (float)Math.PI / 180F;

		Vector3f forward = new Vector3f(-MathHelper.sin(yawRad), 0.44f, MathHelper.cos(yawRad));
		Vector3f right = new Vector3f(forward.z, 0, -forward.x);

		Vector3f toWall = new Vector3f(wallPos.x - (float)player.x, 0, wallPos.z - (float)player.z);
		float len = (float)Math.sqrt(toWall.x * toWall.x + toWall.z * toWall.z);
		if (len != 0) {
			toWall.x /= len;
			toWall.z /= len;
		}

		float dot = right.x * toWall.x + right.z * toWall.z;
		float lateralPush = 0.25f;

		Vector3f lateral = new Vector3f(
			right.x * (dot > 0 ? -lateralPush : lateralPush),
			0,
			right.z * (dot > 0 ? -lateralPush : lateralPush)
		);

		player.fallDistance = 0;
		player.yd = forward.y;
		player.xd = forward.x * 0.42 + lateral.x;
		player.zd = forward.z * 0.42 + lateral.z;
	}

	// === IPlayerParcool Implementation ===

	@Override
	public boolean parcool$isCollidingWithWall() {
		Player player = (Player)(Object)this;
		if (player.world == null) return false;
		return parcool$checkWallCollision(player, player.world, player.bbWidth + 0.8f, null);
	}

	@Override
	public Vector3f parcool$getWallPosition() {
		Player player = (Player)(Object)this;
		if (player.world == null) return new Vector3f(0, 0, 0);
		Vector3f result = new Vector3f(0, 0, 0);
		return parcool$checkWallCollision(player, player.world, player.bbWidth + 0.8f, result) ? result : new Vector3f(0, 0, 0);
	}

	@Override
	public boolean parcool$checkWallCollision(Player player, World world, float bbWidth, Vector3f outPos) {
		for (int i = 0; i < 8; ++i) {
			float f = ((i & 1) - 0.5F) * bbWidth * 0.9F;
			float f1 = (((i >> 1) & 1) - 0.5F) * 0.1F;
			float f2 = (((i >> 2) & 1) - 0.5F) * bbWidth * 0.9F;

			int x = MathHelper.floor(player.x + f);
			int y = MathHelper.floor(player.y + player.getHeadHeight() + f1);
			int z = MathHelper.floor(player.z + f2);

			if (world.isBlockNormalCube(x, y, z)) {
				if (outPos != null) outPos.set(x, y, z);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean parcool$isWallSliding() {
		return momentum$isSprintingOnAir() && parcool$isCollidingWithWall();
	}
}
