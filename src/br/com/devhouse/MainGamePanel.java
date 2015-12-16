package br.com.devhouse;

import br.com.devhouse.model.Ninja;
import br.com.devhouse.model.components.Speed;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MainGamePanel extends SurfaceView implements Callback {
	
	private static final String TAG = MainGamePanel.class.getSimpleName();
	
	private MainThread thread;
	
	private String avgFps;
	
	private Ninja ninja;
	private Ninja inimigo;

	public MainGamePanel(Context context) {
		super(context);
		
		//Adicionando o addCallback(this) para o SurfaceHolder para interceptar eventos
		getHolder().addCallback(this);
		
		//carrega o ninja
		ninja = new Ninja(BitmapFactory.decodeResource(getResources(), R.drawable.ninja_01), 100, 100);
		
		//carrega o ninja
		inimigo = new Ninja(BitmapFactory.decodeResource(getResources(), R.drawable.armored_monster), 900, 100);
		inimigo.getSpeed().setXv(5);
		inimigo.getSpeed().setYv(5);
		
		//Cria o processo de loop principal
		thread = new MainThread(getHolder(), this);
		
		//Fazer com que MainGamePanel ganhe foco para manipular eventos
		setFocusable(true);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface sendo destruida");
		
		//diz para o thread terminar e aguarda o seu termino
		boolean retry = true;
		while(retry){
			try{
				thread.join();
				retry = false;
			}
			catch(InterruptedException e){
				//tente novamente finalizando o processo
			}
		}
		Log.d(TAG, "Thread foi finalizado limpamente");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			
			//delegando a manipulacao do evento ao ninja
			ninja.handleActionDown((int) event.getX(), (int) event.getY());
						
			//Verifica se está na parte mais inferior da tela para sair do programa.
			if(event.getY() > getHeight() - 50){
				thread.setRunning(false);
				((Activity)getContext()).finish();
			}
			else{
				Log.d(TAG, "Coords: x=" + event.getX() + ", y=" + event.getY());
			}
		}
		
		if(event.getAction() == MotionEvent.ACTION_MOVE){
			//os gestos
			if(ninja.isTouched()){
				//O ninja foi segurado e esta sendo arrastado
				ninja.setX((int) event.getX());
				ninja.setY((int) event.getY());
			}
		}
		
		if(event.getAction() == MotionEvent.ACTION_UP){
			//termino do toque
			if(ninja.isTouched()){
				ninja.setTouched(false);
			}
		}
		
		return true;
	}
	
	public void setAvgFps(String avgFps){
		this.avgFps = avgFps;
	}

	protected void render(Canvas canvas){
		canvas.drawColor(Color.BLACK);
		
		//canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fundo_noite), 0, -1500, null);
		
		inimigo.draw(canvas);
				
		ninja.draw(canvas);
		
		//exibe o FPS
		displayFps(canvas, avgFps);
	}
	
	private void displayFps(Canvas canvas, String fps){
		if(canvas != null && fps != null){
			Paint paint = new Paint();
			paint.setARGB(255, 255, 255, 255);
			canvas.drawText(fps, this.getWidth() - 50, 20, paint);
		}
	}
	
	public void update(){
		//verifica colisao com a parede da direita se estiver indo para a direita
		if((inimigo.getSpeed().getxDirection() == Speed.DIRECTION_RIGHT) && (inimigo.getX() + inimigo.getBitmap().getWidth() / 2 >= getWidth())){
			inimigo.getSpeed().toggleXDirection();
		}
		
		//verifica colisao com a parede da esquerda se estiver indo para a esquerda
		if((inimigo.getSpeed().getxDirection() == Speed.DIRECTION_LEFT) && (inimigo.getX() - inimigo.getBitmap().getWidth() / 2 <= 0)){
			inimigo.getSpeed().toggleXDirection();
		}
		
		//verifica colisao com a parede inferior se estiver indo para baixo
		if((inimigo.getSpeed().getyDirection() == Speed.DIRECTION_DOWN) && (inimigo.getY() + inimigo.getBitmap().getHeight() / 2 >= getHeight())){
			inimigo.getSpeed().toggleYDirection();
		}
		
		//verifica colisao com a parede superior se estiver indo para cima
		if((inimigo.getSpeed().getyDirection() == Speed.DIRECTION_UP) && (inimigo.getY() - inimigo.getBitmap().getHeight() / 2 <= 0)){
			inimigo.getSpeed().toggleYDirection();
		}
		
		inimigo.update();
	}
}
