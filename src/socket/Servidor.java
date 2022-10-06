package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Servidor {

    static ServerSocket serverSocket;
    boolean bloqueada = false;
    Map<String, Socket> usuariosLogados = new HashMap<>();

    Servidor(int porta) throws IOException {
        serverSocket = new ServerSocket(porta);
    }

    public ServerSocket getServer() {
        return serverSocket;
    }

    public void addLogado(String usuario, Socket s) {
        usuariosLogados.put(usuario, s);
    }

    public Map<String, Socket> getLogados() {
        return usuariosLogados;
    }

    public void removeLogado(String usuario) {
        usuariosLogados.remove(usuario);
    }

    public boolean getBloqueada() {
        return bloqueada;
    }

    public void bloquearDesbloquear() {
        bloqueada = !bloqueada;
    }

    public void listen() {
        int i = 0;
        while (true) {
            i++;
            System.out.println("Nova thread " + i);
            bloquearDesbloquear(); // TRUE
            Thread cliente = new Thread() {
                String nome = "";

                public void run() {
                    try {
                        String ID = "";
                        Socket clientSocket = getServer().accept();
                        System.out.println("Conexao aberta!");
                        // addSocket(clientSocket);
                        OutputStream saida = clientSocket.getOutputStream();
                        saida.write("Digite nome::NOME para logar\n".getBytes());
                        bloquearDesbloquear(); // FALSE
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        InputStreamReader inStreamReader = new InputStreamReader(clientSocket.getInputStream()); // entrada
                        BufferedReader entrada = new BufferedReader(inStreamReader);
                        // OutputStream saida = clientSocket.getOutputStream();// canal de saida
                        boolean fechar = false;
                        while (!fechar) {
                            try {
                                String recebido = entrada.readLine();
                                // System.out.println(recebido);
                                String comando[] = recebido.split("::");
                                if (comando.length < 2) {
                                    System.out.println("Formato da mensagem incorreto!");
                                } else {
                                    switch (comando[0]) {
                                        case "nome":
                                            nome = comando[1];
                                            ID = nome + clientSocket.toString();
                                            System.out.println("Usuario logado: " + nome);
                                            addLogado(ID, clientSocket);
                                            saida.write("Logado com sucesso!\n".getBytes());
                                            break;
                                        case "escrever":
                                            String msg = nome + " escreveu: " + comando[1] + "\n";
                                            for (Entry<String, Socket> es : getLogados().entrySet()) {
                                                if (!es.getValue().isClosed()) {
                                                    es.getValue().getOutputStream().write(msg.getBytes());
                                                }
                                            }
                                            break;
                                        case "grupo":
                                            // Enviar mensagem para grupo
                                            break;
                                        case "fechar":
                                            System.out.println("Fechando o socket de " + nome);
                                            String saiuMsg = nome + " saiu!\n";
                                            removeLogado(ID);
                                            for (Entry<String, Socket> es : getLogados().entrySet()) {
                                                if (!es.getValue().isClosed()) {
                                                    es.getValue().getOutputStream().write(saiuMsg.getBytes());
                                                }
                                            }
                                            fechar = true;
                                            break;
                                        default:
                                            System.out.print("Comando incorreto!");
                                    }
                                }
                            } catch (NullPointerException npe) {
                                npe.printStackTrace();
                            }
                        }
                        clientSocket.close();
                        System.out.println("Conexao fechada!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } // aceitar conexao
                }
            };
            cliente.start();
            while (bloqueada) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String args[]) throws IOException {
        int porta = 9000;
        Servidor server = new Servidor(porta);
        server.listen();
    }
}
