package org.metaborg.sdf2table.symbol;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoConstructor;

public class ContextFree extends ConcreteNonTerminal{
	private Symbol _symbol;
	private static final StrategoConstructor CONS_CF = new StrategoConstructor("cf", 1);
	
	public ContextFree(Symbol symbol){
		super();
		_symbol = symbol;
	}
	
	@Override
	public Type type(){
		return Type.CONTEXT_FREE;
	}
	
	@Override
	public boolean isLayout(){
		return _symbol.isLayout();
	}
	
	@Override
	public boolean isEpsilon(){
		return isLayout() || super.isEpsilon();
	}
	
	public Symbol getSymbol(){
		return _symbol;
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof ContextFree){
			return other != null && _symbol != null && _symbol.equals(((ContextFree)other).getSymbol());
		}
		return false;
	}

	@Override
	public String toString() {
		return _symbol.toString()+"-CF";
	}
	
	public IStrategoTerm toATerm(){
		return new StrategoAppl(CONS_CF, new IStrategoTerm[]{_symbol.toATerm()}, null, 0);
	}

}
