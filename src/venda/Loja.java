package venda;

public class Loja {
	
	boolean emEstoque = false;
		
	public synchronized void get(int idCliente) {
		while (!emEstoque) {
			try {
				System.out.println("Cliente "+idCliente+" na fila!");
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Um produto foi comprado pelo cliente "+idCliente);
		emEstoque = false;
		notifyAll();
	}
	
	public synchronized void put(int idFornecedor) {
		while (emEstoque) {
			try {
				System.out.println("Fornecedor "+idFornecedor+" na fila!");
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Um produto foi inserido pelo fornecedor "+idFornecedor);
		emEstoque = true;
		notifyAll();
	}

}
