package glang.launcher;

import glang.compiler.error.CompileFailedException;
import glang.runtime.cl.GlangClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ScriptLauncher {
    public static void main(String[] args) throws Throwable {
        String jarFile = ScriptLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarFile = jarFile.substring(jarFile.lastIndexOf('/') + 1);

        if (args.length == 0) {
            System.err.println("Usage: java -jar " + jarFile + " <script> [args...]");
            System.exit(1);
            return;
        }

        final Deque<String> arguments = new ArrayDeque<>(List.of(args));

        final String scriptPathStr = arguments.removeFirst();
        if (!scriptPathStr.endsWith(".glang")) {
            System.err.println("Script path does not end with .glang");
            System.exit(1);
            return;
        }

        final Path scriptPath;
        try {
            scriptPath = Path.of(scriptPathStr).toAbsolutePath().normalize();
        } catch (Exception e) {
            System.err.println("Invalid path: " + scriptPathStr);
            System.exit(1);
            return;
        }
        if (!Files.isRegularFile(scriptPath)) {
            System.err.println("File does not exist: " + scriptPath);
            System.exit(1);
            return;
        }

        final Path cwd = Path.of(".").toAbsolutePath().normalize();
        if (!scriptPath.startsWith(cwd)) {
            System.err.println("Script file not in cwd or subdirectory");
            System.exit(1);
            return;
        }

        final String relativePath = cwd
            .relativize(scriptPath)
            .toString()
            .replace(FileSystems.getDefault().getSeparator(), "/");

        try (var cl = new GlangClassLoader(new URL[] {cwd.toUri().toURL()})) {
            cl.loadClassFromResource(relativePath)
                .getDeclaredMethod("main", String[].class)
                .invoke(null, (Object)arguments.toArray(String[]::new));
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (ClassNotFoundException e) {
            if (e.getCause() instanceof CompileFailedException compileFailed) {
                System.err.println(compileFailed.getMessage());
                System.exit(1);
                return;
            }
            throw e;
        }
    }
}
