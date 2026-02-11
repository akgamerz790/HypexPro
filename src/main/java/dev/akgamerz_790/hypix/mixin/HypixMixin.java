package dev.akgamerz_790.hypix.mixin;

import java.util.Collection;
import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(InGameHud.class)
public class HypixMixin {
	private static final String DISASTER_PREFIX = "disaster:";
	private static final String UNKNOWN_DISASTER = "Unknown";

	@Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
	private void renderDisasterOverlay(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
		if (!isHypixelServer()) {
			return;
		}
		if (objective == null) {
			return;
		}

		String disaster = extractDisaster(objective);
		if (disaster == null) {
			disaster = UNKNOWN_DISASTER;
		}

		Text title = Text.literal("Current Disaster").formatted(Formatting.GOLD, Formatting.BOLD);
		Text value = Text.literal(disaster).formatted(Formatting.YELLOW);

		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, title, 8, 8, 0xFFFFFF);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, value, 8, 20, 0xFFFFFF);
		ci.cancel();
	}

	private boolean isHypixelServer() {
		MinecraftClient client = MinecraftClient.getInstance();
		ServerInfo entry = client.getCurrentServerEntry();
		if (entry == null || entry.address == null) {
			return false;
		}

		String address = entry.address.toLowerCase(Locale.ROOT);
		return address.contains("hypixel.net") || address.contains("hyixel.net");
	}

	private String extractDisaster(ScoreboardObjective objective) {
		Scoreboard scoreboard = objective.getScoreboard();
		Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);

		for (ScoreboardEntry entry : entries) {
			String line = entry.name().getString().trim();
			String lower = line.toLowerCase(Locale.ROOT);
			if (lower.startsWith(DISASTER_PREFIX)) {
				String disaster = line.substring(DISASTER_PREFIX.length()).trim();
				return disaster.isEmpty() ? UNKNOWN_DISASTER : disaster;
			}
		}

		return null;
	}
}
