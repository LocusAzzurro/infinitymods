package tt432.infinitymods.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.NotificationModUpdateScreen;
import net.minecraftforge.internal.BrandingControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tt432.infinitymods.TextUtil;

import javax.annotation.Nullable;

/**
 * @author DustW
 **/
@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {
    @Shadow private long fadeInStart;

    @Shadow @Final private boolean fading;

    @Shadow @Final private PanoramaRenderer panorama;

    @Shadow @Final private static ResourceLocation PANORAMA_OVERLAY;

    @Shadow @Final private static ResourceLocation MINECRAFT_LOGO;

    @Shadow @Final private boolean minceraftEasterEgg;

    @Shadow @Final private static ResourceLocation MINECRAFT_EDITION;

    @Shadow @Nullable private String splash;

    @Shadow protected abstract boolean realmsNotificationsEnabled();

    @Shadow private Screen realmsNotificationsScreen;

    @Shadow private NotificationModUpdateScreen modUpdateNotification;

    protected MixinTitleScreen(Component p_96550_) {
        super(p_96550_);
    }

    @Inject(method = "render", cancellable = true, at = @At("HEAD"))
    private void infRender(PoseStack p_96739_, int p_96740_, int p_96741_, float p_96742_, CallbackInfo ci) {
        ci.cancel();

        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        float f = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
        this.panorama.render(p_96742_, Mth.clamp(f, 0.0F, 1.0F));
        int i = 274;
        int j = this.width / 2 - 137;
        int k = 30;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(f, 0.0F, 1.0F)) : 1.0F);
        blit(p_96739_, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float f1 = this.fading ? Mth.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = Mth.ceil(f1 * 255.0F) << 24;
        if ((l & -67108864) != 0) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, MINECRAFT_LOGO);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f1);
            if (this.minceraftEasterEgg) {
                this.blitOutlineBlack(j, 30, (p_210862_, p_210863_) -> {
                    this.blit(p_96739_, p_210862_ + 0, p_210863_, 0, 0, 99, 44);
                    this.blit(p_96739_, p_210862_ + 99, p_210863_, 129, 0, 27, 44);
                    this.blit(p_96739_, p_210862_ + 99 + 26, p_210863_, 126, 0, 3, 44);
                    this.blit(p_96739_, p_210862_ + 99 + 26 + 3, p_210863_, 99, 0, 26, 44);
                    this.blit(p_96739_, p_210862_ + 155, p_210863_, 0, 45, 155, 44);
                });
            } else {
                this.blitOutlineBlack(j, 30, (p_211778_, p_211779_) -> {
                    this.blit(p_96739_, p_211778_ + 0, p_211779_, 0, 0, 155, 44);
                    this.blit(p_96739_, p_211778_ + 155, p_211779_, 0, 45, 155, 44);
                });
            }

            RenderSystem.setShaderTexture(0, MINECRAFT_EDITION);
            blit(p_96739_, j + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);

            net.minecraftforge.client.ForgeHooksClient.renderMainMenu((TitleScreen) (Object) this, p_96739_, this.font, this.width, this.height, l);
            if (this.splash != null) {
                p_96739_.pushPose();
                p_96739_.translate((double)(this.width / 2 + 90), 70.0D, 0.0D);
                p_96739_.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
                float f2 = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) * 0.1F);
                f2 = f2 * 100.0F / (float)(this.font.width(this.splash) + 32);
                p_96739_.scale(f2, f2, f2);
                drawCenteredString(p_96739_, this.font, this.splash, 0, -8, 16776960 | l);
                p_96739_.popPose();
            }

            String s = "Minecraft " + SharedConstants.getCurrentVersion().getName();
            if (this.minecraft.isDemo()) {
                s = s + " Demo";
            } else {
                s = s + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
            }

            if (Minecraft.checkModStatus().shouldReportAsModified()) {
                s = s + I18n.get("menu.modded");
            }

            BrandingControl.forEachLine(true, true, (brdline, brd) -> {
                if (brdline == 0) {
                    drawString(p_96739_, this.font,
                            TextUtil.makeFabulous("Infinity") + ChatFormatting.RESET + " mods loaded",
                            2, this.height - 10,
                            16777215 | l);
                }
                else {
                    drawString(p_96739_, this.font, brd,
                            2, this.height - ( 10 + brdline * (this.font.lineHeight + 1)),
                            16777215 | l);
                }
            });

            BrandingControl.forEachAboveCopyrightLine((brdline, brd) ->
                    drawString(p_96739_, this.font, brd, this.width - font.width(brd), this.height - (10 + (brdline + 1) * ( this.font.lineHeight + 1)), 16777215 | l)
            );


            for(GuiEventListener guieventlistener : this.children()) {
                if (guieventlistener instanceof AbstractWidget) {
                    ((AbstractWidget)guieventlistener).setAlpha(f1);
                }
            }

            super.render(p_96739_, p_96740_, p_96741_, p_96742_);
            if (this.realmsNotificationsEnabled() && f1 >= 1.0F) {
                this.realmsNotificationsScreen.render(p_96739_, p_96740_, p_96741_, p_96742_);
            }
            if (f1 >= 1.0f) {
                modUpdateNotification.render(p_96739_, p_96740_, p_96741_, p_96742_);
            }
        }
    }
}
