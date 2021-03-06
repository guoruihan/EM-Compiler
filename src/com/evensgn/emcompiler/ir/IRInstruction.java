package com.evensgn.emcompiler.ir;

import com.evensgn.emcompiler.utils.CompilerError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class IRInstruction {
    private IRInstruction prevInst = null, nextInst = null;
    private BasicBlock parentBB;
    protected List<IRRegister> usedRegisters = new ArrayList<>();
    protected List<RegValue> usedRegValues = new ArrayList<>();
    private boolean removed = false;
    public Set<VirtualRegister> liveIn = null, liveOut = null;

    public IRInstruction(BasicBlock parentBB) {
        this.parentBB = parentBB;
    }

    public void prependInst(IRInstruction inst) {
        if (prevInst != null) prevInst.linkNextInst(inst);
        else parentBB.setFirstInst(inst);
        inst.linkNextInst(this);
    }

    public void appendInst(IRInstruction inst) {
        if (nextInst != null) nextInst.linkPrevInst(inst);
        else parentBB.setLastInst(inst);
        inst.linkPrevInst(this);
    }

    public void linkPrevInst(IRInstruction inst) {
        prevInst = inst;
        inst.setNextInst(this);
    }

    public void linkNextInst(IRInstruction inst) {
        nextInst = inst;
        inst.setPrevInst(this);
    }

    public void setPrevInst(IRInstruction prevInst) {
        this.prevInst = prevInst;
    }

    public void setNextInst(IRInstruction nextInst) {
        this.nextInst = nextInst;
    }

    public void remove() {
        if (removed) {
            throw new CompilerError("cannot remove an instruction already removed");
        }
        removed = true;
        if (prevInst != null) prevInst.setNextInst(nextInst);
        if (nextInst != null) nextInst.setPrevInst(prevInst);
        if (this instanceof IRJumpInstruction) {
            parentBB.removeJumpInst();
        }
        if (this == parentBB.getFirstInst()) parentBB.setFirstInst(nextInst);
        if (this == parentBB.getLastInst()) parentBB.setLastInst(prevInst);
    }

    public void replace(IRInstruction inst) {
        if (removed) {
            throw new CompilerError("cannot remove an instruction already removed");
        }
        removed = true;
        inst.setPrevInst(prevInst);
        inst.setNextInst(nextInst);
        if (prevInst != null) prevInst.setNextInst(inst);
        if (nextInst != null) nextInst.setPrevInst(inst);
        if (this == parentBB.getFirstInst()) parentBB.setFirstInst(inst);
        if (this == parentBB.getLastInst()) parentBB.setLastInst(inst);
    }

    public abstract void accept(IRVisitor visitor);

    public IRInstruction getPrevInst() {
        return prevInst;
    }

    public IRInstruction getNextInst() {
        return nextInst;
    }

    public BasicBlock getParentBB() {
        return parentBB;
    }

    public abstract IRInstruction copyRename(Map<Object, Object> renameMap);

    public abstract void reloadUsedRegistersRegValues();

    public List<IRRegister> getUsedRegisters() {
        return usedRegisters;
    }

    public List<RegValue> getUsedRegValues() {
        return usedRegValues;
    }

    public boolean isRemoved() {
        return removed;
    }

    public abstract IRRegister getDefinedRegister();

    public abstract void setUsedRegisters(Map<IRRegister, IRRegister> renameMap);

    public abstract void setDefinedRegister(IRRegister vreg);
}
