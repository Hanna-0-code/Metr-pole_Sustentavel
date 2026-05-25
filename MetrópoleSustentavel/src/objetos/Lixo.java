package objetos;

import java.awt.Color;
import java.awt.Graphics2D;

public class Lixo extends Objeto {

    private static final int LARGURA  = 18;
    private static final int ALTURA   = 18;

    public boolean coletado = false;

    private int floatOffset = 0;

    public Lixo(int x, int y) {
        super(x, y, LARGURA, ALTURA);
    }

    /** Deve ser chamado uma vez por frame para animar a flutuação. */
    public void update(double aguaNivel, long time, int index) {
        if (!coletado && y + ALTURA > aguaNivel) {
            floatOffset = (int)(Math.sin(time * 0.00000001 + index) * 2);
        } else {
            floatOffset = 0;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Sem contexto de água — não usada diretamente
    }

    public void draw(Graphics2D g2, double aguaNivel) {
        if (coletado) return;

        int drawY = y + floatOffset;
        
            // Lixo 
            g2.setColor(Color.RED);
            // Desenha forma de saco de lixo
            g2.fillRoundRect(x + 2, y + 4, 14, 14, 6, 6);
            g2.setColor(new Color(200, 50, 50));
            g2.fillRect(x + 5, y + 1, 8, 5); // gargalo do saco
        
            // Letra identificadora
            g2.setColor(Color.WHITE);
            g2.drawString("L", x + 4, drawY + 13);
    }

    public boolean colideComJogador(int jogX, int jogY, int jogTamanho) {
        return jogX < x + LARGURA && jogX + jogTamanho > x
            && jogY < y + ALTURA  && jogY + jogTamanho > y;
    }
}
