package dev.akgamerz_790.hypix.debug;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;

public final class ScoreboardDumpWriter {
	private static final long MIN_DUMP_INTERVAL_MS = 1200L;
	private static long lastDumpAtMs = 0L;
	private static String lastFingerprint = "";

	private ScoreboardDumpWriter() {
	}

	public static void dumpSidebarSnapshot(ScoreboardObjective objective, Collection<ScoreboardEntry> entries, String detectedDisaster) {
		long now = System.currentTimeMillis();
		if (now - lastDumpAtMs < MIN_DUMP_INTERVAL_MS) {
			return;
		}

		String payload = buildJson(objective, entries, detectedDisaster);
		String fingerprint = Integer.toHexString(payload.hashCode());
		if (fingerprint.equals(lastFingerprint)) {
			return;
		}

		lastDumpAtMs = now;
		lastFingerprint = fingerprint;

		Path dumpDir = FabricLoader.getInstance().getGameDir().resolve("hypixelx").resolve("scoreboard_dumps");
		Path latest = dumpDir.resolve("latest.json");
		Path history = dumpDir.resolve("history.jsonl");

		try {
			Files.createDirectories(dumpDir);
			Files.writeString(latest, payload, StandardCharsets.UTF_8);
			Files.writeString(history, payload + System.lineSeparator(), StandardCharsets.UTF_8,
				java.nio.file.StandardOpenOption.CREATE,
				java.nio.file.StandardOpenOption.APPEND);
		} catch (IOException ignored) {
			// Temporary debug utility: ignore write failures to avoid affecting gameplay.
		}
	}

	private static String buildJson(ScoreboardObjective objective, Collection<ScoreboardEntry> entries, String detectedDisaster) {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("{\n");
		sb.append("  \"timestamp\": \"").append(escape(Instant.now().toString())).append("\",\n");
		sb.append("  \"objectiveName\": \"").append(escape(objective.getName())).append("\",\n");
		sb.append("  \"objectiveDisplay\": \"").append(escape(objective.getDisplayName().getString())).append("\",\n");
		sb.append("  \"detectedDisaster\": \"").append(escape(detectedDisaster)).append("\",\n");
		sb.append("  \"entries\": [\n");

		boolean first = true;
		for (ScoreboardEntry entry : entries) {
			if (!first) {
				sb.append(",\n");
			}
			first = false;

			String display = entry.display() != null ? entry.display().getString() : "";
			sb.append("    {\n");
			sb.append("      \"owner\": \"").append(escape(entry.owner())).append("\",\n");
			sb.append("      \"name\": \"").append(escape(entry.name().getString())).append("\",\n");
			sb.append("      \"display\": \"").append(escape(display)).append("\",\n");
			sb.append("      \"value\": ").append(entry.value()).append("\n");
			sb.append("    }");
		}

		sb.append("\n  ]\n");
		sb.append("}\n");
		return sb.toString();
	}

	private static String escape(String value) {
		if (value == null) {
			return "";
		}
		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\r", "\\r")
			.replace("\n", "\\n")
			.replace("\t", "\\t");
	}
}
