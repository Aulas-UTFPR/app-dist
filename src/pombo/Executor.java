package pombo;

public class Executor {
	
	public static void main(String args[]) {
	
		Mochila mochila = new Mochila();
		Pombo pombo = new Pombo(mochila);
		Usuario usuario1 = new Usuario(mochila);
		Usuario usuario2 = new Usuario(mochila);
		
		pombo.start();
		usuario1.start();
		usuario2.start();
	}
}
