
%{
package compi;
import java.lang.Math;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
%}

/* YACC Declarations */
%token ID CTE_LONG CTE_SINGLE IF THEN ELSE ENDIF FUNC RETURN BEGIN END BREAK PRINT REPEAT ASSIGN COMP_IGUAL COMP_MENOR_IGUAL COMP_MAYOR_IGUAL DISTINTO AND OR CADENA_MULTILINEA TYPEDEF POST TRY CATCH LONG SINGLE
%start program
/* Grammar follows */
%%



program	: ID ';' sentencias_declarativas bloque_programa { 
															agregarEstructura("Se termino de compilar el programa correctamente");}
		//| sentencias_declarativas bloque_programa {yyerror($1.ival,"Falta el nombre del programa");}
;



sentencias_declarativas	: sentencias_declarativas declaracion
						| declaracion 
;

declaracion	: declaracion_tipo 
		| funcion
		| tipo_definido  {agregarEstructura("DECLARACION DE TYPEDEF en linea " + $1.ival);}
		| error ';' {yyerror($1.ival,"Error en la declaracion de variables");}
;

bloque_programa	: comienzo_bloque_programa sentencias_ejecutables END
				| comienzo_bloque_programa sentencias_ejecutables {yyerror($2.ival,"Falta el END");}
				| comienzo_bloque_programa END {yyerror($1.ival,"El bloque del programa no puede estar vacio");}
				| error sentencias_ejecutables END {yyerror($1.ival, "Falta el BEGIN");}
;

comienzo_bloque_programa	: BEGIN {   
										//Cambiamos de polacaFunciones a polacaEjecutable al empezar con el bloque de programa
										polaca = polacaEjecutable;
										ejecutable = true;
										//Verificamos que las funciones que fueron invocadas dentro de la declaracion de una funcion y todavía no habían sido declaradas, hayan sido declaradas
										aDefinir.forEach((k, v) -> {
											if (!tSimbolos.containsKey(k))
											{
												yyerrorSem("Una funcion utilizó una funcion no declarada ");
											}
										});
								}
;

bloque_de_sentencias 	: BEGIN sentencias_ejecutables END
			| ejecutable
			| ejecutable sentencias_ejecutables END {yyerror($1.ival,"Falta el BEGIN");}
			| ejecutable END {yyerror($1.ival,"Falta el BEGIN");}
			| BEGIN sentencias_ejecutables {yyerror($1.ival,"Falta el END");}
;

sentencias_ejecutables	: sentencias_ejecutables ejecutable
			| ejecutable
;

ejecutable	: asignacion {};
		| seleccion
		| mensaje
		| repeticion
		| trycatch
		| BREAK ';' { if (cantRepeat == 0)
					  {
						 yyerror($1.ival, "No puede haber un break si no se encuentra dentro del repeat");
					  }
					  else
					  {
						pilaBreak.add(polaca.size());
					  	polaca.add(null);
					  	polaca.add("BR");
						  cantBreak++;
						  indicesBreak.add(cantRepeat);
					  }
					  
					}
;

declaracion_tipo	: tipo lista_de_variables ';' {estipodefinido = false;}
					| error ID ';' {yyerror($1.ival,"Declaracion de tipo faltante en linea " + $1.ival);}
					| tipo ';' {yyerror($1.ival,"Falta el id de la variable en linea " + $1.ival);}
					| tipo lista_de_variables {yyerror($2.ival,"; faltante");}
;

lista_de_variables	: lista_de_variables ',' ID {agregarEstructura("DECLARACION MULTIPLE en linea " + $1.ival);
												//Verificamos que la variable no haya sido redeclarada
												if (tSimbolos.get($3.sval+ambito).containsKey("definida"))
												{
													yyerror($1.ival,"Variable redeclarada");
												}
												//Si la variable se está declarando por primera vez
												else
												{
												//Le añadimos los atributos a la variable, el tipo de atributo, fue seteado en la regla "tipo" que se realizó anteriormente
   												tSimbolos.get($3.sval+ambito).put("tipo",atributotipo);
												tSimbolos.get($3.sval+ambito).put("uso","variable");
												tSimbolos.get($3.sval+ambito).put("definida",true);
												//Si el tipo de atributo no es ni single ni long, obtenemos el tipo de retorno del tipo definido para poder hacer la verificacion de tipos compatibles
												if (!atributotipo.equals("LONG") && !atributotipo.equals("SINGLE")){
													String aux;
													aux = atributotipo;
													aux = aux.substring(0,aux.indexOf(" "));
													tSimbolos.get($3.sval+ambito).put("tipo_parametro",aux);
													aux = atributotipo;
													aux = atributotipo.substring(aux.indexOf(" ")+1,aux.length());
													tSimbolos.get($3.sval+ambito).put("retorno",aux);
												 }
												}
												if (estipodefinido)
												{
													indiceFunciones.put($3.sval,indiceFunciones.size()+1);
												}
											 }
			| ID {agregarEstructura("DECLARACION SIMPLE en linea " + $1.ival);
					//Verificamos que la variable no haya sido redeclarada
					if (tSimbolos.get($1.sval+ambito).containsKey("definida"))
					{
						yyerror($1.ival,"Variable redeclarada");
					}
					else
					{
						//Le añadimos los atributos a la variable
						tSimbolos.get($1.sval+ambito).put("tipo",atributotipo);
						tSimbolos.get($1.sval+ambito).put("uso","variable");
						tSimbolos.get($1.sval+ambito).put("definida",true);
						//Si el tipo de atributo no es ni single ni long, obtenemos el tipo de retorno del tipo definido para poder hacer la verificacion de tipos compatibles
						if (!atributotipo.equals("LONG") && !atributotipo.equals("SINGLE")){
							String aux;
							aux = atributotipo;
							aux = aux.substring(0,aux.indexOf(" "));
							tSimbolos.get($1.sval+ambito).put("tipo_parametro",aux);
							aux = atributotipo;
							aux = atributotipo.substring(aux.indexOf(" ")+1,aux.length());
							tSimbolos.get($1.sval+ambito).put("retorno",aux);
						}
					}
					if (estipodefinido)
					{
						indiceFunciones.put($1.sval,indiceFunciones.size()+1);
					}
				}
			| ',' {yyerror($1.ival,"Parametro faltante luego de la ',' ");}
;

funcion	: cabeza_funcion sentencias_declarativas_fn BEGIN sentencias_ejecutables RETURN retorno END ';' {agregarEstructura("DECLARACION DE FUNCION en linea " + $1.ival + " hasta linea " + $8.ival);
																											//Verificamos que el tipo del retorno sea el mismo que el tipo de retorno de la funcion
																											if (getTS($6.sval+ambito) != null)
																											{
																												if (!getTS($6.sval+ambito).get("tipo").equals($1.sval))
																												{
																													yyerror($6.ival,"Retorno incorrecto de tipo");
																												}
																											}
																											//ME QUEDE ACA PQ ME CANSE
																											if (funcionactual != null)
																											{
																												getTS(funcionactual).put("tipo",(String)tSimbolos.get(funcionactual).get("tipo_parametro")+" "+(String)tSimbolos.get(funcionactual).get("retorno"));
																												indiceFunciones.put(funcionactual,indiceFunciones.size()+1);
																												getTS(funcionactual).put("postcondicion","false");
																											 	funcionactual = null;
																											}
																											ambito=ambito.substring(0,ambito.lastIndexOf(charAmbito));
																											
																											}
		| cabeza_funcion sentencias_declarativas_fn BEGIN sentencias_ejecutables RETURN retorno postcondicion END ';' {agregarEstructura("DECLARACION DE FUNCION CON POSTCONDICION en linea " + $1.ival + " hasta linea " + $9.ival);
																														//Verificamos que el tipo del retorno sea el mismo que el tipo de retorno de la funcion
																														if (getTS($6.sval+ambito) != null)
																														{
																															if (!getTS($6.sval+ambito).get("tipo").equals($1.sval))
																															{
																																yyerror($6.ival,"Retorno incorrecto de tipo");
																															}
																														}
																														if (funcionactual != null)
																														{
																															getTS(funcionactual).put("tipo",(String)tSimbolos.get(funcionactual).get("tipo_parametro")+" "+(String)tSimbolos.get(funcionactual).get("retorno"));
																															getTS(funcionactual).put("postcondicion",true);
																															indiceFunciones.put(funcionactual,indiceFunciones.size()+1);
																															getTS(funcionactual).put("postcondicion","true");
																															funcionactual=null;
																														}
																														ambito=ambito.substring(0,ambito.lastIndexOf(charAmbito));
																														
																													}
;

//CAMBIAR TODO

	
	
	/*| FUNC ID '(' parametro ')' sentencias_declarativas BEGIN sentencias_ejecutables RETURN retorno END ';' {yyerror($1.ival,"Tipo faltante en la delcaracion de funcion en linea ");}
	| tipo ID '(' parametro ')' sentencias_declarativas BEGIN sentencias_ejecutables RETURN retorno END ';' {yyerror($1.ival,"Palabra reservada FUNC faltante en la delcaracion de funcion en linea ");}
	| tipo FUNC '(' parametro ')' sentencias_declarativas BEGIN sentencias_ejecutables RETURN retorno END ';' {yyerror($2.ival,"Nombre de funcion faltante en la delcaracion de funcion en linea ");}
	| tipo FUNC ID parametro ')' sentencias_declarativas BEGIN sentencias_ejecutables RETURN retorno END ';' {yyerror($3.ival,"Parentesis inicial faltante en la delcaracion de funcion en linea ");}
	| tipo FUNC ID '(' ')' sentencias_declarativas BEGIN sentencias_ejecutables RETURN retorno END ';' {yyerror($4.ival,"Parametro faltante en la delcaracion de funcion en linea ");}
	| tipo FUNC ID '(' parametro sentencias_declarativas BEGIN sentencias_ejecutables RETURN retorno END ';' {yyerror($5.ival,"Parametro final faltante en la delcaracion de funcion en linea ");}
	| tipo FUNC ID '(' parametro ')' sentencias_declarativas sentencias_ejecutables RETURN retorno END ';' {yyerror($8.ival,"BEGIN faltante en la delcaracion de funcion en linea ");}
	//| tipo FUNC ID '(' parametro ')' sentencias_declarativas BEGIN sentencias_ejecutables retorno END ';' {yyerror($10.ival,"Palabra reservada RETURN faltante en la delcaracion de funcion en linea ");}
	| tipo FUNC ID '(' parametro ')' sentencias_declarativas BEGIN sentencias_ejecutables RETURN END ';' {yyerror($9.ival,"retorno faltante en la delcaracion de funcion en linea ");}
	//| tipo FUNC ID '(' parametro ')' sentencias_declarativas BEGIN sentencias_ejecutables RETURN retorno ';' {yyerror($10.ival,"END faltante en la delcaracion de funcion en linea ");}
	| tipo FUNC ID '(' parametro ')' sentencias_declarativas BEGIN sentencias_ejecutables RETURN retorno END  {yyerror($11.ival,"; en la delcaracion de funcion en linea ");}*/
;

cabeza_funcion	: cuerpo_fn '(' parametro_funcion ')' {
												polaca.add("FN");
											}
												

cuerpo_fn	: tipo FUNC ID { 	if (tSimbolos.containsKey($3.sval))
								{
									yyerror($1.ival, "Funcion redeclarada");
								}	
								//if (!tSimbolos.containsKey($3.sval+ambito))
								else
								{
									tSimbolos.put($3.sval,new Hashtable<>());
									Hashtable<String,Object> aux = new Hashtable<String,Object>();
									aux = (Hashtable<String, Object>) (tSimbolos.get($3.sval+ambito).clone());
									for (Map.Entry<String, Object> keyValue: aux.entrySet()) {
										tSimbolos.get($3.sval).put(keyValue.getKey(),keyValue.getValue());
									}
									tSimbolos.remove($3.sval+ambito);
									if (tSimbolos.get($3.sval).containsKey("definida"))
									{
										yyerror($3.ival,"Funcion redeclarada");
									}
									else
									{
										tSimbolos.get($3.sval).put("uso","funcion");
										tSimbolos.get($3.sval).put("definida",true);
									}
									tSimbolos.get($3.sval).put("retorno",$1.sval);
									funcionactual = $3.sval;
									polaca.add($3.sval);
								}
								ambito=ambito+charAmbito+$3.sval.toString();
							}
								
;

sentencias_declarativas_fn :	sentencias_declarativas
						   |	
;

tipo_definido	: encabezado_tipo_definido encabezado_de_funcion ';' {funcionactual = null;} 
		| TYPEDEF '=' encabezado_de_funcion ';' {yyerror($1.ival,"Falta el ID del TYPEDEF");}
		| TYPEDEF ID encabezado_de_funcion ';' {yyerror($2.ival,"Falta el '=' del TYPEDEF");}
		| TYPEDEF ID '=' ';' {yyerror($3.ival,"Falta el encabezado de funcion del TYPEDEF");}
		| TYPEDEF error encabezado_de_funcion ';' {yyerror($1.ival,"Error en la declaracion del TYPEDEF");}
		| TYPEDEF ID '=' error ';' {yyerror($1.ival,"Error en el encabezado de funcion del TYPEDEF");}
		//| TYPEDEF ID '=' encabezado_de_funcion {yyerror($4.ival,"; faltante");} 
;

encabezado_tipo_definido	: TYPEDEF ID '=' {if (tSimbolos.get($2.sval+ambito).containsKey("definida"))
															{
																yyerror($1.ival,"Variable redeclarada");
															}
												else{
															tSimbolos.get($2.sval+ambito).put("uso","tipo_definido");
															tSimbolos.get($2.sval+ambito).put("definida",true);
															//Añadimos a la estructura de tiposdefinidos, el id del tipo definido
															tiposdefinidos.add($2.sval);
															funcionactual = $2.sval+ambito;}
														}
;

asignacion	: ID ASSIGN expresion_aritmetica ';' {agregarEstructura("ASIGNACION en linea " + $1.ival);
												  if (Parser.getTS($1.sval+ambito)!=null)
												  {
													  if (!Parser.getTS($1.sval+ambito).get("tipo").equals("LONG") && !Parser.getTS($1.sval+ambito).get("tipo").equals("SINGLE"))
                    							      {
                        								Parser.getTS($1.sval+ambito).put("postcondicion",Parser.getTS($3.sval+ambito).get("postcondicion"));
                    							  	  }
												  }
												  polaca.add($1.sval+ambito);
												  polaca.add(":=");
												  //System.out.println(getTS($1.sval+ambito).get("uso"));
												  if (getTS($1.sval+ambito) != null)
												  {
													if (getTS($1.sval+ambito).get("uso").equals("variable") && !getTS($1.sval+ambito).get("tipo").equals("LONG") && !getTS($1.sval+ambito).get("tipo").equals("SINGLE"))
													{
														if (getTS($3.sval+ambito).get("uso").equals("variable") && !getTS($3.sval+ambito).get("tipo").equals("LONG") && !getTS($3.sval+ambito).get("tipo").equals("SINGLE"))
														{
															getTS($1.sval+ambito).put("valor",getTS($3.sval+ambito).get("valor"));
														}
														else
														{
															getTS($1.sval+ambito).put("valor",$3.sval);
														}
													}
													if (!getTS($1.sval+ambito).get("uso").equals("tipo_definido") && !getTS($1.sval+ambito).get("uso").equals("funcion"))
													{}
													else
													{
														if (getTS($1.sval+ambito).get("uso").equals("tipo_definido"))
															yyerror($1.ival,"No se puede asignar valor a un tipo definido");
														else
															yyerror($1.ival,"No se puede asignar valor a una funcion");
													}
												  }
												  else
												  {
													  yyerrorSem($1.ival,"La variable no fue declarada");
												  }
												  }
												  
			//podemos agregar mas asignaciones
		| ID expresion_aritmetica ';' {yyerror($1.ival,"Falta el operador de asignacion ");}
		| ID ASSIGN ';' {yyerror($1.ival,"Falta la expresion aritmetica");}
		| ID ASSIGN error ';' {yyerror($1.ival,"Error en la expresion aritmetica");}
		| ID error expresion_aritmetica ';' {yyerror($1.ival,"Error en el operador de asignacion");}
		//| ID ASSIGN expresion_aritmetica {yyerror($3.ival,"; faltante");}
;

seleccion	: IF '(' cpo_if ')' THEN cpo_then ELSE cpo_else ENDIF ';' {agregarEstructura("SELECCION en linea " + $1.ival + " hasta linea " + $10.ival);}
		| IF '(' cpo_if ')' THEN cpo_then_simple ENDIF ';' {agregarEstructura("SELECCION en linea " + $1.ival + " hasta linea " + $8.ival);}

		| IF cpo_if ')' THEN cpo_then ENDIF ';' {yyerror($1.ival,"Falta el parentesis inicial de la condicion");}
		| IF '(' ')' THEN cpo_then ENDIF ';' {yyerror($2.ival,"Falta la condicion del IF");}
		| IF '(' cpo_if THEN cpo_then ENDIF ';' {yyerror($3.ival,"Falta el parentesis final de la condicion");}
		| IF '(' cpo_if ')' cpo_then ENDIF ';' {yyerror($4.ival,"Falta el THEN");}
		| IF '(' cpo_if ')' THEN cpo_then ';' {yyerror($7.ival,"Falta el ENDIF");}
		| IF '(' error ')' THEN cpo_then ';' {yyerror($2.ival,"Error en la condicion");}
		| IF error ';' {yyerror($1.ival,"Error en la sentencia de seleccion");}
		//| IF '(' cpo_if ')' THEN cpo_then ENDIF {yyerror($9.ival,"; faltante");}

		| IF cpo_if ')' THEN cpo_then ELSE cpo_else ENDIF ';' {yyerror($1.ival,"Falta el parentesis inicial de la condicion");}
		| IF '(' ')' THEN cpo_then ELSE cpo_else ENDIF ';' {yyerror($2.ival,"Falta la condicion del IF");}
		| IF '(' cpo_if THEN cpo_then ELSE cpo_else ENDIF ';' {yyerror($3.ival,"Falta el parentesis final de la condicion");}
		| IF '(' cpo_if ')' cpo_then ELSE cpo_else ENDIF ';' {yyerror($4.ival,"Falta el THEN");}
		| IF '(' cpo_if ')' THEN cpo_then ELSE cpo_else ';' {yyerror($8.ival,"Falta el ENDIF");}
		//| IF '(' cpo_if ')' THEN cpo_then ELSE cpo_else ENDIF {yyerror($9.ival,"; faltante");}
;

cpo_if	: condicion {pilaPolaca.add(polaca.size());
					 polaca.add(null);
					 polaca.add("BF");}
;

cpo_then_simple	:	bloque_de_sentencias {polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()-cantRepeat*5));
											pilaPolaca.remove(pilaPolaca.size()-1);}

cpo_then	: bloque_de_sentencias {polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()+2-cantRepeat*5));
									pilaPolaca.add(polaca.size());

									
									polaca.add(null);
									polaca.add("BI");
									
									pilaPolaca.remove(pilaPolaca.size()-2);}
									
;

cpo_else	: bloque_de_sentencias { polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()-cantRepeat*5));
									 pilaPolaca.remove(pilaPolaca.size()-1);}
;

mensaje	: PRINT '(' CADENA_MULTILINEA ')' ';' {	if ($1.ival == $4.ival) {
													agregarEstructura("MENSAJE en linea " + $1.ival);
												}
												else
												{
													agregarEstructura("MENSAJE MULTILINEA de linea " + $1.ival + " a linea " + $4.ival);
												}
												polaca.add("msj"+(nroMensaje-1)+ambito);
												polaca.add("PR");
												tSimbolos.get("msj"+(nroMensaje-1)+ambito).put("uso","mensaje");
											}
	| PRINT CADENA_MULTILINEA ')' ';' {yyerror($1.ival,"Falta el parentesis inicial en el mensaje");}
	| PRINT '(' ')' ';' {yyerror($2.ival,"Falta el cuerpo del mensaje");}
	| PRINT '(' CADENA_MULTILINEA ';' {yyerror($3.ival,"Falta el parentesis final en el mensaje");}
	| PRINT error ';' {yyerror($1.ival,"Error en el print");}
	//| PRINT '(' CADENA_MULTILINEA ')'
;

repeticion 	: REPEAT '(' cond_repeat ')' cpo_repeat ';' {agregarEstructura("REPEAT en linea " + $1.ival + " hasta linea " + $6.ival);
														 cantRepeat--;
														}
		//| REPEAT ID ASSIGN constante ';' condicion ';' constante ')' cpo_repeat ';' {yyerror($1.ival,"Falta el parentesis inicial del REPEAT");}
		//| REPEAT '(' ASSIGN constante ';' condicion ';' constante ')' cpo_repeat ';' {yyerror($2.ival,"Falta la variable del REPEAT");}
		//| REPEAT '(' ID constante ';' condicion ';' constante ')' cpo_repeat ';' {yyerror($3.ival,"Falta el operador de asignacion del REPEAT");}
		//| REPEAT '(' ID ASSIGN ';' condicion ';' constante ')' cpo_repeat ';' {yyerror($4.ival,"Falta el valor asignado a la variable del REPEAT");}
		//| REPEAT '(' ID ASSIGN constante condicion ';' constante ')' cpo_repeat ';' {yyerror($5.ival,"Falta ';' en la asignacion del REPEAT ");}
		//| REPEAT '(' ID ASSIGN constante ';' ';' constante ')' cpo_repeat ';' {yyerror($6.ival,"Falta la condicion del REPEAT");}
		//| REPEAT '(' ID ASSIGN constante error ';' constante ')' cpo_repeat ';' {yyerror($5.ival,"Error en la condicion");}

		//| REPEAT '(' ID ASSIGN constante ';' condicion constante ')' cpo_repeat ';' {yyerror($7.ival,"Falta ';' en la condicion del REPEAT");}

		//| REPEAT '(' ID ASSIGN constante ';' condicion ';' ')' cpo_repeat ';' {yyerror($8.ival,"Falta la constante del REPEAT");}
		//| REPEAT '(' ID ASSIGN constante ';' condicion ';' constante cpo_repeat ';' {yyerror($9.ival,"Falta el parentesis final del REPEAT");}
		//| REPEAT error cpo_repeat ';' {yyerror($1.ival,"Error en la delcaracion del REPEAT");}
		//| REPEAT '(' ID ASSIGN constante ';' error ';' constante cpo_repeat ';' {yyerror($6.ival,"Error en la condicion del REPEAT");}

		//| REPEAT '(' ID ASSIGN constante ';' condicion ';' constante ')' cpo_repeat 
;

cond_repeat	: asignacion_repeat ';' condicion_repeat ';' constante; {
																	inicioAsignacion.add(polaca.size()-1);
																	polaca.add($1.sval+ambito);
															      try {
																	if (Integer.parseInt($5.sval) > 0)
																	{
																		polaca.add("+");
																	}
																	else
																	{
																		polaca.add("-");
																	}
																	polaca.add($1.sval+ambito);
																	polaca.add(":=");
																}
																catch (NumberFormatException e) {
																if (Double.parseDouble($5.sval) >= 0)
																	yyerror($5.ival, "El aumento del repeat no es entero");
																else if (Double.parseDouble($5.sval) < 0)
																	yyerror($5.ival, "El decremento del repeat no es entero");
																}
																	}
		/*|   ASSIGN constante ';' condicion ';' constante  {yyerror($2.ival,"Falta la variable del REPEAT");}
		|   ID constante ';' condicion ';' constante {yyerror($3.ival,"Falta el operador de asignacion del REPEAT");}
		|   ID ASSIGN ';' condicion ';' constante  {yyerror($4.ival,"Falta el valor asignado a la variable del REPEAT");}
		|   ID ASSIGN constante condicion ';' constante  {yyerror($5.ival,"Falta ';' en la asignacion del REPEAT ");}
		|   ID ASSIGN constante ';' ';' constante  {yyerror($6.ival,"Falta la condicion del REPEAT");}
		|  ID ASSIGN constante error ';' constante  {yyerror($5.ival,"Error en la condicion");}*/
;		

asignacion_repeat :	ID ASSIGN constante {polaca.add($1.sval+ambito);
										 polaca.add(":=");
										 polaca.add("LI");
										 pilaPolaca.add(polaca.size());
										 if (getTS($1.sval+ambito) == null){
													  yyerrorSem($1.ival,"Variable no declarada");
										 }
										else
										{
											try {
												Integer.parseInt($3.sval);
										 	}
											catch (NumberFormatException e) {
												if (Double.parseDouble($3.sval) >= 0)
													yyerror($3.ival, "La constante de asignacion no es entera");
												}
										}
										cantRepeat++;
									   }
;

condicion_repeat	: condicion {pilaPolaca.add(polaca.size());
										 polaca.add(null);
										 polaca.add("RP");
								}
;


cpo_repeat	: bloque_de_sentencias {
									if (cantBreak > 0 && indicesBreak.contains(cantRepeat))
									{
										polaca.set(pilaBreak.get(pilaBreak.size()-1),indicePolacaToString(polaca.size()+2-(cantRepeat-1)*5));
										pilaBreak.remove(pilaBreak.size()-1);
										indicesBreak.remove(cantRepeat);
										cantBreak--;
									}
									
									for (int i=0; i<5; i++)
									{
										polaca.add(polaca.get(inicioAsignacion.get(inicioAsignacion.size()-1)));
										polaca.remove(((int)inicioAsignacion.get(inicioAsignacion.size()-1)));
									}
									inicioAsignacion.remove(inicioAsignacion.size()-1);

									polaca.add(indicePolacaToString(pilaPolaca.get(pilaPolaca.size()-2)));
									polaca.add("BI");
									pilaPolaca.remove(pilaPolaca.size()-2);

									

									polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()-(cantRepeat-1)*5));
									pilaPolaca.remove(pilaPolaca.size()-1);}
;

trycatch	: inicio_try cpo_try CATCH cpo_catch ';' {agregarEstructura("TRYCATCH en linea " + $1.ival + " hasta linea " + $4.ival);
																	  polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()));
																	  pilaPolaca.remove(pilaPolaca.size()-1);
																	  cantTry = false;}
		| inicio_try CATCH cpo_catch ';' {yyerror($1.ival,"Falta el cpo_try despues del TRY");}
		| inicio_try error ';' {yyerror($1.ival,"Error en el TRYCATCH");}

		//| inicio_try cpo_try CATCH BEGIN sentencias_ejecutables END
;

inicio_try : TRY {cantTry =true;}
;

cpo_try	: asignacion {
					  pilaPolaca.add(polaca.size());
					  polaca.add(null);
					  polaca.add("BI");
					  polaca.add("LI");}
		| seleccion //{System.out.println("polaca seleccion = "+polaca);}
		| mensaje //{System.out.println("polaca mensaje = "+polaca);}
		| BREAK ';'
		| repeticion//{System.out.println("polaca repeat = "+polaca);}
;

cpo_catch	: BEGIN sentencias_ejecutables END {polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()-cantRepeat*5));}
;



				//Seteamos la variable "atributoTipo" para que luego saber de que tipo son las variables declaradas
tipo	: LONG { atributotipo = $1.sval;}
		| SINGLE { atributotipo = $1.sval;}
		| ID  { //Si el tipo de definicion de una variable es un tipo definido, verificamos que este exista
				if(!tiposdefinidos.contains($1.sval))
				{
					yyerror($1.ival,"El tipo definido es incorrecto");
				}
				else
				{
					atributotipo = ((String)getTS($1.sval+ambito).get("tipo_parametro"))+" "+((String)getTS($1.sval+ambito).get("retorno"));
					estipodefinido = true;
				}
			}
;

encabezado_de_funcion	: tipo FUNC '(' tipo ')' {  if (funcionactual != null)
													{
														tSimbolos.get(funcionactual).put("tipo_parametro",$4.sval);
														tSimbolos.get(funcionactual).put("retorno",$1.sval);
														tSimbolos.get(funcionactual).put("tipo",(String)tSimbolos.get(funcionactual).get("tipo_parametro")+" "+(String)tSimbolos.get(funcionactual).get("retorno"));
													}
													}
			| FUNC '(' tipo ')' {yyerror($1.ival,"Falta el tipo de funcion");}
			| tipo '(' tipo ')' {yyerror($1.ival,"Falta el FUNC en el encabezado de funcion");}
			| tipo FUNC tipo ')' {yyerror($2.ival,"Falta el parentesis inicial del encabezado de funcion");}
			| tipo FUNC '(' ')' {yyerror($3.ival,"Falta el tipo de parametro de la funcion");}
			| tipo FUNC '(' tipo {yyerror($4.ival,"Falta el parentesis final del encabezado de funcion");}
;

retorno	: expresion_aritmetica ';'  {
									 polaca.add("RT");}
		| expresion_aritmetica {yyerror($1.ival,"Falta el ';' en el retorno de la funcion");}
;

postcondicion	: POST ':' '(' condicion ')' ';' {polaca.add("POST");}
		| POST '(' condicion ')' ';' {yyerror($1.ival,"Faltan los ':'");}
		| POST ':' condicion ')' ';' {yyerror($2.ival,"Falta el parentesis inicial':'");}
		| POST ':' '(' ')' ';' {yyerror($3.ival,"Falta la condicion");}
		| POST ':' '(' condicion ';' {yyerror($4.ival,"Faltan el parentesis final");}
		| POST ':' '(' error ')' ';' {yyerror($3.ival,"Error en la condicion");}
		| POST error ';' {yyerror($1.ival,"Error en la declaracion del POST");}
		| POST ':' '(' condicion ')' {yyerror($5.ival,"Falta el ;");}
;


parametro	: tipo ID { 
						if (funcionactual != null)
						{
							tSimbolos.get($2.sval+ambito).put("uso","parametro");
							tSimbolos.get(funcionactual).put("tipo_parametro",$1.sval);
							tSimbolos.get($2.sval+ambito).put("tipo",$1.sval);
							tSimbolos.get($2.sval+ambito).put("definida",true);
							tSimbolos.get($2.sval+ambito).put("valor",tSimbolos.get($2.sval+ambito));
						}
						polaca.add($2.sval+ambito); 
						idParametro = $2.sval+ambito;
					  }
			| tipo {yyerror($1.ival,"Falta el id del parametro");}
			//| ID {yyerror($1.ival,"Falta el tipo del parametro");}
;

parametro_invocacion :	parametro {
						if (!$1.sval.equals((String)getTS(idParametro).get("tipo")))
						{
							yyerror($1.ival,"Los tipos del parametro son distintos");
						}
}

parametro_funcion : parametro {
	tSimbolos.get(idParametro).put("uso","variable");
	tSimbolos.get(idParametro).put("tipo",$1.sval);
	tSimbolos.get(idParametro).put("definida","true");
}

expresion_aritmetica	: expresion_aritmetica '+' termino {polaca.add("+");}
 			| expresion_aritmetica '-' termino {polaca.add("-");}
 			| termino 
;

termino		: termino '*' factor { polaca.add("*");}
 			| termino '/' factor { polaca.add("/");}
 			| factor

factor	: ID {	polaca.add($1.sval+ambito);
				if (getTS($1.sval+ambito) == null)
				{
					yyerrorSem($1.ival,"Variable no declarada");
				}
			 }
		| constante 
		| invocacion {polaca.add("IN");
					  if (getTS($1.sval+ambito)!=null)
					  {
						    if (getTS($1.sval+ambito).get("postcondicion").equals("true"))
							{
								if (!cantTry)
								{
									yyerror($1.ival,"La invocacion con postcondicion no se encuentra dentro de un try");
								}
							}
					  }
					 }

		| '-' ID {polaca.add($2.sval+ambito);
				  if (!getTS($2.sval+ambito).containsKey("definida")){
					yyerrorSem($1.ival,"Variable no declarada");}}
		| '-' invocacion {polaca.add($2.sval+ambito);}
;

invocacion	: ID '(' parametro_invocacion ')' {
									//Si dentro de una funcion se invoca una funcion, tiraria error ya que la funcion puede estar definida mas abajo en el codigo
									//Por lo que si ese es el caso, añadimos la funcion llamada a la estructura "aDefinir" para luego verificar si fue definida o no
									if (getTS($1.sval+ambito) == null)
									{
										//Si ya estamos en el codigo ejecutable significa que la funcion no se declaro
										if (ejecutable)
										{
											yyerrorSem($1.ival,"Funcion no declarada");
										}
										else
										{
											aDefinir.put($1.sval,$3.sval);
										}
									}						
									else
									{	
										//Si la funcion no fue definida añadimos el error
										if (getTS($1.sval+ambito) == null) {
											yyerrorSem($1.ival,"Funcion no declarada");}
										else {	
											//Si la funcion ya fue definida
											//Verificamos que el parametro de la funcion sea el correcto
											if (!getTS($1.sval+ambito).get("tipo_parametro").equals($3.sval))
											{
												yyerror($1.ival,"El parametro de la funcion es incorrecto");}
											}
									}
									//Si nos encontramos en un ambito distinto al del programa principal
									//PARA QUE
									if (!ambito.equals(charAmbito+ambitoInicial))
									{
										String funcionLlamadora = ambito.substring(0,ambito.indexOf('@'));
										funcionesLlamadas.put(ambito.substring(0,ambito.indexOf('@')),new ArrayList<>());
										funcionesLlamadas.get(funcionLlamadora).add($3.sval);
									}
									//Añadimos a la polaca el id de la funcion, antes se añadió el parametro
									//En caso de que sea un tipo_definido y no una funcion, le añadimos el ambito
									//System.out.println("Busco en la tabla de simbolos "+$1.sval);
									//System.out.println("tsimbolos="+tSimbolos);
									if (tSimbolos.get($1.sval) == null)
									{
									//	System.out.println("existe");
										polaca.add($1.sval+ambito);
									}
									else
									{
									//	System.out.println("no existe");
										polaca.add($1.sval);
									}
											
									}
			| ID parametro_invocacion ')' {yyerror($1.ival,"Falta el parentesis de apertura de la invocacion");}
			| ID '(' ')' {yyerror($2.ival,"Falta el parametro de la invocacion");}
			//| ID '(' parametro_invocacion {yyerror($2.ival,"Falta el parentesis final de la invocacion");}
;

constante	: CTE_LONG { 
						//Verificamos que el numero no se pase de los límites ahora que sabemos si el número es negativo o positivo
						if ($1.sval.equals("2147483648")){
							yyerror($1.ival,"Constante positiva fuera de rango");
							//Solucionamos el error, poniendo como valor el valor maximo
							$1.sval="2147483647";
						}
						//Si la tabla de simbolos no contenia a la constante, la añadimos
						if (!tSimbolos.containsKey($1.sval)){
							tSimbolos.put($1.sval,new Hashtable<String,Object>());
						}
						//Le añadimos los atributos a la constante
						tSimbolos.get($1.sval).put("uso","constante");
						tSimbolos.get($1.sval).put("definida","true");
						tSimbolos.get($1.sval).put("token",(int)Parser.CTE_LONG);
						tSimbolos.get($1.sval).put("tipo","LONG");
						tSimbolos.get($1.sval).put("valor",$1.sval);
						//Añadimos a la polaca el valor de la constante
						polaca.add($1.sval);
					}	

		| '-' CTE_LONG  {	
							//Al ser negativa la constante es probable que no se haya añadido a la tabla de Simbolos, por lo que la añadimos
							if (!tSimbolos.containsKey("-"+$2.sval))
							{
								tSimbolos.put("-"+$2.sval,new Hashtable<String,Object>());
							}
							//Le añadimos los atributos a la constante
							tSimbolos.get("-"+$2.sval).put("uso","constante");
							tSimbolos.get("-"+$2.sval).put("definida","true");
							tSimbolos.get("-"+$2.sval).put("token",(int)Parser.CTE_LONG);
							tSimbolos.get("-"+$2.sval).put("tipo","LONG");
							tSimbolos.get("-"+$2.sval).put("valor","-"+$2.sval);
							//Añadimos a la polaca la constante
							polaca.add("-"+$2.sval);
						}
						
		| CTE_SINGLE {
						/*if (!tSimbolos.containsKey($1.sval))
							{
								tSimbolos.put("-"+$1.sval,new Hashtable<String,Object>());
							}*/
						//Le añadimos los atributos a la constante
						tSimbolos.get($1.sval).put("uso","constante");
						tSimbolos.get($1.sval).put("definida","true");
					  	tSimbolos.get($1.sval).put("token",(int)Parser.CTE_SINGLE);
					  	tSimbolos.get($1.sval).put("tipo","SINGLE");
					  	tSimbolos.get($1.sval).put("valor",$1.sval);
						//Añadimos a la polaca la constante  
						polaca.add($1.sval);
					}

		| '-' CTE_SINGLE {	
							//Al ser negativa la constante es probable que no se haya añadido a la tabla de Simbolos, por lo que la añadimos
							if (!tSimbolos.containsKey("-"+$2.sval))
							{
								tSimbolos.put("-"+$2.sval,new Hashtable<String,Object>());
							}
							//Le añadimos los atributos a la constante
							tSimbolos.get("-"+$2.sval).put("uso","constante");
							tSimbolos.get("-"+$2.sval).put("definida","true");
							tSimbolos.get("-"+$2.sval).put("token",(int)Parser.CTE_SINGLE);
							tSimbolos.get("-"+$2.sval).put("tipo","SINGLE");
							tSimbolos.get("-"+$2.sval).put("valor","-"+$2.sval);
							polaca.add("-"+$2.sval);
						 }
;

condicion	: condicion AND expresion_booleana { polaca.add("&&");}
			| expresion_booleana 
			| error AND comparacion {yyerror($2.ival,"Error en la expresion de la izquierda");}
			| expresion_booleana error comparacion {yyerror($1.ival,"Error en el comparador");}
			//| expresion_booleana AND error ';' {yyerror($2.ival,"Error en la expresion de la derecha");}
;

expresion_booleana	: expresion_booleana  OR comparacion { polaca.add("||");}
			| comparacion
;

comparacion	: expresion_aritmetica comparador expresion_aritmetica {polaca.add(varcomparador);}
		| error comparador expresion_aritmetica {yyerror($1.ival, "Error en expresion de la izquierda");}
		| expresion_aritmetica error expresion_aritmetica {yyerror($1.ival, "Comparador faltante o invalido");}
;

comparador	: '>' {varcomparador = ">";}
		| '<'{varcomparador = "<";}
		| COMP_IGUAL{varcomparador = "==";}
		| COMP_MENOR_IGUAL{varcomparador = "<=";}
		| COMP_MAYOR_IGUAL{varcomparador = ">=";}
		| DISTINTO{varcomparador = "<>";}
;

%%

  private Lexico lexico;
  public static int erroresS = 0;
  public static int nLinea = 1;
  public static Hashtable<String, Hashtable<String,Object>> tSimbolos = new Hashtable<>();
  private FileWriter txtErrores = null ;
  private static PrintWriter pw = null ;
  private FileWriter txtTokens = null ;
  private static PrintWriter pwTo = null ;
  private FileWriter txtTabla = null ;
  private static PrintWriter pwTa;
  private FileWriter txtEstruc = null ;
  private static PrintWriter pwEs = null ;
  private FileWriter txtAssembler = null ;
  public static PrintWriter pwAs = null ;
  private FileWriter txtPolaca = null ;
  private static PrintWriter pwPo = null ;
  private StringBuffer buffer = new StringBuffer();
  public static ArrayList<String> polacaEjecutable = new ArrayList<>();
  public static ArrayList<String> polacaFunciones = new ArrayList<>();
  private ArrayList<String> polaca = polacaFunciones;
  private ArrayList<Integer> pilaPolaca = new ArrayList<>();
  private String atributotipo;
  private String funcionactual;
  private ArrayList<String> tiposdefinidos = new ArrayList<>();
  private String varcomparador;
  public static Hashtable<String,Integer> indiceFunciones = new Hashtable<>();
  //private indiceFuncion = 1;
  private int nroMensaje = 1;
  private static char charAmbito = '@';
  private String ambitoInicial = "pp";
  private String ambito = charAmbito+ambitoInicial;
  private Hashtable<String,String> aDefinir = new Hashtable<>();
  private Hashtable<String,ArrayList<String>> funcionesLlamadas = new Hashtable<>();
  private boolean ejecutable = false;
  private String ubicacionDeCodigo;
  private String idParametro;
  private boolean estipodefinido;
  private int cantBreak = 0;
  private HashSet<Integer> indicesBreak = new HashSet();
  private int cantRepeat = 0;
  private boolean cantTry = false;
  private ArrayList<Integer> inicioAsignacion = new ArrayList<>();
  private ArrayList<Integer> pilaBreak = new ArrayList<>();


  public Parser(String ubicacionDeCodigo) throws IOException {
    lexico = new Lexico(ubicacionDeCodigo);
    this.ubicacionDeCodigo = ubicacionDeCodigo;
    txtErrores = new FileWriter(ubicacionDeCodigo.substring(0, ubicacionDeCodigo.indexOf('.')) + "Errores.txt");
    pw = new PrintWriter(txtErrores);
    txtTokens = new FileWriter(ubicacionDeCodigo.substring(0, ubicacionDeCodigo.indexOf('.')) + "Tokens.txt");
    pwTo = new PrintWriter(txtTokens);
    txtEstruc = new FileWriter(ubicacionDeCodigo.substring(0, ubicacionDeCodigo.indexOf('.')) + "Estructura.txt");
    pwEs = new PrintWriter(txtEstruc);
    txtTabla = new FileWriter(ubicacionDeCodigo.substring(0, ubicacionDeCodigo.indexOf('.')) + "TablaS.txt");
    pwTa = new PrintWriter(txtTabla);
    txtPolaca = new FileWriter(ubicacionDeCodigo.substring(0, ubicacionDeCodigo.indexOf('.')) + "Polaca.txt");
    pwPo = new PrintWriter(txtPolaca);
  }

  public int yylex() {
    TokenLexema token = this.lexico.yylex();
    yylval = new ParserVal((String)token.getLexema());
    yylval.ival = nLinea; //se utiliza la variable ival de la clase ParserVal para guardar el numero de linea en el que se detecto el token
    if (token.getLexema() != null){
		pwTo.println(token.getLexema()); // agrega el token entregado al archivo de tokens
		if (!tSimbolos.containsKey(token.getLexema())) {
			if (!tSimbolos.containsKey(token.getLexema()+ambito)){
				Hashtable<String,Object> aux = new Hashtable<>();
				if (token.getToken()==278)
				{
					aux.put("token",token.getToken());
					aux.put("valor",(String)token.getLexema());
					tSimbolos.put("msj"+nroMensaje+ambito,(Hashtable<String, Object>) aux.clone());
					nroMensaje++;
				}
				else
				{
					if (token.getToken() == 258 || token.getToken() == 259)
					{
						aux.put("token",token.getToken());
						tSimbolos.put((String)token.getLexema(),(Hashtable<String,Object>) aux.clone());
					}
					else
					{
						aux.put("token",token.getToken());
						tSimbolos.put((String)token.getLexema()+ambito,(Hashtable<String, Object>) aux.clone());
					}
				}
				
			}
		}
    }
	else
	{
		pwTo.println(token.getToken()); // agrega el token entregado al archivo de tokens
	}
    return (int) token.getToken();
  }

  private void yyerror(String s) {
    if (!s.equals("syntax error")) { // Ignora el error default de yacc.
      erroresS++;
      System.out.println("Error de sintaxis cerca de la línea " + nLinea + ": " + s);
      pw.println("Error de sintaxis cerca de la línea " + nLinea + ": " + s);
    }
  }

  private void yyerror(int linea, String s) {
    erroresS++;
    System.out.println("Linea: " + linea + " - Error: " + s);
    pw.println("Error de sintaxis cerca de la línea " + linea + ": " + s);
  }

  public static void yyerrorS(String s) {
    if (!s.equals("syntax error")) { // Ignora el error default de yacc.
      erroresS++;
      System.out.println("Error de sintaxis : " + s);
      pw.println("Error de sintaxis : " + s);
    }
  }

  private void yyerrorLex(String s) {
    // Agrega los errores lexicos que detecta la gramatica
    Lexico.erroresL++;
	System.out.println("Linea: " + nLinea + " - Error: " + s);
    pw.println("Error lexico cerca de la línea " + nLinea + ": " + s);
  }

  private void yyerrorSem(int linea, String s) {
    // Agrega los errores semanticos que detecta la gramatica
    erroresS++;
	System.out.println("Linea: " + linea + " - Error: " + s);
    pw.println("Error semántico cerca de la línea " + linea + ": " + s);
  }

    private void yyerrorSem(String s) {
    // Agrega los errores semanticos que detecta la gramatica
    erroresS++;
	System.out.println("Linea: " + nLinea + " - Error: " + s);
    pw.println("Error semántico cerca de la línea " + nLinea + ": " + s);
  }

  public void cerrarFicheros() throws IOException {
    if (txtErrores != null)
      txtErrores.close();
    if (txtTokens != null)
      txtTokens.close();
    if (txtTabla != null)
      txtTabla.close();
    if (txtEstruc != null)
      txtEstruc.close();
	if (txtAssembler != null && erroresS==0)
      txtAssembler.close();
    if (txtPolaca != null)
      txtPolaca.close();
  }

  public void crearAssembler() throws IOException 
  {
    txtAssembler = new FileWriter(ubicacionDeCodigo.substring(0, ubicacionDeCodigo.indexOf('.')) + "CodigoAssembler.asm");
    pwAs = new PrintWriter(txtAssembler);
  }

  public void escribirTablaS() {
    //escribe todos los datos que se tenga en la tabla de simbolos con sus lexemas
    tSimbolos.forEach((k, v) -> {
      pwTa.println("Simbolo: " + k + ", lexemas: " + v);
    });
  }

  private void agregarEstructura(String s) {
    //A medida que se detecta una estructura, se inserta en un stringbuffer que almacena estas
    buffer.insert(0, s + "\n");
  }

public void escribirEstruc() {
	//se escribe todo lo del string buffer que registra las estructuras, en el archivo
	pwEs.println(buffer.toString());
}

public void escribirPolaca() {
  pwPo.println("Polaca de funciones: \n");
  for (int i = 0; i< polacaFunciones.size(); i++)
  {
    pwPo.println(i+" "+polacaFunciones.get(i));
  }
  pwPo.println("Polaca ejecutable: \n");
  for (int j = polacaFunciones.size(); j < polacaFunciones.size() + polacaEjecutable.size(); j++)
  {
    pwPo.println(j+" "+polacaEjecutable.get(j-polacaFunciones.size()));
  }
}

public String indicePolacaToString(int valorPolaca){
	return "["+Integer.toString(valorPolaca)+"]";
}

public static Hashtable<String,Object> getTS(String variable){
	long count = variable.chars().filter(ch -> ch == '@').count();
	while (count >= 0)
	{
		if (tSimbolos.containsKey(variable) && tSimbolos.get(variable).containsKey("definida"))
		{
			return tSimbolos.get(variable);
		}
		else
		{
			if (count > 0)
				variable=variable.substring(0,variable.lastIndexOf(charAmbito));
		}
		count--;
	}
	return null;
}
