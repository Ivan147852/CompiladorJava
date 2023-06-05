package compi;

public class VerificarRangoSingle extends AccionSemantica {

	//HECHO
	private Double max = 3.40282347*Math.pow(10, 38);
	private Double min = 1.17549435*Math.pow(10, -38);

	@Override
	public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
		int posS = buffer.indexOf("S");
		double aux;
		lex[0]=true;
		if (posS > -1) {
			String exponente = (buffer.substring(posS+1, buffer.length()));
			String flotante = (buffer.substring(0, posS));
			aux = Double.parseDouble(flotante);
			int exp = Integer.parseInt(exponente);
			aux = aux*Math.pow(10, exp);
		}
		else {
			aux = Double.parseDouble(buffer.toString());
		}
		pos[0]--;
		if ((aux < max && aux > min) || aux == 0 || (aux > -max && aux < -min)) {
			//Si cumple, esta en rango y se devuelve el token
			buffer.delete(0, buffer.length());
			buffer.append(aux);
			return Parser.CTE_SINGLE;
		}
		//si no cumple, se alerta pero de igual forma se devuelve un token de float con un lexema por defecto para seguir procesando el codigo
		System.out.println("Error lexico constante flotante fuera de rango cerca de linea: "  + Parser.nLinea);
		Lexico.erroresL++;
		//TRATAR EL ERROR
		return Parser.CTE_SINGLE;
	}
	
}
