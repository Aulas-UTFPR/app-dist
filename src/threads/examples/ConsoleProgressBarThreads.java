package threads.examples;

public class ConsoleProgressBarThreads {
	
	static int complete = 0;
	static boolean finished = false;
	
	public static void progressPercentage(int remain, int total) {
	    if (remain > total) {
	        throw new IllegalArgumentException();
	    }
	    int maxBareSize = 20; // 5 units for 100%
	    int remainProcent = ((400 * remain) / total) / maxBareSize;
	    char defaultChar = '-';
	    String icon = "*";
	    String bare = new String(new char[maxBareSize]).replace('\0', defaultChar) + "]";
	    StringBuilder bareDone = new StringBuilder();
	    bareDone.append("[");
	    for (int i = 0; i < remainProcent; i++) {
	        bareDone.append(icon);
	    }
	    String bareRemain = bare.substring(remainProcent, bare.length());
	    System.out.print("\r" + bareDone + bareRemain + " " + remainProcent * 5 + "% ");
	    if (remain == total) {
	        System.out.print("\n");
	    }
	}
	
	public void task() { // Simulação de uma tarefa que leva um certo tempo para executar.
		ConsoleProgressBarThreads.complete += 5;
		if (ConsoleProgressBarThreads.complete >= 100) {
			finished = true;
		}
	}
	
	public void progressBar(int done) {
		    progressPercentage(done, 100);
		    try {
		        Thread.sleep(500);
		    } catch (Exception e) {
		    }
	}
	
	public static void main(String args[]) throws InterruptedException {
		ConsoleProgressBarThreads cpbar = new ConsoleProgressBarThreads();
		ConsoleProgressBarThreads.complete = 0;
		while(!ConsoleProgressBarThreads.finished) {
			cpbar.task();
			cpbar.progressBar(ConsoleProgressBarThreads.complete);
			Thread.sleep(500);
		}
	}
}
