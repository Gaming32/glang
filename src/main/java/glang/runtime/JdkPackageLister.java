package glang.runtime;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class JdkPackageLister {
    public static List<String> listJavaLang() {
        return listJavaBase("java.lang");
    }

    public static List<String> listJavaBase(String packageName) {
        return listClasses("java.base", packageName);
    }

    public static List<String> listClasses(String moduleName, String packageName) {
        final URI uri;
        try {
            uri = new URI("jrt:/" + moduleName + '/' + packageName.replace('.', '/'));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Illegal module or path");
        }
        try (Stream<Path> stream = Files.list(Path.of(uri))) {
            return stream
                .filter(p -> {
                    final String str = p.toString();
                    return str.endsWith(".class") && str.lastIndexOf('$') == -1;
                })
                .filter(p -> {
                    try (InputStream is = Files.newInputStream(p)) {
                        return (new ClassReader(is).getAccess() & Opcodes.ACC_PUBLIC) != 0;
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to read " + p);
                    }
                })
                .map(Path::toString)
                .map(p -> p.substring(p.lastIndexOf('/') + 1, p.lastIndexOf('.')))
                .map(p -> packageName + '.' + p)
                .toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("No such module/package: " + moduleName + '/' + packageName);
        }
    }
}
