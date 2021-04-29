package concorrencia;

public class Fabrica extends Thread {
	int numero;
	Loja loja;
	
	Fabrica(Loja a, int number) {
		loja = a;
		this.numero = number;
	}
	
	public void run() {
		for (int i=0; i<10; i++) {
			loja.serial++;
			loja.put(numero, loja.serial);
			try {
				Thread.sleep((int) Math.random() * 100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}