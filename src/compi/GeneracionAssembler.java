package compi;

import javax.net.ssl.SSLEngineResult;
import java.lang.reflect.Array;
import java.util.*;

public class GeneracionAssembler {

    //Estructura auxiliar que es una copia de la tabla de simbolos original, para poder iterar, ya que en la original iriamos agregando cosas
    private Hashtable<String, Hashtable<String, Object>> tSimbolos = new Hashtable<>();
    //Estructura que nos indica en que posicion se tendrían que crear las etiquetas
    private HashSet<Integer> labels=new HashSet<>();
    //Estructura que nos indica las variables auxiliar que no fueron añadidas al .data ya que se añadieron en la tabla de simbolos luego de la generación del código
    private Hashtable<String,Hashtable<String,Object>> nuevasVariables = new Hashtable<>();
    //Variables del assembler para poder devolver el parametro en las funciones
    private String variableRetornoLong = "@var_ret_long";
    private String variableRetornoSingle = "@var_ret_single";
    private String variablePostcondicion = "@var_postcondicion";
    //Variables auxiliares
    private String variableAuxiliar;
    private String variableAuxiliarSiguiente;
    private String comparadorAuxiliar;
    //Strings que son el codigo assembler que se copiara en un archivo asm
    private String label="";
    private String declaraciones = "";
    private String funciones = "";
    //Variable para saber si estamos en la etapa de .code o .data
    private boolean cargando = false;
    private boolean post = false;
    //Indices que nos indican que cantidad de variables auxiliares creamos, tanto comparativas, como variables, como de etiquetas
    private int indiceVariable = 0;
    private int indiceComparacion = 0;
    private int indiceVerificar = 1;
    private int resta = 0;
    private String operadorPostcondicion = null;

    public GeneracionAssembler()
    {
        ;
    }

    //Método que clona la tabla de símbolos original y lo guarda en tSimbolos local
    public void clonarTablaSimbolos()
    {
        cargarMensajesDeError();
        for (Map.Entry<String, Hashtable<String, Object>> entry : Parser.tSimbolos.entrySet()) {
            Hashtable<String,Object> hashAux = new Hashtable<>();
            hashAux = entry.getValue();
            tSimbolos.put(entry.getKey(),hashAux);
        }
    }

    //Mensajes fijos de error que se mostrarán por pantalla en caso de haber un error
    public void cargarMensajesDeError()
    {
        Parser.tSimbolos.put("msjDivisionPorCero", new Hashtable<>());
        Parser.tSimbolos.get("msjDivisionPorCero").put("definida","true");
        Parser.getTS("msjDivisionPorCero").put("uso","mensaje");
        Parser.getTS("msjDivisionPorCero").put("valor","error de division por cero");
        Parser.tSimbolos.put("msjOverflowSuma", new Hashtable<>());
        Parser.tSimbolos.get("msjOverflowSuma").put("definida","true");
        Parser.getTS("msjOverflowSuma").put("uso","mensaje");
        Parser.getTS("msjOverflowSuma").put("valor","error de overflow de suma");
        Parser.tSimbolos.put("msjRecursionMutua", new Hashtable<>());
        Parser.tSimbolos.get("msjRecursionMutua").put("definida","true");
        Parser.getTS("msjRecursionMutua").put("uso","mensaje");
        Parser.getTS("msjRecursionMutua").put("valor","error de recursion mutua");
        Parser.tSimbolos.put("msjNoVerificar", new Hashtable<>());
        Parser.tSimbolos.get("msjNoVerificar").put("definida","true");
        Parser.getTS("msjNoVerificar").put("uso","mensaje");
        Parser.getTS("msjNoVerificar").put("valor","no hay que verificar la recursion ya que no es un caso posible");
    }

    //Método donde se genera el código assembler
    public String polacaToAssembler(ArrayList<String> polaca)
    {
        String res = "";
        Parser.tSimbolos.put("@var_ret_long",new Hashtable<>());
        Parser.tSimbolos.get("@var_ret_long").put("definida","true");
        Parser.getTS("@var_ret_long").put("uso","variable");
        Parser.getTS("@var_ret_long").put("tipo","LONG");
        Parser.tSimbolos.put("@var_ret_single",new Hashtable<>());
        Parser.tSimbolos.get("@var_ret_single").put("definida","true");
        Parser.getTS("@var_ret_single").put("uso","variable");
        Parser.getTS("@var_ret_single").put("tipo","SINGLE");
        Parser.tSimbolos.put("@var_postcondicion",new Hashtable<>());
        Parser.tSimbolos.get("@var_postcondicion").put("definida","true");
        Parser.getTS("@var_postcondicion").put("uso","comparador");
        Parser.getTS("@var_postcondicion").put("tipo","boolean");
        Parser.tSimbolos.put("@FP_MAX",new Hashtable<>());
        Parser.tSimbolos.get("@FP_MAX").put("definida","true");
        Parser.getTS("@FP_MAX").put("uso","constante");
        Parser.getTS("@FP_MAX").put("tipo","FINAL");
        Parser.getTS("@FP_MAX").put("valor",Double.toString(3.40282347*Math.pow(10, 38)));
        //Se recorre la polaca desde la posicion 2 ya que los primeros 2 valores no corresponden a una palabra clave
        for(int i = 1; i<polaca.size(); i++)
        {
            //Guardamos los operadores
            String operador = polaca.get(i);
            String operando1 = polaca.get(i-1);
            String operando2 = null;
            if (i > 1)
                operando2 = polaca.get(i-2);

            //Si nos encontramos en una linea donde debería haber una etiqueta, la agregamos
            if (labels.contains(i+resta)) {
            	res+="label"+(i+resta)+":\n";
            	labels.remove(i);
            }
            switch(operador){
                case ":=" :
                    verificarTipo(operando1,operando2);
                    //Si la variable es de tipo entero, usamos registros
                    if (getTipo(operando2).equals("LONG"))
                    {
                        if (Parser.getTS(operando2).get("uso").equals("funcion"))
                            res += "MOV EAX, "+quitarAmbito(operando2)+"\n";
                        else
                            res += "MOV EAX,"+operando2+"\n";
                        res += "MOV "+operando1+", EAX\n";
                    }
                    //Sino usamos el coprocesador
                    else
                    {
                        if (getTipo(operando1).equals("SINGLE"))
                        {
                            if (Parser.getTS(operando2).get("uso").equals("constante"))
                            {
                                res += "FLD @"+getParteEntera(operando2)+"f"+getParteDecimal(operando2)+"\n";
                                res += "FSTP "+ operando1+"\n";
                                //res += "FSTP @"+getParteEntera(operando2)+"f"+getParteDecimal(operando2)+"\n";
                            }
                            else
                            {
                                res += "FLD "+operando2+"\n";
                                res += "FSTP "+ operando1+"\n";
                                //res += "FSTP "+ operando2+"\n";
                            }
                        }
                    }
                    break;
                case "+" :
                    variableAuxiliar = "@aux"+indiceVariable;
                    //Si las variables son de tipo entero usamos registros
                    if (getTipo(operando1).equals("LONG"))
                    {
                        res += "MOV EAX,"+operando2+"\n";
                        res += "ADD EAX,"+operando1+"\n";
                        //Verificamos el overflow de la suma
                        res += "JO overflow_suma\n";
                        res += "MOV "+variableAuxiliar+", EAX"+"\n";
                    }
                    //Sino usamos el coprocesador
                    else
                    {
                        //En caso de que la variable sea constante, creamos una variable auxiliar
                        if (Parser.getTS(operando2).get("uso").equals("constante"))
                        {
                            res += "FLD @"+getParteEntera(operando2)+"f"+getParteDecimal(operando2)+"\n";
                        }
                        else
                        {
                            res += "FLD "+operando2+"\n";
                        }
                        //En caso de que la variable sea constante, creamos una variable auxiliar
                        if (Parser.getTS(operando2).get("uso").equals("constante"))
                        {
                            res += "FADD @"+getParteEntera(operando1)+"f"+getParteDecimal(operando1)+"\n";
                        }
                        else
                        {
                            res += "FADD "+operando1+"\n";
                        }
                        //Verificamos el overflow de la suma
                        /*res += "FCOM @FP_MAX\n";
                        res += "FSTSW AX\n";
                        res += "SAHF\n";
                        res += "JA overflow_suma\n";*/
                        res += "FSTP "+variableAuxiliar+"\n";
                    }
                    //Si estamos en la etapa del .data (que se hace al final), cargamos las nuevas variables auxiliares al .data
                    if (cargando)
                    {
                        cargarVariableTabla(nuevasVariables,operando1,operando2,polaca,i);
                    }
                    //Sino, las cargamos en las tabla de simbolos y luego se añadiran al .data en la etapa posterior
                    else
                    {
                        cargarVariableTabla(tSimbolos,operando1,operando2,polaca,i);
                    }
                    polaca.remove(i-1); polaca.remove(i-2);
                    i -= 2;
                    resta+=2;
                    break;
                case "-" :
                    variableAuxiliar = "@aux"+indiceVariable;
                    //Si las variables son de tipo entero usamos registros
                    if (getTipo(operando1).equals("LONG"))
                    {
                        res += "MOV EAX,"+operando2+"\n";
                        res += "SUB EAX,"+operando1+"\n";
                        res += "JO overflow_suma\n";
                        res += "MOV "+variableAuxiliar+", EAX"+"\n";
                    }
                    //Sino usamos el coprocesador
                    else
                    {
                        //En caso de que la variable sea constante, creamos una variable auxiliar
                        if (Parser.getTS(operando2).get("uso").equals("constante"))
                        {
                            res += "FLD @"+getParteEntera(operando2)+"f"+getParteDecimal(operando2)+"\n";
                        }
                        else
                        {
                            res += "FLD "+operando2+"\n";
                        }
                        //En caso de que la variable sea constante, creamos una variable auxiliar
                        if (Parser.getTS(operando1).get("uso").equals("constante"))
                        {
                            res += "FSUB @"+getParteEntera(operando1)+"f"+getParteDecimal(operando1)+"\n";
                        }
                        else
                        {
                            res += "FSUB "+operando1+"\n";
                        }
                        res += "FSTP "+variableAuxiliar+"\n";
                    }
                    //Si estamos en la etapa del .data (que se hace al final), cargamos las nuevas variables auxiliares al .data
                    if (cargando)
                    {
                        cargarVariableTabla(nuevasVariables,operando1,operando2,polaca,i);
                    }
                    //Sino, las cargamos en las tabla de simbolos y luego se añadiran al .data en la etapa posterior
                    else
                    {
                        cargarVariableTabla(tSimbolos,operando1,operando2,polaca,i);
                    }
                    polaca.remove(i-1); polaca.remove(i-2);
                    i -= 2;
                    resta+=2;
                    break;
                case "*" :
                    //Si las variables son de tipo entero usamos registros
                    variableAuxiliar = "@aux"+indiceVariable;
                    if (getTipo(operando1).equals("LONG"))
                    {
                        res += "MOV EAX, "+operando2+"\n";
                        res += "IMUL EAX, "+operando1+"\n";
                        res += "MOV "+variableAuxiliar+", EAX"+"\n";
                    }
                    //Sino usamos el coprocesador
                    else
                    {
                        //En caso de que la variable sea constante, creamos una variable auxiliar
                        if (Parser.getTS(operando2).get("uso").equals("constante"))
                        {
                            res += "FLD @"+getParteEntera(operando2)+"f"+getParteDecimal(operando2)+"\n";
                        }
                        else
                        {
                            res += "FLD "+operando2+"\n";
                        }
                        //En caso de que la variable sea constante, creamos una variable auxiliar
                        if (Parser.getTS(operando1).get("uso").equals("constante"))
                        {
                            res += "FMUL @"+getParteEntera(operando1)+"f"+getParteDecimal(operando1)+"\n";
                        }
                        else
                        {
                            res += "FMUL "+operando1+"\n";
                        }
                        res += "FSTP "+variableAuxiliar+"\n";
                    }
                    //Si estamos en la etapa del .data (que se hace al final), cargamos las nuevas variables auxiliares al .data
                    if (cargando)
                    {
                        cargarVariableTabla(nuevasVariables,operando1,operando2,polaca,i);
                    }
                    //Sino, las cargamos en las tabla de simbolos y luego se añadiran al .data en la etapa posterior
                    else
                    {
                        cargarVariableTabla(tSimbolos,operando1,operando2,polaca,i);
                    }
                    polaca.remove(i-1); polaca.remove(i-2);
                    i -= 2;
                    resta+=2;
                    break;
                case "/" :

                    if (getTipo(operando1).equals("LONG"))
                    {
                        res += "MOV EAX, "+operando1+"\n";
                        res += "XOR EDX, EDX\n";
                        res += "CMP EDX, "+operando1+"\n";
                        res += "JZ division_por_cero\n";
                    }
                    else
                    {
                        variableAuxiliar = "@aux"+indiceVariable;
                        variableAuxiliarSiguiente = "@aux"+(indiceVariable+1);
                        res += "FLDZ ; introduce un 0 en ST\n";
                        if (Parser.getTS(operando1).get("uso").equals("constante")) {
                            res += "FLD @"+getParteEntera(operando1)+"f"+getParteDecimal(operando1)+" ; si el divisor es una constante lo pasamos a una variable auxiliar \n";
                            res += "FSTP "+variableAuxiliar+"\n";
                            res += "FCOMP "+variableAuxiliar+" ; verificamos que el divisor no sea 0 \n";
                        }
                        else {
                            res += "FCOMP " + operando1 + " ; verificamos que el divisor no sea 0\n";
                        }
                        res += "FSTSW AX\n";
                        res += "SAHF\n";
                        res += "JZ division_por_cero\n";
                    }

                    //Si las variables son de tipo entero usamos registros
                    if (getTipo(operando1).equals("LONG")) {
                        res += "MOV EAX, " + operando2 + "\n";
                        res += "CDQ\n";
                        if (Parser.getTS(operando1).get("uso").equals("constante")) {
                            variableAuxiliar = "@aux" + indiceVariable;
                            res += "MOV " + variableAuxiliar + ", " + operando1 + "\n";
                            res += "IDIV " + variableAuxiliar + "\n";
                            if (cargando) {
                                cargarVariableTabla(nuevasVariables, operando1, operando2, polaca, i);
                            } else {
                                cargarVariableTabla(tSimbolos, operando1, operando2, polaca, i);
                            }
                        }
                        else
                        {
                            res += "IDIV "+operando1+"\n";
                        }
                        variableAuxiliar = "@aux"+indiceVariable;
                        res += "MOV "+variableAuxiliar+", EAX"+"\n";
                    }
                    //Sino usamos el coprocesador
                    else
                    {
                        //En caso de que la variable sea constante, creamos una variable auxiliar
                        if (Parser.getTS(operando2).get("uso").equals("constante"))
                        {
                            res += "FLD @"+getParteEntera(operando2)+"f"+getParteDecimal(operando2)+"\n";
                        }
                        else
                        {
                            res += "FLD "+operando2+"\n";
                        }
                        //En caso de que la variable sea constante, creamos una variable auxiliar
                        if (Parser.getTS(operando1).get("uso").equals("constante"))
                        {
                            res += "FDIV @"+getParteEntera(operando1)+"f"+getParteDecimal(operando1)+"\n";
                        }
                        else
                        {
                            res += "FDIV "+operando1+"\n";
                        }
                        res += "FSTP "+variableAuxiliar+"\n";
                    }
                    //Si estamos en la etapa del .data (que se hace al final), cargamos las nuevas variables auxiliares al .data
                    if (cargando)
                    {
                        cargarVariableTabla(nuevasVariables,operando1,operando2,polaca,i);
                    }
                    //Sino, las cargamos en las tabla de simbolos y luego se añadiran al .data en la etapa posterior
                    else
                    {
                        cargarVariableTabla(tSimbolos,operando1,operando2,polaca,i);
                    }
                    polaca.remove(i-1); polaca.remove(i-2);
                    i -= 2;
                    resta+=2;
                    break;
                case "PR" :
                    //res += "invoke printf, cfm$(\""+operando1+"\\n\")\n";
                    res += "invoke MessageBox, NULL, addr "+operando1+", addr "+operando1+", MB_OK\n";
                    break;
                case "IN" :
                    //Cargamos el valor del operando de la funcion en el parametro de la funcion
                    res += "MOV EAX, "+operando2+"\n";
                    res += "MOV "+buscarParametroFuncion(operando1)+", EAX\n";
                    //En caso de que estemos en las sentencias declarativas (es decir, una invocacion dentro de una funcion), verificamos la recursion mutua
                    if (cargando)
                    {

                        res += "MOV EAX, "+Parser.indiceFunciones.get(quitarAmbito(operando1))+";Arranca la verificacion de la recursion mutua\n";
                        res += "MOV @var_fun_llamada, EAX ;cargamos la funcion llamada\n";

                        res += "FLD @var_ultima_fun; introduce la ultima funcion en ST\n";
                        res += "FLD @var_fun_llamada; introduce la funcion llamada en ST\n";
                        res += "FCOMP\n";
                        res += "FSTSW AX\n";
                        res += "SAHF\n";
                        res += "JE verificar_recursion_mutua"+indiceVerificar+" ; si ambas eran 0 (es un llamado de funcion dentro del main), se carga la funcion actual en la variable \n";
                        res += "JNE no_verificar"+indiceVerificar+" ;salteamos el proceso de verificacion ya que no es necesario\n";

                        res += "verificar_recursion_mutua"+indiceVerificar+": ;codigo para verificar recursion mutua\n";
                        res += "FLD @var_ultima_fun ; cargamos en ST el indice de la ultima funcion\n";
                        res += "FLD @var_fun_actual ; cargamos en ST la funcion actual\n";
                        res += "FCOMP ; comparamos la funcion inicial, con la funcion actual\n";
                        res += "FSTSW AX\n";
                        res += "SAHF\n";
                        res += "JNE recursion_mutua ; si son distintas significa que hubo recursion mutua \n";

                        res += "no_verificar"+indiceVerificar+":\n";
                        //res += "invoke printf, cfm$(\"no hay que verificar la recursion ya que no es un caso posible\\n\")\n";
                        res += "invoke MessageBox, NULL, addr msjNoVerificar, addr msjNoVerificar, MB_OK\n";
                        res += "MOV EAX, @var_fun_actual\n";
                        res += "MOV @var_ultima_fun, EAX ; termina la verificacion de la recursion mutua\n";
                        indiceVerificar++;
                    }
                    //Cargado de la variable funcion actual para realizar la verificacion de recursion mutua
                    res += "MOV EAX, "+Parser.indiceFunciones.get(quitarAmbito(operando1))+"\n";
                    res += "MOV @var_fun_actual, EAX ; cargamos la funcion actual en la variable @var_fun_actual\n";
                    //Si la funcion es un tipo definido, utilizamos una referencia a la funcion
                    if (Parser.getTS(operando1).get("uso").equals("variable")) {
                        res += "call ["+operando1+"]\n";
                    }
                    //Sino simplemente llamamos a la funcion mediante su nombre/etiqueta
                    else{
                        res += "call "+quitarAmbito(operando1)+"\n";
                    }
                    String tipoRetorno = (String)Parser.getTS(operando1).get("retorno");
                    //Cargamos el retorno de la funcion en polaca para luego cargarla en la variable correspondiente en caso de ser necesario
                    variableAuxiliar = "@aux"+indiceVariable;
                    if (cargando)
                    {
                        cargarVariableTabla(nuevasVariables,operando1,operando1,polaca,i);
                    }
                    //Sino, las cargamos en las tabla de simbolos y luego se añadiran al .data en la etapa posterior
                    else
                    {
                        cargarVariableTabla(tSimbolos,operando1,operando1,polaca,i);
                    }
                    if (tipoRetorno.equals("LONG"))
                    {
                        res += "MOV EAX, @var_ret_long\n";
                        res += "MOV "+variableAuxiliar+", EAX\n";
                        polaca.set(i,variableAuxiliar);
                    }
                    else
                    {
                        res += "MOV EAX, @var_ret_single\n";
                        res += "MOV "+variableAuxiliar+", EAX\n";
                        polaca.set(i,variableAuxiliar);
                    }











                    if (Parser.getTS(operando1).get("postcondicion").equals("true"))
                    {
                        operadorPostcondicion = buscarOperadorPostcondicion(operando1);
                        polaca.add(i,"BC");
                        int j=i;
                        while (!polaca.get(j).equals("LI"))
                        {
                            j++;
                        }
                        polaca.add(i,"["+(j+resta)+"]");
                        resta-=2;
                    }
                    polaca.remove(i-1); polaca.remove(i-2);
                    i -= 2;
                    resta+=2;

                    break;
                case ">" :
                case ">=" :
                case "<" :
                case "<=" :
                case "==" :
                case "<>" :
                    comparadorAuxiliar = "@aux_comp"+indiceComparacion;
                    //Usamos el coprocesador para comparar
                    //En caso de que la variable sea constante, creamos una variable auxiliar
                    if (Parser.getTS(operando2).get("uso").equals("constante"))
                    {
                        if (getTipo(operando2).equals("LONG"))
                        {
                            variableAuxiliar = "@aux"+indiceVariable;
                            res += "MOV "+variableAuxiliar+", "+operando2+"\n";
                            res += "FILD "+variableAuxiliar+"\n";
                            if (cargando)
                            {
                                cargarVariableTabla(nuevasVariables,operando1,operando2,polaca,i);
                            }
                            else
                            {
                                cargarVariableTabla(tSimbolos,operando1,operando2,polaca,i);
                            }
                        }
                        else
                        {
                            res += "FLD @"+getParteEntera(operando2)+"f"+getParteDecimal(operando2)+"\n";
                        }
                    }
                    else
                    {
                        if (getTipo(operando2).equals("LONG"))
                        {
                            res += "FILD "+operando2+"\n";
                        }
                        else
                        {
                            res += "FLD "+operando2+"\n";
                        }
                    }
                    if (Parser.getTS(operando1).get("uso").equals("constante"))
                    {
                        if (getTipo(operando1).equals("LONG"))
                        {
                            variableAuxiliar = "@aux"+indiceVariable;
                            res += "MOV "+variableAuxiliar+", "+operando1+"\n";
                            res += "FICOM "+variableAuxiliar+"\n";
                            if (cargando)
                            {
                                cargarVariableTabla(nuevasVariables,operando1,operando2,polaca,i);
                            }
                            else
                            {
                                cargarVariableTabla(tSimbolos,operando1,operando2,polaca,i);
                            }
                        }
                        else
                        {
                            if (getTipo(operando1).equals("LONG"))
                            {
                                res += "FICOM "+operando1+"\n";

                            }
                            else
                            {
                                res += "FLD @"+getParteEntera(operando1)+"f"+getParteDecimal(operando1)+"\n";
                                res += "FCOM\n";
                            }
                        }
                    }
                    else
                    {
                        if (getTipo(operando1).equals("LONG"))
                        {
                            res += "FICOM "+operando1+"\n";
                        }
                        else
                        {
                            res += "FCOM "+operando1+"\n";
                        }
                    }
                    res += "FSTSW @aux_comp"+indiceComparacion+"\n";
                    res += "MOV AX, @aux_comp"+indiceComparacion+"\n";
                    res += "SAHF\n";

                    if (cargando)
                    {
                        nuevasVariables.put(comparadorAuxiliar,new Hashtable<>());
                        nuevasVariables.get(comparadorAuxiliar).put("definida","true");
                        nuevasVariables.get(comparadorAuxiliar).put("uso","comparador");
                        nuevasVariables.get(comparadorAuxiliar).put("valor",operador);
                        polaca.set(i,comparadorAuxiliar);
                        String tipo = (String)Parser.getTS(operando1).get("tipo");
                        nuevasVariables.get(comparadorAuxiliar).put("tipo",tipo);
                        indiceComparacion++;
                    }
                    else {
                        Parser.tSimbolos.put(comparadorAuxiliar, new Hashtable<>());
                        Parser.tSimbolos.get(comparadorAuxiliar).put("definida","true");
                        Parser.getTS(comparadorAuxiliar).put("uso", "comparador");
                        Parser.getTS(comparadorAuxiliar).put("valor",operador);
                        String tipo = (String)Parser.getTS(operando1).get("tipo");
                        Parser.getTS(comparadorAuxiliar).put("tipo",tipo);
                        polaca.set(i, comparadorAuxiliar);
                        indiceComparacion++;
                    }
                    polaca.remove(i-1); polaca.remove(i-2);
                    i -= 2;
                    resta+=2;
                    break;
                case "BF" :
                case "RP" :
                    //Le sacamos los corchetes al string para obtener la posicion de salto
                	operando1 = operando1.substring(1,operando1.length()-1);
                	//En la estructura de etiquetas añadimos la posicion para cuando se llegue a esa posicion añadir una etiqueta
                	labels.add(Integer.parseInt(operando1));
                	//Salto condicional
                    String comparador = null;
                    if (cargando)
                    {
                        comparador = (String)nuevasVariables.get(comparadorAuxiliar).get("valor");
                    }
                    else
                    {
                        comparador = (String)Parser.getTS(comparadorAuxiliar).get("valor");
                    }
                    switch (comparador)
                    {
                        case ">" :
                            res += "JBE label"+(Integer.parseInt(operando1))+"\n"; break;
                        case ">=" :
                            res += "JB label"+(Integer.parseInt(operando1))+"\n"; break;
                        case "<" :
                            res += "JAE label"+(Integer.parseInt(operando1))+"\n"; break;
                        case "<=" :
                            res += "JA label"+(Integer.parseInt(operando1))+"\n"; break;
                        case "==" :
                            res += "JNE label"+(Integer.parseInt(operando1))+"\n"; break;
                        case "<>" :
                            res += "JE label"+(Integer.parseInt(operando1))+"\n"; break;
                        case "&&" :
                            res += "JNZ label"+(Integer.parseInt(operando1))+"\n"; break;
                        case "||" :
                            res += "JZ label"+(Integer.parseInt(operando1))+"\n"; break;
                    }
                    break;
                case "BI" :
                case "BR" :
                    //Le sacamos los corchetes al string para obtener la posicion de salto
                	operando1 = operando1.substring(1,operando1.length()-1);
                	//Agregamos la etiqueta de salto en caso de que sea mayor a la posicion actual
                	//if (Integer.parseInt(operando1)-resta > i)
                    //{
                    labels.add(Integer.parseInt(operando1));
                    //}
                	//Salto incondicional
                	res += "JMP label"+(Integer.parseInt(operando1))+"\n";
                    break;
                case "LI" :
                    //Añadimos las etiquetas en donde va a ser necesario saltar hacia atrás
                    labels.add(i+1+resta);
                    break;
                case "&&" :
                case "||" :
                    comparadorAuxiliar = "@aux_comp"+indiceComparacion;
                    //Pasamos el primer operando a un registro
                    res += "MOV AX, "+operando1+"\n";
                    if (operador.equals("&&")) {
                        res += "AND AX, "+operando2+"\n";
                    }
                    else {
                        res += "OR AX, "+operando2+"\n";
                    }
                    res += "MOV "+comparadorAuxiliar+", AX"+"\n";
                    //Si estamos en la etapa del .data (que se hace al final), cargamos las nuevas variables auxiliares al .data
                    //Sino, las cargamos en las tabla de simbolos y luego se añadiran al .data en la etapa posterior
                    if (cargando)
                    {
                        nuevasVariables.put(comparadorAuxiliar,new Hashtable<>());
                        nuevasVariables.get(comparadorAuxiliar).put("definida","true");
                        nuevasVariables.get(comparadorAuxiliar).put("uso","comparador");
                        nuevasVariables.get(comparadorAuxiliar).put("valor",operador);
                        polaca.set(i,comparadorAuxiliar);
                        String tipo = (String)Parser.getTS(operando1).get("tipo");
                        nuevasVariables.get(comparadorAuxiliar).put("tipo",tipo);
                        indiceComparacion++;
                    }
                    else {
                        Parser.tSimbolos.put(comparadorAuxiliar, new Hashtable<>());
                        Parser.tSimbolos.get(comparadorAuxiliar).put("definida","true");
                        Parser.getTS(comparadorAuxiliar).put("uso", "comparador");
                        Parser.getTS(comparadorAuxiliar).put("valor", operador);
                        String tipo = (String)Parser.getTS(operando1).get("tipo");
                        Parser.getTS(comparadorAuxiliar).put("tipo",tipo);
                        polaca.set(i, comparadorAuxiliar);
                        indiceComparacion++;
                    }
                    break;
                case "RT" :
                    if (getTipo(operando1).equals("LONG"))
                    {
                        res += "MOV EAX,"+operando1+"\n";
                        res += "MOV @var_ret_long, EAX\n";
                    }
                    else
                    {
                        res += "FLD "+operando1+"\n";
                        res += "FSTP @var_ret_single\n";
                    }
                    res += "MOV @var_ultima_fun, 0\n";
                    break;
                case "BC":
                    operando1 = operando1.substring(1,operando1.length()-1);
                    labels.add(Integer.parseInt(operando1));
                    switch (operadorPostcondicion)
                    {
                        case ">" :
                            res += "JBE label"+operando1+"\n"; break;
                        case ">=" :
                            res += "JB label"+operando1+"\n"; break;
                        case "<" :
                            res += "JAE label"+operando1+"\n"; break;
                        case "<=" :
                            res += "JA label"+operando1+"\n"; break;
                        case "==" :
                            res += "JNE label"+operando1+"\n"; break;
                        case "<>" :
                            res += "JE label"+operando1+"\n"; break;
                        case "&&" :
                            res += "JNZ label"+operando1+"\n"; break;
                        case "||" :
                            res += "JZ label"+operando1+"\n"; break;
                    }
                    break;
                    case "POST":
                        res += "MOV @var_postcondicion, AX\n";
                        break;

                    default:break;
            }
        }

        if (!labels.isEmpty())
        {
            for (int label : labels
                 ) {
                if (polaca.size()-1+resta < label)
                    res+="label"+label+":\n";
            }
        }
        labels.clear();
        return res;
    }

    public void cargarTablaSimbolos(ArrayList<String> polaca)
    {
        declaraciones = ".386\n"+
            "include \\masm32\\include\\masm32rt.inc\n" +
            "dll_dllcrt0 PROTO C\n" +
            "option casemap :none\n" +
            "includelib \\masm32\\lib\\kernel32.lib\n" +
            "includelib \\masm32\\lib\\user32.lib\n" +
                ".data\n" +
                "@var_ultima_fun DD 0\n" +
                "@var_fun_actual DD ?\n" +
                "@var_fun_llamada DD ?\n";

        funciones ="";
        funciones += ".code\n";

        cargando = true;
        cargarTablaSimbolos(polaca,tSimbolos);
        cargarTablaSimbolos(polaca,nuevasVariables);

        funciones += "start:";
        Parser.pwAs.println(declaraciones);
        Parser.pwAs.println(funciones);
    }

    public void cargarTablaSimbolos(ArrayList<String> polaca, Hashtable<String,Hashtable<String,Object>> tablaSimbolos) {

        for (Map.Entry<String, Hashtable<String, Object>> entry : tablaSimbolos.entrySet()) {
            String key = entry.getKey();
            Hashtable<String,Object> value = entry.getValue();
            if (value.containsKey("uso")) {
                switch ((String) value.get("uso")) {
                    case "constante":
                        if (value.get("tipo").equals("SINGLE"))
                                declaraciones += "@"+getParteEntera(key)+"f"+getParteDecimal(key)+" DD "+value.get("valor")+"\n";
                        else if (value.get("tipo").equals("FINAL"))
                        {
                            declaraciones += key+" DD "+value.get("valor")+"\n";
                        }
                        break;
                    case "variable":
                    case "parametro":
                        if (value.get("tipo")!=null && value.get("tipo").equals("LONG"))
                        {
                            declaraciones += key+" DD ?\n";
                        }
                        else {
                                declaraciones += key + " DD ?\n";
                            }
                        break;
                    case "funcion":
                        funciones += key+":\n";
                        int inicio = buscarIncioEnPolacaFunciones(key);
                        int fin = buscarFinPolacaFunciones(key,inicio);
                        ArrayList<String> polacaReducida = new ArrayList<>(polaca.subList(inicio+1,fin+1));
                        funciones += polacaToAssembler(polacaReducida);
                        polaca.addAll(fin+1,polacaReducida);
                        for (int i=inicio+1; i<fin+1; i++)
                        {
                            polaca.remove(inicio+1);
                        }
                        funciones += "ret\n\n";
                        break;
                    case "tipo_definido":
                        declaraciones += key+" DD ?\n";
                        break;
                    case "mensaje":
                        declaraciones += key+" DB \""+value.get("valor")+"\",0\n";
                        break;
                    case "comparador":
                        declaraciones += key+ " DW ?\n";
                }
            }
        }
    }

    public String getParteEntera(String flotante)
    {
        String variable = flotante.substring(0,flotante.indexOf("."));
        if (variable.contains("-"))
        {
            variable = variable.substring(variable.indexOf('-')+1,variable.length());
            variable = "n"+variable;
            return variable;
        }
        else
            return variable;
    }

    public String getParteDecimal(String flotante)
    {
        return flotante.substring(flotante.indexOf(".")+1,flotante.length());
    }

    public int buscarIncioEnPolacaFunciones(String nombreFuncion)
    {
        for (int i = 0; i < Parser.polacaFunciones.size(); i++)
        {
            if (Parser.polacaFunciones.get(i).equals("FN"))
            {
                if (Parser.polacaFunciones.get(i-2).equals(nombreFuncion))
                {
                    return i;
                }
            }
        }
        return -1;
    }

    public int buscarFinPolacaFunciones(String nombreFuncion, int inicio)
    {
        //VER FUNCION CON POSTCONDICION
        String fin;
        if (Parser.getTS(nombreFuncion).get("postcondicion").equals("true"))
        {
            fin = "POST";
        }
        else
        {
            fin = "RT";
        }
        for (int i=inicio; i<Parser.polacaFunciones.size(); i++)
        {
            if (Parser.polacaFunciones.get(i).equals(fin))
            {
                return i;
            }
        }
        return -1;
    }

    public String buscarParametroFuncion(String nombreFuncion)
    {
        if (Parser.getTS(nombreFuncion).get("uso").equals("variable"))
        {
            nombreFuncion = (String)Parser.getTS(nombreFuncion).get("valor");
        }
        else
        {
            nombreFuncion = quitarAmbito(nombreFuncion);
        }
        for (int i=0; i<=Parser.polacaFunciones.size()-2; i++)
        {
            if (Parser.polacaFunciones.get(i).equals(nombreFuncion) && Parser.polacaFunciones.get(i+2).equals(("FN")))
            {
                return Parser.polacaFunciones.get(i+1);
            }
        }
        return null;
    }

    public void cargarVariableTabla(Hashtable<String,Hashtable<String,Object>> tabla, String operando1,String operando2, ArrayList<String> polaca, int i)
    {
        tabla.put(variableAuxiliar,new Hashtable<>());
        tabla.get(variableAuxiliar).put("definida","true");
        tabla.get(variableAuxiliar).put("uso","variable");
        String tipo = null;
        if (verificarTipo(operando1, operando2))
             tipo = (String)Parser.getTS(operando1).get("tipo");
        tabla.get(variableAuxiliar).put("tipo",tipo);
        indiceVariable++;
        polaca.set(i,variableAuxiliar);
        Parser.tSimbolos.put(variableAuxiliar,new Hashtable<>());
        Parser.tSimbolos.get(variableAuxiliar).put("definida","true");
        Parser.getTS(variableAuxiliar).put("uso","variable");
        Parser.getTS(variableAuxiliar).put("tipo",tipo);
        if (!tipo.equals("LONG") && !tipo.equals("SINGLE"))
        {
            String retorno = tipo.substring(tipo.indexOf(' ')+1,tipo.length());
            String tipo_parametro = tipo.substring(0,tipo.indexOf(' '));
            tabla.get(variableAuxiliar).put("retorno",retorno);
            tabla.get(variableAuxiliar).put("tipo_parametro",tipo_parametro);
            Parser.getTS(variableAuxiliar).put("retorno",retorno);
            Parser.getTS(variableAuxiliar).put("tipo_parametro",tipo_parametro);
        }
    }

    public String getTipo(String operando)
    {
        String uso = (String)Parser.getTS(operando).get("uso");
        String tipo = (String)Parser.getTS(operando).get("tipo");
        if (uso.equals("funcion"))
        {
            return (String)Parser.getTS(operando).get("retorno");
        }
        else
        {
            if (uso.equals("variable"))
            {
                if (tipo.equals("SINGLE") || tipo.equals("LONG"))
                {
                    return tipo;
                }
                else
                {
                    return tipo.substring(tipo.indexOf(" ")+1,tipo.length());
                }
            }
            else
            {
                if (uso.equals("tipo definido"))
                {
                    return tipo.substring(tipo.indexOf(" ")+1,tipo.length());
                }
                else
                {
                    if (uso.equals("constante"))
                    {
                        return tipo;
                    }
                }
            }
        }
        return null;
    }

    public boolean verificarTipo(String operando1, String operando2)
    {
        String tipo1,tipo2;
        tipo1 = (String)Parser.getTS(operando1).get("tipo");
        tipo2 = (String)Parser.getTS(operando2).get("tipo");
        if (tipo1.equals("LONG") || tipo1.equals("SINGLE"))
        {
            if (!tipo2.equals("LONG") && !tipo2.equals("SINGLE"))
            {
                tipo2 = (String)Parser.getTS(operando2).get("retorno");
            }
        }
        if (!tipo1.equals(tipo2))
        {
            Parser.yyerrorS("Los tipos son incompatibles al asignar un valor a la variable "+quitarAmbito(operando1));
            Parser.erroresS++;
            return false;
        }
        return true;
    }

    public String buscarOperadorPostcondicion(String nombreFuncion)
    {
        int pos = 0;
        for (int i=0; i<=Parser.polacaFunciones.size(); i++) {
            if (Parser.polacaFunciones.get(i).equals(nombreFuncion)) {
                pos = i;
                break;
            }
        }
        for (int i = pos; i< Parser.polacaFunciones.size(); i++) {
            if (Parser.polacaFunciones.get(i).equals("POST"))
                return Parser.polacaFunciones.get(i-1);
        }
        return null;
    }

    public String quitarAmbito (String variable){
        if (variable.chars().filter(ch -> ch == '@').count() > 0)
            return variable.substring(0,variable.indexOf("@"));
        return variable;
    }

}

