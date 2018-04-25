package com.evensgn.emcompiler.frontend;

import com.evensgn.emcompiler.ast.*;
import com.evensgn.emcompiler.scope.*;
import com.evensgn.emcompiler.type.*;
import com.evensgn.emcompiler.utils.SemanticError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GlobalScopePreScanner implements ASTVisitor {
    private Scope scope = new Scope();

    public Scope getScope() {
        return scope;
    }

    private void putBuiltInFunc(Scope thisScope, String name, List<VarEntity> parameters, Type returnType) {
        String key = Scope.funcKey(name);
        FuncEntity entity = new FuncEntity(name, new FunctionType(name));
        entity.setParameters(parameters);
        entity.setReturnType(returnType);
        thisScope.putCheck(name, key, entity);
    }

    private void putBuiltInFuncs() {
        putBuiltInFunc(scope,"print", Collections.singletonList(new VarEntity("str", StringType.getInstance())), VoidType.getInstance());
        putBuiltInFunc(scope,"println", Collections.singletonList(new VarEntity("str", StringType.getInstance())), VoidType.getInstance());
        putBuiltInFunc(scope,"getString", new ArrayList<>(), StringType.getInstance());
        putBuiltInFunc(scope,"getInt", new ArrayList<>(), IntType.getInstance());
        putBuiltInFunc(scope,"toString", Collections.singletonList(new VarEntity("i", IntType.getInstance())), StringType.getInstance());
        String stringKey = Scope.classKey("#string");
        ClassEntity stringEntity = new ClassEntity("string", new ClassType("#string"), scope);
        putBuiltInFunc(stringEntity.getScope(), "length", new ArrayList<>(), IntType.getInstance());
        putBuiltInFunc(stringEntity.getScope(), "substring", Arrays.asList(new VarEntity("left", IntType.getInstance()), new VarEntity("right", IntType.getInstance())), StringType.getInstance());
        putBuiltInFunc(stringEntity.getScope(), "parseInt", new ArrayList<>(), IntType.getInstance());
        putBuiltInFunc(stringEntity.getScope(), "ord", Collections.singletonList(new VarEntity("pos", IntType.getInstance())), IntType.getInstance());
        scope.putCheck("#string", stringKey, stringEntity);
        String arrayKey = Scope.classKey("#array");
        ClassEntity arrayEntity = new ClassEntity("string", new ClassType("#array"), scope);
        putBuiltInFunc(arrayEntity.getScope(), "size", new ArrayList<>(), IntType.getInstance());
        scope.putCheck("#array", arrayKey, arrayEntity);
    }

    private void checkMainFunc(FuncEntity mainFunc) {
        if (mainFunc == null) throw new SemanticError("\"main\" function not found");
        if (!(mainFunc.getReturnType() instanceof IntType)) throw new SemanticError("\"main\" function's return type should be \"int\"");
        if (!mainFunc.getParameters().isEmpty()) throw new SemanticError("\"main\" function should have no parameter");
    }

    @Override
    public void visit(ProgramNode node) {
        putBuiltInFuncs();
        for (DeclNode decl : node.getDecls()) {
            if (decl instanceof VarDeclNode) continue;
            decl.accept(this);
        }
        checkMainFunc((FuncEntity) scope.get(Scope.funcKey("main")));
    }

    @Override
    public void visit(FuncDeclNode node) {
        String key = Scope.funcKey(node.getName());
        Entity entity = new FuncEntity(node);
        scope.putCheck(node.location(), node.getName(), key, entity);
    }

    @Override
    public void visit(ClassDeclNode node) {
        String key = Scope.classKey(node.getName());
        Entity entity = new ClassEntity(node, scope);
        scope.putCheck(node.location(), node.getName(), key, entity);
    }

    @Override
    public void visit(VarDeclListNode node) {

    }

    @Override
    public void visit(VarDeclNode node) {

    }

    @Override
    public void visit(BlockStmtNode node) {

    }

    @Override
    public void visit(ExprStmtNode node) {

    }

    @Override
    public void visit(CondStmtNode node) {

    }

    @Override
    public void visit(WhileStmtNode node) {

    }

    @Override
    public void visit(ForStmtNode node) {

    }

    @Override
    public void visit(ContinueStmtNode node) {

    }

    @Override
    public void visit(BreakStmtNode node) {

    }

    @Override
    public void visit(ReturnStmtNode node) {

    }

    @Override
    public void visit(SuffixExprNode node) {

    }

    @Override
    public void visit(FuncCallExprNode node) {

    }

    @Override
    public void visit(SubscriptExprNode node) {

    }

    @Override
    public void visit(MemberAccessExprNode node) {

    }

    @Override
    public void visit(PrefixExprNode node) {

    }

    @Override
    public void visit(NewExprNode node) {

    }

    @Override
    public void visit(BinaryExprNode node) {

    }

    @Override
    public void visit(AssignExprNode node) {

    }

    @Override
    public void visit(IdentifierExprNode node) {

    }

    @Override
    public void visit(IntConstExprNode node) {

    }

    @Override
    public void visit(StringConstExprNode node) {

    }

    @Override
    public void visit(BoolConstExprNode node) {

    }

    @Override
    public void visit(NullExprNode node) {

    }

    @Override
    public void visit(TypeNode node) {

    }
}
