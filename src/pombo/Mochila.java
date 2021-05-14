package pombo;

public class Mochila {
	
	int qtdeCartas = 0;
	public static boolean executando = true;
	
	public synchronized void escrever(int id) {
		while (qtdeCartas >= 5) {
			try {
				System.out.println("Mochila cheia... usuário "+id+" esperando...");
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Usuario "+id+" escreveu uma carta... ");
		this.qtdeCartas++;
		System.out.println("Qtde de cartas: "+qtdeCartas);
		notifyAll();
	}
	
	public synchronized void esvaziar() {
		while (qtdeCartas < 5) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			System.out.println("Pombo viajando...");
			Pombo.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Mochila esvaziada! "+qtdeCartas);
		this.qtdeCartas = 0;
		notifyAll();
	}

}
