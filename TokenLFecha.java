package controller.parser;

public class TokenLFecha extends TokenLiteral{
	public Integer day, month, year;
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof TokenLFecha)) return false;
		TokenLFecha T = (TokenLFecha) obj;
		
		if (T.type == null) {
			if (this.type != null) return false;
		}
		else if (this.type != T.type) return false;
		
		if (T.LType == null) {
			if (this.LType != null) return false;
		}
		else if (! this.LType.equals(T.LType)) return false;
		
		if (T.day == null) {
			if (this.day != null) return false;
		}
		else if (! this.day.equals(T.day)) return false;
		
		if (T.month == null) {
			if (this.month != null) return false;
		}
		else if (! this.month.equals(T.month)) return false;
		
		if (T.year == null) {
			if (this.year != null) return false;;
		}
		else if (! this.year.equals(T.year)) return false;
		
		return true;
	}
}
