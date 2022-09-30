package chamados;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Executar {
	
	public static void main(String args[]) {
		Chamados chamados = new Chamados();
		ExecutorService executor = Executors.newFixedThreadPool(10);
		executor.execute(new Cliente(chamados, 1));
		executor.execute(new Cliente(chamados, 2));
		executor.execute(new Cliente(chamados, 3));
		executor.execute(new Cliente(chamados, 4));
		executor.execute(new Cliente(chamados, 5));
//		executor.execute(new Produtor(armazem, 2));
		executor.execute(new Atendente(chamados, 1));
		executor.execute(new Atendente(chamados, 2));
		executor.execute(new Atendente(chamados, 3));
		executor.execute(new Atendente(chamados, 4));
		executor.execute(new Atendente(chamados, 5));
		executor.shutdown();
	}

}
