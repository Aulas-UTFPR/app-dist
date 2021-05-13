package pombo;

public class Pombo extends Thread {
	
	Mochila mochila;
	
	public Pombo(Mochila m) {
		this.mochila = m;
	}
	
	public void run() {
		while (mochila.executando) {
				mochila.esvaziar();
				try {
					Thread.sleep((int) Math.random() * 2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}
