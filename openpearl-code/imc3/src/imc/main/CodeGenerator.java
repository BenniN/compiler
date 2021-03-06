package imc.main;
/*
 [A "BSD license"]
 Copyright (c) 2016 Rainer Mueller
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import imc.types.Association;
import imc.types.Module;
import imc.types.ModuleEntrySystemPart;
import imc.types.Platform;
import imc.types.PlatformSystemElement;
import imc.utilities.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * create C++ code for the detected elements
 * 
 * The order of definitions in the source code does not strictly depend on the
 * sequence in the PEARL code. The order is dominated by the sequence of
 * definition, which is overwritten if forward usage is detected.
 * 
 * @author mueller
 * 
 */

/**
 * The code generater class creates the C++-file with the system information
 * 
 * OpenPEARL make an intensive use of static objects. The sequence instantiation
 * of these objects in not defined across compilation units. User dation on
 * module level are static objects, which need an already instantiated system
 * device. To ensure the instantiation of these system device, they must be
 * defined static in an simple function with is called to initialize a simple
 * variable in the user module like: <pre>int x=SystemPart::createDevices();</pre>
 * 
 * The file with the system part consists of the following elements: 
 * <ol>
 * <li> a header section
 * <li> simple object definitions and pointer to system devices 
 * <li>  * a function 'createDevices()'
 * <li> with a body defining the system devices 
 * <li> * function and module termination
 * 
 * part 2 and 4 are created in strings and printed, when the system information
 * is processed
 * 
 * @author mueller
 * 
 */
public class CodeGenerator {
	private static StringBuilder prototypes = new StringBuilder();
	private static StringBuilder functionBody = new StringBuilder();
	private static Module module;
	private static boolean useNamespace;
	private static int autoNumber=0;
	/**
	 * create to output file with to complete content
	 * 
	 * if errors like loops in definition are detected, an error message occurs
	 */
	public static void create(String outputFile, List<Module> modules, boolean useNamespace) {
		CodeGenerator.useNamespace = useNamespace;
		PrintWriter file;

		try {
			file = new PrintWriter(new FileWriter(outputFile));
		} catch (IOException e) {
			System.err.println("could not create output file");

			e.printStackTrace();
			return;
		}
		file.println("// [automatic generated by intermodule checker (imc v3) -- do not change ]");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		file.println("// "+dateFormat.format(date));
		file.println("// use of namespace is: "+useNamespace+"\n");
		file.println("#include \"PearlIncludes.h\" \n");


		instantiateLogFirst(modules);
		instantiateRemainingSystemElements(modules);

		/*		// instantiate Log first
		for (int i = 0; i < SystemEntries.size(); i++) {
			SystemEntry un = SystemEntries.get(i);
			if (un.getSystemName().equals("Log")) {
				Error.setLocation(un.getFileName(), un.getLine());
				doAllPrerequistes(un,10);
			}	
		}

		boolean otherNamesNeeded;
		boolean newCodeCreated;
		do {
			otherNamesNeeded = false;
			newCodeCreated = false;
			for (int i = 0; i < SystemEntries.size(); i++) {
				SystemEntry un = SystemEntries.get(i);
				if (!un.codeIsCompleted()) {
					SystemEntry req = un.requiresOtherSystemEntry();
					if (req == null) {
						un.getCompleteCode(simpleElements, functionBody);
						newCodeCreated = true;
					} else {
						// System.out.println("need "+req.getName()+" first");
						otherNamesNeeded = true;
					}
				}
				// }
			}
		} while (otherNamesNeeded == true && newCodeCreated == true);
		if (otherNamesNeeded == true && newCodeCreated == false) {
			for (int i = 0; i < SystemEntries.size(); i++) {
				SystemEntry un = SystemEntries.get(i);
				if (un.getType() != null) {
					if (!un.codeIsCompleted()) {
						Error.setLocation(un.getFileName(), un.getLine());
						Error.error("circular dependency detected at the definition of "
								+ un.getName());
						Error.append("needs "
								+ un.requiresOtherSystemEntry().getName());
					}
				}
			}

		}
		 */
		file.println("\n\nnamespace pearlrt {\n   int createSystemElements();");
		file.println("}\n");
		file.println("// enshure that the static (parametrized) objects are instantiated first");
		file.println("static int dummy = pearlrt::createSystemElements();");
		file.println();
		file.println(prototypes);
		file.println("\n\nnamespace pearlrt {\n   int createSystemElements() {");
		file.println(functionBody);
		file.println("\treturn 0;");
		file.println("   }\n}");
		file.close();

	}

	private static void instantiateRemainingSystemElements(List<Module> modules) {
		boolean newCodeGenerated=false;
		boolean systemElementsPending = false;
		for (Module m: modules) {
			module = m;
			if (useNamespace) {
				prototypes.append("namespace "+module.getModuleName()+ " {\n");
			}
			do {
				for (ModuleEntrySystemPart se: m.getSystemElements()) {
					if (!se.isCodeGenerated()) {
						doAllPrerequisites(se, 10);
						Association a = se.getAssociation();
						if (a != null) {
							ModuleEntrySystemPart mse = ((Association)a).getUsername();
							if (!mse.isCodeGenerated()) {
								systemElementsPending = true;
							}
						}
					}
				}
			} while (systemElementsPending && newCodeGenerated);
			if (useNamespace) {
				prototypes.append("}\n\n");
			}

			if (systemElementsPending) {
				Log.error("circular dependency detected");
			}
		}

	}

	private static void instantiateLogFirst(List<Module> modules) {
		boolean logFound = false;
		for (Module m: modules) {
			module = m;

			for (ModuleEntrySystemPart se: m.getSystemElements()) {
				if (se.getNameOfSystemelement().equals("Log")) {
					if (logFound) {
						Log.setLocation(m.getSourceFileName(), se.getLine());
						Log.error("multiple definition of Log");
					}
					if (useNamespace) {
						prototypes.append("namespace "+module.getModuleName()+ " {\n");
					}
					logFound = true;
					doAllPrerequisites(se, 10);  // max depth of recursion is 10
					//addToCodeParts(se);
					if (useNamespace) {
						prototypes.append("}\n\n");
					}
				}
			}

		}
	}

	private static void doAllPrerequisites(ModuleEntrySystemPart se, int remainingDepth) {
		Log.setLocation(module.getSourceFileName(),se.getLine());
		Log.info("treat "+se.getNameOfSystemelement());
		if (remainingDepth >= 0) {
			Association a = se.getAssociation();
			if (a==null) {
				se.setCodeGenerated(true);
				addToCodeParts(se);

			}

			if (a != null) {
				ModuleEntrySystemPart mse = a.getUsername();
				if (!mse.isCodeGenerated()) {
				   doAllPrerequisites(mse,remainingDepth-1);
					mse.setCodeGenerated(true);
				}
				addToCodeParts(se);
				se.setCodeGenerated(true);
			}

		} else {
			Log.error("too long chain of associations");
		}
	}


	private static void addToCodeParts(ModuleEntrySystemPart se) {
		
		
		String userName;
		if (se.getUserName() != null) {
			userName=se.getPrefix()+se.getUserName();
		} else {
			userName = "configurarion_"+autoNumber++;
		}

		String s= se.getNameOfSystemelement();
		PlatformSystemElement pse = Platform.getInstance().getSystemElement(s);
		String nameSpacePrefix = "";
		if (useNamespace) {
			nameSpacePrefix = module.getModuleName()+"::";
		} 

		if (pse.getType().equals(Platform.DATION)) {
			prototypes.append("\tpearlrt::Device *  d"+userName+";" + locationComment(se));

			functionBody.append("\t// " + module.getSourceFileName()+":"+se.getLine()+"\n");
			functionBody.append("\tstatic pearlrt::"+se.getNameOfSystemelement()+" "+nameSpacePrefix+"s"+userName
					+ parameterList(se)+";\n");
			functionBody.append("\t"+nameSpacePrefix+"d"+userName + "= &"+nameSpacePrefix + "s"+userName+";\n\n");
		} else if (pse.getType().equals(Platform.CONNECTION)) {
			
			functionBody.append("\t// " + module.getSourceFileName()+":"+se.getLine()+"\n");
			functionBody.append("\tstatic pearlrt::"+se.getNameOfSystemelement()+" "+nameSpacePrefix+"s"+userName
					+ parameterList(se)+";\n");
		} else  if (pse.getType().equals(Platform.CONFIGURATION)) {
			functionBody.append("\t// " + module.getSourceFileName()+":"+se.getLine()+"\n");
			functionBody.append("\tstatic pearlrt::"+se.getNameOfSystemelement()+" "+
					nameSpacePrefix+userName+parameterList(se)+";\n");
		} else if (pse.getType().equals(Platform.INTERRUPT)) {
			prototypes.append("\tpearlrt::Interrupt *"+ userName + ";" + locationComment(se));
			functionBody.append("\t// " + module.getSourceFileName()+":"+se.getLine()+"\n");
			functionBody.append("\tstatic pearlrt::"+se.getNameOfSystemelement()+" "+nameSpacePrefix + "sys"+userName
					+ parameterList(se)+";\n");
			functionBody.append("\t"+nameSpacePrefix + userName + "= (pearlrt::Interrupt*) &"+nameSpacePrefix +"sys"+userName+";\n\n");

		} else if (pse.getType().equals(Platform.SIGNAL)) {
			prototypes.append("\tstatic pearlrt::"+se.getNameOfSystemelement()+ " "+nameSpacePrefix +userName+";"+locationComment(se));
			prototypes.append("\tpearlrt::Signal "+ nameSpacePrefix+"generalized"+userName+"= & "+nameSpacePrefix+userName+";\n\n");
		} else {
			Log.error("unsupported type: "+pse.getType());
		}


	}

	private static String locationComment(ModuleEntrySystemPart se) {
		String s = "\t\t// "+module.getSourceFileName()+":"+se.getLine()+"\n";
		return s;
	}



	private static String parameterList(ModuleEntrySystemPart se) {
		StringBuilder sb = new StringBuilder();
		Association a = se.getAssociation();
		boolean firstParameter = true;
		if (se.getParameters().size() > 0 || se.getAssociation() != null) {
			sb.append("(");
			if (a != null) {
				ModuleEntrySystemPart mse = a.getUsername();
				sb.append("& s"+mse.getPrefix()+mse.getUserName());
				firstParameter = false;
			}
			for (int i=0; i<se.getParameters().size(); i++){
				if (! firstParameter) sb.append(", ");
				sb.append(se.getParameters().get(i).getCppCode());
				firstParameter = false;
			}
			sb.append(")");
		}
		return sb.toString();
	}

}
