package com.diamonddagger590.mccore.file;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class IOUtil {

    public static void saveResource(Plugin plugin, String resourcePath, boolean replace) {

        if (resourcePath != null && !resourcePath.equals("")) {

            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = plugin.getResource(resourcePath);

            if (in == null) {
                throw new IllegalArgumentException("The embedded resource \'" + resourcePath + "\' cannot be found");
            }
            else {

                File outFile = new File(plugin.getDataFolder(), resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);

                File outDir = new File(plugin.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }
                try {
                    if (outFile.exists() && !replace) {
                        plugin.getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
                    }
                    else {
                        FileOutputStream ex = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            ex.write(buf, 0, len);
                        }
                        ex.close();
                        in.close();
                    }
                }
                catch (IOException var10) {
                    plugin.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
                }

            }
        }
        else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }
}
