package app.presentation.views;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class App extends JFrame {

    public App(String title, Dimension size, Color bgColor) {
        initApp(title, size, bgColor);
    }

    private void initApp(String title, Dimension size, Color bgColor) {
        this.setSize(size);
        this.setTitle(title);
        this.setDefaultCloseOperation(App.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(bgColor);
        this.setResizable(false);
    }

    public void setBackgroundColor(Color color) {
        this.getContentPane().setBackground(color);
    }

    public void setAppIcon(ImageIcon icon) {
        this.setIconImage(icon.getImage());
    }

    public void setLocation(Dimension screenSize) {
        int centerX = screenSize.width / 2;
        int centerY = screenSize.height / 2;

        this.setLocation(centerX - this.getWidth() / 2, centerY - this.getHeight() / 2);
    }

    public void setVisiblity(boolean isVisible) {
        this.setVisible(isVisible);
    }

    public void refresh() {
        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }
}