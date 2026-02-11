package dev.akgamerz_790.hypix.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.akgamerz_790.hypix.sidebar.DisasterSidebarParser;
import dev.akgamerz_790.hypix.sidebar.SidebarRemover;
import dev.akgamerz_790.hypix.util.HypixelServerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(InGameHud.class)
public class HypixMixin {
	private static final String UNKNOWN_DISASTER = "Unknown";

	@Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
	private void renderDisasterOverlay(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
		if (!HypixelServerUtil.isHypixelServer() || objective == null) {
			return;
		}

		DisasterSidebarParser.Result parsed = DisasterSidebarParser.parse(objective);

		if (SidebarRemover.shouldRemoveSidebar()) {
			drawCurrentBox(context, parsed.currentDisasters());
			drawPreviousBox(context, parsed.previousDisasters());
			drawCountdownBox(context, parsed.countdownText());
			ci.cancel();
		}
	}

	@Inject(method = "setTitle(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
	private void hideHypixelCountdownTitle(Text title, CallbackInfo ci) {
		if (!HypixelServerUtil.isHypixelServer() || title == null) {
			return;
		}

		String text = title.getString().trim().toLowerCase();
		if (text.matches("[1-3]") || text.equals("go") || text.equals("go!")) {
			ci.cancel();
		}
	}

	private void drawCurrentBox(DrawContext context, List<String> currentDisasters) {
		String value = currentDisasters.isEmpty() ? UNKNOWN_DISASTER : currentDisasters.get(0);
		Text title = Text.literal("Current Disaster").formatted(Formatting.GOLD, Formatting.BOLD);
		Text line = Text.literal(value).formatted(Formatting.YELLOW);
		drawCenteredTopBox(context, title, List.of(line), 8);
	}

	private void drawPreviousBox(DrawContext context, List<String> previousDisasters) {
		if (previousDisasters.isEmpty()) {
			return;
		}

		Text title = Text.literal("Previous Disasters").formatted(Formatting.GRAY, Formatting.BOLD);
		List<Text> lines = new java.util.ArrayList<>();
		for (int i = 0; i < previousDisasters.size() && i < 4; i++) {
			lines.add(Text.literal("- " + previousDisasters.get(i)).formatted(Formatting.DARK_GRAY));
		}
		drawCenteredTopBox(context, title, lines, 48);
	}

	private void drawCountdownBox(DrawContext context, String countdownText) {
		if (countdownText == null || countdownText.isBlank()) {
			return;
		}

		Text text = Text.literal(countdownText).formatted(Formatting.WHITE, Formatting.BOLD);
		MinecraftClient client = MinecraftClient.getInstance();
		int padding = 6;
		int boxWidth = client.textRenderer.getWidth(text) + (padding * 2);
		int boxHeight = client.textRenderer.fontHeight + (padding * 2);
		int x = (context.getScaledWindowWidth() - boxWidth) / 2;
		int y = context.getScaledWindowHeight() - 62;

		context.fill(x, y, x + boxWidth, y + boxHeight, 0xAA000000);
		context.drawTextWithShadow(client.textRenderer, text, x + padding, y + padding, 0xFFFFFF);
	}

	private void drawCenteredTopBox(DrawContext context, Text title, List<Text> lines, int y) {
		MinecraftClient client = MinecraftClient.getInstance();
		int lineHeight = client.textRenderer.fontHeight + 2;
		int contentWidth = client.textRenderer.getWidth(title);
		for (Text line : lines) {
			contentWidth = Math.max(contentWidth, client.textRenderer.getWidth(line));
		}

		int padding = 6;
		int boxWidth = contentWidth + (padding * 2);
		int boxHeight = lineHeight + (lines.size() * lineHeight) + padding * 2 - 2;
		int x = (context.getScaledWindowWidth() - boxWidth) / 2;

		context.fill(x, y, x + boxWidth, y + boxHeight, 0xAA000000);

		int titleX = x + (boxWidth - client.textRenderer.getWidth(title)) / 2;
		int textY = y + padding;
		context.drawTextWithShadow(client.textRenderer, title, titleX, textY, 0xFFFFFF);

		textY += lineHeight;
		for (Text line : lines) {
			int lineX = x + (boxWidth - client.textRenderer.getWidth(line)) / 2;
			context.drawTextWithShadow(client.textRenderer, line, lineX, textY, 0xFFFFFF);
			textY += lineHeight;
		}
	}
}
