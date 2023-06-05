package compi;

import static java.lang.Boolean.TRUE;

public class VerificarKeyWord extends AccionSemantica {

	//HECHA
	@Override
	public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex){
		pos[0]--;
		String bufferValue = buffer.toString();
		int token = 0;
		if (Parser.tSimbolos.get(bufferValue)!=null) {
			token = (int) Parser.tSimbolos.get(bufferValue).get("token");
			if (token==Parser.ID) {
				lex[0]=TRUE;
				token= Parser.ID;
			}
		}
		else {
			if (actual == '%'){
				//Parser.tSimbolos.put(bufferValue,(int)Parser.CADENA_MULTILINEA);
			}
			else{
				token = Parser.ID;
				//Parser.tSimbolos.put(bufferValue,(int)Parser.ID);
			}
			lex[0]=TRUE;
		}
		lex[0]=true;
		return token;
	}
	
}
