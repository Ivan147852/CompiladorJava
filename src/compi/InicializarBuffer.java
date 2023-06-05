package compi;

public class InicializarBuffer extends AccionSemantica {

	//HECHO
	@Override
	public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex){
		buffer.delete(0, buffer.length());
		if (actual != '%')
			buffer.append(actual);
		return (int) actual;
	}
}
