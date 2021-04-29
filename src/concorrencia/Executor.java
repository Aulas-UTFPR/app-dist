package concorrencia;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Executor {
	
	public static void main(String args[]) {
		Loja loja = new Loja();
		ExecutorService executor = Executors.newFixedThreadPool(3);
		executor.execute(new Fabrica(loja, 1));
		executor.execute(new Consumidor(loja, 1));
		executor.execute(new Consumidor(loja, 2));
		executor.shutdown();
	}

}