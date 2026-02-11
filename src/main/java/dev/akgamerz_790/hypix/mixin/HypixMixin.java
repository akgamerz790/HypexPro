package dev.akgamerz_790.hypix.mixin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.akgamerz_790.hypix.disaster.DisasterTracker;
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
	private static final Pattern DISASTER_PATTERN = Pattern.compile("disaster\\s*[:\\-]?\\s*(.+)", Pattern.CASE_INSENSITIVE);
	private static final Map<String, String> KNOWN_DISASTERS = createKnownDisasters();

	@Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
	private void renderDisasterOverlay(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
		if (!isHypixelServer()) {
			return;
		}
		if (objective == null) {
			return;
		}

		String disaster = DisasterTracker.getCurrentDisaster();
		if (disaster == null) {
			disaster = extractDisaster(objective);
		}
		if (disaster == null) {
			disaster = UNKNOWN_DISASTER;
		}

		Text title = Text.literal("Current Disaster").formatted(Formatting.GOLD, Formatting.BOLD);
		Text value = Text.literal(disaster).formatted(Formatting.YELLOW);
		int screenWidth = context.getScaledWindowWidth();
		int titleX = (screenWidth - MinecraftClient.getInstance().textRenderer.getWidth(title)) / 2;
		int valueX = (screenWidth - MinecraftClient.getInstance().textRenderer.getWidth(value)) / 2;

		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, title, titleX, 8, 0xFFFFFF);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, value, valueX, 20, 0xFFFFFF);
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
		String fallbackKnown = null;

		for (ScoreboardEntry entry : entries) {
			String displayLine = entry.display() != null ? entry.display().getString() : null;
			String found = extractDisasterFromLine(displayLine, true);
			if (found != null) {
				return found;
			}

			found = extractDisasterFromLine(entry.name().getString());
			if (found != null) {
				return found;
			}

			String known = extractDisasterFromLine(displayLine, false);
			if (known != null && fallbackKnown == null) {
				fallbackKnown = known;
			}
			known = extractDisasterFromLine(entry.name().getString(), false);
			if (known != null && fallbackKnown == null) {
				fallbackKnown = known;
			}
		}

		return fallbackKnown;
	}

	private String extractDisasterFromLine(String line) {
		return extractDisasterFromLine(line, true);
	}

	private String extractDisasterFromLine(String line, boolean requireDisasterKeyword) {
		if (line == null) {
			return null;
		}

		String cleaned = line.trim();
		if (cleaned.isEmpty()) {
			return null;
		}

		String normalized = normalize(cleaned);
		Matcher matcher = DISASTER_PATTERN.matcher(normalized);
		if (matcher.find()) {
			String raw = matcher.group(1).trim();
			if (!raw.isEmpty() && !raw.startsWith("in ")) {
				return canonicalizeDisaster(raw);
			}
		}

		if (requireDisasterKeyword && !normalized.contains("disaster")) {
			return null;
		}

		if (normalized.contains("next disaster")) {
			return null;
		}

		String known = findKnownDisaster(normalized);
		return known;
	}

	private String canonicalizeDisaster(String rawDisaster) {
		String lower = normalize(rawDisaster);
		String known = findKnownDisaster(lower);
		return known != null ? known : rawDisaster;
	}

	private String findKnownDisaster(String lowerLine) {
		for (Map.Entry<String, String> entry : KNOWN_DISASTERS.entrySet()) {
			if (lowerLine.contains(entry.getKey())) {
				return entry.getValue();
			}
		}

		return null;
	}

	private String normalize(String text) {
		String lower = text.toLowerCase(Locale.ROOT);
		return lower.replaceAll("[^a-z0-9: -]", " ").replaceAll("\\s+", " ").trim();
	}

	private static Map<String, String> createKnownDisasters() {
		Map<String, String> disasters = new LinkedHashMap<>();
		disasters.put("dragons", "Dragons");
		disasters.put("purge", "Purge");
		disasters.put("werewolf", "Werewolf");
		disasters.put("the floor is lava", "The Floor Is Lava");
		disasters.put("floor is lava", "The Floor Is Lava");
		return disasters;
	}
}
