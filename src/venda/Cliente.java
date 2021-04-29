package venda;

public class Cliente extends Thread {

	Loja loja;
	int numero;
	
	Cliente(Loja l, int n) {
		loja = l;
		numero = n;
	}
	
	public void run() {
		for (int i=0;i<10;i++) { 
			loja.get(numero);
			try {
				Thread.sleep((int) Math.random() * 100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
