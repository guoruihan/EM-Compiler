package com.evensgn.emcompiler.ast;

import com.evensgn.emcompiler.type.Type;

public class PrimitiveTypeNode extends TypeNode {
    private Location location;

    public PrimitiveTypeNode(Type.Types type, Location location) {
        this.type = type;
        this.location = location;
    }

    @Override
    public Location location() {
        return location;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
