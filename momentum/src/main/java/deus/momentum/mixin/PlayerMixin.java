package deus.momentum.mixin;

import com.mojang.nbt.tags.CompoundTag;
import deus.momentum.interfaces.mixin.IPlayerMovementExtra;
import deus.momentum.interfaces.mixin.IPlayerStamina;
import deus.momentum.systems.stamina.StaminaConstants;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static deus.momentum.Momentum.*;


@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin extends Mob implements IPlayerStamina, IPlayerMovementExtra
{
	@Shadow
	public Gamemode gamemode;

	@Shadow
	public abstract boolean isInWall();

	// From 0.0f to 100.0f
	@Unique
	public float stamina = 100.0f;
	@Unique
	public float prevStamina = 100.0f;
	@Unique
	public boolean exhausted = false;

	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type)
	{
		boolean hurtSuccess = super.hurt(attacker, damage, type);

		if (!world.getGameRuleValue(DISABLE_STAMINA) && !world.getGameRuleValue(DISABLE_STAMINA_ON_HURT)) {
			if (hurtSuccess)
			{
				stamina -= (damage / 2f) * StaminaConstants.staminaLossPerHeart;
				if (stamina < 0)
				{
					stamina = 0;
					exhausted = true;
				}
			}
		}
		return hurtSuccess;
	}

	@Inject(method = "onDeath", at = @At("TAIL"), remap = false)
	private void death(Entity entity, CallbackInfo ci)
	{
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
	private void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci)
	{
		tag.putFloat("Stamina", stamina);
		tag.putBoolean("Exhausted", exhausted);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"), remap = false)
	private void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci)
	{
		stamina = tag.getFloat("Stamina");
		exhausted = tag.getBoolean("Exhausted");
	}

	@Inject(method = "onLivingUpdate()V", at = @At("HEAD"), remap = false)
	private void playerTick(CallbackInfo ci)
	{
		prevStamina = stamina;
		if (!this.gamemode.equals(Gamemode.creative) && !world.getGameRuleValue(DISABLE_STAMINA))
		{
			if (!exhausted)
			{
				if (this.isSprinting() && !world.getGameRuleValue(DISABLE_STAMINA_ON_SPRITING))
				{
					stamina -= StaminaConstants.exhaustionSpeed / 20f;
				}
				else
				{
					recoverStamina();
				}
			}
			else
			{
				// We're exhausted, only recover stamina.
				recoverStamina();
			}

			if (stamina <= 0)
			{
				stamina = 0;
				exhausted = true;
			}

			if (exhausted)
			{
				this.setSprinting(false);
			}
		}
	}

	@Unique
	private void recoverStamina()
	{
		stamina += StaminaConstants.exhaustionRecoverySpeed / 20f;
		if (stamina >= 100)
		{
			stamina = 100;
			exhausted = false;
		}
	}

	public PlayerMixin(World world)
	{
		super(world);
	}

	@Override
	@Unique
	public float momentum$getStamina()
	{
		return stamina;
	}

	@Override
	@Unique
	public void momentum$setStamina(float stamina)
	{
		this.stamina = stamina;
	}

	@Override
	public boolean momentum$isExhausted()
	{
		return exhausted;
	}

	@Override
	public void momentum$setExhausted(boolean exhausted)
	{
		this.exhausted = exhausted;
	}

	@Override
	public float momentum$getPrevStamina()
	{
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
