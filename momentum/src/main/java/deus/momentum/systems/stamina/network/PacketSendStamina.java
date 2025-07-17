package deus.momentum.systems.stamina.network;

import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketSendStamina extends Packet
{
	public float stamina;
	public boolean exhausted;

	public PacketSendStamina()
	{
	}

	@Override
	public void read(DataInputStream dataInputStream) throws IOException {
		stamina = dataInputStream.readFloat();
		exhausted = dataInputStream.readBoolean();
	}

	@Override
	public void write(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeFloat(stamina);
		dataOutputStream.writeBoolean(exhausted);
	}

	@Override
	public void handlePacket(PacketHandler packetHandler) {
		((INetHandler)packetHandler).momentum$handleStaminaPacket(this);
	}

	@Override
	public int getEstimatedSize() {
		return 0;
	}

	public PacketSendStamina(float stamina, boolean exhausted)
	{
		this.stamina = stamina;
		this.exhausted = exhausted;
	}
}
