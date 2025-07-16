package deus.momentum.interfaces.mixin;

public interface IPlayerStamina
{
	float getStamina();
	void setStamina(float stamina);
	boolean isExhausted();
	void setExhausted(boolean exhausted);
	float getPrevStamina();
}
