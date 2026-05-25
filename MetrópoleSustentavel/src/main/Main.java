package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
    	som.musica somfundo = new som.musica();
    	somfundo.tocarMusica("src/som/musica.wav", true); // ADICIONADO ", true" AQUI
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame();
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setResizable(false);
            janela.setTitle("Metrópole Sustentável");

            Painel_Jogo painel = new Painel_Jogo();
            janela.add(painel);
            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);

            painel.startGameThread();
        });
    }
}
