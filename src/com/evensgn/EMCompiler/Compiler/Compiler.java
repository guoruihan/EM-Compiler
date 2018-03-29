package com.evensgn.EMCompiler.Compiler;

import com.evensgn.EMCompiler.FrontEnd.ASTBuilder;
import com.evensgn.EMCompiler.Parser.EMxStarLexer;
import com.evensgn.EMCompiler.Parser.EMxStarParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.InputStream;
import java.io.OutputStream;

public class Compiler {
    private InputStream inS;
    private OutputStream outS;

    public Compiler(InputStream inS, OutputStream outS) {
        this.inS = inS;
        this.outS = outS;
    }

    private void testTree() throws Exception {
        CharStream input = CharStreams.fromStream(inS);
        EMxStarLexer lexer = new EMxStarLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        EMxStarParser parser = new EMxStarParser(tokens);
        ParseTree tree = parser.program();
        ASTBuilder astBuilder = new ASTBuilder();
        new ParseTreeWalker().walk(astBuilder, tree);
    }

    public void run() throws Exception {
        System.out.println("Compiler is running");
        testTree();
    }
}
