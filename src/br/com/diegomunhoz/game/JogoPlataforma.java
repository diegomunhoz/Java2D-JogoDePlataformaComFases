package br.com.diegomunhoz.game;

import br.com.diegomunhoz.core.AudioManager;
import br.com.diegomunhoz.core.DataManager;
import br.com.diegomunhoz.core.Game;
import br.com.diegomunhoz.core.InputManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;

public class JogoPlataforma extends Game {

    // Modelo do jogo.
    CollisionDetector collisionDetector;
    ArrayList<Entidade> entidades;
    Entidade jogador;
    boolean gameOver;
    Point rolagem;
    Dimension mundo;
    String musica;
    String nextLevel;
    int transicao;

    public JogoPlataforma() {
        entidades = new ArrayList<Entidade>();
        collisionDetector = new CollisionDetector(entidades);
        gameOver = false;
        rolagem = new Point(0, 0);
        mundo = new Dimension();
    }

    @Override
    public void onLoad() {

        // Inicializa a fase.
        // Inclui o objeto do jogador (que inicia na posição (300,300)
        jogador = new EntidadeJogador(0, 0);
        nextLevel = "level1.dat";
    }

    @Override
    public void onUnload() {
        try {
            if (musica != null) {
                AudioManager.getInstance().loadAudio(musica).stop();
            }
        } catch (IOException ex) {
            System.out.println("2  " + ex.getClass().getName() + ":" + ex.
                    getMessage());
        }
    }

    @Override
    public void onUpdate(int currentTick) {
        if (nextLevel != null) {
            transicao++;
            if (transicao > 100) {
                carregaArquivo(nextLevel);
                nextLevel = null;
                transicao = 0;
            }
        } else {
            if (!gameOver) {
                for (Entidade e : entidades) {
                    e.update(currentTick);
                }
                collisionDetector.update(currentTick);
            }
            if (InputManager.getInstance().isPressed(KeyEvent.VK_ESCAPE)) {
                terminate();
            }
            rolagem.x = (int) jogador.pos.x - getWidth() / 2;
            rolagem.y = (int) jogador.pos.y - getHeight() / 2;
            if (rolagem.x < 0) {
                rolagem.x = 0;
            } else if (rolagem.x > mundo.width - getWidth()) {
                rolagem.x = mundo.width - getWidth();
            }
            if (rolagem.y < 0) {
                rolagem.y = 0;
            } else if (rolagem.y > mundo.height - getHeight()) {
                rolagem.y = mundo.height - getHeight();
            }
        }
    }

    @Override
    public void onRender(Graphics2D g) {

        g.setColor(Color.blue);
        g.fillRect(0, 0, 800, 600);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(-rolagem.x, -rolagem.y);
        for (Entidade e : entidades) {
            e.render(g2);
        }
        if (transicao > 0) {
            g.setColor(Color.black);
            g.fillRect(0, 0, 800, 600 / 100 * transicao);
        }
    }

    public void setNextLevel(String nextLevel) {
        this.nextLevel = nextLevel;
    }

    public void carregaArquivo(String fileName) {
        try {
            DataManager dm = new DataManager(getClass().getResource("/"
                    + fileName).toURI());
            entidades.clear();
            entidades.add(jogador);
            jogador.init();
            jogador.pos.x = 300;
            jogador.pos.y = 0;
            mundo.setSize(getWidth() + 10, getHeight() + 10);
            if (musica != null) {
                AudioManager.getInstance().loadAudio(musica).stop();
            }
            musica = dm.read("musica", musica);
            if (musica != null) {
                AudioManager.getInstance().loadAudio(musica).loop();
            }
            int qtd = 0;
            qtd = dm.read("plataformas", qtd);
            for (int i = 0; i < qtd; i++) {
                String fn = null;
                fn = dm.read("plataforma." + i + ".levelFileName", fn);
                Entidade e = null;
                if (fn == null) {
                    e = new EntidadePlataforma(0, 0, 0, 0);
                } else {
                    e = new EntidadeLink(0, 0, 0, 0, fn, this);
                }
                e.pos.x = dm.read("plataforma." + i + ".x", (int) e.pos.x);
                e.pos.y = dm.read("plataforma." + i + ".y", (int) e.pos.y);
                e.pos.width = dm.read("plataforma." + i + ".width",
                        (int) e.pos.width);
                e.pos.height = dm.read("plataforma." + i + ".height",
                        (int) e.pos.height);
                e.init();
                entidades.add(e);
                if (mundo.width < e.pos.x + e.pos.width) {
                    mundo.width = (int) (e.pos.x + e.pos.width);
                }
                if (mundo.height < e.pos.y + e.pos.height) {
                    mundo.height = (int) (e.pos.y + e.pos.height);
                }
            }
        } catch (Exception ex) {
            // Se não conseguir ler (der erro), nada faz.
        }
    }
}
