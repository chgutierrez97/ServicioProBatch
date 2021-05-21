//package ast.servicio.probatch.os.service.impl.windows;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotEquals;
//
//import java.nio.file.attribute.AclEntryPermission;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.junit.Before;
//import org.junit.Test;
//
//public class AclPermissionsTest {
//
//	private AclPermissions aclPermissions;
//
//	@Before
//	public void setUp() {
//		Set<AclEntryPermission> permissions = new HashSet<AclEntryPermission>();
//		permissions.add(AclEntryPermission.WRITE_DATA);
//		permissions.add(AclEntryPermission.READ_DATA);
//		aclPermissions = new AclPermissions(permissions);
//	}
//
//	@Test
//	public void testParsePermissions() {
//		assertEquals("rw", aclPermissions.parsePermissions());
//		assertNotEquals("rwx", aclPermissions.parsePermissions());
//	}
//
//}
