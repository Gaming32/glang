package glang.compiler.bytecode;

import glang.compiler.SourceLocation;
import glang.compiler.error.CompileFailedException;
import glang.compiler.error.ErrorCollector;
import glang.compiler.token.Token;
import glang.compiler.token.TokenType;
import glang.compiler.tree.ASTNode;
import glang.compiler.tree.GlangTreeifier;
import glang.compiler.tree.StatementList;
import glang.compiler.tree.expression.*;
import glang.compiler.tree.statement.BlockStatement;
import glang.compiler.tree.statement.ExpressionStatement;
import glang.compiler.tree.statement.StatementNode;
import glang.compiler.tree.statement.VariableDeclaration;
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

    private static final String GLOBALS = "$$GLOBALS$$";
    private static final String GLOBALS_DESC = "Ljava/util/Map;";
    private static final String GLOBALS_SIG = "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;";

    private static final String CONDY_DESC_PREFIX = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;";

    private final String namespacePath;
    private final String className;
    private final String classNameInternal;
    private final StatementList code;
    private final Function<String, ClassWriter> visitors;
    private final ErrorCollector errorCollector;

    private boolean insertDebugPrints = false;

    private final StateStack<ClassState> classStates = new StateStack<>(ClassState::new);
    private final StateStack<MethodState> methodStates = new StateStack<>(MethodState::new);
    private final StateStack<ScopeState> scopeStates = new StateStack<>(ScopeState::new);

    public GlangCompiler(String namespacePath, StatementList code, Function<String, ClassWriter> visitors, ErrorCollector errorCollector) {
        this.namespacePath = namespacePath;
        this.className = namespacePathToClassName(namespacePath);
        this.classNameInternal = className.replace('.', '/');
        this.code = code;
        this.visitors = visitors;
        this.errorCollector = errorCollector;
    }

    public GlangCompiler(String namespacePath, String source, Function<String, ClassWriter> visitors) throws CompileFailedException {
        this(namespacePath, GlangTreeifier.treeify(source), visitors, new ErrorCollector(source));
    }

    public static void compile(
        String namespacePath, StatementList code, Function<String, ClassWriter> visitors, ErrorCollector errorCollector
    ) throws CompileFailedException {
        final GlangCompiler compiler = new GlangCompiler(namespacePath, code, visitors, errorCollector);
        compiler.compile();
        errorCollector.throwIfFailed();
    }

    public static void compile(String namespacePath, String source, Function<String, ClassWriter> visitors) throws CompileFailedException {
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
        clazz.visitor = getVisitor(className);
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
        final VariableInfo argsVariable = new VariableInfo(0);
        argsVariable.isArg = true;
        scope.variables.put("args", argsVariable);
        method.currentLocal++;
        compileRoot();
        scopeStates.pop();
        method.visitor.visitMaxs(0, 0);
        method.visitor.visitEnd();
        methodStates.pop();

        clazz.visitor.visitEnd();
        classStates.pop();
    }

    private ClassVisitor getVisitor(String name) {
        final ClassWriter writer = visitors.apply(name);
        if (!writer.hasFlags(ClassWriter.COMPUTE_FRAMES)) {
            throw new IllegalArgumentException(
                "GlangCompiler relies on ClassWriter.COMPUTE_FRAMES. " +
                    "Please pass this flag to ClassWriters passed to GlangCompiler."
            );
        }
        return writer;
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
            error(node, "ASTNode " + node.getClass().getSimpleName() + " not supported");
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
        } else if (statement instanceof VariableDeclaration decl) {
            final ScopeState scope = scopeStates.get();
            VariableInfo variable = scope.variables.get(decl.getName());
            if (variable != null) {
                error(statement, "Duplicate local variable " + Token.Identifier.prettyPrint(decl.getName()));
            } else {
                variable = new VariableInfo(method.currentLocal++);
                scope.variables.put(decl.getName(), variable);
            }
            if (decl.getInitializer() != null) {
                compileExpression(decl.getInitializer());
            } else {
                method.checkLine(statement);
                visitor.visitInsn(Opcodes.ACONST_NULL);
            }
            method.checkLine(statement);
            visitor.visitVarInsn(Opcodes.ASTORE, variable.index);
        } else {
            error(statement, "StatementNode " + statement.getClass().getSimpleName() + " not supported");
        }
    }

    private void compileExpression(ExpressionNode expression) {
        final MethodState method = methodStates.get();
        final MethodVisitor visitor = method.visitor;

        if (expression instanceof LiteralExpression<?> literal) {
            compileLiteral(literal);
        } else if (expression instanceof CallExpression call) {
            int argCount = call.getArgs().size();
            if (call.getTarget() instanceof AccessExpression access) {
                compileExpression(access.getTarget());
                method.checkLine(expression);
                visitor.visitInsn(Opcodes.DUP);
                compileAccess(access, access.getType().toMethodAccess());
                visitor.visitInsn(Opcodes.SWAP);
                argCount++; // TODO: I think this will break if argCount > 16
            } else {
                compileExpression(call.getTarget());
            }
            if (argCount < 17) {
                call.getArgs().forEach(this::compileExpression);
                method.checkLine(expression);
                visitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC, g_r_ObjectInvokers, "invokeObject",
                    "(" + j_l_Object_DESC.repeat(argCount + 1) + ")" + j_l_Object_DESC,
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
        } else if (expression instanceof AccessExpression access) {
            compileExpression(access.getTarget());
            method.checkLine(expression);
            compileAccess(access, access.getType());
        } else if (expression instanceof AssignmentExpression assignment) {
            compileAssignment(assignment);
        } else {
            error(expression, "ExpressionNode " + expression.getClass().getSimpleName() + " not supported");
            method.checkLine(expression);
            visitor.visitInsn(Opcodes.ACONST_NULL);
        }
    }

    private void compileAssignment(AssignmentExpression assignment) {
        final MethodState method = methodStates.get();
        final MethodVisitor visitor = method.visitor;
        if (!(assignment.getVariable() instanceof AssignableExpression)) {
            throw new IllegalArgumentException(assignment.getVariable() + " is not assignable!");
        }
        if (assignment.getOperator() != TokenType.EQUAL) {
            error(assignment, "Only = is supported for AssignmentExpression currently");
            compileExpression(assignment.getValue());
            return;
        }
        if (assignment.getVariable() instanceof IdentifierExpression identifier) {
            boolean found = false;
            for (final ScopeState scope : (Iterable<ScopeState>)scopeStates.stack::descendingIterator) {
                final VariableInfo variable = scope.variables.get(identifier.getValue());
                if (variable != null) {
                    found = true;
                    variable.makeNonFinal(identifier);
                    compileExpression(assignment.getValue());
                    method.checkLine(assignment);
                    visitor.visitInsn(Opcodes.DUP);
                    visitor.visitVarInsn(Opcodes.ASTORE, variable.index);
                    break;
                }
            }
            if (!found) {
                method.checkLine(assignment);
                visitor.visitFieldInsn(Opcodes.GETSTATIC, classNameInternal, GLOBALS, GLOBALS_DESC);
                visitor.visitLdcInsn(identifier.getValue());
                compileExpression(assignment.getValue());
                method.checkLine(assignment);
                visitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC, g_r_GlangRuntime, "putGlobal",
                    "(Ljava/util/Map;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;",
                    false
                );
            }
        } else {
            error(assignment, "AssignmentExpression to " + assignment.getVariable().getClass().getSimpleName() + " not supported");
            compileExpression(assignment.getValue());
        }
    }

    private void compileAccess(AccessExpression access, AccessExpression.Type type) {
        final MethodState method = methodStates.get();
        final MethodVisitor visitor = method.visitor;
        method.checkLine(access);
        visitor.visitLdcInsn(access.getMember());
        switch (type) {
            case METHOD -> visitor.visitMethodInsn(
                Opcodes.INVOKESTATIC, g_r_GlangRuntime, "getInstanceMethod",
                "(Ljava/lang/Object;Ljava/lang/String;)Lglang/runtime/lookup/MethodLookup;",
                false
            );
            case DIRECT_METHOD -> visitor.visitMethodInsn(
                Opcodes.INVOKESTATIC, g_r_GlangRuntime, "getDirectMethod",
                "(Ljava/lang/Object;Ljava/lang/String;)Lglang/runtime/lookup/MethodLookup;",
                false
            );
            default -> {
                error(access, "AccessExpression " + type + " not supported");
                method.checkLine(access);
                visitor.visitInsn(Opcodes.POP);
            }
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
                error(literal, "Number " + literal.getClass().getSimpleName() + " not supported");
                method.checkLine(literal);
                visitor.visitInsn(Opcodes.ACONST_NULL);
            }
        } else if (literal instanceof IdentifierExpression identifier) {
            boolean found = false;
            for (final ScopeState scope : (Iterable<ScopeState>)scopeStates.stack::descendingIterator) {
                final VariableInfo variable = scope.variables.get(identifier.getValue());
                if (variable != null) {
                    found = true;
                    if (scope.owner != method) {
                        variable.makeCaptured(identifier);
                        error(identifier, "Cannot access scopes from other methods currently");
                    }
                    method.checkLine(literal);
                    visitor.visitVarInsn(Opcodes.ALOAD, variable.index);
                    break;
                }
            }
            if (!found) {
                method.checkLine(literal);
                visitor.visitFieldInsn(Opcodes.GETSTATIC, classNameInternal, GLOBALS, GLOBALS_DESC);
                visitor.visitLdcInsn(identifier.getValue());
                visitor.visitMethodInsn(
                    Opcodes.INVOKESTATIC, g_r_GlangRuntime, "getGlobal",
                    "(Ljava/util/Map;Ljava/lang/String;)Ljava/lang/Object;",
                    false
                );
            }
        } else if (literal instanceof NullExpression) {
            method.checkLine(literal);
            visitor.visitInsn(Opcodes.ACONST_NULL);
        } else {
            error(literal, "LiteralExpression " + literal.getClass().getSimpleName() + " not supported");
            method.checkLine(literal);
            visitor.visitInsn(Opcodes.ACONST_NULL);
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

    private interface State {
        default void end() {
        }
    }

    private static class StateStack<T extends State> {
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
            final T removed = stack.removeLast();
            removed.end();
            return removed;
        }
    }

    private static class ClassState implements State {
        final String name;
        ClassVisitor visitor;

        ClassState(String name) {
            this.name = name;
        }
    }

    private class MethodState implements State {
        final String name;
        final ClassState owner = classStates.get();
        MethodVisitor visitor;
        int currentLine = -1;
        int currentLocal = 0;

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

    private class ScopeState implements State {
        final String name;
        final MethodState owner = methodStates.get();
        final Label startLabel = new Label();
        final Label endLabel = new Label();
        final Map<String, VariableInfo> variables = new LinkedHashMap<>();

        ScopeState(String name) {
            this.name = name;
            owner.visitor.visitLabel(startLabel);
        }

        @Override
        public void end() {
            owner.currentLocal -= variables.size();
            owner.visitor.visitLabel(endLabel);
            for (final var entry : variables.entrySet()) {
                owner.visitor.visitLocalVariable(
                    entry.getKey(), j_l_Object_DESC, null, startLabel, endLabel, entry.getValue().index
                );
            }
        }
    }

    private class VariableInfo {
        final int index;
        boolean isArg = false;
        boolean isEffectivelyFinal = true;
        boolean isForceFinal = false;
        boolean isCaptured = false;

        VariableInfo(int index) {
            this.index = index;
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