package controller.parser;

public class TokenLNumerico extends TokenLiteral{
	public double contenido;
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof TokenLNumerico)) return false;
		TokenLNumerico T = (TokenLNumerico) obj;
		
		if (T.type == null) {
			if (this.type != null) return false;
		}
		else if (this.type != T.type) return false;
		
		if (T.LType == null) {
			if (this.LType != null) return false;
		}
		else if (! this.LType.equals(T.LType)) return false;
		
		if (T.contenido != this.contenido) return false;
		
		return true;
	}
}
