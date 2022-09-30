package chamados;

import java.util.ArrayList;

public class Chamados {
	
	ArrayList<Integer> chamados = new ArrayList();
	boolean vazia = true;
	boolean cheia = false;
	static int serial = 0;
	
	public synchronized void mover() {
		int tamanho = chamados.size();
		int ultimo = tamanho - 1;
		int posterior;
		for (int i=0;i<tamanho;i++) {
			if (i<ultimo) {
				posterior = chamados.get(i+1);
				chamados.remove(i);
				chamados.add(i, posterior);
			} else {
				chamados.remove(i);
			}
		}
	}
	
	public synchronized void get(int quem) {
		while (vazia == true) {
			try {
				System.out.println("Nenhum chamado para o atendente "+quem);
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Chamado do cliente "+chamados.get(0)+" foi atendido pelo atendente "+quem);
		// MOVER
		mover();
		if (chamados.isEmpty()) {
			vazia = true;
		} else if (chamados.size() < 5) {
			cheia = false;
		}
		notifyAll();
	}
	
	public synchronized void put(int quem) {
		while (cheia == true) {
			try {
				System.out.println("Fila de chamados cheia para o cliente "+quem);
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		chamados.add(chamados.size(), quem);
		System.out.println("Cliente "+quem+" entrou na fila de chamados");
		if (chamados.size()>=5) {
			cheia = true;
		}
		vazia = false;
		notifyAll();
	}

}
