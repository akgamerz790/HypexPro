package dev.akgamerz_790.hypix.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.akgamerz_790.hypix.disaster.DisasterTracker;
import dev.akgamerz_790.hypix.util.HypixelServerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(InGameHud.class)
public class HypixMixin {
	private static final String UNKNOWN_DISASTER = "Unknown";
	private static final Pattern DISASTER_PATTERN = Pattern.compile("disaster\\s*[:\\-]?\\s*(.+)", Pattern.CASE_INSENSITIVE);
	private static final Map<String, String> KNOWN_DISASTERS = createKnownDisasters();

	@Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
	private void renderDisasterOverlay(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
		if (!HypixelServerUtil.isHypixelServer()) {
			return;
		}
		if (objective == null) {
			return;
		}

		List<String> disasters = new ArrayList<>(DisasterTracker.getCurrentDisasters());
		if (disasters.isEmpty()) {
			String fallback = extractDisaster(objective);
			if (fallback != null) {
				disasters.add(fallback);
			}
		}
		if (disasters.isEmpty()) {
			disasters.add(UNKNOWN_DISASTER);
		}

		Text title = Text.literal(disasters.size() > 1 ? "Current Disasters" : "Current Disaster")
			.formatted(Formatting.GOLD, Formatting.BOLD);
		List<Text> lines = new ArrayList<>();
		for (String disaster : disasters) {
			lines.add(Text.literal("- " + disaster).formatted(Formatting.YELLOW));
		}

		MinecraftClient client = MinecraftClient.getInstance();
		int lineHeight = client.textRenderer.fontHeight + 2;
		int contentWidth = client.textRenderer.getWidth(title);
		for (Text line : lines) {
			contentWidth = Math.max(contentWidth, client.textRenderer.getWidth(line));
		}

		int padding = 6;
		int boxWidth = contentWidth + (padding * 2);
		int boxHeight = lineHeight + (lines.size() * lineHeight) + padding * 2 - 2;
		int screenWidth = context.getScaledWindowWidth();
		int boxX = (screenWidth - boxWidth) / 2;
		int boxY = 8;

		context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xAA000000);

		int titleX = boxX + (boxWidth - client.textRenderer.getWidth(title)) / 2;
		int textY = boxY + padding;
		context.drawTextWithShadow(client.textRenderer, title, titleX, textY, 0xFFFFFF);

		textY += lineHeight;
		for (Text line : lines) {
			int lineX = boxX + (boxWidth - client.textRenderer.getWidth(line)) / 2;
			context.drawTextWithShadow(client.textRenderer, line, lineX, textY, 0xFFFFFF);
			textY += lineHeight;
		}

		ci.cancel();
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

			found = extractDisasterFromLine(entry.name().getString(), true);
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

		return findKnownDisaster(normalized);
	}

	private String canonicalizeDisaster(String rawDisaster) {
		String known = findKnownDisaster(normalize(rawDisaster));
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
		return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9: -]", " ").replaceAll("\\s+", " ").trim();
	}

	private static Map<String, String> createKnownDisasters() {
		Map<String, String> disasters = new LinkedHashMap<>();
		disasters.put("dragons", "Dragons");
		disasters.put("purge", "Purge");
		disasters.put("werewolf", "Werewolf");
		disasters.put("the floor is lava", "The Floor Is Lava");
		disasters.put("floor is lava", "The Floor Is Lava");
		disasters.put("withers", "Withers");
		disasters.put("anvil rain", "Anvil Rain");
		return disasters;
	}
}
