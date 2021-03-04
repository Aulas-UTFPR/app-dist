package threads.aula;

public class PrintThreads {
	
	static Thread ta, tb;
	
	static void a(int end) {
		ta = new Thread() {
			public void run() {
				for (int i=1; i<=end; i++) {
					System.out.println("Thread A");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
	}
	
	static void b(int end) {
		tb = new Thread() {
			public void run() {
				for (int i=1; i<=end; i++) {
					System.out.println("Thread B");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
	}
	
	public static void main(String args[]) {
		int end = 10;
		a(end);
		b(end);
		ta.start();
		tb.start();
	}

}
