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
			StackTraceElement stack = Thread.currentThread().getStackTrace()[4];
			String callerClassName = stack.getClassName();
			boolean isNMS = callerClassName.startsWith("net.minecraft.server." + SkriptSecurityMain.getVersion());
			boolean isTriggerItem = callerClassName.equals("ch.njol.skript.lang.TriggerItem");
			boolean isWalkMethod = stack.getMethodName().equals("walk");
			if(!isNMS && isTriggerItem && isWalkMethod){
			StringBuilder message = new StringBuilder();
			if (perm instanceof FilePermission) {
				String rawActions = perm.getActions();
				ArrayList<String> actions = Lists.newArrayList();
				if (rawActions.contains("delete")) {
					actions.add("削除");
				}
				if (rawActions.contains("execute")) {
					actions.add("実行");
				}
				if (rawActions.contains("read")) {
					actions.add("読み取り");
				}
				if (rawActions.contains("write")) {
					actions.add("書き込み");
				}
				if (rawActions.contains("readlink")) {
					actions.add("リンクの読み取り");
				}
				String combinedActions = String.join("/", actions.toArray(new String[0]));
				message.append(String.format("ファイルの%sはできません (%s)", combinedActions, perm.getName()));
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
