package dev.akgamerz_790.hypix.sidebar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

public final class DisasterSidebarParser {
	private static final Pattern DATE_LINE = Pattern.compile("^\\d{2}/\\d{2}/\\d{2}.*$");
	private static final Pattern STRIKETHROUGH_CODE = Pattern.compile("(?:\u00A7m|Â§m)");

	private DisasterSidebarParser() {
	}

	public static Result parse(ScoreboardObjective objective) {
		Scoreboard scoreboard = objective.getScoreboard();
		List<ScoreboardEntry> entries = new ArrayList<>(scoreboard.getScoreboardEntries(objective));
		entries.sort(Comparator.comparingInt(ScoreboardEntry::value).reversed().thenComparing(ScoreboardEntry::owner));

		List<Line> lines = new ArrayList<>();
		for (ScoreboardEntry entry : entries) {
			String raw = buildRenderedLine(scoreboard, entry);
			String clean = cleanLine(raw);
			if (!clean.isBlank()) {
				lines.add(new Line(raw, clean));
			}
		}

		String countdown = extractCountdown(lines);
		List<String> current = new ArrayList<>();
		List<String> previous = new ArrayList<>();
		extractDisasters(lines, current, previous);
		return new Result(current, previous, countdown);
	}

	private static String buildRenderedLine(Scoreboard scoreboard, ScoreboardEntry entry) {
		String display = entry.display() != null ? entry.display().getString() : "";
		if (!display.isBlank()) {
			return display;
		}

		Team team = scoreboard.getScoreHolderTeam(entry.owner());
		return Team.decorateName(team, Text.literal(entry.owner())).getString();
	}

	private static String cleanLine(String raw) {
		return raw
			.replaceAll("\u00A7.", "")
			.replaceAll("Â§.", "")
			.replace('\u00A0', ' ')
			.trim()
			.replaceAll("\\s+", " ");
	}

	private static String extractCountdown(List<Line> lines) {
		for (Line line : lines) {
			String lower = line.clean.toLowerCase();
			if (lower.startsWith("time left:") || lower.startsWith("next disaster:") || lower.startsWith("game starts in:")) {
				return line.clean;
			}
		}
		return null;
	}

	private static void extractDisasters(List<Line> lines, List<String> current, List<String> previous) {
		int headerIndex = -1;
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).clean.toLowerCase().startsWith("disasters:")) {
				headerIndex = i;
				break;
			}
		}
		if (headerIndex < 0) {
			return;
		}

		for (int i = headerIndex + 1; i < lines.size(); i++) {
			Line line = lines.get(i);
			String clean = line.clean;
			String lower = clean.toLowerCase();

			if (lower.contains("www.hypixel.net") || lower.startsWith("players alive:") || lower.startsWith("time left:") || DATE_LINE.matcher(clean).matches()) {
				break;
			}
			if (clean.endsWith(":")) {
				break;
			}
			if (clean.length() < 2) {
				continue;
			}

			boolean previousDisaster = STRIKETHROUGH_CODE.matcher(line.raw).find();
			if (previousDisaster) {
				previous.add(clean);
			} else if (current.isEmpty()) {
				current.add(clean);
			}
		}
	}

	public static final class Result {
		private final List<String> currentDisasters;
		private final List<String> previousDisasters;
		private final String countdownText;

		public Result(List<String> currentDisasters, List<String> previousDisasters, String countdownText) {
			this.currentDisasters = currentDisasters;
			this.previousDisasters = previousDisasters;
			this.countdownText = countdownText;
		}

		public List<String> currentDisasters() {
			return currentDisasters;
		}

		public List<String> previousDisasters() {
			return previousDisasters;
		}

		public String countdownText() {
			return countdownText;
		}
	}

	private static final class Line {
		private final String raw;
		private final String clean;

		private Line(String raw, String clean) {
			this.raw = raw;
			this.clean = clean;
		}
	}
}
