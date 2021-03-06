package org.metaborg.sdf2table.grammar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.metaborg.sdf2table.core.Utilities;
import org.metaborg.sdf2table.io.Exporter;
import org.metaborg.sdf2table.parsetable.Context;
import org.metaborg.sdf2table.parsetable.ContextualProduction;
import org.metaborg.sdf2table.parsetable.ContextualSymbol;
import org.metaborg.sdf2table.parsetable.Label;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.symbol.CharClass;
import org.metaborg.sdf2table.symbol.NonTerminal;
import org.metaborg.sdf2table.symbol.Symbol;
import org.metaborg.sdf2table.symbol.Terminal;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.StrategoConstructor;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.StrategoString;

/**
 * Grammar production.
 * 
 */
public class SyntaxProduction extends Production{
	/**
	 * Some Stratego constructors for the ATerm translation.
	 */
	private static final StrategoConstructor CONS_LABEL = new StrategoConstructor("label", 2);
	private static final StrategoConstructor CONS_PROD = new StrategoConstructor("prod", 3);
	private static final StrategoConstructor CONS_NO_ATTRS = new StrategoConstructor("no-attrs", 0);
	private static final StrategoConstructor CONS_ATTRS = new StrategoConstructor("attrs", 1);
	private static final StrategoConstructor CONS_TERM = new StrategoConstructor("term", 1);
	private static final StrategoConstructor CONS_CONS = new StrategoConstructor("cons", 1);
	
	/**
	 * Symbol synthesized by the production.
	 */
	NonTerminal _symbol;
	
	/**
	 * Constructor name, if any.
	 */
	String _cons = null;
	
	/**
	 * Attributes of the production.
	 */
	Set<Attribute> _attributes;
	
	/**
	 * List of symbols of the production. From left to right.
	 */
	List<Symbol> _rhs;
	
	Label _label = null;
	Set<ContextualProduction> _cprods = null;
	
	/**
	 * List the priorities where this production is the direct ascendant.
	 */
	//Set<Priority> _priorities = new HashSet<>();
	Priorities _priorities = new Priorities(this);
	
	Set<Symbol> _left;
	Set<Symbol> _right;
	
	/**
	 * String representation of the production.
	 * <p>
	 * This attribute is computed when {@link #toString()} is first called to avoid multiple computation of the same value.
	 * <p>
	 * The string value is also used to compute the {@link #hashCode()}.
	 */
	String _str;
	
	/**
	 * Main constructor.
	 * @param symbol The symbol produced by the production.
	 * @param cons The name of the constructor. May be null is the production have no constructor defined.
	 * @param rhs A list of symbols that compose the production, from left to right.
	 * @param attributes A list of attributes.
	 */
	public SyntaxProduction(NonTerminal symbol, String cons, List<Symbol> rhs, Set<Attribute> attributes){
		_symbol = symbol;
		_rhs = rhs;
		_cons = cons;
		_attributes = attributes;
		
		_label = null;
	}
	
	public void addContextualProduction(ContextualProduction p){
		if(_cprods == null){
			_cprods = new HashSet<>();
		}
		
		_cprods.add(p);
	}
	
	public Set<ContextualProduction> contextualProductions(){
		return _cprods;
	}
	
	/**
	 * Get the list of priorities.
	 * @return The list the priorities where this production is the direct ascendant.
	 */
	public Priorities priorities(){		
		return _priorities;
	}
	
	public Label label(){
		if(_label == null)
			_label = ParseTable.newLabel(this);
		return _label;
	}
	
	public SyntaxProduction syntaxProduction(){
		return this;
	}
	
	public boolean containsTerminal(){
		for(Symbol s : _rhs){
			if(s instanceof Terminal)
				return true;
		}
		return false;
	}
	
	/**
	 * @return The last symbol of the production, or null if the production is empty.
	 */
	public Symbol lastSymbol(){
		if(_rhs.isEmpty())
			return null;
		return _rhs.get(_rhs.size()-1);
	}
	
	/**
	 * Declare priority.
	 * <p>
	 * This method should not be directly used.
	 * @param prio
	 */
	/*public boolean addPriority(Priority prio){
		return _priorities.add(prio);
	}*/
	
	/**
	 * List of attributes.
	 * @return A list of attributes.
	 */
	public Set<Attribute> attributes(){
		return _attributes;
	}
	
	/**
	 * Synthesized symbol.
	 * @return The symbol resulting of the production.
	 */
	public NonTerminal product(){
		return _symbol;
	}
	
	public int size(){
		return _rhs.size();
	}
	
	public boolean isEmpty(){
		return _rhs.isEmpty();
	}
	
	public boolean isEpsilon(){
		for(Symbol s : _rhs){
			if(!s.isLayout()){
				return false;
			}
		}
		return true;
	}
	
	public Symbol symbol(int position){
		return _rhs.get(position);
	}
	
	/**
	 * Symbols defining the production.
	 * @return A list of symbols defining the production, from left to right.
	 */
	public List<Symbol> symbols(){
		return _rhs;
	}
	
	/**
	 * Production constructor.
	 * @return The name of the constructor is any, null otherwise.
	 */
	public String constructor(){
		return _cons;
	}
	
	public boolean potentialLeftDeepConflict(PriorityLevel l){
		Set<SyntaxProduction> set = new HashSet<>();
		return doPotentialLeftDeepConflict(l, set);
	}
	
	public boolean potentialRightDeepConflict(PriorityLevel l){
		Set<SyntaxProduction> set = new HashSet<>();
		return doPotentialRightDeepConflict(l, set);
	}
	
	private boolean doPotentialLeftDeepConflict(PriorityLevel l, Set<SyntaxProduction> set){
		if((left() != null && left().nonEpsilon()) || set.contains(this))
			return false;
		/*if(l.production().priorities().deepConflicts(this, l.position()))
			return true;*/
		if(l.conflicts(this))
			return true;
		
		set.add(this);
		
		for(int i = 0; i < size(); ++i){
			Symbol s = symbol(i);
			
			if(s != null && s instanceof NonTerminal){
				NonTerminal n = (NonTerminal)s;
				for(Production p : n.productions()){
					SyntaxProduction sp = p.syntaxProduction();
					
					if(!directConflicts(sp, i) && sp.doPotentialLeftDeepConflict(l, set)){
						return true;
					}
				}
				
				if(n.nonEpsilon())
					break;
			}else{
				break;
			}
		}
		
		return false;
	}
	
	private boolean doPotentialRightDeepConflict(PriorityLevel l, Set<SyntaxProduction> set){
		if((right() != null && right().nonEpsilon()) || set.contains(this))
			return false;
		/*if(l.production().priorities().deepConflicts(this, l.position()))
			return true;*/
		if(l.conflicts(this))
			return true;
		
		set.add(this);
		
		for(int i = size()-1; i >= 0; --i){
			Symbol s = symbol(i);
			
			if(s != null && s instanceof NonTerminal){
				NonTerminal n = (NonTerminal)s;
				
				for(Production p : n.productions()){
					SyntaxProduction sp = p.syntaxProduction();
					
					if(!directConflicts(sp, i) && sp.doPotentialRightDeepConflict(l, set)){
						return true;
					}
				}
				
				if(n.nonEpsilon())
					break;
			}else{
				break;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this)
			return true;
		if(o != null && o instanceof SyntaxProduction){
			SyntaxProduction p = (SyntaxProduction)o;
			if(p._symbol.equals(_symbol) && p._rhs.size() == _rhs.size() && p._attributes.size() == _attributes.size()){
				for(int i = 0; i < _rhs.size(); ++i){
					if(!_rhs.get(i).equals(p._rhs.get(i)))
						return false;
				}

				/*for(int i = 0; i < _attributes.size(); ++i){
					if(!_attributes.get(i).equals(p._attributes.get(i)))
						return false;
				}*/
				_attributes.equals(p._attributes);
				
				return true;
			}
		}
		return false;
	}
	
	public boolean merge(SyntaxProduction p){
		if(p._symbol.equals(_symbol) && p._rhs.size() == _rhs.size()){
			for(int i = 0; i < _rhs.size(); ++i){
				if(!_rhs.get(i).equals(p._rhs.get(i)))
					return false;
			}
			
			_attributes.addAll(p.attributes());
			
			return true;
		}
		
		return false;
	}
	
	public ContextualProduction contextualize(ContextualSymbol cs, boolean conflicts_left, boolean conflicts_right, Set<PriorityLevel> prio_left, Set<PriorityLevel> prio_right, int l, int r) throws UndefinedSymbolException{
		List<Symbol> rhs = new ArrayList<>();
		boolean contains_non_layout_symbol = false;
		boolean inside_layout = _symbol.isLayout();
		
		for(int i = 0; i < size(); ++i){
			Symbol s = symbol(i);
			
			if(cs.filter() == ContextualSymbol.Filter.LAYOUT_ONLY && !s.isEpsilon()){
				return null;
			}
			
			if(s instanceof CharClass){ // A char class removes all contexts.
				rhs.add(s);
				contains_non_layout_symbol = true;
			}else{
				NonTerminal nt = (NonTerminal)s.nonContextual();
				NonTerminal symbol; // new symbol
				
				if(i <= l || i >= r){
					Context left = null, right = null; // new symbol contexts
					ContextualSymbol.Filter filter = ContextualSymbol.Filter.LAYOUT_ONLY;
					
					if(i == l && i == r){
						left = cs.leftContext().union(prio_right);
						right = cs.rightContext().union(prio_left);
					}else if(i <= l){
						left = new Context(cs.leftContext());
						right = new Context(prio_left);
					}else{ //if(i >= r)
						left = new Context(prio_right);
						right = new Context(cs.rightContext());
					}
					
					left.leftSimplify(this, nt, i);
					right.rightSimplify(this, nt, i);
					
					if(i == l || i == r){
						filter = ContextualSymbol.Filter.REJECT_LAYOUT;
						contains_non_layout_symbol = true;
						
						if(inside_layout || ((left == null || left.isEmpty()) && ( right == null || right.isEmpty()))){
							filter = ContextualSymbol.Filter.NONE;
						}
					}
					
					symbol = ContextualSymbol.unique(left, nt, right, filter);
				}else{
					symbol = ContextualSymbol.unique(null, (NonTerminal)s.nonContextual(), null, ContextualSymbol.Filter.NONE);
				}
				
				if(symbol == null) // This production is not possible
					return null;
				
				rhs.add(symbol);
			}
		} // ~ symbol iteration
		
		if(!contains_non_layout_symbol && cs.filter() == ContextualSymbol.Filter.REJECT_LAYOUT){ // Avoid layout/empty productions if filter = REJECT_LAYOUT
			next_rl_production:
			for(int i = 0; i < size(); ++i){
				List<Symbol> rhs_rl = new ArrayList<>();
				for(int j = 0; j < size(); ++j){
					if(i == j){
						NonTerminal s = ContextualSymbol.unique(null, (NonTerminal)rhs.get(j), null, ContextualSymbol.Filter.REJECT_LAYOUT);
						if(s == null){
							continue next_rl_production;
						}
						rhs_rl.add(s);
					}else{
						rhs_rl.add(rhs.get(j));
					}
				}
				return ContextualProduction.unique(this, cs, rhs_rl);
			}
		}
			
		return ContextualProduction.unique(this, cs, rhs);
	}
	
	@Override
	public void contextualize(ContextualSymbol cs, Set<ContextualProduction> set) throws UndefinedSymbolException{
		boolean conflicts_left = cs.leftContext().conflictsLeft(this);
		boolean conflicts_right = cs.rightContext().conflictsRight(this);
		boolean inside_layout = _symbol.isLayout();
		boolean contains_terminal = false;
		
		if(isEmpty()){
			if(cs.filter() != ContextualSymbol.Filter.REJECT_LAYOUT){
				ContextualProduction cp = ContextualProduction.unique(this, cs, symbols());
				if(cp != null){
					cp.addDependant(cs);
					set.add(cp);
				}
			}
		}else{
			for(int l = 0; l < size(); ++l){
				Symbol sym_left = symbol(l);
				if(sym_left.type().level() < _symbol.type().level())
					contains_terminal = true;
				
				if(!conflicts_left || ((!sym_left.isLayout() || inside_layout) && sym_left.type().level() < _symbol.type().level())){
					Set<PriorityLevel> prio_left = priorities().priorityLevels(l);
					
					for(int r = size()-1; r >= l; --r){
						Symbol sym_right = symbol(r);
						if(sym_right.type().level() < _symbol.type().level())
							contains_terminal = true;
						
						if(!conflicts_right || sym_right.type().level() < _symbol.type().level()){
							Set<PriorityLevel> prio_right = priorities().priorityLevels(r);
							
							ContextualProduction cp = contextualize(cs, conflicts_left, conflicts_right, prio_left, prio_right, l, r);
							if(cp != null){
								cp.addDependant(cs);
								set.add(cp);
							}
						}
					
						if(cs.rightContext().isEmpty() || ((!sym_right.isLayout() || inside_layout) && sym_right.nonEpsilon()))
							break;
						
						/*if(inside_layout || (!layout_only && (_right.isEmpty() || sym_right.nonEpsilon())))
							break;*/
					}
				}
				
				if(cs.leftContext().isEmpty() || sym_left.nonEpsilon())
					break;
			}
			
			// Add the full layout production if necessary
			if(!contains_terminal && cs.filter() != ContextualSymbol.Filter.REJECT_LAYOUT && !cs.leftContext().isEmpty() && !cs.rightContext().isEmpty()){
				List<Symbol> rhs = new ArrayList<>();
				
				for(int i = 0; i < size(); ++i){
					Symbol s = symbol(i);
					NonTerminal nt = (NonTerminal)s.nonContextual();
					
					Set<PriorityLevel> prio_left = priorities().priorityLevels(i);
					Set<PriorityLevel> prio_right = priorities().priorityLevels(i);
					
					Context left = cs.leftContext().union(prio_right);
					Context right = cs.rightContext().union(prio_left);
					
					left.leftSimplify(this, nt, i);
					right.rightSimplify(this, nt, i);
					
					Symbol symbol = ContextualSymbol.unique(left, (NonTerminal)s.nonContextual(), right, ContextualSymbol.Filter.LAYOUT_ONLY);
					
					if(symbol == null) // This production is not possible
						return;
					
					rhs.add(symbol);
				}
				
				ContextualProduction cp = ContextualProduction.unique(this, cs, rhs);
				cp.addDependant(cs);
				set.add(cp);
			}
		}
	}
	
	public String shortString(){
		if(_cons != null && !_cons.isEmpty())
			return _symbol.toString()+"."+_cons;
		return toString();
	}
	
	@Override
	public String toString(){
		if(_str == null){
			_str = "";
			for(Symbol s : _rhs)
				_str += s.toString()+" ";
			_str += "→ "+_symbol.toString();
			if(_cons != null && !_cons.isEmpty())
				_str += "."+_cons;
			if(!_attributes.isEmpty()){
				int i = 0;
				_str += " {";
				for(Attribute attr : _attributes){
					if(i > 0)
						_str += ",";
					_str += attr.name();
					++i;
				}
				_str += "}";
			}
		}
		
		return _str;
	}
	
	@Override
    public int hashCode() {
        return toString().hashCode();
    }
	
	public IStrategoTerm toATerm(int id){
		IStrategoTerm[] list = new IStrategoTerm[_rhs.size()];
		for(int i = 0; i < _rhs.size(); ++i)
			list[i] = _rhs.get(i).toATerm();
		
		IStrategoTerm attrs = null;
		if(_attributes.isEmpty() && (_cons == null || _cons.isEmpty())){
			attrs = new StrategoAppl(CONS_NO_ATTRS, new IStrategoTerm[]{}, null, 0);
		}else{
			IStrategoTerm[] attr_list = new IStrategoTerm[_attributes.size()+1];
			
			int i = 0;
			for(Attribute attr : _attributes){
				attr_list[i] = Exporter.exportAttribute(attr);
				++i;
			}
			
			if(_cons != null && !_cons.isEmpty()){
				attr_list[attr_list.length-1] = new StrategoAppl(
						CONS_TERM,
						new IStrategoTerm[]{
								new StrategoAppl(
										CONS_CONS,
										new IStrategoTerm[]{new StrategoString(_cons, null, 0)},
										null,
										0)
						},
						null,
						0);
			}
			
			attrs = new StrategoAppl(CONS_ATTRS, new IStrategoTerm[]{Utilities.strategoListFromArray(attr_list)}, null, 0);
		}
		
		return new StrategoAppl(
				CONS_LABEL,
				new IStrategoTerm[]{
						new StrategoAppl(
							CONS_PROD,
							new IStrategoTerm[]{
									Utilities.strategoListFromArray(list),
									_symbol.toATerm(),
									attrs
							},
							null,
							0
						),
						new StrategoInt(id, null, 0)
				},
				null,
				0
				);
	}
}
