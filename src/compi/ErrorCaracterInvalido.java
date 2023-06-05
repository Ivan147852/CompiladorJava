package compi;

public class ErrorCaracterInvalido extends AccionSemantica{
    @Override
    public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
        System.out.println("ERROR: Se leyo un caracter invalido en linea " +Parser.nLinea);
        Lexico.erroresL++;
        return 0;
    }
}
