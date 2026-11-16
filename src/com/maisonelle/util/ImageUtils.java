package com.maisonelle.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Image loading and scaling utility using standard Java ImageIO and AWT.
 * Provides bicubic scaling, aspect-ratio preservation, and pastel fallback generation.
 * Strictly no third-party libraries.
 */
public class ImageUtils {

    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();

    public static ImageIcon loadProductImage(String imageName, int targetWidth, int targetHeight) {
        if (imageName == null || imageName.trim().isEmpty()) {
            imageName = "placeholder.jpg";
        }

        String cacheKey = imageName + "_" + targetWidth + "x" + targetHeight;
        if (CACHE.containsKey(cacheKey)) {
            return CACHE.get(cacheKey);
        }

        BufferedImage original = findImageFile(imageName);

        if (original != null) {
            ImageIcon icon = scaleImage(original, targetWidth, targetHeight);
            CACHE.put(cacheKey, icon);
            return icon;
        }

        // Generate attractive pastel placeholder
        BufferedImage placeholder = createPlaceholder(imageName, targetWidth, targetHeight);
        ImageIcon icon = new ImageIcon(placeholder);
        CACHE.put(cacheKey, icon);
        return icon;
    }

    private static BufferedImage findImageFile(String name) {
        String[] lookupPaths = {
                "resources/images/" + name,
                "resources/images/" + name.replace(".png", ".jpg"),
                "resources/images/" + name.replace(".jpg", ".png"),
                name
        };

        for (String path : lookupPaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) return img;
                } catch (Exception ignored) {}
            }

            // Check classpath
            java.net.URL url = ImageUtils.class.getResource("/images/" + name);
            if (url != null) {
                try {
                    BufferedImage img = ImageIO.read(url);
                    if (img != null) return img;
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static ImageIcon scaleImage(BufferedImage src, int maxWidth, int maxHeight) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();

        double scale = Math.min((double) maxWidth / srcW, (double) maxHeight / srcH);
        int scaledW = (int) Math.round(srcW * scale);
        int scaledH = (int) Math.round(srcH * scale);

        BufferedImage canvas = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();

        // High Quality Bicubic Scaling Hints
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill with warm ivory / card white
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, maxWidth, maxHeight);

        int x = (maxWidth - scaledW) / 2;
        int y = (maxHeight - scaledH) / 2;

        g2.drawImage(src, x, y, scaledW, scaledH, null);
        g2.dispose();

        return new ImageIcon(canvas);
    }

    private static BufferedImage createPlaceholder(String caption, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Warm ivory / soft beige background
        g2.setColor(Theme.SECONDARY_BACKGROUND);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 12, 12));

        g2.setColor(Theme.BORDERS);
        g2.draw(new RoundRectangle2D.Float(0, 0, width - 1, height - 1, 12, 12));

        // Elegant Maisonelle Monogram
        g2.setColor(Theme.MAIN_HEADINGS);
        g2.setFont(new Font("Serif", Font.BOLD, Math.max(16, height / 5)));
        FontMetrics fm = g2.getFontMetrics();
        String brand = "Maisonelle";
        g2.drawString(brand, (width - fm.stringWidth(brand)) / 2, height / 2 - 4);

        g2.setColor(Theme.MUTED_TEXT);
        g2.setFont(Theme.FONT_SMALL);
        FontMetrics fm2 = g2.getFontMetrics();
        String sub = "Curated Piece";
        g2.drawString(sub, (width - fm2.stringWidth(sub)) / 2, height / 2 + 16);

        g2.dispose();
        return img;
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
