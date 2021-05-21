package ast.servicio.probatch.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ast.servicio.probatch.domain.Atributo;
import ast.servicio.probatch.domain.EstadoProceso;
import ast.servicio.probatch.domain.ParametrosProceso;
import ast.servicio.probatch.message.MensajeError;
import ast.servicio.probatch.message.MensajeValidacion;
import ast.servicio.probatch.os.service.OsServiceFactory;

/**
 * @author marcos.barroso
 */
public class Utils {
    public static Logger logger;
    private static String STRING_EMPTY = " ";
    private static String PUTNO_COMA = ";";
    private static Map<String, String> statusMap = new HashMap<String, String>();

    static {
        try {
            logger = LoggerFactory.getLogger(MensajeValidacion.class);
        } catch (Exception e) {
            System.err.println("ast.servicio.probatch.util.Utils::imposible inicializar logger");
        }
    }

    /**
     * Consturctor que genera el xml basado en el mensaje de entrada, valida
     * ademas que el mensaje sea valido.
     *
     * @param xmlMessage
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws MensajeError
     */
    public static Document parsearMensaje(String xmlMessage) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        Document tramaXml = builder.parse(new InputSource(new StringReader(xmlMessage)));
        return tramaXml;

    }

    public static boolean esNumerico(String input) { // devuelve true si el
        // String Input consiste
        // solo de numeros
        return input.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
    }

    /**
     * Xor para desencriptar o encriptar clave.
     *
     * @param key - Clave en arch de config.
     * @param id  - id a concatenar.
     * @param arg - Texto encriptado o a encriptar.
     * @return
     */
    public static byte[] xorstr(String key, String id, String arg) {

        byte[] resultado = null;
        byte[] decryptArg = hex2Byte(arg);
        byte messageDigest[];
        byte[] s;

        String key_id = key + id;

        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(key_id.getBytes());

            messageDigest = algorithm.digest();

            int la = decryptArg.length;
            int lk = messageDigest.length;

            int tamS = (messageDigest.length * (la / lk)) + (la % lk);
            s = new byte[tamS];

            int base = 0;
            for (int i = 0; i < (la / lk); i++) {
                base = messageDigest.length * i;
                for (int j = 0; j < messageDigest.length; j++)
                    s[base + j] = messageDigest[j];

            }

            base = tamS - (la % lk);
            for (int j = 0; j < (la % lk); j++)
                s[base + j] = messageDigest[j];

            int minimo = Math.min(decryptArg.length, s.length);

            resultado = new byte[minimo];

            for (int i = 0; i < minimo; i++) {
                resultado[i] = (byte) (decryptArg[i] ^ s[i]);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(5);
        }

        return resultado;
    }

    /**
     * Convierte un arreglo de bytes en un String con caracteres hexadecimales.
     *
     * @param b - Arreglo de bytes a parsear.
     * @return String con caracteres hexadecimales.
     */
    public static String byte2hex(byte[] b) {
        // String Buffer can be used instead
        String hs = "";
        String stmp = "";

        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));

            if (stmp.length() == 1) {
                hs = hs + "%" + "0" + stmp;
            } else {
                hs = hs + "%" + stmp;
            }

            if (n < b.length - 1) {
                hs = hs + "";
            }
        }

        return hs;
    }

    public static byte[] hex2Byte(String str) {
        String str1 = str.replaceAll("%", "");
        byte[] bytes = new byte[str1.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(str1.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    /**
     * Obtiene todos los segmentos que estan separados por el delim y los guarda
     * en una lista. Por ejemplo: "string1/string2/string3/..../stringN" el
     * metodo arma una lista de n elementos (del tipo String) y cada uno de sus
     * elementos va a ser (0:string1,1:string2,2:string3,.....n-1:stringN)
     *
     * @param cadena
     * @param delim
     * @return
     */
    public static List<String> obtenerCadenas(String cadena, String delim) {
        List<String> lista = new ArrayList<String>();
        StringTokenizer tokens = new StringTokenizer(cadena, delim);
        while (tokens.hasMoreElements()) {
            lista.add(tokens.nextToken().trim());
        }
        return lista;

    }

    /**
     * ejemplo: "hola/chau/chau2", usando el delimitador "/" se obtiene la
     * cadena segunda: "chau"
     *
     * @param cadena
     * @param delim
     * @return
     */
    public static String obtenerSegundaCad(String cadena, String delim) {
        StringTokenizer tokens = new StringTokenizer(cadena, delim);
        String cadDer = null;
        tokens.nextToken();
        if (tokens.hasMoreElements())
            cadDer = tokens.nextToken();
        return cadDer;

    }

    /**
     * ejemplo: "hola/chau2/chau3", usando el delimitador "/" se obtiene la
     * cadena tercera: "chau3"
     *
     * @param cadena
     * @param delim
     * @return
     */
    public static String obtenerTerceraCad(String cadena, String delim) {

        StringTokenizer tokens = new StringTokenizer(cadena, delim);
        String terceraCadena = null;
        tokens.nextToken();
        if (tokens.hasMoreElements())
            tokens.nextToken();
        if (tokens.hasMoreElements())
            terceraCadena = tokens.nextToken();
        return terceraCadena;

    }

    /**
     * ejemplo: "hola/chau2/chau3/chau4", usando el delimitador "/" se obtiene
     * la cadena tercera: "chau4"
     *
     * @param cadena
     * @param delim
     * @return
     */
    public static String obtenerCuartaCad(String cadena, String delim) {

        StringTokenizer tokens = new StringTokenizer(cadena, delim);
        String cuartaCadena = null;
        tokens.nextToken();
        if (tokens.hasMoreElements())
            tokens.nextToken();
        if (tokens.hasMoreElements())
            tokens.nextToken();
        if (tokens.hasMoreElements())
            cuartaCadena = tokens.nextToken();
        return cuartaCadena;

    }

    /**
     * valida existencia de un archivo
     *
     * @param nombreArchivo
     * @return
     */
    public static boolean validarExistenciaArchivo(String nombreArchivo) {
        File file = new File(nombreArchivo);
        return file.exists();
    }

    /**
     * Obtiene un parametro (nombreParametro) de una determinada tramaXML
     * (trama). Por ejemplo si deseo obtener el parametro "desde" que esta
     * dentro de la trama; "<estado desde="1" hasta="3"/>" el resultado del
     * metodo va a ser un String que tiene como valor "1"
     *
     * @param trama
     * @param nombreParametro
     * @return
     */
    public static String obtenerParametroTramaString(String trama, String nombreParametro) {
        String resultado = null;
        if (trama.contains(nombreParametro)) {
            resultado = trama.substring(trama.indexOf(nombreParametro));
            resultado = resultado.substring(resultado.indexOf('"') + 1, resultado.indexOf('"', resultado.indexOf('"') + 1));
        }
        return resultado;

    }

    /**
     * Este metodo arma un String con todos los archivos no encontrados despues
     * de validar la existencia de cada uno de ellos. Los arma separadaos por
     * ";", de esta manera:
     * archivo_no_encontrado_1;archivo_no_encontrado_2;....;
     * archivo_no_encontrado_N
     *
     * @param cadena
     * @return
     */
    public static String archivosNoEncontrados(String cadena) {
        String resultado = "";
        List<String> lista = obtenerCadenas(cadena, ";");
        for (String texto : lista) {
            texto = texto.replace("%d", Utils.seccionaFecha("dd"));
            texto = texto.replace("%m", Utils.seccionaFecha("MM"));
            texto = texto.replace("%y", Utils.seccionaFecha("yy"));
            texto = texto.replace("%Y", Utils.seccionaFecha("yyyy"));
            if (texto.contains("*")) {
                texto = OsServiceFactory.getOsService().reemplazaExpRegArchivo(texto);
            }
            if (!validarExistenciaArchivo(texto))
                resultado = resultado + texto + ";";
        }
        if (resultado.endsWith(";")) {
            resultado = resultado.substring(0, resultado.length() - 1);
        }
        return resultado;
    }

    /**
     * ejemplo: "hola/chau", usando el delimitador "/" se obtiene la cadena
     * izquierda: hola
     *
     * @param cadena
     * @param delim
     * @return
     */
    public static String obtenerCadIzquierda(String cadena, String delim) {
        StringTokenizer tokens = new StringTokenizer(cadena, delim);
        String cadIzq = tokens.nextToken();
        return cadIzq;
    }

    /**
     * ejemplo: "hola/chau", usando el delimitador "/" se obtiene la cadena
     * derecha: chau
     *
     * @param cadena
     * @param delim
     * @return
     */
    public static String obtenerCadDerecha(String cadena, String delim) {
        String cadDer = null;
        StringTokenizer tokens = new StringTokenizer(cadena, delim);
        tokens.nextToken();
        if (tokens.hasMoreElements())
            cadDer = tokens.nextToken();
        return cadDer;
    }

    /**
     * este metodo busca insertar a la expresion regular el caracter = "\\" ya
     * que java lo omite a la hora de traerlo del archivo properties
     * <p>
     * lo que java toma = ^[x00-x20]*$ lo que java deberia tomar =
     * ^[\\x00-\\x20]*$
     *
     * @param regex
     * @return
     */

    public static String formatearRegex(String regex) {

        StringBuilder nuevoRegex = new StringBuilder();
        char[] array = regex.toCharArray();

        for (int i = 0; i < array.length; i++) {

            nuevoRegex.append("\\");
            nuevoRegex.append(array[i]);

            nuevoRegex.append(array[i]);

        }
        return nuevoRegex.toString();
    }

    /**
     * ejemplo: "string1/string2/..../stringN", usando el delimitador "/" se
     * obtiene la ultima cadena: stringN
     *
     * @param cadena
     * @param delim
     * @return
     */
    public static String obtenerUltimoSegmento(String cadena, String delim) {
        String ultimaCadena = null;
        StringTokenizer tokens = new StringTokenizer(cadena, delim);
        while (tokens.hasMoreElements())
            ultimaCadena = tokens.nextToken();
        return ultimaCadena;
    }

    public static boolean validaExpresionesRegulares(String cadenaInput, String expresion) {
        if (expresion == null || "".equals(expresion)) {
            return false;
        }
        //String s = "\"cadenaInput\": " + cadenaInput + "||||" + "\"expresion\": " + expresion;
        String s = String.format("%s => %s ", cadenaInput, expresion);

        if (cadenaInput == null) {
            cadenaInput = "";
        }

        boolean matches = false;

        Pattern p = Pattern.compile(expresion);
        Matcher m = p.matcher(cadenaInput);
        if (m.matches()) {
            logger.debug(s + " => REGEXP OK!");
            matches = true;
        } else {
            logger.debug(s + " => REGEXP FALLA!");
            matches = false;
        }

        return matches;
    }

    public static boolean validaLiterales(String cadenaInput, String expresion) {
        boolean matches = true;
        try {
            matches = expresion.equals(cadenaInput.trim());
            if (matches) logger.debug(cadenaInput + " => " + expresion + " => LITERAL OK!");
            else logger.debug(cadenaInput + " => " + expresion + " => LITERAL FALLA!");
        } catch (Exception ex) {
            logger.debug("Error al comparar expresiones literales" + ex.getMessage());
            return false;
        }
        return matches;
    }

    public static boolean validarMensajeCobolLiteral(String cadenaInput, String expresion) {
        String s = cadenaInput + " => " + expresion;

        if (expresion.contains(cadenaInput)) {
            logger.debug( s + " => EVALUACION OK!");
            return true;
        }
        logger.debug( s + " => EVALUACION FALLA!");
        return false;
    }

    public static String seccionaFecha(String seccion) {
        Date fechaActual = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat(seccion);
        return "" + dateFormat.format(fechaActual);
    }

    public static String ultimoModificado(File[] listaInterfaces) {

        File ultimaInterfaz = listaInterfaces[0];

        for (int i = 0; i < listaInterfaces.length; i++) {
            if (listaInterfaces[i].lastModified() > ultimaInterfaz.lastModified()) {
                ultimaInterfaz = listaInterfaces[i];
            }
        }

        return ultimaInterfaz.toString();
    }

    public static List<EstadoProceso> levantarEstados(String fileName) {
        List<EstadoProceso> estadoMensajes = Collections.synchronizedList(new LinkedList<EstadoProceso>());
        File file = new File(fileName);
        String leido = "";
        if (file.exists()) {
            try {
                BufferedReader bufferedReader;
                bufferedReader = new BufferedReader(new FileReader(file));
                leido = bufferedReader.readLine();
                bufferedReader.close();

                estadoMensajes = stringEstadosToListaEstados(leido);

            } catch (IOException e) {
                logger.error("Error al leer el archivo de estados guardado: " + e.getMessage());
            }
        }

        return estadoMensajes;
    }

    public static List<EstadoProceso> stringEstadosToListaEstados(String stringEstados) {
        EstadoProceso estadoProceso;
        String id;
        String nombre;
        long ts;
        int pid;
        Integer estado;
        String estadoString;
        List<EstadoProceso> estadoMensajes = Collections.synchronizedList(new LinkedList<EstadoProceso>());

        if (!"".equals(stringEstados) && stringEstados != null) {
            stringEstados = stringEstados.replace("<estado-guardado>", "");
            stringEstados = stringEstados.replace("</estado-guardado>", "");

            stringEstados = stringEstados.replaceAll("</transicion>", "|");

            for (String estadoGuardado : obtenerCadenas(stringEstados, "|")) {
                id = obtenerParametroTramaString(estadoGuardado, "id");
                nombre = obtenerParametroTramaString(estadoGuardado, "nombre");
                ts = new Long(obtenerParametroTramaString(estadoGuardado, "ts"));
                pid = 0;
                estadoString = obtenerParametroTramaString(estadoGuardado, "estado");

                if ("exito".equals(estadoString))
                    estado = 0;
                else if ("muerte".equals(estadoString))
                    estado = -9999;
                else if ("falla".equals(estadoString))
                    estado = 1;
                else
                    estado = null;

                estadoProceso = new EstadoProceso(id, nombre, ts, pid, estado);
                estadoProceso.setDump(true);

                estadoMensajes.add(estadoProceso);
            }

        }

        return estadoMensajes;
    }

    // FIXME : NUNCA ES INVOCADO. PROVOCA RECURSIVIDAD INFINITA
    // public static boolean matar(int pid) {
    // try {
    // Utils.matar(pid);
    // OsServiceFactory.getOsService().executeCommand((OsServiceFactory.getOsService().getKillCommand(pid)));
    // } catch (Exception e) {
    // logger.error("Error al matar el proceso con pid: " + pid);
    // logger.trace(e.getMessage());
    // return false;
    // }
    // return true;
    // }

    public static String getArrayCommands(ParametrosProceso pParameters) {

        StringBuilder parametersBuilder = new StringBuilder();
        parametersBuilder.append(pParameters.getComando() + STRING_EMPTY);

        if (pParameters.getArgumentos() != null) {
            for (Iterator<Atributo> iterator = pParameters.getArgumentos().iterator(); iterator.hasNext(); ) {
                Atributo atributo = iterator.next();
                parametersBuilder.append(atributo.getValor() + STRING_EMPTY);
            }
        }
        parametersBuilder.toString().trim();
        parametersBuilder.append(PUTNO_COMA);
        return parametersBuilder.toString();
    }

    public static boolean clearGarbageSolaris(String cadena) {

        boolean isclean = true;

        String solaris_label = "Oracle Corporation";
        if (cadena.indexOf(solaris_label) != -1) {
            isclean = false;
        }

        return isclean;
    }
}
