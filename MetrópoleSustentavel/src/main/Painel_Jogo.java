package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

import fases.Fase1;
import fases.Fase2;
import personagem.Jogador;
import ui.UI;

public class Painel_Jogo extends JPanel implements Runnable {

    private static final long serialVersionUID = 1L;

    // ******** ESTADOS DO JOGO ********
    private static final int TELA_INICIAL = 0;
    private static final int DIALOGO = 1;
    private static final int JOGANDO = 2;
    private static final int PAUSADO = 3;
    private static final int GAME_OVER = 4;
    private static final int VITORIA = 5;
    private static final int DIALOGO_FASE2 = 6;
    private static final int JOGANDO_FASE2 = 7;
    private static final int VITORIA_FINAL = 8;
    private int ultimaFase = JOGANDO;

    private int estadoJogo = TELA_INICIAL;

    // ******** DIÁLOGOS - FASE 1 ********
    private final String[] dialogos = {
        "A cidade está um caos...",
        "Os bueiros estão entupidos e o lixo está por toda parte!",
        "Se nada for feito, esse lugar ficará inabitável!",
        "Sua missão é limpar a cidade!",
        "Colete todo o lixo espalhado pelo mapa.",
        "Desentupa os bueiros pressionando E ou Espaço perto deles.",
        "Cuidado! A cidade está submersa...",
        "Se ficar muito tempo debaixo d'água, vai ficar sem ar!",
        "Use as setas ou WAD para se mover e pular.",
        "Boa sorte! A cidade depende de você!"
    };

    // ******** DIÁLOGOS - FASE 2 ********
    private final String[] dialogosFase2 = {
        "Uau! Você limpou as enchentes. Mas a cidade ainda sofre...",
        "Sem árvores, o concreto absorve todo o calor do sol, fazendo as pessoas passarem mal.",
        "A temperatura está subindo a cada minuto que passa!",
        "A solução de verdade é plantar árvores!",
        "Você precisa plantar árvores para não superaquecer.",
        "Encontre os spots de plantio e pressione E para plantar.",
        "Cada árvore cria sombra e refresca o ar ao redor.",
        "Plante 5 árvores para mostrar que a natureza importa!",
        "Cuidado com o calor — ele drena sua energia rapidamente!"
    };

    private int linhaAtual = 0;
    private String textoAtual = "";
    private int indexLetra = 0;
    private int contadorTexto = 0;
    private static final int VELOCIDADE_TEXTO = 2;

    // ******** TELA ********
    private static final int TILE_BASE = 16;
    private static final int SCALE = 3;
    private static final int TILE_SIZE = TILE_BASE * SCALE;
    private static final int COLS = 16;
    private static final int ROWS = 12;
    public static final int SCREEN_W = TILE_SIZE * COLS;
    public static final int SCREEN_H = TILE_SIZE * ROWS;
    public static final int WORLD_W = SCREEN_W * 5;
    public static final int WORLD_H = SCREEN_H;

    private static final int FPS = 60;

    // ******** CÂMERA ********
    private int cameraX = 0;

    // ******** COMPONENTES ********
    private final KeyHandler keyH = new KeyHandler();
    private final UI ui = new UI(SCREEN_W, SCREEN_H);
    private final Jogador jogador = new Jogador(100, 452, TILE_SIZE);
    private final Fase1 fase1 = new Fase1(jogador, keyH, TILE_SIZE, WORLD_W, WORLD_H);
    private final Fase2 fase2 = new Fase2(jogador, keyH, TILE_SIZE, WORLD_W, WORLD_H);

    private Thread gameThread;

    public Painel_Jogo() {
        this.setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // ******** !!NÃO MEXER!! ********
    @Override
    public void run() {
        double drawInterval  = 1_000_000_000.0 / FPS;
        double nextDrawTime  = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();

            try {
                double remaining = (nextDrawTime - System.nanoTime()) / 1_000_000.0;
                if (remaining < 0) remaining = 0;
                Thread.sleep((long) remaining);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } 
    }
    
    private void update() {
    	ui.gerenciarSonsDoJogo(estadoJogo == JOGANDO || estadoJogo == JOGANDO_FASE2 ? "jogando" : estadoJogo == PAUSADO ? "pausado" : estadoJogo == GAME_OVER ? "gameover" : "menu");

        switch (estadoJogo) {

            case TELA_INICIAL:
                if (keyH.enterPressed) {
                    estadoJogo = DIALOGO;
                    keyH.consumirEnter();
                }
                break;

            case DIALOGO:
                atualizarDialogo(dialogos, JOGANDO);
                break;

            case JOGANDO:
                if (keyH.escPressed) {
                    ultimaFase = JOGANDO; 
                    estadoJogo = PAUSADO;
                    keyH.escPressed = false;
                    break;
                }
                atualizarJogoFase1();
                break;
                
            case JOGANDO_FASE2:
                if (keyH.escPressed) {
                    ultimaFase = JOGANDO_FASE2; 
                    estadoJogo = PAUSADO;
                    keyH.escPressed = false;
                    break;
                }

                atualizarJogoFase2();
                break;

            case PAUSADO:
                if (keyH.escPressed) {
                    estadoJogo = ultimaFase;
                    keyH.escPressed = false;
                }
                break;

            case GAME_OVER:
                if (keyH.enterPressed) {
                    if (ultimaFase == JOGANDO) {
                        fase1.reiniciarJogo();
                        estadoJogo = JOGANDO;
                    } else if (ultimaFase == JOGANDO_FASE2) {
                        fase2.reiniciarJogo();
                        estadoJogo = JOGANDO_FASE2;
                    }
                    keyH.consumirEnter();
                }
                break;

            case VITORIA:
                if (keyH.enterPressed) {
                    iniciarFase2();
                    keyH.consumirEnter();
                }
                break;

            case DIALOGO_FASE2:
                atualizarDialogo(dialogosFase2, JOGANDO_FASE2);
                break;

        }
    }

    private void atualizarDialogo(String[] linhas, int proximoEstado) {
        contadorTexto++;
        if (contadorTexto > VELOCIDADE_TEXTO) {
            if (indexLetra < linhas[linhaAtual].length()) {
                textoAtual += linhas[linhaAtual].charAt(indexLetra);
                indexLetra++;
            }
            contadorTexto = 0;
        }

        if (keyH.enterPressed) {
            if (indexLetra < linhas[linhaAtual].length()) {
                textoAtual = linhas[linhaAtual];
                indexLetra = linhas[linhaAtual].length();
            } else {
                linhaAtual++;
                textoAtual = "";
                indexLetra = 0;
                if (linhaAtual >= linhas.length) {
                    linhaAtual = 0;
                    estadoJogo = proximoEstado;
                }
            }
            keyH.consumirEnter();
        }
    }

    private void atualizarJogoFase1() {
        ultimaFase = JOGANDO;

        fase1.update();

        if (fase1.gameOver) {
            estadoJogo = GAME_OVER;
            return;
        }
        if (fase1.venceu) {
            estadoJogo = VITORIA;
            return;
        }
        moverJogador();
    }

    private void iniciarFase2() {
        fase2.reiniciarJogo();
        estadoJogo = DIALOGO_FASE2;
        linhaAtual = 0;
        textoAtual = "";
        indexLetra = 0;
    }

    private void atualizarJogoFase2() {
        ultimaFase = JOGANDO_FASE2;

        fase2.update();

        if (fase2.gameOver) {
            estadoJogo = GAME_OVER;
            return;
        }
        if (fase2.venceu) {
            estadoJogo = VITORIA_FINAL;
            return;
        }
        moverJogador();
    }

    private void moverJogador() {
        boolean movendo = false;
        int vel = jogador.naAgua ? Jogador.SPEED_AGUA : Jogador.SPEED_TERRA;
        
        if (keyH.leftPressed)  {
        	jogador.setX(jogador.getX() - vel); movendo = true; 
        }
        if (keyH.rightPressed) {
        	jogador.setX(jogador.getX() + vel); movendo = true; 
        }

        jogador.updateAnimacao(movendo);

        if (keyH.upPressed && jogador.noChao) {
            jogador.velocidadeY = -8;
            jogador.noChao = false;
        }

        if (jogador.getX() < 0) jogador.setX(0);
        if (jogador.getX() > WORLD_W - TILE_SIZE) jogador.setX(WORLD_W - TILE_SIZE);

        if (jogador.getY() > WORLD_H) {
            if (estadoJogo == JOGANDO) {
            	fase1.gameOver = true; estadoJogo = GAME_OVER; 
            } else if (estadoJogo == JOGANDO_FASE2) {
            	fase2.gameOver = true; estadoJogo = GAME_OVER; 
            	}
        }

        cameraX = jogador.getX() - SCREEN_W / 2 + TILE_SIZE / 2;
        cameraX = Math.max(0, Math.min(cameraX, WORLD_W - SCREEN_W));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        switch (estadoJogo) {

            case TELA_INICIAL:
                ui.drawTelaInicial(g2);
                break;

            case DIALOGO:
                desenharDialogo(g2, "Fase 1", dialogos);
                break;

            case JOGANDO:
                desenharJogoFase1(g2);
                break;

            case PAUSADO:
                if (fase1.venceu || estadoJogo == JOGANDO_FASE2) {
                    desenharJogoFase2(g2);
                } else {
                    desenharJogoFase1(g2);
                }
                ui.drawPausa(g2);
                break;

            case GAME_OVER:
                ui.drawGameOver(g2);
                break;

            case VITORIA:
                desenharTelaVitoriaFase1(g2);
                break;

            case DIALOGO_FASE2:
                desenharDialogo(g2, "Fase 2", dialogosFase2);
                break;

            case JOGANDO_FASE2:
                desenharJogoFase2(g2);
                break;

            case VITORIA_FINAL:
                desenharVitoriaFinal(g2);
                break;
        }

        g2.dispose();
    }

    private void desenharDialogo(Graphics2D g2, String titulo, String[] linhas) {
        g2.setColor(new Color(20, 80, 20));
        g2.fillRect(0, 0, SCREEN_W, SCREEN_H);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20f));
        int tw = g2.getFontMetrics().stringWidth(titulo);
        g2.drawString(titulo, SCREEN_W / 2 - tw / 2, 100);

        int cx = 50, cy = SCREEN_H - 150, cw = SCREEN_W - 100, ch = 100;
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRoundRect(cx, cy, cw, ch, 20, 20);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(cx, cy, cw, ch, 20, 20);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14f));
        g2.drawString(textoAtual, cx + 20, cy + 40);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        g2.drawString("Pressione ENTER para continuar...", cx + 20, cy + 80);

        for (int i = 0; i < linhas.length; i++) {
            int dotX = SCREEN_W / 2 - (linhas.length * 12) / 2 + i * 12;
            g2.setColor(i == linhaAtual ? Color.WHITE : new Color(255, 255, 255, 80));
            g2.fillOval(dotX, SCREEN_H - 40, 8, 8);
        }
    }

    private void desenharJogoFase1(Graphics2D g2) {
        g2.translate(-cameraX, 0);
        fase1.draw(g2);
        jogador.draw(g2, fase1.gameOver);
        g2.translate(cameraX, 0);
        fase1.drawHUD(g2);
    }

    private void desenharJogoFase2(Graphics2D g2) {
        g2.translate(-cameraX, 0);
        fase2.draw(g2);
        jogador.draw(g2, fase2.gameOver);
        g2.translate(cameraX, 0);
        fase2.drawHUD(g2);
    }

    private void desenharTelaVitoriaFase1(Graphics2D g2) {
        g2.setColor(new Color(20, 80, 20));
        g2.fillRect(0, 0, SCREEN_W, SCREEN_H);
        
        // Efeito de gradiente no fundo (opcional)
        for (int i = 0; i < SCREEN_H; i += 2) {
            g2.setColor(new Color(20, 80 + i / 10, 20, 50));
            g2.fillRect(0, i, SCREEN_W, 2);
        }

        g2.setColor(new Color(80, 200, 80));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));
        String t1 = "Fase 1 Concluída!";
        g2.drawString(t1, SCREEN_W / 2 - g2.getFontMetrics().stringWidth(t1) / 2, 100);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
        String t2 = "A cidade está livre do alagamento!";
        g2.drawString(t2, SCREEN_W / 2 - g2.getFontMetrics().stringWidth(t2) / 2, 160);
        
        //  descarte correto e bueiros
        g2.setColor(new Color(100, 255, 150));
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14f));
        String dica = "Manter os bueiros limpos e descartar o lixo corretamente evita enchentes!";
        g2.drawString(dica, SCREEN_W / 2 - g2.getFontMetrics().stringWidth(dica) / 2, 210);
        
        // Linha decorativa separando as mensagens
        g2.setColor(new Color(80, 200, 80, 100));
        int lineY = 230;
        int lineWidth = 400;
        g2.fillRect(SCREEN_W / 2 - lineWidth / 2, lineY, lineWidth, 1);

        g2.setColor(new Color(200, 255, 100));
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14f));
        String t3 = "Mas agora temos outro problema. A cidade está superaquecendo...";
        g2.drawString(t3, SCREEN_W / 2 - g2.getFontMetrics().stringWidth(t3) / 2, 260);

        boolean pisca = System.currentTimeMillis() % 800 < 400;
        if (pisca) {
            g2.setColor(new Color(255, 220, 50));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            String t4 = "Pressione ENTER para continuar para a Fase 2";
            g2.drawString(t4, SCREEN_W / 2 - g2.getFontMetrics().stringWidth(t4) / 2, 340);
        }
    }

    private void desenharVitoriaFinal(Graphics2D g2) {
        g2.setColor(new Color(100, 180, 240)); 
        g2.fillRect(0, 0, SCREEN_W, SCREEN_H);

        // ********* PRÉDIOS AO FUNDO ********
        int[] posX =      {40, 180, 340, 520, 700};
        int[] alturas =   {170, 240, 150, 280, 200};
        int[] larguras =  {80, 100, 70, 110, 90};

        for (int i = 0; i < posX.length; i++) {
            int x = posX[i];
            int altura = alturas[i];
            int largura = larguras[i];

            // prédio principal
            g2.setColor(new Color(90, 90, 110));
            g2.fillRect(x, SCREEN_H - altura - 60, largura, altura);

            // lateral escura
            g2.setColor(new Color(70, 70, 90));
            g2.fillRect(x + largura - 10, SCREEN_H - altura - 60, 10, altura);

            // janelas
            g2.setColor(new Color(255, 255, 180));
            for (int y = SCREEN_H - altura - 40; y < SCREEN_H - 90; y += 18) {
                for (int wx = x + 8; wx < x + largura - 12; wx += 18) {
                    g2.fillRect(wx, y, 8, 10);
                }
            }
        }
        
        long t = System.nanoTime();
        for (int i = 0; i < 8; i++) {
            int tx = 60 + i * 110;
            int balanco = (int)(Math.sin(t * 0.000000004 + i) * 2);
            g2.setColor(new Color(120, 70, 30));
            g2.fillRoundRect(tx + 12, SCREEN_H - 100, 8, 50, 4, 4);
            g2.setColor(new Color(30, 120, 30));
            g2.fillOval(tx - 10 + balanco, SCREEN_H - 155, 60, 55);
            g2.setColor(new Color(60, 160, 50));
            g2.fillOval(tx + balanco, SCREEN_H - 165, 40, 45);
        }

        g2.setColor(new Color(20, 80, 20));
        g2.fillRect(0, SCREEN_H - 60, SCREEN_W, 60);

        Font titleFont;
        Font subtitleFont;
        Font textFont;
        
        try {
            titleFont = new Font("Segoe UI", Font.BOLD, 42);
            subtitleFont = new Font("Segoe UI", Font.BOLD, 22);
            textFont = new Font("Segoe UI", Font.PLAIN, 16);
        } catch (Exception e) {
            titleFont = g2.getFont().deriveFont(Font.BOLD, 42f);
            subtitleFont = g2.getFont().deriveFont(Font.BOLD, 22f);
            textFont = g2.getFont().deriveFont(Font.PLAIN, 16f);
        }
        
        // Título principal 
        g2.setColor(new Color(255, 230, 50));
        g2.setFont(titleFont);
        String t1 = "Parabéns!";
        int titleY = 70; 
        g2.drawString(t1, SCREEN_W / 2 - g2.getFontMetrics().stringWidth(t1) / 2, titleY);
        
        g2.setColor(new Color(255, 255, 100, 80));
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 || j != 0) {
                    g2.drawString(t1, SCREEN_W / 2 - g2.getFontMetrics().stringWidth(t1) / 2 + i, titleY + j);
                }
            }
        }
        g2.setColor(new Color(255, 230, 50));
        g2.drawString(t1, SCREEN_W / 2 - g2.getFontMetrics().stringWidth(t1) / 2, titleY);
        
        // Subtítulo
        g2.setColor(Color.WHITE);
        g2.setFont(subtitleFont);
        String t2 = "Metrópole Sustentável Concluída!";
        int subtitleY = 120; 
        g2.drawString(t2, SCREEN_W / 2 - g2.getFontMetrics().stringWidth(t2) / 2, subtitleY);
        
        // Linha decorativa
        g2.setColor(new Color(255, 230, 50, 150));
        int lineY = subtitleY + 15;
        int lineWidth = 300;
        g2.fillRect(SCREEN_W / 2 - lineWidth / 2, lineY, lineWidth, 2);
        
        // Mensagens de texto
        g2.setFont(textFont);
        String[] msgs = {
            "Você limpou o lixo, acabou com a enchente e plantou árvores,",
            "mostrando como a natureza é essencial para a cidade.",
            "Árvores reduzem o calor, purificam o ar e salvam vidas!"
        };
        
        int startY = subtitleY + 45; 
        
        for (int i = 0; i < msgs.length; i++) {
            if (i == 2) {
                g2.setColor(new Color(120, 255, 120));
                g2.setFont(textFont.deriveFont(Font.BOLD, 17f));
            } else {
                g2.setColor(Color.WHITE);
                g2.setFont(textFont);
            }
            g2.drawString(msgs[i], 
                         SCREEN_W / 2 - g2.getFontMetrics().stringWidth(msgs[i]) / 2, 
                         startY + i * 30);
        }
    }
}
