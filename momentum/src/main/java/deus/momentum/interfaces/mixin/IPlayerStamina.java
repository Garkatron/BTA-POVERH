package deus.momentum.interfaces.mixin;

public interface IPlayerStamina
{
	float momentum$getStamina();
	void momentum$setStamina(float stamina);
	boolean momentum$isExhausted();
	void momentum$setExhausted(boolean exhausted);
	float momentum$getPrevStamina();
}
