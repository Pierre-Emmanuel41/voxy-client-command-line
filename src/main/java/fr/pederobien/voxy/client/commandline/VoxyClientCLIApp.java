package fr.pederobien.voxy.client.commandline;

import fr.pederobien.commandtree.impl.CLI;
import fr.pederobien.utils.event.Logger;

public class VoxyClientCLIApp {

	public static void main(String[] args) {
		Logger.setPrintInColor(true);

		if (args.length == 0)
			// Max debug level is 3 in project voxy-client
			Logger.setPrintDebugLevel(4);
		if (args.length >= 2 && args[0].equals("-d"))
			try {
				Logger.setPrintDebugLevel(Integer.parseInt(args[1]));
			} catch (Exception e) {
				Logger.warning("Minimum debug level could not be parsed");
				Logger.setPrintDebugLevel(4);
			}

		Runnable cli = CLI.simpleInterface("voxy>", arg -> arg.equals("exit"), new VoxyCommandTree().getTree());
		cli.run();
	}
}
