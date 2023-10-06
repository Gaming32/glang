plugins {
    java
    `java-library`
}

group = "io.github.gaming32"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.6")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    compileOnlyApi("org.jetbrains:annotations:24.0.1")
}

tasks.test {
    useJUnitPlatform()
}

val generateObjectInvokers by tasks.registering {
    val outputFile = file("src/main/java/glang/runtime/ObjectInvokers.java")
    val invokerCount = 17

    doLast {
        outputFile.printWriter().use { w ->
            w.println("package glang.runtime;")
            w.println()
            w.println("import glang.exception.UninvokableObjectException;")
            w.println("import glang.runtime.lookup.MethodLookup;")
            w.println()
            w.println("import java.util.List;")
            w.println()
            w.println("public final class ObjectInvokers {")
            w.println("    private ObjectInvokers() {")
            w.println("    }")

            repeat(invokerCount) { argCount ->
                w.println()

                w.print("    public static Object invokeObject(Object target")
                repeat(argCount) { arg ->
                    w.print(", Object arg$arg")
                }
                w.println(") throws Throwable {")

                w.println("        if (target instanceof MethodLookup lookup) {")

                if (argCount > 0) {
                    w.print("            return lookup.getInvoker(List.of(")
                    repeat(argCount) { arg ->
                        if (arg > 0) {
                            w.print(", ")
                        }
                        w.print("arg$arg.getClass()")
                    }
                    w.println("))")
                    w.print("                .invoke(")
                    repeat(argCount) { arg ->
                        if (arg > 0) {
                            w.print(", ")
                        }
                        w.print("arg$arg")
                    }
                    w.println(");")
                } else {
                    w.println("            return lookup.invoke();")
                }

                w.println("        }")
                w.println("        throw new UninvokableObjectException(\"Cannot invoke object of type \" + target.getClass().getName());")
                w.println("    }")
            }

            w.println()
            w.println("    public static Object invokeObject(Object target, Object... args) throws Throwable {")
            w.println("        if (target instanceof MethodLookup lookup) {")
            w.println("            return lookup.invoke(List.of(args));")
            w.println("        }")
            w.println("        throw new UninvokableObjectException(\"Cannot invoke object of type \" + target.getClass().getName());")
            w.println("    }")
            w.println("}")
        }
    }
}
