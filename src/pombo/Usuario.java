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
		while (mochila.executando) {
			mochila.escrever(idUsuario);
		}
	}

}
