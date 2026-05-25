package objetos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class SpotPlantio {
    private final int x;
    private final int y;
    private boolean arvorePlantada;
    
    public SpotPlantio(int x, int y) {
        this.x = x;
        this.y = y;
        this.arvorePlantada = false;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getCentroX() {
        return x + 16; // centro do tronco
    }
    
    public int getCentroY() {
        return y - 20; // centro da copa
    }
    
    public boolean isArvorePlantada() {
        return arvorePlantada;
    }
    
    public void plantarArvore() {
        this.arvorePlantada = true;
    }
    
    public void reset() {
        this.arvorePlantada = false;
    }
    
    public boolean isJogadorProximo(int jogadorCentroX, int jogadorYBase, int distanciaX, int distanciaY) {
        int dx = Math.abs(jogadorCentroX - (x + 16));
        int dy = Math.abs(jogadorYBase - y);
        return dx < distanciaX && dy < distanciaY;
    }
    
    public boolean isSombraProjetada(int jogadorCentroX, int jogadorCentroY, int raioSombra) {
        int ax = getCentroX();
        int ay = getCentroY();
        int dx = jogadorCentroX - ax;
        int dy = jogadorCentroY - ay;
        return (dx * dx + dy * dy) < raioSombra * raioSombra;
    }
    
    public void draw(Graphics2D g2, boolean isPerto, long time) {
        if (!arvorePlantada) {
            // circulo indicador
            g2.setColor(isPerto ? new Color(100, 255, 100, 120) : new Color(200, 255, 100, 50));
            g2.fillOval(x - 10, y - 10, 50, 20);
            
            // ícone de muda
            g2.setColor(new Color(120, 200, 80));
            g2.fillRect(x + 14, y - 16, 4, 16);
            g2.setColor(new Color(80, 160, 60));
            g2.fillOval(x + 6, y - 26, 20, 16);
            
            if (isPerto) {
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
                g2.drawString("[E] Plantar", x - 8, y - 32);
            }
        } else {
            drawArvore(g2, time);
        }
    }
    
    private void drawArvore(Graphics2D g2, long time) {
        int balanco = (int)(Math.sin(time * 0.000000004 + x * 0.01) * 2);
        
        // Sombra no chão
        g2.setColor(new Color(0, 80, 0, 40));
        g2.fillOval(x - 30, y - 5, 90, 20);
        
        // Tronco
        g2.setColor(new Color(120, 70, 30));
        g2.fillRoundRect(x + 12, y - 40, 8, 40, 4, 4);
        
        // Copa (3 camadas para volume)
        g2.setColor(new Color(30, 110, 30));
        g2.fillOval(x - 15 + balanco, y - 95, 62, 60);
        g2.setColor(new Color(50, 150, 50));
        g2.fillOval(x - 8 + balanco, y - 105, 50, 55);
        g2.setColor(new Color(80, 180, 60));
        g2.fillOval(x + balanco, y - 108, 32, 38);
    }
    
    public void drawSombraHalo(Graphics2D g2, int raioSombra) {
        if (arvorePlantada) {
            g2.setColor(new Color(100, 220, 120, 25));
            g2.fillOval(x - raioSombra + 16, y - raioSombra - 20,
                       raioSombra * 2, raioSombra * 2);
        }
    }
}