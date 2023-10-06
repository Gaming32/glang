package glang.compiler.bytecode;

import glang.compiler.SourceLocation;
import glang.compiler.error.CompileFailedException;
import glang.compiler.error.ErrorCollector;
import glang.compiler.tree.ASTNode;
import glang.compiler.tree.GlangTreeifier;
import glang.compiler.tree.StatementList;
import glang.compiler.tree.expression.*;
import glang.compiler.tree.statement.BlockStatement;
import glang.compiler.tree.statement.ExpressionStatement;
import glang.compiler.tree.statement.StatementNode;
import org.objectweb.asm.*;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class GlangCompiler {
    private static final String j_l_Object = "java/lang/Object";
    private static final String j_l_Object_DESC = "L" + j_l_Object + ";";
    private static final String j_l_System = "java/lang/System";
    private static final String j_l_StringBuilder = "java/lang/StringBuilder";
    private static final String j_i_PrintStream = "java/io/PrintStream";
    private static final String j_i_PrintStream_DESC = "L" + j_i_PrintStream + ";";
    private static final String g_r_ConstantBootstrap = "glang/runtime/ConstantBootstrap";
    private static final String g_r_GlangRuntime = "glang/runtime/GlangRuntime";
    private static final String g_r_ObjectInvokers = "glang/runtime/ObjectInvokers";

    private static final String GLOBALS = "GLOBALS";
    private static final String GLOBALS_DESC = "Ljava/util/Map;";
    private static final String GLOBALS_SIG = "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;";

    private static final String CONDY_DESC_PREFIX = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;";

    private final String namespacePath;
    private final String className;
    private final String classNameInternal;
    private final StatementList code;
    private final Function<String, ClassVisitor> visitors;
    private final ErrorCollector errorCollector;

    private boolean insertDebugPrints = false;

    private final StateStack<ClassState> classStates = new StateStack<>(ClassState::new);
    private final StateStack<MethodState> methodStates = new StateStack<>(MethodState::new);
    private final StateStack<ScopeState> scopeStates = new StateStack<>(ScopeState::new);

    public GlangCompiler(String namespacePath, StatementList code, Function<String, ClassVisitor> visitors, ErrorCollector errorCollector) {
        this.namespacePath = namespacePath;
        this.className = namespacePathToClassName(namespacePath);
        this.classNameInternal = className.replace('.', '/');
        this.code = code;
        this.visitors = visitors;
        this.errorCollector = errorCollector;
    }

    public GlangCompiler(String namespacePath, String source, Function<String, ClassVisitor> visitors) throws CompileFailedException {
        this(namespacePath, GlangTreeifier.treeify(source), visitors, new ErrorCollector(source));
    }

    public static void compile(
        String namespacePath, StatementList code, Function<String, ClassVisitor> visitors, ErrorCollector errorCollector
    ) throws CompileFailedException {
        final GlangCompiler compiler = new GlangCompiler(namespacePath, code, visitors, errorCollector);
        compiler.compile();
        errorCollector.throwIfFailed();
    }

    public static void compile(String namespacePath, String source, Function<String, ClassVisitor> visitors) throws CompileFailedException {
        final GlangCompiler compiler = new GlangCompiler(namespacePath, source, visitors);
        compiler.compile();
        compiler.errorCollector.throwIfFailed();
    }

    public static String namespacePathToClassName(String namespacePath) {
        final int lastDotIndex = namespacePath.lastIndexOf('.');
        String lastPart = namespacePath.substring(lastDotIndex + 1);
        if (!lastPart.isEmpty()) {
            lastPart = Character.toUpperCase(lastPart.charAt(0)) + lastPart.substring(1);
        }
        return namespacePath.substring(0, lastDotIndex + 1) + lastPart + "Ns";
    }

    public String getNamespacePath() {
        return namespacePath;
    }

    public String getClassName() {
        return className;
    }

    public ErrorCollector getErrorCollector() {
        return errorCollector;
    }

    public boolean insertDebugPrints() {
        return insertDebugPrints;
    }

    public void insertDebugPrints(boolean insertDebugPrints) {
        this.insertDebugPrints = insertDebugPrints;
    }

    public void compile() {
        compile((String)null);
    }

    public void compile(String sourceFile) {
        final ClassState clazz = classStates.push(classNameInternal);
        clazz.visitor = visitors.apply(className);
        clazz.visitor.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, clazz.name, null, j_l_Object, null);
        if (sourceFile != null) {
            clazz.visitor.visitSource(sourceFile, null);
        }
        clazz.visitor.visitField(
            Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
            GLOBALS, GLOBALS_DESC, GLOBALS_SIG, null
        );
        {
            final MethodVisitor mv = clazz.visitor.visitMethod(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                "<clinit>", "()V", null, null
            );
            mv.visitCode();
            mv.visitTypeInsn(Opcodes.NEW, "java/util/LinkedHashMap");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/LinkedHashMap", "<init>", "()V", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, classNameInternal, GLOBALS, GLOBALS_DESC);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        final MethodState method = methodStates.push("main");
        method.visitor = clazz.visitor.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "main", "([Ljava/lang/String;)V",
            null, null
        );
        method.visitor.visitCode();
        final ScopeState scope = scopeStates.push("");
        final VariableInfo argsVariable = new VariableInfo(0, "[Ljava/lang/String;");
        scope.variables.put("args", argsVariable);
        compileRoot();
        scopeStates.pop();
        method.visitor.visitMaxs(0, 0);
        method.visitor.visitEnd();
        methodStates.pop();

        clazz.visitor.visitEnd();
        classStates.pop();
    }

    private void compileRoot() {
        compile(code);
        methodStates.get().visitor.visitInsn(Opcodes.RETURN);
    }

    private void compile(ASTNode node) {
        if (node instanceof StatementNode statement) {
            compileStatement(statement);
        } else if (node instanceof ExpressionNode expression) {
            compileExpression(expression);
        } else if (node instanceof StatementList list) {
            list.getStatements().forEach(this::compileStatement);
        } else {
            throw new UnsupportedOperationException("Unsupported ASTNode " + node.getClass().getSimpleName());
        }
    }

    private void compileStatement(StatementNode statement) {
        final MethodState method = methodStates.get();
        final MethodVisitor visitor = method.visitor;

        if (statement instanceof ExpressionStatement expressionStatement) {
            if (insertDebugPrints) {
                method.checkLine(statement);
                final SourceLocation location = expressionStatement.getStartLocation();
                visitor.visitFieldInsn(
                    Opcodes.GETSTATIC, j_l_System, "err", j_i_PrintStream_DESC
                );
                visitor.visitTypeInsn(Opcodes.NEW, j_l_StringBuilder);
                visitor.visitInsn(Opcodes.DUP);
                visitor.visitLdcInsn("[DEBUG " + location.line() + ":" + location.column() + "] ");
                visitor.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    j_l_StringBuilder,
                    "<init>",
                    "(Ljava/lang/String;)V",
                    false
                );
            }
            compileExpression(expressionStatement.getExpression());
            if (insertDebugPrints) {
                method.checkLine(statement);
                visitor.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    j_l_StringBuilder,
                    "append",
                    "(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
                    false
                );
                visitor.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    j_i_PrintStream,
                    "println",
                    "(Ljava/lang/Object;)V",
                    false
                );
            } else {
                method.checkLine(statement);
                visitor.visitInsn(Opcodes.POP);
            }
        } else if (statement instanceof BlockStatement blockStatement) {
            scopeStates.push("block");
            compile(blockStatement.getStatements());
            scopeStates.pop();
        } else {
            throw new UnsupportedOperationException("Unsupported StatementNode " + statement.getClass().getSimpleName());
        }
    }

    private void compileExpression(ExpressionNode expression) {
        final MethodState method = methodStates.get();
        final MethodVisitor visitor = method.visitor;

        if (expression instanceof LiteralExpression<?> literal) {
            compileLiteral(literal);
        } else if (expression instanceof CallExpression call) {
            compileExpression(call.getTarget());
            if (call.getArgs().size() < 17) {
                call.getArgs().forEach(this::compileExpression);
                method.checkLine(expression);
                visitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC, g_r_ObjectInvokers, "invokeObject",
                    "(" + j_l_Object_DESC.repeat(call.getArgs().size() + 1) + ")" + j_l_Object_DESC,
                    false
                );
            } else {
                compileArray(visitor, call.getArgs(), this::compileExpression);
                method.checkLine(expression);
                visitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC, g_r_GlangRuntime, "invokeObject",
                    "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                    false
                );
            }
        } else {
            throw new UnsupportedOperationException("Unsupported ExpressionNode " + expression.getClass().getSimpleName());
        }
    }

    private void compileLiteral(LiteralExpression<?> literal) {
        final MethodState method = methodStates.get();
        final MethodVisitor visitor = method.visitor;

        if (literal instanceof BooleanExpression booleanExpression) {
            method.checkLine(literal);
            visitor.visitFieldInsn(
                Opcodes.GETSTATIC, "java/lang/Boolean",
                booleanExpression.getValue() ? "TRUE" : "FALSE",
                "Ljava/lang/Boolean;"
            );
        } else if (literal instanceof StringExpression stringExpression) {
            method.checkLine(literal);
            visitor.visitLdcInsn(stringExpression.getValue());
        } else if (literal instanceof NumberExpression numberExpression) {
            if (numberExpression.getValue() instanceof Integer integer) {
                method.checkLine(literal);
                visitor.visitLdcInsn(new ConstantDynamic(
                    "$glang$int$", "Ljava/lang/Integer;",
                    new Handle(
                        Opcodes.H_INVOKESTATIC,
                        g_r_ConstantBootstrap,
                        "intWrapper",
                        CONDY_DESC_PREFIX + "I)Ljava/lang/Integer;",
                        false
                    ),
                    integer
                ));
            } else if (numberExpression.getValue() instanceof Double doubleValue) {
                method.checkLine(literal);
                visitor.visitLdcInsn(new ConstantDynamic(
                    "$glang$double$", "Ljava/lang/Double;",
                    new Handle(
                        Opcodes.H_INVOKESTATIC,
                        g_r_ConstantBootstrap,
                        "doubleWrapper",
                        CONDY_DESC_PREFIX + "D)Ljava/lang/Double;",
                        false
                    ),
                    doubleValue
                ));
            } else if (numberExpression.getValue() instanceof Long longValue) {
                method.checkLine(literal);
                visitor.visitLdcInsn(new ConstantDynamic(
                    "$glang$long$", "Ljava/lang/Long;",
                    new Handle(
                        Opcodes.H_INVOKESTATIC,
                        g_r_ConstantBootstrap,
                        "longWrapper",
                        CONDY_DESC_PREFIX + "J)Ljava/lang/Long;",
                        false
                    ),
                    longValue
                ));
            } else if (numberExpression.getValue() instanceof BigInteger bigInteger) {
                method.checkLine(literal);
                visitor.visitLdcInsn(new ConstantDynamic(
                    "$glang$bigInteger$", "Ljava/math/BigInteger;",
                    new Handle(
                        Opcodes.H_INVOKESTATIC,
                        g_r_ConstantBootstrap,
                        "bigInteger",
                        CONDY_DESC_PREFIX + "Ljava/lang/String;)Ljava/math/BigInteger;",
                        false
                    ),
                    bigInteger.toString()
                ));
            } else {
                throw new UnsupportedOperationException("Unsupported Number " + literal.getClass().getSimpleName());
            }
        } else if (literal instanceof IdentifierExpression identifier) {
            method.checkLine(literal);
            // TODO: Local variables
            visitor.visitFieldInsn(Opcodes.GETSTATIC, classNameInternal, GLOBALS, GLOBALS_DESC);
            visitor.visitLdcInsn(identifier.getValue());
            visitor.visitMethodInsn(
                Opcodes.INVOKESTATIC, g_r_GlangRuntime, "getGlobal",
                "(Ljava/util/Map;Ljava/lang/String;)Ljava/lang/Object;",
                false
            );
        } else if (literal instanceof NullExpression) {
            method.checkLine(literal);
            visitor.visitInsn(Opcodes.ACONST_NULL);
        } else {
            throw new UnsupportedOperationException("Unsupported LiteralExpression " + literal.getClass().getSimpleName());
        }
    }

    private void error(ASTNode node, String reason) {
        SourceLocation location = node.singleLineLocation();
        if (location == null) {
            location = node.getStartLocation();
        }
        errorCollector.addError(reason, location);
    }

    private static <T> void compileArray(MethodVisitor visitor, List<T> array, Consumer<T> compiler) {
        visitInt(visitor, array.size());
        visitor.visitTypeInsn(Opcodes.ANEWARRAY, j_l_Object);
        for (int i = 0; i < array.size(); i++) {
            visitor.visitInsn(Opcodes.DUP);
            visitInt(visitor, i);
            compiler.accept(array.get(i));
        }
    }

    private static void visitInt(MethodVisitor visitor, int value) {
        if (value >= -1 && value <= 5) {
            visitor.visitInsn(Opcodes.ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            visitor.visitIntInsn(Opcodes.BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            visitor.visitIntInsn(Opcodes.SIPUSH, value);
        } else {
            visitor.visitLdcInsn(value);
        }
    }

    private static class StateStack<T> {
        final Function<String, T> factory;
        final Deque<T> stack = new ArrayDeque<>();

        StateStack(Function<String, T> factory) {
            this.factory = factory;
        }

        int size() {
            return stack.size();
        }

        T push(String name) {
            final T newState = factory.apply(name);
            stack.add(newState);
            return newState;
        }

        T get() {
            return stack.getLast();
        }

        T pop() {
            return stack.removeLast();
        }
    }

    private static class ClassState {
        final String name;
        ClassVisitor visitor;

        ClassState(String name) {
            this.name = name;
        }
    }

    private class MethodState {
        final String name;
        final ClassState owner = classStates.get();
        MethodVisitor visitor;
        int currentLine = -1;

        MethodState(String name) {
            this.name = name;
        }

        void checkLine(ASTNode node) {
            final int line = node.getStartLocation().line();
            if (line != currentLine) {
                final Label lineLabel = new Label();
                visitor.visitLabel(lineLabel);
                visitor.visitLineNumber(line, lineLabel);
                currentLine = line;
            }
        }
    }

    private class ScopeState {
        final String name;
        final MethodState owner = methodStates.get();
        final Map<String, VariableInfo> variables = new LinkedHashMap<>();

        ScopeState(String name) {
            this.name = name;
        }
    }

    private class VariableInfo {
        final int index;
        final String descriptor;
        boolean isArg = false;
        boolean isEffectivelyFinal = true;
        boolean isForceFinal = false;
        boolean isCaptured = false;

        VariableInfo(int index, String descriptor) {
            this.index = index;
            this.descriptor = descriptor;
        }

        VariableInfo(int index) {
            this(index, j_l_Object_DESC);
        }

        void makeNonFinal(ASTNode node) {
            if (isForceFinal) {
                error(node, "Variable is final and cannot be modified");
            } else if (isCaptured) {
                error(node, "Variable is captured in a closure and cannot be modified");
            } else {
                isEffectivelyFinal = false;
            }
        }

        void makeCaptured(ASTNode node) {
            if (!isEffectivelyFinal) {
                error(node, "Variable may be modified and cannot be captured in a closure");
            }
            isCaptured = true;
        }
    }
}
