package tools.php.ast2cpg;

import java.io.FileReader;
import java.io.IOException;

import ast.expressions.Expression;
import ast.php.statements.GlobalStatement;
import cfg.nodes.ASTNodeContainer;
import cfg.nodes.CFGNode;
import filesystem.IncludeMap;
import filesystem.PHPIncludeMapFactory;
import inherit.IG;
import inherit.PHPInheritFactory;
import org.apache.commons.cli.ParseException;

import ast.php.functionDef.FunctionDef;
import cfg.ASTToCFGConverter;
import cfg.CFG;
import cfg.PHPCFGFactory;
import cg.CG;
import cg.PHPCGFactory;
import ddg.CFGAndUDGToDefUseCFG;
import ddg.DDGCreator;
import ddg.php.PHPDDGCreator;
import ddg.DataDependenceGraph.DDG;
import ddg.DefUseCFG.DefUseCFG;
import inputModules.csv.KeyedCSV.exceptions.InvalidCSVFile;
import inputModules.csv.csvFuncExtractor.CSVFunctionExtractor;
import outputModules.common.Writer;
import outputModules.csv.MultiPairCSVWriterImpl;
import outputModules.csv.exporters.CSVCFGExporter;
import outputModules.csv.exporters.CSVCGExporter;
import outputModules.csv.exporters.CSVDDGExporter;
import udg.CFGToUDGConverter;
import udg.php.useDefAnalysis.PHPASTDefUseAnalyzer;
import udg.useDefGraph.UseDefGraph;

public class Main {

	// command line interface
	static CommandLineInterface cmdLine = new CommandLineInterface();

	// converters
	static CSVFunctionExtractor extractor = new CSVFunctionExtractor();
	//static PHPCFGFactory cfgFactory = new PHPCFGFactory();
	static ASTToCFGConverter ast2cfgConverter = new ASTToCFGConverter();
	static CFGToUDGConverter cfgToUDG = new CFGToUDGConverter();
	static CFGAndUDGToDefUseCFG udgAndCfgToDefUseCFG = new CFGAndUDGToDefUseCFG();
	static PHPDDGCreator ddgCreator = new PHPDDGCreator();

	// exporters
	static CSVCFGExporter csvCFGExporter = new CSVCFGExporter();
	static CSVDDGExporter csvDDGExporter = new CSVDDGExporter();
	static CSVCGExporter csvCGExporter = new CSVCGExporter();

	public static void main(String[] args) throws InvalidCSVFile, IOException {
		// parse command line
		parseCommandLine(args);

		// initialize readers
		String nodeFilename = cmdLine.getNodeFile();
		String edgeFilename = cmdLine.getEdgeFile();
		FileReader nodeFileReader = new FileReader(nodeFilename);
		FileReader edgeFileReader = new FileReader(edgeFilename);

		// initialize converters

		extractor.setInterpreters(new PHPCSVNodeInterpreter(), new PHPCSVEdgeInterpreter());
		extractor.initialize(nodeFileReader, edgeFileReader);
		ast2cfgConverter.setFactory(new PHPCFGFactory());
		cfgToUDG.setASTDefUseAnalyzer(new PHPASTDefUseAnalyzer());

		// initialize writers
		MultiPairCSVWriterImpl csvWriter = new MultiPairCSVWriterImpl();
		csvWriter.openEdgeFile( ".", "cpg_edges.csv");
		Writer.setWriterImpl( csvWriter);

		// let's go...
		FunctionDef rootnode;
		while ((rootnode = (FunctionDef)extractor.getNextFunction()) != null) {

			PHPCGFactory.addFunctionDef( rootnode);

			CFG cfg = ast2cfgConverter.convert(rootnode);
			csvCFGExporter.writeCFGEdges(cfg);

			for (CFGNode vertex : cfg.getVertices()){
				if(vertex instanceof ASTNodeContainer && ((ASTNodeContainer)vertex).getASTNode() instanceof GlobalStatement){
					PHPCGFactory.addCfg(((ASTNodeContainer)vertex).getASTNode().getNodeId(), cfg);
					break;
				}
			}

			UseDefGraph udg = cfgToUDG.convert(cfg);
			DefUseCFG defUseCFG = udgAndCfgToDefUseCFG.convert(cfg, udg);
			DDG ddg = ddgCreator.createForDefUseCFG(defUseCFG);
			csvDDGExporter.writeDDGEdges(ddg);
		}
		// mxy: we have collected all include statements and constants, build include map
		PHPIncludeMapFactory.newInstance();
		PHPIncludeMapFactory.writeIncludeEdges();

		// append inherit relationships
		IG ig = PHPInheritFactory.newInstance();
		PHPInheritFactory.writeInheritEdges(ig);

		// now that we wrapped up all functions, let's finish off with the call graph
		PHPCGFactory.setMode(cmdLine.getParseMode());
		CG cg = PHPCGFactory.newInstance();
		csvCGExporter.writeCGEdges(cg);

		csvWriter.closeEdgeFile();

		System.out.println();
		System.out.println("ast.so 70 new AST node kinds");
		System.out.println("----------------------------------------");

		System.out.println("The number of AST_NULLABEL = " + PHPCSVNodeInterpreter.counter_new_NULLABALE);
		System.out.println("The number of AST_ARROW_FUNC = " + PHPCSVNodeInterpreter.counter_new_ARROW_FUNC);
		System.out.println("The number of AST_CLASS_NAME = " + PHPCSVNodeInterpreter.counter_new_CLASS_NAME);
		System.out.println("The number of AST_PROP_GROUP = " + PHPCSVNodeInterpreter.counter_new_PROP_GROUP);

		System.out.println();
		System.out.println("ast.so 70 changed AST node kinds");
		System.out.println("----------------------------------------");

		System.out.println("The number of AST_CLASS = " + PHPCSVNodeInterpreter.counter_CLASS);
		System.out.println("The number of AST_CLOSURE = " + PHPCSVNodeInterpreter.counter_CLOSURE);
		System.out.println("The number of AST_CONST_ELEM = " + PHPCSVNodeInterpreter.counter_CONST_ELEM);
		System.out.println("The number of AST_FUNC_DECL = " + PHPCSVNodeInterpreter.counter_FUNC_DECL);
		System.out.println("The number of AST_METHOD = " + PHPCSVNodeInterpreter.counter_METHOD);
	}

	private static void parseCommandLine(String[] args)	{

		try {
			cmdLine.parseCommandLine(args);
		}
		catch (RuntimeException | ParseException e) {
			printHelpAndTerminate(e);
		}
	}

	private static void printHelpAndTerminate(Exception e) {

		System.err.println(e.getMessage());
		cmdLine.printHelp();
		System.exit(0);
	}

}
