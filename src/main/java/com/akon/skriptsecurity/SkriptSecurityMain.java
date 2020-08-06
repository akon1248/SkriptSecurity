package com.akon.skriptsecurity;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkriptSecurityMain extends JavaPlugin {

	private static String version;
	private static SkriptSecurityMain instance;

	@Override
	public void onEnable() {
		instance = this;
		version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		Bukkit.getScheduler().runTask(this, () -> {
			System.setSecurityManager(new SkriptSecurity());
			this.getLogger().info("SkriptSecurity has been initialized.");
		});
	}

	@Override
	public void onDisable() {
		System.setSecurityManager(null);
	}

	public static String getVersion() {
		return version;
	}

	public static SkriptSecurityMain getInstance() {
		return instance;
	}

}
