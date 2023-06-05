package compi;

import static java.lang.Boolean.TRUE;

public class VerificarLargoID extends AccionSemantica {

	//HECHO
	@Override
	public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
		buffer.append(actual);
		if (buffer.length() == 23){
			System.out.println("WARNING: El ID es muy largo en linea " +Parser.nLinea);
			buffer.delete(buffer.length()-1, buffer.length());
		}
		lex[0]=TRUE;
		return Parser.ID;
	}
	
}
