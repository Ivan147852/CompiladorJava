package compi;

public class ErrorSobraCaracter extends AccionSemantica{
    @Override
    public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
        System.out.println("ERROR: Hay un caracter sobrante en linea " +Parser.nLinea);
        Lexico.erroresL++;
        buffer.deleteCharAt(buffer.length()-1);
        pos[0]--;
        return 0;
    }
}
