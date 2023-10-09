
#include <string>
#include <Windows.h>
#include <iostream>


extern "C" __declspec(dllexport) const char* GetEnvVariable(const char* name) {
    static std::string pathValue;
    char buffer[4096];
    DWORD size = GetEnvironmentVariableA(name, buffer, sizeof(buffer));

    if (size > 0 && size < sizeof(buffer)) {
        pathValue = buffer;
        return pathValue.c_str();
    }
    else {
        return nullptr;
    }
}

extern "C" __declspec(dllexport) void SetEnvVariable(const char* name, const char* value) {
    SetEnvironmentVariableA(name, value);
}


extern "C" __declspec(dllexport) bool SetPermanentEnvironmentVariable(LPCTSTR value, LPCTSTR data)
{
    HKEY hKey;
    LPCTSTR keyPath = TEXT("SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment");

    LSTATUS lOpenStatus = RegCreateKeyEx(HKEY_LOCAL_MACHINE, keyPath, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_SET_VALUE, NULL, &hKey, NULL);

    if (lOpenStatus == ERROR_SUCCESS)
    {
        int dataSize = lstrlen(data) * sizeof(wchar_t);

        LSTATUS lSetStatus = RegSetValueEx(hKey, value, 0, REG_SZ, reinterpret_cast<BYTE*>(const_cast<wchar_t*>(data)), dataSize);
        RegCloseKey(hKey);

        if (lSetStatus == ERROR_SUCCESS)
        {
            SendMessageTimeout(HWND_BROADCAST, WM_SETTINGCHANGE, 0, (LPARAM)keyPath, SMTO_NORMAL, 100, NULL);
            return true;
        }

    }


    return false;
}

extern "C" __declspec(dllexport) void RestartExplorer() {
    HWND hwnd = GetShellWindow();
    PostMessage(hwnd, WM_COMMAND, 41504, 0);
    SendMessageTimeoutW(HWND_BROADCAST, WM_SETTINGCHANGE, 0, (LPARAM)L"Environment", SMTO_ABORTIFHUNG, 5000, nullptr);
}