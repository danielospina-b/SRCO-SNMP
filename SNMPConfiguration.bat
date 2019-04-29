@echo off
cls
echo.
echo.
reg add "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\services\SNMP\Parameters\PermittedManagers" /v 1 /t REG_SZ /d 192.168.0.16 /f
cls
echo.
echo.
reg add "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\services\SNMP\Parameters\ValidCommunities" /v public /t REG_DWORD /d 8 /f
cls
echo.
echo.
net stop snmp
cls
echo.
echo.
net start snmp