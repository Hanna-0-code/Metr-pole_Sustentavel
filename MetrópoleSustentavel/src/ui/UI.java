package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class UI {

    private final int screenWidth;
    private final int screenHeight;
    
    private som.musica somFinal = new som.musica();
    
    private boolean somDerrotaTocado = false;
    private boolean somVitoriaTocado = false;
    
    // Rastreia o estado para saber o momento exato em que o jogador revive ou despausa
    private String estadoAnterior = "jogando";

    public UI(int screenWidth, int screenHeight) {
        this.screenWidth  = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void desenharTextoCentralizado(Graphics2D g2, String texto, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(texto)) / 2;
        g2.drawString(texto, x, y);
    }

    // CONTROLE AUTOMÁTICO DE TRANSIÇÃO DOS SONS
    public void gerenciarSonsDoJogo(String estadoAtual) {
        // Se saiu do Pause ou do Game Over e voltou para a gameplay normal
        if ((estadoAnterior.equals("pausado") || estadoAnterior.equals("gameover")) && estadoAtual.equals("jogando")) {
            somDerrotaTocado = false;
            somVitoriaTocado = false;
            somFinal.pararMusica(); // Limpa resíduos de som
            somFinal.tocarMusica("src/som/musica.wav", true); // Solta a música em loop de volta!
        }
        
        // Se acabou de pausar, corta a música imediatamente
        if (!estadoAnterior.equals("pausado") && estadoAtual.equals("pausado")) {
            somFinal.pararMusica();
        }

        estadoAnterior = estadoAtual;
    }

    public void drawGameOver(Graphics2D g2) {
        gerenciarSonsDoJogo("gameover");

        if (!somDerrotaTocado) {
            somFinal.pararMusica(); 
            somFinal.tocarMusica("src/som/gameover.wav", false); 
            somDerrotaTocado = true;
        }
        // Fundo escuro
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30f));
        g2.setColor(Color.RED);
        desenharTextoCentralizado(g2, "GAME OVER", screenHeight / 2 - 40);

        g2.setFont(g2.getFont().deriveFont(16f));
        g2.setColor(Color.WHITE);
        desenharTextoCentralizado(g2, "Você morreu de forma trágica!", screenHeight / 2);
        desenharTextoCentralizado(g2, "Pressione ENTER para reiniciar", screenHeight / 2 + 40);
    }

    public void drawWin(Graphics2D g2) {
        gerenciarSonsDoJogo("vitoria");

        if (!somVitoriaTocado) {
            somFinal.pararMusica(); 
            somFinal.tocarMusica("src/som/win.wav", false); 
            somVitoriaTocado = true;
        }
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 28f));
        g2.setColor(Color.YELLOW);
        desenharTextoCentralizado(g2, "MISSÃO CUMPRIDA!", screenHeight / 2 - 60);

        g2.setFont(g2.getFont().deriveFont(16f));
        g2.setColor(Color.WHITE);
        desenharTextoCentralizado(g2, "A cidade está livre do alagamento!", screenHeight / 2 - 10);
        desenharTextoCentralizado(g2, "Manter bueiros limpos e descartar o lixo", screenHeight / 2 + 20);
        desenharTextoCentralizado(g2, "corretamente evita enchentes!",            screenHeight / 2 + 42);
    }

    public void drawPausa(Graphics2D g2) {
        gerenciarSonsDoJogo("pausado"); 

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 26f));
        g2.setColor(Color.WHITE);
        desenharTextoCentralizado(g2, "PAUSADO", screenHeight / 2 - 40);

        g2.setFont(g2.getFont().deriveFont(14f));
        g2.setColor(new Color(200, 200, 200));
        desenharTextoCentralizado(g2, "Pressione ESC para continuar", screenHeight / 2 + 10);
    }

    public void drawTelaInicial(Graphics2D g2) {
        gerenciarSonsDoJogo("menu");
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Título
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 28f));
        g2.setColor(new Color(100, 200, 100));
        desenharTextoCentralizado(g2, "METRÓPOLE SUSTENTÁVEL", screenHeight / 2 - 60);

        // Subtítulo
        g2.setFont(g2.getFont().deriveFont(14f));
        g2.setColor(new Color(180, 180, 180));
        desenharTextoCentralizado(g2, "Ajude a salvar a cidade!",
                screenHeight / 2 - 20);

        // Controles
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(13f));
        desenharTextoCentralizado(g2, "← → ou A/D: Mover    W/↑: Pular    E/Espaço: Interagir    ESC: Pausar",
                screenHeight / 2 + 20);

        // Instrução para começar
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        g2.setColor((System.currentTimeMillis() % 1000 < 500) ? Color.WHITE : new Color(150, 150, 150));
        desenharTextoCentralizado(g2, "Pressione ENTER para começar", screenHeight / 2 + 70);
    }
}
