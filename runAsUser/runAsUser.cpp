// runAsUser.cpp : Defines the entry point for the console application.
//
#pragma region Includes and Manifest Dependencies
#include <windows.h>
#include <iostream>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "runAsUser.h"
#include "stdafx.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// The one and only application object

CWinApp theApp;

using namespace std;

#define GENERIC_ACCESS (GENERIC_READ | GENERIC_WRITE | GENERIC_EXECUTE | GENERIC_ALL)

#define WINSTA_ALL                                                                                                                          \
  (WINSTA_ACCESSCLIPBOARD | WINSTA_ACCESSGLOBALATOMS | WINSTA_CREATEDESKTOP | WINSTA_ENUMDESKTOPS | WINSTA_ENUMERATE | WINSTA_EXITWINDOWS | \
   WINSTA_READATTRIBUTES | WINSTA_READSCREEN | WINSTA_WRITEATTRIBUTES | DELETE | READ_CONTROL | WRITE_DAC | WRITE_OWNER)

#define DESKTOP_ALL                                                                                                                        \
  (DESKTOP_CREATEMENU | DESKTOP_CREATEWINDOW | DESKTOP_ENUMERATE | DESKTOP_HOOKCONTROL | DESKTOP_JOURNALPLAYBACK | DESKTOP_JOURNALRECORD | \
   DESKTOP_READOBJECTS | DESKTOP_SWITCHDESKTOP | DESKTOP_WRITEOBJECTS | DELETE | READ_CONTROL | WRITE_DAC | WRITE_OWNER)

//
//   FUNCTION: GetLogonSID(HANDLE, PSID *)
//
//   PURPOSE: Obtain the logon SID of the user.
//
//   PARAMETERS:
//   * hToken -
//   * ppSid -
//
BOOL GetLogonSID(HANDLE hToken, PSID* ppSid) {
  BOOL fSucceeded = FALSE;
  DWORD cbTokenGroups = 0;
  PTOKEN_GROUPS pTokenGroups = NULL;
  DWORD dwIndex = 0;
  DWORD dwLength = 0;
  fSucceeded = false;
  int codiError = 0;

  // Get required buffer size and allocate the TOKEN_GROUPS buffer.
  if (!GetTokenInformation(hToken, TokenGroups, NULL, 0, &cbTokenGroups)) {
    if (ERROR_INSUFFICIENT_BUFFER != GetLastError()) {
      codiError = GetLastError();
      cout << "\nError al GetTokenInformation " << codiError;
      return codiError;
    }

    pTokenGroups = (PTOKEN_GROUPS)LocalAlloc(LPTR, cbTokenGroups);
    if (pTokenGroups == NULL) {
      codiError = GetLastError();
      cout << "\nError al LocalAlloc " << codiError;
      return codiError;
    }
  }

  // Get the token group information from the access token.
  if (!GetTokenInformation(hToken, TokenGroups, pTokenGroups, cbTokenGroups, &cbTokenGroups)) {
    codiError = GetLastError();
    cout << "\nError al GetTokenInformation" << codiError;
    return codiError;
  }

  // Loop through the groups to find the logon SID.
  for (dwIndex = 0; dwIndex < pTokenGroups->GroupCount; dwIndex++) {
    if ((pTokenGroups->Groups[dwIndex].Attributes & SE_GROUP_LOGON_ID) == SE_GROUP_LOGON_ID) {
      // Found the logon SID; make a copy of it.

      // Determine the length of the SID.
      dwLength = GetLengthSid(pTokenGroups->Groups[dwIndex].Sid);

      // Allocate a buffer for the logon SID.
      *ppSid = (PSID)LocalAlloc(LPTR, dwLength);
      if (*ppSid == NULL) {
        codiError = GetLastError();
        cout << "\nError al LocalAlloc" << codiError;
        return codiError;
      }

      // Obtain a copy of the logon SID.
      if (!CopySid(dwLength, *ppSid, pTokenGroups->Groups[dwIndex].Sid)) {
        codiError = GetLastError();
        cout << "\nError al CopySid" << codiError;
        return codiError;
      }

      break;
    }
  }

  fSucceeded = true;
  return fSucceeded;
}

PSECURITY_DESCRIPTOR spsd = NULL;
PACL spacl;
SECURITY_INFORMATION si = DACL_SECURITY_INFORMATION;

BOOL AddAceToWindowStation(HWINSTA hwinsta, PSID psid) {
  ACCESS_ALLOWED_ACE* pace = NULL;
  ACL_SIZE_INFORMATION aclSizeInfo;
  BOOL bDaclExist;
  BOOL bDaclPresent;
  BOOL bSuccess = FALSE;
  DWORD dwNewAclSize;
  DWORD dwSidSize = 0;
  DWORD dwSdSizeNeeded;

  // PSECURITY_DESCRIPTOR psd = NULL;
  // PACL spacl;

  PACL pNewAcl = NULL;
  PSECURITY_DESCRIPTOR psdNew = NULL;
  PVOID pTempAce;
  // SECURITY_INFORMATION si = DACL_SECURITY_INFORMATION;
  unsigned int i;

  __try {
    // Obtain the DACL for the window station.

    if (!GetUserObjectSecurity(hwinsta, &si, spsd, dwSidSize, &dwSdSizeNeeded))
      if (GetLastError() == ERROR_INSUFFICIENT_BUFFER) {
        spsd = (PSECURITY_DESCRIPTOR)HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, dwSdSizeNeeded);

        if (spsd == NULL) __leave;

        psdNew = (PSECURITY_DESCRIPTOR)HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, dwSdSizeNeeded);

        if (psdNew == NULL) __leave;

        dwSidSize = dwSdSizeNeeded;

        if (!GetUserObjectSecurity(hwinsta, &si, spsd, dwSidSize, &dwSdSizeNeeded)) __leave;
      } else
        __leave;

    // Create a new DACL.

    if (!InitializeSecurityDescriptor(psdNew, SECURITY_DESCRIPTOR_REVISION)) __leave;

    // Get the DACL from the security descriptor.

    if (!GetSecurityDescriptorDacl(spsd, &bDaclPresent, &spacl, &bDaclExist)) __leave;

    // Initialize the ACL.

    ZeroMemory(&aclSizeInfo, sizeof(ACL_SIZE_INFORMATION));
    aclSizeInfo.AclBytesInUse = sizeof(ACL);

    // Call only if the DACL is not NULL.

    if (spacl != NULL) {
      // get the file ACL size info
      if (!GetAclInformation(spacl, (LPVOID)&aclSizeInfo, sizeof(ACL_SIZE_INFORMATION), AclSizeInformation)) __leave;
    }

    // Compute the size of the new ACL.

    cout << "\nStation::aclSizeInfo.AclBytesInUse: " << aclSizeInfo.AclBytesInUse;
    dwNewAclSize = aclSizeInfo.AclBytesInUse + (2 * sizeof(ACCESS_ALLOWED_ACE)) + (2 * GetLengthSid(psid)) - (2 * sizeof(DWORD));

    // Allocate memory for the new ACL.

    pNewAcl = (PACL)HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, dwNewAclSize);

    if (pNewAcl == NULL) __leave;

    // Initialize the new DACL.

    if (!InitializeAcl(pNewAcl, dwNewAclSize, ACL_REVISION)) __leave;

    // If DACL is present, copy it to a new DACL.

    if (bDaclPresent) {
      // Copy the ACEs to the new ACL.
      if (aclSizeInfo.AceCount) {
        for (i = 0; i < aclSizeInfo.AceCount; i++) {
          // Get an ACE.
          if (!GetAce(spacl, i, &pTempAce)) __leave;

          // Add the ACE to the new ACL.
          if (!AddAce(pNewAcl, ACL_REVISION, MAXDWORD, pTempAce, ((PACE_HEADER)pTempAce)->AceSize)) __leave;
        }
      }
    }

    // Add the first ACE to the window station.

    pace = (ACCESS_ALLOWED_ACE*)HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, sizeof(ACCESS_ALLOWED_ACE) + GetLengthSid(psid) - sizeof(DWORD));

    if (pace == NULL) __leave;

    pace->Header.AceType = ACCESS_ALLOWED_ACE_TYPE;
    pace->Header.AceFlags = CONTAINER_INHERIT_ACE | INHERIT_ONLY_ACE | OBJECT_INHERIT_ACE;
    pace->Header.AceSize = LOWORD(sizeof(ACCESS_ALLOWED_ACE) + GetLengthSid(psid) - sizeof(DWORD));
    pace->Mask = GENERIC_ACCESS;

    if (!CopySid(GetLengthSid(psid), &pace->SidStart, psid)) __leave;

    if (!AddAce(pNewAcl, ACL_REVISION, MAXDWORD, (LPVOID)pace, pace->Header.AceSize)) __leave;

    // Add the second ACE to the window station.

    pace->Header.AceFlags = NO_PROPAGATE_INHERIT_ACE;
    pace->Mask = WINSTA_ALL;

    if (!AddAce(pNewAcl, ACL_REVISION, MAXDWORD, (LPVOID)pace, pace->Header.AceSize)) __leave;

    // Set a new DACL for the security descriptor.

    if (!SetSecurityDescriptorDacl(psdNew, TRUE, pNewAcl, FALSE)) __leave;

    // Set the new security descriptor for the window station.

    if (!SetUserObjectSecurity(hwinsta, &si, psdNew)) __leave;

    // Indicate success.

    bSuccess = TRUE;
  } __finally {
    // Free the allocated buffers.
    if (pace != NULL) HeapFree(GetProcessHeap(), 0, (LPVOID)pace);

    if (pNewAcl != NULL) HeapFree(GetProcessHeap(), 0, (LPVOID)pNewAcl);

    if (spsd != NULL) HeapFree(GetProcessHeap(), 0, (LPVOID)spsd);

    if (psdNew != NULL) HeapFree(GetProcessHeap(), 0, (LPVOID)psdNew);
  }

  return bSuccess;
}

bool hasUser(FILE* file, char* usuarioDominio) {
  if (file == NULL) return false;
  char name[64];
  int res = fscanf(file, "%s", name);
  if (res <= 0) return false;
  return strcmp(usuarioDominio, name) == 0 || hasUser(file, usuarioDominio);
}

BOOL AddAceToDesktop(HDESK hdesk, PSID psid) {
  PSECURITY_DESCRIPTOR dpsd = NULL;
  PACL dpacl;

  ACL_SIZE_INFORMATION aclSizeInfo;
  BOOL bDaclExist;
  BOOL bDaclPresent;
  BOOL bSuccess = FALSE;
  DWORD dwNewAclSize;
  DWORD dwSidSize = 0;
  DWORD dwSdSizeNeeded;

  // PSECURITY_DESCRIPTOR psd = NULL;
  // PACL pacl;

  PACL pNewAcl = NULL;
  PSECURITY_DESCRIPTOR psdNew = NULL;
  PVOID pTempAce;
  // SECURITY_INFORMATION si = DACL_SECURITY_INFORMATION;
  unsigned int i;

  __try {
    // Obtain the security descriptor for the desktop object.

    if (!GetUserObjectSecurity(hdesk, &si, dpsd, dwSidSize, &dwSdSizeNeeded)) {
      if (GetLastError() == ERROR_INSUFFICIENT_BUFFER) {
        dpsd = (PSECURITY_DESCRIPTOR)HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, dwSdSizeNeeded);

        if (dpsd == NULL) __leave;

        psdNew = (PSECURITY_DESCRIPTOR)HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, dwSdSizeNeeded);

        if (psdNew == NULL) __leave;

        dwSidSize = dwSdSizeNeeded;

        if (!GetUserObjectSecurity(hdesk, &si, dpsd, dwSidSize, &dwSdSizeNeeded)) __leave;
      } else
        __leave;
    }

    // Create a new security descriptor.

    if (!InitializeSecurityDescriptor(psdNew, SECURITY_DESCRIPTOR_REVISION)) __leave;

    // Obtain the DACL from the security descriptor.

    if (!GetSecurityDescriptorDacl(dpsd, &bDaclPresent, &dpacl, &bDaclExist)) __leave;

    // Initialize.

    ZeroMemory(&aclSizeInfo, sizeof(ACL_SIZE_INFORMATION));
    aclSizeInfo.AclBytesInUse = sizeof(ACL);

    // Call only if NULL DACL.

    if (dpacl != NULL) {
      // Determine the size of the ACL information.

      if (!GetAclInformation(dpacl, (LPVOID)&aclSizeInfo, sizeof(ACL_SIZE_INFORMATION), AclSizeInformation)) __leave;
    }

    // Compute the size of the new ACL.
    cout << "\nDesktop::aclSizeInfo.AclBytesInUse: " << aclSizeInfo.AclBytesInUse;
    dwNewAclSize = aclSizeInfo.AclBytesInUse + sizeof(ACCESS_ALLOWED_ACE) + GetLengthSid(psid) - sizeof(DWORD);

    // Allocate buffer for the new ACL.

    pNewAcl = (PACL)HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, dwNewAclSize);

    if (pNewAcl == NULL) __leave;

    // Initialize the new ACL.

    if (!InitializeAcl(pNewAcl, dwNewAclSize, ACL_REVISION)) __leave;

    // If DACL is present, copy it to a new DACL.

    if (bDaclPresent) {
      // Copy the ACEs to the new ACL.
      if (aclSizeInfo.AceCount) {
        for (i = 0; i < aclSizeInfo.AceCount; i++) {
          // Get an ACE.
          if (!GetAce(dpacl, i, &pTempAce)) __leave;

          // Add the ACE to the new ACL.
          if (!AddAce(pNewAcl, ACL_REVISION, MAXDWORD, pTempAce, ((PACE_HEADER)pTempAce)->AceSize)) __leave;
        }
      }
    }

    // Add ACE to the DACL.

    if (!AddAccessAllowedAce(pNewAcl, ACL_REVISION, DESKTOP_ALL, psid)) __leave;

    // Set new DACL to the new security descriptor.

    if (!SetSecurityDescriptorDacl(psdNew, TRUE, pNewAcl, FALSE)) __leave;

    // Set the new security descriptor for the desktop object.

    if (!SetUserObjectSecurity(hdesk, &si, psdNew)) __leave;

    // Indicate success.

    bSuccess = TRUE;
  } __finally {
    // Free buffers.

    if (pNewAcl != NULL) HeapFree(GetProcessHeap(), 0, (LPVOID)pNewAcl);

    if (dpsd != NULL) HeapFree(GetProcessHeap(), 0, (LPVOID)dpsd);

    if (psdNew != NULL) HeapFree(GetProcessHeap(), 0, (LPVOID)psdNew);
  }

  return bSuccess;
}

int _tmain(int argc, TCHAR* argv[], TCHAR* envp[]) {
  bool debugModeOn = false;

  ostream salida = cout;

  if (debugModeOn) {
    salida << "iniciando runAsUser" << endl;
  }

  LPVOID lpvEnv = NULL;
  BOOL result;
  HANDLE hToken;
  HANDLE hTokenDup;
  DWORD dwWaitResult;

  HANDLE hStdout = GetStdHandle(STD_OUTPUT_HANDLE);
  HANDLE hStdin = GetStdHandle(STD_INPUT_HANDLE);
  HANDLE hStderr = GetStdHandle(STD_ERROR_HANDLE);

  HWINSTA hWinstaSave = NULL;
  HWINSTA hwinsta = NULL;
  HDESK hdesk = NULL;
  PSID pSid = NULL;

  STARTUPINFO sInfo = {sizeof(sInfo)};
  PROCESS_INFORMATION pInfo = {0};

  BOOL bResult = FALSE;

  typedef BOOL(WINAPI * CREATEENVIRONMENTBLOCK)(LPVOID * environment, HANDLE token, BOOL inherit);
  typedef BOOL(WINAPI * DESTROYENVIRONMENTBLOCK)(LPVOID environment);

  char* envLevelError;
  int codiError = 0;
  unsigned long exitCode = 0;

  // cout << "xxxxxxxxxxxxxxxxxxxxxx	   V E R S I O N    28  // xxxxxxxxxxxxxxxxxxxxxxxxxxxx"--------------------------------------------

  if (argc < 5) {
    cout << "uso de RunAsUser:   \n\nExecAs.exe dir usuario@dominio password "
            "comando argumentos\n";

    return 1;
  }

  char* lpCurrentDirectory = argv[1];
  char* usuarioDominio = argv[2];
  char* password = argv[3];
  char* comando = argv[4];
  char* argumento = argv[5];

  // Separar Dominio
  int lenUsuarioDominio = strlen(usuarioDominio);
  char usuario[255];
  char dominio[255];

  int i = 0;
  int d = 0;
  int u = 0;
  bool arrobaencontrado = false;

  for (i = 0; i < lenUsuarioDominio; i++) {
    if (arrobaencontrado) {
      dominio[d] = usuarioDominio[i];
      d++;
    } else {
      if (usuarioDominio[i] == '@') {
        arrobaencontrado = true;

      } else {
        usuario[u] = usuarioDominio[i];
        u++;
      }
    }
  }

  if (!arrobaencontrado) {
    dominio[d] = '.';
    d++;
  }
  usuario[u] = '\0';
  dominio[d] = '\0';
  // Fin Separar Dominio

  char* usersFile = "users.txt";
  char* pbatchenv = getenv("PBATCHDIR");
  if(pbatchenv == NULL || strlen(pbatchenv) == 0) pbatchenv = ".";
  //printf("PBATCHDIR:%s\n", pbatchenv);

  // BOOLEANO que determina si acaso es necesario agregar los usuarios al archivo de memoria de usuarios y al ACL
  bool addusers;

  char* usersFilePath = (char*)malloc(sizeof(char) * (strlen(pbatchenv) + strlen("\\") + strlen(usersFile) + 1));
  sprintf(usersFilePath, "%s\\%s", pbatchenv, usersFile);
  //printf("Path de archivo de usuarios recordados: %s\n", usersFilePath);

  FILE* file = fopen(usersFilePath, "r");
  if (file == NULL) {
    addusers = true;
  } else {
    addusers = !hasUser(file, usuarioDominio);
    fclose(file);
  }

  if (addusers) {
    //cout << "\nRecordando usuario " << usuarioDominio;
    FILE* file = fopen(usersFilePath, "a");
    fprintf(file, "%s\n", usuarioDominio);
    fclose(file);
  }

  free(usersFilePath);

  sInfo.cb = sizeof(STARTUPINFO);
  sInfo.lpReserved = NULL;
  sInfo.lpReserved2 = NULL;
  sInfo.cbReserved2 = 0;
  sInfo.lpDesktop = "winsta0\\default";
  sInfo.lpTitle = NULL;
  sInfo.dwFlags = 0;
  sInfo.dwX = 0;
  sInfo.dwY = 0;
  sInfo.dwFillAttribute = 0;
  sInfo.wShowWindow = SW_SHOW;

  sInfo.hStdInput = hStdin;
  sInfo.hStdOutput = hStdout;
  sInfo.hStdError = hStderr;

  char directorio[1024];
  strcpy(directorio, lpCurrentDirectory);
  strcat(directorio, "\0");

  HMODULE hModule = LoadLibrary("Userenv.dll");
  if (!hModule) {
    codiError = GetLastError();
    salida << "\nError al cargar Userenv.dll " << codiError;
    return codiError;
  }

  CREATEENVIRONMENTBLOCK createEnvironmentBlock = (CREATEENVIRONMENTBLOCK)GetProcAddress(hModule, "CreateEnvironmentBlock");
  if (createEnvironmentBlock == NULL) {
    codiError = GetLastError();
    salida << "\nError al createEnvironmentBlock " << codiError;
    return codiError;
  }

  DESTROYENVIRONMENTBLOCK destroyEnvironmentBlock = (DESTROYENVIRONMENTBLOCK)GetProcAddress(hModule, "DestroyEnvironmentBlock");
  if (destroyEnvironmentBlock == NULL) {
    codiError = GetLastError();
    salida << "\nError al destroyEnvironmentBlock " << codiError;
    return codiError;
  }

  result = LogonUser(usuario, dominio, password, LOGON32_LOGON_INTERACTIVE, LOGON32_PROVIDER_DEFAULT, &hToken);

  if (!result) {
    codiError = GetLastError();
    salida << "\nError al ejecutar LogonUser " << codiError;
    return codiError;
  }

  // Save a handle to the caller's current window station.
  hWinstaSave = GetProcessWindowStation();
  if (hWinstaSave == NULL) {
    codiError = GetLastError();
    salida << "\nError al GetProcessWindowStation " << codiError;
    return codiError;
  }

  // Get a handle to the interactive window station.
  hwinsta = OpenWindowStation(_T("winsta0"),              // the interactive window station
                              FALSE,                      // handle is not inheritable
                              READ_CONTROL | WRITE_DAC);  // rights to read/write the DACL

  if (hwinsta == NULL) {
    codiError = GetLastError();
    salida << "\nError al OpenWindowStation " << codiError;
    return codiError;
  }

  // To get the correct default desktop, set the caller's window station
  // to the interactive window station.
  if (!SetProcessWindowStation(hwinsta)) {
    codiError = GetLastError();
    salida << "\nError al SetProcessWindowStation " << codiError;
    return codiError;
  }

  // Get a handle to the interactive desktop.
  hdesk = OpenDesktop(_T("default"),  // the interactive window station
                      0,              // no interaction with other desktop processes
                      FALSE,          // handle is not inheritable
                      READ_CONTROL |  // request the rights to read and write the DACL
                          WRITE_DAC | DESKTOP_WRITEOBJECTS | DESKTOP_READOBJECTS);

  // Restore the caller's window station.
  if (!SetProcessWindowStation(hWinstaSave)) {
    codiError = GetLastError();
    salida << "\nError al OpenDesktop " << codiError;
    return codiError;
  }

  if (hdesk == NULL) {
    codiError = GetLastError();
    salida << "\nError al hdesk null " << codiError;
    return codiError;
  }

  // Get the SID for the client's logon session.
  if (!GetLogonSID(hToken, &pSid)) {
    codiError = GetLastError();
    salida << "\nError al GetLogonSID " << codiError;
    return codiError;
  }

  /* Si el usuario con el cual debo ejecutar el comando NO EXISTE en el archivo de usuarios recientes 'users.txt' entonces
  es necesario agregar ACEs a las ACL de winstation y desktop */
  if (addusers) {
    // Allow logon SID full access to interactive window station.
    if (!AddAceToWindowStation(hwinsta, pSid)) {
      codiError = GetLastError();
      salida << "\nError al AddAceToWindowStation " << codiError;
      return codiError;
    }

    // Allow logon SID full access to interactive desktop.
    if (!AddAceToDesktop(hdesk, pSid)) {
      codiError = GetLastError();
      salida << "\nError al AddAceToDesktop " << codiError;
      return codiError;
    }
  }

  char cmd[1024];

  strcpy(cmd, comando);
  strcat(cmd, " ");
  strcat(cmd, argumento);

  strcat(cmd, "\0");

  /* IMPRESION DEL COMANDO QUE SE ENVIA A EJECUTAR */
  // salida << "Comando: " << cmd << "\n";

  DuplicateTokenEx(hToken, MAXIMUM_ALLOWED,
                   // GENERIC_ACCESS,
                   NULL, SecurityIdentification, TokenPrimary, &hTokenDup);

  // Retrieve the environment variables for the specified user. The
  // environment block inherits from the current process's environment.
  if (!createEnvironmentBlock(&lpvEnv, hTokenDup, TRUE)) {
    codiError = GetLastError();
    salida << "\nError al CreateEnvironmentBlock " << codiError;
    return codiError;
    // lpvEnv=NULL;
  }

  // Launch the process in the client's logon session.

  bResult = CreateProcessAsUser(
      hTokenDup,
      NULL,  // comando,
      cmd,   // argumento,
      0, 0, false,
      CREATE_UNICODE_ENVIRONMENT,  // CREATE_NO_WINDOW,//CREATE_DEFAULT_ERROR_MODE,//CREATE_NO_WINDOW,//DETACHED_PROCESS,//CREATE_DEFAULT_ERROR_MODE,//NORMAL_PRIORITY_CLASS
                                   // | CREATE_NEW_CONSOLE | CREATE_DEFAULT_ERROR_MODE,
      lpvEnv, directorio, &sInfo, &pInfo);

  RevertToSelf();

  if (!bResult) {
    // CreateProcess failed
    salida << "\nSALIDA POR ERROR ";
    codiError = GetLastError();
    if (codiError != ERROR_NO_MORE_FILES) {
      salida << "\nCreateProcess failed " << codiError;
      return codiError;
    }
  }

  dwWaitResult = WaitForSingleObject(pInfo.hProcess, INFINITE);

  GetExitCodeProcess(pInfo.hProcess, &exitCode);

  if (exitCode != 0) {
    if (debugModeOn) {
      salida << "\n exitCode!=0 : exitCode=" << exitCode;
    }

    goto Cleanup;
  }

  if (debugModeOn) {
    salida << "exitCode es 0 ; analizando ERRORLEVEL...\n";
  }

  /*ERRORLEVEL puede ser NULL...*/
  envLevelError = getenv("ERRORLEVEL");
  if (envLevelError == NULL) {
    if (debugModeOn) {
      salida << "envLevelError es NULL! abortando analisis de ERRORLEVEL... \n";
    }

    goto Cleanup;
  }

  if (debugModeOn) salida << "envLevelError=" << envLevelError << "\n";

  exitCode = strtoul(envLevelError, NULL, 0);

Cleanup:
  //salida << "\nCORRIENDO CLEANUP!";

  if (pInfo.hProcess) {
    //salida << "\nCloseHandle(pInfo.hProcess)";
    CloseHandle(pInfo.hProcess);
    pInfo.hProcess = NULL;
  }

  if (pInfo.hThread) {
    //salida << "\nCloseHandle(pInfo.hThread)";
    CloseHandle(pInfo.hThread);
    pInfo.hThread = NULL;
  }

  if (lpvEnv) {
    //salida << "\ndestroyEnvironmentBlock(lpvEnv)";
    destroyEnvironmentBlock(lpvEnv);
    lpvEnv = NULL;
  }

  if (hWinstaSave != NULL) {
    //salida << "\nSetProcessWindowStation(hWinstaSave)";
    SetProcessWindowStation(hWinstaSave);
  }

  // Free the buffer for the logon SID.

  if (pSid) {
    //salida << "\nFreeLogonSID(&pSid)";
    FreeSid(&pSid);
  }

  // Close the handles to the interactive window station and desktop.
  if (hwinsta) {
    //salida << "\nCloseWindowStation(hwinsta)";
    CloseWindowStation(hwinsta);
  }
  if (hdesk) {
    //salida << "\nCloseDesktop(hdesk)";
    CloseDesktop(hdesk);
  }

  // Close the handle to the client's access token.
  if (hToken != INVALID_HANDLE_VALUE) CloseHandle(hToken);

  return exitCode;
}
