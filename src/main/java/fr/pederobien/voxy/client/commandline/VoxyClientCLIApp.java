package fr.pederobien.voxy.client.commandline;

import fr.pederobien.commandtree.impl.CLI;
import fr.pederobien.utils.event.Logger;

public class VoxyClientCLIApp {

	public static void main(String[] args) {
		Logger.setPrintInColor(true);
		parseArgs(args);

		Runnable cli = CLI.simpleInterface("voxy>", arg -> arg.equals("exit"), new VoxyCommandTree().getTree());
		cli.run();
	}

	private static void parseArgs(String[] args) {
		if (args.length == 0) {
			Logger.setPrintDebugLevel(Integer.MAX_VALUE);
			return;
		}

		for (int i = 0; i < args.length; /* Managed inside the loop */) {
			// Debug
			if (args[i].equals("-d")) {
				i++;
				try {
					int level = Integer.parseInt(args[i]);
					Logger.setPrintDebugLevel(level);
					Logger.info("Minimum debug level is %s", level);
					i++;
				} catch (Exception e) {
					Logger.warning("Minimum debug level could not be parsed, setting to maximum value (ie: no debug output)");
					Logger.setPrintDebugLevel(Integer.MAX_VALUE);
				}

				continue;
			}

			// Event
			if (args[i].equals("-e")) {
				Logger.setPrintEvent(true);
				Logger.info("Internal events will be printed");
				i++;
			}
		}
	}
}
