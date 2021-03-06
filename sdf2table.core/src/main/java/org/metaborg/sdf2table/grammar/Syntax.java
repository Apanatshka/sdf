package org.metaborg.sdf2table.grammar;

import java.util.ArrayList;
import java.util.List;

import org.metaborg.sdf2table.parsetable.ContextualProduction;
import org.metaborg.sdf2table.symbol.SymbolCollection;

public class Syntax{
	List<SyntaxProduction> _productions = new ArrayList<>();
	List<ContextualProduction> _cproductions = new ArrayList<>();
	SymbolCollection _symbols = new SymbolCollection();
	Module _main_module = null;
	
	public Syntax(){
		//
	}
	
	public Module mainModule(){
		return _main_module;
	}
	
	public void setMainModule(Module module){
		_main_module = module;
	}
	
	public SymbolCollection symbols(){
		return _symbols;
	}
	
	public void computeFollowSets(){ // Total complexity O(3n)
		for(SyntaxProduction p : _productions){
			p.computeDependencies();
		}
		for(ContextualProduction p : _cproductions){
			p.computeDependencies();
		}
		
		for(SyntaxProduction p : _productions){
			p.computeSets();
		}
		for(ContextualProduction p : _cproductions){
			p.computeSets();
		}
		
		for(SyntaxProduction p : _productions){
			p.computeSetsComponents();
		}
		for(ContextualProduction p : _cproductions){
			p.computeSetsComponents();
		}
	}
	
	/**
	 * Get the syntax definition production corresponding to the production prod.
	 * This method can be use to choose a unique internal Java representation of a production.
	 * @param prod A production.
	 * @return A production equals to prod which is registered by this Syntax instance,
	 * or null is prod is not declared in the syntax definition.
	 */
	public SyntaxProduction uniqueProduction(SyntaxProduction prod){
		for(SyntaxProduction p : _productions){
			if(p.merge(prod))
				return p;
		}
		
		_productions.add(prod);
		return prod;
	}
	
	public List<SyntaxProduction> productions(){
		return _productions;
	}
	
	public List<ContextualProduction> contextualProductions(){
		return _cproductions;
	}
	
	public SyntaxProduction startProduction(){
		for(SyntaxProduction p : _productions){
			if(p.product().isFileStart())
				return p;
		}
		return null;
	}

	public void declareContextualProduction(ContextualProduction contextualProduction) {
		_cproductions.add(contextualProduction);
	}
}
