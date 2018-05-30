package com.evensgn.emcompiler.ir;

import java.util.Map;

public class IRMove extends IRInstruction {
    private IRRegister lhs;
    private RegValue rhs;

    public IRMove(BasicBlock parentBB, IRRegister lhs, RegValue rhs) {
        super(parentBB);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    public IRRegister getLhs() {
        return lhs;
    }

    public RegValue getRhs() {
        return rhs;
    }

    @Override
    public IRMove copyRename(Map<Object, Object> renameMap) {
        return new IRMove(
                (BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB()),
                (IRRegister) renameMap.getOrDefault(lhs, lhs),
                (RegValue) renameMap.getOrDefault(rhs, rhs)
        );
    }
}
