import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class App{
    public static void main(String[] args) {
        int BHeight = 800;
        int BWidth = 1200;

        JFrame frame = new JFrame("Snake");
        SnakeGame snakeGame = new SnakeGame(BHeight, BWidth);
        frame.add(snakeGame);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        snakeGame.requestFocus();
    }
}