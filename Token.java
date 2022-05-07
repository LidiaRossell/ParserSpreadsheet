package controller.parser;

public class Token {

	public enum typeToken {
			operacion, bloque, literal, exception
		}

	
	public typeToken type;

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Token)) return false;
		Token T = (Token) obj;
		if (T.type == null) return (this.type == null);
		return this.type == T.type;
	}
	
}
