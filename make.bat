javac -d . -classpath lib/spigot-api.jar src/jcv8000/customtime/CustomTime.java src/jcv8000/customtime/CTWorldData.java src/jcv8000/customtime/CommandCustomTime.java src/jcv8000/customtime/CustomTimeTabCompleter.java
jar cf CustomTime.jar jcv8000/customtime/*.class plugin.yml
rmdir /s /q jcv8000