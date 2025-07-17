package deus.parcool.mixin;

import deus.momentum.interfaces.mixin.IPlayerMovementExtra;
import deus.momentum.interfaces.mixin.IPlayerStamina;
import deus.momentum.utils.TicksTimer;
import deus.parcool.interfaces.IPlayerParcool;
import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin extends Mob implements IPlayerParcool, IPlayerMovementExtra, IPlayerStamina {
	@Unique private boolean wasSpriting = false;
	@Unique private TicksTimer spritingTimer = (new TicksTimer(()->{wasSpriting=false;},12)); // Remaining ticks until next wall jump

	@Unique private final int ticksDelayWallJump = 10; // Cooldown duration (1 second)
	@Unique private int ticksRemainingWallJump = 0; // Remaining ticks until next wall jump
	@Unique private Vector3f lastWallJumpPos = null;
	@Unique private boolean canWallJump = true;

	public PlayerMixin(@Nullable World world) {
		super(world);
	}


	@Inject(method = "onLivingUpdate()V", at = @At("HEAD"), remap = false)
	private void playerTick(CallbackInfo ci) {
		spritingTimer.update();

		System.out.println(wasSpriting);

		Player player = (Player)(Object)this;
		Vector3f wallPos = new Vector3f();

		// Decrement wall jump cooldown
		if (ticksRemainingWallJump > 0) {
			ticksRemainingWallJump--;
		}

		// Verifica si hay una pared
		boolean wall = parcool$checkWallCollision(player, player.world, player.bbWidth + 0.75f, wallPos);

		// Si el jugador ya no está tocando una pared, limpiar la última
		if (!wall) {
			lastWallJumpPos = null;
			canWallJump = true;
		}

		if (momentum$isSprintingOnAir()) {
			wasSpriting = true;
			spritingTimer.restart();
		}

		if (Minecraft.getMinecraft().thePlayer.input.jump && wall && !player.noPhysics && ticksRemainingWallJump <= 0 && wasSpriting) {
			if (canWallJump && !isSameWall(wallPos, lastWallJumpPos)) {
				performWallJump(player, wallPos);
				ticksRemainingWallJump = ticksDelayWallJump;
				canWallJump = false;
				lastWallJumpPos = new Vector3f(wallPos);
				momentum$setStamina(momentum$getStamina() - 8);
			}
		}
	}

	@Unique
	private boolean isSameWall(Vector3f a, Vector3f b) {
		if (a == null || b == null) return false;
		float dx = a.x - b.x;
		float dy = a.y - b.y;
		float dz = a.z - b.z;
		float threshold = 0.01f;
		return dx * dx + dy * dy + dz * dz < threshold * threshold;
	}


	@Inject(method = "jump", at = @At("HEAD"), cancellable = true, remap = false)
	protected void jump(CallbackInfo ci) {
		ci.cancel();
		Player player = (Player)(Object)this;

		if (!player.noPhysics) {
			// Regular jump when on ground
			player.yd = 0.42;
			if (player.isSprinting()) {
				float f = player.yRot * 0.01745329F;
				player.xd -= (double)(MathHelper.sin(f) * 0.2F);
				player.zd += (double)(MathHelper.cos(f) * 0.2F);
			}
		}
	}



	@Unique
	private void performWallJump(Player player, Vector3f wallPos) {
		float yawRad = player.yRot * (float)Math.PI / 180F;

		// Forward
		float forwardX = -MathHelper.sin(yawRad);
		float forwardZ = MathHelper.cos(yawRad);
		Vector3f forward = new Vector3f(forwardX, 0.52f, forwardZ);

		// - wall direction
		Vector3f right = new Vector3f(forwardZ, 0, -forwardX);

		// Vector to wall

		Vector3f toWall = new Vector3f(
			wallPos.x - (float)player.x,
			0,
			wallPos.z - (float)player.z
		);
		// Normalize

		float len = (float)Math.sqrt(toWall.x * toWall.x + toWall.z * toWall.z);
		if (len != 0) {
			toWall.x /= len;
			toWall.z /= len;
		}

		// Wall side
		float dot = right.x * toWall.x + right.z * toWall.z;
		float lateralPush = 0.25f;

		Vector3f lateral = new Vector3f(
			right.x * (dot > 0 ? -lateralPush : lateralPush),
			0,
			right.z * (dot > 0 ? -lateralPush : lateralPush)
		);

		// Apply jump
		player.fallDistance = 0;

		player.yd = forward.y;
		player.xd = forward.x * 0.42 + lateral.x;
		player.zd = forward.z * 0.42 + lateral.z;
	}

	@Override
	public boolean parcool$canWallJump() {
		Player player = (Player)(Object)this;
		if (player.world == null) {
			return false;
		}
		return parcool$checkWallCollision(player, player.world, player.bbWidth + 0.8f, null);
	}

	@Override
	public Vector3f parcool$getWallPosition() {
		Player player = (Player)(Object)this;
		if (player.world == null) {
			return new Vector3f(0, 0, 0);
		}
		Vector3f result = new Vector3f(0, 0, 0);
		if (parcool$checkWallCollision(player, player.world, player.bbWidth + 0.8f, result)) {
			return result;
		}
		return new Vector3f(0, 0, 0);
	}

	@Override
	public boolean parcool$checkWallCollision(Player player, World world, float bbWidth, Vector3f outPosition) {
		for (int i = 0; i < 8; ++i) {
			float f = ((float)((i & 1)) - 0.5F) * bbWidth * 0.9F;
			float f1 = ((float)((i >> 1) & 1) - 0.5F) * 0.1F;
			float f2 = ((float)((i >> 2) & 1) - 0.5F) * bbWidth * 0.9F;
			int x = MathHelper.floor(player.x + f);
			int y = MathHelper.floor(player.y + player.getHeadHeight() + f1);
			int z = MathHelper.floor(player.z + f2);
			if (world.isBlockNormalCube(x, y, z)) {
				if (outPosition != null) {
					outPosition.set(x, y, z);
				}
				return true;
			}
		}
		return false;
	}


}
