//### This file created by BYACC 1.8(/Java extension  1.15)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";






//#line 3 "gramatica.y"
package compi;
import java.lang.Math;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
//#line 26 "Parser.java"




public class Parser
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 500;  //maximum stack size
int statestk[] = new int[YYSTACKSIZE]; //state stack
int stateptr;
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
final void state_push(int state)
{
  try {
		stateptr++;
		statestk[stateptr]=state;
	 }
	 catch (ArrayIndexOutOfBoundsException e) {
     int oldsize = statestk.length;
     int newsize = oldsize * 2;
     int[] newstack = new int[newsize];
     System.arraycopy(statestk,0,newstack,0,oldsize);
     statestk = newstack;
     statestk[stateptr]=state;
  }
}
final int state_pop()
{
  return statestk[stateptr--];
}
final void state_drop(int cnt)
{
  stateptr -= cnt; 
}
final int state_peek(int relative)
{
  return statestk[stateptr-relative];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
final boolean init_stacks()
{
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//public class ParserVal is defined in ParserVal.java


String   yytext;//user variable to return contextual strings
ParserVal yyval; //used to return semantic vals from action routines
ParserVal yylval;//the 'lval' (result) I got from yylex()
ParserVal valstk[];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
void val_init()
{
  valstk=new ParserVal[YYSTACKSIZE];
  yyval=new ParserVal();
  yylval=new ParserVal();
  valptr=-1;
}
void val_push(ParserVal val)
{
  if (valptr>=YYSTACKSIZE)
    return;
  valstk[++valptr]=val;
}
ParserVal val_pop()
{
  if (valptr<0)
    return new ParserVal();
  return valstk[valptr--];
}
void val_drop(int cnt)
{
int ptr;
  ptr=valptr-cnt;
  if (ptr<0)
    return;
  valptr = ptr;
}
ParserVal val_peek(int relative)
{
int ptr;
  ptr=valptr-relative;
  if (ptr<0)
    return new ParserVal();
  return valstk[ptr];
}
final ParserVal dup_yyval(ParserVal val)
{
  ParserVal dup = new ParserVal();
  dup.ival = val.ival;
  dup.dval = val.dval;
  dup.sval = val.sval;
  dup.obj = val.obj;
  return dup;
}
//#### end semantic value section ####
public final static short ID=257;
public final static short CTE_LONG=258;
public final static short CTE_SINGLE=259;
public final static short IF=260;
public final static short THEN=261;
public final static short ELSE=262;
public final static short ENDIF=263;
public final static short FUNC=264;
public final static short RETURN=265;
public final static short BEGIN=266;
public final static short END=267;
public final static short BREAK=268;
public final static short PRINT=269;
public final static short REPEAT=270;
public final static short ASSIGN=271;
public final static short COMP_IGUAL=272;
public final static short COMP_MENOR_IGUAL=273;
public final static short COMP_MAYOR_IGUAL=274;
public final static short DISTINTO=275;
public final static short AND=276;
public final static short OR=277;
public final static short CADENA_MULTILINEA=278;
public final static short TYPEDEF=279;
public final static short POST=280;
public final static short TRY=281;
public final static short CATCH=282;
public final static short LONG=283;
public final static short SINGLE=284;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    3,    3,    3,    3,    2,    2,    2,
    2,    7,    9,    9,    9,    9,    9,    8,    8,   10,
   10,   10,   10,   10,   10,    4,    4,    4,    4,   17,
   17,   17,    5,    5,   18,   22,   19,   19,    6,    6,
    6,    6,    6,    6,   24,   11,   11,   11,   11,   11,
   12,   12,   12,   12,   12,   12,   12,   12,   12,   12,
   12,   12,   12,   12,   27,   30,   28,   29,   13,   13,
   13,   13,   13,   14,   32,   34,   35,   33,   15,   15,
   15,   37,   38,   38,   38,   38,   38,   39,   16,   16,
   16,   25,   25,   25,   25,   25,   25,   20,   20,   21,
   21,   21,   21,   21,   21,   21,   21,   40,   40,   41,
   23,   26,   26,   26,   42,   42,   42,   43,   43,   43,
   43,   43,   44,   44,   44,   36,   36,   36,   36,   31,
   31,   31,   31,   45,   45,   46,   46,   46,   47,   47,
   47,   47,   47,   47,
};
final static short yylen[] = {                            2,
    4,    2,    1,    1,    1,    1,    2,    3,    2,    2,
    3,    1,    3,    1,    3,    2,    2,    2,    1,    1,
    1,    1,    1,    1,    2,    3,    3,    2,    2,    3,
    1,    1,    8,    9,    4,    3,    1,    0,    3,    4,
    4,    4,    4,    5,    3,    4,    3,    3,    4,    4,
   10,    8,    7,    7,    7,    7,    7,    7,    3,    9,
    9,    9,    9,    9,    1,    1,    1,    1,    5,    4,
    4,    4,    3,    6,    5,    3,    1,    1,    5,    4,
    3,    1,    1,    1,    1,    2,    1,    3,    1,    1,
    1,    5,    4,    4,    4,    4,    4,    2,    1,    6,
    5,    5,    5,    5,    6,    3,    5,    2,    1,    1,
    1,    3,    3,    1,    3,    3,    1,    1,    1,    1,
    2,    2,    4,    3,    3,    1,    2,    1,    2,    3,
    1,    3,    3,    3,    1,    3,    3,    3,    1,    1,
    1,    1,    1,    1,
};
final static short yydefred[] = {                         0,
    0,    0,    0,    0,   91,    0,   89,   90,    0,    3,
    4,    5,    6,    0,    0,    0,    0,    0,    7,    0,
    0,    0,    0,   12,    1,    2,    0,   31,    0,   28,
   32,    0,    0,    0,    0,    0,    0,    0,   27,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   82,    0,
   19,   20,   21,   22,   23,   24,    0,    0,   10,    0,
   36,   26,    0,    0,    0,    0,  111,    0,    0,    0,
   39,   43,    0,   42,   41,   40,    0,    0,  126,  128,
    0,    0,    0,  119,    0,  117,  120,    0,    0,    0,
    0,    0,    0,  135,   25,    0,    0,    0,    0,   11,
   18,    0,    0,    0,   83,   84,   85,   87,    0,    8,
   30,    0,  108,   35,    0,    0,    0,    0,   44,    0,
    0,  110,    0,    0,   48,    0,    0,  127,  129,  122,
   47,    0,    0,    0,    0,  141,  142,  143,  144,    0,
   59,  139,  140,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   73,    0,    0,    0,    0,    0,    0,
   81,   86,    0,    0,    0,    0,   93,   96,    0,   95,
   94,   50,  125,    0,  124,   49,   46,    0,    0,  115,
  116,    0,  132,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  133,  134,   70,   72,    0,   71,    0,    0,
    0,    0,   80,    0,    0,    0,   92,  123,    0,    0,
   67,    0,    0,    0,    0,    0,    0,   69,    0,   76,
   78,    0,    0,    0,    0,   88,   79,    0,    0,    0,
   98,    0,    0,   16,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   74,    0,   33,    0,
    0,    0,    0,   58,   13,   15,   68,    0,   54,    0,
   55,    0,   57,    0,    0,   56,    0,   53,   75,  106,
    0,    0,    0,   34,    0,    0,    0,   52,    0,    0,
    0,    0,    0,    0,    0,   61,   62,    0,   64,   63,
   60,  101,    0,  103,  104,    0,  102,   51,  105,  100,
};
final static short yydgoto[] = {                          2,
    9,   25,   10,   11,   12,   13,   27,   50,  211,  212,
   52,   53,   54,   55,   56,   14,   32,   15,   34,  205,
  230,   16,   66,   17,   38,   90,   91,  213,  258,  242,
   92,  159,  222,  160,  225,   84,   57,  109,  164,  122,
  123,   85,   86,   87,   93,   94,  144,
};
final static short yysindex[] = {                      -236,
  -10,    0, -209,  -49,    0,   29,    0,    0,  -93,    0,
    0,    0,    0,  -35, -209,   -1, -154,   32,    0, -154,
  -58, -154,  134,    0,    0,    0,  246,    0, -169,    0,
    0,    9, -209, -172, -171,   76,  -32,   38,    0,   59,
  -16,   63,   68,  -44,  -20,   98,  -26,  114,    0,  262,
    0,    0,    0,    0,    0,    0,  242,  124,    0,  268,
    0,    0,  -90,  357,  -86,  133,    0, -171,  -40, -171,
    0,    0,  117,    0,    0,    0,  127,  -38,    0,    0,
  175, -110,  125,    0,   58,    0,    0,  135,   95,  115,
  147,  -84, -221,    0,    0,  137,  157,  -37,  -56,    0,
    0,  165,  169,   -9,    0,    0,    0,    0,  -21,    0,
    0,  291,    0,    0,  218,  -36,  225,  233,    0,  144,
  -34,    0,  238,  224,    0,  159,  -38,    0,    0,    0,
    0,  127,  127,  127,  127,    0,    0,    0,    0,  166,
    0,    0,    0,  127,   73,   27,  -30,  127,  127,   35,
  166,  166,  166,    0,  251,  -25,  259,   30,  282,  281,
    0,    0,  357,  284,   -9,  127,    0,    0,  321,    0,
    0,    0,    0,  329,    0,    0,    0,   58,   58,    0,
    0,   57,    0,  -12,  131,  297,  297,  206,  -12,  -12,
  297,  101,    0,    0,    0,    0,  318,    0,    5,  297,
  171,  313,    0,  334, -250,  393,    0,    0,  297,  357,
    0,  317, -191, -112,  297,  -83,   45,    0, -150,    0,
    0,  341,  145,  -84,  342,    0,    0,  346,  -14,  139,
    0,  354,  335,    0,  340,  297,  355,  297,  367,    0,
  -27,  174,  297,  381,  297,  383,    0,    5,    0,  385,
  171,  116,  386,    0,    0,    0,    0,  196,    0,  197,
    0,  297,    0,  392,  199,    0,  202,    0,    0,    0,
  -23,  140,  -22,    0,  409,  410,   -8,    0,  411,  420,
  424,   93,  425,  -18,  427,    0,    0,  430,    0,    0,
    0,    0,  432,    0,    0,  433,    0,    0,    0,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  219,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  198,  227,    0,    0,    0,    0,    0,    0,    0,
 -118,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  500,
    0,    0,    0,    0,  464,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   -5,    0,    0,
    0,    0,    0,    0,   39,    0,    0,    0,    0,    0,
    0,  -28,  100,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   17,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  448,    0,
    0,    0,    0,    0,    0,    0,    0,   61,   83,    0,
    0,    0,    0,    4,    0,    0,    0,    0,   26,   48,
    0,  103,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0, -184,    0,    0,    0,    0,
    0,  -53,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  449,    0,    0,    0,    0,    0,    0,
    0,    0,   14,    0,    0,    0,    0,    0,    0,   36,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  250,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
  494,    0,   19,    0,    0,    0,    0,  278,  235,  389,
  461,  463,  466,  469,    0,  426,    0,    0,    0,    0,
    0,    0,    0,    0,  436,   34,  444, -122, -216,    0,
 -120,    0,    0,    0,    0, -187,    0,    0,  356,  499,
  418,  188,  192,  458,  390,  -15,  454,
};
final static int YYTABLESIZE=638;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                        116,
   82,  121,   41,  157,  168,   14,  173,   70,   31,   19,
  188,  220,   65,   98,   39,  197,  228,  281,  285,   89,
    1,  260,  296,   30,   82,  251,  265,   26,  267,  229,
  132,  263,  133,  196,  152,  118,  118,  118,   35,  118,
  295,  118,   74,  252,  137,  277,    4,    5,    3,  219,
  289,   26,   63,  118,  118,  153,  118,  121,  121,  121,
  269,  121,  137,  121,  214,  216,  138,   62,  217,    6,
  236,  237,   17,    7,    8,  121,  121,   83,  121,  114,
  224,  114,   99,  114,  138,    5,  232,   61,  136,   22,
   39,   83,  241,   64,   67,   99,   71,  114,  114,  134,
  114,  112,    5,  112,  135,  112,  136,  128,  129,   36,
  120,    7,    8,  185,  126,   68,  143,   72,  142,  112,
  112,   75,  112,  113,  183,  113,   76,  113,    7,    8,
  271,  273,  143,  293,  142,  146,  193,  194,   45,   82,
  131,  113,  113,  130,  113,   45,  127,  128,  129,  238,
  239,  284,  143,   99,  142,  272,   95,  132,  131,  133,
   82,  130,   23,    5,   45,   45,  111,  132,   82,  133,
  113,   82,   24,  114,  143,  119,  142,  184,  243,  244,
  283,  189,  190,  131,   82,    6,  132,  150,  133,    7,
    8,  151,   19,  141,  143,  154,  142,  155,    5,  206,
  158,  132,  172,  133,  143,   36,  142,   18,   14,   14,
   82,   77,   78,   79,   80,   82,    5,  177,    5,   82,
    5,   28,    5,  161,    7,    8,   81,  162,   29,   96,
  187,   69,   65,  125,  262,   88,   78,   79,   80,   73,
  156,  250,    7,    8,    7,    8,    7,    8,    7,    8,
  118,   97,  151,  151,  288,  118,  163,  151,  167,  137,
  165,  118,   79,   80,  137,  170,  118,  118,  118,  118,
  118,  118,  121,  171,  118,   17,   17,  121,  175,  137,
  137,  138,  176,  121,   20,   21,  138,  186,  121,  121,
  121,  121,  121,  121,  114,  191,  121,   67,   66,  114,
  199,  138,  138,  136,   60,  114,  245,  246,  136,  195,
  114,  114,  114,  114,  114,  114,  112,  198,  114,  178,
  179,  112,  200,  136,  136,  180,  181,  112,  136,  137,
  138,  139,  112,  112,  112,  112,  112,  112,  113,  201,
  112,  112,  203,  113,  136,  137,  138,  139,  140,  113,
  145,   78,   79,   80,  113,  113,  113,  113,  113,  113,
  131,  207,  113,  130,  136,  137,  138,  139,  140,  208,
  148,  223,   78,   79,   80,  131,  218,  153,  130,   77,
   78,   79,   80,   78,   79,   80,  136,  137,  138,  139,
   44,  209,  227,   45,   81,  282,   78,   79,   80,  247,
  248,   46,   47,   48,  249,  253,  136,  137,  138,  139,
  140,   51,  254,  259,   49,   51,  136,  137,  138,  139,
  140,  182,   78,   79,   80,  261,  223,   78,   79,   80,
  124,   78,   79,   80,  221,  132,  264,  133,  101,  266,
  202,  268,   37,  270,  274,   37,   37,   37,  101,  240,
  278,  231,   51,   29,   29,   40,   42,   43,  275,  276,
   65,  279,   58,   29,  280,   45,  215,  286,  287,  290,
  257,  210,  257,   46,   47,   48,   29,  257,  291,  257,
   29,   29,  292,  294,   38,  297,   49,  233,  298,  235,
  299,  300,   37,  115,  117,  118,  257,  102,   58,    9,
  101,   45,   58,   65,  109,   45,   97,   77,   33,  103,
   47,   48,   59,   46,   47,   48,  107,  105,   58,  106,
  204,   45,  107,  104,   58,  108,   49,   45,  100,   46,
   47,   48,  147,   67,  110,   46,   47,   48,  174,  130,
  192,  169,   49,  149,    0,    0,   65,   58,   49,    0,
   45,   51,   65,   58,    0,  166,   45,    0,   46,   47,
   48,    0,  210,    0,   46,   47,   48,    0,    0,   58,
    0,   49,   45,   58,    0,    0,   45,   49,    0,  226,
   46,   47,   48,  234,   46,   47,   48,    0,    0,    0,
  101,   58,    0,   49,   45,    0,   58,   49,   51,   45,
   51,  255,   46,   47,   48,    0,  256,   46,   47,   48,
    0,    0,    0,   58,    0,   49,   45,    0,    0,    0,
   49,  101,    0,  101,   46,   47,   48,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   49,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         40,
   45,   40,   61,   41,   41,   59,   41,   40,   44,   59,
   41,  199,   41,   40,   59,   41,  267,   41,   41,   40,
  257,  238,   41,   59,   45,   40,  243,    9,  245,  280,
   43,   59,   45,   59,  256,   41,   42,   43,   40,   45,
   59,   47,   59,   58,   41,  262,  256,  257,   59,   45,
   59,   33,   44,   59,   60,  277,   62,   41,   42,   43,
  248,   45,   59,   47,  187,  188,   41,   59,  191,  279,
  262,  263,   59,  283,  284,   59,   60,   44,   62,   41,
  201,   43,  267,   45,   59,  257,  209,  257,   41,   61,
   59,   58,  215,  266,   59,  280,   59,   59,   60,   42,
   62,   41,  257,   43,   47,   45,   59,  258,  259,  264,
   77,  283,  284,   41,   81,   40,   60,   59,   62,   59,
   60,   59,   62,   41,  140,   43,   59,   45,  283,  284,
  251,  252,   60,   41,   62,   41,  152,  153,  257,   45,
   41,   59,   60,   41,   62,  264,  257,  258,  259,  262,
  263,  272,   60,   40,   62,   40,   59,   43,   59,   45,
   45,   59,  256,  257,  283,  284,  257,   43,   45,   45,
  257,   45,  266,   41,   60,   59,   62,  144,  262,  263,
   41,  148,  149,   59,   45,  279,   43,   41,   45,  283,
  284,  276,   59,   59,   60,   59,   62,   41,  257,  166,
  257,   43,   59,   45,   60,  264,   62,  257,  262,  263,
   45,  256,  257,  258,  259,   45,  257,   59,  257,   45,
  257,  257,  257,   59,  283,  284,  271,   59,  264,  256,
  261,  264,  261,   59,  262,  256,  257,  258,  259,  256,
  278,  256,  283,  284,  283,  284,  283,  284,  283,  284,
  256,  278,  276,  276,  263,  261,  266,  276,   41,  256,
  282,  267,  258,  259,  261,   41,  272,  273,  274,  275,
  276,  277,  256,   41,  280,  262,  263,  261,   41,  276,
  277,  256,   59,  267,  256,  257,  261,  261,  272,  273,
  274,  275,  276,  277,  256,  261,  280,  262,  263,  261,
  271,  276,  277,  256,   27,  267,  262,  263,  261,   59,
  272,  273,  274,  275,  276,  277,  256,   59,  280,  132,
  133,  261,   41,  276,  277,  134,  135,  267,  272,  273,
  274,  275,  272,  273,  274,  275,  276,  277,  256,   59,
  280,   64,   59,  261,  272,  273,  274,  275,  276,  267,
  256,  257,  258,  259,  272,  273,  274,  275,  276,  277,
  261,   41,  280,  261,  272,  273,  274,  275,  276,   41,
  256,  256,  257,  258,  259,  276,   59,  277,  276,  256,
  257,  258,  259,  257,  258,  259,  272,  273,  274,  275,
  257,  261,   59,  260,  271,  256,  257,  258,  259,   59,
   59,  268,  269,  270,   59,  267,  272,  273,  274,  275,
  276,   23,   59,   59,  281,   27,  272,  273,  274,  275,
  276,  256,  257,  258,  259,   59,  256,  257,  258,  259,
  256,  257,  258,  259,  200,   43,  263,   45,   50,   59,
  163,   59,   17,   59,   59,   20,   21,   22,   60,  215,
   59,   59,   64,  256,  257,   20,   21,   22,  263,  263,
   35,  263,  257,  266,  263,  260,  261,   59,   59,   59,
  236,  266,  238,  268,  269,  270,  279,  243,   59,  245,
  283,  284,   59,   59,  266,   59,  281,  210,   59,  212,
   59,   59,  266,   68,   69,   70,  262,  256,  257,    0,
  112,  260,  257,   78,   41,  260,   59,   59,   15,  268,
  269,  270,  267,  268,  269,  270,  267,   57,  257,   57,
  165,  260,   57,  282,  257,   57,  281,  260,  267,  268,
  269,  270,   89,   35,  267,  268,  269,  270,  121,   82,
  151,  116,  281,   90,   -1,   -1,  121,  257,  281,   -1,
  260,  163,  127,  257,   -1,  265,  260,   -1,  268,  269,
  270,   -1,  266,   -1,  268,  269,  270,   -1,   -1,  257,
   -1,  281,  260,  257,   -1,   -1,  260,  281,   -1,  267,
  268,  269,  270,  267,  268,  269,  270,   -1,   -1,   -1,
  202,  257,   -1,  281,  260,   -1,  257,  281,  210,  260,
  212,  267,  268,  269,  270,   -1,  267,  268,  269,  270,
   -1,   -1,   -1,  257,   -1,  281,  260,   -1,   -1,   -1,
  281,  233,   -1,  235,  268,  269,  270,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  281,
};
}
final static short YYFINAL=2;
final static short YYMAXTOKEN=284;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,"'('","')'","'*'","'+'","','",
"'-'",null,"'/'",null,null,null,null,null,null,null,null,null,null,"':'","';'",
"'<'","'='","'>'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,"ID","CTE_LONG","CTE_SINGLE","IF","THEN","ELSE",
"ENDIF","FUNC","RETURN","BEGIN","END","BREAK","PRINT","REPEAT","ASSIGN",
"COMP_IGUAL","COMP_MENOR_IGUAL","COMP_MAYOR_IGUAL","DISTINTO","AND","OR",
"CADENA_MULTILINEA","TYPEDEF","POST","TRY","CATCH","LONG","SINGLE",
};
final static String yyrule[] = {
"$accept : program",
"program : ID ';' sentencias_declarativas bloque_programa",
"sentencias_declarativas : sentencias_declarativas declaracion",
"sentencias_declarativas : declaracion",
"declaracion : declaracion_tipo",
"declaracion : funcion",
"declaracion : tipo_definido",
"declaracion : error ';'",
"bloque_programa : comienzo_bloque_programa sentencias_ejecutables END",
"bloque_programa : comienzo_bloque_programa sentencias_ejecutables",
"bloque_programa : comienzo_bloque_programa END",
"bloque_programa : error sentencias_ejecutables END",
"comienzo_bloque_programa : BEGIN",
"bloque_de_sentencias : BEGIN sentencias_ejecutables END",
"bloque_de_sentencias : ejecutable",
"bloque_de_sentencias : ejecutable sentencias_ejecutables END",
"bloque_de_sentencias : ejecutable END",
"bloque_de_sentencias : BEGIN sentencias_ejecutables",
"sentencias_ejecutables : sentencias_ejecutables ejecutable",
"sentencias_ejecutables : ejecutable",
"ejecutable : asignacion",
"ejecutable : seleccion",
"ejecutable : mensaje",
"ejecutable : repeticion",
"ejecutable : trycatch",
"ejecutable : BREAK ';'",
"declaracion_tipo : tipo lista_de_variables ';'",
"declaracion_tipo : error ID ';'",
"declaracion_tipo : tipo ';'",
"declaracion_tipo : tipo lista_de_variables",
"lista_de_variables : lista_de_variables ',' ID",
"lista_de_variables : ID",
"lista_de_variables : ','",
"funcion : cabeza_funcion sentencias_declarativas_fn BEGIN sentencias_ejecutables RETURN retorno END ';'",
"funcion : cabeza_funcion sentencias_declarativas_fn BEGIN sentencias_ejecutables RETURN retorno postcondicion END ';'",
"cabeza_funcion : cuerpo_fn '(' parametro_funcion ')'",
"cuerpo_fn : tipo FUNC ID",
"sentencias_declarativas_fn : sentencias_declarativas",
"sentencias_declarativas_fn :",
"tipo_definido : encabezado_tipo_definido encabezado_de_funcion ';'",
"tipo_definido : TYPEDEF '=' encabezado_de_funcion ';'",
"tipo_definido : TYPEDEF ID encabezado_de_funcion ';'",
"tipo_definido : TYPEDEF ID '=' ';'",
"tipo_definido : TYPEDEF error encabezado_de_funcion ';'",
"tipo_definido : TYPEDEF ID '=' error ';'",
"encabezado_tipo_definido : TYPEDEF ID '='",
"asignacion : ID ASSIGN expresion_aritmetica ';'",
"asignacion : ID expresion_aritmetica ';'",
"asignacion : ID ASSIGN ';'",
"asignacion : ID ASSIGN error ';'",
"asignacion : ID error expresion_aritmetica ';'",
"seleccion : IF '(' cpo_if ')' THEN cpo_then ELSE cpo_else ENDIF ';'",
"seleccion : IF '(' cpo_if ')' THEN cpo_then_simple ENDIF ';'",
"seleccion : IF cpo_if ')' THEN cpo_then ENDIF ';'",
"seleccion : IF '(' ')' THEN cpo_then ENDIF ';'",
"seleccion : IF '(' cpo_if THEN cpo_then ENDIF ';'",
"seleccion : IF '(' cpo_if ')' cpo_then ENDIF ';'",
"seleccion : IF '(' cpo_if ')' THEN cpo_then ';'",
"seleccion : IF '(' error ')' THEN cpo_then ';'",
"seleccion : IF error ';'",
"seleccion : IF cpo_if ')' THEN cpo_then ELSE cpo_else ENDIF ';'",
"seleccion : IF '(' ')' THEN cpo_then ELSE cpo_else ENDIF ';'",
"seleccion : IF '(' cpo_if THEN cpo_then ELSE cpo_else ENDIF ';'",
"seleccion : IF '(' cpo_if ')' cpo_then ELSE cpo_else ENDIF ';'",
"seleccion : IF '(' cpo_if ')' THEN cpo_then ELSE cpo_else ';'",
"cpo_if : condicion",
"cpo_then_simple : bloque_de_sentencias",
"cpo_then : bloque_de_sentencias",
"cpo_else : bloque_de_sentencias",
"mensaje : PRINT '(' CADENA_MULTILINEA ')' ';'",
"mensaje : PRINT CADENA_MULTILINEA ')' ';'",
"mensaje : PRINT '(' ')' ';'",
"mensaje : PRINT '(' CADENA_MULTILINEA ';'",
"mensaje : PRINT error ';'",
"repeticion : REPEAT '(' cond_repeat ')' cpo_repeat ';'",
"cond_repeat : asignacion_repeat ';' condicion_repeat ';' constante",
"asignacion_repeat : ID ASSIGN constante",
"condicion_repeat : condicion",
"cpo_repeat : bloque_de_sentencias",
"trycatch : inicio_try cpo_try CATCH cpo_catch ';'",
"trycatch : inicio_try CATCH cpo_catch ';'",
"trycatch : inicio_try error ';'",
"inicio_try : TRY",
"cpo_try : asignacion",
"cpo_try : seleccion",
"cpo_try : mensaje",
"cpo_try : BREAK ';'",
"cpo_try : repeticion",
"cpo_catch : BEGIN sentencias_ejecutables END",
"tipo : LONG",
"tipo : SINGLE",
"tipo : ID",
"encabezado_de_funcion : tipo FUNC '(' tipo ')'",
"encabezado_de_funcion : FUNC '(' tipo ')'",
"encabezado_de_funcion : tipo '(' tipo ')'",
"encabezado_de_funcion : tipo FUNC tipo ')'",
"encabezado_de_funcion : tipo FUNC '(' ')'",
"encabezado_de_funcion : tipo FUNC '(' tipo",
"retorno : expresion_aritmetica ';'",
"retorno : expresion_aritmetica",
"postcondicion : POST ':' '(' condicion ')' ';'",
"postcondicion : POST '(' condicion ')' ';'",
"postcondicion : POST ':' condicion ')' ';'",
"postcondicion : POST ':' '(' ')' ';'",
"postcondicion : POST ':' '(' condicion ';'",
"postcondicion : POST ':' '(' error ')' ';'",
"postcondicion : POST error ';'",
"postcondicion : POST ':' '(' condicion ')'",
"parametro : tipo ID",
"parametro : tipo",
"parametro_invocacion : parametro",
"parametro_funcion : parametro",
"expresion_aritmetica : expresion_aritmetica '+' termino",
"expresion_aritmetica : expresion_aritmetica '-' termino",
"expresion_aritmetica : termino",
"termino : termino '*' factor",
"termino : termino '/' factor",
"termino : factor",
"factor : ID",
"factor : constante",
"factor : invocacion",
"factor : '-' ID",
"factor : '-' invocacion",
"invocacion : ID '(' parametro_invocacion ')'",
"invocacion : ID parametro_invocacion ')'",
"invocacion : ID '(' ')'",
"constante : CTE_LONG",
"constante : '-' CTE_LONG",
"constante : CTE_SINGLE",
"constante : '-' CTE_SINGLE",
"condicion : condicion AND expresion_booleana",
"condicion : expresion_booleana",
"condicion : error AND comparacion",
"condicion : expresion_booleana error comparacion",
"expresion_booleana : expresion_booleana OR comparacion",
"expresion_booleana : comparacion",
"comparacion : expresion_aritmetica comparador expresion_aritmetica",
"comparacion : error comparador expresion_aritmetica",
"comparacion : expresion_aritmetica error expresion_aritmetica",
"comparador : '>'",
"comparador : '<'",
"comparador : COMP_IGUAL",
"comparador : COMP_MENOR_IGUAL",
"comparador : COMP_MAYOR_IGUAL",
"comparador : DISTINTO",
};

//#line 781 "gramatica.y"

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
//#line 798 "Parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
int yyparse()
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  val_push(yylval);     //save empty value
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable[yyn]);
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0)   //check for under & overflow here
            {
            yyerror("stack underflow. aborting...");  //note lower case 's'
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0)   //check for under & overflow here
              {
              yyerror("Stack underflow. aborting...");  //capital 'S'
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    yyval = dup_yyval(yyval); //duplicate yyval if ParserVal is used as semantic value
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 21 "gramatica.y"
{ 
															agregarEstructura("Se termino de compilar el programa correctamente");}
break;
case 6:
//#line 34 "gramatica.y"
{agregarEstructura("DECLARACION DE TYPEDEF en linea " + val_peek(0).ival);}
break;
case 7:
//#line 35 "gramatica.y"
{yyerror(val_peek(1).ival,"Error en la declaracion de variables");}
break;
case 9:
//#line 39 "gramatica.y"
{yyerror(val_peek(0).ival,"Falta el END");}
break;
case 10:
//#line 40 "gramatica.y"
{yyerror(val_peek(1).ival,"El bloque del programa no puede estar vacio");}
break;
case 11:
//#line 41 "gramatica.y"
{yyerror(val_peek(2).ival, "Falta el BEGIN");}
break;
case 12:
//#line 44 "gramatica.y"
{   
										/*Cambiamos de polacaFunciones a polacaEjecutable al empezar con el bloque de programa*/
										polaca = polacaEjecutable;
										ejecutable = true;
										/*Verificamos que las funciones que fueron invocadas dentro de la declaracion de una funcion y todavía no habían sido declaradas, hayan sido declaradas*/
										aDefinir.forEach((k, v) -> {
											if (!tSimbolos.containsKey(k))
											{
												yyerrorSem("Una funcion utilizó una funcion no declarada ");
											}
										});
								}
break;
case 15:
//#line 60 "gramatica.y"
{yyerror(val_peek(2).ival,"Falta el BEGIN");}
break;
case 16:
//#line 61 "gramatica.y"
{yyerror(val_peek(1).ival,"Falta el BEGIN");}
break;
case 17:
//#line 62 "gramatica.y"
{yyerror(val_peek(1).ival,"Falta el END");}
break;
case 20:
//#line 69 "gramatica.y"
{}
break;
case 25:
//#line 74 "gramatica.y"
{ if (cantRepeat == 0)
					  {
						 yyerror(val_peek(1).ival, "No puede haber un break si no se encuentra dentro del repeat");
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
break;
case 26:
//#line 90 "gramatica.y"
{estipodefinido = false;}
break;
case 27:
//#line 91 "gramatica.y"
{yyerror(val_peek(2).ival,"Declaracion de tipo faltante en linea " + val_peek(2).ival);}
break;
case 28:
//#line 92 "gramatica.y"
{yyerror(val_peek(1).ival,"Falta el id de la variable en linea " + val_peek(1).ival);}
break;
case 29:
//#line 93 "gramatica.y"
{yyerror(val_peek(0).ival,"; faltante");}
break;
case 30:
//#line 96 "gramatica.y"
{agregarEstructura("DECLARACION MULTIPLE en linea " + val_peek(2).ival);
												/*Verificamos que la variable no haya sido redeclarada*/
												if (tSimbolos.get(val_peek(0).sval+ambito).containsKey("definida"))
												{
													yyerror(val_peek(2).ival,"Variable redeclarada");
												}
												/*Si la variable se está declarando por primera vez*/
												else
												{
												/*Le añadimos los atributos a la variable, el tipo de atributo, fue seteado en la regla "tipo" que se realizó anteriormente*/
   												tSimbolos.get(val_peek(0).sval+ambito).put("tipo",atributotipo);
												tSimbolos.get(val_peek(0).sval+ambito).put("uso","variable");
												tSimbolos.get(val_peek(0).sval+ambito).put("definida",true);
												/*Si el tipo de atributo no es ni single ni long, obtenemos el tipo de retorno del tipo definido para poder hacer la verificacion de tipos compatibles*/
												if (!atributotipo.equals("LONG") && !atributotipo.equals("SINGLE")){
													String aux;
													aux = atributotipo;
													aux = aux.substring(0,aux.indexOf(" "));
													tSimbolos.get(val_peek(0).sval+ambito).put("tipo_parametro",aux);
													aux = atributotipo;
													aux = atributotipo.substring(aux.indexOf(" ")+1,aux.length());
													tSimbolos.get(val_peek(0).sval+ambito).put("retorno",aux);
												 }
												}
												if (estipodefinido)
												{
													indiceFunciones.put(val_peek(0).sval,indiceFunciones.size()+1);
												}
											 }
break;
case 31:
//#line 125 "gramatica.y"
{agregarEstructura("DECLARACION SIMPLE en linea " + val_peek(0).ival);
					/*Verificamos que la variable no haya sido redeclarada*/
					if (tSimbolos.get(val_peek(0).sval+ambito).containsKey("definida"))
					{
						yyerror(val_peek(0).ival,"Variable redeclarada");
					}
					else
					{
						/*Le añadimos los atributos a la variable*/
						tSimbolos.get(val_peek(0).sval+ambito).put("tipo",atributotipo);
						tSimbolos.get(val_peek(0).sval+ambito).put("uso","variable");
						tSimbolos.get(val_peek(0).sval+ambito).put("definida",true);
						/*Si el tipo de atributo no es ni single ni long, obtenemos el tipo de retorno del tipo definido para poder hacer la verificacion de tipos compatibles*/
						if (!atributotipo.equals("LONG") && !atributotipo.equals("SINGLE")){
							String aux;
							aux = atributotipo;
							aux = aux.substring(0,aux.indexOf(" "));
							tSimbolos.get(val_peek(0).sval+ambito).put("tipo_parametro",aux);
							aux = atributotipo;
							aux = atributotipo.substring(aux.indexOf(" ")+1,aux.length());
							tSimbolos.get(val_peek(0).sval+ambito).put("retorno",aux);
						}
					}
					if (estipodefinido)
					{
						indiceFunciones.put(val_peek(0).sval,indiceFunciones.size()+1);
					}
				}
break;
case 32:
//#line 153 "gramatica.y"
{yyerror(val_peek(0).ival,"Parametro faltante luego de la ',' ");}
break;
case 33:
//#line 156 "gramatica.y"
{agregarEstructura("DECLARACION DE FUNCION en linea " + val_peek(7).ival + " hasta linea " + val_peek(0).ival);
																											/*Verificamos que el tipo del retorno sea el mismo que el tipo de retorno de la funcion*/
																											if (getTS(val_peek(2).sval+ambito) != null)
																											{
																												if (!getTS(val_peek(2).sval+ambito).get("tipo").equals(val_peek(7).sval))
																												{
																													yyerror(val_peek(2).ival,"Retorno incorrecto de tipo");
																												}
																											}
																											/*ME QUEDE ACA PQ ME CANSE*/
																											if (funcionactual != null)
																											{
																												getTS(funcionactual).put("tipo",(String)tSimbolos.get(funcionactual).get("tipo_parametro")+" "+(String)tSimbolos.get(funcionactual).get("retorno"));
																												indiceFunciones.put(funcionactual,indiceFunciones.size()+1);
																												getTS(funcionactual).put("postcondicion","false");
																											 	funcionactual = null;
																											}
																											ambito=ambito.substring(0,ambito.lastIndexOf(charAmbito));
																											
																											}
break;
case 34:
//#line 176 "gramatica.y"
{agregarEstructura("DECLARACION DE FUNCION CON POSTCONDICION en linea " + val_peek(8).ival + " hasta linea " + val_peek(0).ival);
																														/*Verificamos que el tipo del retorno sea el mismo que el tipo de retorno de la funcion*/
																														if (getTS(val_peek(3).sval+ambito) != null)
																														{
																															if (!getTS(val_peek(3).sval+ambito).get("tipo").equals(val_peek(8).sval))
																															{
																																yyerror(val_peek(3).ival,"Retorno incorrecto de tipo");
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
break;
case 35:
//#line 215 "gramatica.y"
{
												polaca.add("FN");
											}
break;
case 36:
//#line 220 "gramatica.y"
{ 	if (tSimbolos.containsKey(val_peek(0).sval))
								{
									yyerror(val_peek(2).ival, "Funcion redeclarada");
								}	
								/*if (!tSimbolos.containsKey($3.sval+ambito))*/
								else
								{
									tSimbolos.put(val_peek(0).sval,new Hashtable<>());
									Hashtable<String,Object> aux = new Hashtable<String,Object>();
									aux = (Hashtable<String, Object>) (tSimbolos.get(val_peek(0).sval+ambito).clone());
									for (Map.Entry<String, Object> keyValue: aux.entrySet()) {
										tSimbolos.get(val_peek(0).sval).put(keyValue.getKey(),keyValue.getValue());
									}
									tSimbolos.remove(val_peek(0).sval+ambito);
									if (tSimbolos.get(val_peek(0).sval).containsKey("definida"))
									{
										yyerror(val_peek(0).ival,"Funcion redeclarada");
									}
									else
									{
										tSimbolos.get(val_peek(0).sval).put("uso","funcion");
										tSimbolos.get(val_peek(0).sval).put("definida",true);
									}
									tSimbolos.get(val_peek(0).sval).put("retorno",val_peek(2).sval);
									funcionactual = val_peek(0).sval;
									polaca.add(val_peek(0).sval);
								}
								ambito=ambito+charAmbito+val_peek(0).sval.toString();
							}
break;
case 39:
//#line 256 "gramatica.y"
{funcionactual = null;}
break;
case 40:
//#line 257 "gramatica.y"
{yyerror(val_peek(3).ival,"Falta el ID del TYPEDEF");}
break;
case 41:
//#line 258 "gramatica.y"
{yyerror(val_peek(2).ival,"Falta el '=' del TYPEDEF");}
break;
case 42:
//#line 259 "gramatica.y"
{yyerror(val_peek(1).ival,"Falta el encabezado de funcion del TYPEDEF");}
break;
case 43:
//#line 260 "gramatica.y"
{yyerror(val_peek(3).ival,"Error en la declaracion del TYPEDEF");}
break;
case 44:
//#line 261 "gramatica.y"
{yyerror(val_peek(4).ival,"Error en el encabezado de funcion del TYPEDEF");}
break;
case 45:
//#line 265 "gramatica.y"
{if (tSimbolos.get(val_peek(1).sval+ambito).containsKey("definida"))
															{
																yyerror(val_peek(2).ival,"Variable redeclarada");
															}
												else{
															tSimbolos.get(val_peek(1).sval+ambito).put("uso","tipo_definido");
															tSimbolos.get(val_peek(1).sval+ambito).put("definida",true);
															/*Añadimos a la estructura de tiposdefinidos, el id del tipo definido*/
															tiposdefinidos.add(val_peek(1).sval);
															funcionactual = val_peek(1).sval+ambito;}
														}
break;
case 46:
//#line 278 "gramatica.y"
{agregarEstructura("ASIGNACION en linea " + val_peek(3).ival);
												  if (Parser.getTS(val_peek(3).sval+ambito)!=null)
												  {
													  if (!Parser.getTS(val_peek(3).sval+ambito).get("tipo").equals("LONG") && !Parser.getTS(val_peek(3).sval+ambito).get("tipo").equals("SINGLE"))
                    							      {
                        								Parser.getTS(val_peek(3).sval+ambito).put("postcondicion",Parser.getTS(val_peek(1).sval+ambito).get("postcondicion"));
                    							  	  }
												  }
												  polaca.add(val_peek(3).sval+ambito);
												  polaca.add(":=");
												  /*System.out.println(getTS($1.sval+ambito).get("uso"));*/
												  if (getTS(val_peek(3).sval+ambito) != null)
												  {
													if (getTS(val_peek(3).sval+ambito).get("uso").equals("variable") && !getTS(val_peek(3).sval+ambito).get("tipo").equals("LONG") && !getTS(val_peek(3).sval+ambito).get("tipo").equals("SINGLE"))
													{
														if (getTS(val_peek(1).sval+ambito).get("uso").equals("variable") && !getTS(val_peek(1).sval+ambito).get("tipo").equals("LONG") && !getTS(val_peek(1).sval+ambito).get("tipo").equals("SINGLE"))
														{
															getTS(val_peek(3).sval+ambito).put("valor",getTS(val_peek(1).sval+ambito).get("valor"));
														}
														else
														{
															getTS(val_peek(3).sval+ambito).put("valor",val_peek(1).sval);
														}
													}
													if (!getTS(val_peek(3).sval+ambito).get("uso").equals("tipo_definido") && !getTS(val_peek(3).sval+ambito).get("uso").equals("funcion"))
													{}
													else
													{
														if (getTS(val_peek(3).sval+ambito).get("uso").equals("tipo_definido"))
															yyerror(val_peek(3).ival,"No se puede asignar valor a un tipo definido");
														else
															yyerror(val_peek(3).ival,"No se puede asignar valor a una funcion");
													}
												  }
												  else
												  {
													  yyerrorSem(val_peek(3).ival,"La variable no fue declarada");
												  }
												  }
break;
case 47:
//#line 319 "gramatica.y"
{yyerror(val_peek(2).ival,"Falta el operador de asignacion ");}
break;
case 48:
//#line 320 "gramatica.y"
{yyerror(val_peek(2).ival,"Falta la expresion aritmetica");}
break;
case 49:
//#line 321 "gramatica.y"
{yyerror(val_peek(3).ival,"Error en la expresion aritmetica");}
break;
case 50:
//#line 322 "gramatica.y"
{yyerror(val_peek(3).ival,"Error en el operador de asignacion");}
break;
case 51:
//#line 326 "gramatica.y"
{agregarEstructura("SELECCION en linea " + val_peek(9).ival + " hasta linea " + val_peek(0).ival);}
break;
case 52:
//#line 327 "gramatica.y"
{agregarEstructura("SELECCION en linea " + val_peek(7).ival + " hasta linea " + val_peek(0).ival);}
break;
case 53:
//#line 329 "gramatica.y"
{yyerror(val_peek(6).ival,"Falta el parentesis inicial de la condicion");}
break;
case 54:
//#line 330 "gramatica.y"
{yyerror(val_peek(5).ival,"Falta la condicion del IF");}
break;
case 55:
//#line 331 "gramatica.y"
{yyerror(val_peek(4).ival,"Falta el parentesis final de la condicion");}
break;
case 56:
//#line 332 "gramatica.y"
{yyerror(val_peek(3).ival,"Falta el THEN");}
break;
case 57:
//#line 333 "gramatica.y"
{yyerror(val_peek(0).ival,"Falta el ENDIF");}
break;
case 58:
//#line 334 "gramatica.y"
{yyerror(val_peek(5).ival,"Error en la condicion");}
break;
case 59:
//#line 335 "gramatica.y"
{yyerror(val_peek(2).ival,"Error en la sentencia de seleccion");}
break;
case 60:
//#line 338 "gramatica.y"
{yyerror(val_peek(8).ival,"Falta el parentesis inicial de la condicion");}
break;
case 61:
//#line 339 "gramatica.y"
{yyerror(val_peek(7).ival,"Falta la condicion del IF");}
break;
case 62:
//#line 340 "gramatica.y"
{yyerror(val_peek(6).ival,"Falta el parentesis final de la condicion");}
break;
case 63:
//#line 341 "gramatica.y"
{yyerror(val_peek(5).ival,"Falta el THEN");}
break;
case 64:
//#line 342 "gramatica.y"
{yyerror(val_peek(1).ival,"Falta el ENDIF");}
break;
case 65:
//#line 346 "gramatica.y"
{pilaPolaca.add(polaca.size());
					 polaca.add(null);
					 polaca.add("BF");}
break;
case 66:
//#line 351 "gramatica.y"
{polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()-cantRepeat*5));
											pilaPolaca.remove(pilaPolaca.size()-1);}
break;
case 67:
//#line 354 "gramatica.y"
{polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()+2-cantRepeat*5));
									pilaPolaca.add(polaca.size());

									
									polaca.add(null);
									polaca.add("BI");
									
									pilaPolaca.remove(pilaPolaca.size()-2);}
break;
case 68:
//#line 365 "gramatica.y"
{ polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()-cantRepeat*5));
									 pilaPolaca.remove(pilaPolaca.size()-1);}
break;
case 69:
//#line 369 "gramatica.y"
{	if (val_peek(4).ival == val_peek(1).ival) {
													agregarEstructura("MENSAJE en linea " + val_peek(4).ival);
												}
												else
												{
													agregarEstructura("MENSAJE MULTILINEA de linea " + val_peek(4).ival + " a linea " + val_peek(1).ival);
												}
												polaca.add("msj"+(nroMensaje-1)+ambito);
												polaca.add("PR");
												tSimbolos.get("msj"+(nroMensaje-1)+ambito).put("uso","mensaje");
											}
break;
case 70:
//#line 380 "gramatica.y"
{yyerror(val_peek(3).ival,"Falta el parentesis inicial en el mensaje");}
break;
case 71:
//#line 381 "gramatica.y"
{yyerror(val_peek(2).ival,"Falta el cuerpo del mensaje");}
break;
case 72:
//#line 382 "gramatica.y"
{yyerror(val_peek(1).ival,"Falta el parentesis final en el mensaje");}
break;
case 73:
//#line 383 "gramatica.y"
{yyerror(val_peek(2).ival,"Error en el print");}
break;
case 74:
//#line 387 "gramatica.y"
{agregarEstructura("REPEAT en linea " + val_peek(5).ival + " hasta linea " + val_peek(0).ival);
														 cantRepeat--;
														}
break;
case 75:
//#line 408 "gramatica.y"
{
																	inicioAsignacion.add(polaca.size()-1);
																	polaca.add(val_peek(4).sval+ambito);
															      try {
																	if (Integer.parseInt(val_peek(0).sval) > 0)
																	{
																		polaca.add("+");
																	}
																	else
																	{
																		polaca.add("-");
																	}
																	polaca.add(val_peek(4).sval+ambito);
																	polaca.add(":=");
																}
																catch (NumberFormatException e) {
																if (Double.parseDouble(val_peek(0).sval) >= 0)
																	yyerror(val_peek(0).ival, "El aumento del repeat no es entero");
																else if (Double.parseDouble(val_peek(0).sval) < 0)
																	yyerror(val_peek(0).ival, "El decremento del repeat no es entero");
																}
																	}
break;
case 76:
//#line 438 "gramatica.y"
{polaca.add(val_peek(2).sval+ambito);
										 polaca.add(":=");
										 polaca.add("LI");
										 pilaPolaca.add(polaca.size());
										 if (getTS(val_peek(2).sval+ambito) == null){
													  yyerrorSem(val_peek(2).ival,"Variable no declarada");
										 }
										else
										{
											try {
												Integer.parseInt(val_peek(0).sval);
										 	}
											catch (NumberFormatException e) {
												if (Double.parseDouble(val_peek(0).sval) >= 0)
													yyerror(val_peek(0).ival, "La constante de asignacion no es entera");
												}
										}
										cantRepeat++;
									   }
break;
case 77:
//#line 459 "gramatica.y"
{pilaPolaca.add(polaca.size());
										 polaca.add(null);
										 polaca.add("RP");
								}
break;
case 78:
//#line 466 "gramatica.y"
{
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
break;
case 79:
//#line 492 "gramatica.y"
{agregarEstructura("TRYCATCH en linea " + val_peek(4).ival + " hasta linea " + val_peek(1).ival);
																	  polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()));
																	  pilaPolaca.remove(pilaPolaca.size()-1);
																	  cantTry = false;}
break;
case 80:
//#line 496 "gramatica.y"
{yyerror(val_peek(3).ival,"Falta el cpo_try despues del TRY");}
break;
case 81:
//#line 497 "gramatica.y"
{yyerror(val_peek(2).ival,"Error en el TRYCATCH");}
break;
case 82:
//#line 502 "gramatica.y"
{cantTry =true;}
break;
case 83:
//#line 505 "gramatica.y"
{
					  pilaPolaca.add(polaca.size());
					  polaca.add(null);
					  polaca.add("BI");
					  polaca.add("LI");}
break;
case 88:
//#line 516 "gramatica.y"
{polaca.set(pilaPolaca.get(pilaPolaca.size()-1),indicePolacaToString(polaca.size()-cantRepeat*5));}
break;
case 89:
//#line 522 "gramatica.y"
{ atributotipo = val_peek(0).sval;}
break;
case 90:
//#line 523 "gramatica.y"
{ atributotipo = val_peek(0).sval;}
break;
case 91:
//#line 524 "gramatica.y"
{ /*Si el tipo de definicion de una variable es un tipo definido, verificamos que este exista*/
				if(!tiposdefinidos.contains(val_peek(0).sval))
				{
					yyerror(val_peek(0).ival,"El tipo definido es incorrecto");
				}
				else
				{
					atributotipo = ((String)getTS(val_peek(0).sval+ambito).get("tipo_parametro"))+" "+((String)getTS(val_peek(0).sval+ambito).get("retorno"));
					estipodefinido = true;
				}
			}
break;
case 92:
//#line 537 "gramatica.y"
{  if (funcionactual != null)
													{
														tSimbolos.get(funcionactual).put("tipo_parametro",val_peek(1).sval);
														tSimbolos.get(funcionactual).put("retorno",val_peek(4).sval);
														tSimbolos.get(funcionactual).put("tipo",(String)tSimbolos.get(funcionactual).get("tipo_parametro")+" "+(String)tSimbolos.get(funcionactual).get("retorno"));
													}
													}
break;
case 93:
//#line 544 "gramatica.y"
{yyerror(val_peek(3).ival,"Falta el tipo de funcion");}
break;
case 94:
//#line 545 "gramatica.y"
{yyerror(val_peek(3).ival,"Falta el FUNC en el encabezado de funcion");}
break;
case 95:
//#line 546 "gramatica.y"
{yyerror(val_peek(2).ival,"Falta el parentesis inicial del encabezado de funcion");}
break;
case 96:
//#line 547 "gramatica.y"
{yyerror(val_peek(1).ival,"Falta el tipo de parametro de la funcion");}
break;
case 97:
//#line 548 "gramatica.y"
{yyerror(val_peek(0).ival,"Falta el parentesis final del encabezado de funcion");}
break;
case 98:
//#line 551 "gramatica.y"
{
									 polaca.add("RT");}
break;
case 99:
//#line 553 "gramatica.y"
{yyerror(val_peek(0).ival,"Falta el ';' en el retorno de la funcion");}
break;
case 100:
//#line 556 "gramatica.y"
{polaca.add("POST");}
break;
case 101:
//#line 557 "gramatica.y"
{yyerror(val_peek(4).ival,"Faltan los ':'");}
break;
case 102:
//#line 558 "gramatica.y"
{yyerror(val_peek(3).ival,"Falta el parentesis inicial':'");}
break;
case 103:
//#line 559 "gramatica.y"
{yyerror(val_peek(2).ival,"Falta la condicion");}
break;
case 104:
//#line 560 "gramatica.y"
{yyerror(val_peek(1).ival,"Faltan el parentesis final");}
break;
case 105:
//#line 561 "gramatica.y"
{yyerror(val_peek(3).ival,"Error en la condicion");}
break;
case 106:
//#line 562 "gramatica.y"
{yyerror(val_peek(2).ival,"Error en la declaracion del POST");}
break;
case 107:
//#line 563 "gramatica.y"
{yyerror(val_peek(0).ival,"Falta el ;");}
break;
case 108:
//#line 567 "gramatica.y"
{ 
						if (funcionactual != null)
						{
							tSimbolos.get(val_peek(0).sval+ambito).put("uso","parametro");
							tSimbolos.get(funcionactual).put("tipo_parametro",val_peek(1).sval);
							tSimbolos.get(val_peek(0).sval+ambito).put("tipo",val_peek(1).sval);
							tSimbolos.get(val_peek(0).sval+ambito).put("definida",true);
							tSimbolos.get(val_peek(0).sval+ambito).put("valor",tSimbolos.get(val_peek(0).sval+ambito));
						}
						polaca.add(val_peek(0).sval+ambito); 
						idParametro = val_peek(0).sval+ambito;
					  }
break;
case 109:
//#line 579 "gramatica.y"
{yyerror(val_peek(0).ival,"Falta el id del parametro");}
break;
case 110:
//#line 583 "gramatica.y"
{
						if (!val_peek(0).sval.equals((String)getTS(idParametro).get("tipo")))
						{
							yyerror(val_peek(0).ival,"Los tipos del parametro son distintos");
						}
}
break;
case 111:
//#line 590 "gramatica.y"
{
	tSimbolos.get(idParametro).put("uso","variable");
	tSimbolos.get(idParametro).put("tipo",val_peek(0).sval);
	tSimbolos.get(idParametro).put("definida","true");
}
break;
case 112:
//#line 596 "gramatica.y"
{polaca.add("+");}
break;
case 113:
//#line 597 "gramatica.y"
{polaca.add("-");}
break;
case 115:
//#line 601 "gramatica.y"
{ polaca.add("*");}
break;
case 116:
//#line 602 "gramatica.y"
{ polaca.add("/");}
break;
case 118:
//#line 605 "gramatica.y"
{	polaca.add(val_peek(0).sval+ambito);
				if (getTS(val_peek(0).sval+ambito) == null)
				{
					yyerrorSem(val_peek(0).ival,"Variable no declarada");
				}
			 }
break;
case 120:
//#line 612 "gramatica.y"
{polaca.add("IN");
					  if (getTS(val_peek(0).sval+ambito)!=null)
					  {
						    if (getTS(val_peek(0).sval+ambito).get("postcondicion").equals("true"))
							{
								if (!cantTry)
								{
									yyerror(val_peek(0).ival,"La invocacion con postcondicion no se encuentra dentro de un try");
								}
							}
					  }
					 }
break;
case 121:
//#line 625 "gramatica.y"
{polaca.add(val_peek(0).sval+ambito);
				  if (!getTS(val_peek(0).sval+ambito).containsKey("definida")){
					yyerrorSem(val_peek(1).ival,"Variable no declarada");}}
break;
case 122:
//#line 628 "gramatica.y"
{polaca.add(val_peek(0).sval+ambito);}
break;
case 123:
//#line 631 "gramatica.y"
{
									/*Si dentro de una funcion se invoca una funcion, tiraria error ya que la funcion puede estar definida mas abajo en el codigo*/
									/*Por lo que si ese es el caso, añadimos la funcion llamada a la estructura "aDefinir" para luego verificar si fue definida o no*/
									if (getTS(val_peek(3).sval+ambito) == null)
									{
										/*Si ya estamos en el codigo ejecutable significa que la funcion no se declaro*/
										if (ejecutable)
										{
											yyerrorSem(val_peek(3).ival,"Funcion no declarada");
										}
										else
										{
											aDefinir.put(val_peek(3).sval,val_peek(1).sval);
										}
									}						
									else
									{	
										/*Si la funcion no fue definida añadimos el error*/
										if (getTS(val_peek(3).sval+ambito) == null) {
											yyerrorSem(val_peek(3).ival,"Funcion no declarada");}
										else {	
											/*Si la funcion ya fue definida*/
											/*Verificamos que el parametro de la funcion sea el correcto*/
											if (!getTS(val_peek(3).sval+ambito).get("tipo_parametro").equals(val_peek(1).sval))
											{
												yyerror(val_peek(3).ival,"El parametro de la funcion es incorrecto");}
											}
									}
									/*Si nos encontramos en un ambito distinto al del programa principal*/
									/*PARA QUE*/
									if (!ambito.equals(charAmbito+ambitoInicial))
									{
										String funcionLlamadora = ambito.substring(0,ambito.indexOf('@'));
										funcionesLlamadas.put(ambito.substring(0,ambito.indexOf('@')),new ArrayList<>());
										funcionesLlamadas.get(funcionLlamadora).add(val_peek(1).sval);
									}
									/*Añadimos a la polaca el id de la funcion, antes se añadió el parametro*/
									/*En caso de que sea un tipo_definido y no una funcion, le añadimos el ambito*/
									/*System.out.println("Busco en la tabla de simbolos "+$1.sval);*/
									/*System.out.println("tsimbolos="+tSimbolos);*/
									if (tSimbolos.get(val_peek(3).sval) == null)
									{
									/*	System.out.println("existe");*/
										polaca.add(val_peek(3).sval+ambito);
									}
									else
									{
									/*	System.out.println("no existe");*/
										polaca.add(val_peek(3).sval);
									}
											
									}
break;
case 124:
//#line 683 "gramatica.y"
{yyerror(val_peek(2).ival,"Falta el parentesis de apertura de la invocacion");}
break;
case 125:
//#line 684 "gramatica.y"
{yyerror(val_peek(1).ival,"Falta el parametro de la invocacion");}
break;
case 126:
//#line 688 "gramatica.y"
{ 
						/*Verificamos que el numero no se pase de los límites ahora que sabemos si el número es negativo o positivo*/
						if (val_peek(0).sval.equals("2147483648")){
							yyerror(val_peek(0).ival,"Constante positiva fuera de rango");
							/*Solucionamos el error, poniendo como valor el valor maximo*/
							val_peek(0).sval="2147483647";
						}
						/*Si la tabla de simbolos no contenia a la constante, la añadimos*/
						if (!tSimbolos.containsKey(val_peek(0).sval)){
							tSimbolos.put(val_peek(0).sval,new Hashtable<String,Object>());
						}
						/*Le añadimos los atributos a la constante*/
						tSimbolos.get(val_peek(0).sval).put("uso","constante");
						tSimbolos.get(val_peek(0).sval).put("definida","true");
						tSimbolos.get(val_peek(0).sval).put("token",(int)Parser.CTE_LONG);
						tSimbolos.get(val_peek(0).sval).put("tipo","LONG");
						tSimbolos.get(val_peek(0).sval).put("valor",val_peek(0).sval);
						/*Añadimos a la polaca el valor de la constante*/
						polaca.add(val_peek(0).sval);
					}
break;
case 127:
//#line 709 "gramatica.y"
{	
							/*Al ser negativa la constante es probable que no se haya añadido a la tabla de Simbolos, por lo que la añadimos*/
							if (!tSimbolos.containsKey("-"+val_peek(0).sval))
							{
								tSimbolos.put("-"+val_peek(0).sval,new Hashtable<String,Object>());
							}
							/*Le añadimos los atributos a la constante*/
							tSimbolos.get("-"+val_peek(0).sval).put("uso","constante");
							tSimbolos.get("-"+val_peek(0).sval).put("definida","true");
							tSimbolos.get("-"+val_peek(0).sval).put("token",(int)Parser.CTE_LONG);
							tSimbolos.get("-"+val_peek(0).sval).put("tipo","LONG");
							tSimbolos.get("-"+val_peek(0).sval).put("valor","-"+val_peek(0).sval);
							/*Añadimos a la polaca la constante*/
							polaca.add("-"+val_peek(0).sval);
						}
break;
case 128:
//#line 725 "gramatica.y"
{
						/*if (!tSimbolos.containsKey($1.sval))
							{
								tSimbolos.put("-"+$1.sval,new Hashtable<String,Object>());
							}*/
						/*Le añadimos los atributos a la constante*/
						tSimbolos.get(val_peek(0).sval).put("uso","constante");
						tSimbolos.get(val_peek(0).sval).put("definida","true");
					  	tSimbolos.get(val_peek(0).sval).put("token",(int)Parser.CTE_SINGLE);
					  	tSimbolos.get(val_peek(0).sval).put("tipo","SINGLE");
					  	tSimbolos.get(val_peek(0).sval).put("valor",val_peek(0).sval);
						/*Añadimos a la polaca la constante  */
						polaca.add(val_peek(0).sval);
					}
break;
case 129:
//#line 740 "gramatica.y"
{	
							/*Al ser negativa la constante es probable que no se haya añadido a la tabla de Simbolos, por lo que la añadimos*/
							if (!tSimbolos.containsKey("-"+val_peek(0).sval))
							{
								tSimbolos.put("-"+val_peek(0).sval,new Hashtable<String,Object>());
							}
							/*Le añadimos los atributos a la constante*/
							tSimbolos.get("-"+val_peek(0).sval).put("uso","constante");
							tSimbolos.get("-"+val_peek(0).sval).put("definida","true");
							tSimbolos.get("-"+val_peek(0).sval).put("token",(int)Parser.CTE_SINGLE);
							tSimbolos.get("-"+val_peek(0).sval).put("tipo","SINGLE");
							tSimbolos.get("-"+val_peek(0).sval).put("valor","-"+val_peek(0).sval);
							polaca.add("-"+val_peek(0).sval);
						 }
break;
case 130:
//#line 756 "gramatica.y"
{ polaca.add("&&");}
break;
case 132:
//#line 758 "gramatica.y"
{yyerror(val_peek(1).ival,"Error en la expresion de la izquierda");}
break;
case 133:
//#line 759 "gramatica.y"
{yyerror(val_peek(2).ival,"Error en el comparador");}
break;
case 134:
//#line 763 "gramatica.y"
{ polaca.add("||");}
break;
case 136:
//#line 767 "gramatica.y"
{polaca.add(varcomparador);}
break;
case 137:
//#line 768 "gramatica.y"
{yyerror(val_peek(2).ival, "Error en expresion de la izquierda");}
break;
case 138:
//#line 769 "gramatica.y"
{yyerror(val_peek(2).ival, "Comparador faltante o invalido");}
break;
case 139:
//#line 772 "gramatica.y"
{varcomparador = ">";}
break;
case 140:
//#line 773 "gramatica.y"
{varcomparador = "<";}
break;
case 141:
//#line 774 "gramatica.y"
{varcomparador = "==";}
break;
case 142:
//#line 775 "gramatica.y"
{varcomparador = "<=";}
break;
case 143:
//#line 776 "gramatica.y"
{varcomparador = ">=";}
break;
case 144:
//#line 777 "gramatica.y"
{varcomparador = "<>";}
break;
//#line 1883 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
/**
 * A default run method, used for operating this parser
 * object in the background.  It is intended for extending Thread
 * or implementing Runnable.  Turn off with -Jnorun .
 */
public void run()
{
  yyparse();
}
//## end of method run() ########################################



//## Constructors ###############################################
/**
 * Default constructor.  Turn off with -Jnoconstruct .

 */
public Parser()
{
  //nothing to do
}


/**
 * Create a parser, setting the debug to true or false.
 * @param debugMe true for debugging, false for no debug.
 */
public Parser(boolean debugMe)
{
  yydebug=debugMe;
}
//###############################################################



}
//################### END OF CLASS ##############################
