package dev.akgamerz_790.hypix.mixin;

import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.akgamerz_790.hypix.util.HypixelServerUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.boss.BossBar;

@Mixin(BossBarHud.class)
public class BossBarFilterMixin {
	private static final Pattern TIME_LEFT_PATTERN = Pattern.compile("^Time Left: \\d{1,2}:\\d{2}$");

	@Inject(method = "renderBossBar(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/entity/boss/BossBar;)V", at = @At("HEAD"), cancellable = true)
	private void hypixelx$filterTimeLeftBossBar(DrawContext context, int x, int y, BossBar bossBar, CallbackInfo ci) {
		if (!HypixelServerUtil.isHypixelServer() || bossBar == null) {
			return;
		}

		String text = bossBar.getName().getString();
		if (text != null && TIME_LEFT_PATTERN.matcher(text.trim()).matches()) {
			ci.cancel();
		}
	}
}
