package compi;

public class AgregarCaracter extends AccionSemantica {

	//HECHA
	@Override
	public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
		buffer.append(actual);
		if (buffer.toString().equals("&&")){
			return Parser.AND;
		}
		if (buffer.toString().equals("||")) {
			return Parser.OR;
		}
		return Parser.CADENA_MULTILINEA;
	}
	
}
