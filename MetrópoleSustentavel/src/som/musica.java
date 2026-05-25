package som;

import javax.sound.sampled.Clip;

public class musica {

    // static garante que o controle do som de fundo seja compartilhado por todo o jogo
    private static Clip clipFundo; 

    public void tocarMusica(String caminho, boolean emLoop) {
        new Thread(() -> {
            try {
                java.io.File arquivo = new java.io.File(caminho);
                if (arquivo.exists()) {
                    javax.sound.sampled.AudioInputStream stream = javax.sound.sampled.AudioSystem.getAudioInputStream(arquivo);
                    Clip novoClip = javax.sound.sampled.AudioSystem.getClip();
                    novoClip.open(stream);
                    
                    if (emLoop) {
                        novoClip.loop(Clip.LOOP_CONTINUOUSLY); 
                        clipFundo = novoClip; // Guarda a música de fundo para conseguir pará-la depois
                    }
                    
                    novoClip.start();
                } else {
                    System.out.println("Arquivo de som nao encontrado: " + caminho);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void pararMusica() {
        if (clipFundo != null && clipFundo.isRunning()) {
            clipFundo.stop();
        }
    }
}