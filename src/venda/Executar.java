package venda;

public class Executar {

	public static void main(String args[]) {
		Loja loja = new Loja();
		Fornecedor fornecedor = new Fornecedor(loja,1);
		Cliente c1 = new Cliente(loja,1);
		Cliente c2 = new Cliente(loja,2);
		
		fornecedor.start();
		c1.start();
		c2.start();
	}
	
	
}
