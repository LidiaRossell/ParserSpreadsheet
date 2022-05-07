package controller.parser;
import java.util.*;

public class OpTree {
	public Token node;
	public ArrayList<ArrayList<OpTree>> hijos;
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof OpTree)) return false;
		OpTree T = (OpTree) obj;
		if (T.node == null) {
			if (this.node != null) return false;
		}
		else if (! this.node.equals(T.node)) return false;
		if (T.hijos == null) {
			if (this.hijos != null) return false;
		}
		else if (! this.hijos.equals(T.hijos)) return false;
		
		return true;
	}
}
