import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Minimal replacement for the old JDK "appletviewer" tool.
 *
 * Usage: java AppletRunner [options] ClassName
 *   --classpath DIR       where the compiled applet classes live (default: .)
 *   --width N             applet width  (default: 400)
 *   --height N            applet height (default: 300)
 *   --param NAME=VALUE    applet parameter, readable via getParameter(NAME); repeatable
 *   --screenshot FILE     render once to a PNG file and exit instead of opening a window
 *   --delay MS            wait before taking the screenshot (default: 500)
 */
@SuppressWarnings("removal")
public class AppletRunner {

    public static void main(String[] args) throws Exception {
        String classpath = ".";
        int width = 400;
        int height = 300;
        String screenshot = null;
        long delay = 500;
        Map<String, String> params = new HashMap<>();
        String className = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--classpath": classpath = args[++i]; break;
                case "--width": width = Integer.parseInt(args[++i]); break;
                case "--height": height = Integer.parseInt(args[++i]); break;
                case "--screenshot": screenshot = args[++i]; break;
                case "--delay": delay = Long.parseLong(args[++i]); break;
                case "--param": {
                    String[] kv = args[++i].split("=", 2);
                    params.put(kv[0].toLowerCase(), kv.length > 1 ? kv[1] : "");
                    break;
                }
                default: className = args[i];
            }
        }
        if (className == null) {
            System.err.println("Usage: java AppletRunner [--classpath DIR] [--width N] [--height N]"
                    + " [--param NAME=VALUE] [--screenshot FILE] ClassName");
            System.exit(2);
        }

        URL codeBase = new File(classpath).getAbsoluteFile().toURI().toURL();
        ClassLoader loader = new URLClassLoader(new URL[] {codeBase}, AppletRunner.class.getClassLoader());
        Class<?> cls = Class.forName(className, true, loader);
        if (!Applet.class.isAssignableFrom(cls)) {
            System.err.println(className + " does not extend java.applet.Applet");
            System.exit(1);
        }
        Applet applet = (Applet) cls.getDeclaredConstructor().newInstance();
        applet.setStub(new Stub(applet, codeBase, params));

        final int w = width, h = height;
        Frame frame = new Frame(className);
        SwingUtilities.invokeAndWait(() -> {
            frame.add(applet);
            applet.setPreferredSize(new java.awt.Dimension(w, h));
            frame.pack();
            frame.setLocationByPlatform(true);
            applet.init();
            frame.setVisible(true);
            applet.start();
        });

        if (screenshot != null) {
            Thread.sleep(delay);
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            SwingUtilities.invokeAndWait(() -> {
                Graphics2D g = img.createGraphics();
                g.setColor(applet.getBackground() != null ? applet.getBackground() : java.awt.Color.WHITE);
                g.fillRect(0, 0, w, h);
                g.setColor(applet.getForeground() != null ? applet.getForeground() : java.awt.Color.BLACK);
                g.setFont(applet.getFont());
                applet.printAll(g);
                g.dispose();
                applet.stop();
                applet.destroy();
                frame.dispose();
            });
            ImageIO.write(img, "png", new File(screenshot));
            System.out.println("Saved screenshot: " + screenshot);
            System.exit(0);
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                applet.stop();
                applet.destroy();
                frame.dispose();
                System.exit(0);
            }
        });
    }

    /** Supplies the applet with its parameters, code base and context. */
    static class Stub implements AppletStub, AppletContext {
        private final Applet applet;
        private final URL codeBase;
        private final Map<String, String> params;

        Stub(Applet applet, URL codeBase, Map<String, String> params) {
            this.applet = applet;
            this.codeBase = codeBase;
            this.params = params;
        }

        @Override public boolean isActive() { return true; }
        @Override public URL getDocumentBase() { return codeBase; }
        @Override public URL getCodeBase() { return codeBase; }
        @Override public String getParameter(String name) { return params.get(name.toLowerCase()); }
        @Override public AppletContext getAppletContext() { return this; }
        @Override public void appletResize(int width, int height) { applet.setSize(width, height); }

        @Override public AudioClip getAudioClip(URL url) { return null; }
        @Override public Image getImage(URL url) { return Toolkit.getDefaultToolkit().getImage(url); }
        @Override public Applet getApplet(String name) { return null; }
        @Override public Enumeration<Applet> getApplets() { return Collections.enumeration(Collections.singletonList(applet)); }
        @Override public void showDocument(URL url) { System.out.println("[showDocument] " + url); }
        @Override public void showDocument(URL url, String target) { System.out.println("[showDocument] " + url + " (" + target + ")"); }
        @Override public void showStatus(String status) { System.out.println("[status] " + status); }
        @Override public void setStream(String key, InputStream stream) { }
        @Override public InputStream getStream(String key) { return null; }
        @Override public Iterator<String> getStreamKeys() { return Collections.emptyIterator(); }
    }
}
