package controller.parser;

public class TokenOperacion extends Token{
	
	public enum opName {
		round, floor, abs, sqrt, add, subs, mult, div, mod, pow, inc, dec, 
			binToDec, hexToDec, octToDec, toBin, toHex, toOct, 
			convertir,
			media, mediana, varianza, covarianza, desviacion, pearson,
			extractDay, extractMonth, extractYear, weekDay, newDate,
			replace, size
	}
	
	public enum typeClaseOp {
		numerica, conversion, estadistica, monetaria, fecha, textual
	}
	
	public opName operation;
	public Integer typeOp;
	public Integer numArguments;
	public typeClaseOp tipoClase;
	
	
	
	public void initializeOp() {
		if (operation == null) return;
		
		//type1: 1 lista argumentos + 1 lista resultado mismo tama�o (intersecci�n puede ser no vac�a)
		//type2: 1 lista argumentos + 1 lista resultado mismo tama�o (intersecci�n tiene que ser vac�a)
		//type3: 1 lista argumentos + 1 sola celda resultado
		//type4: 1 lista argumentos + 1 argumento a parte + 1 lista resultado mismo tama�o lista inicial
		//type5: 2 listas de argumentos de igual tama�o + 1 sola celda resultado
		//type6: 1 lista de argumentos + 2 argumentos + 1 lista resultado mismo tama�o lista inicial
		final opName[][] types = {{opName.floor, opName.abs, opName.binToDec, 
									opName.hexToDec, opName.octToDec, opName.toBin, opName.toHex, opName.toOct,
									opName.extractDay, opName.extractMonth, opName.extractYear, opName.weekDay},	//type1			
								{opName.sqrt, opName.inc, opName.dec, opName.size},					//type2
								{opName.add, opName.media, opName.mediana, opName.varianza, opName.desviacion},	//type3									
								{opName.round, opName.subs, opName.mult, opName.div, opName.mod, opName.pow, opName.newDate},			//type4
								{opName.covarianza, opName.pearson},			//type5
								{opName.convertir, opName.replace}};								//type6			
		
		
		
		//calcular variable typeOp
		typeOp = 0;
		for (int i = 0; i < types.length && typeOp == 0; ++i) {
			for (int j = 0; j < types[i].length && typeOp == 0; ++j) {
				if (types[i][j] == operation) typeOp = i + 1;
			}
		}
		
		
		//calcular variable numArgumentos
		if (typeOp == 1 || typeOp == 2 || typeOp == 3) numArguments = 1;
		else if (typeOp == 4 || typeOp == 5) numArguments = 2;
		else if (typeOp == 6) numArguments = 3;
		
		
		
		final opName[][] classDivision = {{opName.round, opName.floor, opName.abs, opName.sqrt, opName.add, opName.subs,
			opName.mult, opName.div, opName.mod, opName.pow, opName.inc, opName.dec}, 		//numerica
			{opName.binToDec, opName.hexToDec, opName.octToDec, opName.toBin, opName.toHex, opName.toOct},		//conversion
			{opName.media, opName.mediana, opName.varianza, opName.covarianza, opName.desviacion, opName.pearson},		//estadistica
			{opName.convertir},			//monetaria
			{opName.extractDay, opName.extractMonth, opName.extractYear, opName.weekDay, opName.newDate},		//fecha
			{opName.replace, opName.size}};		//textual
		
		final typeClaseOp[] arrayTiposClase = {typeClaseOp.numerica, typeClaseOp.conversion, typeClaseOp.estadistica, typeClaseOp.monetaria, 
											typeClaseOp.fecha, typeClaseOp.textual};
		
		//calcular variable tipoClase
		for (int i = 0; i < classDivision.length && tipoClase == null; ++i) {
			for (int j = 0; j < classDivision[i].length && tipoClase == null; ++j) {
				if (classDivision[i][j] == operation) tipoClase = arrayTiposClase[i];
			}
		}
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof TokenOperacion)) return false;
		TokenOperacion T = (TokenOperacion) obj;
		
		if (T.type == null) {
			if (this.type != null) return false;
		}
		else if (this.type != T.type) return false;
		
		if (T.operation == null) {
			if (this.operation != null) return false;
		}
		else if (this.operation != T.operation) return false;
		
		if (T.typeOp == null) {
			if (this.typeOp != null) return false;
		}
		else if (! this.typeOp.equals(T.typeOp)) return false;
		
		if (T.numArguments == null) {
			if (this.numArguments != null) return false;
		}
		else if (! this.numArguments.equals(this.numArguments)) return false;
		
		if (T.tipoClase == null) {
			if (this.tipoClase != null) return false;
		}
		else if (this.tipoClase != T.tipoClase) return false;
		
		return true;
	}
}
