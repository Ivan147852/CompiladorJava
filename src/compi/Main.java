package compi;
import javax.swing.*;
import java.io.File;
import java.io.IOException;


public class Main {

			public static void main(String[] args) throws IOException {

				//Selector de archivo
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showDialog(null, "seleccione el archivo");
				File file = null;
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					file = fc.getSelectedFile();
				}
				//File file = new File("E:\\Ivan\\TPCompi\\TP-Compiladores2021_G18\\Ejecutable\\prueba.txt");
				String ubicacionDeCodigo = file.getPath();
				Parser p = new Parser(ubicacionDeCodigo);
				p.yyparse();
				p.escribirPolaca();

				if (Lexico.erroresL == 0 && Parser.erroresS == 0)
				{
					p.crearAssembler();
					GeneracionAssembler ga = new GeneracionAssembler();
					String codigo = ga.polacaToAssembler(Parser.polacaEjecutable);
					codigo += "JMP final\n";
					codigo += "division_por_cero: invoke MessageBox, NULL, addr msjDivisionPorCero, addr msjDivisionPorCero, MB_OK\n";
					codigo += "JMP final\n";
					codigo += "overflow_suma: invoke MessageBox, NULL, addr msjOverflowSuma, addr msjOverflowSuma, MB_OK\n";
					codigo += "JMP final\n";
					codigo += "recursion_mutua: invoke MessageBox, NULL, addr msjRecursionMutua, addr msjRecursionMutua, MB_OK\n";
					codigo += "final: invoke ExitProcess, 0 \nend start\n";
					ga.clonarTablaSimbolos();
					ga.cargarTablaSimbolos(Parser.polacaFunciones);
					Parser.pwAs.print(codigo);
				}
				p.escribirPolaca();
				p.escribirTablaS();
				p.escribirEstruc();

				p.cerrarFicheros();


			}
}

