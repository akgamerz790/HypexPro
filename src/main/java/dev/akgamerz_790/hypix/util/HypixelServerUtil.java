package dev.akgamerz_790.hypix.util;

import java.util.Locale;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public final class HypixelServerUtil {
	private HypixelServerUtil() {
	}

	public static boolean isHypixelServer() {
		MinecraftClient client = MinecraftClient.getInstance();
		ServerInfo entry = client.getCurrentServerEntry();
		if (entry == null || entry.address == null) {
			return false;
		}

		String address = entry.address.toLowerCase(Locale.ROOT);
		return address.contains("hypixel.net") || address.contains("hyixel.net");
	}
}
