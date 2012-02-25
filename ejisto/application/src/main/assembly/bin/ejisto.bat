@echo off
rem
rem Ejisto, a powerful developer assistant
rem
rem Copyright (C) 2010-2012  Celestino Bellone
rem
rem Ejisto is free software: you can redistribute it and/or modify
rem it under the terms of the GNU General Public License as published by
rem the Free Software Foundation, either version 3 of the License, or
rem (at your option) any later version.
rem
rem Ejisto is distributed in the hope that it will be useful,
rem but WITHOUT ANY WARRANTY; without even the implied warranty of
rem MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
rem GNU General Public License for more details.
rem
rem You should have received a copy of the GNU General Public License
rem along with this program.  If not, see <http://www.gnu.org/licenses/>.
rem

SET CLASSPATH=lib\
rem Many thanks to Ruben Farias and stack overflow (http://stackoverflow.com/a/2027110)
For %%f in (lib\*.jar) DO call :concat %%f
goto :run-application

:concat
set CLASSPATH=%CLASSPATH%%1;
goto :eof

:run-application
java -cp "%CLASSPATH%" com.ejisto.core.launcher.Main