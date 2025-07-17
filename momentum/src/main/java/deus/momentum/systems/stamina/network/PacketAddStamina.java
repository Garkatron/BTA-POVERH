package deus.momentum.systems.stamina.network;

import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketAddStamina extends Packet
{
	public float stamina;

	public PacketAddStamina()
	{
	}

	@Override
	public void read(DataInputStream dataInputStream) throws IOException {
		stamina = dataInputStream.readFloat();

	}

	@Override
	public void write(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeFloat(stamina);
	}

	@Override
	public void handlePacket(PacketHandler packetHandler) {
		((INetHandler)packetHandler).momentum$handleAddStaminaPacket(this);
	}

	@Override
	public int getEstimatedSize() {
		return 0;
	}

	public PacketAddStamina(float stamina)
	{
		this.stamina = stamina;
	}





}
