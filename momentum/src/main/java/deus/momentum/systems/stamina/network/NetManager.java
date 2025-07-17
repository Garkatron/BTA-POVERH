package deus.momentum.systems.stamina.network;

import turniplabs.halplibe.helper.NetworkHelper;

public class NetManager
{
	public void onInitialize()
	{
		NetworkHelper.register(PacketSendStamina.class, true, true);
		NetworkHelper.register(PacketAddStamina.class, false, true);
	}
}
