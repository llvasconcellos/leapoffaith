package br.com.devhouse;

import java.text.DecimalFormat;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class MainThread extends Thread {
	
	private static final String TAG = MainThread.class.getSimpleName();
	
	private boolean running;
	private SurfaceHolder surfaceHolder;
	private MainGamePanel gamePanel;
	
	//FPS desejado
	private final static int MAX_FPS = 50;
	
	//Numero máximo de frames para ser pulado (frameskip)
	private final static int MAX_FRAME_SKIPS = 5;
	
	//O tempo de uma frame
	private final static int FRAME_PERIOD = 1000 / MAX_FPS;
	
	
	/* DADOS PARA ESTATISTICAS */
	private DecimalFormat df = new DecimalFormat("0.##");  // 2 pontos decimais
	// vamos ler as estatisticas a cada segundo
	private final static int 	STAT_INTERVAL = 1000; //em micro segundos
	// a media sera calculada guardando o ultimo numero de FPS
	private final static int	FPS_HISTORY_NR = 10;
	// ultima vez que o status foi armazenado
	private long lastStatusStore = 0;
	// o contador de tempo do status
	private long statusIntervalTimer	= 0l;
	// numero de frames puladas desde que o jogo comecou
	private long totalFramesSkipped			= 0l;
	// numero de frames puladas em um ciclo de armazenamento (1 sec)
	private long framesSkippedPerStatCycle 	= 0l;

	// numero de frames renderizadas em um intervalo
	private int frameCountPerStatCycle = 0;
	private long totalFrameCount = 0l;
	// os ultimos valores de FPS
	private double 	fpsStore[];
	// numero de vezes que as estatisticas foram lidas
	private long 	statsCount = 0;
	// a media de FPS desde que o jogo comecou
	private double 	averageFps = 0.0;
	
	
	public MainThread(SurfaceHolder surfaceHolder, MainGamePanel gamePanel){
		super();
		this.surfaceHolder = surfaceHolder;
		this.gamePanel = gamePanel;
	}
	
	public void setRunning(boolean running){
		this.running = running;
	}
	
	@Override
	public void run(){
		Canvas canvas;
		
		Log.d(TAG, "Iniciando o Loop do Jogo");
		
		//inicializa os elementos de cronometragem para estatísticas
		initTimingElements();
		
		long beginTime; 	//a hora que o ciclo comecou
		long timeDiff;		//o tempo que levou para executar o ciclo
		int sleepTime;		//micro segundos para o sleep (< 0 se estivermos atrasados)
		int framesSkipped;	//numero de frames sendo puladas
		
		sleepTime = 0;
		
		while(running){
			canvas = null;
			
			//Tentativa de travar o canvas para edicao exclusiva de pixels na Surface
			try{
				canvas = this.surfaceHolder.lockCanvas();
				synchronized (surfaceHolder) {
					beginTime = System.currentTimeMillis();
					
					framesSkipped = 0; //reset da quantidade de frames puladas

					//Atualiza o estado do jogo
					this.gamePanel.update();
					
					//desenha o canvas no painel
					this.gamePanel.render(canvas);
					
					//calcula quanto tempo levou para o ciclo
					timeDiff = System.currentTimeMillis() - beginTime;
					
					//calcular o tempo para o sleep;
					sleepTime = (int)(FRAME_PERIOD - timeDiff);

					if (sleepTime > 0) {
						//se sleepTime > 0 esta tudo bem
						try{
							//Bota a Thread para dormir por um curto periodo de tempo.
							//Otimo para economizar bateria
							Thread.sleep(sleepTime);
						}
						catch (InterruptedException e) {}
					}
					
					while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
						//Estamos atrasados, precisamos alcancar!
						//Update sem rederizar
						this.gamePanel.update();
						
						//Adicionado o tempo da frame para a verificacao no proximo loop
						sleepTime += FRAME_PERIOD;
						framesSkipped++;
					}
					
					if (framesSkipped > 0) {
						Log.d(TAG, "Skipped:" + framesSkipped);
					}
					// para estatisticas
					framesSkippedPerStatCycle += framesSkipped;
					// chamada do metodo para armazenamento das estatisticas
					storeStats();
				}
			}
			catch(Exception e){
				Log.d(TAG, "erro de thread", e);
			}
			finally{
				//No caso de uma excessão a Surface nao eh deixada em um estado de inconsistencia
				if(canvas != null){
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}			
		}
	}
	
	/**
	 * As estatísticas - são chamadas todo ciclo. Verifica se o tempo deste o ultimo
	 * armazenamento é maior que o tempo de recolhimento das estatísticas (1 sec) e
	 * se for, calcula o FPS para o ultimo periodo e o armazena.
	 *
	 *  Ele rastreia o numero de frames por periodo. O numero de frames desde o
	 *  início do periodo são somadas e o cálculo só acontece no próximo periodo
	 *  e a contagem de frames é zerada.
	 */
	private void storeStats() {
		frameCountPerStatCycle++;
		totalFrameCount++;

		// verifica o tempo atual
		statusIntervalTimer += (System.currentTimeMillis() - statusIntervalTimer);

		if (statusIntervalTimer >= lastStatusStore + STAT_INTERVAL) {
			// calcula o tempo de frames por intervalo de verificação
			double actualFps = (double)(frameCountPerStatCycle / (STAT_INTERVAL / 1000));

			//armazena o ultipo FPS no array
			fpsStore[(int) statsCount % FPS_HISTORY_NR] = actualFps;

			// aumenta o numero de vezes que as estatísticas foram calculadas
			statsCount++;

			double totalFps = 0.0;
			// soma os valores de FPS
			for (int i = 0; i < FPS_HISTORY_NR; i++) {
				totalFps += fpsStore[i];
			}

			// obtem a média
			if (statsCount < FPS_HISTORY_NR) {
				// em caso dos primeiros 10 gatilhos
				averageFps = totalFps / statsCount;
			} else {
				averageFps = totalFps / FPS_HISTORY_NR;
			}
			// salvando o numero total de frames puladas
			totalFramesSkipped += framesSkippedPerStatCycle;
			// zerando os contadores depois do registro das estatísticas (1 sec)
			framesSkippedPerStatCycle = 0;
			statusIntervalTimer = 0;
			frameCountPerStatCycle = 0;

			statusIntervalTimer = System.currentTimeMillis();
			lastStatusStore = statusIntervalTimer;
//			Log.d(TAG, "Média de FPS:" + df.format(averageFps));
			gamePanel.setAvgFps("FPS: " + df.format(averageFps));
		}
	}

	private void initTimingElements() {
		// Inicializa os elementos de cronometragem
		fpsStore = new double[FPS_HISTORY_NR];
		for (int i = 0; i < FPS_HISTORY_NR; i++) {
			fpsStore[i] = 0.0;
		}
		Log.d(TAG + ".initTimingElements()", "Elementos de tempo inicializados");
	}

}
