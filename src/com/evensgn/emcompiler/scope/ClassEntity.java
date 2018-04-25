package com.evensgn.emcompiler.scope;

import com.evensgn.emcompiler.ast.ClassDeclNode;
import com.evensgn.emcompiler.ast.FuncDeclNode;
import com.evensgn.emcompiler.type.ClassType;
import com.evensgn.emcompiler.type.Type;
import com.evensgn.emcompiler.utils.SemanticError;

public class ClassEntity extends Entity {
    Scope scope;

    public ClassEntity(String name, Type type, Scope parentScope) {
        super(name, type);
        scope = new Scope(parentScope);
    }

    public ClassEntity(ClassDeclNode node, Scope parentScope) {
        super(node.getName(), new ClassType(node.getName()));
        String key;
        FuncEntity entity;
        for (FuncDeclNode funcMemDecl : node.getFuncMember()) {
            key = Scope.funcKey(funcMemDecl.getName());
            entity = new FuncEntity(funcMemDecl);
            if (!scope.put(key, entity)) throw new SemanticError(funcMemDecl.location(), String.format("Symbol name \"%s\" already defined", funcMemDecl.getName()));
        }
        scope = new Scope(parentScope);
    }

    public Scope getScope() {
        return scope;
    }
}
