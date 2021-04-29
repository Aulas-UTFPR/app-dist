package concorrencia;

public class Consumidor extends Thread {
	
	Loja loja;
	int numero;
	
	Consumidor(Loja a, int number) {
		loja = a;
		this.numero = number;
	}
	
	public void run() {
		int produto = 0;
		for (int i = 0; i < 10; i++) {
			produto = loja.get(this.numero);
			try {
				Thread.sleep((int) Math.random() * 100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}