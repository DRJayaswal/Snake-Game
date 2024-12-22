import java.awt.*;
import java.util.ArrayList;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;


public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile{
        int x, y;
        Tile(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
    int BHeight, BWidth;
    int TSize = 25;

    Tile snakeHead;
    Tile food;
    ArrayList<Tile> obstacles;
    Random random;
    Timer gameLoop;
    int XVelocity;
    int YVelocity;
    ArrayList<Tile> snakeBody;
    int initialDelay = 200;
    int delayDecrease = 10;
    int minDelay = 50;
    int score;
    boolean gameOver;
    boolean paused;
    int highScore;

    SnakeGame(int BHeight, int BWidth){
        this.BHeight = BHeight;
        this.BWidth = BWidth;
        setPreferredSize(new Dimension(this.BWidth, this.BHeight));
        setBackground(Color.BLACK);

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow(); // Ensure the panel requests focus

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();
        snakeBody.add(new Tile(snakeHead.x, snakeHead.y));
        snakeBody.add(new Tile(snakeHead.x + 1, snakeHead.y)); // Add initial body part

        food = new Tile(6, 6);
        obstacles = new ArrayList<>();

        random = new Random();
        placeFood();
        placeObstacles();

        gameLoop = new Timer(initialDelay, this);
        gameLoop.start();

        XVelocity = -1;
        YVelocity = 0;
        score = 0;
        gameOver = false;
        paused = false;
        highScore = 0;
    }

    public void placeFood(){
        food.x = 1 + random.nextInt((BWidth/TSize) - 2);
        food.y = 1 + random.nextInt((BHeight/TSize) - 2);
    }

    public void placeObstacles() {
        obstacles.clear();
        for (int i = 0; i < 5; i++) {
            int x = 1 + random.nextInt((BWidth / TSize) - 2);
            int y = 1 + random.nextInt((BHeight / TSize) - 2);
            obstacles.add(new Tile(x, y));
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    void draw(Graphics g){
        for(int i = 0; i < BWidth / TSize; i++){
            g.drawLine(i * TSize, 0, i * TSize, BHeight);
            g.drawLine(0, i * TSize, BWidth, i * TSize);
        }

        // Food
        g.setColor(Color.RED);
        g.fillOval(food.x * TSize, food.y * TSize, TSize, TSize);
        // Food Border
        g.setColor(Color.ORANGE);
        g.drawOval(food.x * TSize, food.y * TSize, TSize, TSize);

        // Obstacles
        g.setColor(Color.GRAY);
        for (Tile obstacle : obstacles) {
            g.fillRect(obstacle.x * TSize, obstacle.y * TSize, TSize, TSize);
        }

        // Snake
        for (int i = 0; i < snakeBody.size(); i++) {
            int size = TSize - (i * 2); // Decrease size towards the tail
            if (size < 5) size = 5; // Minimum size for the tail
            if (i == 0) {
                g.setColor(Color.GREEN); // Head
            } else {
                g.setColor(Color.DARK_GRAY); // Body
            }
            g.fillRect(snakeBody.get(i).x * TSize, snakeBody.get(i).y * TSize, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(snakeBody.get(i).x * TSize, snakeBody.get(i).y * TSize, size, size);
        }

        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 20);

        // Draw high score in golden color
        g.setColor(new Color(255, 215, 0)); // Golden color
        g.drawString("High Score: " + highScore, 10, 50);

        // Draw game over message
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("Game Over", BWidth / 2 - 150, BHeight / 2);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Press R to Restart", BWidth / 2 - 100, BHeight / 2 + 50);
        }

        // Draw paused message
        if (paused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("Paused", BWidth / 2 - 100, BHeight / 2);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Press ESC to Resume", BWidth / 2 - 100, BHeight / 2 + 50);
        }
    }

    public void move(){
        if (gameOver || paused) return;

        // Move the body
        for (int i = snakeBody.size() - 1; i > 0; i--) {
            snakeBody.get(i).x = snakeBody.get(i - 1).x;
            snakeBody.get(i).y = snakeBody.get(i - 1).y;
        }
        // Move the head
        snakeHead.x += XVelocity;
        snakeHead.y += YVelocity;

        // Update the head position in the body list
        snakeBody.get(0).x = snakeHead.x;
        snakeBody.get(0).y = snakeHead.y;

        // Check for collision with food
        if(snakeHead.x == food.x && snakeHead.y == food.y){
            growSnake();
            placeFood();
            increaseSpeed();
            score += 10; // Increase score
        }

        // Check for collision with walls
        if (snakeHead.x < 0 || snakeHead.x >= BWidth / TSize || snakeHead.y < 0 || snakeHead.y >= BHeight / TSize) {
            gameOver = true;
            gameLoop.stop();
            updateHighScore();
        }

        // Check for collision with itself
        for (int i = 1; i < snakeBody.size(); i++) {
            if (snakeHead.x == snakeBody.get(i).x && snakeHead.y == snakeBody.get(i).y) {
                gameOver = true;
                gameLoop.stop();
                updateHighScore();
            }
        }

        // Check for collision with obstacles
        for (Tile obstacle : obstacles) {
            if (snakeHead.x == obstacle.x && snakeHead.y == obstacle.y) {
                gameOver = true;
                gameLoop.stop();
                updateHighScore();
            }
        }
    }

    public void growSnake() {
        Tile newTile = new Tile(snakeBody.get(snakeBody.size() - 1).x, snakeBody.get(snakeBody.size() - 1).y);
        snakeBody.add(newTile);
    }

    public void increaseSpeed() {
        int delay = gameLoop.getDelay();
        if (delay > minDelay) {
            gameLoop.setDelay(delay - delayDecrease);
        }
    }

    public void updateHighScore() {
        if (score > highScore) {
            highScore = score;
        }
    }

    public void restartGame() {
        snakeHead = new Tile(5, 5);
        snakeBody.clear();
        snakeBody.add(new Tile(snakeHead.x, snakeHead.y));
        snakeBody.add(new Tile(snakeHead.x + 1, snakeHead.y));
        placeFood();
        placeObstacles();
        XVelocity = -1;
        YVelocity = 0;
        if (score > highScore) {
            highScore = score;
        }
        score = 0;
        gameOver = false;
        paused = false;
        gameLoop.setDelay(initialDelay);
        gameLoop.start();
    }

    public void togglePause() {
        paused = !paused;
        if (paused) {
            gameLoop.stop();
        } else {
            gameLoop.start();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver) {
            move();
        }
        repaint();
    }

    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (gameOver && key == KeyEvent.VK_R) {
            restartGame();
        }
        if (key == KeyEvent.VK_ESCAPE) { // Change pause button to ESC
            togglePause();
        }
        if(key == KeyEvent.VK_UP && YVelocity != 1){
            XVelocity = 0;
            YVelocity = -1;
        }
        if(key == KeyEvent.VK_DOWN && YVelocity != -1){
            XVelocity = 0;
            YVelocity = 1;
        }
        if(key == KeyEvent.VK_LEFT && XVelocity != 1){
            XVelocity = -1;
            YVelocity = 0;
        }
        if(key == KeyEvent.VK_RIGHT && XVelocity != -1){
            XVelocity = 1;
            YVelocity = 0;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}
