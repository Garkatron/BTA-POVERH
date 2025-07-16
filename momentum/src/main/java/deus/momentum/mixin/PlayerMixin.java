package deus.momentum.mixin;

import com.mojang.nbt.tags.CompoundTag;
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

@Mixin(value = Player.class, remap = false)
public class PlayerMixin extends Mob implements IPlayerStamina
{
	@Shadow
	public Gamemode gamemode;
	// From 0.0f to 100.0f
	public float stamina = 100.0f;
	public float prevStamina = 100.0f;
	public boolean exhausted = false;

	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type)
	{
		boolean hurtSuccess = super.hurt(attacker, damage, type);

		if (hurtSuccess)
		{
			stamina -= (damage / 2f) * StaminaConstants.staminaLossPerHeart;
			if (stamina < 0)
			{
				stamina = 0;
				exhausted = true;
			}
		}

		return hurtSuccess;
	}

	@Inject(method = "onDeath", at = @At("TAIL"), remap = false)
	private void death(Entity entity, CallbackInfo ci)
	{
		stamina = 100;
		exhausted = false;
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
		if (!this.gamemode.equals(Gamemode.creative))
		{
			if (!exhausted)
			{
				if (this.isSprinting())
				{
					stamina -= StaminaConstants.exhaustionSpeed / 20f;

					if (stamina <= 0)
					{
						stamina = 0;
						exhausted = true;
					}
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
	public float getStamina()
	{
		return stamina;
	}

	@Override
	@Unique
	public void setStamina(float stamina)
	{
		this.stamina = stamina;
	}

	@Override
	public boolean isExhausted()
	{
		return exhausted;
	}

	@Override
	public void setExhausted(boolean exhausted)
	{
		this.exhausted = exhausted;
	}

	@Override
	public float getPrevStamina()
	{
		return prevStamina;
	}
}
