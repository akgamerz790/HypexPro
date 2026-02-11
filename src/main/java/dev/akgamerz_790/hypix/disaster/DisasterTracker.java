package dev.akgamerz_790.hypix.disaster;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.text.Text;

public final class DisasterTracker {
	private static final Pattern ANNOUNCE_PATTERN = Pattern.compile("^\\s*([A-Za-z][A-Za-z ']+?)\\s*[-:]+\\s+.+$");
	private static final Pattern PURGE_PATTERN = Pattern.compile("^\\s*([A-Za-z][A-Za-z ']+?)\\s+PvP is enabled.+$");
	private static final Pattern IN_THE_PATTERN = Pattern.compile("in the ([A-Za-z ]+?)(?:[.!]|$)", Pattern.CASE_INSENSITIVE);
	private static final Map<String, String> DISASTER_NAMES = createDisasterNames();
	private static final long MAX_AGE_MS = 10 * 60 * 1000L;

	private static volatile String currentDisaster = null;
	private static volatile long lastUpdateMs = 0L;

	private DisasterTracker() {
	}

	public static void onIncomingMessage(Text message) {
		if (message == null) {
			return;
		}

		String raw = message.getString();
		if (raw == null || raw.isBlank()) {
			return;
		}

		String line = normalize(raw);

		Matcher announce = ANNOUNCE_PATTERN.matcher(line);
		if (announce.matches()) {
			String detected = canonicalize(announce.group(1));
			if (detected != null) {
				update(detected);
				return;
			}
		}

		Matcher purge = PURGE_PATTERN.matcher(line);
		if (purge.matches()) {
			String detected = canonicalize(purge.group(1));
			if (detected != null) {
				update(detected);
				return;
			}
		}

		Matcher inThe = IN_THE_PATTERN.matcher(line);
		if (inThe.find()) {
			String detected = canonicalize(inThe.group(1));
			if (detected != null) {
				update(detected);
				return;
			}
		}

		String direct = canonicalize(line);
		if (direct != null) {
			update(direct);
		}
	}

	public static String getCurrentDisaster() {
		String disaster = currentDisaster;
		if (disaster == null) {
			return null;
		}

		if (System.currentTimeMillis() - lastUpdateMs > MAX_AGE_MS) {
			return null;
		}

		return disaster;
	}

	private static void update(String disaster) {
		currentDisaster = disaster;
		lastUpdateMs = System.currentTimeMillis();
	}

	private static String canonicalize(String raw) {
		if (raw == null) {
			return null;
		}

		String lower = normalize(raw).toLowerCase(Locale.ROOT);
		for (Map.Entry<String, String> entry : DISASTER_NAMES.entrySet()) {
			if (lower.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	private static String normalize(String text) {
		return text
			.replaceAll("§.", "")
			.replaceAll("[^A-Za-z0-9 :\\-!]", " ")
			.replaceAll("\\s+", " ")
			.trim();
	}

	private static Map<String, String> createDisasterNames() {
		Map<String, String> names = new LinkedHashMap<>();
		names.put("the floor is lava", "The Floor Is Lava");
		names.put("floor is lava", "The Floor Is Lava");
		names.put("zombie apocalypse", "Zombie Apocalypse");
		names.put("meteor shower", "Meteor Shower");
		names.put("tnt rain", "TNT Rain");
		names.put("hot potato", "Hot Potato");
		names.put("stampede", "Stampede");
		names.put("dragons", "Dragons");
		names.put("purge", "Purge");
		names.put("werewolf", "Werewolf");
		return names;
	}
}
