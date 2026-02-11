package dev.akgamerz_790.hypix.debug;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

public final class ScoreboardTextLogWriter {
	private static final long MIN_LOG_INTERVAL_MS = 1200L;
	private static long lastLogAtMs = 0L;
	private static String lastFingerprint = "";

	private ScoreboardTextLogWriter() {
	}

	public static void logSidebar(ScoreboardObjective objective, Collection<ScoreboardEntry> entries) {
		long now = System.currentTimeMillis();
		if (now - lastLogAtMs < MIN_LOG_INTERVAL_MS) {
			return;
		}

		String payload = buildPlainText(objective, entries);
		String fingerprint = Integer.toHexString(payload.hashCode());
		if (fingerprint.equals(lastFingerprint)) {
			return;
		}

		lastLogAtMs = now;
		lastFingerprint = fingerprint;

		Path dir = FabricLoader.getInstance().getGameDir().resolve("hypixelx").resolve("scoreboard_text");
		Path latest = dir.resolve("latest.txt");
		Path history = dir.resolve("history.log");

		try {
			Files.createDirectories(dir);
			Files.writeString(latest, payload, StandardCharsets.UTF_8);
			Files.writeString(history, payload + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException ignored) {
			// Temporary debug logger: never interrupt gameplay.
		}
	}

	private static String buildPlainText(ScoreboardObjective objective, Collection<ScoreboardEntry> entries) {
		StringBuilder sb = new StringBuilder(1024);
		Scoreboard scoreboard = objective.getScoreboard();
		sb.append("[").append(Instant.now()).append("]").append(System.lineSeparator());
		sb.append("Objective: ").append(objective.getDisplayName().getString()).append(System.lineSeparator());

		List<String> lines = new ArrayList<>();
		List<ScoreboardEntry> sortedEntries = new ArrayList<>(entries);
		sortedEntries.sort(Comparator.comparingInt(ScoreboardEntry::value).reversed().thenComparing(ScoreboardEntry::owner));

		for (ScoreboardEntry entry : sortedEntries) {
			String display = entry.display() != null ? entry.display().getString() : "";
			String name = entry.name().getString();
			Team team = scoreboard.getScoreHolderTeam(entry.owner());
			String decorated = Team.decorateName(team, Text.literal(entry.owner())).getString();
			String text = !display.isBlank() ? display : (!decorated.isBlank() ? decorated : name);
			lines.add(text);
		}

		for (String line : lines) {
			sb.append(line).append(System.lineSeparator());
		}

		return sb.toString();
	}
}
