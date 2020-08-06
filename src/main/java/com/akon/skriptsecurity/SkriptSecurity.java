package com.akon.skriptsecurity;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.FilePermission;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLPermission;
import java.security.Permission;
import java.util.ArrayList;

public class SkriptSecurity extends SecurityManager {

	private static final URL SERVER_JAR_LOCATION = Bukkit.class.getProtectionDomain().getCodeSource().getLocation();

	@Override
	public void checkPermission(Permission perm) {
		checkSkript(perm);
	}

	@Override
	public void checkPermission(Permission perm, Object context) {
		checkSkript(perm);
	}

	private void checkSkript(Permission perm) {
		if ((perm instanceof FilePermission || perm instanceof SocketPermission || perm instanceof URLPermission || (perm instanceof RuntimePermission && (perm.getName().equals("readFileDescriptor") || perm.getName().equals("writeFileDescriptor")))) && Bukkit.getPluginManager().isPluginEnabled("Skript")) {
			for (StackTraceElement ste : new Throwable().getStackTrace()) {
				String className = ste.getClassName();
				if (className.startsWith("net.minecraft.server." + SkriptSecurityMain.getVersion())) {
					try {
						Class<?> clazz = Class.forName(className);
						if (clazz.getProtectionDomain().getCodeSource().getLocation().equals(SERVER_JAR_LOCATION)) {
							return;
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				} else if (className.equals("ch.njol.skript.lang.TriggerItem") && ste.getMethodName().equals("walk")) {
					StringBuilder message = new StringBuilder();
					if (perm instanceof FilePermission) {
						String actions = perm.getActions();
						ArrayList<String> actionTexts = Lists.newArrayList();
						if (actions.contains("delete")) {
							actionTexts.add("削除");
						}
						if (actions.contains("execute")) {
							actionTexts.add("実行");
						}
						if (actions.contains("read")) {
							actionTexts.add("読み取り");
						}
						if (actions.contains("write")) {
							actionTexts.add("書き込み");
						}
						if (actions.contains("readlink")) {
							actionTexts.add("リンクの読み取り");
						}
						message.append("ファイルの").append(String.join("/", actionTexts.toArray(new String[0]))).append("はできません (").append(perm.getName()).append(")");
					} else if (perm instanceof SocketPermission || perm instanceof URLPermission) {
						message.append(perm.getName()).append("との通信はできません");
					} else if (perm.getName().equals("readFileDescriptor")) {
						message.append("ファイルの読み取りはできません");
					} else if (perm.getName().equals("writeFileDescriptor")) {
						message.append("ファイルの書き込みはできません");
					}
					String warningMessage = "SkriptSecurityによってSkriptがブロックされました: " + message.toString();
					Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("skriptsecurity.admin")).forEach(player -> player.sendMessage(ChatColor.RED + warningMessage));
					SkriptSecurityMain.getInstance().getLogger().warning(warningMessage);
					throw new SecurityException(message.toString());
				}
			}
		}
	}

}
