@echo off
java -ea -Djava.library.path="C:/Program Files/Tribal Trouble/lib/native" -Dorg.lwjgl.util.Debug=false -Xmx400m -cp ".;./lib/*" com.oddlabs.tt.Main --silent