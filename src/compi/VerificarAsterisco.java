package compi;

public class VerificarAsterisco extends AccionSemantica {

	//HECHO
	@Override
	public int accionar(StringBuffer buffer, char actual, int[] pos, boolean[] lex) {
		//Se llama luego de un asterisco si no se inicia comentario, ya que representa el token del asterisco (multiplicacion)
		pos[0]--;
		char c = '*';
		return (int)c;
	}
	
}
