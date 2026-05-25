package personagem;

import java.awt.Color;
import java.awt.Graphics2D;

public class Jogador {

    // Posição
    private int x, y;

    // Velocidades
    public static final int SPEED_TERRA = 4;
    public static final int SPEED_AGUA  = 2;

    public double velocidadeY = 0;
    public double gravidade   = 0.4;

    // Estado
    public boolean noChao = false;
    public boolean naAgua = false;

    // Visual
    public final int tileSize;
    private static final int ALTURA_VISUAL = 34;

    // Animação de caminhada
    private int contadorAnim = 0;
    private int frameAnim    = 0;   // 0 = parado, 1 = passo A, 2 = passo B
    private boolean movendoAntes = false;

    public Jogador(int x, int y, int tileSize) {
        this.x = x;
        this.y = y;
        this.tileSize = tileSize;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    /** Atualiza animação — chamar uma vez por frame. */
    public void updateAnimacao(boolean movendo) {
        if (movendo && !naAgua) {
            contadorAnim++;
            if (contadorAnim > 8) {
                contadorAnim = 0;
                frameAnim = (frameAnim == 1) ? 2 : 1;
            }
        } else {
            frameAnim = 0;
            contadorAnim = 0;
        }
        movendoAntes = movendo;
    }

    public void draw(Graphics2D g2, boolean gameOver) {
        int px = x;
        int py = y;
        int t  = tileSize;
        int offsetY = t - ALTURA_VISUAL;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval(px + 4, py + t - 4, 24, 6);

        // Pernas com animação de caminhada
        g2.setColor(new Color(20, 20, 20));
        if (naAgua) {
            // Pernas flutuando abertas
            g2.drawLine(px + 12, py + offsetY + 28, px + 6,  py + offsetY + 34);
            g2.drawLine(px + 20, py + offsetY + 28, px + 26, py + offsetY + 34);
        } else if (frameAnim == 1) {
            // Passo A
            g2.drawLine(px + 12, py + offsetY + 28, px + 8,  py + offsetY + 36);
            g2.drawLine(px + 20, py + offsetY + 28, px + 22, py + offsetY + 34);
        } else if (frameAnim == 2) {
            // Passo B
            g2.drawLine(px + 12, py + offsetY + 28, px + 14, py + offsetY + 34);
            g2.drawLine(px + 20, py + offsetY + 28, px + 24, py + offsetY + 36);
        } else {
            // Parado
            g2.drawLine(px + 12, py + offsetY + 28, px + 12, py + offsetY + 34);
            g2.drawLine(px + 20, py + offsetY + 28, px + 20, py + offsetY + 34);
        }

        // Corpo
        g2.setColor(new Color(30, 90, 160));
        g2.fillRoundRect(px + 3, py + offsetY + 10, 26, 18, 10, 10);

        // Cabeça
        g2.setColor(new Color(255, 220, 180));
        g2.fillOval(px + 6, py + offsetY - 6, 20, 20);

        // Olhos
        g2.setColor(Color.BLACK);
        g2.fillOval(px + 11, py + offsetY + 2, 3, 4);
        g2.fillOval(px + 19, py + offsetY + 2, 3, 4);

        // Boca expressiva
        if (gameOver) {
            // Triste
            g2.drawArc(px + 11, py + offsetY + 9, 10, 5, 0, -180);
        } else if (naAgua) {
            // Assustado (círculo)
            g2.drawOval(px + 14, py + offsetY + 8, 5, 5);
        } else {
            // Normal
            g2.drawLine(px + 12, py + offsetY + 11, px + 20, py + offsetY + 11);
        }

        // Braços
        g2.setColor(new Color(255, 220, 180));
        if (naAgua) {
            g2.drawLine(px + 3,  py + offsetY + 16, px - 6,  py + offsetY + 10);
            g2.drawLine(px + 29, py + offsetY + 16, px + 38, py + offsetY + 10);
        } else {
            g2.drawLine(px + 3,  py + offsetY + 16, px - 2,  py + offsetY + 18);
            g2.drawLine(px + 29, py + offsetY + 16, px + 34, py + offsetY + 18);
        }
    }
}
