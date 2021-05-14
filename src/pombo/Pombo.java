package pombo;

public class Pombo extends Thread {
	
	Mochila mochila;
	
	public Pombo(Mochila m) {
		this.mochila = m;
	}
	
	public void run() {
		System.out.println("Executando pombo...");
		while (mochila.executando) {
				System.out.println("Pombo esperando...");
				mochila.esvaziar();
		}
	}
}
