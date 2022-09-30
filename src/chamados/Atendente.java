package chamados;

public class Atendente extends Thread {
	
	Chamados chamados;
	int numero;
	
	Atendente(Chamados a, int number) {
		chamados = a;
		this.numero = number;
	}
	
	public void run() {
		for (int i = 0; i < 10; i++) {
			chamados.get(this.numero);
			try {
				Thread.sleep((int) Math.random() * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
