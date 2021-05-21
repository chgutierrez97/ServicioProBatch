package ast.servicio.probatch.test;

import java.io.File;
import java.io.PrintWriter;

import ast.servicio.probatch.util.FileUtils;

public class DeleteFilesTest {
	public static void main(String[] args) {
		try {
			/* creo un grupo de archivos */
			for (int i = 0; i < 10; i++) {
				String fileName = "pid_" + (i + 1);
				File file = new File(fileName);
				if (!file.exists()) {
					file.createNewFile();

					PrintWriter printWriter = new PrintWriter(file);
					printWriter.println("este es un archivo de prueba");
					printWriter.flush();
					printWriter.close();
				}
			}

			FileUtils fileUtils = FileUtils.getInstance();
			File dir = new File("./");
			String regexp = "^pid_.*";
			fileUtils.deleteFiles(dir, regexp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// main
}// DeleteFilesTest
