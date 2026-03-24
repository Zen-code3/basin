import java.awt.Dimension;
import javax.swing.JFrame;

public final class WindowHelper {

    public static final int FRAME_WIDTH = 800;
    public static final int FRAME_HEIGHT = 600;

    private WindowHelper() {
    }

    /**
     * Sets frame size to 800×600 and centers it on the screen (call after {@code initComponents()}.
     */
    public static void sizeAndCenter(JFrame frame) {
        frame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setLocationRelativeTo(null);
    }
}
