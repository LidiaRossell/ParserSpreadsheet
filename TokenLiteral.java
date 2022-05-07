package controller.parser;

public class TokenLiteral extends Token{
	public enum literalType { numerico, textual, fecha };
	
	public literalType LType;

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof TokenLiteral)) return false;
		TokenLiteral T = (TokenLiteral) obj;
		
		if (T.type == null) {
			if (this.type != null) return false;
		}
		else if (this.type != T.type) return false;
		
		if (T.LType == null) {
			if (this.LType != null) return false;
		}
		else if (! this.LType.equals(T.LType)) return false;
		
		return true;
	}
}
