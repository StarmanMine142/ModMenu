package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.config.ModMenuConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class DrawingUtil {
    public static void drawRandomVersionBackground(ModContainer container, int x, int y, int width, int height) {
        ModMetadata meta = container.getMetadata();
        int seed = meta.getName().hashCode() + meta.getVersion().getFriendlyString().hashCode();

        Random random = new Random(seed);
        int color = 0xFF000000 | Color.HSBtoRGB(random.nextFloat(), random.nextFloat() * 0.1f + 0.7f, 0.9f);
        if (!ModMenuConfigManager.getConfig().getRandomJavaColors()) {
            color = 0xFFDD5656;
        }

        float red = (float) ((color >> 16) & 255) / 255.0f;
        float green = (float) ((color >> 8) & 255) / 255.0f;
        float blue = (float) (color & 255) / 255.0f;
        float alpha = (float) ((color >> 24) & 255) / 255.0f;

        GLRenderer.pushFrame();
        GLRenderer.setShader(Shaders.COLOR);
        GLRenderer.enableState(State.BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GLRenderer.setColor4f(red, green, blue, alpha);

        TessellatorGeneral t = GLRenderer.getTessellator();
        t.startDrawingQuads();
        t.addVertexWithUV(x, y, 0, 0, 0);
        t.addVertexWithUV(x, y + height, 0, 0, 1);
        t.addVertexWithUV(x + width, y + height, 0, 1, 1);
        t.addVertexWithUV(x + width, y, 0, 1, 0);
        t.draw();

        GLRenderer.popFrame();
    }
}