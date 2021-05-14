package pombo;

public class Usuario extends Thread {
	
	Mochila mochila;
	static int id = 0;
	int idUsuario = 0;
	
	public Usuario(Mochila m) {
		this.mochila = m;
		id++;
		this.idUsuario = id;
	}
	
	public void run() {
		System.out.println("Executando usuario "+idUsuario+"...");
		while (mochila.executando) {
			mochila.escrever(idUsuario);
			try {
				Integer t = (int) Math.random() * 500 + 1000;
				Thread.sleep(t);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
