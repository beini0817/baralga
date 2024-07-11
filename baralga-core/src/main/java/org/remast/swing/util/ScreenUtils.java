package org.remast.swing.util;

import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JApplet;

public class ScreenUtils {
    private static final Area SCREEN_AREA;
    private static final Rectangle[] SCREENS;
    private static final Insets[] INSETS;
    private static final Rectangle[] SCREENS_WITH_INSETS;
    private static final Rectangle SCREEN_BOUNDS;

    static {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = environment.getScreenDevices();

        Area screenArea = new Area();
        Rectangle screenBounds = new Rectangle();
        List<Rectangle> screensList = new ArrayList<>();
        List<Insets> insetsList = new ArrayList<>();
        List<Rectangle> screensWithInsets = new ArrayList<>();

        for (GraphicsDevice device : screenDevices) {
            GraphicsConfiguration config = device.getDefaultConfiguration();
            Rectangle deviceBounds = config.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
            Rectangle boundsWithInsets = getBoundsWithInsets(deviceBounds, insets);

            screensList.add(deviceBounds);
            insetsList.add(insets);
            screensWithInsets.add(boundsWithInsets);
            screenArea.add(new Area(boundsWithInsets));
            screenBounds = screenBounds.union(deviceBounds);
        }

        SCREEN_AREA = screenArea;
        SCREEN_BOUNDS = screenBounds;
        SCREENS = screensList.toArray(new Rectangle[0]);
        INSETS = insetsList.toArray(new Insets[0]);
        SCREENS_WITH_INSETS = screensWithInsets.toArray(new Rectangle[0]);
    }

    private static Rectangle getBoundsWithInsets(Rectangle bounds, Insets insets) {
        return new Rectangle(
                bounds.x + insets.left, bounds.y + insets.top,
                bounds.width - insets.right, bounds.height - insets.bottom
        );
    }

    private ScreenUtils() {
        // Hide constructor
    }

    public static Rectangle containsInScreenBounds(final Component invoker, final Rectangle rect) {
        Rectangle screenBounds = getScreenBounds(invoker);
        Point p = adjustPointWithinBounds(rect.getLocation(), rect.getSize(), screenBounds);
        return new Rectangle(p, rect.getSize());
    }

    private static Point adjustPointWithinBounds(Point p, Dimension size, Rectangle bounds) {
        if (p.x + size.width > bounds.x + bounds.width) {
            p.x = bounds.x + bounds.width - size.width;
        }
        if (p.y + size.height > bounds.y + bounds.height) {
            p.y = bounds.y + bounds.height - size.height;
        }
        if (p.x < bounds.x) {
            p.x = bounds.x;
        }
        if (p.y < bounds.y) {
            p.y = bounds.y;
        }
        return p;
    }

    public static Rectangle overlapWithScreenBounds(final Component invoker, final Rectangle rect) {
        Rectangle screenBounds = getScreenBounds(invoker);
        Point p = adjustPointForOverlap(rect.getLocation(), rect.getSize(), screenBounds);
        return new Rectangle(p, rect.getSize());
    }

    private static Point adjustPointForOverlap(Point p, Dimension size, Rectangle bounds) {
        if (p.x > bounds.x + bounds.width) {
            p.x = bounds.x + bounds.width - size.width;
        }
        if (p.y > bounds.y + bounds.height) {
            p.y = bounds.y + bounds.height - size.height;
        }
        if (p.x + size.width < bounds.x) {
            p.x = bounds.x;
        }
        if (p.y + size.height < bounds.y) {
            p.y = bounds.y;
        }
        return p;
    }

    public static Dimension getScreenSize(final Component invoker) {
        Dimension screenSize = SCREEN_BOUNDS.getSize();
        if (invoker != null && !(invoker instanceof JApplet) && invoker.getGraphicsConfiguration() != null) {
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(invoker.getGraphicsConfiguration());
            screenSize.width -= insets.left + insets.right;
            screenSize.height -= insets.top + insets.bottom;
        }
        return screenSize;
    }

    public static Dimension getLocalScreenSize(final Component invoker) {
        if (invoker != null && !(invoker instanceof JApplet) && invoker.getGraphicsConfiguration() != null) {
            GraphicsConfiguration gc = invoker.getGraphicsConfiguration();
            Rectangle bounds = gc.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
            return bounds.getSize();
        } else {
            return getScreenSize(invoker);
        }
    }

    public static Rectangle getScreenBounds(final Component invoker) {
        Rectangle bounds = (Rectangle) SCREEN_BOUNDS.clone();
        if (invoker != null && !(invoker instanceof JApplet) && invoker.getGraphicsConfiguration() != null) {
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(invoker.getGraphicsConfiguration());
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
        }
        return bounds;
    }

    public static Rectangle getLocalScreenBounds() {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return e.getMaximumWindowBounds();
    }

    public static Rectangle ensureVisible(final Component invoker, final Rectangle bounds) {
        Rectangle mainScreenBounds = getLocalScreenBounds();
        if (!mainScreenBounds.contains(bounds.getLocation())) {
            Rectangle screenBounds = getScreenBounds(invoker);
            adjustBoundsToEnsureVisibility(bounds, mainScreenBounds, screenBounds);
        }
        return bounds;
    }

    private static void adjustBoundsToEnsureVisibility(Rectangle bounds, Rectangle mainScreenBounds, Rectangle screenBounds) {
        if (bounds.x > screenBounds.x + screenBounds.width || bounds.x < screenBounds.x) {
            bounds.x = mainScreenBounds.x;
        }
        if (bounds.y > screenBounds.y + screenBounds.height || bounds.y < screenBounds.y) {
            bounds.y = mainScreenBounds.y;
        }
    }

    public static Rectangle ensureOnScreen(final Rectangle rect) {
        Rectangle localScreenBounds = getLocalScreenBounds();
        if (localScreenBounds.contains(rect)) {
            return rect;
        }

        Rectangle containingScreen = findContainingScreen(rect);
        if (containingScreen == null) {
            centerRectOnFirstScreen(rect);
        } else {
            adjustRectToFitScreen(rect, containingScreen);
        }
        return rect;
    }

    private static Rectangle findContainingScreen(Rectangle rect) {
        for (Rectangle screenBounds : SCREENS_WITH_INSETS) {
            if (screenBounds.contains(rect.getLocation()) || screenBounds.intersects(rect)) {
                return screenBounds;
            }
        }
        return null;
    }

    private static void centerRectOnFirstScreen(Rectangle rect) {
        rect.x = (SCREENS_WITH_INSETS[0].width - rect.width) / 2;
        rect.y = (SCREENS_WITH_INSETS[0].width - rect.width) / 2;
    }

    private static void adjustRectToFitScreen(Rectangle rect, Rectangle screen) {
        int rectRight = rect.x + rect.width;
        int screenRight = screen.x + screen.width;
        if (rectRight > screenRight) {
            rect.x = screenRight - rect.width;
        }
        if (rect.x < screen.x) {
            rect.x = screen.x;
        }

        int rectBottom = rect.y + rect.height;
        int screenBottom = screen.y + screen.height;
        if (rectBottom > screenBottom) {
            rect.y = screenBottom - rect.height;
        }
        if (rect.y < screen.y) {
            rect.y = screen.y;
        }
    }

    public static Rectangle getContainingScreenBounds(final Rectangle rect, final boolean considerInsets) {
        Rectangle containingScreen = findContainingScreen(rect);
        if (containingScreen == null) {
            containingScreen = SCREENS[0];
        }

        Rectangle bounds = new Rectangle(containingScreen);
        if (considerInsets) {
            Insets insets = getInsetsForScreen(containingScreen);
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
        }
        return bounds;
    }

    private static Insets getInsetsForScreen(Rectangle screen) {
        for (int i = 0; i < SCREENS.length; i++) {
            if (SCREENS[i].equals(screen)) {
                return INSETS[i];
            }
        }
        return new Insets(0, 0, 0, 0); // Default to no insets if not found
    }

    public static Area getScreenArea() {
        return SCREEN_AREA;
    }
}
