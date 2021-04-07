javac -d . -classpath spigot-api.jar src/CustomTime.java src/CTWorldData.java src/CommandCustomTime.java src/CustomTimeTabCompleter.java
jar cvf CustomTime.jar jcv8000/customtime/*.class plugin.yml
rmdir /s /q jcv8000
pause