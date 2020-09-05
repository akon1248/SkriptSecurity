package com.akon.skriptsecurity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkriptSecurityMain extends JavaPlugin {

	@Getter
	private static String version;
	@Getter
	private static SkriptSecurityMain instance;
	private static boolean packetListener = false;
	public static final String TEMPLATE = "SkriptSecurityによってSkriptがブロックされました: %s";

	@Override
	public void onEnable() {
		instance = this;
		version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		Bukkit.getScheduler().runTask(this, () -> {
			System.setSecurityManager(new SkriptSecurity(Bukkit.getPluginManager().getPlugin("ProtocolLib")));
			this.getLogger().info("SkriptSecurity has been initialized.");
			if (Bukkit.getPluginManager().isPluginEnabled("MundoSK") && Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
				ProtocolLibrary.getProtocolManager().addPacketListener(new PacketSecurity(PacketType.Play.Client.WINDOW_CLICK));
				this.getLogger().info("PacketSecurity for MundoSK has been initialized.");
				packetListener = true;
			}
		});
	}

	@Override
	public void onDisable() {
		System.setSecurityManager(null);
		if (packetListener) {
			ProtocolLibrary.getProtocolManager().removePacketListeners(this);
		}
	}

	public static void sendSecurityMessage(String msg) {
		Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("skriptsecurity.admin")).forEach(player -> player.sendMessage(ChatColor.RED + msg));
		instance.getLogger().warning(msg);
	}

}
