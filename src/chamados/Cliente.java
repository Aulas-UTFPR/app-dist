package chamados;

public class Cliente extends Thread {
	int numero;
	Chamados chamados;
	
	Cliente(Chamados a, int number) {
		chamados = a;
		this.numero = number;
	}
	
	public void run() {
		for (int i=0; i<10; i++) {
			chamados.put(numero);
//			armazem.put(numero, i);
			try {
				Thread.sleep((int) Math.random() * 100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
