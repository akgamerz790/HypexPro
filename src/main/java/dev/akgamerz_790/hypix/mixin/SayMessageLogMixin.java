package dev.akgamerz_790.hypix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.akgamerz_790.hypix.debug.SayMessageLogWriter;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
public class SayMessageLogMixin {
	@Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
	private void hypixelx$logIncomingMessage(Text message, CallbackInfo ci) {
		SayMessageLogWriter.logReceivedMessage(message);
	}
}
