package pombo;

public class Pombo extends Thread {
	
	Mochila mochila;
	
	public Pombo(Mochila m) {
		this.mochila = m;
	}
	
	public void run() {
		while (mochila.executando) {
			mochila.esvaziar();
		}
	}

}
