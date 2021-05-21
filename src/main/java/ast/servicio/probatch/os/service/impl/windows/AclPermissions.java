//package ast.servicio.probatch.os.service.impl.windows;
//
//import java.nio.file.attribute.AclEntryPermission;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Set;
//
//public class AclPermissions {
//
//	private Set<AclEntryPermission> permissions;
//
//	public AclPermissions(Set<AclEntryPermission> permissions) {
//		this.permissions = permissions;
//	}
//
//	public String parsePermissions() {
//		if (hasFullControl())
//			return "rwx";
//		else if (canChange())
//			return "rw";
//		else
//			return "n";
//	}
//
//	private boolean canChange() {
//		return permissions.containsAll(Collections.unmodifiableCollection(Arrays.asList(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA)));
//	}
//
//	private boolean hasFullControl() {
//		return permissions.containsAll(
//				Collections.unmodifiableCollection(Arrays.asList(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA, AclEntryPermission.EXECUTE)));
//	}
//
//	@Override
//	public String toString() {
//		return "AclPermissions [permissions=" + permissions.toString() + "]";
//	}
//
//}
