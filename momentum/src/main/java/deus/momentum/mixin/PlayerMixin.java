package deus.momentum.mixin;

import com.mojang.nbt.tags.CompoundTag;
import deus.momentum.interfaces.mixin.IPlayerMovementExtra;
import deus.momentum.interfaces.mixin.IPlayerStamina;
import deus.momentum.systems.stamina.StaminaConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.world.World;
import org.checkerframework.checker.units.qual.A;
import org.lwjgl.util.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static deus.momentum.Momentum.*;


@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin extends Mob implements IPlayerStamina, IPlayerMovementExtra {
	@Unique
	private final Minecraft mc = Minecraft.getMinecraft();
	@Shadow
	public Gamemode gamemode;
	// From 0.0f to 100.0f
	@Unique
	public float stamina = 100.0f;
	@Unique
	public float prevStamina = 100.0f;
	@Unique
	public boolean exhausted = false;
	public PlayerMixin(World world) {
		super(world);
	}

	@Unique public boolean isProne = false;
	@Shadow
	public abstract boolean isInWall();

	@Shadow
	protected boolean isDwarf;

	@Inject(method = "hurt", at = @At("TAIL"), remap = false)
	public void hurt(Entity attacker, int damage, DamageType type, CallbackInfoReturnable<Boolean> cir) {

		if (!world.getGameRuleValue(DISABLE_STAMINA) && !world.getGameRuleValue(DISABLE_STAMINA_ON_HURT)) {
			if (cir.getReturnValue()) {
				stamina -= (damage / 2f) * StaminaConstants.staminaLossPerHeart;
				if (stamina < 0) {
					stamina = 0;
					exhausted = true;
				}
			}
		}
		//return hurtSuccess;
	}

	@Inject(method = "onDeath", at = @At("TAIL"), remap = false)
	private void death(Entity entity, CallbackInfo ci) {
		if (world.getGameRuleValue(DISABLE_STAMINA)) return;
		stamina = 100;
		exhausted = false;
	}

	@Inject(method = "jump", at = @At("TAIL"), remap = false)
	public void afterJump(CallbackInfo ci) {
		if (world.getGameRuleValue(DISABLE_STAMINA) || world.getGameRuleValue(DISABLE_STAMINA_ON_JUMP)) return;
		stamina -= 2.5f;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"), remap = false)
	private void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		tag.putFloat("Stamina", stamina);
		tag.putBoolean("Exhausted", exhausted);
		tag.putBoolean("IsProne", isProne);

	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"), remap = false)
	private void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		stamina = tag.getFloat("Stamina");
		exhausted = tag.getBoolean("Exhausted");
		isProne = tag.getBoolean("IsProne");
		if (isProne) {
			setSharedFlag(5, true);
		}
	}

	@Inject(method = "onLivingUpdate()V", at = @At("HEAD"), remap = false)
	private void playerTick(CallbackInfo ci) {
		updateStamina();

	}

	@Inject(method = "tick", at = @At("HEAD"), remap = false)
	private void tickM(CallbackInfo ci) {
		updateGoProne();
	}

	@Inject(method = "resetPos", at = @At("TAIL"), remap = false)
	private void mResetPos(CallbackInfo ci) {
		updateGoProne();
	}

	@Unique
	private void updateGoProne() {
		if (momentum$isProne() && !this.isDwarf && !isProne && !isInWater() && !isPlayerSleeping()) {
			proneSizes();
		} else if (!momentum$isProne() && isProne) {
			if (!canStandUp()) {
				proneSizes(); // Stay prone if can't stand
			} else {
				standUp();
			}
		}
		this.isProne = momentum$isProne();
	}

	@Unique void proneSizes() {
		this.setSize(0.6F, 0.1F);
		this.heightOffset = 0.62F;
		this.setPos(this.x, this.y - 1.0, this.z);
	}

	@Inject(method = "getHeightOffset", at = @At("TAIL"), cancellable = true, remap = false)
	private void adjustHeightOffset(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(this.isDwarf || isProne ? 0.31F : 0.81F);
	}


	@Unique
	void updateStamina() {
		prevStamina = stamina;
		if (!this.gamemode.equals(Gamemode.creative) && !world.getGameRuleValue(DISABLE_STAMINA)) {
			if (!exhausted) {
				if (this.isSprinting() && !world.getGameRuleValue(DISABLE_STAMINA_ON_SPRITING)) {
					stamina -= StaminaConstants.exhaustionSpeed / 20f;
				} else {
					recoverStamina();
				}
			} else {
				// We're exhausted, only recover stamina.
				recoverStamina();
			}

			if (stamina <= 0) {
				stamina = 0;
				exhausted = true;
			}

			if (exhausted) {
				this.setSprinting(false);
			}
		}
	}

	@Unique
	private void recoverStamina() {
		stamina += StaminaConstants.exhaustionRecoverySpeed / 20f;
		if (stamina >= 100) {
			stamina = 100;
			exhausted = false;
		}
	}


	@Unique
	private void standUp() {
		this.setSize(0.6F, 1.8F);
		this.heightOffset = 1.62F;
		this.setPos(this.x, this.y + 1.0, this.z);
	}

	@Unique
	private boolean canStandUp() {
		// Check if player can fit in default size (0.6F, 1.8F)
		AABB standingBB = AABB.getTemporaryBB(
			this.x - 0.3F, this.y, this.z - 0.3F,
			this.x + 0.3F, this.y + 1.8F, this.z + 0.3F
		);
		return this.world.getCubes(this, standingBB).isEmpty();
	}

	@Override
	public boolean momentum$isProne() {
		return getSharedFlag(5);
	}

	@Override
	public void momentum$goProne() {
		setSharedFlag(5, true);
	}




	@Override
	@Unique
	public float momentum$getStamina() {
		return stamina;
	}

	@Override
	@Unique
	public void momentum$setStamina(float stamina) {
		this.stamina = stamina;
	}

	@Override
	public boolean momentum$isExhausted() {
		return exhausted;
	}

	@Override
	public void momentum$setExhausted(boolean exhausted) {
		this.exhausted = exhausted;
	}

	@Override
	public float momentum$getPrevStamina() {
		return prevStamina;
	}

	@Override
	public void momentum$setSprintingOnAir() {
		setSharedFlag(4, true);
	}

	@Override
	public boolean momentum$isSprintingOnAir() {
		return getSharedFlag(4);
	}

	@Override
	public boolean momentum$spendStamina(float amount) {
		if (world.getGameRuleValue(DISABLE_STAMINA)) return false;

		if (amount <= 0.0f) {
			stamina = 0;
			return true;
		}

		if (momentum$isExhausted()) {
			return false;
		}

		stamina -= amount;
		return true;
	}




}
