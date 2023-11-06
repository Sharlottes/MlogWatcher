@rem put this project path into PATH_FROM
setlocal
set PATH_FROM=C:\Users\user\Documents\GitHub\mlogJsWatcher
@rem put your mindustry local path into PATH_TO
setlocal
set PATH_TO=C:\Users\user\AppData\Roaming\Mindustry

if exist %PATH_TO%\mods\MLogJsWatcherDesktop.jar del %PATH_TO%\mods\MLogJsWatcherDesktop.jar
xcopy %PATH_FROM%\build\libs\MLogJsWatcherDesktop.jar %PATH_TO%\mods\ /k /y