package threads.aula;

public class PrintThreads {
	
	static Thread ta = new Thread() {
		public void run() {
			for (int i=0; i<10; i++) {
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
	
	static Thread tb = new Thread() {
		public void run() {
			for (int i=0; i<10; i++) {
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


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ta.start();
		tb.start();
	}

}
