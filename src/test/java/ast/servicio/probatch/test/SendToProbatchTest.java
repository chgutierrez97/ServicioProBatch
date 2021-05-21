package ast.servicio.probatch.test;

import java.io.*;
import java.net.Socket;

public class SendToProbatchTest {
	public static void main(String[] args) throws IOException, InterruptedException {
		String svr = args.length > 0 ? args[0] : "127.0.0.1";
		int port = args.length > 1 ? Integer.parseInt(args[1]) : 5006;

		System.out.println("Ip: " + svr);
		System.out.println("Puerto: " + port);

		Socket socket = new Socket(svr, port);

		InputStream in = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String auth = reader.readLine();
		System.out.println("RECIBIDO:" + auth); // autenticacion

		OutputStream out = socket.getOutputStream();
		String authSend = "<autenticacion>%85%BE%98%0C%06%5D%A0%2E</autenticacion>";
		send(out, authSend);

		System.out.println(reader.readLine()); // latido

		//		String process = "<proceso id=\"3434996\" nombre=\"FOX\" categoria=\"batch\" clase=\"\" ><chdir>D:\\ProBatch\\datosPrueba\\windows</chdir><comando>cmd</comando><arg>sleeper.bat 2</arg><arg>4</arg><usuario clave=\"%bc%5e%0e%bb%bc%78%22%93%c7\">martin.zaragoza@accusysargbsas.local</usuario></proceso>";
		//		String process = "<proceso id=\"3434996\" nombre=\"CMD\" categoria=\"batch\" clase=\"\" ><chdir>D:\\ProBatch\\datosPrueba\\windows</chdir><comando>cmd</comando><arg>sleeper_ret_0.bat</arg><arg>4</arg><usuario clave=\"%bb%4a%10%a8%bc%78%22%93%ca\">franco.milanese@accusysargbsas.local</usuario></proceso>";
//		String process = "<proceso id=\"3434996\" nombre=\"CMD\" categoria=\"batch\" clase=\"\" ><chdir>D:\\ProBatch\\datosPrueba\\windows</chdir><comando>cmd</comando><arg>sleeper_ret_0.bat</arg><arg>2</arg><usuario clave=\"%b0%58%13%b2%a7%25%20%92%ce%63\">franco.milanese@accusysargbsas.local</usuario></proceso>";
//				String process = "<proceso id=\"3434996\" nombre=\"FOX\" categoria=\"batch\" clase=\"\" ><chdir>D:\\ProBatch\\datosPrueba\\windows</chdir><comando>cmd</comando><arg>sleeper_ret_0.bat</arg><arg>4</arg><usuario clave=\"%b0%5d%0e%a8%bf%78%22%93%c7\">martin.zaragoza@accusysargbsas.local</usuario></proceso>";
				String process = "<proceso id=\"3434996\" nombre=\"FOX\" categoria=\"batch\" clase=\"\" ><chdir>C:\\ProBatch\\datosPrueba\\windows</chdir><comando>cmd</comando><arg>sleeper_ret_0.bat</arg><arg>4</arg><usuario clave=\"%90%5c%1f%b4%a0%33%61%93%cd%65%b8\">Administrador</usuario></proceso>";

		sendTillError(process, out, reader);

		//		send(out, process);
		//		waitForEnd(reader);

		socket.close();
	}

	private static void sendTillError(String msg, OutputStream out, BufferedReader reader) throws IOException, InterruptedException {
		//estado="falla"
		int idx = 0;
		while (true) {
			System.out.println("ENVIANDO TRAMA " + ++idx);

			send(out, msg);
			String last = waitForEnd(reader);
			if (last != null && last.contains("estado=\"falla\"")) {
				System.out.println("FALLA DETECTADA");
				return;
			}

			int sleepTime = 1000 * 1;
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
