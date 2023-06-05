package compi;

import java.util.Hashtable;

public class TokenLexema {

	private int token;
	private String lexema;
	
	public TokenLexema(int token,String lexema) {
		this.token=token;
		this.lexema = lexema;
	}
	
	public int getToken() {
		return this.token;
	}

	public Object getLexema(){
		return lexema;
	}

}
