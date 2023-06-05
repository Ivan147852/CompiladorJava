package compi;

public class ContarNewLine extends AccionSemantica {

	//HECHO PERO VER QUE ONDA EL CANTLINEAS
	@Override
	public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
		Parser.nLinea++;
		return 0;
	}
	
}
