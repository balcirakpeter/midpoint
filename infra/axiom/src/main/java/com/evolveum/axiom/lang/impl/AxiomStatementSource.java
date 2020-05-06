package com.evolveum.axiom.lang.impl;

import java.beans.Statement;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import com.evolveum.axiom.api.AxiomIdentifier;
import com.evolveum.axiom.lang.antlr.AxiomLexer;
import com.evolveum.axiom.lang.antlr.AxiomParser;
import com.evolveum.axiom.lang.antlr.AxiomParser.StatementContext;
import com.evolveum.axiom.lang.api.AxiomStatementStreamListener;

public class AxiomStatementSource implements AxiomModelInfo {

    private final StatementContext root;

    public static AxiomStatementSource from(InputStream stream) throws IOException, AxiomSyntaxException {
        return from(null, CharStreams.fromStream(stream));
    }

    public static AxiomStatementSource from(String sourceName, CharStream stream) throws AxiomSyntaxException {

        AxiomLexer lexer = new AxiomLexer(stream);
        AxiomParser parser = new AxiomParser(new CommonTokenStream(lexer));

        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        AxiomErrorListener errorListener = new AxiomErrorListener(sourceName);
        parser.addErrorListener(errorListener);
        StatementContext statement = parser.statement();
        errorListener.validate();
        return new AxiomStatementSource(statement);
    }

    private AxiomStatementSource(StatementContext statement) {
        this.root = statement;
    }

    @Override
    public String getModelName() {
        return root.argument().identifier().localIdentifier().getText();
    }

    @Override
    public String getNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    public void stream(AxiomIdentifierResolver resolver, AxiomStatementStreamListener listener) {
        stream(resolver, listener, Optional.empty());
    }

    private void stream(AxiomIdentifierResolver resolver, AxiomStatementStreamListener listener,
            Optional<Set<AxiomIdentifier>> emitOnly) {
        AxiomAntlrVisitor<?> visitor = new AxiomAntlrVisitor<>(resolver, listener, emitOnly.orElse(null));
        visitor.visit(root);
    }
}