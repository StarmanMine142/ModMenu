package io.github.prospector.modmenu.gui.widget;

import io.github.prospector.modmenu.ModMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import org.lwjgl.opengl.GL11;

public class UpdateAvailableBadge {
    private static final String UPDATE_ICON = "/assets/" + ModMenu.MOD_ID + "/textures/gui/update_available.png";

    public static void renderBadge(int x, int y) {
        GLRenderer.pushFrame();
        GLRenderer.setShader(Shaders.INTERFACE);
        GLRenderer.enableState(State.BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().textureManager.bindTexture(Minecraft.getMinecraft().textureManager.loadTexture(UPDATE_ICON));

        int totalFrames = 6;
        int frameHeight = 8;
        int currentFrame = (int) (System.currentTimeMillis() / 200) % totalFrames;

        float vMin = (float) (currentFrame * frameHeight) / (totalFrames * frameHeight);
        float vMax = (float) ((currentFrame + 1) * frameHeight) / (totalFrames * frameHeight);

        TessellatorGeneral t = GLRenderer.getTessellator();
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + 8, 0, 0.0F, vMax);
        t.addVertexWithUV(x + 8, y + 8, 0, 1.0F, vMax);
        t.addVertexWithUV(x + 8, y, 0, 1.0F, vMin);
        t.addVertexWithUV(x, y, 0, 0.0F, vMin);
        t.draw();

        GLRenderer.popFrame();
    }
}