package com.akon.skriptsecurity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

import java.util.function.Predicate;

public class PacketSecurity extends PacketAdapter {

	public PacketSecurity(Plugin plugin, PacketType... types) {
		super(plugin, types);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		this.checkMundoSK(event.getPacket());
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		this.checkMundoSK(event.getPacket());
	}

	private void checkMundoSK(PacketContainer packet) {
		boolean flag = packet.getType().isServer();
		Predicate<StackTraceElement> predicate;
		if (flag) {
			predicate = ste -> ste.getClassName().equals("com.pie.tlatoani.ProtocolLib.PacketManager") && ste.getMethodName().equals("sendPacket");
		} else {
			predicate = ste -> ste.getClassName().equals("com.pie.tlatoani.ProtocolLib.EffReceivePacket") && ste.getMethodName().equals("execute");
		}
		for (StackTraceElement ste: new Throwable().getStackTrace()) {
			if (predicate.test(ste)) {
				String message = packet.getHandle().getClass().getSimpleName() + "の" + (flag ? "送信" : "受信") + "はできません";
				String warningMessage = String.format(SkriptSecurityMain.TEMPLATE, message);
				SkriptSecurityMain.sendSecurityMessage(warningMessage);
				throw new SecurityException(message);
			}
		}
	}

}
