package controller.parser;

public class TokenLTextual extends TokenLiteral{
	public String contenido;
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof TokenLTextual)) return false;
		TokenLTextual T = (TokenLTextual) obj;
		
		if (T.type == null) {
			if (this.type != null) return false;
		}
		else if (this.type != T.type) return false;
		
		if (T.LType == null) {
			if (this.LType != null) return false;
		}
		else if (! this.LType.equals(T.LType)) return false;
		
		if (T.contenido == null) {
			if (this.contenido != null) return false;
		}
		else if (! this.contenido.equals(T.contenido)) return false;
		
		
		return true;
	}
}
