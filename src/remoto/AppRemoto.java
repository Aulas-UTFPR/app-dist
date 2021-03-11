package remoto;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AppRemoto extends Remote {
	public void mensagem(String m) throws RemoteException;
}
