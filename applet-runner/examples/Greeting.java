import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/** Shows init() and getParameter(): run with --param name=YourName */
public class Greeting extends Applet {
    private String name;

    @Override
    public void init() {
        name = getParameter("name");
        if (name == null) name = "World";
        setBackground(Color.WHITE);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillOval(20, 20, 60, 60);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Hello, " + name + "!", 100, 55);
    }
}
