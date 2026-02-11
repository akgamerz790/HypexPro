package dev.akgamerz_790.hypix;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import dev.akgamerz_790.hypix.sidebar.SidebarRemover;

public class HypixelXClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
			literal("scb")
				.executes(context -> {
					boolean enabled = SidebarRemover.toggle();
					sendFeedback(enabled);
					return 1;
				})
				.then(literal("toggle").executes(context -> {
					boolean enabled = SidebarRemover.toggle();
					sendFeedback(enabled);
					return 1;
				}))
				.then(literal("on").executes(context -> {
					SidebarRemover.setEnabled(true);
					sendFeedback(true);
					return 1;
				}))
				.then(literal("off").executes(context -> {
					SidebarRemover.setEnabled(false);
					sendFeedback(false);
					return 1;
				}))
				.then(literal("status").executes(context -> {
					sendFeedback(SidebarRemover.isEnabled());
					return 1;
				}))
		));
	}

	private static void sendFeedback(boolean enabled) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) {
			return;
		}

		String state = enabled ? "ON" : "OFF";
		client.player.sendMessage(Text.literal("[HypixelX] Sidebar remover: " + state), false);
	}
}
