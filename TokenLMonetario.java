package controller.parser;

public class TokenLMonetario extends TokenLNumerico{
	public TipoMoneda moneda;
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof TokenLMonetario)) return false;
		TokenLMonetario T = (TokenLMonetario) obj;
		
		if (T.type == null) {
			if (this.type != null) return false;
		}
		else if (this.type != T.type) return false;
		
		if (T.LType == null) {
			if (this.LType != null) return false;
		}
		else if (! this.LType.equals(T.LType)) return false;
		
		if (T.contenido != this.contenido) return false;
		
		if (T.moneda == null) {
			if (this.moneda != null) return false;
		}
		else if (this.moneda != T.moneda) return false;
		
		return true;
	}
}
