package com.evensgn.emcompiler.backend;

import com.evensgn.emcompiler.ast.AssignExprNode;
import com.evensgn.emcompiler.ast.FuncCallExprNode;
import com.evensgn.emcompiler.ir.*;

import java.util.*;

public class RegLivelinessAnalysis {
    private IRRoot ir;
    private boolean eliminationChanged;

    public RegLivelinessAnalysis(IRRoot ir) {
        this.ir = ir;
    }

    private void livelinessAnalysis(IRFunction irFunction) {
        List<BasicBlock> reversePreOrder = irFunction.getReversePreOrder();
        for (BasicBlock bb : reversePreOrder) {
            // init basic block
            for (IRInstruction inst = bb.getFirstInst(); inst != null; inst = inst.getNextInst()) {
                if (inst.liveIn == null) inst.liveIn = new HashSet<>();
                else inst.liveIn.clear();
                if (inst.liveOut == null) inst.liveOut = new HashSet<>();
                else inst.liveOut.clear();
            }
        }
        Set<VirtualRegister> liveIn = new HashSet<>();
        Set<VirtualRegister> liveOut = new HashSet<>();

        // iterations to solve liveliness equation
        boolean converged = false;
        while (!converged) {
            converged = true;
            for (BasicBlock bb : reversePreOrder) {
                for (IRInstruction inst = bb.getLastInst(); inst != null; inst = inst.getPrevInst()) {
                    liveIn.clear();
                    liveOut.clear();
                    if (inst instanceof IRJumpInstruction) {
                        if (inst instanceof IRJump) {
                            liveOut.addAll(((IRJump) inst).getTargetBB().getFirstInst().liveIn);
                        } else if (inst instanceof IRBranch) {
                            liveOut.addAll(((IRBranch) inst).getThenBB().getFirstInst().liveIn);
                            liveOut.addAll(((IRBranch) inst).getElseBB().getFirstInst().liveIn);
                        }
                    } else {
                        if (inst.getNextInst() != null)
                            liveOut.addAll(inst.getNextInst().liveIn);
                    }
                    liveIn.addAll(liveOut);
                    IRRegister definedReg = inst.getDefinedRegister();
                    if (definedReg instanceof VirtualRegister) {
                        liveIn.remove(definedReg);
                    }
                    for (IRRegister usedReg : inst.getUsedRegisters()) {
                        if (usedReg instanceof VirtualRegister) {
                            liveIn.add((VirtualRegister) usedReg);
                        }
                    }
                    if (!inst.liveIn.equals(liveIn)) {
                        converged = false;
                        inst.liveIn.clear();
                        inst.liveIn.addAll(liveIn);
                    }
                    if (!inst.liveOut.equals(liveOut)) {
                        converged = false;
                        inst.liveOut.clear();
                        inst.liveOut.addAll(liveOut);
                    }
                }
            }
        }
    }

    void tryEliminate(IRFunction func) {
        List<BasicBlock> reversePreOrder = func.getReversePreOrder();
        for (BasicBlock bb : reversePreOrder) {
            for (IRInstruction inst = bb.getLastInst(), prevInst; inst != null; inst = prevInst) {
                prevInst = inst.getPrevInst();
                if (inst instanceof IRBinaryOperation || inst instanceof IRComparison ||
                        inst instanceof IRLoad || inst instanceof IRMove || inst instanceof IRUnaryOperation ||
                        inst instanceof IRHeapAlloc) {
                    IRRegister dest = inst.getDefinedRegister();
                    if (dest == null || !inst.liveOut.contains(dest)) {
                        eliminationChanged = true;
                        inst.remove();
                    }
                }
            }
        }

        for (IRRoot.ForRecord forRec : ir.forRecMap.values()) {
            if (forRec.processed) continue;
            boolean isFieldOutside = false;
            if (forRec.condBB == null || forRec.stepBB == null || forRec.bodyBB == null || forRec.afterBB == null) continue;
            List<BasicBlock> bbList = new ArrayList<>();
            bbList.add(forRec.condBB); bbList.add(forRec.stepBB); bbList.add(forRec.bodyBB); bbList.add(forRec.afterBB);
            IRInstruction afterFirstInst = forRec.afterBB.getFirstInst();
            for (int i = 0; i < 3; ++i) {
                for (IRInstruction inst = bbList.get(i).getFirstInst(); inst != null; inst = inst.getNextInst()) {
                    if (inst instanceof IRFunctionCall) {
                        isFieldOutside = true;
                        continue;
                    }
                    if (inst.getDefinedRegister() != null) {
                        if (afterFirstInst.liveIn.contains(inst.getDefinedRegister())) {
                            isFieldOutside = true;
                        }
                        continue;
                    }
                    if (inst instanceof IRStore) {
                        isFieldOutside = true;
                        continue;
                    }
                    if (inst instanceof IRJump) {
                        if (!bbList.contains(((IRJump) inst).getTargetBB()))
                            isFieldOutside = true;
                        continue;
                    }
                    if (inst instanceof IRBranch) {
                        if (!bbList.contains(((IRBranch) inst).getThenBB()) || !bbList.contains(((IRBranch) inst).getElseBB()))
                            isFieldOutside = true;
                        continue;
                    }
                    if (inst instanceof IRReturn || inst instanceof IRPush || inst instanceof IRStore) {
                        isFieldOutside = true;
                        continue;
                    }
                }
            }
            if (!isFieldOutside) {
                forRec.condBB.reInit();
                forRec.condBB.setJumpInst(new IRJump(forRec.condBB, forRec.afterBB));
                forRec.processed = true;
            }
        }
    }

    private Map<BasicBlock, BasicBlock> jumpTargetBBMap = new HashMap<>();

    BasicBlock replaceJumpTarget(BasicBlock bb) {
        BasicBlock ret = bb, query = jumpTargetBBMap.get(bb);
        while (query != null) {
            ret = query;
            query = jumpTargetBBMap.get(query);
        }
        return ret;
    }

    void removeBlankBB(IRFunction func) {
        jumpTargetBBMap.clear();
        for (BasicBlock bb : func.getReversePostOrder()) {
            if (bb.getFirstInst() == bb.getLastInst()) {
                IRInstruction inst = bb.getFirstInst();
                if (inst instanceof IRJump) {
                    jumpTargetBBMap.put(bb, ((IRJump) inst).getTargetBB());
                }
            }
        }
        for (BasicBlock bb : func.getReversePostOrder()) {
            if (bb.getLastInst() instanceof IRJump) {
                IRJump jumpInst = (IRJump) bb.getLastInst();
                jumpInst.setTargetBB(replaceJumpTarget(jumpInst.getTargetBB()));
            } else if (bb.getLastInst() instanceof IRBranch) {
                IRBranch branchInst = (IRBranch) bb.getLastInst();
                branchInst.setThenBB(replaceJumpTarget(branchInst.getThenBB()));
                branchInst.setElseBB(replaceJumpTarget(branchInst.getElseBB()));
                if (branchInst.getThenBB() == branchInst.getElseBB()) {
                    branchInst.replace(new IRJump(bb, branchInst.getThenBB()));
                }
            }
        }
    }

    public void run() {
        for (IRFunction irFunction : ir.getFuncs().values()) {
            livelinessAnalysis(irFunction);
        }
        eliminationChanged = true;
        while (eliminationChanged) {
            eliminationChanged = false;
            for (IRFunction irFunction : ir.getFuncs().values()) {
                if (irFunction.isBuiltIn()) continue;
                tryEliminate(irFunction);
                removeBlankBB(irFunction);
                livelinessAnalysis(irFunction);
            }
        }
    }
}
