package ast.servicio.probatch.os.service.recursive;


public class FactoryRecursiveKillers {

	public RecursiveKiller getKiller(String osSistem) {
		
		RecursiveKiller recursiveKiller = null;
				
		if(osSistem.contains("sunos")){
			recursiveKiller = new RecursiveKillerSolaris(); 
		}else if(osSistem.contains("unix")){
			recursiveKiller = new RecursiveKillerUnix();
		}else if (osSistem.contains("linux")){
			recursiveKiller = new RecursiveKillerLinux();
		}else if (osSistem.contains("windows")){
			recursiveKiller = new RecursiveKillerWindows();
		}
					
		return recursiveKiller;
		
	}

}
