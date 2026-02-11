package dev.akgamerz_790.hypix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.akgamerz_790.hypix.disaster.DisasterTracker;
import dev.akgamerz_790.hypix.debug.SayMessageLogWriter;
import dev.akgamerz_790.hypix.util.HypixelServerUtil;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
public class SayMessageLogMixin {
	@Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
	private void hypixelx$logIncomingMessage(Text message, CallbackInfo ci) {
		if (!HypixelServerUtil.isHypixelServer()) {
			return;
		}

		DisasterTracker.onIncomingMessage(message);
		SayMessageLogWriter.logReceivedMessage(message);
		if (DisasterTracker.shouldHideChatMessage(message)) {
			ci.cancel();
		}
	}
}
