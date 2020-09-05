package com.akon.skriptsecurity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import java.util.function.Predicate;

public class PacketSecurity extends PacketAdapter {

	public PacketSecurity(PacketType... types) {
		super(SkriptSecurityMain.getInstance(), ListenerPriority.MONITOR, types);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		this.checkMundoSK(event);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		this.checkMundoSK(event);
	}

	private void checkMundoSK(PacketEvent event) {
		if (!event.isCancelled()) {
			boolean flag = event.getPacket().getType().isServer();
			Predicate<StackTraceElement> predicate;
			if (flag) {
				predicate = ste -> ste.getClassName().equals("com.pie.tlatoani.ProtocolLib.PacketManager") && ste.getMethodName().equals("sendPacket");
			} else {
				predicate = ste -> ste.getClassName().equals("com.pie.tlatoani.ProtocolLib.EffReceivePacket") && ste.getMethodName().equals("execute");
			}
			for (StackTraceElement ste : new Throwable().getStackTrace()) {
				if (predicate.test(ste)) {
					String message = event.getPacket().getHandle().getClass().getSimpleName() + "の" + (flag ? "送信" : "受信") + "はできません";
					String warningMessage = String.format(SkriptSecurityMain.TEMPLATE, message);
					SkriptSecurityMain.sendSecurityMessage(warningMessage);
					event.setCancelled(true);
				}
			}
		}
	}

}
