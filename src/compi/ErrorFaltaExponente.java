package compi;

public class ErrorFaltaExponente extends AccionSemantica{
    @Override
    public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
        buffer.deleteCharAt(buffer.length()-1);
        buffer.append('1');
        pos[0]--;
        System.out.println("ERROR: Falta el exponente en linea " +Parser.nLinea);
        Lexico.erroresL++;
        return Parser.CTE_SINGLE;
    }
}
