package compi;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;

import static java.lang.Boolean.FALSE;


public class Lexico {
	

	private int pos[] = {0};
	private char actual;
	public StringBuffer buffer = new StringBuffer();
	public static int erroresL = 0;
	private AccionSemantica accion;
	private int token;
	private boolean[] lex = {FALSE};
	private AgregarCaracter AC = new AgregarCaracter();
	private ContarNewLine CNL = new ContarNewLine();
	private InicializarBuffer IB= new InicializarBuffer();
	private VerificarAsterisco A= new VerificarAsterisco();
	private VerificarComparador VC= new VerificarComparador();
	private VerificarKeyWord VK= new VerificarKeyWord();
	private VerificarLargoID VL= new VerificarLargoID();
	private VerificarRangoLong VRL= new VerificarRangoLong();
	private VerificarRangoSingle VS = new VerificarRangoSingle();
	private ComenzarCadena CC = new ComenzarCadena();

	private ErrorCadenaMultilinea ECM = new ErrorCadenaMultilinea();
	private ErrorCaracterInvalido ECI = new ErrorCaracterInvalido();
	private ErrorComparacion EC = new ErrorComparacion();
	private ErrorFaltaExponente EFE = new ErrorFaltaExponente();
	private ErrorSobraCaracter ESC = new ErrorSobraCaracter();
	private ErrorSobraExponente ESE = new ErrorSobraExponente();
	private ErrorSumaMultilinea ESM = new ErrorSumaMultilinea();
	
	private String codigo;
	
	private String path;
											//   0	  1	   2    3    4    5    6    7    8    9    10   11	 12	  13   14   15   16   17   18   19   20   21   22   23   24
	private AccionSemantica [][] matAcciones={  {null,null,CNL, IB,  IB,  IB,  IB  ,IB  ,IB  ,IB  ,IB  ,IB  ,IB  ,IB,  IB,  null,IB,  CC,  IB,  IB,  IB,  IB,  IB,  ECI, null},
												{VK,  VK,  VK,  VL,  VL,  VL,  VK,  VK,  VK,  VK,  VK,  VK,  VK,  VK,  VK,  VK,  VL,  VK,  VK,  VK,  VK,  VK,  VK,  VK,  null},
												{VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  null},
												{VRL, VRL, VRL, VRL, VRL, AC,  VRL, VRL, VRL, VRL, VRL, VRL, VRL, AC,  VRL, VRL, VRL, VRL, VRL, VRL, VRL, VRL, VRL, VRL, null},
												{ESC, ESC, ESC, ESC, ESC, AC,  ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC, ESC},
												{VS,  VS,  VS,  VS,  VS,  AC,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  AC,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  null},
												{ESE, ESE, ESE, ESE, ESE, AC,  AC,  ESE, AC,  ESE, ESE, ESE, ESE, ESE, ESE, ESE, ESE, ESE, ESE, ESE, ESE, ESE, ESE, ESE, null},
												{EFE, EFE, EFE, EFE, EFE, AC,  EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, EFE, null},
												{VS,  VS,  VS,  VS,  VS,  AC,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  VS,  null},
												{A,   A,   A,   A,   A,   A,   A,   A,   A,   A,   A,   A,   A,   A,   A,   null,A,   A,   A,   A,   A,   A,   A,   A,   null},
												{null,null,CNL, null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null},
												{AC,  AC,  ECM, AC,  AC,  AC,  AC,  AC,  null,AC,  AC,  AC,  AC,  AC,  AC,  AC,  AC,  null,AC,  AC,  AC,  AC,  AC,  AC,  null},
												{null,AC,  CNL, AC,  AC,  AC,  AC,  AC,  AC  ,AC,  AC,  AC,  AC,  AC,  AC,  AC,  AC,  AC,  AC,  AC,  AC,  AC,  AC,  AC,  null},
												{ESM, ESM, ESM, ESM, ESM, ESM, ESM, ESM, null,ESM, ESM, ESM, ESM, ESM, ESM, ESM, ESM, ESM, ESM, ESM, ESM, ESM, ESM, ESM, null},
												{VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  null},
												{VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  null},
												{VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  VC,  null},
												{EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  AC,  EC,  EC,  null},
												{EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  EC,  AC,  EC,  null,}};
	
	
	
	private int [][] matEstados = { {0 ,0 ,0 ,1 ,1 ,3 ,-1,-1,-1,-1,-1,-1,-1,4 ,2 ,9 ,1 ,11,14,16,15,17,18,0 ,0 },
									{-1,-1,-1,1 ,1 ,1 ,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1 ,-1,-1,-1,-1,-1,-1,-1,-1},
									{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
									{-1,-1,-1,-1,-1,3 ,-1,-1,-1,-1,-1,-1,-1,5 ,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
									{0 ,0 ,0 ,0 ,0 ,5 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,0 ,-1},
									{-1,-1,-1,-1,-1,5 ,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,6 ,-1,-1,-1,-1,-1,-1,-1,-1},
									{-1,-1,-1,-1,-1,8 ,7 ,-1,7 ,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
									{-1,-1,-1,-1,-1,8 ,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
									{-1,-1,-1,-1,-1,8 ,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
									{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,10,-1,-1,-1,-1,-1,-1,-1,-1,-1},
									{10,10,0 ,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,-1},
									{11,11,-1,11,11,11,11,11,12,11,11,11,11,11,11,11,11,-1,11,11,11,11,11,11,-1},
									{12,11,13,11,11,11,11,11,12,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,-1},
									{11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,11,-1},
									{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
									{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
									{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
									{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
									{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1}};
	//48 - 57 numero
	//97 - 122 minuscula
	//65 - 90 mayuscula


	
	public Lexico(String path) {
		// TODO Auto-generated constructor stub
		// Se inicializa la clase lexico con una ruta de un archivo y si existe se extrae el codigo
		this.path = path;
		try {
			this.codigo= new String(Files.readAllBytes(Paths.get(path)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		aniadirPalabrasReservadas();
	}
	
	public void aniadirPalabrasReservadas() {
		Hashtable<String,Object> hash = new Hashtable<String,Object>();
		hash.put("token",(int)Parser.IF);
		Parser.tSimbolos.put("IF", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.THEN);
		Parser.tSimbolos.put("THEN", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.ELSE);
		Parser.tSimbolos.put("ELSE", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.ENDIF);
		Parser.tSimbolos.put("ENDIF", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.PRINT);
		Parser.tSimbolos.put("PRINT", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.FUNC);
		Parser.tSimbolos.put("FUNC", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.RETURN);
		Parser.tSimbolos.put("RETURN", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.BEGIN);
		Parser.tSimbolos.put("BEGIN", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.END);
		Parser.tSimbolos.put("END", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.BREAK);
		Parser.tSimbolos.put("BREAK", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.REPEAT);
		Parser.tSimbolos.put("REPEAT", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.TYPEDEF);
		Parser.tSimbolos.put("TYPEDEF", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.POST);
		Parser.tSimbolos.put("POST", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.TRY);
		Parser.tSimbolos.put("TRY",(Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.CATCH);
		Parser.tSimbolos.put("CATCH", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.LONG);
		Parser.tSimbolos.put("LONG", (Hashtable<String, Object>) hash.clone());
		hash.clear();
		hash.put("token",(int)Parser.SINGLE);
		Parser.tSimbolos.put("SINGLE", (Hashtable<String, Object>) hash.clone());
		hash.clear();
	}

	public TokenLexema yylex() {
		//metodo encargado de la devolucion de tokens
		int estado = 0;
		token = 0;
		lex[0] = false;
		if (pos[0] < codigo.length()) {
			while (estado != -1 && pos[0]< codigo.length())
			{
				actual = codigo.charAt(pos[0]);
				accion = matAcciones[estado][getColumna(actual)];
				if (accion != null) {
					token = accion.accionar(buffer, actual, pos, lex);
				}
				estado= matEstados[estado][getColumna(actual)];
				if (pos[0]< codigo.length()) {
					pos[0]++;
				}
				else {
					break;
				}
			}
			if (lex[0]) {
				return new TokenLexema (token, buffer.toString());
			}
			return new TokenLexema(token,null);
		}
		return new TokenLexema(0,null);
	}
	
	
	
	
	public int getCantErrores() {
		return this.erroresL; 
	}
	
	
	private int getColumna(char c) {
		//indica a que columna corresponde el caracter actual
		switch (c){
			case ' ':
				return 0;
			case '\t':
				return 1;
			case '\n':
				return 2;
			case '_':
				return 4;
			case '-':
				return 6;
			case '/':
				return 7;
			case '+':
				return 8;
			case '(':
				return 9;
			case ')':
				return 10;
			case ';':
				return 11;
			case ',':
				return 12;
			case '.':
				return 13;
			case ':':
				return 14;
			case '*':
				return 15;
			case 'S':
				return 16;
			case '%':
				return 17;
			case '>':
				return 18;
			case '<' : return 19;
			case '=' : return 20;
			case '&' : return 21;
			case '|' : return 22;
			default:break;
		}
		int ascii = (int) c;
		if ((ascii > 96 && ascii < 123) || ((ascii > 64 && ascii < 83) || (ascii > 83 && ascii < 91)))
			return 3;
		if ((ascii > 47) && (ascii < 58))
			return 5;
		if (ascii != 13)
			return 23;
		return 0;
	}
	
}