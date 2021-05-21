package ast.servicio.probatch.os.service.impl;
//
//import org.junit.Assert;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.mockito.*;
//
//import ast.servicio.probatch.configuration.Configurador;
//import ast.servicio.probatch.service.ServicioAgente;
//
//public class AS400ServiceTest {
//
//	@Ignore
//	@Test
//	public void testDirOrFileExists() {
//		AS400Service as400 = new AS400Service();
//		as400.setUser("CABOT");
//		as400.setPass("ACCUSYS");
//		
//		ServicioAgente servicio = Mockito.mock(ServicioAgente.class);
//		Configurador mock = Mockito.mock(servicio.cfg.getClass());
//		Mockito.when(mock.getaS400Server()).thenReturn("os/400");
//		Assert.assertTrue(as400.dirOrFileExists("/"));
//	}
//
//}
