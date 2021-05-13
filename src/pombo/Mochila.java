package pombo;

public class Mochila {
	
	int qtdeCartas = 0;
	public static boolean executando = true;
	
	public synchronized void escrever(int id) {
		
	}
	
	public synchronized void esvaziar() {
		if (qtdeCartas < 20) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
