package eubr.atmosphere.tma.actuator;

import jep.Jep;
import jep.JepException;

public class teste {

	public static void main(String[] args) {
		try (Jep jep = new Jep(false,
				"/home/jorge/workspace/sts/tma-framework-e/development/actuators/demo-actuator-java/src/main/java/eubr/atmosphere/tma/actuator/docker-actuator.py")) {
			jep.runScript("docker-actuator.py");
			Object ret = jep.invoke("execute_action", "DECREASE_CPU");
			if ((Boolean) ret) {
				System.out.println("--- Ok!");	
			} else {
				System.out.println("--- Problem when performing the action!");
			}
			
		} catch (JepException e) {
			e.printStackTrace();
		}

//		try (Interpreter interp = new SharedInterpreter()) {
//			String s = String.join("\n", "import subprocess", "import docker", "import sys", "", "def run(cmd):", "");
//
//			interp.exec("import docker");
//			interp.exec("cli = docker.from_env()");
//
//		} catch (JepException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

}
