package ast.servicio.probatch.test.misc;

import java.io.*;
import java.net.Socket;

public class SendToProbatchTest {
	static long sleepTime;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		String svr = args.length > 0 ? args[0]:"192.168.2.219";
		int port = args.length > 1 ? Integer.parseInt(args[1]) : 9000;
		sleepTime = args.length > 2 ? Integer.parseInt(args[2]) : 10000;

		Socket socket = new Socket(svr, port);

		InputStream in = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String auth = reader.readLine();
		System.out.println("RECIBIDO:" + auth); // autenticacion

		OutputStream out = socket.getOutputStream();
		String authSend = "<autenticacion>%04%fa%5a%fa%7e%d2%2a%dc%4b%f0</autenticacion>";
		send(out, authSend);

		System.out.println(reader.readLine()); // latido

		String process = "<proceso id=\"3434996\" nombre=\"FOX\" categoria=\"batch\" clase=\"\" ><chdir>C:\\prueba</chdir><comando>cmd</comando><arg>conn.bat</arg><usuario clave=\"%90%5c%1f%b4%a0%33%61%93%cd%65%b8\">administrador</usuario></proceso>";

		//sendTillError(process, out, reader);

				send(out, process);
				waitForEnd(reader);
		
				Thread.sleep(1000);
		//		send(out, process);
		//		waitForEnd(reader);

		socket.close();
	}

	private static void sendTillError(String msg, OutputStream out, BufferedReader reader) throws IOException, InterruptedException {
		int counter = 1;
		
		//estado="falla"
		while (true) {
			System.out.println("ENVIANDO TRAMA " + counter++);
			send(out, msg);
			String last = waitForEnd(reader);
			if (last != null && last.contains("estado=\"falla\"")) {
				System.out.println("FALLA DETECTADA");
				return;
			}

			System.out.println("DURMIENDO " + sleepTime / 1000 + " segundos");
			Thread.sleep(sleepTime);
		}
	}

	private static void send(OutputStream out, String process) throws IOException {
		System.out.println("ENVIANDO: " + process);
		out.write(toBytes(process));
		out.flush();
	}

	private static String waitForEnd(BufferedReader reader) throws IOException {
		return waitFor("<fin", reader);
	}

	private static String waitFor(String what, BufferedReader reader) throws IOException {
		while (true) {
			String line = reader.readLine();
			if (line == null) { return null; }
			System.out.println(line);
			if (line.contains(what)) { return line; }
		}
	}

	public static byte[] toBytes(String s) throws UnsupportedEncodingException { return s.getBytes("UTF-8"); }

}
