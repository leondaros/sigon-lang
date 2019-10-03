package br.ufsc.ine.agent;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import agent.AgentLexer;
import agent.AgentParser;
import br.ufsc.ine.agent.context.ContextService;
import br.ufsc.ine.agent.context.ontologic.OntologicContextService;
import br.ufsc.ine.parser.AgentWalker;
import br.ufsc.ine.parser.VerboseListener;

public class Main {

	public static void main(String[] args) {

		//String filename = getLastArgument(args);

//		String filename="/home/valdirluiz/works/agent-project/examples/annotation";
		String filename="/home/valdirluiz/works/agent-project/examples/ex1";

		File file = new File(filename);
		if (!file.exists()) {
			System.out.println("File " + filename + " not found.");
			return;
		}

		try {
			CharStream stream = CharStreams.fromFileName(filename);
			AgentLexer lexer = new AgentLexer(stream);
			CommonTokenStream tokens = new CommonTokenStream(lexer);

			AgentParser parser = new AgentParser(tokens);
			parser.removeErrorListeners();
			parser.addErrorListener(new VerboseListener());

			ParseTree tree = parser.agent();
			ParseTreeWalker walker = new ParseTreeWalker();

			AgentWalker agentWalker = new AgentWalker();
			walker.walk(agentWalker, tree);

			OntologicContextService ontologicContextService = OntologicContextService.getInstance();
			ContextService[] cc = new ContextService[] {ontologicContextService};
			
			Agent agent = new Agent();
			agent.run(agentWalker,cc);

		} catch (IOException e) {
			System.out.println("I/O exception.");
		}

	}

	private static String getLastArgument(String[] args) {
		return args[args.length - 1];
	}

}
