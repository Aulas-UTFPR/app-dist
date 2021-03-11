package remoto;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AppRemotoImpl extends UnicastRemoteObject implements AppRemoto{

	protected AppRemotoImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void mensagem(String m) throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("Recebi a mensagem: "+m);
	}

}
