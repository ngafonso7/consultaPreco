package com.example.teste;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	Button conectar;
	Button etiqueta;
	Button reposicao;
	EditText txtCodigo;
	EditText txtDescricao;
	EditText txtPreco;
	EditText txtEstoque;
	Connection con;
	
	String codBarra = "";
	String codIn = "";
	String descr = "";
	String preco = "";
	int estoque = 0;
	
	
	static String ipServer = "192.168.0.125";
	//static String ipServer = "192.168.0.171";
	static String portaServer = "12345";
	
	static Socket server = null;
	
	public Boolean testServer = true;
	public Boolean insert = false;
	
	InputStream entrada = null;
	OutputStream saida = null;
	
	static ProgressDialog progressBar;
	private boolean progressBarStatus = false;
	private Handler progressBarHandler = new Handler();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conectar = (Button) findViewById(R.id.button1);
        etiqueta = (Button) findViewById(R.id.button2);
        reposicao = (Button) findViewById(R.id.botaoReposicao);
        
        txtCodigo = (EditText) findViewById(R.id.txtCodigo);
        txtDescricao = (EditText) findViewById(R.id.txtDescricao);
        txtPreco = (EditText) findViewById(R.id.txtPreco);
        txtEstoque = (EditText) findViewById(R.id.txtEstoque);
        
        
        txtDescricao.setEnabled(false);
        txtPreco.setEnabled(false);
        txtEstoque.setEnabled(false);
        
        txtDescricao.setBackgroundColor(Color.WHITE);
        txtDescricao.setTextColor(Color.BLACK);
        txtDescricao.setTextSize(35);
        
        txtPreco.setBackgroundColor(Color.WHITE);
        txtPreco.setTextColor(Color.BLACK);
        txtPreco.setTextSize(60);
        txtPreco.setGravity(Gravity.CENTER);
        
        txtEstoque.setBackgroundColor(Color.WHITE);
        txtEstoque.setTextColor(Color.BLACK);
        txtEstoque.setTextSize(60);
        txtEstoque.setGravity(Gravity.CENTER);
        
        progressBar = new ProgressDialog(MainActivity.this);
		progressBar.setCancelable(true);
		progressBar.setMessage("Aguarde ...");
		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressBar.setProgress(0);
		progressBar.setMax(20);
        
		etiqueta.setEnabled(false);
		reposicao.setEnabled(false);
		
        conectar(ipServer,portaServer);
        
        
        
        
        txtCodigo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				txtCodigo.setText("");
				
				
			}
		});
        
        txtCodigo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
					txtCodigo.setSelection(0, txtCodigo.getText().length());
			}
		});

        
        txtCodigo.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if(arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER & arg2.getAction() == KeyEvent.ACTION_DOWN)
					pesquisar();
				return false;
			}
		});
        
        conectar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				pesquisar();
				
			}
		});
        
        etiqueta.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	        	builder
	        	.setTitle("Impressão de Etiqueta")
	        	.setMessage("Deseja mesmo imprimir a etiqueta?")
	        	.setNegativeButton("YES", new DialogInterface.OnClickListener() {
	        		public void onClick(DialogInterface dialog, int which) {			      	
	        			imprimirEtiqueta();
	        		}
	        	})
	        	.setPositiveButton("NO", new DialogInterface.OnClickListener() {
	        		public void onClick(DialogInterface dialog, int which) {			      	

	        		}
	        	})
	        	.show();
			}
		});
        reposicao.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(MainActivity.this.estoque > 0)
					reposicaoProduto();
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		        	builder
		        	.setTitle("Reposição")
		        	.setMessage("Produto sem estoque.")
		        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {
		        		public void onClick(DialogInterface dialog, int which) {			      	
		        			
		        		}
		        	})
		        	.show();
				}
			}
		});


         
        
    }
    
    public void conectar(String ip,String porta){
    	try{
    		aguarde_init();
    		server = new Socket(ip, Integer.valueOf(porta));
    		
    		entrada = server.getInputStream();
    		saida = server.getOutputStream();
    		if(testServer)
    		{
    			enviar("Test Server");
    			testServer = false;
    		}
    		
    		String palavra = "";
    		
    	    Boolean conexaoOk = false;
    		
    		while(!conexaoOk)
    		{
    			int res = entrada.read();
    			if((char)res != '\n')
    			{
    				palavra = palavra + (char)res;
    			}
    			else
    			{
    				conexaoOk = true;
    				if(palavra.compareTo("Conexao OK")!=0)
    				{
    					Toast.makeText(MainActivity.this, "Servidor não respondeu", 1).show();
    					System.exit(0);
    				}
    			}
    		}
    		aguarde_end();
    		
    	}catch(Exception e){
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder
        	.setTitle("Sem conexão")
        	.setMessage("Servidor não responde !!!")
        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {			      	
        			System.exit(0);
        		}
        	})
        	.show();
    		
    	}
    	
    }

    
    public void enviar(String msg){
    	try{
	    	if(server != null)
	    	{
	    		byte[] b = (msg+"\n").getBytes();
	    		
	    		if(saida != null)
	    		{
	    			
	    			saida.write(b);
	    			saida.flush();
	    		}
	    	}
    	}catch(Exception e){
    		Toast.makeText(MainActivity.this, e.toString(), 2).show();
    	}
    }
    
    
    
    public void receber(){
    	try{
    		aguarde_init();
    		Thread.sleep(1000);
    		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))  
            .hideSoftInputFromWindow(txtCodigo.getWindowToken(), 0);
	    	if(server != null) 
	    	{
	    		if(entrada != null)
	    		{
	    			int res;
	    			Boolean recebendo = true;
	    			String palavra="";
	    			while(recebendo)
	    			{
	    				res = entrada.read();
	    				if((char)res != '\n' & res != -1)
	    				{
	    					palavra += (char)res;
	    				}
	    				else if (res == -1)
	    				{
	    					AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			        	builder
    			        	.setTitle("Consulta Preço")
    			        	.setMessage("Servidor não responde")
    			        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				        		public void onClick(DialogInterface dialog, int which) {			      	
				        			System.exit(0);
				        		}
				        	})
    			        	.show();
    						txtDescricao.setText("");
	    					txtEstoque.setText("");
	    					txtPreco.setText("");
	    					txtCodigo.setSelection(0, txtCodigo.getText().length());
	    					this.codBarra = "";
	    					this.descr = "";
	    					this.preco = "";
	    					this.codIn = "";
	    					this.estoque =0;
    						recebendo = false;
    						etiqueta.setEnabled(false);
    						reposicao.setEnabled(false);
	    				}
	    				else
	    				{
	    					palavra.replace("\n","");
	    					if(palavra.compareTo("Encontrado")==0)
	    					{
	    						palavra = "";
	    						while(recebendo)
	    		    			{
	    		    				res = entrada.read();
	    		    				if((char)res != '\n')
	    		    				{
	    		    					palavra += (char)res;
	    		    				}
	    		    				else
	    		    				{
	    		    					aguarde_end();
	    		    					palavra.replace("\n","");
	    		    					String[] dados = palavra.split(";");
	    		    					txtCodigo.setText(dados[0]);
	    		    					txtDescricao.setText(dados[1]);
	    		    					txtEstoque.setText(dados[2]);
	    		    					txtPreco.setText(dados[3]);
	    		    					this.codBarra = dados[0];
	    		    					this.descr = dados[1];
	    		    					this.preco = dados[3];
	    		    					this.codIn = dados[4];
	    		    					this.estoque = Integer.parseInt(dados[2]);
	    		    					recebendo = false;
	    		    					txtCodigo.setSelection(0, txtCodigo.getText().length());
	    		    					etiqueta.setEnabled(true);
	    		    					reposicao.setEnabled(true);
	    		    				}
	    		    			}
	    					}
	    					else
	    					{
	    						AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    			        	builder
	    			        	.setTitle("Consulta Preço")
	    			        	.setMessage("Produto não cadastrado")
	    			        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					        		public void onClick(DialogInterface dialog, int which) {			      	
					        		
					        		}
					        	})
	    			        	.show();
	    						txtDescricao.setText("");
		    					txtEstoque.setText("");
		    					txtPreco.setText("");
		    					txtCodigo.setSelection(0, txtCodigo.getText().length());
		    					this.codBarra = "";
		    					this.descr = "";
		    					this.preco = "";
		    					this.codIn = "";
		    					this.estoque = 0;
	    						recebendo = false;
	    						aguarde_end();
	    						etiqueta.setEnabled(false);
	    						reposicao.setEnabled(false);
	    					}
	    				}
	    			}
	    		}
	    	}
    	}catch(Exception e){
    		Toast.makeText(MainActivity.this, e.toString(), 2).show();
    	}
    }

    public void imprimirEtiqueta()
    {
    	try {
			conectar(ipServer,portaServer);
			String msg = "ETQ\n"+this.codIn+";"+this.codBarra+";"+this.descr+";"+this.preco+"\n";
			enviar(msg);
    	}catch(Exception e){
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder
        	.setTitle("Sem conexão")
        	.setMessage("Servidor não responde !!!")
        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {			      	
        			System.exit(0);
        		}
        	})
        	.show();
    	}
    	
    }
    
    public void pesquisar()
    {
    	try {
			conectar(ipServer,portaServer);
			if(txtCodigo.getText().toString().compareTo("")!=0)
			{
				enviar(txtCodigo.getText().toString());
				receber();
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder
	        	.setTitle("Consulta Preço")
	        	.setMessage("Digite um código interno/barras")
	        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {
	        		public void onClick(DialogInterface dialog, int which) {			      	
	        		
	        		}
	        	})
	        	.show();
			}
		} catch (Exception e) {
			Toast.makeText(MainActivity.this, e.getMessage(), 2).show();
			
		}
    }
    
    public void reposicaoProduto()
    {
    	try {
			conectar(ipServer,portaServer);

        	final String codigo = this.codIn;
        	final String descri = this.descr;
        	
        	SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
        	Date d = new Date(System.currentTimeMillis());
        	
        	final String data = simpleFormat.format(d);
        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        	
			builder.setTitle("Quantidade do Produto: " + descri);
			
			insert = false;
			// Set up the input
			final EditText input = new EditText(MainActivity.this);
			
			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL );

			builder.setView(input);

			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.cancel();
			    }
			});
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	if(insert == false)
			    	{
				    	insert = true;
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
			            .hideSoftInputFromWindow(input.getWindowToken(), 0);
						String quanti = "";
						if((quanti = input.getText().toString()).compareTo("")!= 0)
						{	
							if(Integer.parseInt(quanti) <= MainActivity.this.estoque)
							{
								enviar("RRP\n"+data+";"+codigo+";"+descri+";"+quanti+"\n");
								dialog.cancel();
							}
							else
							{
								dialog.cancel();
								AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	    			        	builder
	    			        	.setTitle("Reposição Produto")
	    			        	.setMessage("Quantidade maior que estoque atual")
	    			        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					        		public void onClick(DialogInterface dialog, int which) {			      	
					        		
					        		}
					        	})
	    			        	.show();
							}
						}
						else
						{
							dialog.cancel();
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    			        	builder
    			        	.setTitle("Reposição Produto")
    			        	.setMessage("Quantidade não informada")
    			        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				        		public void onClick(DialogInterface dialog, int which) {			      	
				        		
				        		}
				        	})
    			        	.show();
						}
							
			    	}
			    }
			});

			final AlertDialog alert = builder.create();
			
			input.setOnKeyListener(new View.OnKeyListener() {
			
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
					if (keyCode == 66 && insert == false && keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getRepeatCount() ==0) 
					{
						insert = true;
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
			            .hideSoftInputFromWindow(input.getWindowToken(), 0);
						String quanti = "";
						if((quanti = input.getText().toString()).compareTo("")!= 0)
						{	
							if(Integer.parseInt(quanti) <= MainActivity.this.estoque)
							{
								enviar("RRP\n"+data+";"+codigo+";"+descri+";"+quanti+"\n");
								alert.cancel();
							}
							else
							{
								alert.cancel();
								AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	    			        	builder
	    			        	.setTitle("Reposição Produto")
	    			        	.setMessage("Quantidade maior que estoque atual")
	    			        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					        		public void onClick(DialogInterface dialog, int which) {			      	
					        		
					        		}
					        	})
	    			        	.show();
							}
						}
						else
						{
							alert.cancel();
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    			        	builder
    			        	.setTitle("Reposição Produto")
    			        	.setMessage("Quantidade não informada")
    			        	.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				        		public void onClick(DialogInterface dialog, int which) {			      	
				        		
				        		}
				        	})
    			        	.show();
						}
					}
					return false;
				}
			});
			
			alert.show();

    	} catch (Exception e) {
			Toast.makeText(MainActivity.this, e.getMessage(), 2).show();
			
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	try{
        		server.shutdownOutput();
        		server.close();
        		
        		System.exit(0);
        	}catch(Exception e){
        		
        	}
        	
        }

        return super.onKeyDown(keyCode, event);
    }
    
    public void aguarde_init(){
    	// prepare for a progress bar dialog
		
		progressBar.show();
 
		//reset progress bar status
		progressBarStatus = true;
		new Thread(new Runnable() {
			  public void run() {
				while (progressBarStatus==true) {


				  // your computer is too fast, sleep 1 second
				  try {
					Thread.sleep(200);
				  } catch (InterruptedException e) {
					e.printStackTrace();
				  }

				  // Update the progress bar
				  progressBarHandler.post(new Runnable() {
					public void run() {
					  progressBar.setProgress(0);
					}
				  });
				}
				progressBar.dismiss();
			  }
		       }).start();
		
    }
    public void aguarde_end(){
    	progressBarStatus = false;
    }
    
}
