package dev.akgamerz_790.hypix.debug;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

public final class SayMessageLogWriter {
	private static final long MIN_LOG_INTERVAL_MS = 50L;
	private static long lastLogAtMs = 0L;
	private static String lastMessage = "";

	private SayMessageLogWriter() {
	}

	public static void logReceivedMessage(Text message) {
		if (message == null) {
			return;
		}

		String raw = message.getString();
		if (raw == null || raw.isBlank()) {
			return;
		}

		long now = System.currentTimeMillis();
		if (now - lastLogAtMs < MIN_LOG_INTERVAL_MS && raw.equals(lastMessage)) {
			return;
		}

		lastLogAtMs = now;
		lastMessage = raw;

		String payload = buildJson(raw, now);
		Path dir = FabricLoader.getInstance().getGameDir().resolve("hypixelx").resolve("say_logs");
		Path latest = dir.resolve("latest.json");
		Path history = dir.resolve("history.jsonl");

		try {
			Files.createDirectories(dir);
			Files.writeString(latest, payload, StandardCharsets.UTF_8);
			Files.writeString(history, payload + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException ignored) {
			// Temporary debug logger: never interrupt gameplay.
		}
	}

	private static String buildJson(String raw, long epochMillis) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("{");
		sb.append("\"timestamp\":\"").append(escape(Instant.ofEpochMilli(epochMillis).toString())).append("\",");
		sb.append("\"epochMillis\":").append(epochMillis).append(",");
		sb.append("\"message\":\"").append(escape(raw)).append("\"");
		sb.append("}");
		return sb.toString();
	}

	private static String escape(String value) {
		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\r", "\\r")
			.replace("\n", "\\n")
			.replace("\t", "\\t");
	}
}
