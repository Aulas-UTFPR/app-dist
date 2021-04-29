package venda;

public class Fornecedor extends Thread {

	Loja loja;
	int numero;
	
	Fornecedor(Loja l, int n) {
		loja = l;
		numero = n;
	}
	
	public void run() {
		for (int i=0;i<20;i++) {
			loja.put(numero);
			try {
				Thread.sleep((int) Math.random() * 100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
