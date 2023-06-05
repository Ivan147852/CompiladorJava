package compi;

public class VerificarComparador extends AccionSemantica {

	//HECHO
	@Override
	public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
		buffer.append(actual);
		//lex[0]= true;
		switch (buffer.toString()) {
			case "<=":
				return Parser.COMP_MENOR_IGUAL;
			case "==":
				return Parser.COMP_IGUAL;
			case ">=":
				return Parser.COMP_MAYOR_IGUAL;
			case ":=":
				return Parser.ASSIGN;
			case "<>":
				return Parser.DISTINTO;
		}
		buffer.deleteCharAt(buffer.length()-1);
		pos[0]--;
		return buffer.charAt(0);
	}
}
