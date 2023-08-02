package io.github.racoondog.legacylevelhead.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.racoondog.legacylevelhead.HypixelApi;
import io.github.racoondog.legacylevelhead.config.Configs;
import io.github.racoondog.legacylevelhead.config.LevelheadData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.Formatting;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity> {
    public PlayerEntityRendererMixin(EntityRenderDispatcher entityRenderDispatcher, EntityModel entityModel, float f) {
        super(entityRenderDispatcher, entityModel, f);
    }

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;DDDFF)V", at = @At("TAIL"))
    private void injectRender(AbstractClientPlayerEntity entity, double x, double y, double z, float yaw, float tickDelta, CallbackInfo ci) {
        if (!Configs.enableNametag) return;
        if (!HypixelApi.isHypixel()) return;
        if (entity.isMainPlayer() && !Configs.showSelf) return;

        AbstractTeam team = entity.getScoreboardTeam();
        if (team != null) {
            switch (team.getCollisionRule()) {
                case NEVER:
                    return;
                case HIDE_FOR_OTHER_TEAMS:
                    if (!team.isEqual(MinecraftClient.getInstance().player.getScoreboardTeam())) return;
                    break;
                case HIDE_FOR_OWN_TEAM:
                    if (team.isEqual(MinecraftClient.getInstance().player.getScoreboardTeam())) return;
                    break;
            }
        }

        if (entity.rider != null) return;
        double distance = entity.squaredDistanceTo(this.dispatcher.field_11098);
        if (distance > Math.min(4096, Configs.renderDistance * Configs.renderDistance)) return;
        if (entity.hasCustomName() && entity.getCustomName().isEmpty()) return;
        if (entity.getName().asUnformattedString().isEmpty()) return;
        if (entity.getName().asFormattedString().contains(Formatting.OBFUSCATED.toString())) return;
        if (entity.isInvisible()) return;
        if (entity.isInvisibleTo(MinecraftClient.getInstance().player)) return;

        LevelheadData data = HypixelApi.getData(entity.getUuid());
        if (data == null) {
            HypixelApi.queueFetch(entity.getUuid());
            return;
        }

        String text = data.formatNametag();
        TextRenderer renderer = this.getFontRenderer();
        int textWidth = renderer.getStringWidth(text);

        float offset = 0.5f;
        if (!entity.isMainPlayer()) {
            offset = 0.8f;
            if (entity.getScoreboard().getObjectiveForSlot(2) != null && distance < 100) offset += 0.3f;
        }

        boolean sneaking = entity.isSneaking();

        // Matrixes
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height + offset + Configs.offset, z);
        GL11.glNormal3f(0f, 1f, 0f);
        GlStateManager.rotate(-this.dispatcher.yaw, 0f, 1f, 0f);
        boolean invertPitch = MinecraftClient.getInstance().options.perspective == 2;
        GlStateManager.rotate(invertPitch ? -this.dispatcher.pitch : this.dispatcher.pitch, 1f, 0f, 0f);
        float scale = -0.02666667f * Configs.fontSize;  //mamgic numbers
        GlStateManager.scale(scale, scale, scale);
        if (sneaking) GlStateManager.translate(0.0F, 9.374999F, 0.0F); //shift down nametag
        GlStateManager.disableLighting();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        if (!sneaking) GlStateManager.disableDepthTest(); //render through walls

        int center = textWidth / 2;

        // Render Box
        if (Configs.renderBackground) {
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableTexture();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(-center - 1, -1, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).next();
            bufferBuilder.vertex(-center - 1, 8, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).next();
            bufferBuilder.vertex(center + 1, 8, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).next();
            bufferBuilder.vertex(center + 1, -1, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).next();
            tessellator.draw();

            GlStateManager.enableTexture();
            GlStateManager.depthMask(true);
        }

        // Render Text
        renderer.draw(text, -center, 0, 553648127);
        if (!sneaking) {
            GlStateManager.enableDepthTest();
            renderer.draw(text, -center, 0, -1);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }
}
