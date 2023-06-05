package compi;

public class ErrorComparacion extends AccionSemantica{
    @Override
    public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
        char comparador = buffer.charAt(buffer.length()-1);
        buffer.append(comparador);
        System.out.println("ERROR LEXICO: Falto un caracter de comparacion en linea " +Parser.nLinea);
        Lexico.erroresL++;
        pos[0]--;
        if (comparador == '&') {
            return Parser.AND;
        }
        return Parser.OR;
    }
}
