package concorrencia;

public class Loja {
	
	int produto;
	boolean disponivel = false;
	static int serial = 0;
	
	public synchronized int get(int quem) { // Método usado pelo cliente para compra.
		while (disponivel == false) {
			try {
				wait(500); // Espera até receber uma notificação.
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Produto com número de série "+produto+" foi comprado por "+quem);
		disponivel = false;
		notifyAll(); // Notifica todos que estão esperando.
		return produto;
	}
	
	public synchronized void put(int quem, int serial) { // Insere no estoque.
		while (disponivel == true) {
			try {
				wait(500); // Espera até receber uma notificação.
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		produto = serial;
		System.out.println("Produto "+produto+" foi produzido por "+quem);
		disponivel = true;
		notifyAll(); // Notifica todos que estão esperando.
	}

}