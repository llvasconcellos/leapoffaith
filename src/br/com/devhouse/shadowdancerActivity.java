package br.com.devhouse;

import br.com.devhouse.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class shadowdancerActivity extends Activity {
	
	private static final String TAG = shadowdancerActivity.class.getSimpleName();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Desligar o titulo da janela
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //Ativar fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        //setar o MainGamePanel como View
        setContentView(new MainGamePanel(this));
        
        //setContentView(R.layout.main);
        
        Log.d(TAG, "View Adicionada");
    }
    
    @Override
    protected void onDestroy(){
    	Log.d(TAG, "Destruindo...");
    	super.onDestroy();
    }
    
    @Override
    protected void onStop(){
    	Log.d(TAG, "Parando...");
    	super.onStop();
    }
}