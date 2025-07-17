package deus.momentum.systems.stamina.network;

public interface INetHandler
{
	void momentum$handleStaminaPacket(PacketSendStamina packet);
	void momentum$handleAddStaminaPacket(PacketAddStamina packet);
}
