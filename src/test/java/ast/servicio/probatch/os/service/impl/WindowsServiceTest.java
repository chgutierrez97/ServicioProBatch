package ast.servicio.probatch.os.service.impl;

//import org.junit.Test;

import ast.servicio.probatch.exception.MensajeErrorException;

public class WindowsServiceTest {
//	//FIXME: falla  assertNotEquals(nombre, valor);
//	@Ignore
//    @Test
//    public void reemplazaExpRegArchivo() throws Exception {
//        WindowsService windowsService = new WindowsService();
//        String nombre = "testFiles\\archivo-*";
//
//        String valor = windowsService.reemplazaExpRegArchivo(nombre);
//        assertNotEquals(nombre, valor);
//        System.out.println(valor);
//
//        String nombreInvalido = "asd\\pepe";
//        String val = windowsService.reemplazaExpRegArchivo(nombreInvalido);
//        assertEquals(nombreInvalido, val);
//
//        nombre = "testFiles\\*_posting.txt";
//        valor = windowsService.reemplazaExpRegArchivo(nombre);
//        assertNotEquals(nombre, valor);
//        System.out.println(valor);
//    }
//
//    @Test
//    public void resuelveVariablesDeSistemaOk() throws Exception {
//        WindowsService windowsService = new WindowsService();
//
//        String v1 = System.getenv("OS");
//        String v2 = System.getenv("APPDATA");
//        String v3 = System.getenv("COMPUTERNAME");
//
//        String out = windowsService.resuelveVariablesDeSistema("Hola %OS% como %APPDATA% estas %COMPUTERNAME%");
//
//        String expected = String.format("Hola %s como %s estas %s", v1, v2, v3);
//
//        assertEquals(expected, out);
//    }
//
//    @Test
//    public void resuelveNingunaVariablesDeSistema() throws Exception {
//        WindowsService windowsService = new WindowsService();
//
//        String out = windowsService.resuelveVariablesDeSistema("Hola Como estas");
//
//        String expected = "Hola Como estas";
//
//        assertEquals(expected, out);
//    }
//
//    @Test
//    public void resuelveVariablesDeSistemaNoExistentes() throws Exception {
//        WindowsService windowsService = new WindowsService();
//
//        String out = windowsService.resuelveVariablesDeSistema("Hola %FOO% Como %BAR% estas");
//
//        String expected = "Hola %FOO% Como %BAR% estas";
//
//        assertEquals(expected, out);
//    }
//    
//    @Test
//    public void getPermisosUsuario() throws MensajeErrorException {
//    	WindowsService windowsService = new WindowsService();
//    	windowsService.getPermisosUsuario("D:\\BUGS_PROBATCH\\CFA\\Prueba_Permisos.bat");
//    }
//    
//    @Test
//    public void buscarUsuarioPermisosTest() throws MensajeErrorException {
//    	WindowsService windowsService = new AclWindowsService();
//    	windowsService.buscarUsuarioPermisos("floriana.villella", "r", "C:\\procesos", "ACCUSYSARGBSAS");
//    }
    
}