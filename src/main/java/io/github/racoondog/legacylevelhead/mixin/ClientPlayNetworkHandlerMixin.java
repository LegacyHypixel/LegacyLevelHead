package io.github.racoondog.legacylevelhead.mixin;

import io.github.racoondog.legacylevelhead.HypixelApi;
import io.github.racoondog.legacylevelhead.config.Configs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onPlayerList", at = @At("HEAD"))
    private void getPlayers(PlayerListS2CPacket packet, CallbackInfo ci) {
        if (packet.getAction() != PlayerListS2CPacket.Action.ADD_PLAYER) return;
        if (!HypixelApi.isHypixel()) return;

        List<UUID> players = new ArrayList<>();
        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            if (!Configs.showSelf && entry.getProfile().equals(MinecraftClient.getInstance().getSession().getProfile())) continue;
            if (HypixelApi.getData(entry.getProfile().getId()) != null) continue;
            players.add(entry.getProfile().getId());
        }

        if (!players.isEmpty()) HypixelApi.queueFetch(players);
    }
}
