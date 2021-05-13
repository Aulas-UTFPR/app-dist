package pombo;

public class Mochila {
	
	int qtdeCartas = 0;
	public static boolean executando = true;
	
	public synchronized void escrever(int id) {
		while (qtdeCartas >= 20) {
			try {
				System.out.println("Mochila cheia... usuário "+id+" esperando...");
				wait(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Usuario "+id+" escreveu uma carta...");
		qtdeCartas++;
		notifyAll();
	}
	
	public synchronized void esvaziar() {
		while (qtdeCartas < 20) {
			try {
				System.out.println("Pombo esperando cartas...");
				wait(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Mochila esvaziada!");
		qtdeCartas = 0;
		notifyAll();
	}

}
