package compi;

public class ComenzarCadena extends AccionSemantica{
    @Override
    public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
        buffer.delete(0,buffer.length());
        lex[0] = true;
        return 0;
    }
}
