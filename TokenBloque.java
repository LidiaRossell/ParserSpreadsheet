package controller.parser;
import java.util.ArrayList;

public class TokenBloque extends Token {
	public ArrayList<ArrayList<Integer>> listaCeldas;
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof TokenBloque)) return false;
		TokenBloque T = (TokenBloque) obj;
		
		if (T.type == null) {
			if (this.type != null) return false;
		}
		else if (this.type != T.type) return false;
		
		if (T.listaCeldas == null) {
			if (this.listaCeldas != null) return false;
		}
		else if (! this.listaCeldas.equals(T.listaCeldas)) return false;
		
		return true;
	}
}
