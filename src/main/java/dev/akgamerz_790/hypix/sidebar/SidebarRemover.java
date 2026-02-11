package dev.akgamerz_790.hypix.sidebar;

import dev.akgamerz_790.hypix.util.HypixelServerUtil;

public final class SidebarRemover {
	private static boolean enabled = true;

	private SidebarRemover() {
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean value) {
		enabled = value;
	}

	public static boolean toggle() {
		enabled = !enabled;
		return enabled;
	}

	public static boolean shouldRemoveSidebar() {
		return enabled && HypixelServerUtil.isHypixelServer();
	}
}
