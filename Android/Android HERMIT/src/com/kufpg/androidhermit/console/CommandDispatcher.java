package com.kufpg.androidhermit.console;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.kufpg.androidhermit.StandardActivity;
import com.kufpg.androidhermit.console.CommandDispatcher.Command;
import com.kufpg.androidhermit.dialog.TerminalNotInstalledDialog;
import com.kufpg.androidhermit.server.HermitServer;

import android.content.Intent;
import android.widget.Toast;

@SuppressWarnings("unused")
public class CommandDispatcher {
	private static ConsoleActivity mConsole;

	//List of Commands
	private static Command clear = new Command("clear", 0, true) {
		@Override
		protected void run(String... args) {
			mConsole.clear();
		}
	};
	private static Command consider = new Command("consider", 1, false) {
		@Override
		protected void run(String... args) {
			try {
				String jstr = "{command:consider},{args:" + args[0] + "}";
				HermitServer request = new HermitServer(mConsole, new JSONObject(jstr));
				request.execute();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	};
	private static Command exit = new Command("exit", 0, false) {
		@Override
		protected void run(String... args) {
			mConsole.exit();
		}
	};
	private static Command resume = new Command("resume", 0, false) {
		@Override
		protected void run(String... args) {
			mConsole.appendConsoleEntry("TODO: Figure out what resume does.");
		}
	};
	private static Command toast = new Command("toast", 0, true) {
		@Override
		protected void run(String... args) {
			Toast theToast = null;
			if (args.length == 0) {
				theToast = Toast.makeText(mConsole, "No arguments!", Toast.LENGTH_SHORT);
			} else {
				theToast = Toast.makeText(mConsole,
						varargsToString(args), Toast.LENGTH_SHORT);
			}
			theToast.show();
		}
	};
	private static Command terminal = new Command("terminal",0,true){
		@Override
		protected void run(String... args){
			String packageName = "jackpal.androidterm";
			boolean installed = StandardActivity.appInstalledOrNot(packageName);  
			if (installed) {
				Intent i = new Intent("jackpal.androidterm.RUN_SCRIPT");
				i.addCategory(Intent.CATEGORY_DEFAULT);
				i.putExtra("jackpal.androidterm.iInitialCommand", varargsToString(args));
				mConsole.startActivity(i);
			} else {
				TerminalNotInstalledDialog tnid = new TerminalNotInstalledDialog();
				tnid.show(mConsole.getFragmentManager(), "tnid");
			}
		}
	};
	private static Map<String, Command> mCommandMap = mapOfInstances(Command.class);

	//List of Keywords
	private static Keyword red = new Keyword("red", "toast", PrettyPrinter.RED);
	private static Keyword green = new Keyword("green", "toast", PrettyPrinter.GREEN);
	private static Keyword blue = new Keyword("blue", "toast", PrettyPrinter.BLUE);
	private static Map<String, Keyword> mKeywordMap = mapOfInstances(Keyword.class);

	public CommandDispatcher(ConsoleActivity console) {
		mConsole = console;
	}

	public void runOnConsole(String commandName, String... args) {
		Command command = mCommandMap.get(commandName);
		if (command != null) {
			runOnConsole(command, args);
		} else {
			mConsole.appendConsoleEntry("Error: " + commandName + " is not a valid command.");
		}
	}

	private void runOnConsole(Command command, String... args) {
		String commandString = command.getCommandName()
				+ " " + varargsToString(args);
		mConsole.addConsoleEntry(commandString);
		mConsole.addCommandEntry(command.getCommandName());

		if (command.hasLowerArgBound()) {
			if (args.length < command.getArgsNum()) {
				mConsole.appendConsoleEntry("Error: " + command.getCommandName() +
						" requires at least " + command.getArgsNum() +
						(command.getArgsNum() == 1 ? " argument." :
								" arguments."));
				return;
			}
		} else if (args.length != command.getArgsNum()) {
			mConsole.appendConsoleEntry("Error: " + command.getCommandName() +
					" requires exactly " + command.getArgsNum() +
					(command.getArgsNum() == 1 ? " argument." :
							" arguments."));
			return;
		}
		command.run(args);
	}

	public void runKeywordCommand(String keywordName, String arg) {
		Keyword keyword = mKeywordMap.get(keywordName);
		if (keyword != null) {
			runOnConsole(keyword.getCommand(), arg);
		} else {
			// Should not happen
			mConsole.appendConsoleEntry("Error: " + keyword + " is not a valid keyword.");
		}
	}

	public static boolean isCommand(String commandName) {
		return mCommandMap.containsKey(commandName);
	}

	public static boolean isKeyword(String keywordName) {
		return mKeywordMap.containsKey(keywordName);
	}

	public static Keyword getKeyword(String keywordName) {
		return mKeywordMap.get(keywordName);
	}

	private static String varargsToString(String... varargs) {
		String newString = "";
		for(String string : varargs) {
			newString += string + " ";
		}
		newString.trim();
		return newString;
	}

	/**
	 * This gets all of this class's instance variables of type instanceType and puts
	 * them into the supplied instanceMap for easy access.
	 */
	@SuppressWarnings("unchecked")
	private static <T> Map<String, T> mapOfInstances(Class<T> instanceType) {
		Map<String, T> instanceMap = new HashMap<String, T>();
		Field[] fields = CommandDispatcher.class.getDeclaredFields();
		for (Field f : fields) {
			if (f.getType().equals(instanceType)) {
				try {
					instanceMap.put(f.getName(), (T) f.get(instanceType));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			} 
		}
		return instanceMap;
	}

	/**
	 * A Command is a series of instructions that is ran when run(args) is called.
	 * A Command can have any number of arguments and may accept at least a
	 * certain number of arguments is it is initialized with a lowerArgBound.
	 */
	public static abstract class Command {

		private String mCommandName;
		private int mArgsNum;
		private boolean mLowerArgBound;

		public Command(String commandName, int minArgs, boolean lowerArgBound) {
			mArgsNum = minArgs;
			mLowerArgBound = lowerArgBound;
			mCommandName = commandName;
		}

		public String getCommandName() {
			return mCommandName;
		}

		public int getArgsNum() {
			return mArgsNum;
		}

		public boolean hasLowerArgBound() {
			return mLowerArgBound;
		}

		protected abstract void run(String... args);

	}

	/**
	 * As opposed to a Command, a Keyword is a word that ConsoleTextView.PrettyPrinter
	 * singles out as important (by coloring it). When a keyword is accessed by long-
	 * clicking a ConsoleTextView, a corresponding command is run.
	 */
	public static class Keyword {

		private String mKeywordName;
		private Command mCommand;
		private String mColor;

		public Keyword(String keywordName, String commandName, String color) {
			mKeywordName = keywordName;
			if (isCommand(commandName)) {
				mCommand = mCommandMap.get(commandName);
			} else {
				mCommand = toast;
			}
			mColor = color;
		}

		public String getKeywordName() {
			return mKeywordName;
		}

		public Command getCommand() {
			return mCommand;
		}

		public String getColor() {
			return mColor;
		}
	}

}
