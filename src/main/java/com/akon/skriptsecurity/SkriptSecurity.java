package com.akon.skriptsecurity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.net.SocketPermission;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLPermission;
import java.security.Permission;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class SkriptSecurity extends SecurityManager {

	private final HashMultimap<String, URL> allowedPackages = HashMultimap.create();

	public SkriptSecurity(Plugin... plugins) {
		this.allowedPackages.put("net.minecraft.server." + SkriptSecurityMain.getVersion(), Bukkit.class.getProtectionDomain().getCodeSource().getLocation());
		for (Plugin plugin: plugins) {
			if (plugin != null) {
				URL pluginLocation = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
				try {
					File file = new File(pluginLocation.toURI());
					JarInputStream jar = new JarInputStream(new FileInputStream(file));
					JarEntry entry;
					JarEntry lastEntry = null;
					while ((entry = jar.getNextJarEntry()) != null) {
						if (entry.getName().endsWith(".class") && lastEntry != null && lastEntry.getName().endsWith("/")) {
							String pkg = lastEntry.getName();
							this.allowedPackages.put(pkg.substring(0, pkg.length() - 1).replace("/", "."), pluginLocation);
						}
						lastEntry = entry;
					}
				} catch (URISyntaxException | IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	@Override
	public void checkPermission(Permission perm) {
		this.checkSkript(perm);
	}

	@Override
	public void checkPermission(Permission perm, Object context) {
		this.checkSkript(perm);
	}

	private void checkSkript(Permission perm) {
		if ((perm instanceof FilePermission || perm instanceof SocketPermission || perm instanceof URLPermission || (perm instanceof RuntimePermission && (perm.getName().equals("readFileDescriptor") || perm.getName().equals("writeFileDescriptor")))) && Bukkit.getPluginManager().isPluginEnabled("Skript")) {
			Set<Map.Entry<String, Collection<URL>>> entries = this.allowedPackages.asMap().entrySet();
			for (StackTraceElement ste: new Throwable().getStackTrace()) {
				String className = ste.getClassName();
				for (Map.Entry<String, Collection<URL>> entry: entries) {
					if (className.matches(entry.getKey() + "\\.[^.]+")) {
						try {
							if (entry.getValue().contains(Class.forName(className).getProtectionDomain().getCodeSource().getLocation())) {
								return;
							}
						} catch (ClassNotFoundException ex) {
							ex.printStackTrace();
						}
					}
				}
				if (className.equals("ch.njol.skript.lang.TriggerItem") && ste.getMethodName().equals("walk")) {
					StringBuilder message = new StringBuilder();
					if (perm instanceof FilePermission) {
						List<String> actions = Arrays.asList(perm.getActions().split(","));
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
					String warningMessage = String.format(SkriptSecurityMain.TEMPLATE, message);
					SkriptSecurityMain.sendSecurityMessage(warningMessage);
					throw new SecurityException(message.toString());
				}
			}
		}
	}

}
