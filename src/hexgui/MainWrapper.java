package hexgui;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/** Wrapper for starting HexGui.
    Loads the main class with the reflection API to set AWT an other
    properties before any AWT class is loaded (otherwise they would be
    ignored). */
public final class MainWrapper
{
    public static void main(String[] args) throws Exception
    {
        // Use GDI rendering on Windows. There are repaint problems using
        // DDraw (last tested with Java 1.6 on Windows 7).
        System.setProperty("sun.java2d.noddraw", "true");

        // Invoke hexgui.Main.main() with reflection API
        Class<?> mainClass = Class.forName("hexgui.Main");
        Class[] argTypes = new Class[] { String[].class };
        Method mainMethod = mainClass.getMethod("main", argTypes);
        mainMethod.invoke(null, (Object)args);
    }

    /** Make constructor unavailable; class is for namespace only. */
    private MainWrapper()
    {
    }
}
