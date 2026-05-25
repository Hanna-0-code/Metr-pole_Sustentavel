package fases;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import main.KeyHandler;
import objetos.Bueiro;
import objetos.Lixo;
import personagem.Jogador;

public class Fase1 {

    // **** CONSTANTES ****
    private static final double AGUA_NIVEL_INICIAL    = 250;
    private static final double AGUA_NIVEL_FINAL      = 550;
    private static final double VELOCIDADE_ESCOAMENTO = 50;
    private static final double AGUA_INTERPOLACAO     = 0.05;
    private static final double GRAVIDADE_AGUA_FATOR  = 0.3;
    private static final double VELOCIDADE_AGUA_MAX   = 2.0;
    private static final int    AR_MAX                = 400;
    private static final int    AR_RECARGA            = 2;

    // **** DIMENSÕES ****
    private final int worldWidth;
    private final int worldHeight;
    private final int tileSize;

    // **** PLATAFORMAS ****
    private final int[][] plataformas = {
        {0,    470, 300, 20}, {400,  470, 250, 20},
        {150,  300, 120, 20}, {300,  220, 120, 20}, {450, 350, 120, 20},
        {750,  470, 150, 20}, {1100, 470, 250, 20},
        {850,  350, 120, 20}, {1150, 360, 120, 20}, {950, 280, 100, 20},
        {1450, 470, 300, 20}, {1550, 400, 120, 20}, {1800, 340, 120, 20},
        {2100, 420, 100, 20}, {2300, 350, 100, 20}, {2500, 300, 100, 20},
        {2700, 450, 120, 20}, {2900, 470, 300, 20}
    };

    // **** OBJETOS ****
    private final ArrayList<Lixo>   lixos   = new ArrayList<>();
    private final ArrayList<Bueiro> bueiros = new ArrayList<>();

    // **** ESTADO DO BUEIRO PRÓXIMO ****
    private boolean pertoDeBueiro = false;
    private int     bueiroProximo = -1;

    // **** SISTEMA DE ÁGUA ****
    private double aguaNivel = AGUA_NIVEL_INICIAL;
    private double aguaAlvo  = AGUA_NIVEL_INICIAL;

    // **** SISTEMA DE AR ****
    private int arAtual = AR_MAX;

    // **** SUSTENTABILIDADE (calculado uma vez por frame) ****
    private int sustentabilidade = 0;

    // **** ESTADO DE JOGO ****
    public boolean venceu   = false;
    public boolean gameOver = false;

    // **** REFERÊNCIAS ****
    private final Jogador    jogador;
    private final KeyHandler keyH;

    public Fase1(Jogador jogador, KeyHandler keyH, int tileSize,
                 int worldWidth, int worldHeight) {

        this.jogador     = jogador;
        this.keyH        = keyH;
        this.tileSize    = tileSize;
        this.worldWidth  = worldWidth;
        this.worldHeight = worldHeight;

        // Lixos
        lixos.add(new Lixo(100,  440)); lixos.add(new Lixo(450,  440));
        lixos.add(new Lixo(180,  270)); lixos.add(new Lixo(330,  190));
        lixos.add(new Lixo(480,  320)); lixos.add(new Lixo(800,  440));
        lixos.add(new Lixo(1150, 440)); lixos.add(new Lixo(880,  320));
        lixos.add(new Lixo(1180, 330)); lixos.add(new Lixo(1500, 440));
        lixos.add(new Lixo(1580, 370)); lixos.add(new Lixo(1830, 310));
        lixos.add(new Lixo(2120, 390)); lixos.add(new Lixo(2320, 320));
        lixos.add(new Lixo(2520, 270)); lixos.add(new Lixo(2720, 420));
        lixos.add(new Lixo(3000, 440));

        // Bueiros
        bueiros.add(new Bueiro(200,  460)); bueiros.add(new Bueiro(450,  460));
        bueiros.add(new Bueiro(750,  460)); bueiros.add(new Bueiro(1300, 460));
        bueiros.add(new Bueiro(1700, 460)); bueiros.add(new Bueiro(3150, 460));
    }

    // **** REINÍCIO ****
    public void reiniciarJogo() {
        jogador.setX(100);
        jogador.setY(452);
        jogador.velocidadeY = 0;
        aguaNivel    = AGUA_NIVEL_INICIAL;
        aguaAlvo     = AGUA_NIVEL_INICIAL;
        arAtual      = AR_MAX;
        gameOver     = false;
        venceu       = false;
        sustentabilidade = 0;

        for (Lixo l : lixos)     l.coletado    = false;
        for (Bueiro b : bueiros) b.desentupido = false;
    }

    // **** UPDATE ****
    public void update() {
        if (venceu) return;

        long time = System.nanoTime();

        // Atualiza sustentabilidade UMA vez por frame
        sustentabilidade = calcularSustentabilidade();

        // Estado do jogador na água
        jogador.naAgua = (jogador.getY() + tileSize > aguaNivel);
        boolean submerso = (jogador.getY() + tileSize / 2 > aguaNivel);

        // Sistema de ar
        if (submerso) {
            arAtual--;
        } else {
            arAtual = Math.min(arAtual + AR_RECARGA, AR_MAX);
        }
        if (arAtual <= 0) {
            gameOver = true;
            return;
        }

        // Gravidade (reduzida na água)
        if (jogador.naAgua) {
            jogador.velocidadeY += jogador.gravidade * GRAVIDADE_AGUA_FATOR;
        } else {
            jogador.velocidadeY += jogador.gravidade;
        }
        jogador.setY(jogador.getY() + (int) jogador.velocidadeY);
        jogador.noChao = false;

        // Colisão com plataformas
        for (int[] p : plataformas) {
            int px = p[0], py = p[1], pw = p[2], ph = p[3];

            if (jogador.velocidadeY >= 0
                    && jogador.getX() + tileSize > px && jogador.getX() < px + pw
                    && jogador.getY() + tileSize >= py && jogador.getY() + tileSize <= py + ph + 10) {

                jogador.setY(py - tileSize);
                jogador.velocidadeY = 0;
                jogador.noChao = true;
                jogador.naAgua = false;
            }

            if (jogador.velocidadeY < 0
                    && jogador.getX() + tileSize > px && jogador.getX() < px + pw
                    && jogador.getY() <= py + ph && jogador.getY() >= py) {

                jogador.setY(py + ph);
                jogador.velocidadeY = 0;
            }
        }

        // Resistência da água
        if (!jogador.noChao && jogador.naAgua && jogador.velocidadeY > VELOCIDADE_AGUA_MAX) {
            jogador.velocidadeY = VELOCIDADE_AGUA_MAX;
        }

        // Atualiza lixos
        for (int i = 0; i < lixos.size(); i++) {
            lixos.get(i).update(aguaNivel, time, i);
        }

        // Coleta de lixo
        for (Lixo l : lixos) {
            if (!l.coletado && l.colideComJogador(jogador.getX(), jogador.getY(), tileSize)) {
                l.coletado = true;
            }
        }

        // Detecção de bueiro próximo
        pertoDeBueiro = false;
        bueiroProximo = -1;
        for (int i = 0; i < bueiros.size(); i++) {
            Bueiro b = bueiros.get(i);
            if (!b.desentupido && b.jogadorPerto(jogador.getX(), jogador.getY())) {
                pertoDeBueiro = true;
                bueiroProximo = i;
                break;
            }
        }

        // Interação com bueiro 
        if (keyH.interactPressed && pertoDeBueiro && bueiroProximo != -1) {
            Bueiro b = bueiros.get(bueiroProximo);
            b.desentupido = true;
            aguaAlvo = Math.min(aguaAlvo + VELOCIDADE_ESCOAMENTO, AGUA_NIVEL_FINAL);
            keyH.consumirInteract();
        }

        // Interpolação do nível da água
        if (Math.abs(aguaNivel - aguaAlvo) > 0.5) {
            aguaNivel += (aguaAlvo - aguaNivel) * AGUA_INTERPOLACAO;
        } else {
            aguaNivel = aguaAlvo;
        }

        // Condição de vitória
        boolean vitoriaLixo   = lixos.stream().allMatch(l -> l.coletado);
        boolean vitoriaBueiro = bueiros.stream().allMatch(b -> b.desentupido);
        if (vitoriaLixo && vitoriaBueiro) venceu = true;
    }

    // **** CÁLCULO DE SUSTENTABILIDADE ****
    private int calcularSustentabilidade() {
        long lixosColetados  = lixos.stream().filter(l -> l.coletado).count();
        long bueirosLimpos   = bueiros.stream().filter(b -> b.desentupido).count();
        int totalTarefas     = lixos.size() + bueiros.size();
        if (totalTarefas == 0) return 0;
        return (int)((lixosColetados + bueirosLimpos) * 100 / totalTarefas);
    }

    // **** DRAW ****
    public void draw(Graphics2D g2) {
        long time = System.nanoTime();

        // Céu dinâmico
        Color corCeu;
        if (sustentabilidade > 90)      corCeu = new Color(135, 206, 235);
        else if (sustentabilidade > 50) corCeu = new Color(180, 180, 180);
        else                            corCeu = new Color(100, 100, 100);

        g2.setColor(corCeu);
        g2.fillRect(0, 0, worldWidth, worldHeight);

        // Nuvens animadas
        g2.setColor(new Color(255, 255, 255, 180));
        for (int i = 0; i < 5; i++) {
            int nuvemX = (int)((time * 0.00000002 + i * 400) % (worldWidth + 300) - 150);
            g2.fillOval(nuvemX,      50 + i * 40, 80, 40);
            g2.fillOval(nuvemX + 30, 40 + i * 40, 60, 35);
            g2.fillOval(nuvemX + 60, 50 + i * 40, 70, 40);
        }

        // Chão
        int alturaChao = 100;
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(0, worldHeight - alturaChao, worldWidth, alturaChao);
        g2.setColor(Color.YELLOW);
        for (int i = 0; i < worldWidth; i += 40) g2.fillRect(i, worldHeight - 50, 20, 5);
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(0, worldHeight - alturaChao, worldWidth, 5);

        // Prédios de fundo
        g2.setColor(new Color(80, 80, 100));
        for (int i = 0; i < worldWidth; i += 250) {
            int altPred = 150 + (i % 3) * 50;
            g2.fillRect(i, worldHeight - altPred - 100, 70, altPred);
            g2.setColor(new Color(100, 100, 120));
            for (int wy = worldHeight - altPred - 80; wy < worldHeight - 120; wy += 20) {
                for (int wx = i + 10; wx < i + 60; wx += 15) {
                    g2.fillRect(wx, wy, 8, 12);
                }
            }
            g2.setColor(new Color(80, 80, 100));
        }

        // Plataformas
        for (int[] p : plataformas) {
            g2.setColor(new Color(255, 200, 0));
            g2.fillRect(p[0], p[1], p[2], p[3]);
            g2.setColor(new Color(180, 140, 0));
            g2.drawRect(p[0], p[1], p[2], p[3]);
        }

        // Água com efeitos
        int aguaY = (int) aguaNivel;

        // Gradiente de profundidade
        for (int i = 0; i < 100 && aguaY + i < worldHeight; i++) {
            int alpha = Math.max(0, 180 - i * 2);
            g2.setColor(new Color(0, 100 + i / 2, 180 + i / 3, alpha));
            g2.fillRect(0, aguaY + i, worldWidth, 2);
        }

        // Camada principal
        g2.setColor(new Color(0, 130, 210, 170));
        g2.fillRect(0, aguaY, worldWidth, worldHeight - aguaY);

        // Ondas na superfície
        g2.setColor(new Color(255, 255, 255, 160));
        for (int i = 0; i < worldWidth; i += 12) {
            int wave = (int)(Math.sin(i * 0.025 + time * 0.000000005) * 4)
                     + (int)(Math.sin(i * 0.06  + time * 0.000000007) * 2)
                     + (int)(Math.sin(i * 0.1   + time * 0.000000009) * 1);
            g2.fillRect(i, aguaY + wave, 8, 2);
        }

        // Reflexos de luz
        g2.setColor(new Color(255, 255, 200, 100));
        for (int i = 0; i < 12; i++) {
            int reflexX = (int)((time * 0.000000008 + i * 180) % worldWidth);
            int reflexW = 30 + (int)(Math.sin(time * 0.00000001) * 10);
            g2.fillRect(reflexX, aguaY + 3, reflexW, 3);
        }

        // Bolhas
        g2.setColor(new Color(180, 220, 255, 150));
        for (int i = 0; i < 20; i++) {
            int bolhaX = (int)((time * 0.00000002 * i) % worldWidth);
            int bolhaY = aguaY + (int)((time * 0.0000001 * i) % (worldHeight - aguaY));
            int bolhaS = 3 + (int)(Math.sin(time * 0.00000001 + i) * 2);
            g2.fillOval(bolhaX, bolhaY, bolhaS, bolhaS);
        }

        // Efeito cáustico
        g2.setColor(new Color(255, 255, 200, 40));
        for (int i = 0; i < 30; i++) {
            int caustX = (int)((time * 0.00000003 + i * 123) % worldWidth);
            int caustY = aguaY + 30 + (int)(Math.sin(time * 0.000000005 + i) * 20);
            g2.fillOval(caustX, caustY, 15, 8);
        }

        // Espuma 
        g2.setColor(new Color(255, 255, 255, 120));
        for (int[] p : plataformas) {
            if (p[1] + p[3] > aguaY && p[1] < aguaY + 50) {
                for (int i = 0; i < 5; i++) {
                    int espumaX = p[0] + (i * p[2] / 5);
                    int espumaY = aguaY - 2 + (int)(Math.sin(time * 0.00000002 + espumaX) * 2);
                    g2.fillOval(espumaX, espumaY, 4, 2);
                }
            }
        }

        // Objetos
        for (Lixo l : lixos) {
            l.draw(g2, aguaNivel);
        }
        for (int i = 0; i < bueiros.size(); i++) {
            bueiros.get(i).draw(g2, aguaY, pertoDeBueiro, bueiroProximo == i);
        }
    }

    // **** HUD ****
    public void drawHUD(Graphics2D g2) {
        // Usa sustentabilidade calculada no update()
        long lixosColetados = lixos.stream().filter(l -> l.coletado).count();
        long bueirosLimpos  = bueiros.stream().filter(b -> b.desentupido).count();

        // Painel de fundo
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(10, 10, 210, 105, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(10, 10, 210, 105, 10, 10);

        // Textos
        g2.drawString("Sustentabilidade: " + sustentabilidade + "%", 20, 35);
        g2.drawString("Lixos:   " + lixosColetados + "/" + lixos.size(), 20, 55);
        g2.drawString("Bueiros: " + bueirosLimpos  + "/" + bueiros.size(), 20, 75);

        // Drenagem
        int porcentagemAgua = (int)(((AGUA_NIVEL_FINAL - aguaNivel) / (AGUA_NIVEL_FINAL - AGUA_NIVEL_INICIAL)) * 100);
        porcentagemAgua = Math.max(0, Math.min(100, porcentagemAgua));
        g2.drawString("Drenagem: " + porcentagemAgua + "%", 20, 95);

        // Barra de ar
        g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(50, 108, 100, 10, 4, 4);

        int larguraAr = (int)((arAtual / (double) AR_MAX) * 100);
        boolean piscando = arAtual < AR_MAX * 0.3 && System.currentTimeMillis() % 500 < 250;
        g2.setColor(arAtual < AR_MAX * 0.3 ? (piscando ? Color.ORANGE : Color.RED) : Color.CYAN);
        g2.fillRoundRect(50, 108, larguraAr, 10, 4, 4);

        if (arAtual < AR_MAX * 0.2) {
            g2.setColor(Color.RED);
            g2.setFont(g2.getFont().deriveFont(12f));
            g2.drawString("SEM AR!", 54, 105);
        }
    }

    // **** GETTERS PARA Painel_Jogo *****
    public double getAguaNivel() { 
    	return aguaNivel; 
    }
    public int    getArAtual()   {
    	return arAtual; 
    }
    public int    getArMax()     {
    	return AR_MAX; 
    }
}
