package compi;

public class ErrorSobraExponente extends AccionSemantica{
    @Override
    public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
        System.out.println("ERROR: Sobra la \"S\" del exponente en linea " +Parser.nLinea);
        Lexico.erroresL++;
        buffer.deleteCharAt(buffer.length()-1);
        pos[0]--;
        return Parser.CTE_SINGLE;
    }
}
