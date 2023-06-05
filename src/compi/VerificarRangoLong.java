package compi;

import java.util.Hashtable;

public class VerificarRangoLong extends AccionSemantica {

	@Override
	public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
		Long aux = Long.parseLong(buffer.toString());
		if (aux-1 > 2147483647)
			System.out.println("WARNING: El número es demasiado grande o demasiado chico en linea " +Parser.nLinea);
		pos[0]--;
		lex[0]=true;
		return Parser.CTE_LONG;

	}
	
}
