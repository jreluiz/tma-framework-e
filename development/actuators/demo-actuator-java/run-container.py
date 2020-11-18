import subprocess
import docker
import sys


def run(cmd):
    res = subprocess.check_output(cmd, shell=True)
    return res

def get_container(container_name):
    try:
        # connect to docker
        cli = docker.from_env()
        # get container
        container = cli.containers.get("priva")
        return container
    except Exception as e:
        print("---Erro ao conectar ao container {} - {}".format(container_name, str(e)))
        return None


# Execute the action
def execute_action(action):
    print("Action received: {}".format(action))

    try:
        if action == "run_container":
            run("docker start priva")
        else:
            print('---Action not valid')
            return False
    except Exception as e:
        print("Error {}".format(str(e)))
        return False

    return True

