package objetos;

import java.awt.Color;
import java.awt.Graphics2D;

public class Bueiro extends Objeto {

    private static final int LARGURA = 30;
    private static final int ALTURA  = 15;
    private static final int RAIO_INTERACAO = 50;

    public boolean desentupido = false;

    public Bueiro(int x, int y) {
        super(x, y, LARGURA, ALTURA);
    }

    public boolean jogadorPerto(int jogX, int jogY) {
        return Math.abs(jogX - x) < RAIO_INTERACAO
            && Math.abs(jogY - y) < RAIO_INTERACAO;
    }

    @Override
    public void draw(Graphics2D g2) {
        draw(g2, Integer.MAX_VALUE, false, false);
    }

    public void draw(Graphics2D g2, int aguaY, boolean perto, boolean selecionado) {
        if (!desentupido) {
        	
            // Corpo do bueiro entupido
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(x, y, LARGURA, ALTURA);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, LARGURA, ALTURA);
            
            // Grade do bueiro
            g2.setColor(new Color(60, 60, 60));
            for (int i = x + 5; i < x + LARGURA - 4; i += 7) {
                g2.drawLine(i, y + 3, i, y + ALTURA - 3);
            }
            // Bloqueio visível
            g2.setColor(Color.YELLOW);
            g2.drawString("█", x + 10, y + 12);

            // Efeito de transbordamento debaixo d'água
            if (y < aguaY) {
                g2.setColor(new Color(0, 100, 200, 100));
                g2.fillRect(x + 5, y - 5, 20, 8);
            }

            // Indicador de interação
            if (perto && selecionado) {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x - 25, y - 28, 95, 20, 6, 6);
                g2.setColor(Color.BLACK);
                g2.drawString("Pressione E", x - 20, y - 12);
            }
        } else {
            // Bueiro desentupido
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(x, y, LARGURA, ALTURA);
            g2.setColor(new Color(0, 100, 200));
            g2.drawString("↓", x + 10, y + 12);
        }
    }
}
