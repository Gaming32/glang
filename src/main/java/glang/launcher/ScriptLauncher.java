package glang.launcher;

import glang.compiler.bytecode.GlangCompiler;
import glang.compiler.error.CompileFailedException;
import glang.runtime.cl.GlangClassLoader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ScriptLauncher {
    public static void main(String[] args) throws Throwable {
        String jarFile = ScriptLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarFile = jarFile.substring(jarFile.lastIndexOf('/') + 1);

        if (args.length == 0) {
            System.err.println("Usage: java -jar " + jarFile + " [--compile] <script> [args...]");
            System.exit(1);
            return;
        }

        final Deque<String> arguments = new ArrayDeque<>(List.of(args));

        final boolean compileOnly = arguments.element().equals("--compile");
        if (compileOnly) {
            arguments.remove();
        }

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

        if (compileOnly) {
            final Map<String, ClassWriter> result = new HashMap<>();
            try {
                final GlangCompiler compiler = new GlangCompiler(
                    relativePath.substring(0, relativePath.length() - 6).replace('/', '.'),
                    Files.readString(scriptPath),
                    name -> result.computeIfAbsent(name, n -> new ClassWriter(ClassWriter.COMPUTE_FRAMES))
                );
                compiler.compile(relativePath.substring(relativePath.lastIndexOf('/') + 1));
                result.forEach((clazz, writer) -> {
                    try {
                        Files.write(Path.of(clazz.replace('.', '/') + ".class"), writer.toByteArray());
                    } catch (IOException e) {
                        System.err.println("Failed to write " + clazz);
                        //noinspection ThrowablePrintedToSystemOut
                        System.err.println(e);
                    }
                });
            } catch (CompileFailedException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
            return;
        }

        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        final var cl = new GlangClassLoader(new URL[] {cwd.toUri().toURL()});
        try (cl) {
            Thread.currentThread().setContextClassLoader(cl);
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
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }
}
