
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Pong extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;

    private static final int PADDLE_WIDTH = 12;
    private static final int PADDLE_HEIGHT = 90;

    private static final int WIN_SCORE = 5;

    private double ballX = WIDTH / 2.0;
    private double ballY = HEIGHT / 2.0;

    private double ballVelocityX = 5;
    private double ballVelocityY = 3;

    private int leftPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private int rightPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;

    private int player1Score = 0;
    private int player2Score = 0;

    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    private boolean gameOver = false;

    private final Timer gameTimer;


    private int hitCount = 0;
    private int speedLevel = 1;

    private static final double SPEED_MULTIPLIER = 1.15;
    private static final double MAX_HORIZONTAL_SPEED = 10;
    private static final double MAX_VERTICAL_SPEED = 7;

    public Pong() {

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        setupControls();

        gameTimer = new Timer(16, e -> {
            updateGame();
            repaint();
        });

        gameTimer.start();
    }

    private void setupControls() {

        bindKey("W", KeyEvent.VK_W, () -> wPressed = true);
        bindKey("S", KeyEvent.VK_S, () -> sPressed = true);

        bindKey("UP", KeyEvent.VK_UP, () -> upPressed = true);
        bindKey("DOWN", KeyEvent.VK_DOWN, () -> downPressed = true);

        bindKeyRelease("W_RELEASE", KeyEvent.VK_W,
                () -> wPressed = false);

        bindKeyRelease("S_RELEASE", KeyEvent.VK_S,
                () -> sPressed = false);

        bindKeyRelease("UP_RELEASE", KeyEvent.VK_UP,
                () -> upPressed = false);

        bindKeyRelease("DOWN_RELEASE", KeyEvent.VK_DOWN,
                () -> downPressed = false);

        bindKey("RESTART", KeyEvent.VK_SPACE, this::restartGame);
    }

    private void bindKey(String name, int keyCode, Runnable action) {

        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, false), name);

        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void bindKeyRelease(String name, int keyCode, Runnable action) {

        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, true), name);

        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void updateGame() {

        if (gameOver) {
            return;
        }

        // Player movement
        if (wPressed) {
            leftPaddleY -= 7;
        }

        if (sPressed) {
            leftPaddleY += 7;
        }

        if (upPressed) {
            rightPaddleY -= 7;
        }

        if (downPressed) {
            rightPaddleY += 7;
        }

        // Keep paddles inside the arena
        leftPaddleY = Math.max(0,
                Math.min(HEIGHT - PADDLE_HEIGHT, leftPaddleY));

        rightPaddleY = Math.max(0,
                Math.min(HEIGHT - PADDLE_HEIGHT, rightPaddleY));

        // Move ball
        ballX += ballVelocityX;
        ballY += ballVelocityY;

        // Top and bottom collision
        if (ballY <= 0 || ballY >= HEIGHT - 12) {
            ballVelocityY *= -1;
        }

        // Left paddle collision
        if (ballVelocityX < 0
                && ballX <= 30
                && ballX >= 20
                && ballY + 10 >= leftPaddleY
                && ballY <= leftPaddleY + PADDLE_HEIGHT) {

            ballVelocityX = Math.abs(ballVelocityX);

            registerHit();
        }

        // Right paddle collision
        if (ballVelocityX > 0
                && ballX + 10 >= WIDTH - 30
                && ballX <= WIDTH - 20
                && ballY + 10 >= rightPaddleY
                && ballY <= rightPaddleY + PADDLE_HEIGHT) {

            ballVelocityX = -Math.abs(ballVelocityX);

            registerHit();
        }

        // Player 2 scores
        if (ballX < 0) {
            player2Score++;
            checkWinner();
            resetBall();
        }

        // Player 1 scores
        if (ballX > WIDTH) {
            player1Score++;
            checkWinner();
            resetBall();
        }
    }

    private void checkWinner() {

        if (player1Score >= WIN_SCORE || player2Score >= WIN_SCORE) {
            gameOver = true;
            gameTimer.stop();
        }
    }

    private void resetBall() {

        ballX = WIDTH / 2.0;
        ballY = HEIGHT / 2.0;

        hitCount = 0;
        speedLevel = 1;

        ballVelocityX = Math.random() < 0.5 ? 5 : -5;
        ballVelocityY = Math.random() < 0.5 ? 3 : -3;
    }

    private void restartGame() {

        player1Score = 0;
        player2Score = 0;

        leftPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
        rightPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;

        gameOver = false;

        resetBall();

        gameTimer.start();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(Color.WHITE);

        // Center divider
        for (int y = 0; y < HEIGHT; y += 25) {
            g2.fillRect(WIDTH / 2 - 2, y, 4, 12);
        }

        // Paddles
        g2.fillRect(20, leftPaddleY,
                PADDLE_WIDTH, PADDLE_HEIGHT);

        g2.fillRect(WIDTH - 32, rightPaddleY,
                PADDLE_WIDTH, PADDLE_HEIGHT);

        // Ball
        g2.fillOval((int) ballX, (int) ballY, 12, 12);

        // Score
        g2.setFont(new Font("Monospaced", Font.BOLD, 48));

        g2.drawString(String.valueOf(player1Score),
                WIDTH / 2 - 100, 60);

        g2.drawString(String.valueOf(player2Score),
                WIDTH / 2 + 65, 60);

        // Instructions
        g2.setFont(new Font("Monospaced", Font.PLAIN, 14));

        g2.drawString("P1: W / S", 20, HEIGHT - 15);
        g2.drawString("P2: UP / DOWN", WIDTH - 150, HEIGHT - 15);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));

        String hitsText = "HITS: " + hitCount;
        String speedText = "SPEED: " + speedLevel;

        g2.drawString(hitsText,
            (WIDTH - g2.getFontMetrics().stringWidth(hitsText)) / 2,
            90);

        g2.drawString(speedText,
            (WIDTH - g2.getFontMetrics().stringWidth(speedText)) / 2,
            115);

        if (gameOver) {

            String winner = player1Score >= WIN_SCORE
                    ? "PLAYER 1 WINS!"
                    : "PLAYER 2 WINS!";

            g2.setFont(new Font("Monospaced", Font.BOLD, 36));

            FontMetrics fm = g2.getFontMetrics();

            int textX = (WIDTH - fm.stringWidth(winner)) / 2;

            g2.drawString(winner, textX, HEIGHT / 2);

            g2.setFont(new Font("Monospaced", Font.PLAIN, 18));

            String restart = "PRESS SPACE TO RESTART";

            int restartX = (WIDTH - g2.getFontMetrics()
                    .stringWidth(restart)) / 2;

            g2.drawString(restart, restartX, HEIGHT / 2 + 40);
        }
    }

    private void registerHit() {

        hitCount++;

        if (hitCount % 2 == 0) {

            speedLevel++;

            ballVelocityX = Math.copySign(
                    Math.min(Math.abs(ballVelocityX) * SPEED_MULTIPLIER,
                            MAX_HORIZONTAL_SPEED),
                    ballVelocityX
            );

            ballVelocityY = Math.copySign(
                    Math.min(Math.abs(ballVelocityY) * SPEED_MULTIPLIER,
                            MAX_VERTICAL_SPEED),
                    ballVelocityY
            );

            System.out.println("BALL SPEED INCREASED!");
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("PONG - Java Edition");

            Pong game = new Pong();

            frame.add(game);
            frame.pack();

            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            game.requestFocusInWindow();
        });
    }
}
