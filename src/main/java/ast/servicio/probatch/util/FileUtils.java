package ast.servicio.probatch.util;

import java.io.File;
import java.io.FilenameFilter;

import org.slf4j.LoggerFactory;

/**
 * Herramientas para manejo de archivos.
 * 
 * @author martin.zaragoza
 * 
 */
public class FileUtils {
	private static FileUtils fileUtils;

	private FileUtils() {
	}// FileUtils

	/**
	 * Obtiene la unica instancia de FileUtils.
	 * 
	 * @return obtiene la unica instancia de FileUtils.
	 */
	public static FileUtils getInstance() {
		fileUtils = fileUtils == null ? new FileUtils() : fileUtils;
		return fileUtils;
	}// getInstance

	/**
	 * Determina si un archivo es valido (si no es null y existe).
	 * 
	 * @param file
	 *            - Archivo a evaluar.
	 * @return True en caso que el archivo sea valido, false en caso contrario.
	 */
	public boolean fileIsValid(File file) {
		return file != null && file.exists();
	}// fileIsValid

	/**
	 * Retorna un arreglo de archivos a partir de un directorio y una expresion
	 * regular.
	 * 
	 * @param dir
	 *            - Directorio a obtener archivos.
	 * @param regexp
	 *            - Expresion regular usada para evaluar.
	 * @return arreglo de archivos a partir de un directorio y una expresion
	 *         regular.
	 */
	public File[] getFiles(File dir, final String regexp) {
		if (!fileIsValid(dir))
			return null;

		FilenameFilter filenameFilter = new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return name.matches(regexp);
			}
		};

		return dir.listFiles(filenameFilter);
	}// getFiles

	/**
	 * Borra un grupo de archivos dentro de un directorio a partir de una
	 * expresion regular.
	 * 
	 * @param dir
	 *            - Directorio a evaluar.
	 * @param regexp
	 *            - expresion regular a usar.
	 * @return True en caso de exito, false en caso contrario.
	 */
	public boolean deleteFiles(File dir, String regexp) {
		File[] files = getFiles(dir, regexp);
		if (files == null)
			return false;

		try {
			for (File file : files) {
				file.delete();
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error(
					"FileUtils::deleteFiles::ocurrio un error al borrar archivos de " + dir.getName() + " a partir de " + regexp + " : " + e.toString());
			return false;
		}

		return true;
	}// deleteFiles
}// FileUtils
