import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

public class DungeonCrawler extends JPanel implements KeyListener {
    private static final int TILE_SIZE = 30;
    private static final int WIDTH = 20;
    private static final int HEIGHT = 10;

    // Resim değişkenleri
    private Image playerImg;
    private Image[] enemyImages = new Image[4];
    private Image wallImg;
    private Image goldImg;
    private Image superGoldImg;
    private Image exitImg;
    private Image backgroundImg;

    private char[][] map;
    private int playerX, playerY;
    private int exitX, exitY;
    private int goldCount = 0;
    private int totalGold = 0;
    private int level = 1;
    private boolean gameOver = false;
    private boolean paused = false;
    private List<Enemy> enemies = new ArrayList<>();
    private Random random;
    private Timer enemyTimer;
    private Timer messageTimer;
    private boolean superPowerActive = false;
    private boolean showInsufficientGoldMessage = false;

    public DungeonCrawler() {
        random = new Random();
        loadImages(); // Resimleri yükle
        initializeGame();
        setPreferredSize(new Dimension(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE + 60));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        enemyTimer = new Timer(500, e -> {
            if (!paused && !gameOver) moveEnemies();
            repaint();
        });
        enemyTimer.start();

        messageTimer = new Timer(5000, e -> {
            showInsufficientGoldMessage = false;
            placeExit();
            repaint();
        });
        messageTimer.setRepeats(false);
    }

    private void loadImages() {
        try {
            // Resimleri yüklerken yolunu düzelt (classpath'e göre)
            playerImg = ImageIO.read(getClass().getResource("/images/player.png"));
            enemyImages[0] = ImageIO.read(getClass().getResource("/images/enemy_1.png"));
            enemyImages[1] = ImageIO.read(getClass().getResource("/images/enemy_2.png"));
            enemyImages[2] = ImageIO.read(getClass().getResource("/images/enemy_3.png"));
            enemyImages[3] = ImageIO.read(getClass().getResource("/images/enemy_4.png"));
            wallImg = ImageIO.read(getClass().getResource("/images/wall.png"));
            goldImg = ImageIO.read(getClass().getResource("/images/gold.png"));
            superGoldImg = ImageIO.read(getClass().getResource("/images/super_gold.png"));
            exitImg = ImageIO.read(getClass().getResource("/images/exit.png"));
            backgroundImg = ImageIO.read(getClass().getResource("/images/background.png"))
                    .getScaledInstance(WIDTH*TILE_SIZE, HEIGHT*TILE_SIZE, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            System.err.println("Resim yüklenemedi: " + e.getMessage());
            System.exit(1);
        }
    }

    private Image loadImage(String path) throws IOException {
        BufferedImage img = ImageIO.read(getClass().getResource(path));
        return img.getScaledInstance(TILE_SIZE, TILE_SIZE, Image.SCALE_SMOOTH);
    }

    private void initializeGame() {
        map = new char[HEIGHT][WIDTH];
        generateMap();
        placePlayer();
        placeExit();
        placeGolds(10);
        placeEnemies(level + 1);
        goldCount = 0;
        superPowerActive = false;
    }

    private void generateMap() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                map[y][x] = (x == 0 || x == WIDTH-1 || y == 0 || y == HEIGHT-1) ? '#' : '.';
            }
        }
    }

    private void placePlayer() {
        int attempts = 0;
        do {
            playerX = random.nextInt(WIDTH-2) + 1;
            playerY = random.nextInt(HEIGHT-2) + 1;
            attempts++;
            if (attempts > 1000) break;
        } while (map[playerY][playerX] != '.' || checkCollision(playerX, playerY));
        map[playerY][playerX] = '@';
    }

    private void placeExit() {
        int attempts = 0;
        while (attempts < 10000) { // 10.000 deneme
            exitX = random.nextInt(WIDTH-2) + 1;
            exitY = random.nextInt(HEIGHT-2) + 1;
            if (map[exitY][exitX] == '.' && !checkCollision(exitX, exitY)) {
                map[exitY][exitX] = '>';
                return;
            }
            attempts++;
        }
        // Eğer 10.000 denemede de yer bulunamazsa zorunlu yerleştir
        exitX = 1; exitY = 1;
        map[exitY][exitX] = '>';
    }

    private void placeGolds(int count) {
        for (int i = 0; i < count; i++) {
            int attempts = 0;
            int x, y;
            do {
                x = random.nextInt(WIDTH-2) + 1;
                y = random.nextInt(HEIGHT-2) + 1;
                attempts++;
                if (attempts > 1000) break;
            } while (map[y][x] != '.' || checkCollision(x, y));
            map[y][x] = (level % 10 == 0 && i == 0) ? 'S' : '$';
        }
    }

    private void placeEnemies(int count) {
        enemies.clear();
        for (int i = 0; i < count; i++) {
            int attempts = 0;
            int x, y;
            do {
                x = random.nextInt(WIDTH-2) + 1;
                y = random.nextInt(HEIGHT-2) + 1;
                attempts++;
                if (attempts > 1000) break;
            } while (map[y][x] != '.' || checkCollision(x, y));

            // Rastgele düşman resmi seç
            int imageIndex = random.nextInt(enemyImages.length);
            enemies.add(new Enemy(x, y, imageIndex));
            map[y][x] = 'E';
        }
    }

    private boolean checkCollision(int x, int y) {
        for (Enemy enemy : enemies) {
            if (enemy.x == x && enemy.y == y) return true;
        }
        return (x == exitX && y == exitY) || (x == playerX && y == playerY);
    }

    private void moveEnemies() {
        for (Enemy enemy : enemies) {
            int dx = random.nextInt(3) - 1;
            int dy = random.nextInt(3) - 1;
            int newX = enemy.x + dx;
            int newY = enemy.y + dy;

            // Yeni pozisyon kontrolüne '>' ekleyin
            if (newX > 0 && newX < WIDTH-1 && newY > 0 && newY < HEIGHT-1 &&
                    map[newY][newX] != '>' &&
                    (map[newY][newX] == '.' || (superPowerActive && map[newY][newX] == '@')))
            {
                map[enemy.y][enemy.x] = '.';
                enemy.x = newX;
                enemy.y = newY;
                map[newY][newX] = 'E';
            }
        }
        checkCollisions();
    }

    private void checkCollisions() {
        if (gameOver) return;

        for (Enemy enemy : enemies) {
            if (enemy.x == playerX && enemy.y == playerY && !superPowerActive) {
                gameOver = true;
                enemyTimer.stop();
                repaint();
                return;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Arka plan
        g.drawImage(backgroundImg, 0, 0, this);

        // Haritayı çiz
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int pixelX = x * TILE_SIZE;
                int pixelY = y * TILE_SIZE;

                // Resimleri çiz
                switch (map[y][x]) {
                    case '#':
                        g.drawImage(wallImg, pixelX, pixelY, TILE_SIZE, TILE_SIZE, this);
                        break;
                    case '@':
                        g.drawImage(playerImg, pixelX, pixelY, TILE_SIZE, TILE_SIZE, this);
                        break;
                    case '$':
                        g.drawImage(goldImg, pixelX, pixelY, TILE_SIZE, TILE_SIZE, this);
                        break;
                    case 'E':
                        Enemy enemy = getEnemyAt(x, y);
                        if (enemy != null) {
                            g.drawImage(enemyImages[enemy.imageIndex], pixelX, pixelY, TILE_SIZE, TILE_SIZE, this);
                        }
                        break;
                    case '>':
                        g.drawImage(exitImg, pixelX, pixelY, TILE_SIZE, TILE_SIZE, this);
                        break;
                    case 'S':
                        g.drawImage(superGoldImg, pixelX, pixelY, TILE_SIZE, TILE_SIZE, this);
                        break;
                }
            }
        }

        // UI Metinleri
        g.setColor(Color.WHITE);
        g.drawString("Seviye: " + level, 10, HEIGHT * TILE_SIZE + 20);
        g.drawString("Altın: " + goldCount + "/" + getGoldRequirement(), 10, HEIGHT * TILE_SIZE + 40);
        g.drawString("Toplam Altın: " + totalGold, 10, HEIGHT * TILE_SIZE + 60);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Nasıl Oynanır: WASD/Yön Tuşları, P: Durdur, R: Yeniden Başla", 10, HEIGHT * TILE_SIZE + 80);

        // Mesajlar
        if (showInsufficientGoldMessage) {
            drawCenteredMessage(g, "Önce " + getGoldRequirement() + " Altın Topla!", Color.RED, 24);
        }
        if (paused) {
            drawCenteredMessage(g, "PAUSED", Color.CYAN, 24);
        }
        if (gameOver) {
            drawCenteredMessage(g, "OYUN BİTTİ! R ile Yeniden Başla", Color.RED, 36);
        }
        if (level % 10 == 0 && goldCount >= 10) {
            g.setColor(Color.MAGENTA);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("K tuşu ile Süper Güç Al!", 10, HEIGHT * TILE_SIZE + 40);
        }
    }

    private int getGoldRequirement() {
        return level % 10 == 0 ? 10 : 5;
    }
    private void drawCenteredMessage(Graphics g, String message, Color color, int fontSize) {
        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, fontSize));
        int stringWidth = g.getFontMetrics().stringWidth(message);
        g.drawString(message, (WIDTH * TILE_SIZE - stringWidth)/2, HEIGHT * TILE_SIZE / 2);
    }

    private Enemy getEnemyAt(int x, int y) {
        for (Enemy enemy : enemies) {
            if (enemy.x == x && enemy.y == y) return enemy;
        }
        return null;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R) {
            resetGame();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_P) {
            paused = !paused;
            enemyTimer.setDelay(paused ? 0 : 500);
            return;
        }
        if (gameOver || paused) return;

        int newX = playerX;
        int newY = playerY;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W: newY--; break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S: newY++; break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A: newX--; break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D: newX++; break;
            case KeyEvent.VK_K:
                if (level % 10 == 0 && goldCount >= 10) {
                    superPowerActive = true;
                    goldCount -= 10;
                    totalGold -= 10;
                    JOptionPane.showMessageDialog(this, "Süper Güç Aktif!");
                }
                break;
        }

        if (newX >= 0 && newX < WIDTH && newY >= 0 && newY < HEIGHT) {
            char target = map[newY][newX];

            if (target != '#') {
                if (target == '>') {
                    if (goldCount >= getGoldRequirement()) {
                        totalGold += goldCount;
                        level++;
                        initializeGame();
                        JOptionPane.showMessageDialog(this, "Seviye " + level + "! Altın sınırı: " + getGoldRequirement());
                    } else {
                        showInsufficientGoldMessage = true;
                        messageTimer.restart();
                        map[exitY][exitX] = '>';
                        repaint();
                        return;
                    }
                }

                map[playerY][playerX] = '.';
                playerX = newX;
                playerY = newY;

                if (target == '$' || target == 'S') {
                    goldCount++;
                    totalGold++;
                    if (target == 'S') superPowerActive = true;
                    placeGolds(1);
                }

                map[playerY][playerX] = '@';
                repaint();
            }
        }
        checkCollisions();
    }

    private void resetGame() {
        gameOver = false;
        paused = false;
        level = 1;
        totalGold = 0;
        goldCount = 0;
        superPowerActive = false;
        initializeGame();
        enemyTimer.restart();
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dungeon Crawler");
            DungeonCrawler game = new DungeonCrawler();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }

    private static class Enemy {
        int x, y;
        int imageIndex;
        Enemy(int x, int y, int imageIndex) {
            this.x = x;
            this.y = y;
            this.imageIndex = imageIndex;
        }
    }
}