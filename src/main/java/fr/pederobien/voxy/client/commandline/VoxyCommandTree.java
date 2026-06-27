package fr.pederobien.voxy.client.commandline;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import fr.pederobien.commandtree.impl.NodeHelper;
import fr.pederobien.commandtree.impl.Tree;
import fr.pederobien.commandtree.interfaces.INode;
import fr.pederobien.commandtree.interfaces.INodeBuilder;
import fr.pederobien.commandtree.interfaces.IResult;
import fr.pederobien.commandtree.interfaces.ITree;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.impl.layer.SimpleCertificate;
import fr.pederobien.sound.impl.Mixer;
import fr.pederobien.sound.impl.SoundApi;
import fr.pederobien.sound.impl.filters.SimpleBandPassFilter;
import fr.pederobien.sound.interfaces.ISoundApi;
import fr.pederobien.voxy.client.impl.VoxyClientFactory;
import fr.pederobien.voxy.client.impl.config.VoxyClientConfig;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class VoxyCommandTree {
	private static final String CREATE = "create";
	private static final String CONNECT = "connect";
	private static final String DISCONNECT = "disconnect";
	private static final String DISPOSE = "dispose";
	private static final String JOIN = "join";
	private static final String LEAVE = "leave";
	private static final String SET = "set";
	private static final String MUTE = "mute";
	private static final String DEAF = "deaf";
	private static final String ADD = "add";
	private static final String REMOVE = "remove";
	private static final String RENAME = "rename";
	private static final String ROOM = "room";
	private static final String LIST = "list";
	private final ITree<IVoxyClient> tree;

	public VoxyCommandTree() {
		tree = new Tree<IVoxyClient>();

		INodeBuilder<IVoxyClient> builder;

		// Create -------------------------------------------------------------
		builder = tree.getNodeBuilder(CREATE, "To create a new client");
		builder.withAvailability(client -> client == null || client.isDisposed());
		builder.withExecution((tree, args) -> create(tree, args));
		tree.add(builder.build());

		// Connect ------------------------------------------------------------
		builder = tree.getNodeBuilder(CONNECT, "To attempt the connection with the server");
		builder.withAvailability(client -> client != null && !client.isDisposed());
		builder.withExecution((tree, args) -> connect(tree, args));
		tree.add(builder.build());

		// Disconnect ---------------------------------------------------------
		builder = tree.getNodeBuilder(DISCONNECT, "To disconnect from the server");
		builder.withAvailability(client -> client != null && !client.isDisposed());
		builder.withExecution((tree, args) -> disconnect(tree, args));
		tree.add(builder.build());

		// Dispose ------------------------------------------------------------
		builder = tree.getNodeBuilder(DISPOSE, "To dispose this client,");
		builder.withAvailability(client -> client != null && !client.isDisposed());
		builder.withExecution((tree, args) -> dispose(tree, args));
		tree.add(builder.build());

		// Join ---------------------------------------------------------------
		builder = tree.getNodeBuilder(JOIN, "To join a room on the server");
		builder.withAvailability(client -> client != null && !client.isDisposed());
		builder.withExecution((tree, args) -> join(tree, args));
		tree.add(builder.build());

		// Leave --------------------------------------------------------------
		builder = tree.getNodeBuilder(LEAVE, "To leave a room from the server");
		builder.withAvailability(client -> client != null && !client.isDisposed());
		builder.withExecution((tree, args) -> leave(tree, args));
		tree.add(builder.build());

		// Set ----------------------------------------------------------------
		builder = tree.getNodeBuilder(SET, "To modify the properties of a room or of a player");
		builder.withAvailability(client -> client != null && !client.isDisposed());
		INode<IVoxyClient> set = builder.build();
		tree.add(set);

		// Set Mute -----------------------------------------------------------
		builder = tree.getNodeBuilder(MUTE, "To mute/unmute yourself or a player for yourself");
		builder.withAvailability(client -> client != null);
		builder.withExecution((tree, args) -> setMute(tree, args));
		set.add(builder.build());

		// Set Deaf -----------------------------------------------------------
		builder = tree.getNodeBuilder(DEAF, "To deaf/undeaf yourself");
		builder.withAvailability(client -> client != null);
		builder.withExecution((tree, args) -> setDeaf(tree, args));
		set.add(builder.build());

		// Add ----------------------------------------------------------------
		builder = tree.getNodeBuilder(ADD, "To send an Add request to the server");
		builder.withAvailability(client -> client != null && !client.isDisposed());
		INode<IVoxyClient> add = builder.build();
		tree.add(add);

		// Add Room -----------------------------------------------------------
		builder = tree.getNodeBuilder(ROOM, "To add a room on the server");
		builder.withAvailability(client -> client != null);
		builder.withExecution((tree, args) -> addRoom(tree, args));
		add.add(builder.build());

		// Remove -------------------------------------------------------------
		builder = tree.getNodeBuilder(REMOVE, "To send a remove request to the server");
		builder.withAvailability(client -> client != null);
		INode<IVoxyClient> remove = builder.build();
		tree.add(remove);

		// Remove Room --------------------------------------------------------
		builder = tree.getNodeBuilder(ROOM, "To remove a room from the server");
		builder.withAvailability(client -> client != null && !client.isDisposed());
		builder.withExecution((tree, args) -> removeRoom(tree, args));
		remove.add(builder.build());

		// Rename -------------------------------------------------------------
		builder = tree.getNodeBuilder(RENAME, "To send a Rename request to the server");
		builder.withAvailability(client -> client != null && !client.isDisposed());
		INode<IVoxyClient> rename = builder.build();
		tree.add(rename);

		// Rename Room --------------------------------------------------------
		builder = tree.getNodeBuilder(ROOM, "To rename a room on the server");
		builder.withAvailability(client -> client != null);
		builder.withExecution((tree, args) -> renameRoom(tree, args));
		rename.add(builder.build());

		// List ---------------------------------------------------------------
		builder = tree.getNodeBuilder(LIST, "To list each room registered on the server");
		builder.withAvailability(client -> client != null && !client.isDisposed());
		builder.withExecution((tree, args) -> list(tree, args));
		tree.add(builder.build());
	}

	/**
	 * @return The tree to interact with a voxy client.
	 */
	public ITree<IVoxyClient> getTree() {
		return tree;
	}

	private IResult create(ITree<IVoxyClient> tree, String[] args) {
		if (args.length < 3)
			return NodeHelper.result(false, "The player's name, the server's IP address or port number is missing");

		if (tree.getSeed() != null && !tree.getSeed().isDisposed()) {
			tree.getSeed().disconnect();
			tree.getSeed().dispose();
		}

		String name = args[0];
		String address = args[1];

		if (!NodeHelper.isStrictInt(args[2]))
			return NodeHelper.result(false, "The server's port number shall be of type int");

		int port = NodeHelper.parseInt(args[2]);

		VoxyClientConfig config = VoxyClientFactory.createConfig(name, address, port);
		config.getTcpConfig().setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate(), 500, 10000));
		config.getTcpConfig().setConnectionTimeout(10000);
		config.getUdpConfig().setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate(), 500, 10000));
		config.getUdpConfig().setConnectionTimeout(10000);

		config.setSoundApi(new SoundApi(new Mixer(48000)));
		ISoundApi soundApi = config.getSoundApi();

		// Sound API initialization failed
		if (soundApi.getMicrophone() == null)
			return NodeHelper.result(false, "The sound API could not be initialized");

		soundApi.getMicrophone().setFilter(new SimpleBandPassFilter(20, 3400, soundApi.getMixer().getSampleRate()));
		config.setCompressionAlgorithm(2);

		tree.setSeed(VoxyClientFactory.createClient(config));
		return NodeHelper.result(true, "Client associated to player \"%s\" and server %s:%s created", name, address, port);
	}

	private IResult connect(ITree<IVoxyClient> tree, String[] args) {
		tree.getSeed().connect();
		return NodeHelper.result(true, "Attempting connection with the server");
	}

	private IResult disconnect(ITree<IVoxyClient> tree, String[] args) {
		tree.getSeed().disconnect();
		return NodeHelper.result(true, "Attempting disconnection from the server");
	}

	private IResult dispose(ITree<IVoxyClient> tree, String[] args) {
		tree.getSeed().dispose();
		return NodeHelper.result(true, "Disposing %s's client", tree.getSeed().getPlayer().getName());
	}

	private IResult join(ITree<IVoxyClient> tree, String[] args) {
		if (args.length == 0)
			return NodeHelper.result(false, "The room's name to join is missing");

		Optional<IVoxyRoom> room = tree.getSeed().getRooms().get(args[0]);
		if (room.isEmpty())
			return NodeHelper.result(false, "The room \"%s\" is not registered", args[0]);

		// Joining the room
		room.get().join();
		return NodeHelper.result(true, "Joining the room \"%s\"", room.get().getName());
	}

	private IResult leave(ITree<IVoxyClient> tree, String[] args) {
		Optional<IVoxyRoom> room = tree.getSeed().getRooms().getRoomByPlayerName(tree.getSeed().getPlayer().getName());
		if (!room.isPresent())
			return NodeHelper.result(false, "You are not registered in a room");

		// Leaving the room
		room.get().leave();
		return NodeHelper.result(true, "Leaving the room \"%s\"", room.get().getName());
	}

	private IResult list(ITree<IVoxyClient> tree, String[] args) {
		if (tree.getSeed().getRooms().size() == 0)
			return NodeHelper.result(true, "The server does not have any room");

		List<IVoxyRoom> rooms = tree.getSeed().getRooms().toList();
		IResult result = NodeHelper.result(true, "");

		for (IVoxyRoom room : rooms) {
			StringJoiner joiner = new StringJoiner(", ", "[", "]");
			if (room.getPlayers().size() == 0)
				joiner.add("no player");
			else
				for (IVoxyPlayer player : room.getPlayers().toList())
					joiner.add(player.getName());

			result.getFeedbacks().add(String.format("%s: %s", room.getName(), joiner));
		}

		return result;
	}

	private IResult setMute(ITree<IVoxyClient> tree, String[] args) {
		if (args.length == 0)
			return NodeHelper.result(false, "The mute status is missing, or the name of the other player and the mute status is missing");

		Optional<IVoxyRoom> room = tree.getSeed().getRooms().getRoomByPlayerName(tree.getSeed().getPlayer().getName());
		if (!room.isPresent())
			return NodeHelper.result(false, "You shall be in a room to modify your mute status");

		// Main player is muting/unmuting himself
		if (args.length == 1) {
			if (!NodeHelper.isStrictBool(args[0]))
				return NodeHelper.result(false, "The mute status cannot be parsed, it shall be \"true\" or \"false\", case ignored");

			boolean isMute = NodeHelper.parseBool(args[0]);
			tree.getSeed().getPlayer().setMute(isMute);
			return NodeHelper.result(true, "Sending mute update to the server");
		}

		// Main player is muting/unmuting another player for himself
		else {
			Optional<IVoxyRoom> other = tree.getSeed().getRooms().getRoomByPlayerName(args[0]);
			if (!other.isPresent())
				return NodeHelper.result(false, "The player \"%s\" is not registered in the same room as you", args[0]);

			if (!NodeHelper.isStrictBool(args[1]))
				return NodeHelper.result(false, "The mute status cannot be parsed, it shall be \"true\" or \"false\", case ignored");

			boolean isMute = NodeHelper.parseBool(args[1]);
			other.get().getPlayers().get(args[0]).get().setMute(isMute);
			return NodeHelper.result(true, "Sending mute update to the server");
		}
	}

	private IResult setDeaf(ITree<IVoxyClient> tree, String[] args) {
		if (args.length == 0)
			return NodeHelper.result(false, "The deaf status is missing");

		Optional<IVoxyRoom> room = tree.getSeed().getRooms().getRoomByPlayerName(tree.getSeed().getPlayer().getName());
		if (!room.isPresent())
			return NodeHelper.result(false, "You shall be in a room to modify your deaf status");

		// Main player is deafing/undeafing himself
		if (!NodeHelper.isStrictBool(args[0]))
			return NodeHelper.result(false, "The deaf status cannot be parsed, it shall be \"true\" or \"false\", case ignored");

		boolean isDeaf = NodeHelper.parseBool(args[0]);
		tree.getSeed().getPlayer().setDeaf(isDeaf);
		return NodeHelper.result(true, "Sending deaf update to the server");
	}

	private IResult addRoom(ITree<IVoxyClient> tree, String[] args) {
		if (args.length == 0)
			return NodeHelper.result(false, "The name of the room to add is missing");

		String name = args[0];
		Optional<IVoxyRoom> room = tree.getSeed().getRooms().get(name);
		if (room.isPresent())
			return NodeHelper.result(false, "The room \"%s\" is already registered on the server", name);

		tree.getSeed().getRooms().add(name);
		return NodeHelper.result(true, "Adding room \"%s\" on the server", name);
	}

	private IResult removeRoom(ITree<IVoxyClient> tree, String[] args) {
		if (args.length == 0)
			return NodeHelper.result(false, "The name of the room to remove is missing");

		String name = args[0];
		Optional<IVoxyRoom> room = tree.getSeed().getRooms().get(name);
		if (!room.isPresent())
			return NodeHelper.result(false, "The room \"%s\" is not registered on the server", name);

		tree.getSeed().getRooms().remove(name);
		return NodeHelper.result(true, "Removing room \"%s\" from the server", name);
	}

	private IResult renameRoom(ITree<IVoxyClient> tree, String[] args) {
		if (args.length < 2)
			return NodeHelper.result(false, "The name of the room to rename or the new room's name is missing");

		String toRename = args[0];
		Optional<IVoxyRoom> room = tree.getSeed().getRooms().get(toRename);
		if (!room.isPresent())
			return NodeHelper.result(false, "The room \"%s\" is not registered on the server", toRename);

		String newName = args[1];
		Optional<IVoxyRoom> registered = tree.getSeed().getRooms().get(newName);
		if (registered.isPresent())
			return NodeHelper.result(false, "The room \"%s\" is already registered on the server", newName);

		room.get().setName(newName);
		return NodeHelper.result(true, "Renaming room \"%s\" as \"%s\" on the server", toRename, newName);
	}
}
