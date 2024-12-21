# Skija: Java bindings for Skia
Fork of  
https://github.com/HumbleUI/Skija/

## What are the changes?
OpenGL Texture can now be used as Skia Image

### Example
``` java
public static void drawImage(int textureId, float x, float y, float width, float height) {
    canvas.drawImageRect(Image.adoptTextureFrom(directContext, textureId, GL11.GL_TEXTURE_2D, (int) width,
            (int) height, GL11.GL_RGBA8, SurfaceOrigin.TOP_LEFT, ColorType.RGBA_8888),
            Rect.makeXYWH(x, y, width, height));
}
```
