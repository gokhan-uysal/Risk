package app.ui.views.game.player;

import javax.swing.*;

import java.awt.*;

public class PlayerStatePanel extends JPanel {

    private int cornerRadius = 50;
    public JButton nextButton;
    public JLabel nameField;
    public JButton indicator1, indicator2, indicator3;

    public PlayerStatePanel() {
        super();
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.CENTER));
        this.setPreferredSize(new Dimension(0, 150));
        setBackground(Color.CYAN);
        setForeground(Color.black);

        buildPanel();
    }

    public void buildPanel() {
        nextButton = new JButton("Next");
        nameField = new JLabel("Current State Here...");

        this.add(nextButton);
        this.add(nameField);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        g2.setColor(getForeground());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        g2.dispose();
    }
}