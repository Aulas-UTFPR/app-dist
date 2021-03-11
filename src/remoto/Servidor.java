package remoto;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Servidor {
	public static void main (String[] args) {
		try {
			AppRemoto app = new AppRemotoImpl();
			Naming.rebind("AppRemoto", app);
		} catch (Exception e) {
			System.out.println("Erro: "+e.getMessage());
		}
	}
}
