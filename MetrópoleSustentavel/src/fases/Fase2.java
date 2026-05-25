package fases;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import main.KeyHandler;
import personagem.Jogador;
import objetos.SpotPlantio;

public class Fase2 {

    // ******** CONSTANTES ********
    private static final float TEMPERATURA_INICIAL   = 30f;
    private static final float TEMPERATURA_MAX       = 50f;
    private static final float TAXA_AQUECIMENTO      = 0.05f; // graus por frame
    private static final float REDUCAO_SOMBRA        = 0.03f;  // resfriamento ao estar na sombra
    private static final int   ENERGIA_MAX           = 200;
    private static final float TAXA_PERDA_ENERGIA    = 0.15f;  // por frame acima do limiar
    private static final float TEMP_LIMIAR_DANO      = 42f;    // começa a perder energia acima disso
    private static final int   RAIO_SOMBRA_ARVORE    = 55;
    private static final int   NUM_ARVORES_PLANTIO   = 5;      // árvores que o jogador precisa plantar
    private static final int   DISTANCIA_PLANTIO_X   = 50;     // distância máxima para plantar (x)
    private static final int   DISTANCIA_PLANTIO_Y   = 30;     // distância máxima para plantar (y)

    // ******** DIMENSÕES ********
    private final int worldWidth;
    private final int worldHeight;
    private final int tileSize;

    // ******** PLATAFORMAS ********
    private final int[][] plataformas = {
        // Chão principal
        {0,    470, 200, 20}, {550,  470, 150, 20}, {1050, 470, 240, 20},
        {1960, 470, 200, 20}, {2400, 470, 200, 20},
        {3000, 470, 200, 20}, {3200, 400, 100, 20},
        // Plataformas elevadas
        {180,  380, 100, 20}, {400,  340, 100, 20},
        {700,  380, 120, 20}, {900,  310, 100, 20},
        {1350, 380, 100, 20},
        {1600, 420, 100, 20}, {1800, 360, 100, 20},
        {2250, 380, 100, 20},
        {2550, 400, 100, 20}, {2750, 320, 100, 20}, {3400, 370, 100, 20},
        {3550, 320, 100, 20}, {3700, 380, 100, 20}
    };

    // ******** SPOTS DE PLANTIO ********
    private final ArrayList<SpotPlantio> spotsPlantio;
    private int arvorePerto = -1;

    // ******** ESTADO TEMPERATURA/ENERGIA ********
    private float temperatura = TEMPERATURA_INICIAL;
    private float energia     = ENERGIA_MAX;

    // ******** FLAGS DE ESTADO ********
    private boolean emSombra     = false;
    public  boolean venceu       = false;
    public  boolean gameOver     = false;

    // ******** REFERÊNCIAS ********
    private final Jogador    jogador;
    private final KeyHandler keyH;

    // ******** ANIMAÇÃO / VFX ********
    private long    frameCount  = 0;
    private final ArrayList<ParticulaCalor> particulas = new ArrayList<>();

    // ******** CONSTRUTOR ********
    public Fase2(Jogador jogador, KeyHandler keyH, int tileSize,
                 int worldWidth, int worldHeight) {

        this.jogador     = jogador;
        this.keyH        = keyH;
        this.tileSize    = tileSize;
        this.worldWidth  = worldWidth;
        this.worldHeight = worldHeight;

        // Inicializa os spots de plantio
        spotsPlantio = new ArrayList<>();
        int[][] spotsData = {
            {630,  470}, {1650, 420},
            {2450, 470}, {3100, 470},
            {3730, 380}
        };
        
        for (int[] spot : spotsData) {
            spotsPlantio.add(new SpotPlantio(spot[0], spot[1]));
        }
    }

    // ===== REINÍCIO =====
    public void reiniciarJogo() {
        jogador.setX(80);
        jogador.setY(452);
        jogador.velocidadeY = 0;
        temperatura  = TEMPERATURA_INICIAL;
        energia      = ENERGIA_MAX;
        gameOver     = false;
        venceu       = false;
        arvorePerto  = -1;
        
        // Reseta todos os spots de plantio
        for (SpotPlantio spot : spotsPlantio) {
            spot.reset();
        }
        
        particulas.clear();
    }

    // ******** UPDATE ********
    public void update() {
        if (venceu || gameOver) return;
        frameCount++;

        // Gravidade
        jogador.velocidadeY += jogador.gravidade;
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
            }
            if (jogador.velocidadeY < 0
                    && jogador.getX() + tileSize > px && jogador.getX() < px + pw
                    && jogador.getY() <= py + ph && jogador.getY() >= py) {
                jogador.setY(py + ph);
                jogador.velocidadeY = 0;
            }
        }

        // Detecta sombra de árvore plantada
        emSombra = false;
        int jCX = jogador.getX() + tileSize / 2;
        int jCY = jogador.getY() + tileSize / 2;
        
        for (SpotPlantio spot : spotsPlantio) {
            if (spot.isArvorePlantada() && spot.isSombraProjetada(jCX, jCY, RAIO_SOMBRA_ARVORE)) {
                emSombra = true;
                break;
            }
        }

        // Temperatura dinâmica
        if (emSombra) {
            temperatura -= REDUCAO_SOMBRA;
        } else {
            temperatura += TAXA_AQUECIMENTO;
        }
        temperatura = Math.max(TEMPERATURA_INICIAL, Math.min(temperatura, TEMPERATURA_MAX));

        // Perda de energia por calor
        if (temperatura > TEMP_LIMIAR_DANO) {
            float fatorCalor = (temperatura - TEMP_LIMIAR_DANO) / (TEMPERATURA_MAX - TEMP_LIMIAR_DANO);
            energia -= TAXA_PERDA_ENERGIA * (1f + fatorCalor * 2f);
        } else {
            // Recupera energia lentamente na sombra
            if (emSombra) {
                energia = Math.min(energia + 0.3f, ENERGIA_MAX);
            }
        }

        if (energia <= 0) {
            gameOver = true;
            return;
        }

        // Detecta spot de plantio próximo
        arvorePerto = -1;
        int jogadorYBase = jogador.getY() + tileSize;
        
        for (int i = 0; i < spotsPlantio.size(); i++) {
            SpotPlantio spot = spotsPlantio.get(i);
            if (!spot.isArvorePlantada() && spot.isJogadorProximo(jCX, jogadorYBase, DISTANCIA_PLANTIO_X, DISTANCIA_PLANTIO_Y)) {
                arvorePerto = i;
                break;
            }
        }

        // Plantio de árvore
        if (keyH.interactPressed && arvorePerto != -1) {
            spotsPlantio.get(arvorePerto).plantarArvore();
            keyH.consumirInteract();
            // Refresca ao redor imediatamente
            temperatura -= 2f;
        }

        // Partículas de calor
        if (frameCount % 4 == 0 && temperatura > 38f) {
            float intensidade = (temperatura - 38f) / (TEMPERATURA_MAX - 38f);
            if (Math.random() < intensidade * 0.7) {
                int px = (int)(Math.random() * worldWidth);
                particulas.add(new ParticulaCalor(px, worldHeight - 100));
            }
        }
        particulas.removeIf(p -> p.morta());
        for (ParticulaCalor p : particulas) p.update();

        // Condição de vitória
        int count = 0;
        for (SpotPlantio spot : spotsPlantio) {
            if (spot.isArvorePlantada()) count++;
        }
        if (count >= NUM_ARVORES_PLANTIO && !venceu) {
            som.musica somFinal = new som.musica();
            somFinal.pararMusica();
            somFinal.tocarMusica("src/som/win.wav", false);
            venceu = true;
        }
    }

    // ******** DRAW ********
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        long time = System.nanoTime();

        // ── Céu: cor muda com temperatura ──
        float t = (temperatura - TEMPERATURA_INICIAL) / (TEMPERATURA_MAX - TEMPERATURA_INICIAL);
        int r = (int)(135 + t * 100);
        int gv = (int)(206 - t * 130);
        int b = (int)(235 - t * 180);
        g2.setColor(new Color(Math.min(r,255), Math.max(gv,40), Math.max(b,20)));
        g2.fillRect(0, 0, worldWidth, worldHeight);

        // ── Sol intenso ──
        int solX = worldWidth - 200;
        int solY = 60;
        g2.setColor(new Color(255, 200, 50, 40));
        g2.fillOval(solX - 40, solY - 40, 140, 140);
        g2.setColor(new Color(255, 220, 80, 70));
        g2.fillOval(solX - 20, solY - 20, 100, 100);
        g2.setColor(new Color(255, 240, 60));
        g2.fillOval(solX, solY, 60, 60);

        // ── Ondulação de calor ──
        if (temperatura > 40f) {
            g2.setColor(new Color(255, 150, 50, (int)(t * 30)));
            for (int i = 0; i < worldWidth; i += 80) {
                int waveH = (int)(Math.sin(i * 0.02 + time * 0.000000008) * 8 * t);
                g2.fillRect(i, worldHeight - 120 + waveH, 70, 3);
            }
        }

        // ── Partículas de calor ──
        for (ParticulaCalor p : particulas) p.draw(g2);

        // ── Spots de plantio ──
        for (int i = 0; i < spotsPlantio.size(); i++) {
            spotsPlantio.get(i).draw(g2, arvorePerto == i, time);
        }
        
        // ── Halos de sombra das árvores ──
        for (SpotPlantio spot : spotsPlantio) {
            spot.drawSombraHalo(g2, RAIO_SOMBRA_ARVORE);
        }
        
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
    }

    // ******** HUD ********
    public void drawHUD(Graphics2D g2) {
        int count = 0;
        for (SpotPlantio spot : spotsPlantio) {
            if (spot.isArvorePlantada()) count++;
        }

        // Painel de fundo
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(10, 10, 230, 120, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(10, 10, 230, 120, 12, 12);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 13f));

        // Temperatura
        String tempStr = String.format("%.1f°C", temperatura);
        Color corTemp = temperatura < 38 ? new Color(100, 200, 255)
                      : temperatura < 50 ? new Color(255, 200, 50)
                      : new Color(255, 80, 30);
        g2.setColor(corTemp);
        g2.drawString("Temperatura: " + tempStr, 20, 32);

        // Barra de temperatura
        g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(20, 37, 160, 10, 4, 4);
        float tRatio = (temperatura - TEMPERATURA_INICIAL) / (TEMPERATURA_MAX - TEMPERATURA_INICIAL);
        int tempW = (int)(tRatio * 160);
        g2.setColor(corTemp);
        g2.fillRoundRect(20, 37, tempW, 10, 4, 4);

        // Energia
        g2.setColor(Color.WHITE);
        g2.drawString("Energia: " + (int)energia + "/" + ENERGIA_MAX, 20, 62);

        // Barra de energia
        g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(20, 67, 160, 10, 4, 4);
        float eRatio = energia / ENERGIA_MAX;
        Color corEn = eRatio > 0.5f ? new Color(80, 220, 80)
                    : eRatio > 0.25f ? new Color(255, 200, 50)
                    : new Color(255, 60, 60);
        g2.setColor(corEn);
        g2.fillRoundRect(20, 67, (int)(eRatio * 160), 10, 4, 4);

        // Árvores
        g2.setColor(new Color(100, 220, 80));
        g2.drawString("Árvores: " + count + "/" + NUM_ARVORES_PLANTIO, 20, 92);

        // Estado atual
        String estado = emSombra ? "Na Sombra" : "Pleno Sol";
        g2.setColor(emSombra ? new Color(180, 230, 100) : new Color(255, 180, 50));
        g2.drawString(estado, 20, 112);

        // Alerta crítico
        if (temperatura > 52f) {
            boolean pisca = System.currentTimeMillis() % 600 < 300;
            if (pisca) {
                g2.setColor(Color.RED);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 13f));
                g2.drawString("SUPERAQUECIMENTO!", 20, 128);
            }
        }
    }

    // ===== GETTERS =====
    public float getTemperatura() {
        return temperatura; 
    }
    
    public float getEnergia() {
        return energia; 
    }
    
    public float getEnergiaMax() {
        return ENERGIA_MAX; 
    }

    // ===== CLASSE INTERNA: PARTÍCULA DE CALOR =====
    private static class ParticulaCalor {
        int x, y;
        int vida;
        float vx, vy;

        ParticulaCalor(int x, int y) {
            this.x  = x;
            this.y  = y;
            this.vida = 60 + (int)(Math.random() * 60);
            this.vx = (float)(Math.random() * 2 - 1);
            this.vy = -(float)(Math.random() * 1.5 + 0.5);
        }

        void update() {
            x  += vx;
            y  += vy;
            vida--;
            vx *= 0.99f;
        }

        boolean morta() { return vida <= 0 || y < 0; }

        void draw(Graphics2D g2) {
            int alpha = Math.min(vida * 3, 120);
            g2.setColor(new Color(255, 100 + (int)(Math.random() * 80), 30, alpha));
            g2.fillOval(x, y, 4, 6);
        }
    }
}