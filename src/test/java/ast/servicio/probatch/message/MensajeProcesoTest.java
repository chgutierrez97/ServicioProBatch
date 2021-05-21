package ast.servicio.probatch.message;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ast.servicio.probatch.exception.MensajeErrorException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import ast.servicio.probatch.configuration.Configurador;
import ast.servicio.probatch.service.ServicioAgente;
//import junit.framework.TestCase;
import org.xml.sax.SAXException;

public class MensajeProcesoTest /*extends TestCase*/ {

	/*public void testXmlToObject() throws Exception {
		ServicioAgente.cfg = new Configurador("mantis.conf");

		 String mensajeEntrada = "<proceso id=\"96555\" nombre=\"TESTENTORNO\" categoria=\"batch\" clase=\"\">\r\n" + 
		 		"	<chdir>/opt/ProbatchJavaDemonioSolaris/pbwin/</chdir>\r\n" + 
		 		"	<comando>printvars</comando>\r\n" + 
		 		"	<usuario clave=\"%c7%a8%e2%b0%dd%55%44%e3%c8%06\">martin</usuario>\r\n" + 
		 		"	<arg>frula.sh</arg>\r\n" + 
		 		"	<entorno>\r\n" + 
		 		"		<var nombre=\"VAR1\">MAURICIO</var>\r\n" + 
		 		"		<var nombre=\"VAR2\">SANCHEZ</var>\r\n" + 
		 		"	</entorno>\r\n" + 
		 		"	<patron>\r\n" + 
		 		"		<ignorar tipo=\"re\">Oracle</ignorar>\r\n" + 
		 		"		<ignorar tipo=\"re\">stty</ignorar>\r\n" + 
		 		"	</patron>\r\n" + 
		 		"</proceso>";*/

//		String mensajeEntrada = "<proceso id=\"96555\" nombre=\"TESTENTORNO\" categoria=\"batch\" clase=\"\">\r\n"
//				+ "	<chdir>/opt/ProbatchJavaDemonioSolaris/pbwin/</chdir>\r\n" + "	<comando>printvars</comando>\r\n"
//				+ "	<usuario clave=\"%c7%a8%e2%b0%dd%55%44%e3%c8%06\">martin</usuario>\r\n" + "	<arg>frula.sh</arg>\r\n" + "</proceso>";

		/*MensajeProceso mensajeProceso = new MensajeProceso(mensajeEntrada);

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(mensajeEntrada));

		Document doc = db.parse(is);

		mensajeProceso.XmlToObject(doc);
	}

	public void testXmlToObjectWithInterfaces() throws ParserConfigurationException, IOException, SAXException, MensajeErrorException {
		ServicioAgente.cfg = new Configurador("mantis.conf");

		String msg = "<proceso id=\"96555\" nombre=\"DUMMYPROCESS\" categoria=\"batch\" clase=\"\">\n" + "    <chdir>/home/martin/bin</chdir>\n"
				+ "    <comando>/bin/bash</comando>\n" + "    <arg>/home/martin/bin/dummySleeper.sh</arg>\n" + "    <arg>5</arg>\n"
				+ "    <usuario clave=\"%c7%a8%e2%b0%dd%55%44%e3%c8%06\">martin</usuario>\n" + "    <interfaces>\n"
				+ "        <entrada controlar_todos=\"1\" tiempo_expiracion=\"0\" tiempo_reintento=\"0\">/home/martin/Documents/*-posting.txt</entrada>\n"
				+ "    </interfaces>\n" + "</proceso>\n";

		MensajeProceso mensajeProceso = new MensajeProceso(msg);

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(msg));

		Document doc = db.parse(is);

		mensajeProceso.XmlToObject(doc);

	}*/

}