package glang.runtime.cl;

import glang.compiler.bytecode.GlangCompiler;
import glang.compiler.error.CompileFailedException;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GlangClassLoader extends URLClassLoader {
    private static final String SUFFIX = ".glang";

    private final Map<String, byte[]> waitingClasses = new HashMap<>();

    public GlangClassLoader(URL[] urls, ClassLoader parent) {
        super("glang", urls, parent);
    }

    public GlangClassLoader(URL[] urls) {
        this(urls, getSystemClassLoader());
    }

    public GlangClassLoader() {
        this(new URL[0], getSystemClassLoader());
    }

    public Class<?> loadClassFromResource(String path) throws ClassNotFoundException {
        if (!path.endsWith(".glang")) {
            throw new IllegalArgumentException("Path does not end with .glang");
        }
        return loadClass(GlangCompiler.namespacePathToClassName(
            path.substring(0, path.length() - 6).replace('/', '.')
        ));
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            final byte[] bytes = findClassBytes(name);
            return defineClass(name, bytes, 0, bytes.length);
        } catch (ClassNotFoundException e) {
            if (e.getCause() != null) {
                throw e;
            }
            return super.findClass(name);
        }
    }

    @Override
    public void close() throws IOException {
        waitingClasses.clear(); // Clear waiting to clean up some memory
        super.close();
    }

    private byte[] findClassBytes(String name) throws ClassNotFoundException {
        byte[] result = waitingClasses.remove(name);
        if (result != null) {
            return result;
        }
        final Reference ref = findSourceString(name);
        final Map<String, ClassWriter> writers = new HashMap<>();
        try {
            final GlangCompiler compiler = new GlangCompiler(
                ref.namespacePath, ref.source,
                c -> writers.computeIfAbsent(
                    c, c2 -> new ClassWriter(ClassWriter.COMPUTE_FRAMES)
                )
            );
            compiler.compile(ref.sourceFile);
        } catch (CompileFailedException e) {
            throw new ClassNotFoundException(name, e);
        }
        writers.forEach((c, writer) -> waitingClasses.put(c, writer.toByteArray()));
        result = waitingClasses.remove(name);
        if (result != null) {
            return result;
        }
        throw new ClassNotFoundException(name);
    }

    private Reference findSourceString(String name) throws ClassNotFoundException {
        final int dotIndex = name.lastIndexOf('.');
        final String simpleName = name.substring(dotIndex + 1);
        if (!simpleName.contains("Ns")) {
            throw new ClassNotFoundException(name);
        }
        final String prefix = name.substring(0, dotIndex + 1).replace('.', '/');
        String lowerName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
        while (lowerName.indexOf('$') >= 0) {
            if (lowerName.endsWith("Ns")) {
                final Reference result = getReference(prefix + lowerName.substring(0, lowerName.length() - 2) + SUFFIX);
                if (result != null) {
                    return result;
                }
            }
            lowerName = lowerName.substring(0, lowerName.lastIndexOf('$'));
        }
        if (lowerName.endsWith("Ns")) {
            final Reference result = getReference(prefix + lowerName.substring(0, lowerName.length() - 2) + SUFFIX);
            if (result != null) {
                return result;
            }
        }
        throw new ClassNotFoundException(name);
    }

    private Reference getReference(String name) {
        try (InputStream is = getResourceAsStream(name)) {
            if (is != null) {
                return new Reference(
                    name.substring(name.lastIndexOf('/') + 1),
                    new String(is.readAllBytes(), StandardCharsets.UTF_8),
                    name.substring(0, name.length() - SUFFIX.length()).replace('/', '.')
                );
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private record Reference(String sourceFile, String source, String namespacePath) {
    }
}
