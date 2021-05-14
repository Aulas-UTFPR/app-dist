package pombo;

public class Executor {
	
	public static void main(String args[]) {
	
		Mochila mochila = new Mochila();
		Thread pombo = new Pombo(mochila);
		Thread usuario1 = new Usuario(mochila);
		Thread usuario2 = new Usuario(mochila);
		
		pombo.start();
		usuario1.start();
		usuario2.start();
	}
}
