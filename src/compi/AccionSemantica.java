package compi;

public abstract class AccionSemantica {
	public abstract int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex);
}
