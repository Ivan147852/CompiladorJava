package compi;

public class ErrorSumaMultilinea extends AccionSemantica{
    @Override
    public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
        System.out.println("ERROR: Falta el signo \"+\" para continuar la cadena multilinea en linea " +Parser.nLinea);
        Lexico.erroresL++;
        return 0;
    }
}
