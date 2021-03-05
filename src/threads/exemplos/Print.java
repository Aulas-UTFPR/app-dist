package threads.exemplos;
public class Print {
	
	void a(int end) {
		for (int i=1; i<=end; i++) {
			System.out.println("Thread A");
		}
	}
	
	void b(int end) {
		for (int i=1; i<=end; i++) {
			System.out.println("Thread B");
		}
	}
	
	public static void main(String args[]) {
		int end = 10;
		Print p = new Print();
		p.a(end);
		p.b(end);
	}

}