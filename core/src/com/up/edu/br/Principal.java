package com.up.edu.br;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Principal extends ApplicationAdapter {

	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture cano_baixo;
	private Texture cano_cima;
	private Texture game_over;

	private ShapeRenderer shape_renderer;
	private Circle circulo_passaro;
	private Rectangle retangulo_cano_cima;
	private Rectangle retangulo_cano_baixo;

	private float largura_disp;
	private float altura_disp;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoY_inicial_passaro = 0;
	private float espaco_entre_canos;

	private float posicaoX_cano;
	private float posicaoY_cano;
	private Random random;

	private int pontos = 0;
	private int pontuacao_maxima = 0;
	private boolean passou_cano;
	Preferences preferencias;

	private int status_jogo = 0;

	private float posicaoX_passaro = 0;

	BitmapFont textPontuacao;
	BitmapFont textReiniciar;
	BitmapFont textMelhorPontuacao;

	Sound som_voando;
	Sound som_colisao;
	Sound som_pontuacao;

	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	@Override
	public void create () {
		inicializarTexturas();
		validarPontos();
		inicializarObjetos();
	}

	@Override
	public void render () {
//		Gdx.gl.glClearColor(1, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//limpar frames criados anteriormente
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}

	private void verificarEstadoJogo(){
		boolean toqueTela = Gdx.input.justTouched();

		if (status_jogo == 0){
			if (toqueTela){
				gravidade = -15;
				status_jogo = 1;
				som_voando.play();
			}
		} else if (status_jogo == 1){
			if (toqueTela){
				gravidade = -15;
				som_voando.play();
			}

			posicaoX_cano -= Gdx.graphics.getDeltaTime() * 200;
			if (posicaoX_cano < -cano_cima.getWidth()){
				posicaoX_cano = largura_disp;
				posicaoY_cano = random.nextInt(800) - 400;
				passou_cano = false;
			}

			if (posicaoY_inicial_passaro > 0 || toqueTela)
				posicaoY_inicial_passaro -= gravidade;

			if (posicaoY_inicial_passaro >= altura_disp)
				posicaoY_inicial_passaro = altura_disp-passaros[0].getHeight();

			gravidade++;
		} else if (status_jogo == 2) {
//			if (posicaoY_inicial_passaro > 0 || toqueTela)
//				posicaoY_inicial_passaro -= gravidade;
//			gravidade++;

			if (pontos > pontuacao_maxima){
				pontuacao_maxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacao_maxima);
			}

			posicaoX_passaro = Gdx.graphics.getDeltaTime()*500;

			if (toqueTela){
				status_jogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoX_passaro = 0;
				posicaoY_inicial_passaro = altura_disp / 2;
				posicaoX_cano = largura_disp;
			}
		}
	}

	private void desenharTexturas(){
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		batch.draw(fundo, 0, 0, largura_disp, altura_disp);
		batch.draw(passaros[(int) variacao], 50 + posicaoX_passaro, posicaoY_inicial_passaro);

		batch.draw(cano_baixo, posicaoX_cano, altura_disp/2 - cano_baixo.getHeight() - espaco_entre_canos/2 + posicaoY_cano);
		batch.draw(cano_cima, posicaoX_cano, altura_disp/2 + espaco_entre_canos/2 + posicaoY_cano);

		textPontuacao.draw(batch, String.valueOf(pontos), largura_disp/2-50, altura_disp-110);

		if (status_jogo == 2){
			batch.draw(game_over, largura_disp/2-game_over.getWidth()/2, altura_disp/2);
			textReiniciar.draw(batch, "Toque para reiniciar", largura_disp/2-200, altura_disp/2-game_over.getHeight()/2);
			textMelhorPontuacao.draw(batch, "Recorde: " + pontuacao_maxima, largura_disp/2-140, altura_disp/2-game_over.getHeight() - 70);
		}

		batch.end();
	}

	public void detectarColisoes(){
		circulo_passaro.set(50 + posicaoX_passaro + passaros[0].getWidth()/2, posicaoY_inicial_passaro + passaros[0].getWidth()/2, passaros[0].getWidth()/2);
		retangulo_cano_cima.set(posicaoX_cano, altura_disp/2 + espaco_entre_canos/2 + posicaoY_cano, cano_cima.getWidth(), cano_cima.getHeight());
		retangulo_cano_baixo.set(posicaoX_cano, altura_disp/2 - cano_baixo.getHeight() - espaco_entre_canos/2 + posicaoY_cano, cano_baixo.getWidth(), cano_baixo.getHeight());


		if (Intersector.overlaps(circulo_passaro, retangulo_cano_cima) ||
			Intersector.overlaps(circulo_passaro, retangulo_cano_baixo)){
			if (status_jogo == 1){
				som_colisao.play();
				status_jogo = 2;
			}
		}

		//Apenas para visualizar as boxes de colisão
//		shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
//
//		shape_renderer.circle(50 + passaros[0].getWidth()/2, posicaoY_inicial_passaro + passaros[0].getWidth()/2, passaros[0].getWidth()/2);
//		shape_renderer.rect(posicaoX_cano, altura_disp/2 + espaco_entre_canos/2 + posicaoY_cano, cano_cima.getWidth(), cano_cima.getHeight());
//		shape_renderer.rect(posicaoX_cano, altura_disp/2 - cano_baixo.getHeight() - espaco_entre_canos/2 + posicaoY_cano, cano_baixo.getWidth(), cano_baixo.getHeight());
//
//		shape_renderer.end();
	}

	private void inicializarTexturas(){
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		cano_baixo = new Texture("cano_baixo_maior.png");
		cano_cima = new Texture("cano_topo_maior.png");

		game_over = new Texture("game_over.png");
	}

	private void inicializarObjetos(){
		batch = new SpriteBatch();
		random = new Random();

		largura_disp = VIRTUAL_WIDTH;
		altura_disp = VIRTUAL_HEIGHT;
		posicaoY_inicial_passaro = altura_disp/2;
		posicaoX_cano = largura_disp;
		espaco_entre_canos = 400;

		textPontuacao = new BitmapFont();
		textPontuacao.setColor(Color.WHITE);
		textPontuacao.getData().setScale(10);

		textReiniciar = new BitmapFont();
		textReiniciar.setColor(Color.GREEN);
		textReiniciar.getData().setScale(3);

		textMelhorPontuacao = new BitmapFont();
		textMelhorPontuacao.setColor(Color.RED);
		textMelhorPontuacao.getData().setScale(3);

		shape_renderer = new ShapeRenderer();
		circulo_passaro = new Circle();
		retangulo_cano_baixo = new Rectangle();
		retangulo_cano_cima = new Rectangle();

		som_voando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		som_colisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		som_pontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		preferencias = Gdx.app.getPreferences("weatherBird");
		pontuacao_maxima = preferencias.getInteger("pontuacaoMaxima", 0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	private void validarPontos(){
		if (posicaoX_cano < 50-passaros[0].getWidth()){
			if (!passou_cano){
				pontos++;
				passou_cano = true;
				som_pontuacao.play();
			}
		}

		// getDeltaTime = tempo de diferença entre uma renderização e outra
		variacao += Gdx.graphics.getDeltaTime() * 10;

		if (variacao > 3)
			variacao = 0;
	}

	@Override
	public void dispose () {
//		batch.dispose();
//		img.dispose();
	}
}
