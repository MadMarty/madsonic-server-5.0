@echo off

REM  The directory where Madsonic will create files. Make sure it is writable.
set MADSONIC_HOME=c:\madsonic

REM  The host name or IP address on which to bind Madsonic. Only relevant if you have
REM  multiple network interfaces and want to make Madsonic available on only one of them.
REM  The default value 0.0.0.0 will bind Madsonic to all available network interfaces.
set MADSONIC_HOST=0.0.0.0

REM  The port on which Madsonic will listen for incoming HTTP traffic.
set MADSONIC_PORT=4040

REM  The port on which Madsonic will listen for incoming HTTPS traffic (0 to disable).
set MADSONIC_HTTPS_PORT=0

REM  The context path (i.e., the last part of the Madsonic URL).  Typically "/" or "/madsonic".
set MADSONIC_CONTEXT_PATH=/

REM  The directory for music
set MADSONIC_DEFAULT_MUSIC_FOLDER=c:\media\Artists

REM  The directory for upload
set MADSONIC_DEFAULT_UPLOAD_FOLDER=c:\media\Incoming

REM  The directory for Podcast
set MADSONIC_DEFAULT_PODCAST_FOLDER=c:\media\Podcast

REM  The directory for Playlist
set MADSONIC_DEFAULT_PLAYLIST_IMPORT_FOLDER=c:\media\playlist-Import

REM  The directory for Playlist-export
set MADSONIC_DEFAULT_PLAYLIST_EXPORT_FOLDER=c:\media\playlist-Export

REM  The memory limit (max Java heap size) in megabytes.
set MAX_MEMORY=350

REM  The memory initial size (Init Java heap size) in megabytes.
set INIT_MEMORY=200

java -Xms%INIT_MEMORY%m -Xmx%MAX_MEMORY%m  -Dsubsonic.home=%MADSONIC_HOME% -Dsubsonic.host=%MADSONIC_HOST% -Dsubsonic.port=%MADSONIC_PORT% -Dsubsonic.httpsPort=%MADSONIC_HTTPS_PORT% -Dsubsonic.contextPath=%MADSONIC_CONTEXT_PATH%  -Dsubsonic.defaultMusicFolder=%MADSONIC_DEFAULT_MUSIC_FOLDER% -Dsubsonic.defaultUploadFolder=%MADSONIC_DEFAULT_UPLOAD_FOLDER% -Dsubsonic.defaultPodcastFolder=%MADSONIC_DEFAULT_PODCAST_FOLDER% -Dsubsonic.defaultPlaylistFolder=%MADSONIC_DEFAULT_PLAYLIST_IMPORT_FOLDER% -Dsubsonic.defaultPlaylistExportFolder=%MADSONIC_DEFAULT_PLAYLIST_EXPORT_FOLDER% -jar madsonic-booter.jar

