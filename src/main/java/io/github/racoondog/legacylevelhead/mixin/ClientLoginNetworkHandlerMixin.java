package io.github.racoondog.legacylevelhead.mixin;

import com.google.gson.JsonObject;
import io.github.racoondog.legacylevelhead.HttpUtils;
import io.github.racoondog.legacylevelhead.HypixelApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Optional;

import static io.github.racoondog.legacylevelhead.LegacyLevelHead.LOGGER;

@Environment(EnvType.CLIENT)
@Mixin(ClientLoginNetworkHandler.class)
public abstract class ClientLoginNetworkHandlerMixin {
    /**
     * Setting {@link HypixelApi#hash} to {@code null} on error prevents API calls.
     * Disclaimer: Very limited spanish skills
     */
    @Inject(method = "onLoginSuccess", at = @At("HEAD"))
    private void authenticate(LoginSuccessS2CPacket packet, CallbackInfo ci) {
        String uuid = MinecraftClient.getInstance().getSession().getUuid();
        Optional<JsonObject> authBegin = HttpUtils.getJson(String.format("https://api.sk1er.club/auth/begin?uuid=%s&mod=level_head&ver=8.2.2", uuid));

        if (!authBegin.isPresent() || !authBegin.get().has("success") || !authBegin.get().get("success").getAsBoolean()) {
            throw new RuntimeException("No puedo ir al connectado begiño :sob:");
        }

        HypixelApi.hash = authBegin.get().get("hash").getAsString();

        JsonObject sessionBody = new JsonObject();
        sessionBody.addProperty("accessToken", MinecraftClient.getInstance().getSession().getAccessToken());
        sessionBody.addProperty("selectedProfile", uuid);
        sessionBody.addProperty("serverId", HypixelApi.hash);

        try {
            int statusCode = HttpUtils.postStatus("https://sessionserver.mojang.com/session/minecraft/join", sessionBody);
            if (statusCode != 204) {
                LOGGER.error("Error logging into moyang servors :sob: Status code " + statusCode);
                HypixelApi.hash = null;
            }
        } catch (IOException e) {
            HypixelApi.hash = null;
            throw new RuntimeException("No puedo ir al connectado mojaño :sob:", e);
        }

        String name = MinecraftClient.getInstance().getSession().getUsername();

        Optional<JsonObject> authEnd = HttpUtils.getJson(String.format("https://api.sk1er.club/auth/final?hash=%s&name=%s", HypixelApi.hash, name));

        if (!authEnd.isPresent() || !authEnd.get().has("success") || !authEnd.get().get("success").getAsBoolean()) {
            //noinspection OptionalGetWithoutIsPresent
            LOGGER.error("Could not finish authentication: {}", authEnd.get());
            HypixelApi.hash = null;
        }
    }
}
