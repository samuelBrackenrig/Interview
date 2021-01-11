import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jfree.chart.JFreeChart;

public class GUI extends JPanel {

    private JFrame jframe;
    private JButton button;
    private JLabel label;
    private JPanel panel;
    private JFreeChart chart;

    public GUI(){

        jframe = new JFrame();
        button = new JButton("Test");

        label = new JLabel("Something");

        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.setLayout(new GridLayout(0, 1));
        panel.add(button);
        panel.add(label);

        jframe.add(panel, BorderLayout.CENTER);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setTitle("Test");
        jframe.pack();
        jframe.setVisible(true);

    }

}
