package org.metaborg.sdf2table.symbol;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoConstructor;

public class Lexical extends ConcreteNonTerminal{
	private static final StrategoConstructor CONS_LEX = new StrategoConstructor("lex", 1);
	
	private Symbol _symbol;
	
	public Lexical(Symbol symbol){
		super();
		_symbol = symbol;
	}
	
	@Override
	public Type type(){
		return Type.LEXICAL;
	}
	
	public Symbol getSymbol(){
		return _symbol;
	}
	
	@Override
	public boolean isLayout(){
		return _symbol.isLayout();
	}
	
	@Override
	public boolean isEpsilon(){
		return isLayout() || super.isEpsilon();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Lexical){
			return other != null && _symbol != null && _symbol.equals(((Lexical)other).getSymbol());
		}
		return false;
	}

	@Override
	public String toString() {
		return _symbol.toString()+"-LEX";
	}
	
	public IStrategoTerm toATerm(){
		return new StrategoAppl(CONS_LEX, new IStrategoTerm[]{_symbol.toATerm()}, null, 0);
	}
}
