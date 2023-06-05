package compi;

public class ErrorCadenaMultilinea extends AccionSemantica{
    @Override
    public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
        buffer.deleteCharAt(buffer.length()-1);
        buffer.append("%");
        System.out.println("ERROR LEXICO: Falta cerrar la cadena en linea " +Parser.nLinea);
        Lexico.erroresL++;
        pos[0]--;
        return Parser.CADENA_MULTILINEA;
    }
}
